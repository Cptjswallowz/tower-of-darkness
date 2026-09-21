package com.towerofdarkness.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.VoidBg

@Composable
fun TutorialScreen(gc: GameController) {
    Column(Modifier.fillMaxSize().background(VoidBg).padding(20.dp)) {
        Text("Tutorial", color = Gold, fontSize = 22.sp)
        Spacer(Modifier.height(12.dp))
        when (gc.tutorialStep) {
            0 -> {
                Text("Rumor fog", color = Bone, fontSize = 18.sp)
                Text(
                    "Unrevealed path nodes show vague rumors — never exact stats.\n\n“Steel answers steel beyond the fog.”",
                    color = Bone.copy(0.85f), modifier = Modifier.padding(vertical = 12.dp)
                )
                Button(onClick = { gc.tutorialNext() }) { Text("Next") }
            }
            1 -> {
                Text("Path nodes", color = Bone, fontSize = 18.sp)
                Text(
                    "Icons: fight · shop · rest · event · treasure · boss.\nBranch, then merge into the seal-warden.",
                    color = Bone.copy(0.85f), modifier = Modifier.padding(vertical = 12.dp)
                )
                Button(onClick = { gc.tutorialNext() }) { Text("Next — Loadout") }
            }
            2 -> {
                Text("Loadout: pick 5 skills + Ashbrand", color = Bone, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                LoadoutScreen(gc, tutorialMode = true)
            }
            else -> {
                Text("Dice auto-combat", color = Bone, fontSize = 18.sp)
                Text(
                    "Cards fire by weight when dice roll. You watch — no taps mid-fight.\nBrace absorbs before HP.",
                    color = Bone.copy(0.85f), modifier = Modifier.padding(vertical = 12.dp)
                )
                Button(onClick = { gc.completeTutorial() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Enter the Tower")
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (gc.skipAllowed()) {
            OutlinedButton(onClick = { gc.skipTutorial() }, modifier = Modifier.fillMaxWidth()) {
                Text("Skip (default 5 + Ashbrand)")
            }
        } else {
            Text("Skip unlocks after rumor + loadout.", color = Bone.copy(0.5f), fontSize = 12.sp)
        }
    }
}
