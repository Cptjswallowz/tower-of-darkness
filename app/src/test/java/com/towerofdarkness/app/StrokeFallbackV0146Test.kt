package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.CleaveKit
import com.towerofdarkness.app.domain.combat.CombatFx
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.EnemyKitRole
import com.towerofdarkness.app.domain.combat.FxRecipient
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
 * v0.1.48-strokeboth — drawn Wake-family stroke is the readable cut (PRIMARY).
 * CLEAVE tip garnish optional; no CLEAVE rescale as the WO fix.
 * See docs/strokefallback-v0146.md.
 */
class StrokeFallbackV0146Test {

    @Test
    fun tag_isStrokeFallbackV0146() {
        assertEquals("v0.1.48-strokeboth", CombatFx.TAG)
        assertEquals("v0.1.48-strokeboth", CleaveKit.TAG)
    }

    @Test
    fun pathIsPrimary_debugStringsDiffer() {
        assertEquals("FX stroke on ", CombatFx.STROKE_DEBUG_FMT)
        assertEquals("FX slash-light on ", CombatFx.SLASH_LIGHT_DEBUG_FMT)
        assertNotEquals(CombatFx.STROKE_DEBUG_FMT, CombatFx.SLASH_LIGHT_DEBUG_FMT)
        assertEquals("FX stroke on You", CombatFx.strokeDebugLine("You"))
        assertEquals("FX stroke on Goblin", CombatFx.strokeDebugLine("Goblin"))
        assertEquals("FX slash-light on You", CombatFx.slashLightDebugLine("You"))
        assertEquals("TodFx", CombatFx.LOG_TAG_TOD_FX)
        // Overlay source must prefer drawWakeFamilyStroke (path), not sheet-as-slash
        val overlay = File("src/main/java/com/towerofdarkness/app/ui/components/CombatFxOverlay.kt")
        val src = if (overlay.isFile) overlay else File("app/src/main/java/com/towerofdarkness/app/ui/components/CombatFxOverlay.kt")
        assertTrue(src.isFile)
        val text = src.readText()
        assertTrue("drawWakeFamilyStroke" in text)
        assertTrue("drawCleaveTipGarnish" in text)
        assertTrue("PRIMARY" in text || "drawn Wake-family" in text)
        assertFalse("Prefer CLEAVE sheet whenever" in text)
    }

    @Test
    fun widths_player70to90_enemyAbout60() {
        assertTrue(CombatFx.STROKE_WIDTH_FRAC_PLAYER_SMALL in 0.70f..0.90f)
        assertTrue(CombatFx.STROKE_WIDTH_FRAC_PLAYER_MEDIUM in 0.70f..0.90f)
        assertEquals(0.60f, CombatFx.STROKE_WIDTH_FRAC_ENEMY, 0.001f)
        assertEquals(
            CombatFx.STROKE_WIDTH_FRAC_PLAYER_SMALL,
            CombatFx.strokeWidthFracOfBust(FxRecipient.FOE, FxTier.SMALL),
            0.001f
        )
        assertEquals(
            CombatFx.STROKE_WIDTH_FRAC_PLAYER_MEDIUM,
            CombatFx.strokeWidthFracOfBust(FxRecipient.FOE, FxTier.MEDIUM),
            0.001f
        )
        assertEquals(
            CombatFx.STROKE_WIDTH_FRAC_ENEMY,
            CombatFx.strokeWidthFracOfBust(FxRecipient.YOU, FxTier.SMALL),
            0.001f
        )
        val foeSmall = CombatFx.slashCutGeom(FxRecipient.FOE, FxTier.SMALL)
        val foeMed = CombatFx.slashCutGeom(FxRecipient.FOE, FxTier.MEDIUM)
        val youHit = CombatFx.slashCutGeom(FxRecipient.YOU, FxTier.SMALL)
        assertFalse(foeSmall.spansBothBusts())
        assertFalse(foeMed.spansBothBusts())
        assertFalse(youHit.spansBothBusts())
        // Full width ≈ 2 * halfX / (0.5 * bustFrac) == widthFrac
        val bustFrac = 0.5f * CombatFx.STROKE_BUST_WIDTH_FRAC
        val playerFull = 2f * foeSmall.halfExtentXFrac / bustFrac
        val enemyFull = 2f * youHit.halfExtentXFrac / bustFrac
        assertEquals(CombatFx.STROKE_WIDTH_FRAC_PLAYER_SMALL, playerFull, 0.01f)
        assertEquals(CombatFx.STROKE_WIDTH_FRAC_ENEMY, enemyFull, 0.01f)
        assertTrue(foeMed.halfExtentXFrac > foeSmall.halfExtentXFrac)
        assertTrue(youHit.halfExtentXFrac < foeSmall.halfExtentXFrac)
    }

