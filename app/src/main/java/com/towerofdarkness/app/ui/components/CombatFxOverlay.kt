package com.towerofdarkness.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.towerofdarkness.app.domain.combat.FxStrokeDir
import com.towerofdarkness.app.domain.combat.FxStrokeSpec

/**
 * Presentation layer for v0.1.37-fx kernel stroke (You↔foe).
 * Wake slash stays in [WakeStageOverlay] — do not draw Wake tier here.
 */
@Composable
fun CombatStrokeOverlay(
    stroke: FxStrokeSpec?,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible || stroke == null || stroke.thickness <= 0f) return
    val color = Color(stroke.colorArgb)
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        // Portrait row: You left, foe right — stroke across mid band
        val y = h * 0.42f
        val left = Offset(w * 0.18f, y)
        val right = Offset(w * 0.82f, y)
        val (start, end) = when (stroke.dir) {
            FxStrokeDir.YOU_TO_FOE -> left to right
            FxStrokeDir.FOE_TO_YOU -> right to left
        }
        drawLine(
            color = color.copy(alpha = 0.92f),
            start = start,
            end = end,
            strokeWidth = stroke.thickness * 4f,
            cap = StrokeCap.Round
        )
        // Soft ember core
        drawLine(
            color = color.copy(alpha = 0.55f),
            start = start,
            end = end,
            strokeWidth = stroke.thickness * 8f,
            cap = StrokeCap.Round
        )
    }
}

/** Brief gold/ember flash on the fired skill strip area (top of bar region). */
@Composable
fun CombatTileFlash(
    visible: Boolean,
    colorArgb: Long,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    Box(
        modifier
            .fillMaxSize()
            .background(Color(colorArgb).copy(alpha = 0.28f))
    )
}
