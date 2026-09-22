package com.towerofdarkness.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.VoidBg

@Composable
fun FloorBreakScreen(gc: GameController) {
    Column(
        Modifier
            .fillMaxSize()
            .background(VoidBg)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("The stair turns.", color = Gold, fontSize = 22.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "HP ${gc.playerHp} · ${gc.runWallet} rem · Ashbrand Lv${gc.equippedWeapon.level}",
            color = Bone.copy(0.7f),
            fontSize = 13.sp
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { gc.continueAfterFloorBreak() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}
