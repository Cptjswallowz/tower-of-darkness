package com.towerofdarkness.app

import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.glyphs.SkillGlyph
import com.towerofdarkness.app.domain.glyphs.SkillJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.1.19-glyphs — every catalog skill has a glyph; Ashbrand/weapon has none;
 * job colors locked; no invented ids.
 */
class SkillGlyphV0119Test {

    @Test
    fun everyCatalogSkill_hasGlyph() {
        CardCatalog.all.forEach { card ->
            assertTrue("missing glyph for ${card.id}", SkillGlyph.hasGlyph(card.id))
            assertNotNull(SkillGlyph.drawableName(card.id))
            assertNotNull(SkillGlyph.job(card.id))
        }
        assertTrue(SkillGlyph.catalogFullyMapped())
        assertEquals(11, SkillGlyph.mappedSkillIds().size)
        assertEquals(11, CardCatalog.all.size)
    }

    @Test
    fun ashbrandAndWeapon_haveNoGlyph() {
        assertFalse(SkillGlyph.hasGlyph("ashbrand"))
        assertNull(SkillGlyph.drawableName("ashbrand"))
        assertNull(SkillGlyph.job("ashbrand"))
        assertTrue(SkillGlyph.isWeaponWithoutGlyph("ashbrand"))
    }

    @Test
    fun braceJobs_locked() {
        listOf("iron_mantle", "vow_plate", "dust_veil").forEach { id ->
            assertEquals("$id should be Brace", SkillJob.BRACE, SkillGlyph.job(id))
        }
        assertEquals(SkillGlyph.braceIds(), setOf("iron_mantle", "vow_plate", "dust_veil"))
    }

    @Test
    fun mixedJobs_locked() {
        listOf("ash_press", "relic_shard").forEach { id ->
            assertEquals("$id should be Mixed", SkillJob.MIXED, SkillGlyph.job(id))
        }
    }

    @Test
    fun damageJobs_restOfCatalog() {
        val expected = setOf(
            "hostflint", "emberbrand", "tower_pike", "ruin_seal", "shadow_latch", "cinder_step"
        )
        expected.forEach { id ->
            assertEquals("$id should be Damage", SkillJob.DAMAGE, SkillGlyph.job(id))
        }
        assertEquals(expected, SkillGlyph.damageIds())
    }

    @Test
    fun drawableNames_matchArtDropInContract() {
        val expected = mapOf(
            "hostflint" to "glyph_hostflint",
            "emberbrand" to "glyph_emberbrand",
            "tower_pike" to "glyph_tower_pike",
            "ruin_seal" to "glyph_ruin_seal",
            "shadow_latch" to "glyph_shadow_latch",
            "cinder_step" to "glyph_cinder_step",
            "iron_mantle" to "glyph_iron_mantle",
            "vow_plate" to "glyph_vow_plate",
            "dust_veil" to "glyph_dust_veil",
            "ash_press" to "glyph_ash_press",
            "relic_shard" to "glyph_relic_shard"
        )
        expected.forEach { (id, name) ->
            assertEquals(name, SkillGlyph.drawableName(id))
        }
    }

    @Test
    fun noInventedIds_unknownReturnsNull() {
        assertNull(SkillGlyph.job("invented_skill"))
        assertNull(SkillGlyph.drawableName("fireball"))
        assertFalse(SkillGlyph.hasGlyph("not_a_card"))
    }

    @Test
    fun glyphSize_inBuriedbornesRange() {
        assertTrue(SkillGlyph.GLYPH_SIZE_DP in 24..32)
        assertTrue(SkillGlyph.SKILL_SLOT_HEIGHT_DP > 56)
    }
}