    @Test
    fun peakHolds_player400to500_enemy300to400() {
        // v0.1.48 peaks 500/450; keep architecture locks (player > enemy; total >= peak)
        assertTrue(CombatFx.STROKE_PLAYER_PEAK_MS in 450L..550L)
        assertTrue(CombatFx.STROKE_ENEMY_PEAK_MS in 350L..450L)
        assertTrue(CombatFx.STROKE_SMALL_MS >= CombatFx.STROKE_PLAYER_PEAK_MS)
        assertTrue(CombatFx.STROKE_MEDIUM_MS >= CombatFx.STROKE_PLAYER_PEAK_MS)
        assertTrue(CombatFx.STROKE_ENEMY_SMALL_MS >= CombatFx.STROKE_ENEMY_PEAK_MS)
        assertTrue(CombatFx.STROKE_ENEMY_MEDIUM_MS >= CombatFx.STROKE_ENEMY_PEAK_MS)
        val host = CombatFx.specForPlayer("hostflint")
        assertEquals(CombatFx.STROKE_SMALL_MS, host.strokeMs)
        val peakPlayer = CombatFx.strokePeakMs(host.strokeMs, FxRecipient.FOE)
        assertTrue("player peak $peakPlayer", peakPlayer in 450L..550L)
        val hit = CombatFx.specForEnemy("hit", EnemyKind.GOBLIN)
        assertEquals(CombatFx.STROKE_ENEMY_SMALL_MS, hit.strokeMs)
        val peakEnemy = CombatFx.strokePeakMs(hit.strokeMs, FxRecipient.YOU)
        assertTrue("enemy peak $peakEnemy", peakEnemy in 350L..450L)
        // 2x shortens
        assertEquals(CombatFx.STROKE_SMALL_MS / 2, CombatFx.fxHoldMs(CombatFx.STROKE_SMALL_MS, 2))
        assertEquals(CombatFx.STROKE_ENEMY_SMALL_MS / 2, CombatFx.fxHoldMs(CombatFx.STROKE_ENEMY_SMALL_MS, 2))
    }

    @Test
    fun sparkGetsShortStroke_wakeExclusiveUntouched() {
        assertEquals(FxTier.SMALL, CombatFx.tierForPlayer(CombatFx.ID_ASHBRAND_SPARK))
        val spark = CombatFx.specForSpark()
        assertNotNull(spark.stroke)
        assertEquals(CombatFx.STROKE_SPARK_MS, spark.strokeMs)
        assertTrue(spark.strokeMs < CombatFx.STROKE_SMALL_MS)
        assertTrue(spark.useCleaveHitFlash)
        assertFalse(spark.useWakeSlash)
        assertEquals(FxRecipient.FOE, spark.recipient)
        val peakSpark = CombatFx.strokePeakMs(spark.strokeMs, FxRecipient.FOE, spark = true)
        assertTrue(peakSpark < CombatFx.STROKE_PLAYER_PEAK_MS)

        val wake = CombatFx.specForWake()
        assertNull(wake.stroke)
        assertTrue(wake.useWakeSlash)
        assertFalse(wake.useCleaveHitFlash)
        assertTrue(CombatFx.isWakeExclusiveId(CombatFx.ID_ASHBRAND_WAKE))
        assertTrue(WakeArt.WAKE_ART_FULLY_FROZEN)
        assertTrue(CombatFx.noPlayerCardUsesWakeTier())
    }

