package com.towerofdarkness.app

import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
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
 * v0.1.13-statuspips — Brace / Soften pips mirror combat state; no damage retune.
 */
class StatusPipsV0113Test {

    private fun card(id: String) = CardCatalog.byId(id)!!
    private fun tank() = Enemy(EnemyKind.ORC, maxHp = 500, hp = 500, isBoss = false)
    private fun weapon() = WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0)

    private fun forceFire(engine: CombatEngine, ids: List<String>, wantId: String): com.towerofdarkness.app.domain.combat.CombatState {
        val cards = ids.map { card(it) }
        var s = engine.start(cards, tank(), weapon(), maxHp = 99, playerHp = 99)
        // Exhaust until wantId is the only unspent left, or pick when highlighted
        repeat(12) {
            if (s.finished) return s
            s = engine.diceTumble(s)
            if (s.highlightedId == wantId) return s
            // Resolve and skip to next without caring about side effects on tank
            s = engine.resolveSkill(s)
            if (s.awaitingWeapon) s = engine.resolveWeapon(s)
            if (s.finished || s.enemy.hp <= 0) return s
            s = engine.resolveEnemy(s)
            if (!s.finished) s = engine.readyNext(s)
        }
        return s
    }

    @Test
    fun glossary_hasBraceAndSoften() {
        assertNotNull(Glossary.definition("brace"))
        assertNotNull(Glossary.definition("soften"))
        assertTrue(Glossary.definition("soften")!!.contains("damaging", ignoreCase = true))
    }

    @Test
    fun ironMantle_bracePipUnderYou() {
        val engine = CombatEngine(Random(42))
        var s = forceFire(engine, listOf("iron_mantle", "hostflint", "emberbrand", "ruin_seal", "tower_pike"), "iron_mantle")
        assertEquals("iron_mantle", s.highlightedId)
        s = engine.resolveSkill(s)
        assertEquals(3, s.brace)
        val pips = StatusPips.forPlayer(s)
        assertEquals(1, pips.size)
        assertEquals("brace", pips[0].term)
        assertEquals(3, pips[0].count)
        assertTrue(StatusPips.forEnemy(s).isEmpty())
    }

    @Test
    fun vowPlate_bracePip5() {
        val engine = CombatEngine(Random(7))
        var s = forceFire(engine, listOf("vow_plate", "hostflint", "emberbrand", "ruin_seal", "tower_pike"), "vow_plate")
        assertEquals("vow_plate", s.highlightedId)
        s = engine.resolveSkill(s)
        assertEquals(5, s.brace)
        assertEquals(5, StatusPips.forPlayer(s).single().count)
    }

    @Test
    fun brace_afterHitSpending2_pipIsNMinus2_thenGoneAtRoundEnd() {
        // Soften 5 + force Cleave (8) → dmg 3 after Soften; Brace 5 → remaining 2.
        val n = 5
        val engine = CombatEngine(Random(0))
        var s = engine.start(
            listOf(card("hostflint"), card("emberbrand"), card("ruin_seal"), card("tower_pike"), card("ash_press")),
            tank(),
            weapon(),
            maxHp = 99,
            playerHp = 99
        )
        val kit = com.towerofdarkness.app.domain.combat.EnemyKits.skillsFor(s.enemy)
        s = s.copy(
            brace = n,
            counterPenalty = 5,
            enemySpentIds = kit.map { it.id }.filter { it != "cleave" }.toSet()
        )
        s = engine.resolveEnemy(s)
        assertFalse(s.finished)
        assertEquals(n - 3, s.brace) // Cleave 8 − Soften 5 = 3
        assertEquals(n - 3, StatusPips.forPlayer(s).single().count)
        assertEquals(0, s.counterPenalty)
        assertTrue(StatusPips.forEnemy(s).none { it.term == "soften" })
        s = engine.readyNext(s)
        assertEquals(0, s.brace)
        assertTrue(StatusPips.forPlayer(s).isEmpty())
    }

    @Test
    fun brace_fullySpent_pipGone() {
        // Brace 3 vs counter 7–9 → remaining 0 after absorb
        // v0.1.14: show “0” for the hit beat, hide on readyNext
        val engine = CombatEngine(Random(1))
        var s = forceFire(
            engine,
            listOf("iron_mantle", "hostflint", "emberbrand", "ruin_seal", "tower_pike"),
            "iron_mantle"
        )
        s = engine.resolveSkill(s)
        assertEquals(3, s.brace)
        if (s.awaitingWeapon) s = engine.resolveWeapon(s)
        val kit = com.towerofdarkness.app.domain.combat.EnemyKits.skillsFor(s.enemy)
        s = s.copy(enemySpentIds = kit.map { it.id }.filter { it != "hit" }.toSet())
        s = engine.resolveEnemy(s)
        assertEquals(0, s.brace)
        assertEquals(0, StatusPips.forPlayer(s).single().count)
        s = engine.readyNext(s)
        assertTrue(StatusPips.forPlayer(s).isEmpty())
    }

    @Test
    fun cinderStep_softenPipUnderEnemy_thenGoneAfterCounter() {
        val engine = CombatEngine(Random(3))
        var s = forceFire(
            engine,
            listOf("cinder_step", "hostflint", "emberbrand", "ruin_seal", "tower_pike"),
            "cinder_step"
        )
        s = engine.resolveSkill(s)
        assertEquals(2, s.counterPenalty)
        val enemyPips = StatusPips.forEnemy(s)
        assertTrue(enemyPips.any { it.term == "soften" && it.count == 2 })
        if (s.awaitingWeapon) s = engine.resolveWeapon(s)
        // Force damaging Hit so Soften consumes (Hide would leave Soften)
        val kit = com.towerofdarkness.app.domain.combat.EnemyKits.skillsFor(s.enemy)
        s = s.copy(enemySpentIds = kit.map { it.id }.filter { it != "hit" }.toSet())
        s = engine.resolveEnemy(s)
        assertEquals(0, s.counterPenalty)
        assertTrue(StatusPips.forEnemy(s).none { it.term == "soften" })
    }

    @Test
    fun twoStatuses_bothVisible() {
        // Dust Veil: Brace 2 + no soften; combine with Soften via state copy after Cinder.
        val engine = CombatEngine(Random(9))
        var s = forceFire(
            engine,
            listOf("cinder_step", "dust_veil", "hostflint", "emberbrand", "ruin_seal"),
            "cinder_step"
        )
        s = engine.resolveSkill(s)
        assertEquals(2, s.counterPenalty)
        // Inject Brace without changing Soften (mirrors both live)
        s = s.copy(brace = 3)
        val player = StatusPips.forPlayer(s)
        val enemy = StatusPips.forEnemy(s)
        assertEquals(listOf("brace"), player.map { it.term })
        assertEquals(3, player[0].count)
        assertEquals(listOf("soften"), enemy.map { it.term })
        assertEquals(2, enemy[0].count)
    }

    @Test
    fun hideAtZero_noEmptyPips() {
        val engine = CombatEngine(Random(2))
        val s = engine.start(
            listOf(card("hostflint"), card("emberbrand"), card("ruin_seal"), card("tower_pike"), card("ash_press")),
            tank(),
            weapon(),
            maxHp = 30,
            playerHp = 30
        )
        assertEquals(0, s.brace)
        assertEquals(0, s.counterPenalty)
        assertTrue(StatusPips.forPlayer(s).isEmpty())
        assertTrue(StatusPips.forEnemy(s).isEmpty())
    }
}
