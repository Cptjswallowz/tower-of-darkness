package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.CombatFx
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.FxRecipient
import com.towerofdarkness.app.domain.combat.FxStrokeDir
import com.towerofdarkness.app.domain.combat.FxTier
import com.towerofdarkness.app.domain.combat.WakeArt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * v0.1.39-fxread — bigger Brace shields, heavier slash holds.
 * Aim / who-gets-FX frozen from v0.1.38. See docs/fxread-v0139.md.
 */
class CombatFxReadV0139Test {

    @Test
    fun tag_advancedToFxfixV0140() {
        // TAG advanced in v0.1.40-fxfix; readability locks in this class still hold
        assertEquals("v0.1.46-strokefallback", CombatFx.TAG)
    }

    @Test
    fun slashThickness_is1_4xPrior() {
        assertEquals(CombatFx.THICK_SMALL_PRIOR * 1.4f, CombatFx.THICK_SMALL, 0.001f)
        assertEquals(CombatFx.THICK_MEDIUM_PRIOR * 1.4f, CombatFx.THICK_MEDIUM, 0.001f)
        assertEquals(2.1f, CombatFx.THICK_SMALL, 0.001f)
        assertEquals(4.2f, CombatFx.THICK_MEDIUM, 0.001f)
        assertEquals(2.1f, CombatFx.specForPlayer("hostflint").stroke!!.thickness, 0.001f)
        assertEquals(4.2f, CombatFx.specForPlayer("tower_pike").stroke!!.thickness, 0.001f)
    }

    @Test
    fun strokeHolds_plus200Ms_at1x() {
        // v0.1.46: player peak 400–500 then fade; total ≤500 @1x
        assertEquals(500L, CombatFx.STROKE_SMALL_MS)
        assertEquals(500L, CombatFx.STROKE_MEDIUM_MS)
        assertEquals(500L, CombatFx.specForPlayer("hostflint").strokeMs)
        assertEquals(500L, CombatFx.specForPlayer("tower_pike").strokeMs)
        assertEquals(250L, CombatFx.fxHoldMs(CombatFx.STROKE_SMALL_MS, 2))
        assertEquals(250L, CombatFx.fxHoldMs(CombatFx.STROKE_MEDIUM_MS, 2))
    }

    @Test
    fun braceHold_700At1x_350At2x() {
        assertEquals(700L, CombatFx.BRACE_PIP_MS)
        assertEquals(200L, CombatFx.BRACE_PIP_FADE_MS)
        assertEquals(700L, CombatFx.fxHoldMs(CombatFx.BRACE_PIP_MS, 1))
        assertEquals(350L, CombatFx.fxHoldMs(CombatFx.BRACE_PIP_MS, 2))
    }

    @Test
    fun bracePipRadius_is1_5xPrior() {
        assertEquals(
            CombatFx.BRACE_PIP_RADIUS_FRAC_PRIOR * 1.5f,
            CombatFx.BRACE_PIP_RADIUS_FRAC,
            0.0001f
        )
        assertEquals(0.027f, CombatFx.BRACE_PIP_RADIUS_FRAC, 0.0001f)
    }

    @Test
    fun bracePipAlpha_fullUntilFadeThenDrops() {
        val fadeStart =
            (CombatFx.BRACE_PIP_MS - CombatFx.BRACE_PIP_FADE_MS).toFloat() /
                CombatFx.BRACE_PIP_MS.toFloat()
        // Full opacity before fade window
        assertEquals(0.95f, CombatFx.bracePipAlpha(0f), 0.001f)
        assertEquals(0.95f, CombatFx.bracePipAlpha(fadeStart - 0.01f), 0.001f)
        assertEquals(0.95f, CombatFx.bracePipAlpha(0.5f), 0.001f)
        // Mid-fade ~ halfway through last segment
        val mid = fadeStart + (1f - fadeStart) * 0.5f
        val midA = CombatFx.bracePipAlpha(mid)
        assertTrue("mid fade should drop: $midA", midA < 0.95f && midA > 0.15f)
        // End floors at ~0.15
        assertEquals(0.15f, CombatFx.bracePipAlpha(1f), 0.001f)
        // Not linear (1-progress): at progress 0.5 old linear was 0.5; new stays full
        assertTrue(abs(CombatFx.bracePipAlpha(0.5f) - 0.5f) > 0.2f)
    }

    @Test
    fun wakeSlashFrame_plus200Ms() {
        assertEquals(700L, WakeArt.FRAME_SLASH_MS)
        assertEquals(350L, WakeArt.holdMs(WakeArt.FRAME_SLASH_MS, 2))
        // Charge/impact unchanged
        assertEquals(400L, WakeArt.FRAME_CHARGE_MS)
        assertEquals(400L, WakeArt.FRAME_IMPACT_MS)
    }

    @Test
    fun aimAndRecipient_unchangedFromV0138() {
        val player = CombatFx.specForPlayer("hostflint")
        assertEquals(FxRecipient.FOE, player.recipient)
        assertEquals(FxRecipient.FOE, player.stroke!!.recipient)
        assertEquals(FxStrokeDir.YOU_TO_FOE, player.stroke!!.dir)

        val enemy = CombatFx.specForEnemy("shiv", EnemyKind.GOBLIN)
        assertEquals(FxRecipient.YOU, enemy.recipient)
        assertEquals(FxRecipient.YOU, enemy.stroke!!.recipient)

        val wake = CombatFx.specForWake()
        assertTrue(wake.useWakeSlash)
        assertNull(wake.stroke)
        assertEquals(FxRecipient.FOE, wake.recipient)

        for (r in FxRecipient.entries) {
            val g = CombatFx.slashCutGeom(r, FxTier.SMALL)
            assertFalse(g.spansBothBusts())
        }

        val mantle = CombatFx.playSpec(CombatFx.specForPlayer("iron_mantle"), braceGained = 3)
        assertEquals(3, mantle.bracePipCount)
        assertEquals(FxRecipient.YOU, mantle.braceOwner)
        assertNull(mantle.beat.stroke)

        val hide = CombatFx.playSpec(
            CombatFx.specForEnemy("hide", EnemyKind.GOBLIN),
            braceGained = 2,
            fxPlayer = false
        )
        assertEquals(FxRecipient.FOE, hide.braceOwner)
    }

    @Test
    fun bracePipCount_stillCap5() {
        assertEquals(5, CombatFx.bracePipCount(5))
        assertEquals(5, CombatFx.bracePipCount(9))
        assertEquals(2, CombatFx.bracePipCount(2))
    }
}
