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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.47-strokethick — fat canvas stroke WIDTH + bright colors + enemy→You proof.
 * Path length (half-extent) stayed OK in 0.1.46; hairline was thickness×3.2 ≈ 6.7px.
 * See docs/strokethick-v0147.md.
 */
class StrokeThickV0147Test {

    @Test
    fun tag_isStrokeThickV0147() {
        assertEquals("v0.1.47-strokethick", CombatFx.TAG)
        assertEquals("v0.1.47-strokethick", CleaveKit.TAG)
    }

    @Test
    fun coreGlow_fatVsHairline_player18to28_enemyThinner() {
        val hairline = CombatFx.HAIRLINE_CORE_PX_SMALL
        assertEquals(CombatFx.THICK_SMALL * CombatFx.STROKE_CORE_MULT, hairline, 0.01f)
        assertTrue("hairline baseline ~6.7", hairline in 6f..8f)

        val bust = CombatFx.REF_BUST_WIDTH_PX
        val coreSmall = CombatFx.strokeCoreWidthPx(FxRecipient.FOE, FxTier.SMALL, bust)
        val coreMed = CombatFx.strokeCoreWidthPx(FxRecipient.FOE, FxTier.MEDIUM, bust)
        val coreEnemy = CombatFx.strokeCoreWidthPx(FxRecipient.YOU, FxTier.SMALL, bust)
        // Practical lock: player core ~18–28px @360-stage
        assertTrue("player small core $coreSmall", coreSmall in 18f..28f)
        assertTrue("player medium core $coreMed", coreMed in 20f..32f)
        assertTrue(coreMed > coreSmall)
        assertTrue("enemy thinner $coreEnemy < $coreSmall", coreEnemy < coreSmall)
        assertTrue(coreEnemy >= 12f)
        // Fat >> hairline
        assertTrue(coreSmall > hairline * 2.5f)
        assertTrue(CombatFx.strokeCoreWidth(CombatFx.THICK_SMALL) > hairline * 2.5f)

        val glowSmall = CombatFx.strokeGlowWidthPx(FxRecipient.FOE, FxTier.SMALL, bust)
        val glowEnemy = CombatFx.strokeGlowWidthPx(FxRecipient.YOU, FxTier.SMALL, bust)
        assertTrue(glowSmall > coreSmall)
        assertTrue(glowEnemy > coreEnemy)
        assertTrue(glowEnemy < glowSmall)

        // Bust-frac constants in WO band
        assertTrue(CombatFx.STROKE_CORE_BUST_FRAC_PLAYER_SMALL in 0.08f..0.16f)
        assertTrue(CombatFx.STROKE_CORE_BUST_FRAC_PLAYER_MEDIUM in 0.10f..0.20f)
        assertTrue(CombatFx.STROKE_CORE_BUST_FRAC_ENEMY in 0.06f..0.12f)
    }

    @Test
    fun peakHolds_player450to550_enemy350to450() {
        assertTrue(CombatFx.STROKE_PLAYER_PEAK_MS in 450L..550L)
        assertTrue(CombatFx.STROKE_ENEMY_PEAK_MS in 350L..450L)
        assertTrue(CombatFx.STROKE_SMALL_MS >= CombatFx.STROKE_PLAYER_PEAK_MS)
        assertTrue(CombatFx.STROKE_ENEMY_SMALL_MS >= CombatFx.STROKE_ENEMY_PEAK_MS)
        val host = CombatFx.specForPlayer("hostflint")
        val peakP = CombatFx.strokePeakMs(host.strokeMs, FxRecipient.FOE)
        assertTrue("player peak $peakP", peakP in 450L..550L)
        val hit = CombatFx.specForEnemy("hit", EnemyKind.GOBLIN)
        val peakE = CombatFx.strokePeakMs(hit.strokeMs, FxRecipient.YOU)
        assertTrue("enemy peak $peakE", peakE in 350L..450L)
        assertTrue(peakP > peakE)
    }

    @Test
    fun enemyHit_recipientYou_debugFxStrokeOnYou() {
        for (id in listOf("hit", "shiv", "nip", "seal_pulse", "coal_slam", "cleave", "club", "gate_pulse")) {
            val kind = when (id) {
                "seal_pulse" -> EnemyKind.DRAGON
                "coal_slam" -> EnemyKind.ASH_WARDEN
                "club" -> EnemyKind.CAVE_TROLL
                "gate_pulse" -> EnemyKind.GATE_WARDEN
                else -> EnemyKind.GOBLIN
            }
            assertTrue("$id must produce You stroke", CombatFx.enemyHitProducesYouStroke(id, kind))
            val beat = CombatFx.specForEnemy(id, kind)
            assertNotNull("$id stroke", beat.stroke)
            assertEquals(FxRecipient.YOU, beat.recipient)
            assertEquals(FxRecipient.YOU, beat.stroke!!.recipient)
            assertEquals(FxStrokeDir.FOE_TO_YOU, beat.stroke!!.dir)
            assertEquals(
                "FX stroke on You",
                CombatFx.strokeDebugLine(CombatFx.strokeTargetLabel(FxRecipient.YOU, null))
            )
        }
        // resolveFxPlayer hardens wrong event flag
        assertFalse(CombatFx.resolveFxPlayer("hit", eventFxPlayer = true))
        assertFalse(CombatFx.resolveFxPlayer("seal_pulse", eventFxPlayer = true))
        assertTrue(CombatFx.resolveFxPlayer("hostflint", eventFxPlayer = false))
        assertTrue(CombatFx.resolveFxPlayer(CombatFx.ID_ASHBRAND_WAKE, eventFxPlayer = false))
        // safeSpec with wrong flag still corrected via resolve path in UI; domain still
        // respects explicit fxPlayer — proof helper uses resolveFxPlayer
        val wrong = CombatFx.safeSpec("hit", fxPlayer = true, enemyKind = EnemyKind.GOBLIN)
        assertNotNull(wrong)
        // Without resolve, wrong flag aims FOE — that is why CombatScreen must resolve
        assertEquals(FxRecipient.FOE, wrong!!.recipient)
        val fixed = CombatFx.safeSpec(
            "hit",
            CombatFx.resolveFxPlayer("hit", true),
            EnemyKind.GOBLIN
        )
        assertEquals(FxRecipient.YOU, fixed!!.recipient)
    }

