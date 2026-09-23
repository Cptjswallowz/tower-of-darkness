package com.towerofdarkness.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.domain.cards.Card
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.components.AshbrandIcon
import com.towerofdarkness.app.ui.components.GlossaryText
import com.towerofdarkness.app.ui.theme.Accent
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Ember
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.GlowRare
import com.towerofdarkness.app.ui.theme.GlowUncommon
import com.towerofdarkness.app.ui.theme.Panel
import com.towerofdarkness.app.ui.theme.VoidBg

@Composable
fun LoadoutScreen(gc: GameController, tutorialMode: Boolean = false) {
    val pool = remember(gc.unlockedCards) { CardCatalog.poolForRun(gc.unlockedCards) }
    val selected = remember {
        mutableStateListOf<String>().also { list ->
            gc.loadout.forEach { list.add(it.id) }
            if (list.isEmpty() && tutorialMode) {
                CardCatalog.defaultLoadoutIds.forEach { list.add(it) }
            }
        }
    }
    val weaponId = remember {
        mutableStateOf(gc.equippedWeapon.def.id.ifBlank { WeaponCatalog.ashbrand.id })
    }
    val count = selected.size
    val ok = count == Balance.LOADOUT_MAX && weaponId.value.isNotBlank()

    Column(
        Modifier
            .then(if (tutorialMode) Modifier else Modifier.fillMaxSize().background(VoidBg))
            .padding(if (tutorialMode) 0.dp else 16.dp)
    ) {
        if (!tutorialMode) {
            Text("Loadout", color = Gold, fontSize = 22.sp)
            Text("Select 5 · $count selected", color = Bone.copy(0.7f))
            GlossaryText(
                "Weight (w) = how often a skill is picked while it is still live",
                listOf("loadout"),
                onTerm = { gc.showGlossary(it) },
                fontSizeSp = 12
            )
            Spacer(Modifier.height(8.dp))
        } else {
            Text("Select 5 · $count selected", color = Bone.copy(0.7f), fontSize = 13.sp)
        }
        LazyColumn(
            modifier = if (tutorialMode) Modifier.height(260.dp).fillMaxWidth()
            else Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(pool, key = { it.id }) { card ->
                val on = card.id in selected
                CardRow(card, on, onGlossary = { gc.showGlossary(it) }) {
                    if (on) selected.remove(card.id)
                    else if (selected.size < Balance.LOADOUT_MAX) selected.add(card.id)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Weapon (1)", color = Gold, fontSize = 14.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(WeaponCatalog.ashbrand).forEach { w ->
                val on = weaponId.value == w.id
                Row(
                    Modifier
                        .weight(1f)
                        .background(Panel, RoundedCornerShape(8.dp))
                        .border(2.dp, if (on) Ember else Bone.copy(0.3f), RoundedCornerShape(8.dp))
                        .clickable { weaponId.value = w.id }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Same Ashbrand icon slot size as combat weapon row
                    AshbrandIcon()
                    Column {
                        Text(w.title, color = Bone, fontSize = 13.sp)
                        Text("${w.statusTag} · ${w.abilityTitle}", color = Ember, fontSize = 11.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                val cards = selected.mapNotNull { CardCatalog.byId(it) }
                if (tutorialMode) gc.tutorialOnLoadoutConfirmed(cards)
                gc.confirmLoadout(cards, weaponId.value)
            },
            enabled = ok,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (ok) "Confirm" else "Pick 5 skills + weapon") }
    }
}

@Composable
private fun CardRow(card: Card, selected: Boolean, onGlossary: (String) -> Unit, onClick: () -> Unit) {
    val border = when (card.rarity) {
        Rarity.RARE -> GlowRare
        Rarity.UNCOMMON -> GlowUncommon
        else -> Bone.copy(0.3f)
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(Panel, RoundedCornerShape(8.dp))
            .border(2.dp, if (selected) Accent else border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text("${card.title}  ·  ${card.rarity.displayName}  w${card.weight}", color = Bone, fontSize = 14.sp)
            GlossaryText(card.effect.description, onTerm = onGlossary, fontSizeSp = 12)
        }
        Text(if (selected) "✓" else "+", color = Gold)
    }
}
