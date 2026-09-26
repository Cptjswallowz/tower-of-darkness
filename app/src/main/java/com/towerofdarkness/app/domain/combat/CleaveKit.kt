package com.towerofdarkness.app.domain.combat

/**
 * CLEAVE free-sampler atlas metadata — v0.1.44-slashproof.
 * Pure domain (frame / duration / scale helpers). Compose draws sheets.
 * Only slash-light + hit-flash (additive optional). No shield-block.
 * Phone-readable: bust coverage ~82% of bust diameter (half-stage based),
 * content crop zooms past empty atlas padding, Medium 1.3×, peak hold ~200ms,
 * total ≤500ms @1x. See docs/slashproof-v0144.md.
 *
 * Aim / clip / who-gets-FX stay v0.1.40-fxfix. Does not replace Wake / Brace.
 */
object CleaveKit {
    const val TAG = "v0.1.44-slashproof"

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
    /**
     * Anchor within the **cropped** src (not the full 256 cell).
     * Content sits right-of-center in atlas cells — see [SLASH_CROP_OX].
     */
    const val SLASH_ANCHOR_X = 0.62f
    const val SLASH_ANCHOR_Y = 0.52f

    /**
     * Zoom crop inside each 256 cell. Atlas crescents occupy ~17–38% of the cell
     * (heavy padding); drawing the full cell left Pike/Hostflint unreadably tiny.
     * Crop covers union of play frames 4/6/8/10 content with margin.
     */
    const val SLASH_CROP_PX = 144
    const val SLASH_CROP_OX = 112
    const val SLASH_CROP_OY = 24

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
    const val MEDIUM_SCALE = 1.3f
    const val SMALL_SCALE = 1.0f

    /**
     * Crescent dst size as fraction of **bust diameter** (not shy min(w,h) alone).
     * Lock: 0.70–0.90 of bust. Prefer [bustDiameterPx] from half-stage width.
     */
    const val BUST_COVERAGE = 0.82f

    /** Half-stage width → bust diameter factor (portrait pack ≈ half of half-stage). */
    const val BUST_FROM_HALF_STAGE = 0.55f

    /** Bust diameter also capped by overlay height (portraits sit in ~140.dp stage). */
    const val BUST_FROM_STAGE_H = 0.72f

    /** 1x slash budget must stay ≤ 500ms (WO). */
    const val MAX_SLASH_MS_1X = 500L

    /** Hold brightest (peak) frame @1x Small — then fade. Window 180–220ms. */
    const val PEAK_HOLD_MS = 200L

    /** Medium peak linger slightly longer than Small (still within Medium stroke ≤500). */
    const val PEAK_HOLD_MEDIUM_MS = 220L

    /** Hit-flash hold @1x — Ashbrand SPARK path (first 6–8 frames then fade). */
    const val HIT_FLASH_MS = 320L

    /**
     * Contact hit-flash on damage stroke (Small/Medium) — short additive flash
     * on recipient at cut contact; overlaps slash, not full-screen.
     */
    const val CONTACT_HIT_FLASH_MS = 200L

    /**
     * Slash play list: every-other frames spanning the peak window (4–10).
     * Peak frame (6) lingers [PEAK_HOLD_MS]; total still ≤ [MAX_SLASH_MS_1X] @1x.
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

    /**
     * Bust diameter in stage px — prefer half-stage width, clamp by stage height.
     * Avoids shy `min(w,h)` that still reads tiny when width ≫ height (140.dp strip).
     */
    fun bustDiameterPx(stageW: Float, stageH: Float): Float {
        val halfStage = stageW * 0.5f
        return minOf(halfStage * BUST_FROM_HALF_STAGE, stageH * BUST_FROM_STAGE_H)
            .coerceAtLeast(1f)
    }

    /** Peak linger ms for this stroke hold (scales with 2x via already-halved [holdMs]). */
    fun peakHoldFor(holdMs: Long, tier: FxTier = FxTier.SMALL): Long {
        val base1x = when (tier) {
            FxTier.MEDIUM -> PEAK_HOLD_MEDIUM_MS
            else -> PEAK_HOLD_MS
        }
        val ref1x = when (tier) {
            FxTier.MEDIUM -> CombatFx.STROKE_MEDIUM_MS
            else -> CombatFx.STROKE_SMALL_MS
        }
        // Proportional when speedX halves holdMs; clamp to 40–65% of hold so rest can fade.
        val scaled = (base1x * holdMs / ref1x.coerceAtLeast(1L)).coerceAtLeast(1L)
        val lo = (holdMs * 40L / 100L).coerceAtLeast(1L)
        val hi = (holdMs * 65L / 100L).coerceAtLeast(lo)
        return scaled.coerceIn(lo, hi)
    }

