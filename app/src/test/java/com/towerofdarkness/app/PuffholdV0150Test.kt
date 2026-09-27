package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.CleaveKit
import com.towerofdarkness.app.domain.combat.CombatFx
import com.towerofdarkness.app.domain.combat.FxRecipient
import com.towerofdarkness.app.domain.combat.GarnishKind
import com.towerofdarkness.app.domain.combat.GarnishPack
import com.towerofdarkness.app.domain.combat.PlumeGarnishKit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.50-puffhold — ash-puff Dust/Ash; footstep Cinder land; puff+spark 400–500ms;
 * Soften/Brace; stroke LOCKED. See docs/puffhold-v0150.md.
 */
class PuffholdV0150Test {

    private fun drawableDir(): File {
        val a = File("app/src/main/res/drawable")
        val b = File("src/main/res/drawable")
        return if (a.isDirectory) a else b
    }

    @Test
    fun tag_isPuffholdV0150() {
        assertEquals("v0.1.50-puffhold", CombatFx.TAG)
        assertEquals("v0.1.50-puffhold", CleaveKit.TAG)
        assertEquals("v0.1.50-puffhold", PlumeGarnishKit.TAG)
    }

    @Test
    fun sheetMap_dustAshToAshPuff_cinderToFootstep() {
        val dust = PlumeGarnishKit.dustPuffSpec("dust_veil")
        assertEquals(GarnishPack.PLUME, dust.pack)
        assertEquals("ash-puff", dust.sheetId)
        assertEquals(PlumeGarnishKit.PLUME_ASH_PUFF, dust.drawable)
        assertFalse("Dust Veil must not use footstep-puff", dust.sheetId == "footstep-puff")

        val ash = PlumeGarnishKit.dustPuffSpec("ash_press")
        assertEquals("ash-puff", ash.sheetId)
        assertEquals(PlumeGarnishKit.PLUME_ASH_PUFF, ash.drawable)

        val cinder = PlumeGarnishKit.dustPuffSpec("cinder_step")
        assertEquals("footstep-puff", cinder.sheetId)
        assertEquals(PlumeGarnishKit.PLUME_FOOTSTEP_PUFF, cinder.drawable)

        val tip = PlumeGarnishKit.tipSparkSpec()
        assertEquals("grinder-sparks", tip.sheetId)
        assertTrue(tip.additive)
        assertTrue(tip.bustFrac in 0.20f..0.30f)
        assertEquals(PlumeGarnishKit.COLOR_RUST_GOLD, tip.tintArgb)
        assertTrue(PlumeGarnishKit.neverUsesGroundFogAsDust())
    }

    @Test
    fun holdMs_puffAndSpark_400_500() {
        assertTrue(PlumeGarnishKit.timingWindowsOk())
        assertTrue(
            "puff=${PlumeGarnishKit.PUFF_MS}",
            PlumeGarnishKit.PUFF_MS in 400L..500L
        )
        assertTrue(
            "spark/ember=${PlumeGarnishKit.EMBER_MS}",
            PlumeGarnishKit.EMBER_MS in 400L..500L
        )
        assertTrue(PlumeGarnishKit.SPARK_MS in 400L..500L)
        assertEquals(PlumeGarnishKit.PUFF_MS, PlumeGarnishKit.dustPuffSpec().holdMs)
        assertEquals(PlumeGarnishKit.EMBER_MS, PlumeGarnishKit.tipSparkSpec().holdMs)
        assertTrue(PlumeGarnishKit.PUFF_MS_LO == 400L && PlumeGarnishKit.PUFF_MS_HI == 500L)
        assertTrue(PlumeGarnishKit.EMBER_MS_LO == 400L && PlumeGarnishKit.EMBER_MS_HI == 500L)
    }

    @Test
    fun atlasPlayFrames_peakDwellForStills() {
        val play = PlumeGarnishKit.atlasPlayFrames(24, 10)
        assertTrue(play.size >= 6)
        val peakCount = play.count { it == 10 }
        assertTrue("peak dwell count=$peakCount play=${play.toList()}", peakCount >= 3)
        assertEquals(10 * 256 % (8 * 256), PlumeGarnishKit.atlasCellOrigin(10, 8, 256).first % (8 * 256))
        assertEquals(2 * 256 to 256, PlumeGarnishKit.atlasCellOrigin(10, 8, 256))
    }