    @Test
    fun colors_brightSteelEmberWhite_enemyDullerStillVisible() {
        assertEquals(0xFFF2E6D0L, CombatFx.COLOR_STROKE_YOU)
        assertEquals(0xFFC4B8A8L, CombatFx.COLOR_STROKE_ENEMY)
        assertEquals(0xFFD0B49AL, CombatFx.COLOR_STROKE_ENEMY_EMBER)
        assertEquals(0xFFFFF8ECL, CombatFx.COLOR_STROKE_CORE_HIGHLIGHT)
        // Brighter than muddy 0.1.46 values
        assertNotEquals(0xFFC9B8A0L, CombatFx.COLOR_STROKE_YOU)
        assertNotEquals(0xFF8A8680L, CombatFx.COLOR_STROKE_ENEMY)
        val host = CombatFx.specForPlayer("hostflint")
        assertEquals(CombatFx.COLOR_STROKE_YOU, host.stroke!!.colorArgb)
        val hit = CombatFx.specForEnemy("hit", EnemyKind.GOBLIN)
        assertEquals(CombatFx.COLOR_STROKE_ENEMY, hit.stroke!!.colorArgb)
        // Enemy alpha channel full; RGB duller than player (sum channels)
        fun rgbSum(c: Long): Int {
            val r = ((c shr 16) and 0xFF).toInt()
            val g = ((c shr 8) and 0xFF).toInt()
            val b = (c and 0xFF).toInt()
            return r + g + b
        }
        assertTrue(rgbSum(CombatFx.COLOR_STROKE_YOU) > rgbSum(CombatFx.COLOR_STROKE_ENEMY))
    }

    @Test
    fun wakeExclusive_architectureKept_pathPrimary() {
        val wake = CombatFx.specForWake()
        assertNull(wake.stroke)
        assertTrue(wake.useWakeSlash)
        assertTrue(CombatFx.isWakeExclusiveId(CombatFx.ID_ASHBRAND_WAKE))
        assertTrue(WakeArt.WAKE_ART_FULLY_FROZEN)
        assertTrue(CombatFx.noPlayerCardUsesWakeTier())
        // CLEAVE scale frozen (not the fix)
        assertEquals(0.85f, CleaveKit.BUST_COVERAGE, 0.001f)
        assertEquals(0.78f, CleaveKit.BUST_WIDTH_FRAC, 0.001f)
        assertEquals(140, CleaveKit.SLASH_CROP_PX)
        // Overlay still drawWakeFamilyStroke primary + tip garnish
        val overlay = File("app/src/main/java/com/towerofdarkness/app/ui/components/CombatFxOverlay.kt")
        val src = if (overlay.isFile) overlay else File("src/main/java/com/towerofdarkness/app/ui/components/CombatFxOverlay.kt")
        assertTrue(src.isFile)
        val text = src.readText()
        assertTrue("drawWakeFamilyStroke" in text)
        assertTrue("strokeCoreWidthPx" in text)
        assertTrue("drawCleaveTipGarnish" in text)
        assertTrue("fun CombatStrokeOverlay" in text)
        // CombatScreen hardens resolveFxPlayer + sync FX stroke on You
        val screen = File("app/src/main/java/com/towerofdarkness/app/ui/screens/CombatScreen.kt")
        val ssrc = if (screen.isFile) screen else File("src/main/java/com/towerofdarkness/app/ui/screens/CombatScreen.kt")
        val stext = ssrc.readText()
        assertTrue("resolveFxPlayer" in stext)
        assertTrue("strokeDebugLine" in stext)
    }

    @Test
    fun playerHits_stillStrokeOnFoe_thickerFamily() {
        for (id in listOf(
            "hostflint", "emberbrand", "dust_veil", "tower_pike",
            "ruin_seal", "ash_press", CombatFx.ID_ASHBRAND_SPARK
        )) {
            val s = if (id == CombatFx.ID_ASHBRAND_SPARK) CombatFx.specForSpark()
            else CombatFx.specForPlayer(id)
            assertNotNull("$id", s.stroke)
            assertEquals(FxRecipient.FOE, s.recipient)
            assertEquals(FxStrokeDir.YOU_TO_FOE, s.stroke!!.dir)
        }
        // Enemy thickness factor slightly thinner
        val p = CombatFx.specForPlayer("hostflint").stroke!!.thickness
        val e = CombatFx.specForEnemy("hit", EnemyKind.GOBLIN).stroke!!.thickness
        assertEquals(CombatFx.THICK_SMALL, p, 0.001f)
        assertEquals(CombatFx.THICK_SMALL * CombatFx.THICK_ENEMY_FACTOR, e, 0.001f)
        assertTrue(e < p)
    }
}
