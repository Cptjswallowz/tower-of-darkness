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
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import com.towerofdarkness.app.domain.combat.GarnishKind
import com.towerofdarkness.app.domain.combat.GarnishSpec
import com.towerofdarkness.app.domain.combat.PlumeGarnishKit
import com.towerofdarkness.app.ui.theme.Moss
import kotlin.math.min
import kotlinx.coroutines.delay

/**
 * Presentation layer for v0.1.50-puffhold — drawn Wake-family stroke PRIMARY (LOCKED).
 * Filled crescent blade (outer+inner quadratic arcs; NOT StrokeCap.Round stadium pill)
 * with glow + core layers on recipient bust (same family as Wake arc language).
 * Optional CLEAVE slash-light tip garnish at stroke TIP only — never the slash.
 * v0.1.49: PLUME/Kenney particle garnish overlays (tip spark / dust puff / Soften / Brace flare)
 * share this file's drawImage engine — stroke width/path/peaks/colors untouched.
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
 * Wake-family readable cut — filled crescent blade (outer + reverse inner arc) on recipient bust.
 * v0.1.48: NOT StrokeCap.Round on a modest quadratic (that was the stadium/capsule pill).
 * Path length from [CombatFx.slashCutGeom]; belly thickness from strokeCoreWidthPx (v0.1.47 fat kept).
 * Bow bulge ≥ half-chord ([CombatFx.STROKE_CRESCENT_BOW_MULT]) so the cut reads as a curved blade.
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
    val dx = end.x - start.x
    val dy = end.y - start.y
    val halfChord = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat() * 0.5f
    val chordLen = (halfChord * 2f).coerceAtLeast(1f)
    // Unit perpendicular; force upward bulge (Wake-like belly above the diagonal)
    var nx = -dy / chordLen
    var ny = dx / chordLen
    if (ny > 0f) {
        nx = -nx
        ny = -ny
    }
    val midX = (start.x + end.x) * 0.5f
    val midY = (start.y + end.y) * 0.5f
    val bustW = w * 0.5f * CombatFx.STROKE_BUST_WIDTH_FRAC
    val coreW = CombatFx.strokeCoreWidthPx(stroke.recipient, tier, bustW)
    val glowW = CombatFx.strokeGlowWidthPx(stroke.recipient, tier, bustW)
    val a = alpha.coerceIn(0.15f, 1f)
    val outerBow = CombatFx.crescentBowPx(halfChord)
    // Belly thickness tracks coreW (bust-frac lock); frac is a floor so tips still taper
    val innerFloor = CombatFx.crescentInnerBowPx(outerBow)
    val innerBow = (outerBow - coreW).coerceAtLeast(innerFloor * 0.55f).coerceAtMost(outerBow * 0.92f)
    // Soft glow crescent (wider than core)
    val glowOuter = outerBow + glowW * 0.22f
    val glowInner = (innerBow - glowW * 0.20f).coerceAtLeast(halfChord * 0.06f)
    drawPath(
        path = crescentBladePath(start, end, midX, midY, nx, ny, glowOuter, glowInner),
        color = color.copy(alpha = 0.38f * a)
    )
    drawPath(
        path = crescentBladePath(start, end, midX, midY, nx, ny, outerBow + glowW * 0.08f, innerBow),
        color = color.copy(alpha = 0.55f * a)
    )
    // Core blade — filled crescent (tips taper; belly ≈ core width)
    drawPath(
        path = crescentBladePath(start, end, midX, midY, nx, ny, outerBow, innerBow),
        color = color.copy(alpha = 0.98f * a)
    )
    // Hot highlight — thinner inner crescent
    val hiOuter = (outerBow + innerBow) * 0.5f + coreW * 0.08f
    val hiInner = (innerBow + (innerBow * 0.55f)) * 0.5f
    drawPath(
        path = crescentBladePath(start, end, midX, midY, nx, ny, hiOuter, hiInner.coerceAtMost(hiOuter * 0.92f)),
        color = Color(CombatFx.COLOR_STROKE_CORE_HIGHLIGHT).copy(alpha = 0.62f * a)
    )
}

/**
 * Filled crescent / blade Path: outer quadratic arc start→end, reverse inner arc end→start.
 * Tips meet at [start]/[end] so the silhouette tapers like a Wake crescent — not a Round pill.
 */
