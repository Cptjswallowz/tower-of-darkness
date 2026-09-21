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
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.VoidBg

@Composable
fun EventScreen(gc: GameController) {
    Column(Modifier.fillMaxSize().background(VoidBg).padding(16.dp)) {
        Text("Event", color = Gold, fontSize = 22.sp)
        Text(
            "A chalk question on the wall: take the ash-purse, or risk the niche?",
            color = Bone, modifier = Modifier.padding(vertical = 12.dp)
        )
        Button(onClick = { gc.eventChoice(remnants = true) }, modifier = Modifier.fillMaxWidth()) {
            Text("A — Take remnants (+3)")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { gc.eventChoice(remnants = false) }, modifier = Modifier.fillMaxWidth()) {
            Text("B — Heal 8, or take 4 damage")
        }
    }
}
