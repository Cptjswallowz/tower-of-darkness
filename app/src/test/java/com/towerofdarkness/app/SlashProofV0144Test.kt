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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.44-slashproof — prove fx_slash_light loaded and readable on busts.
 * BUST_COVERAGE 0.70–0.90 of bust diameter (half-stage); content crop; debug log;
 * peak 200ms; ≤500ms. Wake / Brace frozen. See docs/slashproof-v0144.md.
 */
class SlashProofV0144Test {

    @Test
    fun tag_isSlashProofV0144() {
        assertEquals("v0.1.44-slashproof", CombatFx.TAG)
        assertEquals("v0.1.44-slashproof", CleaveKit.TAG)
    }

    @Test
    fun bustCoverage_in70to90_halfStageFormula() {
        assertTrue(CleaveKit.bustCoverageInLockRange())
        assertEquals(0.82f, CleaveKit.BUST_COVERAGE, 0.001f)
        assertTrue(CleaveKit.BUST_COVERAGE in 0.70f..0.90f)
        // Wide stage (phone strip): half-stage based diameter, not shy min(w,h) alone
        val stageW = 1080f
        val stageH = 420f // 140.dp @3x
        val bust = CleaveKit.bustDiameterPx(stageW, stageH)
        val halfStage = stageW * 0.5f
        assertEquals(minOf(halfStage * CleaveKit.BUST_FROM_HALF_STAGE, stageH * CleaveKit.BUST_FROM_STAGE_H), bust, 0.01f)
        val small = CleaveKit.slashDrawPx(stageW, stageH, FxTier.SMALL)
        val medium = CleaveKit.slashDrawPx(stageW, stageH, FxTier.MEDIUM)
        assertEquals(bust * 0.82f, small, 0.01f)
        assertEquals(bust * 0.82f * 1.3f, medium, 0.01f)
        assertTrue(medium > small)
        // Effective crescent ≈ dst (crop zooms past empty padding). Prior full-cell
        // draw at min(w,h)*0.72 left only ~17–38% content visible (~50px). Cropped
        // dst at bust*0.82 fills ~82% of bust diameter — phone-readable.
        val contentFracPrior = 0.20f // peak-frame content width / 256
        val priorVisible = stageH * 0.72f * contentFracPrior
        assertTrue("cropped crescent must beat prior padded visible size", small > priorVisible * 2f)
        assertTrue(small / bust in 0.70f..0.90f)
    }

    @Test
    fun contentCrop_zoomsPastEmptyPadding() {
        assertEquals(144, CleaveKit.SLASH_CROP_PX)
        assertTrue(CleaveKit.SLASH_CROP_PX < CleaveKit.SLASH_CELL_PX)
        assertEquals(112, CleaveKit.SLASH_CROP_OX)
        assertEquals(24, CleaveKit.SLASH_CROP_OY)
        // Crop origin = cell origin + crop offset
        val (cx, cy) = CleaveKit.slashCellOrigin(6)
        val (ox, oy) = CleaveKit.slashCropOrigin(6)
        assertEquals(cx + CleaveKit.SLASH_CROP_OX, ox)
        assertEquals(cy + CleaveKit.SLASH_CROP_OY, oy)
        // Crop stays inside cell
        assertTrue(CleaveKit.SLASH_CROP_OX + CleaveKit.SLASH_CROP_PX <= CleaveKit.SLASH_CELL_PX)
        assertTrue(CleaveKit.SLASH_CROP_OY + CleaveKit.SLASH_CROP_PX <= CleaveKit.SLASH_CELL_PX)
    }

    @Test
    fun peakHold_200ms_totalWithin500() {
        assertEquals(200L, CleaveKit.PEAK_HOLD_MS)
        assertTrue(CleaveKit.PEAK_HOLD_MS in 180L..220L)
        assertTrue(CleaveKit.slashPlayWithinBudget())
        assertTrue(CombatFx.STROKE_SMALL_MS <= CleaveKit.MAX_SLASH_MS_1X)
        assertTrue(CombatFx.STROKE_MEDIUM_MS <= CleaveKit.MAX_SLASH_MS_1X)
        val delays = CleaveKit.slashFrameDelaysMs(CombatFx.STROKE_SMALL_MS, FxTier.SMALL)
        assertEquals(CombatFx.STROKE_SMALL_MS, delays.sum())
        val peakIdx = CleaveKit.SLASH_PLAY_FRAMES.indexOf(CleaveKit.SLASH_PEAK_FRAME)
        assertEquals(CleaveKit.PEAK_HOLD_MS, delays[peakIdx])
    }

