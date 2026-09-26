package com.towerofdarkness.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import com.towerofdarkness.app.R
import com.towerofdarkness.app.domain.combat.CleaveKit
import com.towerofdarkness.app.domain.combat.CombatFx
import com.towerofdarkness.app.domain.combat.FxRecipient
import com.towerofdarkness.app.domain.combat.FxStrokeSpec
import com.towerofdarkness.app.domain.combat.FxTier
import com.towerofdarkness.app.ui.theme.Moss
import kotlin.math.min
import kotlinx.coroutines.delay

/**
 * Presentation layer for v0.1.43-slashread — phone-readable CLEAVE slash-light on
 * recipient bust (half-stage clip + role tint) with Canvas path fallback; Brace
 * Shield stamps unchanged. Wake slash stays in [WakeStageOverlay].
 * Aim / clip locks from v0.1.40-fxfix.
 * Banned: full-width gold bar You↔foe; whole-row tint flash; plate wash; shield-block.
 */
@Composable
fun CombatStrokeOverlay(
    stroke: FxStrokeSpec?,
    visible: Boolean,
    modifier: Modifier = Modifier,
    tier: FxTier = FxTier.SMALL,
    /** Scaled hold (already 1x/2x); drives atlas frame progress + peak linger. */
    holdMs: Long = CombatFx.STROKE_SMALL_MS
) {
    // Prefer CLEAVE sheet whenever stroke is present — do not skip on thickness quirks
    if (!visible || stroke == null) return
    val color = Color(stroke.colorArgb)
    val recipient = stroke.recipient
    val context = LocalContext.current
    // Compile-linked R.drawable — preferred path; fail-soft only if bitmap decode fails
    val slashSheet: ImageBitmap? = remember {
        try {
            ImageBitmap.imageResource(context.resources, R.drawable.fx_slash_light)
        } catch (_: Throwable) {
            try {
                val id = context.resources.getIdentifier(
                    CleaveKit.SLASH_DRAWABLE, "drawable", context.packageName
                )
                if (id == 0) null else ImageBitmap.imageResource(context.resources, id)
            } catch (_: Throwable) {
                null
            }
        }
    }
    var frameIndex by remember(visible, holdMs, tier) { mutableIntStateOf(0) }
    var fadeAlpha by remember(visible, holdMs, tier) { mutableFloatStateOf(1f) }
    LaunchedEffect(visible, holdMs, tier) {
        if (!visible) {
            frameIndex = 0
            fadeAlpha = 1f
            return@LaunchedEffect
        }
        val frames = CleaveKit.SLASH_PLAY_FRAMES
        val delays = CleaveKit.slashFrameDelaysMs(holdMs, tier)
        val peakIdx = frames.indexOf(CleaveKit.SLASH_PEAK_FRAME).coerceAtLeast(0)
        for (i in frames.indices) {
            frameIndex = i
            // Full brightness through peak; fade after peak window
            fadeAlpha = if (i <= peakIdx) 1f else {
                val after = (i - peakIdx).toFloat() / (frames.size - peakIdx).coerceAtLeast(1)
                (1f - after * 0.55f).coerceIn(0.35f, 1f)
            }
            delay(delays.getOrElse(i) { CleaveKit.frameStepMs(holdMs, frames.size) })
        }
        fadeAlpha = 0.35f
    }
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val clip = CombatFx.recipientClipXFrac(recipient)
        clipRect(
            left = clip.start * w,
            top = 0f,
            right = clip.endInclusive * w,
            bottom = h
        ) {
            if (slashSheet != null) {
                drawCleaveSlash(
                    sheet = slashSheet,
                    recipient = recipient,
                    tier = tier,
                    frameIndex = frameIndex,
                    tint = color,
                    alpha = fadeAlpha
                )
            } else if (stroke.thickness > 0f) {
                // Fail-soft: prior Canvas path stroke if sheet missing
                drawPathFallback(stroke, tier, color)
            }
        }
    }
}

