package com.towerofdarkness.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.domain.hub.HubCta
import com.towerofdarkness.app.domain.hub.HubOffers
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.VoidBg

/**
 * Hub remnant shop — v0.1.27-hub.
 * Exactly 4 offers; Buy / OWNED / Can't afford. Old perk/card ladder hidden.
 */
@Composable
fun MetaHubScreen(gc: GameController) {
    Column(
        Modifier.fillMaxSize().background(VoidBg).padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text(HubOffers.hubScreenTitle(), color = Gold, fontSize = 22.sp)
        Text(HubOffers.hubBankLine(gc.remnantsBank), color = Bone)
        Spacer(Modifier.height(16.dp))

        HubOffers.all.forEach { offer ->
            val cta = HubOffers.cta(offer, gc.remnantsBank, gc.unlockedCards)
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(offer.title, color = Gold, fontSize = 16.sp)
                Text(offer.effectLine, color = Bone.copy(0.85f), fontSize = 13.sp)
                Text("Cost ${offer.cost}", color = Bone.copy(0.65f), fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (cta) {
                        HubCta.BUY -> Button(
                            onClick = { gc.hubBuyOffer(offer.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(HubOffers.ctaLabel(cta)) }
                        HubCta.OWNED -> Text(
                            HubOffers.ctaLabel(cta),
                            color = Bone.copy(0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        HubCta.CANT_AFFORD -> OutlinedButton(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(HubOffers.ctaLabel(cta)) }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = { gc.goMenu() }, modifier = Modifier.fillMaxWidth()) {
            Text("Back to title")
        }
    }
}
