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
import com.towerofdarkness.app.domain.climb.ClimbKept
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.VoidBg

/**
 * Run Summary — v0.1.55-hubsplit PART C.
 * Continue is ALWAYS enabled after payout is written in finishRun.
 * Trophy names may still show under Kept; do not gate Continue on flash timing.
 * Menu and Continue both leave via goMenu/goHub → leaveRunSummary (await commit).
 */
@Composable
fun RunSummaryScreen(gc: GameController) {
    val s = gc.summary
    val newTrophies = s?.newTrophyNames.orEmpty()

    Column(Modifier.fillMaxSize().background(VoidBg).padding(16.dp)) {
        Text("Run Summary", color = Gold, fontSize = 22.sp)
        Spacer(Modifier.height(12.dp))
        if (s != null) {
            Text(s.title, color = Bone, fontSize = 18.sp)
            Text("Nodes cleared: ${s.nodesCleared}", color = Bone)
            Text("Floor: ${s.floorReached}", color = Bone)
            Spacer(Modifier.height(10.dp))
            // v0.1.54 Kept headline + breakdown
            Text(ClimbKept.headline(s.kept), color = Gold, fontSize = 18.sp)
            s.keptLines.forEach { line ->
                Text("${line.label}  +${line.amount}", color = Bone.copy(0.9f), fontSize = 14.sp)
            }
            // v0.1.57-titlebank: purse never banks; muted under Kept
            Text(
                "Shop purse ends with the climb.",
                color = Bone.copy(0.55f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (newTrophies.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                newTrophies.forEach { name ->
                    Text(name, color = Gold, fontSize = 15.sp)
                }
            }
            if (!s.won) {
                Text(
                    if (s.nearMiss) "Near-miss — so close." else "The climb ends.",
                    color = Gold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        // PART C: Continue always enabled once summary is shown (payout already written).
        Button(
            onClick = { gc.goHub() },
            enabled = true,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Continue") }
        Spacer(Modifier.height(8.dp))
        // PART C: Menu uses same leave-summary commit path (goMenu → leaveRunSummary).
        OutlinedButton(
            onClick = { gc.goMenu() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Menu") }
    }
}
