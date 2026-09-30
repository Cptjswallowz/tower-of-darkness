package com.towerofdarkness.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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

/**
 * v0.1.64-specials — Settings stub title OK; Assist specials row must work.
 * Preference persists via MetaStore; combat snapshots at fight start only.
 */
@Composable
fun SettingsScreen(gc: GameController) {
    Column(
        Modifier.fillMaxSize().background(VoidBg).padding(24.dp)
    ) {
        Text("Settings", color = Gold, fontSize = 22.sp)
        Text("(stub)", color = Bone.copy(0.5f), fontSize = 12.sp)
        Spacer(Modifier.height(24.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Assist specials", color = Bone, fontSize = 16.sp)
                Text(
                    "Wake / Grave Brand / Ash Vow auto when ON",
                    color = Bone.copy(0.65f),
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = gc.assistSpecialsPref,
                onCheckedChange = { gc.setAssistSpecials(it) }
            )
        }
        Text(
            if (gc.assistSpecialsPref) "ON" else "OFF",
            color = Gold,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.weight(1f))
        OutlinedButton(
            onClick = { gc.goMenu() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Back") }
    }
}
