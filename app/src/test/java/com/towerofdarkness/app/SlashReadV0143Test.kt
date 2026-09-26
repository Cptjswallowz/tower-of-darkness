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
 * v0.1.43-slashread — phone-readable CLEAVE slash-light on busts.
 * Scale ~60–80% bust, Medium 1.3×, peak hold ~200ms, contact hit-flash on damage cuts.
 * Wake / Brace frozen. See docs/slashread-v0143.md.
 */
class SlashReadV0143Test {

    @Test
    fun tag_isSlashReadV0143() {
        assertEquals("v0.1.49-plumegarnish", CombatFx.TAG)
        assertEquals("v0.1.49-plumegarnish", CleaveKit.TAG)
    }

    @Test
    fun mediumScale_is1_3() {
        assertEquals(1.3f, CleaveKit.MEDIUM_SCALE, 0.001f)
        assertEquals(1.3f, CleaveKit.slashScale(FxTier.MEDIUM), 0.001f)
        assertEquals(1.0f, CleaveKit.slashScale(FxTier.SMALL), 0.001f)
        assertTrue(CleaveKit.slashScale(FxTier.MEDIUM) > CleaveKit.slashScale(FxTier.SMALL))
    }

    @Test
    fun bustCoverage_in60to80() {
        assertTrue(CleaveKit.bustCoverageInLockRange())
        assertTrue(CleaveKit.BUST_COVERAGE in 0.70f..0.90f)
        val side = 400f
        assertTrue(CleaveKit.slashDrawPx(side, FxTier.MEDIUM) > CleaveKit.slashDrawPx(side, FxTier.SMALL))
        // Opaque crescent width (content-fill compensated) lands in coverage lock
        assertEquals(
            CleaveKit.bustWidthPx(side) * CleaveKit.BUST_COVERAGE,
            CleaveKit.opaqueCrescentWidthPx(side, FxTier.SMALL),
            0.01f
        )
        assertEquals(
            CleaveKit.bustWidthPx(side) * CleaveKit.BUST_COVERAGE * 1.3f,
            CleaveKit.opaqueCrescentWidthPx(side, FxTier.MEDIUM),
            0.01f
        )
    }

    @Test
    fun slashPlay_within500ms_peakInWindow() {
        assertTrue(CleaveKit.slashPlayWithinBudget())
        assertTrue(CombatFx.STROKE_SMALL_MS <= CleaveKit.MAX_SLASH_MS_1X)
        assertTrue(CombatFx.STROKE_MEDIUM_MS <= CleaveKit.MAX_SLASH_MS_1X)
        assertTrue(CleaveKit.slashPeakInPlayWindow())
        assertEquals(6, CleaveKit.SLASH_PEAK_FRAME)
        assertTrue(CleaveKit.SLASH_PEAK_FRAME in 4..10)
        assertEquals(200L, CleaveKit.PEAK_HOLD_MS)
        assertEquals(220L, CleaveKit.PEAK_HOLD_MEDIUM_MS)
        assertTrue(CleaveKit.PEAK_HOLD_MS in 180L..220L)
        assertTrue(CleaveKit.PEAK_HOLD_MEDIUM_MS in 180L..220L)
    }

    @Test
    fun peakHold_lingersInsideStrokeBudget() {
        val smallDelays = CleaveKit.slashFrameDelaysMs(CombatFx.STROKE_SMALL_MS, FxTier.SMALL)
        assertEquals(CleaveKit.SLASH_PLAY_FRAMES.size, smallDelays.size)
        assertEquals(CombatFx.STROKE_SMALL_MS, smallDelays.sum())
        val peakIdx = CleaveKit.SLASH_PLAY_FRAMES.indexOf(CleaveKit.SLASH_PEAK_FRAME)
        assertTrue(peakIdx >= 0)
        assertEquals(CleaveKit.peakHoldFor(CombatFx.STROKE_SMALL_MS, FxTier.SMALL), smallDelays[peakIdx])
        assertTrue(smallDelays[peakIdx] >= CleaveKit.PEAK_HOLD_MS)
        // Peak is the longest frame
        assertTrue(smallDelays[peakIdx] >= smallDelays.maxOrNull()!!)

        val medDelays = CleaveKit.slashFrameDelaysMs(CombatFx.STROKE_MEDIUM_MS, FxTier.MEDIUM)
        assertEquals(CombatFx.STROKE_MEDIUM_MS, medDelays.sum())
        assertEquals(CleaveKit.peakHoldFor(CombatFx.STROKE_MEDIUM_MS, FxTier.MEDIUM), medDelays[peakIdx])
        assertTrue(medDelays[peakIdx] > smallDelays[peakIdx])
    }

    @Test
    fun drawableName_isFxSlashLight() {
        assertEquals("fx_slash_light", CleaveKit.SLASH_DRAWABLE)
        val drawable = File("src/main/res/drawable")
        val dir = if (drawable.isDirectory) drawable else File("app/src/main/res/drawable")
        assertTrue(dir.isDirectory)
        assertTrue("fx_slash_light.png" in dir.list()!!.toSet())
    }

