package com.towerofdarkness.app

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatBeat
import com.towerofdarkness.app.domain.combat.CombatAnimStyle
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CombatEngineTest {
    private fun fiveCards() = CardCatalog.defaultLoadoutIds.mapNotNull { CardCatalog.byId(it) }
    private fun tank() = Enemy(EnemyKind.ORC, maxHp = 500, hp = 500, isBoss = false)

    @Test
    fun bossHasConfiguredHp() {
        val boss = Enemy.boss()
        assertTrue(boss.maxHp == Balance.BOSS_HP)
        assertTrue(boss.isBoss)
    }

    @Test
    fun bossCounterBand_is6to9() {
        assertEquals(6, Balance.BOSS_COUNTER_MIN)
        assertEquals(9, Balance.BOSS_COUNTER_MAX)
        assertEquals(28, Balance.BOSS_HP)
    }

    @Test
    fun loadoutCapIsFive() {
        assertEquals(5, Balance.LOADOUT_MIN)
        assertEquals(5, Balance.LOADOUT_MAX)
        assertEquals(5, fiveCards().size)
    }

    @Test
    fun exhaust_onlyUnspentPicked_cycleResets() {
        val cards = fiveCards()
        val engine = CombatEngine(Random(7))
        var s = engine.start(cards, tank(), WeaponRuntime(WeaponCatalog.ashbrand), maxHp = 99, playerHp = 99)
        val fired = mutableListOf<String>()
        repeat(5) {
            s = engine.diceTumble(s)
            val id = s.highlightedId
            assertNotNull("dice should highlight a slot", id)
            assertFalse("picked spent $id", id in fired)
            fired += id!!
            s = engine.resolveSkill(s)
            if (s.awaitingWeapon) s = engine.resolveWeapon(s)
            assertFalse("tank should survive first cycle", s.finished)
            s = engine.resolveEnemy(s)
            assertFalse(s.finished)
            s = engine.readyNext(s)
        }
        assertEquals(5, fired.distinct().size)
        assertEquals(5, s.spentIds.size)
        s = engine.diceTumble(s)
        assertTrue(s.log.any { it.message.contains("Cycle reset") })
        assertTrue(s.spentIds.isEmpty())
        assertNotNull(s.highlightedId)
    }

    @Test
    fun weapon_attackAddsCharge_fullWakeResets() {
        val cards = fiveCards()
        val engine = CombatEngine(Random(1))
        var s = engine.start(
            cards,
            tank(),
            WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0),
            maxHp = 99,
            playerHp = 99
        )
        var sawFull = false
        repeat(16) {
            if (s.finished) return@repeat
            s = engine.diceTumble(s)
            s = engine.resolveSkill(s)
            if (s.awaitingWeapon) {
                val wasFull = s.weapon.charge >= s.weapon.threshold
                s = engine.resolveWeapon(s)
                if (wasFull) {
                    sawFull = true
                    assertEquals(0, s.weapon.charge)
                    assertTrue(s.fullProcThisCombat)
                }
            }
            if (!s.finished && s.enemy.hp > 0) {
                s = engine.resolveEnemy(s)
                if (!s.finished) s = engine.readyNext(s)
            }
        }
        assertTrue("expected a full Wake within 16 rounds", sawFull)
    }

    @Test
    fun dice_weightedAmongUnspentOnly() {
        val cards = fiveCards()
        val engine = CombatEngine(Random(99))
        var s = engine.start(cards, tank(), WeaponRuntime(WeaponCatalog.ashbrand), 99, 99)
        s = engine.diceTumble(s)
        val first = s.highlightedId!!
        s = engine.resolveSkill(s)
        if (s.awaitingWeapon) s = engine.resolveWeapon(s)
        s = engine.resolveEnemy(s)
        s = engine.readyNext(s)
        // Force only one live by marking others spent artificially via 4 more fires
        val seen = mutableSetOf(first)
        repeat(4) {
            s = engine.diceTumble(s)
            val id = s.highlightedId!!
            assertFalse(id in seen)
            seen += id
            s = engine.resolveSkill(s)
            if (s.awaitingWeapon) s = engine.resolveWeapon(s)
            s = engine.resolveEnemy(s)
            s = engine.readyNext(s)
        }
        assertEquals(5, seen.size)
    }

    @Test
    fun fightTerminatesWithContinueBeat() {
        val cards = fiveCards()
        val engine = CombatEngine(Random(42))
        var s = engine.start(cards, Enemy.normal(), WeaponRuntime(WeaponCatalog.ashbrand))
        var guard = 0
        while (!s.finished && guard < 80) {
            s = engine.diceTumble(s)
            if (s.highlightedId == null) break
            s = engine.resolveSkill(s)
            if (s.awaitingWeapon) s = engine.resolveWeapon(s)
            if (s.finished) break
            if (s.enemy.hp <= 0) break
            s = engine.resolveEnemy(s)
            if (!s.finished) s = engine.readyNext(s)
            guard++
        }
        assertTrue(s.finished || s.enemy.hp <= 0 || s.playerHp <= 0)
        if (s.finished) assertEquals(CombatBeat.AWAITING_CONTINUE, s.beat)
    }


    @Test
    fun weapon_fullWakeAndSpark_sameBeat() {
        val cards = fiveCards()
        val engine = CombatEngine(Random(0))
        var s = engine.start(
            cards,
            tank(),
            WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 4),
            maxHp = 99,
            playerHp = 99
        )
        // Simulate post-skill queue: CHAIN full + SPARK both pending
        s = s.copy(
            awaitingWeapon = true,
            pendingFullWake = true,
            pendingSpark = true
        )
        s = engine.resolveWeapon(s)
        val msgs = s.log.map { it.message }
        assertTrue(
            "expected full Wake in log: $msgs",
            msgs.any { it.startsWith("ASHBRAND — WAKE") }
        )
        assertTrue(
            "expected SPARK in same beat log: $msgs",
            msgs.any { it.contains("spark", ignoreCase = true) }
        )
        assertEquals("full Wake resets charge", 0, s.weapon.charge)
        assertTrue(s.fullProcThisCombat)
        assertFalse(s.pendingFullWake)
        assertFalse(s.pendingSpark)
    }

    @Test
    fun weapon_sparkRollIndependentOfFull_viaSkill() {
        // Scripted RNG: nextFloat for spark always hits (0f); nextInt for enemy unused here
        val floats = ArrayDeque(listOf(0f)) // spark roll
        val scripted = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextFloat(): Float =
                if (floats.isNotEmpty()) floats.removeFirst() else 0.99f
            override fun nextInt(until: Int): Int = until / 2
        }
        val cards = fiveCards()
        val engine = CombatEngine(scripted)
        var s = engine.start(
            cards,
            tank(),
            WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 2),
            maxHp = 99,
            playerHp = 99
        )
        // Force an attack skill highlight — +1 charge reaches Lv1 threshold 3
        val attack = cards.first { it.id == "hostflint" || it.title == "Hostflint" }
        s = s.copy(lastFiredCard = attack, highlightedId = attack.id, beat = CombatBeat.AFTER_DICE)
        s = engine.resolveSkill(s)
        assertTrue("charge should hit threshold", s.weapon.charge >= s.weapon.threshold)
        assertTrue(s.pendingFullWake)
        assertTrue("spark must roll independent of full", s.pendingSpark)
        s = engine.resolveWeapon(s)
        val msgs = s.log.map { it.message }
        assertTrue(msgs.any { it.startsWith("ASHBRAND — WAKE") })
        assertTrue(msgs.any { it.contains("spark", ignoreCase = true) })
        assertEquals(0, s.weapon.charge)
    }


    @Test
    fun wake_lv1FullAtCharge3() {
        assertEquals(3, WeaponCatalog.ashbrand.threshold(1))
        assertEquals(2, WeaponCatalog.ashbrand.threshold(2))
        assertEquals(2, WeaponCatalog.ashbrand.threshold(3))
        val engine = CombatEngine(Random(0))
        var s = engine.start(
            fiveCards(),
            tank(),
            WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 3),
            99, 99
        )
        s = s.copy(awaitingWeapon = true, pendingFullWake = true, pendingSpark = false)
        s = engine.resolveWeapon(s)
        assertEquals(0, s.weapon.charge)
        assertTrue(s.fullProcThisCombat)
        val wake = s.log.map { it.message }.first { it.startsWith("ASHBRAND — WAKE") }
        assertEquals("ASHBRAND — WAKE 4", wake) // Lv1 full dmg
    }

    @Test
    fun wake_exactLogLine_andSparkNoGoldAnim() {
        val engine = CombatEngine(Random(0))
        var s = engine.start(
            fiveCards(),
            tank(),
            WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 3),
            99, 99
        )
        s = s.copy(awaitingWeapon = true, pendingFullWake = true, pendingSpark = true)
        s = engine.resolveWeapon(s)
        val wakeEv = s.log.first { it.message.startsWith("ASHBRAND — WAKE") }
        assertEquals("ASHBRAND — WAKE 4", wakeEv.message)
        assertTrue(wakeEv.goldLog)
        assertEquals("WAKE", wakeEv.floating?.text)
        assertEquals(CombatAnimStyle.CHARGE_SHAKE_SLOWMO, wakeEv.animStyle)

        val sparkEv = s.log.first { it.message.contains("spark", ignoreCase = true) }
        assertEquals("Ashbrand spark (2)", sparkEv.message)
        assertFalse(sparkEv.goldLog)
        assertEquals(null, sparkEv.floating)
        assertEquals(CombatAnimStyle.QUICK, sparkEv.animStyle)
    }

    @Test
    fun trashCounter_notEleven() {
        assertEquals(7, Balance.ENEMY_COUNTER_MIN)
        assertEquals(9, Balance.ENEMY_COUNTER_MAX)
        EnemyKind.entries.filter { it != EnemyKind.DRAGON && it != EnemyKind.ASH_WARDEN }.forEach { k ->
            assertEquals("goblin/orc/spider/troll min", 7, k.trashCounterMin)
            assertEquals("goblin/orc/spider/troll max", 9, k.trashCounterMax)
            assertTrue("${k} max!=11", k.trashCounterMax != 11)
        }
        assertEquals(6, Balance.BOSS_COUNTER_MIN)
        assertEquals(9, Balance.BOSS_COUNTER_MAX)
        assertEquals(28, Balance.BOSS_HP)
    }
}
