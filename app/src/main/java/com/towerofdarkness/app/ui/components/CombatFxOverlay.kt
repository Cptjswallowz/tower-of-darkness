package com.towerofdarkness.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.towerofdarkness.app.domain.combat.CombatFx
import com.towerofdarkness.app.domain.combat.FxRecipient
import com.towerofdarkness.app.domain.combat.FxStrokeSpec
import com.towerofdarkness.app.domain.combat.FxTier
import kotlin.math.min

/**
 * Presentation layer for v0.1.39-fxread recipient slash (heavier stroke) + Brace pips.
 * Wake slash stays in [WakeStageOverlay] — do not draw Wake tier here.
 * Banned: full-width gold bar You↔foe; whole-row tint flash.
 */
@Composable
fun CombatStrokeOverlay(
    stroke: FxStrokeSpec?,
    visible: Boolean,
    modifier: Modifier = Modifier,
    tier: FxTier = FxTier.SMALL
) {
    if (!visible || stroke == null || stroke.thickness <= 0f) return
    val color = Color(stroke.colorArgb)
    val recipient = stroke.recipient
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val geom = CombatFx.slashCutGeom(recipient, tier)
        val cx = geom.centerXFrac * w
        val cy = geom.centerYFrac * h
        val ex = geom.halfExtentXFrac * w
        val ey = geom.halfExtentYFrac * h
        // Short diagonal cut — arc-ish via slight quadratic bend
        val start = Offset(cx - ex, cy - ey)
        val end = Offset(cx + ex, cy + ey)
        val mid = Offset(cx + ex * 0.15f, cy - ey * 0.35f)
        val path = Path().apply {
            moveTo(start.x, start.y)
            quadraticTo(mid.x, mid.y, end.x, end.y)
        }
        // Soft ember glow (still local to bust)
        drawPath(
            path = path,
            color = color.copy(alpha = 0.45f),
            style = Stroke(width = stroke.thickness * 7f, cap = StrokeCap.Round)
        )
        drawPath(
            path = path,
            color = color.copy(alpha = 0.92f),
            style = Stroke(width = stroke.thickness * 3.2f, cap = StrokeCap.Round)
        )
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
 * Tiny shield pips that float/fade around the Brace owner bust (You or foe).
 * Count already capped via [CombatFx.bracePipCount]. Under-bust Brace number stays elsewhere.
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
    val moss = Color(0xFF6B8F5AL)
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
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
            // Tiny diamond / shield pip
            val path = Path().apply {
                moveTo(px, py - pipR * 1.4f)
                lineTo(px + pipR, py)
                lineTo(px, py + pipR * 1.5f)
                lineTo(px - pipR, py)
                close()
            }
            drawPath(path, moss.copy(alpha = alpha))
            drawPath(
                path,
                Color.White.copy(alpha = alpha * 0.35f),
                style = Stroke(width = 1.2f)
            )
        }
    }
}