private fun crescentBladePath(
    start: Offset,
    end: Offset,
    midX: Float,
    midY: Float,
    nx: Float,
    ny: Float,
    outerBow: Float,
    innerBow: Float
): Path {
    val outerMid = Offset(midX + nx * outerBow, midY + ny * outerBow)
    val innerMid = Offset(midX + nx * innerBow, midY + ny * innerBow)
    return Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(outerMid.x, outerMid.y, end.x, end.y)
        quadraticTo(innerMid.x, innerMid.y, start.x, start.y)
        close()
    }
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

/**
 * Resolve a drawable ImageBitmap by compile-linked R.drawable or getIdentifier fail-soft.
 */
@Composable
private fun rememberGarnishSheet(drawable: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(drawable) {
        try {
            val field = R.drawable::class.java.getField(drawable)
            val id = field.getInt(null)
            ImageBitmap.imageResource(context.resources, id)
        } catch (_: Throwable) {
            try {
                val id = context.resources.getIdentifier(drawable, "drawable", context.packageName)
                if (id == 0) null else ImageBitmap.imageResource(context.resources, id)
            } catch (_: Throwable) {
                null
            }
        }
    }
}

/**
 * PLUME / Kenney particle garnish — one FX engine with CLEAVE (drawImage cell playback).
 * Tip spark at stroke tip; dust puff centered on TARGET bust; Soften under foe HP;
 * Brace flare on owner GAIN only. Half-stage clip. Stroke path untouched.
 */
@Composable
fun CombatParticleGarnishOverlay(
    spec: GarnishSpec?,
    recipient: FxRecipient?,
    visible: Boolean,
    modifier: Modifier = Modifier,
    /** Scaled hold (already 1x/2x). */
    holdMs: Long = 0L,
    debugTarget: String = "foe",
    /** Place Soften pip slightly below bust center (under HP chrome). */
    underHp: Boolean = false,
    /** Tip mode: anchor at stroke tip (end of slash geom) instead of bust center. */
    atStrokeTip: Boolean = false,
    tier: FxTier = FxTier.SMALL,
    onDebug: ((String) -> Unit)? = null
) {
    if (!visible || spec == null || recipient == null) return
    val sheet = rememberGarnishSheet(spec.drawable) ?: return
    val hold = if (holdMs > 0L) holdMs else spec.holdMs
    LaunchedEffect(visible, spec.drawable, debugTarget, spec.pack) {
        if (visible) {
            val line = PlumeGarnishKit.debugLine(spec, debugTarget)
            Log.i(CombatFx.LOG_TAG_TOD_FX, line)
            onDebug?.invoke(line)
        }
    }
    var progress by remember(visible, hold, spec.drawable, underHp) { mutableFloatStateOf(0f) }
    LaunchedEffect(visible, hold, spec.drawable, underHp) {
        if (!visible) {
            progress = 0f
            return@LaunchedEffect
        }
        // Soften under HP: hold mid plateau while status visible (not a one-shot fade-out)
        if (underHp && spec.kind == GarnishKind.SOFTEN_PIP) {
            progress = 0.45f
            return@LaunchedEffect
        }
        val steps = if (spec.atlas) {
            PlumeGarnishKit.atlasPlayFrames(spec.atlasFrames, spec.atlasPeakFrame).size
        } else {
            8
        }
        val step = (hold / steps.coerceAtLeast(1)).coerceAtLeast(1L)
        for (i in 1..steps) {
            progress = i.toFloat() / steps
            delay(step)
        }
        // End on late-peak frame (still readable) rather than empty fade
        progress = 0.72f
    }
    val tint = Color(spec.tintArgb)
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
            drawParticleGarnish(
                sheet = sheet,
                spec = spec,
                recipient = recipient,
                progress = progress,
                tint = tint,
                underHp = underHp,
                atStrokeTip = atStrokeTip,
                tier = tier
            )
        }
    }
}

