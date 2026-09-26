package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.CleaveKit
import com.towerofdarkness.app.domain.combat.CombatFx
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.FxRecipient
import com.towerofdarkness.app.domain.combat.FxStrokeDir
import com.towerofdarkness.app.domain.combat.FxTier
import com.towerofdarkness.app.domain.combat.WakeArt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.48-strokeboth — thick crescent BOTH sides (outgoing foe + incoming You).
 * Shape: filled crescent blade (not Round-cap stadium pill). Peaks 500 / 450.
 * See docs/strokeboth-v0148.md.
 */
class StrokeBothV0148Test {

    @Test
    fun tag_isStrokeBothV0148() {
        assertEquals("v0.1.48-strokeboth", CombatFx.TAG)
        assertEquals("v0.1.48-strokeboth", CleaveKit.TAG)
    }

    @Test
    fun playerCore_geEnemyYouWeight_fracsAndPx() {
        assertTrue(CombatFx.playerCoreBeatsEnemyYouWeight())
        assertTrue(
            CombatFx.STROKE_CORE_BUST_FRAC_PLAYER_SMALL >= CombatFx.STROKE_CORE_BUST_FRAC_ENEMY
        )
        assertEquals(0.14f, CombatFx.STROKE_CORE_BUST_FRAC_PLAYER_SMALL, 0.001f)
        assertEquals(0.18f, CombatFx.STROKE_CORE_BUST_FRAC_PLAYER_MEDIUM, 0.001f)
        assertEquals(0.10f, CombatFx.STROKE_CORE_BUST_FRAC_ENEMY, 0.001f)
        assertEquals(0.30f, CombatFx.STROKE_GLOW_BUST_FRAC_PLAYER_SMALL, 0.001f)
        assertEquals(0.38f, CombatFx.STROKE_GLOW_BUST_FRAC_PLAYER_MEDIUM, 0.001f)
        assertEquals(0.22f, CombatFx.STROKE_GLOW_BUST_FRAC_ENEMY, 0.001f)

        val bust = CombatFx.REF_BUST_WIDTH_PX
        val playerCore = CombatFx.strokeCoreWidthPx(FxRecipient.FOE, FxTier.SMALL, bust)
        val enemyYouCore = CombatFx.strokeCoreWidthPx(FxRecipient.YOU, FxTier.SMALL, bust)
        assertTrue("player $playerCore >= enemyYou $enemyYouCore", playerCore >= enemyYouCore)
        // ~19.7px player / ~14.0px enemy @360
        assertTrue(playerCore in 18f..28f)
        assertTrue(enemyYouCore in 12f..16f)
        assertEquals(enemyYouCore, bust * 0.10f, 0.2f)
        assertEquals(playerCore, bust * 0.14f, 0.2f)
    }

    @Test
    fun peaks_player500_enemy450_totalsAbovePeak() {
        assertEquals(500L, CombatFx.STROKE_PLAYER_PEAK_MS)
        assertEquals(450L, CombatFx.STROKE_ENEMY_PEAK_MS)
        assertTrue(CombatFx.STROKE_SMALL_MS >= 560L)
        assertTrue(CombatFx.STROKE_SMALL_MS >= CombatFx.STROKE_PLAYER_PEAK_MS)
        assertTrue(CombatFx.STROKE_MEDIUM_MS >= CombatFx.STROKE_PLAYER_PEAK_MS)
        assertTrue(CombatFx.STROKE_ENEMY_SMALL_MS >= CombatFx.STROKE_ENEMY_PEAK_MS)
        assertTrue(CombatFx.STROKE_ENEMY_MEDIUM_MS >= CombatFx.STROKE_ENEMY_PEAK_MS)

        val host = CombatFx.specForPlayer("hostflint")
        val peakP = CombatFx.strokePeakMs(host.strokeMs, FxRecipient.FOE)
        assertEquals(500L, peakP)
        val hit = CombatFx.specForEnemy("hit", EnemyKind.GOBLIN)
        val peakE = CombatFx.strokePeakMs(hit.strokeMs, FxRecipient.YOU)
        assertEquals(450L, peakE)
        assertTrue(peakP > peakE)
    }

