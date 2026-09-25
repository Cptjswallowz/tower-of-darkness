package com.towerofdarkness.app

import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatBeat
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.CombatState
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.EnemyKitRole
import com.towerofdarkness.app.domain.combat.EnemyKits
import com.towerofdarkness.app.domain.combat.EnemyLook
import com.towerofdarkness.app.domain.combat.EnemySkillKind
import com.towerofdarkness.app.domain.combat.StatusPips
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import com.towerofdarkness.app.domain.glossary.Glossary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * v0.1.32-enemykit — kits / Soften / Nip / grey / shared looks. See docs/enemykit-v0132.md.
 */
class EnemyKitV0132Test {

    private fun card(id: String) = CardCatalog.byId(id)!!
    private fun weapon() = WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0)
    private fun loadout() = listOf(
        card("hostflint"), card("emberbrand"), card("ruin_seal"), card("tower_pike"), card("ash_press")
    )

    private fun start(
        enemy: Enemy,
        rng: Random = Random(1),
        hp: Int = 99
    ): Pair<CombatEngine, CombatState> {
        val engine = CombatEngine(rng)
        val s = engine.start(loadout(), enemy, weapon(), maxHp = hp, playerHp = hp)
        return engine to s
    }

    /** Force next enemy roll to [skillId] by spending the other tiles. */
    private fun onlySkill(s: CombatState, skillId: String): CombatState {
        val kit = EnemyKits.skillsFor(s.enemy)
        return s.copy(enemySpentIds = kit.map { it.id }.filter { it != skillId }.toSet())
    }

    @Test
    fun goblin_shivAndNipAppear_hitMostCommon() {
        val kit = EnemyKits.skillsFor(EnemyKind.GOBLIN)
        assertEquals(listOf("shiv", "nip", "hit"), kit.map { it.id })
        assertEquals(2, kit.first { it.id == "shiv" }.weight)
        assertEquals(2, kit.first { it.id == "nip" }.weight)
        assertEquals(5, kit.first { it.id == "hit" }.weight)
        assertEquals(5, kit.first { it.id == "shiv" }.damage)
        assertEquals(3, kit.first { it.id == "nip" }.damage)

        val counts = mutableMapOf("shiv" to 0, "nip" to 0, "hit" to 0)
        repeat(900) { seed ->
            val (engine, base) = start(Enemy.normal(EnemyKind.GOBLIN), Random(seed.toLong()))
            var s = engine.resolveEnemy(base)
            val id = s.enemyHighlightedId!!
            counts[id] = counts.getValue(id) + 1
        }
        assertTrue("Hit should dominate: $counts", counts.getValue("hit") > counts.getValue("shiv"))
        assertTrue("Hit should dominate: $counts", counts.getValue("hit") > counts.getValue("nip"))
        assertTrue("Shiv must appear", counts.getValue("shiv") > 0)
        assertTrue("Nip must appear", counts.getValue("nip") > 0)
    }

    @Test
    fun orcHide_braceUnderOrc_playerHitEatsBrace() {
        val (engine, base) = start(Enemy.normal(EnemyKind.ORC), Random(0))
        var s = onlySkill(base, "hide")
        s = engine.resolveEnemy(s)
        assertEquals("hide", s.enemyHighlightedId)
        assertEquals(3, s.enemyBrace)
        val pips = StatusPips.forEnemy(s)
        assertTrue(pips.any { it.term == "brace" && it.count == 3 })
        assertTrue(s.log.any { it.message.contains("Sturdy Orc — Hide 3") })

        // Player Hostflint 5 eats Brace 3 → enemy HP −2, Brace 0
        val host = card("hostflint")
        val hpBefore = s.enemy.hp
        s = s.copy(lastFiredCard = host, highlightedId = host.id, beat = CombatBeat.AFTER_DICE)
        s = engine.resolveSkill(s)
        assertEquals(0, s.enemyBrace)
        assertEquals(hpBefore - 2, s.enemy.hp)
        assertTrue(StatusPips.forEnemy(s).none { it.term == "brace" })
    }

    @Test
    fun softenThenCleave_minusOne_clearsSoften() {
        val (engine, base) = start(Enemy.normal(EnemyKind.ORC), Random(0))
        var s = onlySkill(base, "cleave").copy(counterPenalty = 1)
        assertEquals(1, StatusPips.forEnemy(s).single { it.term == "soften" }.count)
        s = engine.resolveEnemy(s)
        assertEquals("cleave", s.enemyHighlightedId)
        assertEquals(0, s.counterPenalty)
        assertTrue(StatusPips.forEnemy(s).none { it.term == "soften" })
        assertTrue(
            "Cleave 8 Soften −1 → 7: ${s.log.map { it.message }}",
            s.log.any { it.message.contains("Sturdy Orc — Cleave 7") }
        )
    }

    @Test
    fun softenThenHide_softenPipStays() {
        val (engine, base) = start(Enemy.normal(EnemyKind.ORC), Random(0))
        var s = onlySkill(base, "hide").copy(counterPenalty = 2)
        s = engine.resolveEnemy(s)
        assertEquals("hide", s.enemyHighlightedId)
        assertEquals(2, s.counterPenalty)
        assertEquals(3, s.enemyBrace)
        val terms = StatusPips.forEnemy(s).map { it.term }.toSet()
        assertTrue(terms.contains("soften"))
        assertTrue(terms.contains("brace"))
        assertEquals(2, StatusPips.forEnemy(s).first { it.term == "soften" }.count)
    }

    @Test
    fun nipLog_ignoresSoften_doesNotConsume() {
        val (engine, base) = start(Enemy.normal(EnemyKind.GOBLIN), Random(0))
        var s = onlySkill(base, "nip").copy(counterPenalty = 2)
        val hpBefore = s.playerHp
        s = engine.resolveEnemy(s)
        assertEquals("nip", s.enemyHighlightedId)
        assertEquals(2, s.counterPenalty) // Soften stays
        assertTrue(
            s.log.any {
                it.message.contains("Weak Goblin — Nip 3") &&
                    it.message.contains("ignores Soften")
            }
        )
        assertEquals(hpBefore - 3, s.playerHp)
        assertEquals(2, StatusPips.forEnemy(s).single { it.term == "soften" }.count)
    }

    @Test
    fun tapCleave_glossaryDef() {
        assertNotNull(Glossary.definition("cleave"))
        assertTrue(Glossary.definition("cleave")!!.isNotBlank())
        assertEquals(Glossary.definition("brace"), Glossary.definition("hide"))
        assertEquals(Glossary.definition("brace"), Glossary.definition("rust guard"))
        assertEquals(Glossary.definition("brace"), Glossary.definition("cinder hide"))
        val nip = Glossary.definition("nip")!!
        assertTrue(nip.contains("Soften does not reduce", ignoreCase = true))
    }

    @Test
    fun greyTile_noRerollOfThatTile() {
        val (engine, base) = start(Enemy.normal(EnemyKind.GOBLIN), Random(42))
        var s = engine.resolveEnemy(base)
        val first = s.enemyHighlightedId!!
        assertTrue(first in s.enemySpentIds)
        val seen = mutableSetOf(first)
        repeat(2) {
            s = engine.readyNext(s)
            s = engine.resolveEnemy(s)
            val id = s.enemyHighlightedId!!
            assertFalse("must not re-pick spent before full cycle: picked=$id spent=${s.enemySpentIds}", id in seen)
            seen += id
        }
        assertEquals(3, seen.size)
        assertEquals(3, s.enemySpentIds.size)
        s = engine.readyNext(s)
        s = engine.resolveEnemy(s)
        assertTrue(s.log.any { it.message.contains("Enemy cycle reset") })
        assertEquals(1, s.enemySpentIds.size)
        assertNotNull(s.enemyHighlightedId)
    }

    @Test
    fun threeGoblinLooks_shareKit() {
        val knife = EnemyKits.skillsFor(Enemy.normal(EnemyKind.GOBLIN, look = EnemyLook.KNIFE))
        val bottle = EnemyKits.skillsFor(Enemy.normal(EnemyKind.GOBLIN, look = EnemyLook.BOTTLE))
        val spikes = EnemyKits.skillsFor(Enemy.normal(EnemyKind.GOBLIN, look = EnemyLook.SPIKES))
        assertEquals(knife.map { it.id to it.weight }, bottle.map { it.id to it.weight })
        assertEquals(knife.map { it.id to it.weight }, spikes.map { it.id to it.weight })
        assertEquals(EnemyKitRole.WEAK_GOBLIN, EnemyKitRole.fromEnemy(Enemy.normal(EnemyKind.GOBLIN, look = EnemyLook.SPIKES)))
    }

    @Test
    fun wakeAndPlayerBar_unchanged() {
        // Player exhaust machine still 5 cards; Wake line still exact
        val (engine, base) = start(Enemy(EnemyKind.ORC, 500, 500, false), Random(0))
        var s = base.copy(
            weapon = WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 3),
            awaitingWeapon = true,
            pendingFullWake = true,
            pendingSpark = false
        )
        s = engine.resolveWeapon(s)
        assertEquals("ASHBRAND — WAKE 4", s.log.first { it.message.startsWith("ASHBRAND — WAKE") }.message)
        assertEquals(0, s.weapon.charge)

        // Player bar dice still picks among unspent only
        s = engine.start(loadout(), Enemy(EnemyKind.ORC, 500, 500), weapon(), 99, 99)
        val fired = mutableSetOf<String>()
        repeat(5) {
            s = engine.diceTumble(s)
            val id = s.highlightedId!!
            assertFalse(id in fired)
            fired += id
            s = engine.resolveSkill(s)
            if (s.awaitingWeapon) s = engine.resolveWeapon(s)
            s = engine.resolveEnemy(s)
            s = engine.readyNext(s)
        }
        assertEquals(5, fired.size)
    }

    @Test
    fun kits_matchLockTable() {
        assertEquals(9, EnemyKits.totalWeight(EnemyKitRole.WEAK_GOBLIN))
        assertEquals(9, EnemyKits.totalWeight(EnemyKitRole.STURDY_ORC))
        val orc = EnemyKits.sturdyOrc
        assertEquals(8, orc.first { it.id == "cleave" }.damage)
        assertEquals(3, orc.first { it.id == "hide" }.braceGain)
        assertEquals(EnemySkillKind.BRACE, orc.first { it.id == "hide" }.kind)
        val seal = EnemyKits.sealWarden
        assertEquals(7, seal.first { it.id == "seal_pulse" }.damage)
        assertEquals(4, seal.first { it.id == "rust_guard" }.braceGain)
        val ash = EnemyKits.ashWarden
        assertEquals(9, ash.first { it.id == "coal_slam" }.damage)
        assertEquals(3, ash.first { it.id == "cinder_hide" }.braceGain)
        assertEquals(EnemyKitRole.SEAL_WARDEN, EnemyKitRole.fromKind(EnemyKind.DRAGON))
        assertEquals(EnemyKitRole.ASH_WARDEN, EnemyKitRole.fromKind(EnemyKind.ASH_WARDEN))
    }
}
