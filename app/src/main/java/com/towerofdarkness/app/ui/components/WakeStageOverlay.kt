package com.towerofdarkness.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.towerofdarkness.app.R
import com.towerofdarkness.app.domain.combat.WakeStageFrame

/**
 * FULL Wake crescent overlay aimed at the **foe** portrait (v0.1.38-fxaim).
 * Frames: charge → slash → impact (L→R crescent language baked in art).
 * Must NOT cover You — clipped/offset toward the right (foe) half of the stage.
 * SPARK / common skills: do not call with a non-NONE frame.
 */
@Composable
fun WakeStageOverlay(
    frame: WakeStageFrame,
    modifier: Modifier = Modifier
) {
    val res = when (frame) {
        WakeStageFrame.NONE -> null
        WakeStageFrame.CHARGE -> R.drawable.wake_vfx_charge
        WakeStageFrame.SLASH -> R.drawable.wake_vfx_slash
        WakeStageFrame.IMPACT -> R.drawable.wake_vfx_impact
    } ?: return
    // Right ~58% of stage only — You (left bust ~0.18) stays clear of plate/wash
    Box(
        modifier
            .clipToBounds()
            .fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.58f)
                .clipToBounds()
                .offset(x = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(res),
                contentDescription = "Wake",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
