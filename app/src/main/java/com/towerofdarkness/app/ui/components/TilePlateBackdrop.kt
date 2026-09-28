package com.towerofdarkness.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.towerofdarkness.app.R
import com.towerofdarkness.app.domain.art.SharedTilePlate

/**
 * PART B — Art [R.drawable.ui_tile_plate] under content.
 * Skill / Ashbrand / Forge: plate as-is (baked center α ≈ 0.35).
 * Node discs: same drawable at [SharedTilePlate.NODE_DISC_PLATE_ALPHA].
 * No FX packs drawn on the plate.
 */
@Composable
fun SharedTilePlateBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 6.dp,
    /** Extra Compose alpha (1f = use baked PNG alpha only). */
    plateAlpha: Float = SharedTilePlate.OPACITY,
    circular: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val shape: Shape = if (circular) CircleShape else RoundedCornerShape(cornerRadius)
    Box(modifier) {
        if (SharedTilePlate.HOOKS_READY) {
            Image(
                painter = painterResource(R.drawable.ui_tile_plate),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .alpha(SharedTilePlate.opacityInBand(plateAlpha))
            )
        }
        content()
    }
}

/** Always true once ui_tile_plate is packaged (PART B Art drop). */
@Composable
fun sharedTilePlateAvailable(): Boolean = SharedTilePlate.HOOKS_READY