private fun DrawScope.drawCleaveSlash(
    sheet: ImageBitmap,
    recipient: FxRecipient,
    tier: FxTier,
    frameIndex: Int,
    tint: Color,
    alpha: Float
) {
    val w = size.width
    val h = size.height
    val cx = when (recipient) {
        FxRecipient.YOU -> CombatFx.YOU_BUST_X * w
        FxRecipient.FOE -> CombatFx.FOE_BUST_X * w
    }
    val cy = CombatFx.BUST_Y * h
    // Phone-readable: ~60–80% of bust / stage min side (BUST_COVERAGE * Medium scale)
    val base = CleaveKit.slashDrawPx(min(w, h), tier)
    val frames = CleaveKit.SLASH_PLAY_FRAMES
    val frame = frames.getOrElse(frameIndex.coerceIn(0, frames.lastIndex)) {
        CleaveKit.SLASH_PEAK_FRAME
    }
    val (sx, sy) = CleaveKit.slashCellOrigin(frame)
    val dstLeft = cx - base * CleaveKit.SLASH_ANCHOR_X
    val dstTop = cy - base * CleaveKit.SLASH_ANCHOR_Y
    val dst = base.toInt().coerceAtLeast(1)
    // SrcIn: crescent reads as solid role color (gold/ember or dirty green/rust),
    // not faint grey from Modulate wash-out on sampler alpha.
    drawImage(
        image = sheet,
        srcOffset = androidx.compose.ui.unit.IntOffset(sx, sy),
        srcSize = androidx.compose.ui.unit.IntSize(CleaveKit.SLASH_CELL_PX, CleaveKit.SLASH_CELL_PX),
        dstOffset = androidx.compose.ui.unit.IntOffset(dstLeft.toInt(), dstTop.toInt()),
        dstSize = androidx.compose.ui.unit.IntSize(dst, dst),
        alpha = (alpha * 0.98f).coerceIn(0.2f, 1f),
        colorFilter = ColorFilter.tint(tint.copy(alpha = 1f), BlendMode.SrcIn),
        filterQuality = FilterQuality.Low
    )
}

private fun DrawScope.drawPathFallback(
    stroke: FxStrokeSpec,
    tier: FxTier,
    color: Color
) {
    val w = size.width
    val h = size.height
    val geom = CombatFx.slashCutGeom(stroke.recipient, tier)
    val cx = geom.centerXFrac * w
    val cy = geom.centerYFrac * h
    val ex = geom.halfExtentXFrac * w
    val ey = geom.halfExtentYFrac * h
    val start = Offset(cx - ex, cy - ey)
    val end = Offset(cx + ex, cy + ey)
    val mid = Offset(cx + ex * 0.15f, cy - ey * 0.35f)
    val path = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(mid.x, mid.y, end.x, end.y)
    }
    drawPath(
        path = path,
        color = color.copy(alpha = 0.45f),
        style = Stroke(
            width = CombatFx.strokeGlowWidth(stroke.thickness),
            cap = StrokeCap.Round
        )
    )
    drawPath(
        path = path,
        color = color.copy(alpha = 0.92f),
        style = Stroke(
            width = CombatFx.strokeCoreWidth(stroke.thickness),
            cap = StrokeCap.Round
        )
    )
}

/**
 * Contact / Ashbrand SPARK — CLEAVE hit-flash (additive) on [recipient] bust only.
 * Not full-screen. Prefer additive sheet + [BlendMode.Plus]; fail-soft to RGBA atlas.
 */
@Composable
fun CombatHitFlashOverlay(
    recipient: FxRecipient?,
    visible: Boolean,
    modifier: Modifier = Modifier,
    holdMs: Long = CleaveKit.HIT_FLASH_MS,
    /** Role tint — gold/ember for You, dirty green/rust for enemy. */
    tintArgb: Long = CombatFx.COLOR_YOU
) {
    if (!visible || recipient == null) return
    val context = LocalContext.current
    val sheetOrNull = remember {
        try {
            // Prefer additive, then RGBA — compile-linked
            try {
                ImageBitmap.imageResource(context.resources, R.drawable.fx_hit_flash_additive)
            } catch (_: Throwable) {
                ImageBitmap.imageResource(context.resources, R.drawable.fx_hit_flash)
            }
        } catch (_: Throwable) {
            try {
                val additiveId = context.resources.getIdentifier(
                    CleaveKit.HIT_FLASH_ADDITIVE_DRAWABLE, "drawable", context.packageName
                )
                val rgbaId = context.resources.getIdentifier(
                    CleaveKit.HIT_FLASH_DRAWABLE, "drawable", context.packageName
                )
                val id = if (additiveId != 0) additiveId else rgbaId
                if (id == 0) null else ImageBitmap.imageResource(context.resources, id)
            } catch (_: Throwable) {
                null
            }
        }
    }
    val sheet = sheetOrNull ?: return // fail-soft: no flash if sheet missing
    val tint = Color(tintArgb)
    var progress by remember(visible, holdMs) { mutableFloatStateOf(0f) }
    LaunchedEffect(visible, holdMs) {
        if (!visible) {
            progress = 0f
            return@LaunchedEffect
        }
        val frames = CleaveKit.HIT_FLASH_PLAY_FRAMES
        val step = CleaveKit.frameStepMs(holdMs, frames.size)
        for (i in frames.indices) {
            progress = (i + 1).toFloat() / frames.size
            delay(step)
        }
        progress = 1f
    }
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val clip = CombatFx.recipientClipXFrac(recipient)
        clipRect(
            left = clip.start * w,
            top = 0f,
            right = clip.endInclusive * w,
            bottom = h
        ) {
            val cx = when (recipient) {
                FxRecipient.YOU -> CombatFx.YOU_BUST_X * w
                FxRecipient.FOE -> CombatFx.FOE_BUST_X * w
            }
            val cy = CombatFx.BUST_Y * h
            val base = min(w, h) * 0.42f
            val frame = CleaveKit.hitFlashFrameAt(progress)
            val (sx, sy) = CleaveKit.hitFlashCellOrigin(frame)
            // Fade after peak window (~first 6–8 frames already in play list)
            val fade = if (progress < 0.75f) 1f else (1f - (progress - 0.75f) / 0.25f).coerceIn(0.15f, 1f)
            val dstLeft = cx - base * CleaveKit.HIT_FLASH_ANCHOR_X
            val dstTop = cy - base * CleaveKit.HIT_FLASH_ANCHOR_Y
            drawImage(
                image = sheet,
                srcOffset = androidx.compose.ui.unit.IntOffset(sx, sy),
                srcSize = androidx.compose.ui.unit.IntSize(
                    CleaveKit.HIT_FLASH_CELL_PX,
                    CleaveKit.HIT_FLASH_CELL_PX
                ),
                dstOffset = androidx.compose.ui.unit.IntOffset(dstLeft.toInt(), dstTop.toInt()),
                dstSize = androidx.compose.ui.unit.IntSize(
                    base.toInt().coerceAtLeast(1),
                    base.toInt().coerceAtLeast(1)
                ),
                alpha = fade * 0.95f,
                colorFilter = ColorFilter.tint(tint.copy(alpha = 1f), BlendMode.SrcIn),
                blendMode = BlendMode.Plus,
                filterQuality = FilterQuality.Low
            )
        }
    }
}