    @Test
    fun playerSmall_hasHostflintAshPress() {
        assertTrue("hostflint" in CombatFx.PLAYER_SMALL)
        assertTrue("ash_press" in CombatFx.PLAYER_SMALL)
        for (id in listOf("hostflint", "ash_press")) {
            val s = CombatFx.specForPlayer(id)
            assertEquals(FxTier.SMALL, s.tier)
            assertNotNull(s.stroke)
            assertEquals(FxRecipient.FOE, s.recipient)
            assertTrue(s.useCleaveHitFlash)
            assertEquals(CombatFx.COLOR_YOU, s.stroke!!.colorArgb)
        }
    }

    @Test
    fun playerMedium_hasTowerPike() {
        assertTrue("tower_pike" in CombatFx.PLAYER_MEDIUM)
        val pike = CombatFx.specForPlayer("tower_pike")
        assertEquals(FxTier.MEDIUM, pike.tier)
        assertNotNull(pike.stroke)
        assertEquals(1.3f, CleaveKit.slashScale(pike.tier), 0.001f)
        assertTrue(pike.useCleaveHitFlash)
    }

    @Test
    fun enemy_hasHitNipShivCleaveClub() {
        for (id in listOf("hit", "nip", "shiv", "club")) {
            assertTrue("$id in ENEMY_SMALL", id in CombatFx.ENEMY_SMALL)
            val s = CombatFx.specForEnemy(id, EnemyKind.GOBLIN)
            assertEquals(FxTier.SMALL, s.tier)
            assertNotNull(s.stroke)
            assertEquals(FxRecipient.YOU, s.recipient)
        }
        assertTrue("cleave" in CombatFx.ENEMY_MEDIUM)
        val cleave = CombatFx.specForEnemy("cleave", EnemyKitRole.STURDY_ORC)
        assertEquals(FxTier.MEDIUM, cleave.tier)
        assertNotNull(cleave.stroke)
        assertEquals(FxRecipient.YOU, cleave.recipient)
    }

    @Test
    fun debugLog_formatConstant_andHelper() {
        assertEquals("FX slash-light on ", CombatFx.SLASH_LIGHT_DEBUG_FMT)
        assertEquals("TodFx", CombatFx.LOG_TAG_TOD_FX)
        assertEquals("FX slash-light on You", CombatFx.slashLightDebugLine("You"))
        assertEquals("FX slash-light on Goblin", CombatFx.slashLightDebugLine("Goblin"))
        assertEquals("FX slash-light on foe", CombatFx.slashLightDebugLine("foe"))
        assertEquals("You", CombatFx.slashLightTargetLabel(FxRecipient.YOU, "Goblin"))
        assertEquals("Goblin", CombatFx.slashLightTargetLabel(FxRecipient.FOE, "Goblin"))
        assertEquals("foe", CombatFx.slashLightTargetLabel(FxRecipient.FOE, null))
        assertEquals("foe", CombatFx.slashLightTargetLabel(FxRecipient.FOE, "  "))
    }

    @Test
    fun drawableName_andFilePresent() {
        assertEquals("fx_slash_light", CleaveKit.SLASH_DRAWABLE)
        val drawable = File("src/main/res/drawable")
        val dir = if (drawable.isDirectory) drawable else File("app/src/main/res/drawable")
        assertTrue(dir.isDirectory)
        assertTrue("fx_slash_light.png" in dir.list()!!.toSet())
        val png = File(dir, "fx_slash_light.png")
        assertTrue("sheet must be non-trivial bytes", png.length() > 10_000L)
    }

    @Test
    fun rDrawable_linkageName_matchesBasename() {
        // Compile-time R.drawable.fx_slash_light is generated from this basename.
        // Unit test cannot touch R without android jar; lock the basename contract.
        assertEquals("fx_slash_light", CleaveKit.SLASH_DRAWABLE)
        assertFalse(CleaveKit.SLASH_DRAWABLE.contains('-'))
        assertFalse(CleaveKit.SLASH_DRAWABLE.contains('.'))
    }

    @Test
    fun wakeAndBrace_frozen() {
        assertTrue(WakeArt.WAKE_ART_FULLY_FROZEN)
        val wake = CombatFx.specForWake()
        assertNull(wake.stroke)
        assertTrue(wake.useWakeSlash)
        assertFalse(wake.useCleaveHitFlash)
        val mantle = CombatFx.specForPlayer("iron_mantle")
        assertEquals(FxTier.NO_STROKE, mantle.tier)
        assertNull(mantle.stroke)
    }

    @Test
    fun mediumStaysAtLeast1_3xSmall() {
        assertEquals(1.3f, CleaveKit.MEDIUM_SCALE, 0.001f)
        assertTrue(CleaveKit.slashScale(FxTier.MEDIUM) >= CleaveKit.slashScale(FxTier.SMALL) * 1.3f - 0.001f)
    }

    @Test
    fun speed2x_halvesHolds() {
        assertEquals(200L, CombatFx.fxHoldMs(CombatFx.STROKE_SMALL_MS, 2))
        assertEquals(100L, CombatFx.fxHoldMs(CleaveKit.PEAK_HOLD_MS, 2))
    }
}
