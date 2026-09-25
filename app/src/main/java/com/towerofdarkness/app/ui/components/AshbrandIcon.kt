package com.towerofdarkness.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.towerofdarkness.app.R
import com.towerofdarkness.app.domain.combat.WakeArt
import com.towerofdarkness.app.domain.volume.VolumeArt
import com.towerofdarkness.app.domain.combat.WakeIconPhase
import com.towerofdarkness.app.ui.theme.Gold

/**
 * Ashbrand slot icon — painted portrait still in the weapon tile
 * ([R.drawable.ashbrand_icon]; v0.1.34-blade). Engraved fuller + diamond pommel
 * visible; charcoal bg / ember sparks kept. **No rembg / knockout redraw.**
 * Same `R.drawable.ashbrand_icon` name (JPEG still). ContentScale.Fit.
 * v0.1.20: Art-baked volume on blade icon; Compose chrome no-op (not Wake VFX / spark).
 * Same size on loadout weapon plate and combat weapon row.
 * SPARK: [R.drawable.ashbrand_spark] ember overlay only (no crescent).
 * Tap ([onClick]) opens glossary only — never fires Wake / spends Sparks.
 */
@Composable
fun AshbrandIcon(
    phase: WakeIconPhase = WakeIconPhase.IDLE,
    modifier: Modifier = Modifier,
    slotSize: Dp = WakeArt.ICON_SLOT_DP.dp,
    onClick: (() -> Unit)? = null
) {
    val brighten = when (phase) {
        WakeIconPhase.CHARGE -> 1.35f
        WakeIconPhase.CRACK -> 1.15f
        else -> 1f
    }
    val matrix = ColorMatrix(
        floatArrayOf(
            brighten, 0f, 0f, 0f, if (phase == WakeIconPhase.CHARGE) 18f else 0f,
            0f, brighten, 0f, 0f, if (phase == WakeIconPhase.CHARGE) 12f else 0f,
            0f, 0f, brighten * 0.95f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    )
    val seamPhase = phase == WakeIconPhase.CHARGE || phase == WakeIconPhase.CRACK
    // circular=false; volumeChrome is a no-op (Art bake SoT) — do not invent glyph-plate chrome.
    val chrome = if (VolumeArt.appliesToAshbrandIcon()) Modifier.volumeChrome(circular = false) else Modifier
    val tap = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(modifier.size(slotSize).then(chrome).then(tap), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.ashbrand_icon),
            contentDescription = "Ashbrand",
            contentScale = ContentScale.Fit,
            colorFilter = if (brighten != 1f) ColorFilter.colorMatrix(matrix) else null,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (seamPhase) {
                        Modifier.drawWithContent {
                            drawContent()
                            val w = this.size.width
                            val h = this.size.height
                            val cx = w / 2f
                            val seam = if (phase == WakeIconPhase.CRACK) Gold.copy(alpha = 0.95f)
                            else Gold.copy(alpha = 0.55f)
                            val stroke = if (phase == WakeIconPhase.CRACK) w * 0.06f else w * 0.035f
                            drawLine(
                                color = seam,
                                start = Offset(cx, h * 0.12f),
                                end = Offset(cx, h * 0.62f),
                                strokeWidth = stroke
                            )
                            if (phase == WakeIconPhase.CRACK) {
                                drawLine(
                                    color = Color(0xFFFBBF24).copy(alpha = 0.7f),
                                    start = Offset(cx - w * 0.08f, h * 0.28f),
                                    end = Offset(cx + w * 0.1f, h * 0.48f),
                                    strokeWidth = w * 0.025f
                                )
                            }
                        }
                    } else Modifier
                )
        )
        if (phase == WakeIconPhase.SPARK_EMBER) {
            Image(
                painter = painterResource(R.drawable.ashbrand_spark),
                contentDescription = "Ashbrand spark",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
