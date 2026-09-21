package com.towerofdarkness.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.domain.cards.Card
import com.towerofdarkness.app.domain.combat.CombatAnimStyle
import com.towerofdarkness.app.domain.combat.CombatBeat
import com.towerofdarkness.app.domain.combat.WeaponTag
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.components.EnemySilhouette
import com.towerofdarkness.app.ui.components.GlossaryText
import com.towerofdarkness.app.ui.components.HeroShowcase
import com.towerofdarkness.app.ui.theme.Accent
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Ember
import com.towerofdarkness.app.ui.theme.GlowRare
import com.towerofdarkness.app.ui.theme.GlowUncommon
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.Moss
import com.towerofdarkness.app.ui.theme.Panel
import com.towerofdarkness.app.ui.theme.Steel
import com.towerofdarkness.app.ui.theme.VoidBg
import kotlinx.coroutines.delay

@Composable
fun CombatScreen(gc: GameController) {
    val state = gc.combatState
    val shake = remember { Animatable(0f) }
    var floatMsg by remember { mutableStateOf<String?>(null) }
    var diceShake by remember { mutableStateOf(0f) }

    LaunchedEffect(state?.log?.size) {
        val last = state?.log?.lastOrNull() ?: return@LaunchedEffect
        if (last.message.contains("Dice tumble")) {
            repeat(8) {
                diceShake = if (it % 2 == 0) 4f else -4f
                delay(40)
            }
            diceShake = 0f
        }
        last.floating?.let {
            floatMsg = it.text
            delay(700)
            floatMsg = null
        }
        if (last.animStyle == CombatAnimStyle.CHARGE_SHAKE_SLOWMO) {
            repeat(6) {
                shake.snapTo(if (it % 2 == 0) 8f else -8f)
                delay(40)
            }
            shake.snapTo(0f)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(VoidBg)
            .graphicsLayer { translationX = shake.value + diceShake }
            .padding(16.dp)
    ) {
        if (state == null) {
            Text("No combat.", color = Bone)
            return
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                if (state.enemy.isBoss) "BOSS · ${state.enemy.kind.displayName}" else state.enemy.kind.displayName,
                color = if (state.enemy.isBoss) Ember else Gold,
                fontSize = 18.sp
            )
            // 1x stub — disabled, no 2x
            OutlinedButton(onClick = {}, enabled = false) { Text("1x") }
        }
        Text("Round ${state.round}", color = Bone.copy(0.6f), fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HeroShowcase(state.lastFiredCard?.rarity ?: Rarity.COMMON, Modifier.height(90.dp))
                Text("You", color = Bone, fontSize = 12.sp)
                HpBar(state.playerHp, state.playerMaxHp, Moss)
                if (state.brace > 0) {
                    GlossaryText("Brace ${state.brace}", listOf("brace"), onTerm = { gc.showGlossary(it) }, fontSizeSp = 11)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EnemySilhouette(state.enemy.kind.displayName, state.enemy.isBoss)
                Text(state.enemy.kind.displayName, color = Bone, fontSize = 12.sp)
                HpBar(state.enemy.hp, state.enemy.maxHp, Ember)
            }
        }

        Box(Modifier.fillMaxWidth().height(36.dp), contentAlignment = Alignment.Center) {
            floatMsg?.let { Text(it, color = Gold, fontSize = 22.sp) }
        }

        // 5 skill slots
        Text("Skills", color = Bone.copy(0.7f), fontSize = 11.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            state.activeCards.take(5).forEach { card ->
                SkillSlot(
                    card = card,
                    spent = card.id in state.spentIds,
                    current = card.id == state.highlightedId,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))

        // Weapon row (not in dice)
        WeaponBar(state.weapon, flashed = state.weaponFlashed)

        Spacer(Modifier.height(8.dp))
        Column(Modifier.weight(1f)) {
            // Pin FULL Wake line so it stays visible for the Wake hold beat
            val recent = state.log.takeLast(5).toMutableList()
            state.pinnedWakeLine?.let { pin ->
                if (recent.none { it.message == pin }) {
                    recent.add(0, com.towerofdarkness.app.domain.combat.CombatEvent(pin, goldLog = true))
                    while (recent.size > 5) recent.removeAt(0)
                }
            }
            recent.forEachIndexed { idx, ev ->
                val latest = idx == recent.lastIndex
                val color = when {
                    ev.goldLog || ev.message.startsWith("ASHBRAND — WAKE") -> Gold
                    latest -> Gold
                    else -> Bone.copy(0.85f)
                }
                GlossaryText(
                    text = ev.message,
                    highlights = (ev.glossaryHints + listOf("brace", "stun", "freeze")).distinct(),
                    onTerm = { gc.showGlossary(it) },
                    color = color,
                    fontSizeSp = if (ev.goldLog || latest) 17 else 14
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) {
                Text("Flee (locked)")
            }
        }
        if (state.finished || state.beat == CombatBeat.AWAITING_CONTINUE) {
            Spacer(Modifier.height(8.dp))
            Text(
                if (state.playerWon) "Victory" else "Defeat",
                color = Gold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { gc.continueAfterCombat() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Continue") }
        }
    }
}

@Composable
private fun SkillSlot(card: Card, spent: Boolean, current: Boolean, modifier: Modifier = Modifier) {
    val border = when {
        current -> Gold
        spent -> Steel.copy(0.3f)
        card.rarity == Rarity.RARE -> GlowRare
        card.rarity == Rarity.UNCOMMON -> GlowUncommon
        else -> Bone.copy(0.35f)
    }
    Column(
        modifier
            .height(56.dp)
            .alpha(if (spent && !current) 0.35f else 1f)
            .background(Panel, RoundedCornerShape(6.dp))
            .border(if (current) 2.dp else 1.dp, border, RoundedCornerShape(6.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(card.title, color = Bone, fontSize = 9.sp, maxLines = 2)
        Text("w${card.weight}", color = Bone.copy(0.5f), fontSize = 8.sp)
    }
}

@Composable
private fun WeaponBar(weapon: com.towerofdarkness.app.domain.combat.WeaponRuntime, flashed: Boolean) {
    val tagColor = when (weapon.def.statusTag) {
        WeaponTag.Ember -> Ember
        WeaponTag.Notch -> Accent
        WeaponTag.Guard -> Moss
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(Panel, RoundedCornerShape(8.dp))
            .border(if (flashed) 2.dp else 1.dp, if (flashed) Gold else tagColor, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text("${weapon.def.title}  Lv${weapon.level}", color = Bone, fontSize = 13.sp)
            Text("${weapon.def.statusTag} · ${weapon.def.abilityTitle}", color = tagColor, fontSize = 10.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(weapon.threshold) { i ->
                Box(
                    Modifier
                        .size(10.dp)
                        .background(
                            if (i < weapon.pipsFilled) tagColor else Steel.copy(0.4f),
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun HpBar(hp: Int, max: Int, color: androidx.compose.ui.graphics.Color) {
    val f = if (max <= 0) 0f else hp.toFloat() / max
    Column(Modifier.padding(top = 4.dp)) {
        LinearProgressIndicator(progress = { f }, modifier = Modifier.fillMaxWidth(0.4f).height(8.dp), color = color)
        Text("$hp / $max", color = Bone, fontSize = 11.sp)
    }
}
