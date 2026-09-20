package com.towerofdarkness.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.ui.theme.Ash
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Ember
import com.towerofdarkness.app.ui.theme.GlowLegendary
import com.towerofdarkness.app.ui.theme.GlowRare
import com.towerofdarkness.app.ui.theme.GlowUncommon
import com.towerofdarkness.app.ui.theme.Steel

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
        Rarity.EPIC -> GlowUncommon.copy(alpha = alpha * 0.55f) // reuse
        Rarity.LEGENDARY -> GlowLegendary.copy(alpha = alpha * 0.7f)
    }
    Box(modifier.size(180.dp)) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2
            // shadow
            drawOval(Ash.copy(alpha = 0.5f), topLeft = Offset(cx - w * 0.28f, h * 0.82f), size = androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.1f))
            // glow
            if (glow.alpha > 0f) {
                drawCircle(glow, radius = w * 0.42f, center = Offset(cx, h * 0.45f))
            }
            // body
            drawRoundRect(Bone, topLeft = Offset(cx - w * 0.12f, h * 0.28f), size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.42f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
            // head
            drawCircle(Bone, radius = w * 0.1f, center = Offset(cx, h * 0.22f))
            // armor
            drawRoundRect(Steel, topLeft = Offset(cx - w * 0.16f, h * 0.32f), size = androidx.compose.ui.geometry.Size(w * 0.32f, h * 0.22f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f))
            // weapon
            drawRect(Ember, topLeft = Offset(cx + w * 0.18f, h * 0.2f), size = androidx.compose.ui.geometry.Size(w * 0.06f, h * 0.45f))
            // accessory cape
            drawRect(Ash.copy(alpha = 0.8f), topLeft = Offset(cx - w * 0.22f, h * 0.34f), size = androidx.compose.ui.geometry.Size(w * 0.1f, h * 0.35f))
        }
    }
}

@Composable
fun EnemySilhouette(kindLabel: String, isBoss: Boolean, modifier: Modifier = Modifier) {
    Box(modifier.size(if (isBoss) 160.dp else 120.dp)) {
        Canvas(Modifier.matchParentSize()) {
            val cx = size.width / 2
            val body = if (isBoss) Ember else Steel
            drawOval(Ash.copy(0.4f), topLeft = Offset(cx - size.width * 0.3f, size.height * 0.85f), size = androidx.compose.ui.geometry.Size(size.width * 0.6f, size.height * 0.1f))
            drawCircle(body, radius = size.minDimension * 0.28f, center = Offset(cx, size.height * 0.45f))
            if (isBoss) {
                // wings
                drawOval(Ash, topLeft = Offset(0f, size.height * 0.2f), size = androidx.compose.ui.geometry.Size(size.width * 0.35f, size.height * 0.4f))
                drawOval(Ash, topLeft = Offset(size.width * 0.65f, size.height * 0.2f), size = androidx.compose.ui.geometry.Size(size.width * 0.35f, size.height * 0.4f))
            }
        }
    }
}
