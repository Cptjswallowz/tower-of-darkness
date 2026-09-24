package com.towerofdarkness.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
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
import com.towerofdarkness.app.domain.combat.EnemyLook
import com.towerofdarkness.app.domain.combat.PortraitPlate
import com.towerofdarkness.app.domain.volume.VolumeArt
import com.towerofdarkness.app.ui.theme.Ash
import com.towerofdarkness.app.ui.theme.Ember
import com.towerofdarkness.app.ui.theme.Steel

/**
 * Player portrait — circular soldier still ([R.drawable.portrait_you]).
 * v0.1.23-nobg: combat = PNG only on dark stage (no plate / fill / tint / ring).
 * Title may keep a teal circle via [showTitleCircle].
 * [rarity] kept for call-site compat; unused (frozen rarity systems).
 */
@Composable
fun HeroShowcase(
    @Suppress("UNUSED_PARAMETER") rarity: Rarity = Rarity.UNCOMMON,
    modifier: Modifier = Modifier,
    showTitleCircle: Boolean = false
) {
    val slot = BodyArt.PLAYER_SLOT_DP.dp
    Box(
        modifier.then(Modifier.size(slot)),
        contentAlignment = Alignment.Center
    ) {
        if (showTitleCircle && PortraitPlate.titleTealCircleAllowed()) {
            TitleTealCircle()
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
 * Title-only teal circle behind You (v0.1.23-nobg).
 * Static — no infinite pulse. Combat must not call this.
 */
@Composable
private fun TitleTealCircle() {
    val fill = Color(PortraitPlate.TITLE_CIRCLE_ARGB)
    Canvas(Modifier.fillMaxSize()) {
        val cx = size.width / 2
        val cy = size.height / 2
        drawCircle(fill, radius = size.minDimension * 0.5f, center = Offset(cx, cy))
    }
}

@DrawableRes
private fun bossPortraitResId(kind: EnemyKind): Int? = when (BodyArt.enemyPortraitDrawableName(kind)) {
    BodyArt.ASH_WARDEN_DRAWABLE -> R.drawable.portrait_ash_warden
    BodyArt.SEAL_WARDEN_DRAWABLE -> R.drawable.portrait_seal_warden
    else -> null
}

@DrawableRes
private fun packPortraitResId(look: EnemyLook?): Int? = when (look) {
    EnemyLook.KNIFE -> R.drawable.portrait_weak_goblin_knife
    EnemyLook.BOTTLE -> R.drawable.portrait_weak_goblin_bottle
    EnemyLook.SPIKES -> R.drawable.portrait_weak_goblin_spikes
    EnemyLook.AXE -> R.drawable.portrait_sturdy_orc_axe
    EnemyLook.CLEAVER -> R.drawable.portrait_sturdy_orc_cleaver
    EnemyLook.HAMMER -> R.drawable.portrait_sturdy_orc_hammer
    null -> null
}

/**
 * Enemy portrait slot (combat only).
 * Ash-Warden (F2) → [R.drawable.portrait_ash_warden] (+ volume bake).
 * Seal-Warden / [EnemyKind.DRAGON] (F1) → [R.drawable.portrait_seal_warden] (no volume).
 * Hallway packs (v0.1.26): look → portrait_weak_goblin_* / portrait_sturdy_orc_* (no plate).
 * v0.1.23-nobg: PNG = no plate / fill / tint / ring under still.
 */
@Composable
fun EnemySilhouette(
    kind: EnemyKind,
    isBoss: Boolean,
    modifier: Modifier = Modifier,
    look: EnemyLook? = null
) {
    val bossRes = bossPortraitResId(kind)
    val packRes = if (bossRes == null) packPortraitResId(look) else null
    val portraitRes: Int? = bossRes ?: packRes
    val slotDp = when {
        bossRes != null || isBoss -> BodyArt.BOSS_SLOT_DP
        else -> BodyArt.TRASH_SLOT_DP
    }.dp
    Box(modifier.then(Modifier.size(slotDp)), contentAlignment = Alignment.Center) {
        if (portraitRes != null && VolumeArt.appliesToEnemy(kind)) {
            val inset = 1f - BodyArt.SPRITE_INSET_FRACTION
            Box(
                Modifier
                    .fillMaxSize(inset)
                    .volumeChrome(circular = true)
            ) {
                Image(
                    painter = painterResource(portraitRes),
                    contentDescription = kind.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        } else if (portraitRes != null) {
            // Asset-backed boss without volume (Seal-Warden F1) — PNG only, no plate.
            // Pack looks also land here when Art PNGs are present.
            val inset = 1f - BodyArt.SPRITE_INSET_FRACTION
            Image(
                painter = painterResource(portraitRes),
                contentDescription = kind.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize(inset)
                    .clip(CircleShape)
            )
        } else {
            // Trash / Wretch / Seal Spinner — Canvas placeholders, no volume.
            // Silhouette shapes ARE the placeholder body (not a plate behind a PNG).
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
