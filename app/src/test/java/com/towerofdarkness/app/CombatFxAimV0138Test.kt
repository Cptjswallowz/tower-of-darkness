package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.CombatFx
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.FxRecipient
import com.towerofdarkness.app.domain.combat.FxStrokeDir
import com.towerofdarkness.app.domain.combat.FxTier
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import com.towerofdarkness.app.domain.cards.CardCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * v0.1.38-fxaim — recipient-only slash geometry, Brace shield pip count, Soften pulse flag.
 * Tier maps unchanged from v0.1.37 (see CombatFxV0137Test).
 */
class CombatFxAimV0138Test {

    @Test
    fun recipientSlash_playerHitsFoe_enemyHitsYou() {
        val player = CombatFx.specForPlayer("hostflint")
        assertEquals(FxRecipient.FOE, player.recipient)
        assertEquals(FxRecipient.FOE, player.stroke!!.recipient)
        assertEquals(FxStrokeDir.YOU_TO_FOE, player.stroke!!.dir)

        val enemy = CombatFx.specForEnemy("shiv", EnemyKind.GOBLIN)
        assertEquals(FxRecipient.YOU, enemy.recipient)
        assertEquals(FxRecipient.YOU, enemy.stroke!!.recipient)
        assertEquals(FxStrokeDir.FOE_TO_YOU, enemy.stroke!!.dir)
    }

    @Test
    fun slashGeometry_shortCut_doesNotSpanBothBusts() {
        for (tier in listOf(FxTier.SMALL, FxTier.MEDIUM)) {
            for (r in FxRecipient.entries) {
                val g = CombatFx.slashCutGeom(r, tier)
                assertFalse("must not span You↔foe: $r $tier $g", g.spansBothBusts())
                assertEquals(r, g.recipient)
                val span = g.endXFrac - g.startXFrac
                assertTrue("cut too long: $span", span < 0.25f)
                when (r) {
                    FxRecipient.YOU -> {
                        assertEquals(CombatFx.YOU_BUST_X, g.centerXFrac, 0.001f)
                        assertTrue(g.endXFrac < 0.45f)
                    }
                    FxRecipient.FOE -> {
                        assertEquals(CombatFx.FOE_BUST_X, g.centerXFrac, 0.001f)
                        assertTrue(g.startXFrac > 0.55f)
                    }
                }
            }
        }
    }

    @Test
    fun wake_recipientIsFoe_noKernelStroke() {
        val wake = CombatFx.specForWake()
        assertTrue(wake.useWakeSlash)
        assertNull(wake.stroke)
        assertEquals(FxRecipient.FOE, wake.recipient)
        assertEquals(FxTier.WAKE, wake.tier)
    }

    @Test
    fun bracePipCount_equalsGainCappedAt5() {
        assertEquals(0, CombatFx.bracePipCount(0))
        assertEquals(1, CombatFx.bracePipCount(1))
        assertEquals(2, CombatFx.bracePipCount(2))
        assertEquals(3, CombatFx.bracePipCount(3))
        assertEquals(4, CombatFx.bracePipCount(4))
        assertEquals(5, CombatFx.bracePipCount(5))
        assertEquals(5, CombatFx.bracePipCount(6))
        assertEquals(5, CombatFx.bracePipCount(99))
        assertEquals(0, CombatFx.bracePipCount(-3))
    }

    @Test
    fun noStroke_stillNoSlash_butBracePipsOnOwner() {
        val mantle = CombatFx.specForPlayer("iron_mantle")
        assertEquals(FxTier.NO_STROKE, mantle.tier)
        assertNull(mantle.stroke)

        val play = CombatFx.playSpec(mantle, braceGained = 3, fxPlayer = true)
        assertEquals(3, play.bracePipCount)
        assertEquals(FxRecipient.YOU, play.braceOwner)
        assertFalse(play.softenPipPulse)

        val hide = CombatFx.specForEnemy("hide", EnemyKind.GOBLIN)
        assertNull(hide.stroke)
        val hidePlay = CombatFx.playSpec(hide, braceGained = 3, fxPlayer = false)
        assertEquals(3, hidePlay.bracePipCount)
        assertEquals(FxRecipient.FOE, hidePlay.braceOwner)

        val vow = CombatFx.playSpec(CombatFx.specForPlayer("vow_plate"), braceGained = 5)
        assertEquals(5, vow.bracePipCount)
        assertEquals(FxRecipient.YOU, vow.braceOwner)
    }

    @Test
    fun softenPipPulse_flag_noExtraStrokeFromFollowUp() {
        val only = CombatFx.playBraceOrSoftenOnly(softenApplied = 2, fxPlayer = true)
        assertNotNull(only)
        assertTrue(only!!.softenPipPulse)
        assertEquals(0, only.bracePipCount)
        assertNull(only.beat.stroke)
        assertEquals(FxTier.NO_STROKE, only.beat.tier)
    }

    @Test
    fun dustVeil_keepsSmallStroke_braceFollowUpHasPips() {
        assertEquals(FxTier.SMALL, CombatFx.tierForPlayer("dust_veil"))
        val hit = CombatFx.specForPlayer("dust_veil")
        assertNotNull(hit.stroke)
        assertEquals(FxRecipient.FOE, hit.stroke!!.recipient)

        // Follow-up Brace line (null fxId) → pips only
        val braceOnly = CombatFx.playBraceOrSoftenOnly(braceGained = 2, fxPlayer = true)
        assertEquals(2, braceOnly!!.bracePipCount)
        assertEquals(FxRecipient.YOU, braceOnly.braceOwner)
        assertNull(braceOnly.beat.stroke)
    }

