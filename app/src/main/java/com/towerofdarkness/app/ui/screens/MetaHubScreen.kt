package com.towerofdarkness.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.VoidBg

private data class HubPerk(val id: String, val cost: Int, val title: String, val ownedCheck: (GameController) -> Boolean)

@Composable
fun MetaHubScreen(gc: GameController) {
    val cardUnlocks = CardCatalog.hubUnlockables(gc.unlockedCards)
    val perks = listOf(
        HubPerk("meta_hp_2", 15, "meta_hp_2 · +2 max HP") { it.metaHpBonus > 0 },
        HubPerk("scout_charge", 20, "scout_charge · +1 free Scout / climb") { "scout_charge" in it.unlockedCards },
        HubPerk("rest_heal_plus", 25, "rest_heal_plus · Rest Heal +4") { "rest_heal_plus" in it.unlockedCards },
        HubPerk("boss_bonus_2", 25, "boss_bonus_2 · +2 rem on boss win") { "boss_bonus_2" in it.unlockedCards },
        HubPerk("loadout_flex", 30, "loadout_flex · prefer 6-card default") { "loadout_flex" in it.unlockedCards },
        HubPerk("rumor_clarity", 35, "rumor_clarity · 1 rumor re-roll / climb") { "rumor_clarity" in it.unlockedCards },
    )

    Column(
        Modifier.fillMaxSize().background(VoidBg).padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("Meta Hub", color = Gold, fontSize = 22.sp)
        Text("Bank: ${gc.remnantsBank} remnants", color = Bone)
        Spacer(Modifier.height(12.dp))
        Text(
            "Ladder · cheapest card unlock = ${Balance.CHEAPEST_CARD_UNLOCK}",
            color = Bone.copy(0.7f), fontSize = 12.sp
        )
        Spacer(Modifier.height(8.dp))

        Text("Cards", color = Gold, fontSize = 14.sp)
        if (cardUnlocks.isEmpty()) {
            Text("Shadow Latch & Relic Shard owned (or none left).", color = Bone.copy(0.7f), fontSize = 12.sp)
        } else {
            cardUnlocks.forEach { card ->
                val cost = card.unlockCost.coerceAtLeast(Balance.CHEAPEST_CARD_UNLOCK)
                val can = gc.remnantsBank >= cost
                Button(
                    onClick = { gc.hubUnlock(card.id) },
                    enabled = can,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(
                        if (can) "Unlock ${card.title} · $cost"
                        else "Unlock ${card.title} · $cost (need ${cost - gc.remnantsBank} more)"
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Perks", color = Gold, fontSize = 14.sp)
        perks.forEach { perk ->
            if (perk.ownedCheck(gc)) {
                Text("${perk.id} · owned", color = Bone.copy(0.7f), fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
            } else if (perk.id == "meta_hp_2") {
                val can = gc.remnantsBank >= perk.cost
                Button(
                    onClick = { gc.hubUnlockMetaHp() },
                    enabled = can,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(if (can) "${perk.title} · ${perk.cost}" else "${perk.title} · ${perk.cost} (need ${perk.cost - gc.remnantsBank} more)")
                }
            } else {
                val can = gc.remnantsBank >= perk.cost
                Button(
                    onClick = { gc.hubUnlockPerk(perk.id, perk.cost) },
                    enabled = can,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(if (can) "${perk.title} · ${perk.cost}" else "${perk.title} · ${perk.cost} (need ${perk.cost - gc.remnantsBank} more)")
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = { gc.climb() }, modifier = Modifier.fillMaxWidth()) { Text("Climb") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { gc.goMenu() }, modifier = Modifier.fillMaxWidth()) { Text("Menu") }
    }
}
