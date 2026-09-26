package com.towerofdarkness.app.ui.components

import android.util.Log
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
 * Presentation layer for v0.1.47-strokethick — drawn Wake-family stroke PRIMARY.
 * Fat glow + core quadratic crescent (bust-frac canvas widths) on recipient bust (same family as Wake arc language).
 * Optional CLEAVE slash-light tip garnish at stroke TIP only — never the slash.
 * Wake gold arc stays in [WakeStageOverlay]. Aim / clip locks from v0.1.40-fxfix.
 * Banned: full-width gold bar You↔foe; whole-row tint flash; plate wash; shield-block;
 * another bust-coverage rescale of the CLEAVE sheet as the primary FX.
 */
@Composable
fun CombatStrokeOverlay(
    stroke: FxStrokeSpec?,
    visible: Boolean,
    modifier: Modifier = Modifier,
    tier: FxTier = FxTier.SMALL,
    /** Scaled hold (already 1x/2x); drives peak linger then fade. */
    holdMs: Long = CombatFx.STROKE_SMALL_MS,
    /**
     * Debug target label (`You` / foe name / `foe`) — logs `FX stroke on <target>`
     * always when path draws; `FX slash-light on <target>` only if tip garnish draws.
     */
    debugTarget: String = "foe",
    /** Invoked once per visible path stroke with `FX stroke on …` (combat log). */
    onStrokeDebug: ((String) -> Unit)? = null,
    /** Invoked once when optional CLEAVE tip garnish actually draws. */
    onSlashLightDebug: ((String) -> Unit)? = null,
    /** Ashbrand SPARK short stroke — shorter peak via [CombatFx.strokePeakMs]. */
    sparkStroke: Boolean = false
) {
    if (!visible || stroke == null || stroke.thickness <= 0f) return
    val color = Color(stroke.colorArgb)
    val recipient = stroke.recipient
    val context = LocalContext.current
    // Optional tip garnish sheet — never primary; fail-soft if missing
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
    // Drawn path is always the readable cut
    LaunchedEffect(visible, debugTarget, tier, sparkStroke) {
        if (visible) {
            val line = CombatFx.strokeDebugLine(debugTarget)
            Log.i(CombatFx.LOG_TAG_TOD_FX, line)
            onStrokeDebug?.invoke(line)
        }
    }
    // Tip garnish debug only when sheet is present (actually drawn at tip)
    LaunchedEffect(visible, slashSheet != null, debugTarget, tier) {
        if (visible && slashSheet != null) {
            val line = CombatFx.slashLightDebugLine(debugTarget)
            Log.i(CombatFx.LOG_TAG_TOD_FX, line)
            onSlashLightDebug?.invoke(line)
        }
    }
    var fadeAlpha by remember(visible, holdMs, tier, sparkStroke) { mutableFloatStateOf(1f) }
    LaunchedEffect(visible, holdMs, tier, sparkStroke) {
        if (!visible) {
            fadeAlpha = 1f
            return@LaunchedEffect
        }
        val peak = CombatFx.strokePeakMs(holdMs, recipient, spark = sparkStroke)
        val fade = (holdMs - peak).coerceAtLeast(1L)
        fadeAlpha = 1f
        delay(peak)
        val steps = 8
        val stepMs = (fade / steps).coerceAtLeast(1L)
        for (i in 1..steps) {
            fadeAlpha = (1f - i.toFloat() / steps).coerceIn(0.15f, 1f)
            delay(stepMs)
        }
        fadeAlpha = 0.15f
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
            // PRIMARY: Wake-family drawn path (glow + thick core crescent)
            drawWakeFamilyStroke(stroke, tier, color, fadeAlpha)
            // OPTIONAL: tiny CLEAVE tip garnish at stroke tip only (not full slash)
            if (slashSheet != null) {
                drawCleaveTipGarnish(
                    sheet = slashSheet,
                    stroke = stroke,
                    tier = tier,
                    alpha = fadeAlpha
                )
            }
        }
    }
}

/**
 * Wake-family readable cut — glow + thick core quadratic crescent on recipient bust.
 * Path length from [CombatFx.slashCutGeom]; canvas WIDTH from strokeCoreWidthPx (v0.1.47 fat).
 */
private fun DrawScope.drawWakeFamilyStroke(
    stroke: FxStrokeSpec,
    tier: FxTier,
    color: Color,
    alpha: Float
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
    val mid = Offset(cx + ex * 0.12f, cy - ey * 0.55f)
    val path = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(mid.x, mid.y, end.x, end.y)
    }
    val bustW = w * 0.5f * CombatFx.STROKE_BUST_WIDTH_FRAC
    val coreW = CombatFx.strokeCoreWidthPx(stroke.recipient, tier, bustW)
    val glowW = CombatFx.strokeGlowWidthPx(stroke.recipient, tier, bustW)
    val a = alpha.coerceIn(0.15f, 1f)
    drawPath(
        path = path,
        color = color.copy(alpha = 0.42f * a),
        style = Stroke(
            width = glowW * 1.15f,
            cap = StrokeCap.Round
        )
    )
    drawPath(
        path = path,
        color = color.copy(alpha = 0.62f * a),
        style = Stroke(
            width = glowW * 0.65f,
            cap = StrokeCap.Round
        )
    )
    drawPath(
        path = path,
        color = color.copy(alpha = 0.98f * a),
        style = Stroke(
            width = coreW,
            cap = StrokeCap.Round
        )
    )
    drawPath(
        path = path,
        color = Color(CombatFx.COLOR_STROKE_CORE_HIGHLIGHT).copy(alpha = 0.65f * a),
        style = Stroke(
            width = coreW * 0.38f,
            cap = StrokeCap.Round
        )
    )
}

/**
 * Optional CLEAVE tip garnish — small dst at stroke tip only.
 * Invisible tip is NOT fail if path stroke is readable. Never full slash / bust rescale.
 */
private fun DrawScope.drawCleaveTipGarnish(
    sheet: ImageBitmap,
    stroke: FxStrokeSpec,
    tier: FxTier,
    alpha: Float
) {
    val w = size.width
    val h = size.height
    val geom = CombatFx.slashCutGeom(stroke.recipient, tier)
    val cx = geom.centerXFrac * w
    val cy = geom.centerYFrac * h
    val ex = geom.halfExtentXFrac * w
    val ey = geom.halfExtentYFrac * h
    val tipX = cx + ex
    val tipY = cy + ey
    val tipSize = (min(w, h) * 0.11f).coerceAtLeast(24f)
    val frame = CleaveKit.SLASH_PEAK_FRAME
    val (sx, sy) = CleaveKit.slashCropOrigin(frame)
    val crop = CleaveKit.SLASH_CROP_PX
    val dstLeft = tipX - tipSize * 0.55f
    val dstTop = tipY - tipSize * 0.55f
    val a = (alpha * 0.75f).coerceIn(0.1f, 1f)
    drawImage(
        image = sheet,
        srcOffset = androidx.compose.ui.unit.IntOffset(sx, sy),
        srcSize = androidx.compose.ui.unit.IntSize(crop, crop),
        dstOffset = androidx.compose.ui.unit.IntOffset(dstLeft.toInt(), dstTop.toInt()),
        dstSize = androidx.compose.ui.unit.IntSize(
            tipSize.toInt().coerceAtLeast(1),
            tipSize.toInt().coerceAtLeast(1)
        ),
        alpha = a,
        colorFilter = null,
        filterQuality = FilterQuality.Low
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
