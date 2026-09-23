package com.towerofdarkness.app

import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.BraceDrawSync
import com.towerofdarkness.app.domain.combat.BraceHitDrawPhase
import com.towerofdarkness.app.domain.combat.CombatBeat
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.CombatEvent
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.StatusPips
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * v0.1.14-bracesync — pip → absorb float → HP draw order; zero-hold; no math change.
 */
class BraceSyncV0114Test {

    private fun card(id: String) = CardCatalog.byId(id)!!
    private fun tank() = Enemy(EnemyKind.ORC, maxHp = 500, hp = 500, isBoss = false)
    private fun weapon() = WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0)

    private fun forceFire(
        engine: CombatEngine,
        ids: List<String>,
        wantId: String
    ): com.towerofdarkness.app.domain.combat.CombatState {
        val cards = ids.map { card(it) }
        var s = engine.start(cards, tank(), weapon(), maxHp = 99, playerHp = 99)
        repeat(12) {
            if (s.finished) return s
            s = engine.diceTumble(s)
            if (s.highlightedId == wantId) return s
            s = engine.resolveSkill(s)
            if (s.awaitingWeapon) s = engine.resolveWeapon(s)
            if (s.finished || s.enemy.hp <= 0) return s
            s = engine.resolveEnemy(s)
            if (!s.finished) s = engine.readyNext(s)
        }
        return s
    }

    @Test
    fun pipTickBeforeHpBar_drawOrderIsPipThenFloatThenHp() {
        assertEquals(
            listOf(
                BraceHitDrawPhase.PIP_UPDATE,
                BraceHitDrawPhase.ABSORB_FLOAT,
                BraceHitDrawPhase.HP_BAR
            ),
            BraceDrawSync.hitDrawOrder()
        )
        val absorbHit = CombatEvent("Orc hits for 3 (2 Brace)", braceAbsorbed = 2)
        assertTrue(BraceDrawSync.delayHpBarAfter(absorbHit))
        val plainHit = CombatEvent("Orc hits for 8", braceAbsorbed = 0)
        assertFalse(BraceDrawSync.delayHpBarAfter(plainHit))
    }

    @Test
    fun absorbFloatShown_onHitEvent() {
        val with = CombatEvent("Orc hits for 3 (2 Brace)", braceAbsorbed = 2)
        assertEquals(2, BraceDrawSync.absorbFloat(with))
        val without = CombatEvent("Orc hits for 8", braceAbsorbed = 0)
        assertNull(BraceDrawSync.absorbFloat(without))
    }

    @Test
    fun braceZero_visibleOneBeatThenGone() {
        // Find seed where Brace 3 is fully spent by counter
        var matched = false
        for (seed in 0..8000) {
            val engine = CombatEngine(Random(seed))
            var s = engine.start(
                listOf(card("hostflint"), card("emberbrand"), card("ruin_seal"), card("tower_pike"), card("ash_press")),
                tank(),
                weapon(),
                maxHp = 99,
                playerHp = 99
            )
            s = s.copy(brace = 3, counterPenalty = 0)
            val hpBefore = s.playerHp
            s = engine.resolveEnemy(s)
            if (s.finished) continue
            if (s.brace != 0) continue
            val hit = s.log.last { it.message.contains("hits for") }
            assertTrue(hit.braceAbsorbed > 0)
            assertEquals(3, hit.braceAbsorbed)
            // Zero-hold this beat
            val pips = StatusPips.forPlayer(s)
            assertEquals(1, pips.size)
            assertEquals("brace", pips[0].term)
            assertEquals(0, pips[0].count)
            assertTrue(BraceDrawSync.holdBraceZero(s))
            assertEquals(CombatBeat.AFTER_ENEMY, s.beat)
            // Next beat hides
            s = engine.readyNext(s)
            assertEquals(0, s.brace)
            assertTrue(StatusPips.forPlayer(s).isEmpty())
            assertFalse(BraceDrawSync.holdBraceZero(s))
            // Leftover HP math still applied (absorb 3 → dmg reduced by 3)
            assertEquals(hpBefore - (hit.message.substringAfter("hits for ").substringBefore(" ").toInt()), s.playerHp)
            matched = true
            break
        }
        assertTrue("expected a seed where Brace 3 is fully spent", matched)
    }

    @Test
    fun sameBeat_skillBeforeHit_braceAppliesBeforeAbsorb() {
        // Iron Mantle → Brace 3, then enemy hit uses that Brace (resolver order unchanged)
        val engine = CombatEngine(Random(42))
        var s = forceFire(
            engine,
            listOf("iron_mantle", "hostflint", "emberbrand", "ruin_seal", "tower_pike"),
            "iron_mantle"
        )
        assertEquals("iron_mantle", s.highlightedId)
        s = engine.resolveSkill(s)
        assertEquals(3, s.brace)
        assertEquals(3, StatusPips.forPlayer(s).single().count)
        if (s.awaitingWeapon) s = engine.resolveWeapon(s)
        val braceBeforeHit = s.brace
        s = engine.resolveEnemy(s)
        val hit = s.log.last { it.message.contains("hits for") }
        assertTrue(
            "skill-before-hit must absorb with pre-applied Brace",
            hit.braceAbsorbed > 0 || braceBeforeHit == 0
        )
        assertEquals(braceBeforeHit - hit.braceAbsorbed, s.brace)
        // Pip mirrors post-absorb (including 0-hold)
        if (s.brace > 0) {
            assertEquals(s.brace, StatusPips.forPlayer(s).single().count)
        } else if (hit.braceAbsorbed > 0) {
            assertEquals(0, StatusPips.forPlayer(s).single().count)
        }
    }

    @Test
    fun skillAfterHit_noInventedPreBlock() {
        // Enemy hits first with no Brace; later Iron Mantle must NOT retro-absorb
        val engine = CombatEngine(Random(11))
        var s = engine.start(
            listOf(card("iron_mantle"), card("hostflint"), card("emberbrand"), card("ruin_seal"), card("tower_pike")),
            tank(),
            weapon(),
            maxHp = 99,
            playerHp = 99
        )
        assertEquals(0, s.brace)
        s = engine.resolveEnemy(s)
        val hit = s.log.last { it.message.contains("hits for") }
        assertEquals(0, hit.braceAbsorbed)
        assertNull(BraceDrawSync.absorbFloat(hit))
        assertFalse(BraceDrawSync.delayHpBarAfter(hit))
        val hpAfterNakedHit = s.playerHp
        // Apply Iron Mantle after the hit — Brace appears but does not rewrite past damage
        s = s.copy(brace = 3, beat = CombatBeat.AFTER_SKILL)
        assertEquals(3, StatusPips.forPlayer(s).single().count)
        assertEquals(hpAfterNakedHit, s.playerHp)
        assertEquals(0, s.log.last { it.message.contains("hits for") }.braceAbsorbed)
    }

    @Test
    fun absorbMathUnchanged_minBraceDmg() {
        // Soften 5 → counter in 2..4. Seed where dmg==2: Brace 5 → 3, HP loses 0 from that 2.
        var matched = false
        for (seed in 0..8000) {
            val engine = CombatEngine(Random(seed))
            var s = engine.start(
                listOf(card("hostflint"), card("emberbrand"), card("ruin_seal"), card("tower_pike"), card("ash_press")),
                tank(),
                weapon(),
                maxHp = 99,
                playerHp = 99
            )
            s = s.copy(brace = 5, counterPenalty = 5)
            val hpBefore = s.playerHp
            s = engine.resolveEnemy(s)
            if (s.finished) continue
            if (s.brace != 3) continue
            val hit = s.log.last { it.message.contains("hits for") }
            assertEquals(2, hit.braceAbsorbed)
            // leftover HP dmg = 0 for a fully absorbed 2
            assertEquals(hpBefore, s.playerHp)
            assertTrue(hit.message.contains("hits for 0"))
            assertEquals(3, StatusPips.forPlayer(s).single().count)
            matched = true
            break
        }
        assertTrue("expected seed yielding absorb 2 with Soften 5", matched)
    }

    @Test
    fun softenUnchanged_stillHideAtZero() {
        val engine = CombatEngine(Random(3))
        var s = forceFire(
            engine,
            listOf("cinder_step", "hostflint", "emberbrand", "ruin_seal", "tower_pike"),
            "cinder_step"
        )
        s = engine.resolveSkill(s)
        assertEquals(2, s.counterPenalty)
        assertEquals(2, StatusPips.forEnemy(s).single().count)
        if (s.awaitingWeapon) s = engine.resolveWeapon(s)
        s = engine.resolveEnemy(s)
        assertEquals(0, s.counterPenalty)
        assertTrue(StatusPips.forEnemy(s).isEmpty())
    }
}
