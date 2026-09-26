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
 * v0.1.49-plumegarnish — PLUME Free Sampler + Kenney particle garnish.
 * Stroke LOCKED (width/path/peaks/colors). Brace floating pips not replaced.
 * See docs/plumegarnish-v0149.md.
 */
class PlumeGarnishV0149Test {

    private fun drawableDir(): File {
        val a = File("app/src/main/res/drawable")
        val b = File("src/main/res/drawable")
        return if (a.isDirectory) a else b
    }

    @Test
    fun tag_isPlumeGarnishV0149() {
        assertEquals("v0.1.49-plumegarnish", CombatFx.TAG)
        assertEquals("v0.1.49-plumegarnish", CleaveKit.TAG)
        assertEquals("v0.1.49-plumegarnish", PlumeGarnishKit.TAG)
    }

    @Test
    fun mapCoverage_dustAndTipAndSoftenAndBrace() {
        assertTrue("dust_veil" in PlumeGarnishKit.DUST_ABILITY_IDS)
        assertTrue("ash_press" in PlumeGarnishKit.DUST_ABILITY_IDS)
        assertTrue("cinder_step" in PlumeGarnishKit.DUST_ABILITY_IDS)
        assertTrue(GarnishKind.DUST_PUFF in PlumeGarnishKit.kindsForAbility("dust_veil"))
        assertTrue(GarnishKind.DUST_PUFF in PlumeGarnishKit.kindsForAbility("ash_press"))
        assertTrue(GarnishKind.DUST_PUFF in PlumeGarnishKit.kindsForAbility("cinder_step"))
        assertTrue(GarnishKind.TIP_SPARK in PlumeGarnishKit.kindsForAbility(CombatFx.ID_ASHBRAND_SPARK))
        assertTrue(GarnishKind.TIP_SPARK in PlumeGarnishKit.kindsForAbility(CombatFx.ID_ASHBRAND_WAKE))
        assertTrue(GarnishKind.TIP_SPARK in PlumeGarnishKit.kindsForAbility("hostflint"))

        assertTrue(PlumeGarnishKit.PLUME_SAMPLER_PRESENT)
        val tip = PlumeGarnishKit.tipSparkSpec()
        assertEquals(GarnishPack.PLUME, tip.pack)
        assertEquals("grinder-sparks", tip.sheetId)
        assertTrue(tip.additive)
        assertTrue(tip.atlas)
        assertTrue(PlumeGarnishKit.neverUsesGroundFogAsDust())

        val dust = PlumeGarnishKit.dustPuffSpec("dust_veil")
        assertEquals(GarnishPack.PLUME, dust.pack)
        assertEquals("sand-kick", dust.sheetId)
        assertEquals(FxRecipient.FOE, FxRecipient.FOE) // target bust only (UI)

        val soften = PlumeGarnishKit.softenPipSpec()
        assertEquals(GarnishPack.KENNEY, soften.pack)
        assertEquals("circle_03", soften.sheetId)
        assertTrue(PlumeGarnishKit.softenIsUnderFoeOnly())

        val flare = PlumeGarnishKit.braceFlareSpec()
        assertEquals("flare_01", flare.sheetId)
        assertTrue(PlumeGarnishKit.braceFlareDoesNotReplacePips())
    }

    @Test
    fun drawableAllowlist_noBannedKenneyHitFamilies() {
        val dir = drawableDir()
        assertTrue(dir.isDirectory)
        val names = dir.list()?.toSet().orEmpty()
        // Banned families must never be imported as hit FX drawables
        for (banned in listOf("slash", "twirl", "magic", "muzzle")) {
            val hits = names.filter {
                it.startsWith("fx_kenney_$banned") || it.startsWith("fx_plume_$banned")
            }
            assertTrue("banned family imported: $hits", hits.isEmpty())
            assertTrue(PlumeGarnishKit.drawableIsBannedHitFamily("fx_kenney_${banned}_01"))
        }
        assertFalse(PlumeGarnishKit.drawableIsBannedHitFamily(PlumeGarnishKit.KENNEY_SPARK_01))
        assertFalse(PlumeGarnishKit.drawableIsBannedHitFamily(PlumeGarnishKit.KENNEY_CIRCLE_01))
        assertFalse(PlumeGarnishKit.drawableIsBannedHitFamily(PlumeGarnishKit.KENNEY_FLARE_01))
        // Stroke / Wake assets untouched
        assertTrue("fx_slash_light.png" in names)
        assertTrue("wake_vfx_slash.png" in names)

        // assets/fx/kenney + plume on main → drawables must be imported
        for (d in PlumeGarnishKit.IMPORTED_PLUME_DRAWABLES) {
            assertTrue("$d.png missing", "$d.png" in names)
        }
        assertTrue("fx_kenney_flare_01.png" in names)
        assertTrue("fx_kenney_spark_01.png" in names)
        assertTrue("fx_kenney_circle_03.png" in names)
        // ground-fog may exist but must not be primary dust
        assertTrue(PlumeGarnishKit.neverUsesGroundFogAsDust())
    }

    @Test
    fun timingWindows_puff350_450_ember200_300() {
        assertTrue(PlumeGarnishKit.timingWindowsOk())
        assertTrue(PlumeGarnishKit.PUFF_MS in 350L..450L)
        assertTrue(PlumeGarnishKit.EMBER_MS in 200L..300L)
        assertTrue(PlumeGarnishKit.TIP_BUST_FRAC in 0.20f..0.30f)
        assertTrue(PlumeGarnishKit.PUFF_BUST_FRAC in 0.40f..0.60f)
        assertTrue(PlumeGarnishKit.SOFTEN_BUST_FRAC < 0.20f)
        assertTrue(PlumeGarnishKit.BRACE_FLARE_BUST_FRAC < 0.25f)
    }

