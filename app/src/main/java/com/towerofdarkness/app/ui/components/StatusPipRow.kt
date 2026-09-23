package com.towerofdarkness.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.domain.combat.StatusPip
import com.towerofdarkness.app.ui.theme.Ash
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Ember
import com.towerofdarkness.app.ui.theme.Moss
import com.towerofdarkness.app.ui.theme.Panel

/**
 * Row of status pips (glyph + remaining count) under a fighter HP bar.
 * Dark card chrome matches skill slots (Panel / Bone / ember accent).
 */
@Composable
fun StatusPipRow(
    pips: List<StatusPip>,
    onTerm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (pips.isEmpty()) return
    Row(
        modifier = modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        pips.forEach { pip ->
            StatusPipChip(pip = pip, onClick = { onTerm(pip.term) })
        }
    }
}

@Composable
private fun StatusPipChip(pip: StatusPip, onClick: () -> Unit) {
    val (icon, accent) = glyphFor(pip.term)
    val shape = RoundedCornerShape(6.dp)
    Row(
        Modifier
            .background(Panel, shape)
            .border(1.dp, accent.copy(alpha = 0.55f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = pip.term,
            tint = accent,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = pip.count.toString(),
            color = Bone,
            fontSize = 11.sp
        )
    }
}

private fun glyphFor(term: String): Pair<ImageVector, androidx.compose.ui.graphics.Color> =
    when (term.lowercase()) {
        "brace" -> Icons.Filled.Shield to Moss
        "soften" -> Icons.Filled.KeyboardArrowDown to Ember
        else -> Icons.Filled.Shield to Ash
    }
