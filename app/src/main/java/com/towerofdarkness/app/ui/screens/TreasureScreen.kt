package com.towerofdarkness.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.towerofdarkness.app.ui.theme.Ember
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.Moss
import com.towerofdarkness.app.ui.theme.Panel
import com.towerofdarkness.app.ui.theme.VoidBg

@Composable
fun TreasureScreen(gc: GameController) {
    val lose = gc.treasureSwapLoseId?.let { CardCatalog.byId(it) }
    val gain = gc.treasureSwapGainId?.let { CardCatalog.byId(it) }
    val previewing = lose != null && gain != null

    Column(
        Modifier
            .fillMaxSize()
            .background(VoidBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Treasure", color = Gold, fontSize = 22.sp)
        Text("A coffer that forgot its owner.", color = Bone)
        Spacer(Modifier.height(16.dp))

        if (!previewing) {
            Button(onClick = { gc.treasureRemnants() }, modifier = Modifier.fillMaxWidth()) {
                Text("Take remnants (+${Balance.TREASURE_REMNANTS})")
            }
            Spacer(Modifier.height(12.dp))
            Text("Or swap a loadout card — tap one to preview:", color = Bone.copy(0.75f), fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            gc.loadout.forEach { card ->
                Text(
                    card.title,
                    color = Bone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Panel, RoundedCornerShape(8.dp))
                        .clickable { gc.treasureBeginSwap(card.id) }
                        .padding(12.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { gc.treasureBeginSwap(null) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Swap random card (preview)") }
        } else {
            Text("Confirm swap?", color = Gold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, Ember, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text("Lose", color = Ember, fontSize = 12.sp)
                Text(lose!!.title, color = Bone, fontSize = 16.sp)
                Text(lose.effect.description, color = Bone.copy(0.7f), fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, Moss, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text("Gain", color = Moss, fontSize = 12.sp)
                Text(gain!!.title, color = Bone, fontSize = 16.sp)
                Text(gain.effect.description, color = Bone.copy(0.7f), fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { gc.treasureConfirmSwap() }, modifier = Modifier.fillMaxWidth()) {
                Text("Confirm swap")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { gc.treasureCancelSwap() }, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel — keep ${lose.title}")
            }
        }
    }
}