    @Test
    fun shape_filledCrescent_notRoundPill() {
        assertTrue(CombatFx.STROKE_SHAPE_FILLED_CRESCENT)
        assertTrue(CombatFx.STROKE_CRESCENT_BOW_MULT >= 1.0f)
        assertTrue(CombatFx.STROKE_CRESCENT_INNER_BOW_FRAC in 0.20f..0.55f)
        val half = 40f
        val bow = CombatFx.crescentBowPx(half)
        assertTrue("bow $bow >= halfChord $half", bow >= half)
        val inner = CombatFx.crescentInnerBowPx(bow)
        assertTrue(inner < bow)
        assertTrue(inner > 0f)

        val overlay = File("app/src/main/java/com/towerofdarkness/app/ui/components/CombatFxOverlay.kt")
        val src = if (overlay.isFile) overlay else File("src/main/java/com/towerofdarkness/app/ui/components/CombatFxOverlay.kt")
        assertTrue(src.isFile)
        val text = src.readText()
        assertTrue("drawWakeFamilyStroke" in text)
        assertTrue("crescentBladePath" in text)
        assertTrue("NOT StrokeCap.Round" in text || "stadium" in text.lowercase() || "pill" in text.lowercase())
        // Primary draw must not use Round-cap Stroke stadium (pill FAIL)
        assertFalse(
            "Round-cap core stroke must not remain as primary",
            text.contains("cap = StrokeCap.Round")
        )
        assertTrue("quadraticTo" in text)
        assertTrue("fun CombatStrokeOverlay" in text)
    }

    @Test
    fun outgoing_hostflintEmberbrandDustPike_strokeOnFoe() {
        for (id in listOf(
            "hostflint", "emberbrand", "dust_veil", "tower_pike",
            "ruin_seal", "ash_press", CombatFx.ID_ASHBRAND_SPARK
        )) {
            val s = if (id == CombatFx.ID_ASHBRAND_SPARK) CombatFx.specForSpark()
            else CombatFx.specForPlayer(id)
            assertNotNull("$id stroke", s.stroke)
            assertEquals("$id recipient", FxRecipient.FOE, s.recipient)
            assertEquals(FxRecipient.FOE, s.stroke!!.recipient)
            assertEquals(FxStrokeDir.YOU_TO_FOE, s.stroke!!.dir)
            assertEquals(
                "FX stroke on Goblin",
                CombatFx.strokeDebugLine(CombatFx.strokeTargetLabel(FxRecipient.FOE, "Goblin"))
            )
        }
        // Soften-only path stays NO_STROKE (Brace/Soften follow-ups)
        val softenOnly = CombatFx.playBraceOrSoftenOnly(softenApplied = 1, fxPlayer = true)
        assertNotNull(softenOnly)
        assertNull(softenOnly!!.beat.stroke)
        assertTrue(softenOnly.softenPipPulse)
    }

    @Test
    fun incoming_hitNipShivSealPulse_strokeOnYou() {
        for (id in listOf("hit", "nip", "shiv", "seal_pulse", "coal_slam")) {
            val kind = when (id) {
                "seal_pulse" -> EnemyKind.DRAGON
                "coal_slam" -> EnemyKind.ASH_WARDEN
                else -> EnemyKind.GOBLIN
            }
            assertTrue("$id → You", CombatFx.enemyHitProducesYouStroke(id, kind))
            val beat = CombatFx.specForEnemy(id, kind)
            assertNotNull(beat.stroke)
            assertEquals(FxRecipient.YOU, beat.recipient)
            assertEquals(FxStrokeDir.FOE_TO_YOU, beat.stroke!!.dir)
            assertEquals(
                "FX stroke on You",
                CombatFx.strokeDebugLine(CombatFx.strokeTargetLabel(FxRecipient.YOU, null))
            )
        }
        assertFalse(CombatFx.resolveFxPlayer("hit", eventFxPlayer = true))
        assertTrue(CombatFx.resolveFxPlayer("hostflint", eventFxPlayer = false))
    }

    @Test
    fun colors_brightSteel_wakeExclusive_cleaveScaleFrozen() {
        assertEquals(0xFFFFF6E4L, CombatFx.COLOR_STROKE_YOU)
        assertEquals(0xFFC4B8A8L, CombatFx.COLOR_STROKE_ENEMY)
        assertEquals(0xFFD0B49AL, CombatFx.COLOR_STROKE_ENEMY_EMBER)
        assertEquals(0xFFFFF8ECL, CombatFx.COLOR_STROKE_CORE_HIGHLIGHT)
        val wake = CombatFx.specForWake()
        assertNull(wake.stroke)
        assertTrue(wake.useWakeSlash)
        assertTrue(WakeArt.WAKE_ART_FULLY_FROZEN)
        assertEquals(0.85f, CleaveKit.BUST_COVERAGE, 0.001f)
        assertEquals(0.78f, CleaveKit.BUST_WIDTH_FRAC, 0.001f)
        assertEquals(140, CleaveKit.SLASH_CROP_PX)
        // CombatScreen still hardens resolveFxPlayer + sync debug
        val screen = File("app/src/main/java/com/towerofdarkness/app/ui/screens/CombatScreen.kt")
        val ssrc = if (screen.isFile) screen else File("src/main/java/com/towerofdarkness/app/ui/screens/CombatScreen.kt")
        val stext = ssrc.readText()
        assertTrue("resolveFxPlayer" in stext)
        assertTrue("strokeDebugLine" in stext)
    }
}
