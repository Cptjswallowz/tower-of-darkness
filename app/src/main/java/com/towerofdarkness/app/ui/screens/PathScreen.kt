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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.domain.path.NodeType
import com.towerofdarkness.app.domain.path.PathNode
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.theme.Accent
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Ember
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.Moss
import com.towerofdarkness.app.ui.theme.Panel
import com.towerofdarkness.app.ui.theme.Steel
import com.towerofdarkness.app.ui.theme.VoidBg

@Composable
fun PathScreen(gc: GameController) {
    val path = gc.path
    Column(Modifier.fillMaxSize().background(VoidBg).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Floor ${path?.floor ?: 1}", color = Gold, fontSize = 20.sp)
            Text("HP ${gc.playerHp} · ${gc.runWallet} rem", color = Bone, fontSize = 13.sp)
        }
        Text(
            if (gc.loadoutLocked) "Loadout locked" else "Loadout editable",
            color = Bone.copy(0.6f), fontSize = 12.sp
        )
        Spacer(Modifier.height(8.dp))
        if (!gc.loadoutLocked) {
            OutlinedButton(onClick = { gc.openLoadout() }) {
                Text(
                    if (gc.loadout.isEmpty()) "Set Loadout (required before nodes)"
                    else "Edit Loadout (${gc.loadout.size})"
                )
            }
        }
        if (gc.hasPerk("scout_charge") && gc.freeScoutCharges > 0) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Free Scout · charges ${gc.freeScoutCharges} (tap a fogged ? node)",
                color = Bone.copy(0.7f),
                fontSize = 12.sp
            )
        }
        // Always show when Clear Fog unlocked so counter can report 0 (never stuck at "1")
        if (gc.hasPerk("rumor_clarity")) {
            Spacer(Modifier.height(6.dp))
            val rumorHint = if (gc.rumorRerolls > 0) " (tap a fogged ? node)" else ""
            Text(
                "${GameController.rumorRerollsLabel(gc.rumorRerolls)}$rumorHint",
                color = Bone.copy(0.55f),
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        if (path == null) {
            Text("No path.", color = Bone)
            Button(onClick = { gc.startNewRun() }) { Text("Generate") }
            return
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            val rows = path.nodes.groupBy { it.row }.toSortedMap()
            rows.forEach { (row, nodes) ->
                Text("— tier $row —", color = Bone.copy(0.4f), fontSize = 11.sp)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    nodes.sortedBy { it.col }.forEach { node ->
                        PathNodeChip(
                            node = node,
                            currentId = path.currentId,
                            choiceIds = path.choices().map { it.id }.toSet(),
                            canReroll = gc.rumorRerolls > 0 &&
                                !node.revealed && !node.scoutedTypeOnly &&
                                node.type != NodeType.START && node.type != NodeType.BOSS,
                            canFoggedAct = (gc.freeScoutCharges > 0 || gc.rumorRerolls > 0) &&
                                !node.revealed && !node.scoutedTypeOnly &&
                                node.type != NodeType.START && node.type != NodeType.BOSS,
                            onClick = { gc.selectPathNode(node.id) },
                            onFoggedTap = { gc.onFoggedNodeTap(node.id) },
                            onReroll = { gc.rerollRumor(node.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PathNodeChip(
    node: PathNode,
    currentId: String,
    choiceIds: Set<String>,
    canReroll: Boolean = false,
    canFoggedAct: Boolean = false,
    onClick: () -> Unit,
    onFoggedTap: () -> Unit = {},
    onReroll: () -> Unit = {}
) {
    val isCurrent = node.id == currentId
    val selectable = node.id in choiceIds
    val showType = node.revealed || node.scoutedTypeOnly || node.type == NodeType.START || isCurrent
    val label = when {
        node.cleared -> "✓"
        showType -> shortType(node.type)
        else -> "?"
    }
    val color = when {
        isCurrent -> Gold
        selectable -> Accent
        canFoggedAct -> Gold.copy(alpha = 0.85f)
        node.cleared -> Moss
        else -> Steel
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
        Box(
            Modifier
                .size(52.dp)
                .background(Panel, CircleShape)
                .border(2.dp, color, CircleShape)
                // Selectable choices still enter; otherwise Scout-first / rumor dispatcher
                .clickable(enabled = selectable || canFoggedAct) {
                    when {
                        selectable -> onClick()
                        canFoggedAct -> onFoggedTap()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = Bone, fontSize = 12.sp)
        }
        // Free Scout / revealed: icon + type name like row-1 (no extra rumor line)
        val rumorOrType = when {
            showType && (node.revealed || node.scoutedTypeOnly) -> node.type.name.lowercase()
            else -> node.rumor  // wrap in UI (2 lines), do not hard-clip mid-word
        }
        Text(
            rumorOrType,
            color = Bone.copy(0.65f),
            fontSize = 9.sp,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Clip,
            modifier = Modifier
                .padding(top = 2.dp)
                .fillMaxWidth(0.28f)
        )
        // Explicit rumor affordance on selectable fogged choices (rumor wallet only)
        if (canReroll && selectable) {
            Text(
                "↻ rumor",
                color = Gold,
                fontSize = 9.sp,
                modifier = Modifier.clickable { onReroll() }.padding(top = 2.dp)
            )
        }
    }
}

private fun shortType(t: NodeType): String = when (t) {
    NodeType.START -> "S"
    NodeType.COMBAT -> "⚔"
    NodeType.SHOP -> "$"
    NodeType.REST -> "R"
    NodeType.EVENT -> "?"
    NodeType.TREASURE -> "T"
    NodeType.BOSS -> "B"
}
