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
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.components.GlossaryText
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.VoidBg

@Composable
fun RestScreen(gc: GameController) {
    val healAmt = gc.restHealAmount()
    Column(Modifier.fillMaxSize().background(VoidBg).padding(16.dp)) {
        Text("Rest", color = Gold, fontSize = 22.sp)
        Text("HP ${gc.playerHp}", color = Bone)
        Spacer(Modifier.height(8.dp))
        GlossaryText(
            "Choose Heal or Scout, or Leave. Scout reveals an adjacent node's type only.",
            listOf("scout"),
            onTerm = { gc.showGlossary(it) }
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = { gc.restHeal() }, modifier = Modifier.fillMaxWidth()) {
            Text("Heal (+$healAmt)")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { gc.restScout() }, modifier = Modifier.fillMaxWidth()) {
            Text("Scout (type only)")
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = { gc.leaveRest() }, modifier = Modifier.fillMaxWidth()) {
            Text("Leave")
        }
    }
}
