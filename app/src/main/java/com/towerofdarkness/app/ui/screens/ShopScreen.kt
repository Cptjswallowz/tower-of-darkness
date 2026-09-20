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
fun ShopScreen(gc: GameController) {
    val maxHp = Balance.PLAYER_MAX_HP + gc.metaHpBonus
    Column(Modifier.fillMaxSize().background(VoidBg).padding(16.dp)) {
        Text("Shop", color = Gold, fontSize = 22.sp)
        Text(
            "HP ${gc.playerHp} / $maxHp · ${gc.runWallet} rem",
            color = Bone,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(12.dp))
        gc.shopOffers.forEach { offer ->
            Button(
                onClick = { gc.buyOffer(offer) },
                enabled = !offer.sold && gc.runWallet >= offer.price,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(
                    if (offer.sold) "${offer.title} — SOLD"
                    else "${offer.title} · ${offer.price} rem"
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = { gc.leaveShop() }, modifier = Modifier.fillMaxWidth()) {
            Text("Leave")
        }
    }
}