    @Test
    fun softenBrace_andStrokeLock() {
        val soften = PlumeGarnishKit.softenPipSpec()
        assertEquals("circle_03", soften.sheetId)
        assertTrue(PlumeGarnishKit.softenIsUnderFoeOnly())
        val flare = PlumeGarnishKit.braceFlareSpec()
        assertEquals("flare_01", flare.sheetId)
        assertTrue(PlumeGarnishKit.braceFlareDoesNotReplacePips())

        assertEquals(0.14f, CombatFx.STROKE_CORE_BUST_FRAC_PLAYER_SMALL, 0.0001f)
        assertEquals(0.18f, CombatFx.STROKE_CORE_BUST_FRAC_PLAYER_MEDIUM, 0.0001f)
        assertEquals(0.10f, CombatFx.STROKE_CORE_BUST_FRAC_ENEMY, 0.0001f)
        assertEquals(500L, CombatFx.STROKE_PLAYER_PEAK_MS)
        assertEquals(450L, CombatFx.STROKE_ENEMY_PEAK_MS)
        assertEquals(0xFFFFF6E4L, CombatFx.COLOR_STROKE_YOU)
        assertTrue(CombatFx.STROKE_SHAPE_FILLED_CRESCENT)

        val overlay = File("app/src/main/java/com/towerofdarkness/app/ui/components/CombatFxOverlay.kt")
        val src = if (overlay.isFile) overlay else File("src/main/java/com/towerofdarkness/app/ui/components/CombatFxOverlay.kt")
        val text = src.readText()
        assertTrue("fun CombatBracePipsOverlay" in text)
        assertTrue("fun CombatParticleGarnishOverlay" in text)
        assertTrue("crescentBladePath" in text)
        assertFalse("cap = StrokeCap.Round" in text)
    }

    @Test
    fun drawableAshPuffImported_noBannedHits_noFogWallDust() {
        val dir = drawableDir()
        assertTrue(dir.isDirectory)
        val names = dir.list()?.toSet().orEmpty()
        assertTrue("fx_plume_ash_puff.png" in names)
        for (d in PlumeGarnishKit.IMPORTED_PLUME_DRAWABLES) {
            assertTrue("$d.png missing", "$d.png" in names)
        }
        assertTrue("fx_kenney_circle_03.png" in names)
        assertTrue("fx_kenney_flare_01.png" in names)
        assertFalse("ash-puff" in PlumeGarnishKit.PLUME_FULL_PENDING)
        assertTrue(PlumeGarnishKit.neverUsesGroundFogAsDust())
        for (banned in listOf("slash", "twirl", "magic", "muzzle")) {
            assertTrue(PlumeGarnishKit.drawableIsBannedHitFamily("fx_kenney_${banned}_01"))
        }
    }

    @Test
    fun kindsAndDebug() {
        assertTrue(GarnishKind.DUST_PUFF in PlumeGarnishKit.kindsForAbility("dust_veil"))
        assertTrue(GarnishKind.DUST_PUFF in PlumeGarnishKit.kindsForAbility("ash_press"))
        assertTrue(GarnishKind.DUST_PUFF in PlumeGarnishKit.kindsForAbility("cinder_step"))
        assertTrue(GarnishKind.TIP_SPARK in PlumeGarnishKit.kindsForAbility(CombatFx.ID_ASHBRAND_SPARK))
        assertEquals(
            "FX plume ash-puff on foe",
            PlumeGarnishKit.debugLine(PlumeGarnishKit.dustPuffSpec("dust_veil"), "foe")
        )
        assertEquals(
            "FX plume grinder-sparks on Goblin",
            PlumeGarnishKit.plumeDebugLine("grinder-sparks", "Goblin")
        )
        assertEquals("FX stroke on You", CombatFx.strokeDebugLine("You"))
    }
}
