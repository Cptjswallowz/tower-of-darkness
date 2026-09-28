package com.towerofdarkness.app.domain.art

/**
 * Shared dark UI plate under combat skill tiles / Ashbrand / Forge rows —
 * v0.1.59-tilepolish PART B. See docs/art-audio/TILEPOLISH_PLATE_v0.1.59.md.
 *
 * Drawable: ui_tile_plate.png (Art SOURCE crop). Center α ≈ 0.35 baked;
 * Compose may apply additional alpha for node discs (~40% of skill opacity).
 * No FX packs on plate. Do not invent per-rarity plate scenes.
 */
object SharedTilePlate {
    const val TAG = "v0.1.59-tilepolish"
    /** Art deliverable under res/drawable. */
    const val DRAWABLE_NAME = "ui_tile_plate"
    /** Expected Art md5 (drawable bytes). */
    const val EXPECTED_MD5 = "20dc01b0e3b934c2bb08c8b26cf77524"
    /**
     * Extra Compose alpha for skill/Ashbrand/Forge.
     * Plate PNG already has center α ≈ 0.35; use 1f so baked alpha stands.
     */
    const val OPACITY = 1.0f
    /** Documented center alpha band (baked into PNG). */
    const val CENTER_ALPHA_DOC = 0.35f
    const val OPACITY_MIN = 0.25f
    const val OPACITY_MAX = 0.40f
    /**
     * Floor node discs: ~40% of skill-slot plate opacity (Art preview ≈ 0.14 center).
     * Applied as Compose Image alpha on the same drawable.
     */
    const val NODE_DISC_PLATE_ALPHA = 0.40f
    /** Legacy Panel fade when plate image is under content. */
    const val PANEL_OVER_PLATE_ALPHA = 0.55f
    const val HOOKS_READY = true

    fun opacityInBand(alpha: Float = OPACITY): Float =
        alpha.coerceIn(0f, 1f)

    fun assetPresent(drawableResId: Int): Boolean = drawableResId != 0
}
