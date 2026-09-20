package com.towerofdarkness.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ash = Color(0xFF2B2A28)
val Bone = Color(0xFFE6D7B8)
val Ember = Color(0xFFC45A2D)
val Moss = Color(0xFF4A5C3A)
val Steel = Color(0xFF6B7280)
val Rim = Color(0xFFF0E6D0)
val VoidBg = Color(0xFF0D0A14)
val Panel = Color(0xFF1A1228)
val Accent = Color(0xFF7B5EA7)
val Gold = Color(0xFFC9A227)

val GlowUncommon = Color(0xFF4ADE80)
val GlowRare = Color(0xFF38BDF8)
val GlowEpic = Color(0xFFA78BFA)
val GlowLegendary = Color(0xFFFBBF24)

private val Scheme = darkColorScheme(
    primary = Accent,
    onPrimary = Bone,
    secondary = Ember,
    onSecondary = Bone,
    background = VoidBg,
    onBackground = Bone,
    surface = Panel,
    onSurface = Bone,
    tertiary = Gold
)

@Composable
fun TowerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
