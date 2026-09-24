package com.towerofdarkness.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Volume polish — v0.1.20-volume.
 *
 * Art PNGs already bake soft drop shadow + gold-ash top-left rim + 2-tone folds
 * (tools/gen_volume_v0120.py / docs/art-audio/VOLUME_v0.1.20.md). Compose must
 * NOT redraw those, or tiles/figures get double shadows / layout shift.
 *
 * This wrapper is intentional identity wiring so call sites stay marked as
 * volume surfaces without a second pass.
 */
@Composable
fun VolumeFrame(
    modifier: Modifier = Modifier,
    circular: Boolean = false,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.volumeChrome(circular = circular),
        contentAlignment = contentAlignment,
        content = content
    )
}

/**
 * No-op: Art bake is the volume source of truth. Kept so call sites remain
 * explicit volume-surface markers without doubling shadow/rim.
 */
@Suppress("UNUSED_PARAMETER")
fun Modifier.volumeChrome(circular: Boolean = false): Modifier = this
