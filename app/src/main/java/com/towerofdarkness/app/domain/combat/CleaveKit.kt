package com.towerofdarkness.app.domain.combat

/**
 * CLEAVE free-sampler atlas metadata — v0.1.42-cleavekit.
 * Pure domain (frame / duration / scale helpers). Compose draws sheets.
 * Only slash-light + hit-flash (additive optional). No shield-block.
 * See docs/cleavekit-v0142.md + docs/art-audio/CLEAVE_v0.1.42.md.
 *
 * Aim / clip / who-gets-FX stay v0.1.40-fxfix. Does not replace Wake / Brace.
 */
object CleaveKit {
    const val TAG = "v0.1.42-cleavekit"

    /** Android drawable basenames (underscores — no hyphens). */
    const val SLASH_DRAWABLE = "fx_slash_light"
    const val HIT_FLASH_DRAWABLE = "fx_hit_flash"
    const val HIT_FLASH_ADDITIVE_DRAWABLE = "fx_hit_flash_additive"

    // --- slash-light atlas (8×2, 16f@24, peak 6, alpha) ---
    const val SLASH_FRAMES = 16
    const val SLASH_COLS = 8
    const val SLASH_ROWS = 2
    const val SLASH_FPS = 24
    const val SLASH_CELL_PX = 256
    const val SLASH_PEAK_FRAME = 6
    const val SLASH_ANCHOR_X = 0.46f
    const val SLASH_ANCHOR_Y = 0.5f

    // --- hit-flash atlas (6×2, 12f@24, peak 2, additive) ---
    const val HIT_FLASH_FRAMES = 12
    const val HIT_FLASH_COLS = 6
    const val HIT_FLASH_ROWS = 2
    const val HIT_FLASH_FPS = 24
    const val HIT_FLASH_CELL_PX = 256
    const val HIT_FLASH_PEAK_FRAME = 2
    const val HIT_FLASH_ANCHOR_X = 0.5f
    const val HIT_FLASH_ANCHOR_Y = 0.5f

    /** Player Medium / enemy Medium draw scale vs Small. */
    const val MEDIUM_SCALE = 1.2f
    const val SMALL_SCALE = 1.0f

    /** 1x slash budget must stay ≤ 500ms (WO). */
    const val MAX_SLASH_MS_1X = 500L

    /** Hit-flash hold @1x — first 6–8 frames then fade. */
    const val HIT_FLASH_MS = 320L

    /**
     * Slash play list: every-other frames spanning the peak window (4–10).
     * 4 frames × ~100ms fits inside STROKE_SMALL_MS (400) and ≤500ms @1x.
     */
    val SLASH_PLAY_FRAMES: IntArray = intArrayOf(4, 6, 8, 10)

    /** Hit-flash: first 8 frames (peak at 2), then overlay fades with hold end. */
    val HIT_FLASH_PLAY_FRAMES: IntArray = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7)

    /** Banned — Brace stays floating pips; never install/wire this sheet. */
    const val SHIELD_BLOCK_BANNED = "shield-block"

    fun slashScale(tier: FxTier): Float = when (tier) {
        FxTier.MEDIUM -> MEDIUM_SCALE
        else -> SMALL_SCALE
    }

    /** Row-major cell origin (px) for [frame] in a cols×rows atlas. */
    fun cellOrigin(frame: Int, cols: Int, cellPx: Int = SLASH_CELL_PX): Pair<Int, Int> {
        val f = frame.coerceAtLeast(0)
        val col = f % cols
        val row = f / cols
        return col * cellPx to row * cellPx
    }

    fun slashCellOrigin(frame: Int): Pair<Int, Int> =
        cellOrigin(frame, SLASH_COLS, SLASH_CELL_PX)

    fun hitFlashCellOrigin(frame: Int): Pair<Int, Int> =
        cellOrigin(frame, HIT_FLASH_COLS, HIT_FLASH_CELL_PX)

    /**
     * Frame from a play list given progress 0→1.
     * Progress ≥1 clamps to last frame.
     */
    fun frameAt(playFrames: IntArray, progress: Float): Int {
        if (playFrames.isEmpty()) return 0
        val p = progress.coerceIn(0f, 1f)
        if (p >= 1f) return playFrames.last()
        val i = (p * playFrames.size).toInt().coerceIn(0, playFrames.lastIndex)
        return playFrames[i]
    }

    fun slashFrameAt(progress: Float): Int = frameAt(SLASH_PLAY_FRAMES, progress)

    fun hitFlashFrameAt(progress: Float): Int = frameAt(HIT_FLASH_PLAY_FRAMES, progress)

    /** Per-frame ms so [count] frames fill [holdMs] (min 1). */
    fun frameStepMs(holdMs: Long, count: Int): Long =
        (holdMs / count.coerceAtLeast(1)).coerceAtLeast(1L)

    /** True when 1x slash play duration stays within WO cap. */
    fun slashPlayWithinBudget(strokeMs1x: Long = CombatFx.STROKE_SMALL_MS): Boolean =
        strokeMs1x <= MAX_SLASH_MS_1X &&
            SLASH_PLAY_FRAMES.isNotEmpty() &&
            SLASH_PLAY_FRAMES.all { it in 0 until SLASH_FRAMES }

    /** Peak frame is inside the play window (readability lock). */
    fun slashPeakInPlayWindow(): Boolean =
        SLASH_PEAK_FRAME in SLASH_PLAY_FRAMES.min()..SLASH_PLAY_FRAMES.max()

    fun hitFlashPeakInPlayWindow(): Boolean =
        HIT_FLASH_PEAK_FRAME in HIT_FLASH_PLAY_FRAMES.min()..HIT_FLASH_PLAY_FRAMES.max()
}
