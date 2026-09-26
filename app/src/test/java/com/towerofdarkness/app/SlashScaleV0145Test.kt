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
import java.security.MessageDigest

/**
 * v0.1.47-strokethick — scale slash overlay to bust WIDTH; tight crop; color FREE.
 * Same fx_slash_light sheet. See docs/slashscale-v0145.md.
 */
class SlashScaleV0145Test {

    @Test
    fun tag_isSlashScaleV0145() {
        assertEquals("v0.1.47-strokethick", CombatFx.TAG)
        assertEquals("v0.1.47-strokethick", CleaveKit.TAG)
    }

    @Test
    fun sameDrawable_basenameAndFilePresent() {
        assertEquals("fx_slash_light", CleaveKit.SLASH_DRAWABLE)
        val drawable = File("src/main/res/drawable")
        val dir = if (drawable.isDirectory) drawable else File("app/src/main/res/drawable")
        assertTrue(dir.isDirectory)
        val png = File(dir, "fx_slash_light.png")
        assertTrue(png.isFile)
        assertTrue(png.length() > 10_000L)
        val md5 = MessageDigest.getInstance("MD5")
            .digest(png.readBytes())
            .joinToString("") { "%02x".format(it) }
        assertEquals("e02410214790304f8d2e408472cc6cc0", md5)
    }

    @Test
    fun bustCoverage_in70to90() {
        assertTrue(CleaveKit.bustCoverageInLockRange())
        assertEquals(0.85f, CleaveKit.BUST_COVERAGE, 0.001f)
        assertTrue(CleaveKit.BUST_COVERAGE in 0.70f..0.90f)
        assertTrue(CleaveKit.BUST_WIDTH_FRAC in 0.70f..0.85f)
        assertEquals(0.78f, CleaveKit.BUST_WIDTH_FRAC, 0.001f)
    }

    @Test
    fun crop_tightAroundOpaqueUnion_insideCell() {
        assertEquals(140, CleaveKit.SLASH_CROP_PX)
        assertEquals(115, CleaveKit.SLASH_CROP_OX)
        assertEquals(26, CleaveKit.SLASH_CROP_OY)
        assertEquals(102, CleaveKit.OPAQUE_UNION_W)
        assertEquals(133, CleaveKit.OPAQUE_UNION_H)
        assertTrue(CleaveKit.cropInsideCellWithFill())
        // Crop covers measured union (153,30)-(255,163)
        assertTrue(CleaveKit.SLASH_CROP_OX <= 153)
        assertTrue(CleaveKit.SLASH_CROP_OY <= 30)
        assertTrue(CleaveKit.SLASH_CROP_OX + CleaveKit.SLASH_CROP_PX >= 255)
        assertTrue(CleaveKit.SLASH_CROP_OY + CleaveKit.SLASH_CROP_PX >= 163)
        val bboxFill =
            (CleaveKit.OPAQUE_UNION_W * CleaveKit.OPAQUE_UNION_H).toFloat() /
                (CleaveKit.SLASH_CROP_PX * CleaveKit.SLASH_CROP_PX).toFloat()
        assertTrue("bbox fill $bboxFill", bboxFill >= 0.65f)
        assertEquals(
            CleaveKit.OPAQUE_UNION_W.toFloat() / CleaveKit.SLASH_CROP_PX,
            CleaveKit.CONTENT_WIDTH_FRAC,
            0.001f
        )
    }

    @Test
    fun scaleFormula_opaqueLandsInLock_stage360() {
        // Report example: stageW=360dp density1
        val stageW = 360f
        val stageH = 180f
        val halfStage = stageW * 0.5f
        val bust = CleaveKit.bustWidthPx(stageW)
        assertEquals(halfStage * 0.78f, bust, 0.01f) // 140.4
        val smallDraw = CleaveKit.slashDrawPx(stageW, stageH, FxTier.SMALL)
        val mediumDraw = CleaveKit.slashDrawPx(stageW, stageH, FxTier.MEDIUM)
        val smallOpaque = CleaveKit.opaqueCrescentWidthPx(stageW, FxTier.SMALL)
        val mediumOpaque = CleaveKit.opaqueCrescentWidthPx(stageW, FxTier.MEDIUM)
        // opaque = bust * coverage; draw = opaque / contentFill
        assertEquals(bust * 0.85f, smallOpaque, 0.05f)
        assertEquals(smallOpaque / CleaveKit.CONTENT_WIDTH_FRAC, smallDraw, 0.05f)
        assertEquals(smallOpaque * 1.3f, mediumOpaque, 0.05f)
        assertTrue(mediumDraw > smallDraw)
        assertTrue(mediumOpaque >= smallOpaque * 1.3f - 0.01f)
        assertTrue(smallOpaque / bust in 0.70f..0.90f)
        assertTrue(mediumOpaque / bust in 0.70f..1.20f) // Medium may exceed 0.90 of bust
        // Must beat the FAIL blob (~32dp visible on ~80dp dst)
        assertTrue("Small opaque must beat prior ~32dp blob", smallOpaque > 64f)
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
    fun debugLog_formatKept() {
        assertEquals("FX slash-light on ", CombatFx.SLASH_LIGHT_DEBUG_FMT)
        assertEquals("TodFx", CombatFx.LOG_TAG_TOD_FX)
        assertEquals("FX slash-light on You", CombatFx.slashLightDebugLine("You"))
        assertEquals("FX slash-light on Warden", CombatFx.slashLightDebugLine("Warden"))
        assertEquals("You", CombatFx.slashLightTargetLabel(FxRecipient.YOU, "Goblin"))
        assertEquals("Goblin", CombatFx.slashLightTargetLabel(FxRecipient.FOE, "Goblin"))
    }

    @Test
    fun mediumAtLeast1_3xSmall() {
        assertEquals(1.3f, CleaveKit.MEDIUM_SCALE, 0.001f)
        assertTrue(
            CleaveKit.slashScale(FxTier.MEDIUM) >=
                CleaveKit.slashScale(FxTier.SMALL) * 1.3f - 0.001f
        )
    }

    @Test
    fun map_playerEnemySlashLight_unchanged() {
        for (id in listOf("hostflint", "ash_press", "tower_pike")) {
            val s = CombatFx.specForPlayer(id)
            assertNotNull(s.stroke)
            assertEquals(FxRecipient.FOE, s.recipient)
        }
        assertEquals(FxTier.MEDIUM, CombatFx.specForPlayer("tower_pike").tier)
        for (id in listOf("hit", "nip", "shiv", "club")) {
            val s = CombatFx.specForEnemy(id, EnemyKind.GOBLIN)
            assertNotNull(s.stroke)
            assertEquals(FxRecipient.YOU, s.recipient)
        }
        val cleave = CombatFx.specForEnemy("cleave", EnemyKitRole.STURDY_ORC)
        assertEquals(FxTier.MEDIUM, cleave.tier)
        assertEquals(FxRecipient.YOU, cleave.recipient)
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
    fun speed2x_halvesHolds() {
        assertEquals(CombatFx.STROKE_SMALL_MS / 2, CombatFx.fxHoldMs(CombatFx.STROKE_SMALL_MS, 2))
        assertEquals(100L, CombatFx.fxHoldMs(CleaveKit.PEAK_HOLD_MS, 2))
    }
}
