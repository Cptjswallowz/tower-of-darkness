package com.towerofdarkness.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.domain.hub.HubCta
import com.towerofdarkness.app.domain.hub.HubOffer
import com.towerofdarkness.app.domain.hub.HubOffers
import com.towerofdarkness.app.domain.hub.HubRelics
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.components.HeroShowcase
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.VoidBg

/**
 * Hub — v0.1.55-hubsplit PART B.
 * Sticky header: bust + Remnants N. Sections Relics → Perks → Skills.
 * Relics = owned trophies only (display, no Buy). Perks/Skills CTAs unchanged.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MetaHubScreen(gc: GameController) {
    val ownedRelics = HubRelics.ownedRows(gc.unlockedCards)
    val perkOffers = HubRelics.perkOffers()
    val skillOffers = HubRelics.skillOffers()

    Column(
        Modifier.fillMaxSize().background(VoidBg)
    ) {
        // Sticky header: bust + Remnants N (visible while sections scroll)
        Row(
            Modifier
                .fillMaxWidth()
                .background(VoidBg)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroShowcase(
                rarity = Rarity.RARE,
                modifier = Modifier.size(72.dp),
                trophyUnlocks = gc.unlockedCards
            )
            Spacer(Modifier.size(12.dp))
            Column {
                Text(HubOffers.hubScreenTitle(), color = Gold, fontSize = 20.sp)
                Text(HubOffers.hubBankLine(gc.remnantsBank), color = Bone, fontSize = 15.sp)
            }
        }

        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            stickyHeader {
                HubSectionHeader(HubRelics.SECTION_RELICS)
            }
            if (ownedRelics.isEmpty()) {
                item {
                    Text(
                        HubRelics.EMPTY_COPY,
                        color = Bone.copy(0.75f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(ownedRelics, key = { it.id }) { relic ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(relic.name, color = Gold, fontSize = 16.sp)
                        Text(relic.earnLine, color = Bone.copy(0.85f), fontSize = 13.sp)
                    }
                }
            }

            stickyHeader {
                HubSectionHeader(HubRelics.SECTION_PERKS)
            }
            items(perkOffers, key = { it.id }) { offer ->
                HubBuyableRow(gc, offer)
            }

            stickyHeader {
                HubSectionHeader(HubRelics.SECTION_SKILLS)
            }
            items(skillOffers, key = { it.id }) { offer ->
                HubBuyableRow(gc, offer)
            }

            item {
                Spacer(Modifier.height(20.dp))
                OutlinedButton(onClick = { gc.goMenu() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to title")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HubSectionHeader(title: String) {
    Text(
        title,
        color = Gold,
        fontSize = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(VoidBg)
            .padding(vertical = 10.dp)
    )
}

@Composable
private fun HubBuyableRow(gc: GameController, offer: HubOffer) {
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
}
