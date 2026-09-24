package com.towerofdarkness.app.domain.combat

/**
 * Combat portrait slot plate — v0.1.22-plate.
 *
 * Fill behind You / Ash-Warden / Seal-Warden is **static** (one dark fill).
 * No color pulse on dice / log / hit. Victory tint at most once at fight end,
 * then **hold** — never [infiniteRepeatable] / infinite glow.
 * PNG stills carry their own lighting — no second painted oval behind them.
 *
 * Aligns with docs/plate-v0122.md + docs/art-audio/PLATE_v0.1.22.md.
 */
object PortraitPlate {
    const val TAG = "v0.1.22-plate"

    /** Static dark plate (theme Ash). One treatment for You + both Wardens. */
    const val FILL_ARGB = 0xFF2B2A28.toInt()

    /** Plate fill never animates / pulses. */
    const val FILL_IS_STATIC = true

    /** No infinite glow / rememberInfiniteTransition under portraits. */
    const val ALLOWS_INFINITE_PULSE = false

    /** Plate must not recolor on dice roll, combat log, or hit beats. */
    const val ALLOWS_DICE_LOG_HIT_RECOLOR = false

    /**
     * Victory may apply a tint once when the fight ends, then hold.
     * Never loop / infiniteRepeatable.
     */
    const val VICTORY_TINT_ONESHOT_HOLD = true
    const val VICTORY_TINT_INFINITE = false

    /** Held victory tint (Gold @ ~40% alpha) — applied only while victory hold. */
    const val VICTORY_TINT_ARGB = 0x66C9A227.toInt()

    /** Second painted oval behind portrait PNG is forbidden. */
    const val SECOND_OVAL_BEHIND_PNG = false

    fun appliesToPlayer(): Boolean = true
    fun appliesToAshWarden(): Boolean = true
    fun appliesToSealWarden(): Boolean = true

    /** Trash Canvas placeholders keep their own silhouette shapes. */
    fun appliesToTrashPlaceholders(): Boolean = false
}