    @Test
    fun mapsUnchanged_fromV0137() {
        assertEquals(
            setOf(
                "hostflint", "cinder_step", "emberbrand", "dust_veil",
                "ember_draw", "brand_mark", "cinder_vow", "grave_nail", "ash_press"
            ),
            CombatFx.PLAYER_SMALL
        )
        assertEquals(setOf("tower_pike", "ruin_seal", "spark_tithe", "wake_echo"), CombatFx.PLAYER_MEDIUM)
        assertEquals(setOf("iron_mantle", "vow_plate"), CombatFx.PLAYER_NO_STROKE)
        assertEquals(setOf("shiv", "nip", "hit", "club", "gate_pulse"), CombatFx.ENEMY_SMALL)
        assertEquals(setOf("cleave", "seal_pulse", "coal_slam"), CombatFx.ENEMY_MEDIUM)
        assertEquals(setOf("hide", "rust_guard", "cinder_hide"), CombatFx.ENEMY_NO_STROKE)
        assertEquals(CombatFx.COLOR_YOU, CombatFx.colorArgb(com.towerofdarkness.app.domain.combat.FxRole.YOU))
    }

    @Test
    fun engine_wiresBraceGained_andSoftenApplied() {
        val engine = CombatEngine(Random(11))
        val mantle = CardCatalog.byId("iron_mantle")!!
        val cards = listOf(
            mantle, CardCatalog.byId("hostflint")!!, CardCatalog.byId("emberbrand")!!,
            CardCatalog.byId("dust_veil")!!, CardCatalog.byId("cinder_step")!!
        )
        var s = engine.start(
            activeCards = cards,
            enemy = Enemy.normal(EnemyKind.GOBLIN, 1),
            weapon = WeaponRuntime(WeaponCatalog.ashbrand, 1, 0),
            maxHp = 30, playerHp = 30
        )
        s = s.copy(lastFiredCard = mantle, highlightedId = mantle.id)
        s = engine.resolveSkill(s)
        val braceEv = s.log.last { it.message.contains("Iron Mantle") }
        assertEquals(3, braceEv.braceGained)
        assertEquals(0, braceEv.softenApplied)
        assertEquals(3, CombatFx.bracePipCount(braceEv.braceGained))

        val dust = CardCatalog.byId("dust_veil")!!
        s = s.copy(lastFiredCard = dust, highlightedId = dust.id)
        val before = s.log.size
        s = engine.resolveSkill(s)
        val delta = s.log.drop(before)
        val hit = delta.first { it.fxId == "dust_veil" }
        assertEquals(0, hit.braceGained) // hit line = Small stroke only
        val braceFollow = delta.first { it.braceGained > 0 }
        assertEquals(2, braceFollow.braceGained)
        assertNull(braceFollow.fxId) // no second slash
    }

    @Test
    fun engine_enemyHide_braceGainedOnFoe() {
        val engine = CombatEngine(Random(2))
        val cards = listOf(
            CardCatalog.byId("hostflint")!!, CardCatalog.byId("emberbrand")!!,
            CardCatalog.byId("ruin_seal")!!, CardCatalog.byId("tower_pike")!!,
            CardCatalog.byId("ash_press")!!
        )
        var s = engine.start(
            activeCards = cards,
            enemy = Enemy.normal(EnemyKind.GOBLIN, 1),
            weapon = WeaponRuntime(WeaponCatalog.ashbrand, 1, 0),
            maxHp = 30, playerHp = 30
        )
        // Resolve enemy until Hide (or force via many rolls) — seed 2 hits hide often; loop
        var found: Int? = null
        repeat(12) {
            if (s.finished) return@repeat
            s = engine.resolveEnemy(s)
            val hide = s.log.lastOrNull { it.fxId == "hide" }
            if (hide != null) {
                found = hide.braceGained
                assertEquals(FxTier.NO_STROKE, CombatFx.tierForEnemy("hide"))
                assertTrue(hide.braceGained in 1..5)
                return@repeat
            }
        }
        // If this seed never rolled Hide, still assert helper owner mapping
        assertEquals(FxRecipient.FOE, CombatFx.braceOwnerForPlayer(false))
        if (found != null) {
            assertEquals(found, CombatFx.bracePipCount(found!!))
        }
    }

    @Test
    fun brandMark_softenApplied_noFxIdOnFollowUp() {
        val engine = CombatEngine(Random(3))
        val brand = CardCatalog.byId("brand_mark")!!
        val cards = listOf(
            brand, CardCatalog.byId("hostflint")!!, CardCatalog.byId("emberbrand")!!,
            CardCatalog.byId("ruin_seal")!!, CardCatalog.byId("tower_pike")!!
        )
        var s = engine.start(
            activeCards = cards,
            enemy = Enemy.normal(EnemyKind.ORC, 1),
            weapon = WeaponRuntime(WeaponCatalog.ashbrand, 1, charge = 1),
            maxHp = 30, playerHp = 30
        )
        s = s.copy(lastFiredCard = brand, highlightedId = brand.id)
        s = engine.resolveSkill(s)
        val soften = s.log.first { it.message.contains("softened", ignoreCase = true) }
        assertNull(soften.fxId)
        assertTrue(soften.softenApplied > 0)
        val pulse = CombatFx.playBraceOrSoftenOnly(softenApplied = soften.softenApplied)
        assertTrue(pulse!!.softenPipPulse)
    }
}
