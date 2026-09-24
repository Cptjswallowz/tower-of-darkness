package com.towerofdarkness.app.domain.combat

/**
 * Portrait plate / circle contract — v0.1.23-nobg.
 *
 * **Combat:** no plate / fill / tint / ring behind You, Seal-Warden, or Ash-Warden.
 * PNGs sit on the dark stage only. Defeat / victory: no extra ring behind the figure
 * (banner + Continue are enough).
 *
 * **Title:** teal circle behind You may remain (title screen only).
 *
 * Trash Canvas placeholders are the placeholder body — not a plate; leave them.
 *
 * Supersedes combat use of v0.1.22-plate static fill / victory tint.
 * Aligns with docs/nobg-v0123.md + docs/art-audio/NOBG_v0.1.23.md.
 */
object PortraitPlate {
    const val TAG = "v0.1.23-nobg"

    /** Combat portraits draw PNG only — no disc / fill / tint under them. */
    const val COMBAT_PLATE_ENABLED = false

    /** Title screen may keep a teal circle behind You (not a combat plate). */
    const val TITLE_TEAL_CIRCLE_ALLOWED = true

    /** GlowRare teal — title circle fill (static; no pulse). */
    const val TITLE_CIRCLE_ARGB = 0xFF38BDF8.toInt()

    /** No defeat / victory ring behind combat figures. */
    const val VICTORY_RING_BEHIND_FIGURE = false
    const val DEFEAT_RING_BEHIND_FIGURE = false

    /** Historical v0.1.22 dark fill — combat must not draw it anymore. */
    const val FILL_ARGB = 0xFF2B2A28.toInt()

    /** Combat plate fill must stay off (no pulse / recolor either). */
    const val FILL_IS_STATIC = true
    const val ALLOWS_INFINITE_PULSE = false
    const val ALLOWS_DICE_LOG_HIT_RECOLOR = false

    /** Victory tint / ring behind figure is forbidden in combat. */
    const val VICTORY_TINT_ONESHOT_HOLD = false
    const val VICTORY_TINT_INFINITE = false
    const val VICTORY_TINT_ARGB = 0x66C9A227.toInt()

    /** Second painted oval behind portrait PNG is forbidden. */
    const val SECOND_OVAL_BEHIND_PNG = false

    /** Combat You — no plate. */
    fun appliesToPlayer(): Boolean = false

    /** Combat Ash-Warden — no plate. */
    fun appliesToAshWarden(): Boolean = false

    /** Combat Seal-Warden — no plate. */
    fun appliesToSealWarden(): Boolean = false

    /** Trash Canvas placeholders keep their own silhouette shapes (body, not plate). */
    fun appliesToTrashPlaceholders(): Boolean = false

    fun titleTealCircleAllowed(): Boolean = TITLE_TEAL_CIRCLE_ALLOWED
}
