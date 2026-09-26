package com.towerofdarkness.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.towerofdarkness.app.domain.combat.CombatFx
import com.towerofdarkness.app.domain.combat.FxRecipient
import com.towerofdarkness.app.domain.combat.FxStrokeSpec
import com.towerofdarkness.app.domain.combat.FxTier
import com.towerofdarkness.app.ui.theme.Moss
import kotlin.math.min

/**
 * Presentation layer for v0.1.40-fxfix — recipient slash (clipped half-stage + tamed glow)
 * + Brace Shield stamps. Wake slash stays in [WakeStageOverlay].
 * Banned: full-width gold bar You↔foe; whole-row tint flash; plate wash over both busts.
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
        val clip = CombatFx.recipientClipXFrac(recipient)
        // Hard clip to recipient half-stage so glow cannot wash both busts
        clipRect(
            left = clip.start * w,
            top = 0f,
            right = clip.endInclusive * w,
            bottom = h
        ) {
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
            // Soft ember glow — absolute ≈ v0.1.38 (not thickness×7 unbounded)
            drawPath(
                path = path,
                color = color.copy(alpha = 0.45f),
                style = Stroke(
                    width = CombatFx.strokeGlowWidth(stroke.thickness),
                    cap = StrokeCap.Round
                )
            )
            // Core cut — full 1.4× readability thickness
            drawPath(
                path = path,
                color = color.copy(alpha = 0.92f),
                style = Stroke(
                    width = CombatFx.strokeCoreWidth(stroke.thickness),
                    cap = StrokeCap.Round
                )
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
 * Clipped to owner half-stage (same rule as slash).
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
                // Secondary Path diamond (under stamp)
                val diamond = Path().apply {
                    moveTo(px, py - pipR * 1.4f)
                    lineTo(px + pipR, py)
                    lineTo(px, py + pipR * 1.5f)
                    lineTo(px - pipR, py)
                    close()
                }
                drawPath(diamond, moss.copy(alpha = alpha * 0.55f))
                // Primary: Shield vector stamp (StatusPipRow language) — always visible
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
