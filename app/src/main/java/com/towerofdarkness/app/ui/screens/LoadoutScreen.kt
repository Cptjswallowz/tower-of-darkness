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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.domain.cards.Card
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.theme.Accent
import com.towerofdarkness.app.ui.theme.Bone
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
            if (list.isEmpty()) {
                if (tutorialMode) {
                    CardCatalog.defaultLoadoutIds.forEach { list.add(it) }
                } else if (gc.hasLoadoutFlex()) {
                    // Prefer 6-card default: cards 1–5 + next unlocked pool card
                    CardCatalog.defaultLoadoutIds.forEach { list.add(it) }
                    val sixth = CardCatalog.poolForRun(gc.unlockedCards)
                        .map { it.id }
                        .firstOrNull { it !in list }
                    if (sixth != null) list.add(sixth)
                }
            }
        }
    }
    val count = selected.size
    val ok = count in Balance.LOADOUT_MIN..Balance.LOADOUT_MAX

    Column(
        Modifier
            .then(if (tutorialMode) Modifier else Modifier.fillMaxSize().background(VoidBg))
            .padding(if (tutorialMode) 0.dp else 16.dp)
    ) {
        if (!tutorialMode) {
            Text("Loadout", color = Gold, fontSize = 22.sp)
            Text("Select ${Balance.LOADOUT_MIN}–${Balance.LOADOUT_MAX} · $count selected", color = Bone.copy(0.7f))
            Spacer(Modifier.height(8.dp))
        }
        LazyColumn(
            modifier = if (tutorialMode) Modifier.height(320.dp).fillMaxWidth()
            else Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(pool, key = { it.id }) { card ->
                val on = card.id in selected
                CardRow(card, on) {
                    if (on) selected.remove(card.id)
                    else if (selected.size < Balance.LOADOUT_MAX) selected.add(card.id)
                }
            }
        }
        Button(
            onClick = {
                val cards = selected.mapNotNull { CardCatalog.byId(it) }
                if (tutorialMode) gc.tutorialOnLoadoutConfirmed(cards)
                gc.confirmLoadout(cards)
            },
            enabled = ok,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (ok) "Confirm" else "Pick ${Balance.LOADOUT_MIN}–${Balance.LOADOUT_MAX}") }
    }
}

@Composable
private fun CardRow(card: Card, selected: Boolean, onClick: () -> Unit) {
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
            Text(card.effect.description, color = Bone.copy(0.65f), fontSize = 12.sp)
        }
        Text(if (selected) "✓" else "+", color = Gold)
    }
}