    @Test
    fun playerSmall_containsHostflintCinderStepEmberbrand() {
        assertTrue("hostflint" in CombatFx.PLAYER_SMALL)
        assertTrue("cinder_step" in CombatFx.PLAYER_SMALL)
        assertTrue("emberbrand" in CombatFx.PLAYER_SMALL)
        for (id in listOf("hostflint", "cinder_step", "emberbrand")) {
            val s = CombatFx.specForPlayer(id)
            assertEquals(FxTier.SMALL, s.tier)
            assertNotNull(s.stroke)
            assertEquals(FxRecipient.FOE, s.recipient)
            assertTrue(s.useCleaveHitFlash)
            assertEquals(CleaveKit.CONTACT_HIT_FLASH_MS, s.hitFlashMs)
            assertEquals(CombatFx.COLOR_STROKE_YOU, s.stroke!!.colorArgb)
        }
    }

    @Test
    fun playerMedium_containsTowerPike() {
        assertTrue("tower_pike" in CombatFx.PLAYER_MEDIUM)
        val pike = CombatFx.specForPlayer("tower_pike")
        assertEquals(FxTier.MEDIUM, pike.tier)
        assertNotNull(pike.stroke)
        assertEquals(FxRecipient.FOE, pike.recipient)
        assertEquals(1.3f, CleaveKit.slashScale(pike.tier), 0.001f)
        assertTrue(pike.useCleaveHitFlash)
        assertEquals(CleaveKit.CONTACT_HIT_FLASH_MS, pike.hitFlashMs)
        assertTrue(pike.strokeMs <= CleaveKit.MAX_SLASH_MS_1X)
    }

    @Test
    fun enemy_hasHitClubCleaveShivNip() {
        for (id in listOf("hit", "club", "shiv", "nip")) {
            assertTrue("$id in ENEMY_SMALL", id in CombatFx.ENEMY_SMALL)
            val s = CombatFx.specForEnemy(id, EnemyKind.GOBLIN)
            assertEquals(FxTier.SMALL, s.tier)
            assertNotNull(s.stroke)
            assertEquals(FxRecipient.YOU, s.recipient)
            assertTrue(s.useCleaveHitFlash)
        }
        assertTrue("cleave" in CombatFx.ENEMY_MEDIUM)
        val cleave = CombatFx.specForEnemy("cleave", EnemyKitRole.STURDY_ORC)
        assertEquals(FxTier.MEDIUM, cleave.tier)
        assertNotNull(cleave.stroke)
        assertEquals(FxRecipient.YOU, cleave.recipient)
        assertTrue(cleave.useCleaveHitFlash)
        assertEquals(1.3f, CleaveKit.slashScale(cleave.tier), 0.001f)
        // Keep gate_pulse / seal_pulse / coal_slam
        assertTrue("gate_pulse" in CombatFx.ENEMY_SMALL)
        assertTrue("seal_pulse" in CombatFx.ENEMY_MEDIUM)
        assertTrue("coal_slam" in CombatFx.ENEMY_MEDIUM)
    }

    @Test
    fun wakeAndBrace_unchanged_noSlashLightOnWake() {
        assertTrue(WakeArt.WAKE_ART_FULLY_FROZEN)
        val wake = CombatFx.specForWake()
        assertEquals(FxTier.WAKE, wake.tier)
        assertTrue(wake.useWakeSlash)
        assertNull(wake.stroke)
        assertFalse(wake.useCleaveHitFlash)

        val mantle = CombatFx.specForPlayer("iron_mantle")
        assertEquals(FxTier.NO_STROKE, mantle.tier)
        assertNull(mantle.stroke)
        assertFalse(mantle.useCleaveHitFlash)

        val spark = CombatFx.specForSpark()
        assertTrue(spark.useCleaveHitFlash)
        assertEquals(CleaveKit.HIT_FLASH_MS, spark.hitFlashMs)
        assertNotNull(spark.stroke) // v0.1.46: short drawn stroke
        assertFalse(spark.useWakeSlash)
    }

    @Test
    fun aimLocks_halfStage_kept() {
        assertEquals(0f, CombatFx.recipientClipXFrac(FxRecipient.YOU).start, 0.0001f)
        assertEquals(0.5f, CombatFx.recipientClipXFrac(FxRecipient.YOU).endInclusive, 0.0001f)
        assertEquals(0.5f, CombatFx.recipientClipXFrac(FxRecipient.FOE).start, 0.0001f)
        assertFalse(CombatFx.slashCutGeom(FxRecipient.FOE, FxTier.MEDIUM).spansBothBusts())
    }

    @Test
    fun speed2x_halvesHolds() {
        assertEquals(CombatFx.STROKE_SMALL_MS / 2, CombatFx.fxHoldMs(CombatFx.STROKE_SMALL_MS, 2))
        assertEquals(CombatFx.STROKE_MEDIUM_MS / 2, CombatFx.fxHoldMs(CombatFx.STROKE_MEDIUM_MS, 2))
        assertEquals(100L, CombatFx.fxHoldMs(CleaveKit.CONTACT_HIT_FLASH_MS, 2))
        assertEquals(160L, CombatFx.fxHoldMs(CleaveKit.HIT_FLASH_MS, 2))
    }
}