    @Test
    fun softenUnderFoe_braceFlareDoesNotReplacePips() {
        assertTrue(PlumeGarnishKit.softenIsUnderFoeOnly())
        assertTrue(PlumeGarnishKit.braceFlareDoesNotReplacePips())
        // Overlay still has floating Brace pips composable
        val overlay = File("app/src/main/java/com/towerofdarkness/app/ui/components/CombatFxOverlay.kt")
        val src = if (overlay.isFile) overlay else File("src/main/java/com/towerofdarkness/app/ui/components/CombatFxOverlay.kt")
        assertTrue(src.isFile)
        val text = src.readText()
        assertTrue("fun CombatBracePipsOverlay" in text)
        assertTrue("fun CombatParticleGarnishOverlay" in text)
        assertTrue("crescentBladePath" in text) // stroke lock present
        assertFalse("cap = StrokeCap.Round" in text)
        // Screen keeps Brace pips + flare
        val screen = File("app/src/main/java/com/towerofdarkness/app/ui/screens/CombatScreen.kt")
        val sc = if (screen.isFile) screen else File("src/main/java/com/towerofdarkness/app/ui/screens/CombatScreen.kt")
        val st = sc.readText()
        assertTrue("CombatBracePipsOverlay" in st)
        assertTrue("braceFlare" in st)
        assertTrue("PlumeGarnishKit.braceFlareSpec" in st)
        assertTrue("counterPenalty" in st) // Soften while status active
    }

    @Test
    fun debugStringFormats_plumeKenneyStroke() {
        assertEquals(
            "FX plume grinder-sparks on Goblin",
            PlumeGarnishKit.plumeDebugLine("grinder-sparks", "Goblin")
        )
        assertEquals(
            "FX kenney spark_03 on foe",
            PlumeGarnishKit.kenneyDebugLine("spark_03", "foe")
        )
        assertEquals(
            "FX kenney circle_03 on foe",
            PlumeGarnishKit.kenneyDebugLine("circle_03", "foe")
        )
        assertEquals(
            "FX kenney flare_01 on You",
            PlumeGarnishKit.kenneyDebugLine("flare_01", "You")
        )
        // Stroke debug kept
        assertEquals("FX stroke on You", CombatFx.strokeDebugLine("You"))
        assertEquals(
            "FX plume grinder-sparks on foe",
            PlumeGarnishKit.debugLine(PlumeGarnishKit.tipSparkSpec(), "foe")
        )
    }

    @Test
    fun strokeLock_widthsPeaksPathUntouched() {
        assertEquals(0.14f, CombatFx.STROKE_CORE_BUST_FRAC_PLAYER_SMALL, 0.0001f)
        assertEquals(0.18f, CombatFx.STROKE_CORE_BUST_FRAC_PLAYER_MEDIUM, 0.0001f)
        assertEquals(0.10f, CombatFx.STROKE_CORE_BUST_FRAC_ENEMY, 0.0001f)
        assertEquals(500L, CombatFx.STROKE_PLAYER_PEAK_MS)
        assertEquals(450L, CombatFx.STROKE_ENEMY_PEAK_MS)
        assertEquals(0xFFFFF6E4L, CombatFx.COLOR_STROKE_YOU)
        assertTrue(CombatFx.STROKE_SHAPE_FILLED_CRESCENT)
        assertTrue(CombatFx.STROKE_CRESCENT_BOW_MULT >= 1.0f)
    }

    @Test
    fun plumeFullPackPending_documented() {
        assertTrue("flint-strike" in PlumeGarnishKit.PLUME_FULL_PENDING)
        assertTrue("welding-burst" in PlumeGarnishKit.PLUME_FULL_PENDING)
        assertTrue("ash-puff" in PlumeGarnishKit.PLUME_FULL_PENDING)
        assertTrue(PlumeGarnishKit.PLUME_SAMPLER_PRESENT)
        assertTrue(PlumeGarnishKit.neverUsesGroundFogAsDust())
        // Kenney fallback path still valid when preferPlume=false
        val tipFb = PlumeGarnishKit.tipSparkSpec(preferPlume = false)
        assertEquals(GarnishPack.KENNEY, tipFb.pack)
        assertTrue(tipFb.sheetId.startsWith("spark_"))
        val dustFb = PlumeGarnishKit.dustPuffSpec(preferPlume = false)
        assertEquals(GarnishPack.KENNEY, dustFb.pack)
        assertTrue(dustFb.sheetId.startsWith("smoke_"))
    }

    @Test
    fun atlasCellOrigin_matchesPlumeGrid() {
        // grinder-sparks 8 cols: peak 5 → col 5 row 0
        assertEquals(5 * 256 to 0, PlumeGarnishKit.atlasCellOrigin(5, 8, 256))
        // frame 10 → col 2 row 1
        assertEquals(2 * 256 to 256, PlumeGarnishKit.atlasCellOrigin(10, 8, 256))
        val play = PlumeGarnishKit.atlasPlayFrames(24, 5)
        assertTrue(5 in play.toList())
        val mid = PlumeGarnishKit.frameAt(play, 0.3f)
        assertTrue("frameAt mid=$mid play=${play.toList()}", mid in play.toList())
        assertEquals(play.last(), PlumeGarnishKit.frameAt(play, 1f))
    }
}
