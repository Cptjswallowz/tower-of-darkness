package com.towerofdarkness.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.components.HeroShowcase
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.VoidBg

@Composable
fun MainMenuScreen(gc: GameController) {
    var confirmNewClimb by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(VoidBg).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("TOWER OF DARKNESS", color = Gold, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Ashen Host · fallen kingdom", color = Bone.copy(0.7f), fontSize = 13.sp)
        Spacer(Modifier.height(20.dp))
        // v0.1.23-nobg: title teal circle OK (combat has no plate)
        HeroShowcase(Rarity.RARE, Modifier.size(160.dp), showTitleCircle = true)
        Spacer(Modifier.height(28.dp))
        if (gc.hasMidRunSlot) {
            Button(
                onClick = { gc.continueClimb() },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) { Text("Continue") }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { confirmNewClimb = true },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) { Text("New climb") }
        } else {
            Button(
                onClick = { gc.climb() },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) { Text("Climb") }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { gc.goHub() },
            modifier = Modifier.fillMaxWidth(0.85f)
        ) { Text(com.towerofdarkness.app.domain.hub.HubOffers.titleBankLine(gc.remnantsBank)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { /* settings stub */ },
            modifier = Modifier.fillMaxWidth(0.85f)
        ) { Text("Settings (stub)") }
    }

    if (confirmNewClimb) {
        AlertDialog(
            onDismissRequest = { confirmNewClimb = false },
            title = { Text("Start a new climb?") },
            text = {
                Text(
                    "This wipes the mid-run save. Remnants bank and owned perks stay.",
                    color = Bone
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmNewClimb = false
                        gc.confirmNewClimb()
                    }
                ) { Text("New climb") }
            },
            dismissButton = {
                TextButton(onClick = { confirmNewClimb = false }) { Text("Cancel") }
            }
        )
    }
}
