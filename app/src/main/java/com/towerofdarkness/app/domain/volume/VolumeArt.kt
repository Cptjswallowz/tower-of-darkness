package com.towerofdarkness.app.domain.volume

import com.towerofdarkness.app.domain.combat.EnemyKind

/**
 * Volume polish — v0.1.20-volume.
 * Soft drop shadow + gold-ash top-left rim + darker cloak/seal folds.
 * Pure mapping for unit tests (no Compose). Aligns with docs/volume-v0120.md
 * + docs/art-audio/VOLUME_v0.1.20.md (Art bake via tools/gen_volume_v0120.py).
 *
 * Does NOT apply to Seal-Warden / trash placeholders, Wake VFX / spark.
 * Glyph motifs unchanged (Vow Plate optional plate clarification allowed).
 */
object VolumeArt {
    const val TAG = "v0.1.20-volume"

    /** Warm gold-ash rim (ARGB) — Gold diluted toward bone/ash. */
    const val GOLD_ASH_ARGB = 0xD4C9A86A.toInt()

    /** Soft drop-shadow opacity under figure/tile (Compose chrome). */
    const val SHADOW_ALPHA = 0.38f

    /** Rim stroke relative to min dimension (outside content; no layout shift). */
    const val RIM_STROKE_FRACTION = 0.045f

    /**
     * Spent / greyed glyph alpha — readable, not crushed.
     * Prefer greyscale + this alpha over whole-tile 0.35 crush.
     */
    const val DIMMED_GLYPH_ALPHA = 0.68f

    /** SkillSlot column alpha when spent (text/chrome); glyph uses [DIMMED_GLYPH_ALPHA]. */
    const val DIMMED_SLOT_ALPHA = 0.72f

    /** Surfaces wired for volume (Art PNG bake is SoT; Compose chrome is no-op). */
    fun volumeSurfaces(): Set<String> = setOf(
        "portrait_you",
        "portrait_ash_warden",
        "skill_glyph_tiles",
        "ashbrand_icon"
    )

    fun appliesToPlayerPortrait(): Boolean = true

    fun appliesToEnemy(kind: EnemyKind): Boolean = kind == EnemyKind.ASH_WARDEN

    fun appliesToSealWarden(): Boolean = false

    fun appliesToTrash(): Boolean = false

    fun appliesToSkillGlyphTiles(): Boolean = true

    fun appliesToAshbrandIcon(): Boolean = true

    /** Wake frames / spark / crescent stay frozen — no volume pass. */
    fun appliesToWakeVfx(): Boolean = false

    fun appliesToAshbrandSpark(): Boolean = false

    /**
     * Drawable basenames that receive Art PNG volume bake
     * (shadow + rim + 2-tone folds via tools/gen_volume_v0120.py).
     * Glyph *motifs* stay the same; shading only (+ optional Vow Plate plate clarify).
     */
    fun artBakedDrawables(): Set<String> = setOf(
        "portrait_you",
        "portrait_ash_warden",
        "ashbrand_icon",
        "glyph_hostflint",
        "glyph_emberbrand",
        "glyph_tower_pike",
        "glyph_ruin_seal",
        "glyph_shadow_latch",
        "glyph_cinder_step",
        "glyph_iron_mantle",
        "glyph_vow_plate",
        "glyph_dust_veil",
        "glyph_ash_press",
        "glyph_relic_shard",
        "glyph_cinder_vow",
        "glyph_grave_nail",
        "glyph_ember_draw",
        "glyph_brand_mark",
        "glyph_spark_tithe",
        "glyph_wake_echo"
    )

    /** Fold-darkening targets (subset of Art bake — portraits + blade). */
    fun foldDarkenedDrawables(): Set<String> = setOf(
        "portrait_you",
        "portrait_ash_warden",
        "ashbrand_icon"
    )

    fun glyphSymbolsRestyled(): Boolean = false

    /** Compose must not redraw Art-baked shadow/rim (avoids double volume). */
    fun composeChromeDrawsShadowRim(): Boolean = false

    /** Optional Vow Plate plate-glyph clarification shipped with Art volume bake. */
    fun vowPlatePlateClarifyAllowed(): Boolean = true
}
