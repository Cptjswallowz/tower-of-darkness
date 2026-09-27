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
import com.towerofdarkness.app.domain.climb.ClimbKept
import com.towerofdarkness.app.domain.combat.BodyArt
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.EnemyLook
import com.towerofdarkness.app.domain.combat.PortraitPlate
import com.towerofdarkness.app.domain.volume.VolumeArt
import com.towerofdarkness.app.ui.theme.Ash
import com.towerofdarkness.app.ui.theme.Ember
import com.towerofdarkness.app.ui.theme.Steel

/**
 * Shared title + Hub You bust: portrait_you + trophy overlays (stack order).
 * Overlay stack: soot_rim → ash_pauldron → troll_tooth → gate_sigil.
 * Clipped to CircleShape with the portrait crop — no cyan/teal frame, no glow/pulse.
 * Combat must pass empty [trophyUnlocks] (no overlays).
 */
@Composable
fun YouPortraitComposite(
    modifier: Modifier = Modifier,
    trophyUnlocks: Set<String> = emptySet()
) {
    val overlays = ClimbKept.unlockedOverlayDrawables(trophyUnlocks)
    Box(modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.portrait_you),
            contentDescription = "You",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
        // Trophy overlays — same 256 bounds / crop; edge embers stay inside circle clip.
        overlays.forEach { name ->
            val res = trophyOverlayRes(name)
            if (res != 0) {
                Image(
                    painter = painterResource(res),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        }
    }
}

/**
 * Player portrait — circular soldier still ([R.drawable.portrait_you]).
 * v0.1.23-nobg: combat = PNG only on dark stage (no plate / fill / tint / ring).
 * v0.1.55-hubsplit: title teal circle OFF; title + Hub share [YouPortraitComposite].
 * Combat omits trophies (empty [trophyUnlocks]).
 */
@Composable
fun HeroShowcase(
    @Suppress("UNUSED_PARAMETER") rarity: Rarity = Rarity.UNCOMMON,
    modifier: Modifier = Modifier,
    showTitleCircle: Boolean = false,
    /** Meta unlocks; only Hub/title pass trophies. Combat leaves empty. */
    trophyUnlocks: Set<String> = emptySet()
) {
    val slot = BodyArt.PLAYER_SLOT_DP.dp
    Box(
        modifier.then(Modifier.size(slot)),
        contentAlignment = Alignment.Center
    ) {
        // Gated off by PortraitPlate.TITLE_TEAL_CIRCLE_ALLOWED = false (hubsplit).
        if (showTitleCircle && PortraitPlate.titleTealCircleAllowed()) {
            TitleTealCircle()
        }
        val inset = 1f - BodyArt.SPRITE_INSET_FRACTION
        Box(
            Modifier
                .fillMaxSize(inset)
                .then(if (VolumeArt.appliesToPlayerPortrait()) Modifier.volumeChrome(circular = true) else Modifier)
        ) {
            YouPortraitComposite(
                modifier = Modifier.fillMaxSize(),
                trophyUnlocks = trophyUnlocks
            )
        }
    }
}

@DrawableRes
private fun trophyOverlayRes(drawableName: String): Int = when (drawableName) {
    "overlay_soot_rim" -> R.drawable.overlay_soot_rim
    "overlay_ash_pauldron" -> R.drawable.overlay_ash_pauldron
    "overlay_troll_tooth" -> R.drawable.overlay_troll_tooth
    "overlay_gate_sigil" -> R.drawable.overlay_gate_sigil
    else -> 0
}

/**
 * Title-only teal circle (legacy v0.1.23-nobg).
 * Disabled when [PortraitPlate.TITLE_TEAL_CIRCLE_ALLOWED] is false (v0.1.55-hubsplit).
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
    BodyArt.GATE_WARDEN_DRAWABLE -> R.drawable.portrait_gate_warden
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
    EnemyLook.CAVE_TROLL_A -> R.drawable.portrait_cave_troll_a
    EnemyLook.CAVE_TROLL_B -> R.drawable.portrait_cave_troll_b
    EnemyLook.CAVE_TROLL_C -> R.drawable.portrait_cave_troll_c
    null -> null
}

/**
 * Enemy portrait slot (combat only).
 * Ash-Warden (F2) → [R.drawable.portrait_ash_warden] (+ volume bake).
 * Seal-Warden / [EnemyKind.DRAGON] (F1) → [R.drawable.portrait_seal_warden] (no volume).
 * Gate-Warden (F3) → [R.drawable.portrait_gate_warden] (Seal + colder copper; no volume).
 * Hallway packs: goblin/orc looks + Cave Troll A/B/C (no plate).
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
