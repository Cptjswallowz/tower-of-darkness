package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.glyphs.SkillGlyph
import com.towerofdarkness.app.domain.volume.VolumeArt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.1.20-volume — You + Ash-Warden + skill tiles + Ashbrand get volume;
 * Seal-Warden / trash / Wake VFX untouched; glyph motifs not restyled;
 * Compose chrome does not double Art bake.
 */
class VolumeArtV0120Test {

    @Test
    fun tag_andSurfaces_locked() {
        assertEquals("v0.1.20-volume", VolumeArt.TAG)
        val surfaces = VolumeArt.volumeSurfaces()
        assertTrue(surfaces.contains("portrait_you"))
        assertTrue(surfaces.contains("portrait_ash_warden"))
        assertTrue(surfaces.contains("skill_glyph_tiles"))
        assertTrue(surfaces.contains("ashbrand_icon"))
        assertEquals(4, surfaces.size)
    }

    @Test
    fun playerAndAshWarden_getVolume() {
        assertTrue(VolumeArt.appliesToPlayerPortrait())
        assertTrue(VolumeArt.appliesToEnemy(EnemyKind.ASH_WARDEN))
        assertTrue(VolumeArt.appliesToSkillGlyphTiles())
        assertTrue(VolumeArt.appliesToAshbrandIcon())
    }

    @Test
    fun sealWardenAndTrash_noVolume() {
        assertFalse(VolumeArt.appliesToSealWarden())
        assertFalse(VolumeArt.appliesToTrash())
        assertFalse(VolumeArt.appliesToEnemy(EnemyKind.DRAGON))
        listOf(EnemyKind.GOBLIN, EnemyKind.ORC, EnemyKind.TROLL, EnemyKind.SPIDER).forEach { kind ->
            assertFalse("no volume for $kind", VolumeArt.appliesToEnemy(kind))
        }
    }

    @Test
    fun wakeVfxAndSpark_frozenNoVolume() {
        assertFalse(VolumeArt.appliesToWakeVfx())
        assertFalse(VolumeArt.appliesToAshbrandSpark())
    }

    @Test
    fun glyphSymbols_notRestyled_mapUnchanged() {
        assertFalse(VolumeArt.glyphSymbolsRestyled())
        assertEquals("glyph_hostflint", SkillGlyph.drawableName("hostflint"))
        assertEquals("glyph_vow_plate", SkillGlyph.drawableName("vow_plate"))
        assertEquals(17, SkillGlyph.mappedSkillIds().size)
        assertTrue(VolumeArt.vowPlatePlateClarifyAllowed())
    }

    @Test
    fun artBake_coversPortraitsGlyphsAshbrand_notWake() {
        val baked = VolumeArt.artBakedDrawables()
        assertTrue(baked.contains("portrait_you"))
        assertTrue(baked.contains("portrait_ash_warden"))
        assertTrue(baked.contains("ashbrand_icon"))
        assertTrue(baked.contains("glyph_vow_plate"))
        assertEquals(20, baked.size)
        assertFalse(baked.contains("ashbrand_spark"))
        assertFalse(baked.any { it.startsWith("wake_vfx_") })
    }

    @Test
    fun foldDarkened_portraitsAndAshbrand() {
        val folds = VolumeArt.foldDarkenedDrawables()
        assertTrue(folds.contains("portrait_you"))
        assertTrue(folds.contains("portrait_ash_warden"))
        assertTrue(folds.contains("ashbrand_icon"))
    }

    @Test
    fun dimmedGlyph_staysReadable() {
        assertTrue(VolumeArt.DIMMED_GLYPH_ALPHA >= 0.55f)
        assertTrue(VolumeArt.DIMMED_GLYPH_ALPHA <= 0.85f)
        assertTrue(VolumeArt.DIMMED_SLOT_ALPHA >= 0.55f)
        assertTrue(VolumeArt.SHADOW_ALPHA in 0.2f..0.55f)
        assertTrue(VolumeArt.RIM_STROKE_FRACTION in 0.02f..0.08f)
    }

    @Test
    fun goldAshArgb_isWarmGoldFamily() {
        val argb = VolumeArt.GOLD_ASH_ARGB
        val a = (argb ushr 24) and 0xFF
        val r = (argb ushr 16) and 0xFF
        val g = (argb ushr 8) and 0xFF
        val b = argb and 0xFF
        assertTrue("rim should be visible", a >= 0x80)
        assertTrue("warm gold", r > 0x90 && g > 0x70)
        assertTrue("not pure yellow blowout", b < r)
    }

    @Test
    fun composeChrome_doesNotDoubleArtBake() {
        assertFalse(VolumeArt.composeChromeDrawsShadowRim())
    }

    @Test
    fun vowPlate_distinctFromIronMantle() {
        assertEquals("glyph_vow_plate", SkillGlyph.drawableName("vow_plate"))
        assertEquals("glyph_iron_mantle", SkillGlyph.drawableName("iron_mantle"))
        assertTrue(SkillGlyph.drawableName("vow_plate") != SkillGlyph.drawableName("iron_mantle"))
    }
}
