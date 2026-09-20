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
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.VoidBg

@Composable
fun TreasureScreen(gc: GameController) {
    Column(Modifier.fillMaxSize().background(VoidBg).padding(16.dp)) {
        Text("Treasure", color = Gold, fontSize = 22.sp)
        Text("A coffer that forgot its owner.", color = Bone)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { gc.treasureRemnants() }, modifier = Modifier.fillMaxWidth()) {
            Text("Take remnants (+${Balance.TREASURE_REMNANTS})")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { gc.treasureCardSwap() }, modifier = Modifier.fillMaxWidth()) {
            Text("Swap a card (may unlock Rare)")
        }
    }
}
