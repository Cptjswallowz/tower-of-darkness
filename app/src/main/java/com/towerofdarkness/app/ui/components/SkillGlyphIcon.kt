package com.towerofdarkness.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.towerofdarkness.app.R
import com.towerofdarkness.app.domain.glyphs.SkillGlyph
import com.towerofdarkness.app.domain.glyphs.SkillJob
import com.towerofdarkness.app.domain.volume.VolumeArt
import com.towerofdarkness.app.ui.theme.Accent
import com.towerofdarkness.app.ui.theme.Ember
import com.towerofdarkness.app.ui.theme.Moss

/**
 * Shared skill-tile glyph (v0.1.19) — combat bar + loadout row.
 * v0.1.20: Art-baked volume PNG (shadow/rim/folds); volumeChrome is no-op (no double).
 * Size 24–32 dp above/beside name. No animations.
 * When [spent], greys glyph at [VolumeArt.DIMMED_GLYPH_ALPHA] — readable, not crushed.
 * Ashbrand uses [AshbrandIcon] only — do not call this for weapons.
 */
@Composable
fun SkillGlyphIcon(
    cardId: String,
    spent: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = SkillGlyph.GLYPH_SIZE_DP.dp
) {
    val resId = SkillGlyphResources.resId(cardId) ?: return
    val grey = ColorMatrix().apply { setToSaturation(0f) }
    Image(
        painter = painterResource(resId),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        colorFilter = if (spent) ColorFilter.colorMatrix(grey) else null,
        modifier = modifier
            .size(size)
            .then(
                if (VolumeArt.appliesToSkillGlyphTiles()) Modifier.volumeChrome(circular = false)
                else Modifier
            )
            .alpha(if (spent) VolumeArt.DIMMED_GLYPH_ALPHA else 1f)
    )
}

/** True when card is Brace-job (green outline). */
fun skillJobIsBrace(cardId: String): Boolean =
    SkillGlyph.job(cardId) == SkillJob.BRACE

/** Optional job-family border tint (Brace = Moss/green). */
fun skillJobBorderColor(cardId: String): Color? = when (SkillGlyph.job(cardId)) {
    SkillJob.BRACE -> Moss
    SkillJob.MIXED -> Accent
    SkillJob.DAMAGE -> Ember.copy(alpha = 0.55f)
    null -> null
}

/**
 * Resolves locked drawable names → [R.drawable] ids.
 * Art drop-in: replace `glyph_*.png` / PNG with same resource names.
 */
object SkillGlyphResources {
    @DrawableRes
    fun resId(cardId: String): Int? = when (SkillGlyph.drawableName(cardId)) {
        "glyph_hostflint" -> R.drawable.glyph_hostflint
        "glyph_emberbrand" -> R.drawable.glyph_emberbrand
        "glyph_tower_pike" -> R.drawable.glyph_tower_pike
        "glyph_ruin_seal" -> R.drawable.glyph_ruin_seal
        "glyph_shadow_latch" -> R.drawable.glyph_shadow_latch
        "glyph_cinder_step" -> R.drawable.glyph_cinder_step
        "glyph_iron_mantle" -> R.drawable.glyph_iron_mantle
        "glyph_vow_plate" -> R.drawable.glyph_vow_plate
        "glyph_dust_veil" -> R.drawable.glyph_dust_veil
        "glyph_ash_press" -> R.drawable.glyph_ash_press
        "glyph_relic_shard" -> R.drawable.glyph_relic_shard
        "glyph_cinder_vow" -> R.drawable.glyph_cinder_vow
        "glyph_grave_nail" -> R.drawable.glyph_grave_nail
        else -> null
    }
}