    /**
     * Per-play-frame delays that sum to [holdMs], with peak frame lingering longest.
     */
    fun slashFrameDelaysMs(holdMs: Long, tier: FxTier = FxTier.SMALL): LongArray {
        val frames = SLASH_PLAY_FRAMES
        if (frames.isEmpty()) return longArrayOf()
        val peakIdx = frames.indexOf(SLASH_PEAK_FRAME).let { if (it >= 0) it else frames.size / 2 }
        val peak = peakHoldFor(holdMs, tier).coerceAtMost(holdMs)
        val rest = (holdMs - peak).coerceAtLeast(0L)
        val otherCount = (frames.size - 1).coerceAtLeast(1)
        val each = (rest / otherCount).coerceAtLeast(1L)
        var used = 0L
        val out = LongArray(frames.size) { i ->
            if (i == peakIdx) peak else each
        }
        used = out.sum()
        if (used < holdMs) out[out.lastIndex] += holdMs - used
        if (used > holdMs) {
            var over = used - holdMs
            for (i in out.indices.reversed()) {
                if (i == peakIdx) continue
                val cut = minOf(over, out[i] - 1L)
                if (cut > 0) {
                    out[i] -= cut
                    over -= cut
                }
                if (over <= 0) break
            }
        }
        return out
    }

    /**
     * Draw size (px) for slash crescent given stage size and tier.
     * = [bustDiameterPx] × [BUST_COVERAGE] × [slashScale].
     */
    fun slashDrawPx(stageW: Float, stageH: Float, tier: FxTier): Float =
        bustDiameterPx(stageW, stageH) * BUST_COVERAGE * slashScale(tier)

    /**
     * Legacy square-stage helper (tests / callers that only have min side).
     * Treats [minSide] as both w and h so half-stage = minSide/2.
     */
    fun slashDrawPx(minSide: Float, tier: FxTier): Float =
        slashDrawPx(minSide, minSide, tier)

    /** Row-major cell origin (px) for [frame] in a cols×rows atlas. */
    fun cellOrigin(frame: Int, cols: Int, cellPx: Int = SLASH_CELL_PX): Pair<Int, Int> {
        val f = frame.coerceAtLeast(0)
        val col = f % cols
        val row = f / cols
        return col * cellPx to row * cellPx
    }

    fun slashCellOrigin(frame: Int): Pair<Int, Int> =
        cellOrigin(frame, SLASH_COLS, SLASH_CELL_PX)

    /** Absolute src origin for zoomed crop inside the frame cell. */
    fun slashCropOrigin(frame: Int): Pair<Int, Int> {
        val (cx, cy) = slashCellOrigin(frame)
        return cx + SLASH_CROP_OX to cy + SLASH_CROP_OY
    }

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
            CombatFx.STROKE_MEDIUM_MS <= MAX_SLASH_MS_1X &&
            SLASH_PLAY_FRAMES.isNotEmpty() &&
            SLASH_PLAY_FRAMES.all { it in 0 until SLASH_FRAMES } &&
            PEAK_HOLD_MS in 180L..220L &&
            PEAK_HOLD_MEDIUM_MS in 180L..220L

    /** Peak frame is inside the play window (readability lock). */
    fun slashPeakInPlayWindow(): Boolean =
        SLASH_PEAK_FRAME in SLASH_PLAY_FRAMES.min()..SLASH_PLAY_FRAMES.max()

    fun hitFlashPeakInPlayWindow(): Boolean =
        HIT_FLASH_PEAK_FRAME in HIT_FLASH_PLAY_FRAMES.min()..HIT_FLASH_PLAY_FRAMES.max()

    /** Bust coverage lock for tests (0.70–0.90). */
    fun bustCoverageInLockRange(): Boolean =
        BUST_COVERAGE in 0.70f..0.90f
}
