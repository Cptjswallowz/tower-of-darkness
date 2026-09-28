package com.towerofdarkness.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.domain.art.SharedTilePlate
import com.towerofdarkness.app.domain.forge.Forge
import com.towerofdarkness.app.ui.components.SharedTilePlateBox
import com.towerofdarkness.app.ui.components.sharedTilePlateAvailable
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.Panel
import com.towerofdarkness.app.ui.theme.VoidBg

/**
 * Forge UI — Rest + Shop only. Exact chips from docs/forge-choices-v0158.md.
 * Cancel closes without spend. Corner pip II/III. Glossary = upgraded sentence.
 */
@Composable
fun ForgeSheet(gc: GameController) {
    val pickId = gc.forgePickCardId
    Column(
        Modifier
            .fillMaxSize()
            .background(VoidBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Forge", color = Gold, fontSize = 22.sp)
        Text(gc.scrapHudLineWithMax(), color = Bone, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        if (pickId == null) {
            val rows = Forge.rows(gc.loadout, gc.forgeStates, gc.scrapGoblin, gc.scrapOrc)
            rows.forEach { row ->
                val enabled = row.enabled
                val tint = if (enabled) Bone else Bone.copy(0.45f)
                val plateOn = sharedTilePlateAvailable()
                SharedTilePlateBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    cornerRadius = 8.dp
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (enabled) Gold.copy(0.5f) else Bone.copy(0.25f), RoundedCornerShape(8.dp))
                            .background(Panel.copy(alpha = if (plateOn) SharedTilePlate.PANEL_OVER_PLATE_ALPHA else 1f), RoundedCornerShape(8.dp))
                            .clickable(enabled = enabled) { gc.forgeSelectSkill(row.cardId) }
                            .padding(12.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(row.title, color = tint, fontSize = 16.sp)
                                Text(
                                    row.greyReason ?: (row.costLine ?: ""),
                                    color = tint.copy(0.75f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    gc.forgeGlossaryFor(row.cardId).replace("**", ""),
                                    color = tint.copy(0.65f),
                                    fontSize = 11.sp
                                )
                            }
                            // Forge list keeps I/II/III (existing)
                            Text(row.pip, color = Gold, fontSize = 18.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = { gc.closeForge() }, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        } else {
            val cardTitle = gc.loadout.find { it.id == pickId }?.title ?: pickId
            Text("Upgrade $cardTitle", color = Gold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            gc.forgeChoicesForSelected().forEach { choice ->
                Button(
                    onClick = { gc.forgeConfirmChoice(choice.branch) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column {
                        Text(choice.chip) // exact Architect chip
                        Text(
                            choice.glossaryBody.replace("**", ""),
                            fontSize = 11.sp,
                            color = Color.White.copy(0.85f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = { gc.forgeCancelPick() }, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}
