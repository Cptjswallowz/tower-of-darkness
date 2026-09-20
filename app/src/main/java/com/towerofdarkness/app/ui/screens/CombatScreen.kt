package com.towerofdarkness.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.domain.combat.CombatAnimStyle
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.components.EnemySilhouette
import com.towerofdarkness.app.ui.components.HeroShowcase
import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Ember
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.Moss
import com.towerofdarkness.app.ui.theme.VoidBg
import kotlinx.coroutines.delay

@Composable
fun CombatScreen(gc: GameController) {
    val state = gc.combatState
    val shake = remember { Animatable(0f) }
    var floatMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state?.log?.size) {
        val last = state?.log?.lastOrNull() ?: return@LaunchedEffect
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
            .graphicsLayer { translationX = shake.value }
            .padding(16.dp)
    ) {
        if (state == null) {
            Text("No combat.", color = Bone)
            return
        }
        Text(
            if (state.enemy.isBoss) "BOSS · ${state.enemy.kind.displayName}" else state.enemy.kind.displayName,
            color = if (state.enemy.isBoss) Ember else Gold,
            fontSize = 20.sp
        )
        Text("Round ${state.round}", color = Bone.copy(0.6f), fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HeroShowcase(state.lastFiredCard?.rarity ?: Rarity.COMMON, Modifier.height(100.dp))
                Text("You", color = Bone, fontSize = 12.sp)
                HpBar(state.playerHp, state.playerMaxHp, Moss)
                if (state.brace > 0) Text("Brace ${state.brace}", color = Gold, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EnemySilhouette(state.enemy.kind.displayName, state.enemy.isBoss)
                Text(state.enemy.kind.displayName, color = Bone, fontSize = 12.sp)
                HpBar(state.enemy.hp, state.enemy.maxHp, Ember)
            }
        }

        Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
            floatMsg?.let { Text(it, color = Gold, fontSize = 22.sp) }
        }

        Text(
            "Fired: ${state.lastFiredCard?.title ?: "—"}",
            color = Bone, fontSize = 14.sp
        )
        Spacer(Modifier.height(8.dp))
        Column(Modifier.weight(1f)) {
            state.log.takeLast(6).forEach {
                Text(it.message, color = Bone.copy(0.75f), fontSize = 12.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {}, enabled = false) { Text("Flee (locked)") }
            if (state.finished) {
                Text(if (state.playerWon) "Victory" else "Defeat", color = Gold)
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