/**
 * Fired-skill tile flash only — caller must constrain [modifier] to the tile bounds.
 * Do NOT place on the portrait stage with fillMaxSize (whole-row tint is banned).
 */
@Composable
fun CombatTileFlash(
    visible: Boolean,
    colorArgb: Long,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    Box(
        modifier.background(Color(colorArgb).copy(alpha = 0.32f))
    )
}

/**
 * Shield pips that float/fade around the Brace owner bust (You or foe).
 * Uses StatusPipRow language ([Icons.Filled.Shield] / Moss); Path diamond is secondary.
 * Count already capped via [CombatFx.bracePipCount]. Under-bust Brace number stays elsewhere.
 * Clipped to owner half-stage (same rule as slash). **Not** shield-block sheet.
 */
@Composable
fun CombatBracePipsOverlay(
    count: Int,
    owner: FxRecipient?,
    visible: Boolean,
    modifier: Modifier = Modifier,
    progress: Float = 0.5f
) {
    if (!visible || owner == null || count <= 0) return
    val n = CombatFx.bracePipCount(count)
    val moss = Moss
    val shieldPainter = rememberVectorPainter(Icons.Filled.Shield)
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val clip = CombatFx.recipientClipXFrac(owner)
        clipRect(
            left = clip.start * w,
            top = 0f,
            right = clip.endInclusive * w,
            bottom = h
        ) {
            val cx = when (owner) {
                FxRecipient.YOU -> CombatFx.YOU_BUST_X * w
                FxRecipient.FOE -> CombatFx.FOE_BUST_X * w
            }
            val cy = CombatFx.BUST_Y * h
            val radius = min(w, h) * 0.11f
            val alpha = CombatFx.bracePipAlpha(progress)
            val lift = progress * min(w, h) * 0.06f
            val pipR = min(w, h) * CombatFx.BRACE_PIP_RADIUS_FRAC
            for (i in 0 until n) {
                val ang = (i.toFloat() / n.coerceAtLeast(1)) * (Math.PI.toFloat() * 1.6f) - 0.4f
                val px = cx + kotlin.math.cos(ang) * radius
                val py = cy + kotlin.math.sin(ang) * radius * 0.55f - lift
                val diamond = Path().apply {
                    moveTo(px, py - pipR * 1.4f)
                    lineTo(px + pipR, py)
                    lineTo(px, py + pipR * 1.5f)
                    lineTo(px - pipR, py)
                    close()
                }
                drawPath(diamond, moss.copy(alpha = alpha * 0.55f))
                val stamp = pipR * 2.4f
                translate(left = px - stamp / 2f, top = py - stamp / 2f) {
                    with(shieldPainter) {
                        draw(
                            size = Size(stamp, stamp),
                            alpha = alpha,
                            colorFilter = ColorFilter.tint(moss)
                        )
                    }
                }
            }
        }
    }
}
