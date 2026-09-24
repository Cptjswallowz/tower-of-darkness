package com.towerofdarkness.app.domain.glyphs

import com.towerofdarkness.app.domain.cards.CardCatalog

/**
 * Skill tile glyphs — v0.1.19-glyphs.
 * Pure mapping for unit tests (no Compose). Drawable names match
 * `app/src/main/res/drawable/glyph_*.png` so Art can drop-in replace same names.
 * Ashbrand weapon bar stays [AshbrandIcon] only — no second glyph.
 * Aligns with docs/glyphs-v0119.md.
 */
enum class SkillJob {
    /** Default damage read (ember/ash). */
    DAMAGE,
    /** Guard / Brace — green outline on tiles. */
    BRACE,
    /** Damage+heal or heal/brace mixed. */
    MIXED
}

object SkillGlyph {
    /** Combat + loadout glyph slot size (dp). Buriedbornes-style picture on tile. */
    const val GLYPH_SIZE_DP = 28

    /** Suggested skill-slot height with glyph above name (was 56). */
    const val SKILL_SLOT_HEIGHT_DP = 72

    private val DAMAGE_IDS = setOf(
        "hostflint", "emberbrand", "tower_pike", "ruin_seal", "shadow_latch", "cinder_step"
    )
    private val BRACE_IDS = setOf(
        "iron_mantle", "vow_plate", "dust_veil"
    )
    private val MIXED_IDS = setOf(
        "ash_press", "relic_shard"
    )

    /** Locked Elliott map — drawable resource name (no extension). */
    private val DRAWABLE_BY_ID = mapOf(
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

    /** All catalog skill ids that must show a glyph. */
    fun mappedSkillIds(): Set<String> = DRAWABLE_BY_ID.keys

    fun job(cardId: String): SkillJob? = when (cardId) {
        in BRACE_IDS -> SkillJob.BRACE
        in MIXED_IDS -> SkillJob.MIXED
        in DAMAGE_IDS -> SkillJob.DAMAGE
        else -> null
    }

    /**
     * Drawable name for [cardId], or null when no skill glyph
     * (Ashbrand / unknown / invent-not-allowed).
     */
    fun drawableName(cardId: String): String? = DRAWABLE_BY_ID[cardId]

    fun hasGlyph(cardId: String): Boolean = cardId in DRAWABLE_BY_ID

    /** Weapon ids never get a skill glyph (Ashbrand blade only). */
    fun isWeaponWithoutGlyph(id: String): Boolean =
        id == "ashbrand" || id.startsWith("weapon_")

    fun braceIds(): Set<String> = BRACE_IDS

    fun mixedIds(): Set<String> = MIXED_IDS

    fun damageIds(): Set<String> = DAMAGE_IDS

    /** Every [CardCatalog] skill has exactly one mapped glyph. */
    fun catalogFullyMapped(): Boolean =
        CardCatalog.all.all { hasGlyph(it.id) } &&
            mappedSkillIds().all { CardCatalog.byId(it) != null }
}