private fun DrawScope.drawParticleGarnish(
    sheet: ImageBitmap,
    spec: GarnishSpec,
    recipient: FxRecipient,
    progress: Float,
    tint: Color,
    underHp: Boolean,
    atStrokeTip: Boolean,
    tier: FxTier
) {
    val w = size.width
    val h = size.height
    val bustW = w * 0.5f * CombatFx.STROKE_BUST_WIDTH_FRAC
    val drawPx = (bustW * spec.bustFrac).coerceAtLeast(12f)
    val cx: Float
    val cy: Float
    if (atStrokeTip) {
        val geom = CombatFx.slashCutGeom(recipient, tier)
        cx = (geom.centerXFrac + geom.halfExtentXFrac) * w
        cy = (geom.centerYFrac + geom.halfExtentYFrac) * h
    } else {
        cx = when (recipient) {
            FxRecipient.YOU -> CombatFx.YOU_BUST_X * w
            FxRecipient.FOE -> CombatFx.FOE_BUST_X * w
        }
        val baseY = CombatFx.BUST_Y * h
        cy = if (underHp) baseY + bustW * 0.42f else baseY
    }
    val fade = when {
        progress < 0.15f -> (progress / 0.15f).coerceIn(0.2f, 1f)
        progress > 0.75f -> (1f - (progress - 0.75f) / 0.25f).coerceIn(0.15f, 1f)
        else -> 1f
    }
    val alpha = (fade * 0.92f).coerceIn(0.1f, 1f)
    val blend = if (spec.additive) BlendMode.Plus else BlendMode.SrcOver
    val filter = ColorFilter.tint(tint.copy(alpha = 1f), BlendMode.SrcIn)
    if (spec.atlas) {
        val play = PlumeGarnishKit.atlasPlayFrames(spec.atlasFrames, spec.atlasPeakFrame)
        val frame = PlumeGarnishKit.frameAt(play, progress)
        val (sx, sy) = PlumeGarnishKit.atlasCellOrigin(frame, spec.atlasCols, spec.atlasCellPx)
        val cell = spec.atlasCellPx
        val dstLeft = cx - drawPx * spec.anchorX
        val dstTop = cy - drawPx * spec.anchorY
        drawImage(
            image = sheet,
            srcOffset = androidx.compose.ui.unit.IntOffset(sx, sy),
            srcSize = androidx.compose.ui.unit.IntSize(cell, cell),
            dstOffset = androidx.compose.ui.unit.IntOffset(dstLeft.toInt(), dstTop.toInt()),
            dstSize = androidx.compose.ui.unit.IntSize(
                drawPx.toInt().coerceAtLeast(1),
                drawPx.toInt().coerceAtLeast(1)
            ),
            alpha = alpha,
            colorFilter = filter,
            blendMode = blend,
            filterQuality = FilterQuality.Low
        )
    } else {
        // Kenney single sprite — draw full bitmap scaled
        val dstLeft = cx - drawPx * 0.5f
        val dstTop = cy - drawPx * 0.5f
        drawImage(
            image = sheet,
            srcOffset = androidx.compose.ui.unit.IntOffset(0, 0),
            srcSize = androidx.compose.ui.unit.IntSize(sheet.width, sheet.height),
            dstOffset = androidx.compose.ui.unit.IntOffset(dstLeft.toInt(), dstTop.toInt()),
            dstSize = androidx.compose.ui.unit.IntSize(
                drawPx.toInt().coerceAtLeast(1),
                drawPx.toInt().coerceAtLeast(1)
            ),
            alpha = alpha,
            colorFilter = filter,
            blendMode = blend,
            filterQuality = FilterQuality.Low
        )
    }
}
