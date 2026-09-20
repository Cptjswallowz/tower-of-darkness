package com.towerofdarkness.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    Column(
        Modifier.fillMaxSize().background(VoidBg).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("TOWER OF DARKNESS", color = Gold, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Ashen Host · fallen kingdom", color = Bone.copy(0.7f), fontSize = 13.sp)
        Spacer(Modifier.height(20.dp))
        HeroShowcase(Rarity.RARE)
        Spacer(Modifier.height(28.dp))
        Button(onClick = { gc.climb() }) { Text("Climb") }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = { gc.goHub() }) { Text("Hub  ·  ${gc.remnantsBank} remnants") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { /* settings stub */ }) { Text("Settings (stub)") }
    }
}
