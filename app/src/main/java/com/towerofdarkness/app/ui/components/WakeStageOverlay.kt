package com.towerofdarkness.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.towerofdarkness.app.R
import com.towerofdarkness.app.domain.combat.WakeStageFrame

/**
 * FULL Wake crescent overlay on the combat portrait stage.
 * Frames: charge → slash → impact (L→R crescent language baked in art).
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
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(res),
            contentDescription = "Wake",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