    @Test
    fun noCleaveRescale_asFix_constantsFrozen() {
        // WO: do NOT rescale CLEAVE crop/BUST_WIDTH as the fix — freeze 0.1.45 locks
        assertEquals(0.85f, CleaveKit.BUST_COVERAGE, 0.001f)
        assertEquals(0.78f, CleaveKit.BUST_WIDTH_FRAC, 0.001f)
        assertEquals(140, CleaveKit.SLASH_CROP_PX)
        assertEquals(115, CleaveKit.SLASH_CROP_OX)
        assertEquals(26, CleaveKit.SLASH_CROP_OY)
        assertEquals(102, CleaveKit.OPAQUE_UNION_W)
        assertEquals(133, CleaveKit.OPAQUE_UNION_H)
        assertEquals(1.3f, CleaveKit.MEDIUM_SCALE, 0.001f)
        assertTrue(CleaveKit.bustCoverageInLockRange())
        assertTrue(CleaveKit.cropInsideCellWithFill())
        // Primary FX is path — tip garnish may use sheet at tiny dst only
        val overlay = File("app/src/main/java/com/towerofdarkness/app/ui/components/CombatFxOverlay.kt")
        val src = if (overlay.isFile) overlay else File("src/main/java/com/towerofdarkness/app/ui/components/CombatFxOverlay.kt")
        val text = src.readText()
        assertTrue("tipSize" in text || "0.11f" in text)
        assertTrue("drawWakeFamilyStroke" in text)
    }

    @Test
    fun map_playerEnemyStrokes_steelAsh_notForcedGoldGreen() {
        for (id in listOf("hostflint", "emberbrand", "dust_veil", "tower_pike", "ruin_seal", "ash_press")) {
            val s = CombatFx.specForPlayer(id)
            assertNotNull("$id stroke", s.stroke)
            assertEquals(FxRecipient.FOE, s.recipient)
            assertEquals(CombatFx.COLOR_STROKE_YOU, s.stroke!!.colorArgb)
            assertNotEquals(CombatFx.COLOR_YOU, s.stroke!!.colorArgb) // not forced gold
        }
        for (id in listOf("hit", "shiv", "seal_pulse", "coal_slam")) {
            val s = CombatFx.specForEnemy(id, when (id) {
                "seal_pulse" -> EnemyKind.DRAGON
                "coal_slam" -> EnemyKind.ASH_WARDEN
                else -> EnemyKind.GOBLIN
            })
            assertNotNull("$id stroke", s.stroke)
            assertEquals(FxRecipient.YOU, s.recipient)
            assertTrue(
                s.stroke!!.colorArgb == CombatFx.COLOR_STROKE_ENEMY ||
                    s.stroke!!.colorArgb == CombatFx.COLOR_STROKE_ENEMY_EMBER
            )
            assertNotEquals(CombatFx.COLOR_WEAK_GOBLIN, s.stroke!!.colorArgb) // not forced green
        }
        // Brace still NO_STROKE
        assertEquals(FxTier.NO_STROKE, CombatFx.specForPlayer("iron_mantle").tier)
        assertNull(CombatFx.specForPlayer("iron_mantle").stroke)
    }

    @Test
    fun composablePath_CombatStrokeOverlay_drawWakeFamilyStroke() {
        val overlay = File("app/src/main/java/com/towerofdarkness/app/ui/components/CombatFxOverlay.kt")
        val src = if (overlay.isFile) overlay else File("src/main/java/com/towerofdarkness/app/ui/components/CombatFxOverlay.kt")
        assertTrue(src.isFile)
        val text = src.readText()
        assertTrue("fun CombatStrokeOverlay" in text)
        assertTrue("drawWakeFamilyStroke" in text)
        assertTrue("drawCleaveTipGarnish" in text)
        assertTrue("onStrokeDebug" in text)
        // Wake overlay file untouched as exclusive gold arc
        val wake = File("app/src/main/java/com/towerofdarkness/app/ui/components/WakeStageOverlay.kt")
        val wsrc = if (wake.isFile) wake else File("src/main/java/com/towerofdarkness/app/ui/components/WakeStageOverlay.kt")
        assertTrue(wsrc.isFile)
        val wtext = wsrc.readText()
        assertTrue("WakeStageOverlay" in wtext)
        assertTrue("wake_vfx_slash" in wtext)
    }
}
