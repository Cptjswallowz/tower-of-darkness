package com.towerofdarkness.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.towerofdarkness.app.R
import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.domain.combat.BodyArt
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.volume.VolumeArt
import com.towerofdarkness.app.ui.theme.Ash
import com.towerofdarkness.app.ui.theme.Ember
import com.towerofdarkness.app.ui.theme.GlowLegendary
import com.towerofdarkness.app.ui.theme.GlowRare
import com.towerofdarkness.app.ui.theme.GlowUncommon
import com.towerofdarkness.app.ui.theme.Steel

/**
 * Player portrait — v0.1.18 circular soldier still ([R.drawable.portrait_you]).
 * v0.1.20: Art-baked volume PNG; volumeChrome no-op (no double shadow; no pip shift).
 * Sits under frames / Wake crescent / pips; shrink sprite on clip, don't move chrome.
 * Optional rarity glow rings behind the still (no wardrobe / gear overlays).
 */
@Composable
fun HeroShowcase(rarity: Rarity = Rarity.UNCOMMON, modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition(label = "glow")
    val alpha by pulse.animateFloat(
        0.25f, 0.7f,
        infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label = "a"
    )
    val glow = when (rarity) {
        Rarity.COMMON -> Color.Transparent
        Rarity.UNCOMMON -> GlowUncommon.copy(alpha = alpha * 0.35f)
        Rarity.RARE -> GlowRare.copy(alpha = alpha * 0.45f)
        Rarity.EPIC -> GlowUncommon.copy(alpha = alpha * 0.55f)
        Rarity.LEGENDARY -> GlowLegendary.copy(alpha = alpha * 0.7f)
    }
    val slot = BodyArt.PLAYER_SLOT_DP.dp
    Box(
        modifier.then(Modifier.size(slot)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            if (glow.alpha > 0f) {
                drawCircle(glow, radius = size.minDimension * 0.48f, center = Offset(cx, cy))
            }
        }
        val inset = 1f - BodyArt.SPRITE_INSET_FRACTION
        // Volume chrome wraps the clipped still; drawBehind stays outside content box.
        Box(
            Modifier
                .fillMaxSize(inset)
                .then(if (VolumeArt.appliesToPlayerPortrait()) Modifier.volumeChrome(circular = true) else Modifier)
        ) {
            Image(
                painter = painterResource(R.drawable.portrait_you),
                contentDescription = "You",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    }
}

/**
 * Enemy portrait slot. Ash-Warden (F2) uses [R.drawable.portrait_ash_warden] + volume;
 * F1 Seal-Warden / trash / Seal Spinner keep Canvas orange/steel placeholders (no volume).
 */
@Composable
fun EnemySilhouette(
    kind: EnemyKind,
    isBoss: Boolean,
    modifier: Modifier = Modifier
) {
    val bodyName = BodyArt.enemyPortraitDrawableName(kind)
    val slotDp = when {
        bodyName != null -> BodyArt.BOSS_SLOT_DP
        isBoss -> BodyArt.BOSS_SLOT_DP
        else -> BodyArt.TRASH_SLOT_DP
    }.dp
    Box(modifier.then(Modifier.size(slotDp)), contentAlignment = Alignment.Center) {
        if (bodyName != null && VolumeArt.appliesToEnemy(kind)) {
            val inset = 1f - BodyArt.SPRITE_INSET_FRACTION
            Box(
                Modifier
                    .fillMaxSize(inset)
                    .volumeChrome(circular = true)
            ) {
                Image(
                    painter = painterResource(R.drawable.portrait_ash_warden),
                    contentDescription = kind.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        } else if (bodyName != null) {
            // Asset-backed but no volume (should not happen for current map).
            val inset = 1f - BodyArt.SPRITE_INSET_FRACTION
            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2
                drawOval(
                    Ash.copy(0.4f),
                    topLeft = Offset(cx - size.width * 0.3f, size.height * 0.85f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.6f, size.height * 0.1f)
                )
            }
            Image(
                painter = painterResource(R.drawable.portrait_ash_warden),
                contentDescription = kind.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize(inset)
                    .clip(CircleShape)
            )
        } else {
            // Seal-Warden / trash / Seal Spinner — Canvas placeholders, no volume.
            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val body = if (isBoss) Ember else Steel
                drawOval(
                    Ash.copy(0.4f),
                    topLeft = Offset(cx - size.width * 0.3f, size.height * 0.85f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.6f, size.height * 0.1f)
                )
                drawCircle(body, radius = size.minDimension * 0.28f, center = Offset(cx, size.height * 0.45f))
                if (isBoss) {
                    drawOval(
                        Ash,
                        topLeft = Offset(0f, size.height * 0.2f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.35f, size.height * 0.4f)
                    )
                    drawOval(
                        Ash,
                        topLeft = Offset(size.width * 0.65f, size.height * 0.2f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.35f, size.height * 0.4f)
                    )
                }
            }
        }
    }
}
