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
fun RunSummaryScreen(gc: GameController) {
    val s = gc.summary
    Column(Modifier.fillMaxSize().background(VoidBg).padding(16.dp)) {
        Text("Run Summary", color = Gold, fontSize = 22.sp)
        Spacer(Modifier.height(12.dp))
        if (s != null) {
            Text(if (s.won) "Victory" else "Defeat", color = Bone, fontSize = 18.sp)
            Text("Nodes cleared: ${s.nodesCleared}", color = Bone)
            Text("Floor: ${s.floorReached}", color = Bone)
            Text("Remnants earned: ${s.remnantsEarned}", color = Bone)
            Text("Banked total: ${gc.remnantsBank}", color = Bone)
            if (!s.won) {
                Text(
                    if (s.nearMiss) "Near-miss — so close." else "The climb ends.",
                    color = Gold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = { gc.goHub() }, modifier = Modifier.fillMaxWidth()) { Text("Hub") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { gc.goMenu() }, modifier = Modifier.fillMaxWidth()) { Text("Menu") }
    }
}
