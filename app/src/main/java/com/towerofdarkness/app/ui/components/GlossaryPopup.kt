package com.towerofdarkness.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.towerofdarkness.app.domain.glossary.Glossary
import com.towerofdarkness.app.ui.theme.Gold

@Composable
fun GlossaryDialog(term: String?, onDismiss: () -> Unit) {
    if (term == null) return
    val def = Glossary.definition(term) ?: "No entry."
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(term.replaceFirstChar { it.uppercase() }) },
        text = { Text(def) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun GlossaryText(text: String, highlights: List<String>, onTerm: (String) -> Unit, modifier: Modifier = Modifier) {
    val lower = text.lowercase()
    Column(modifier) {
        // Simple: show text + tap chips for terms present
        Text(text, style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            highlights.filter { lower.contains(it.lowercase()) }.forEach { term ->
                Text(
                    term,
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onTerm(term) }
                )
            }
        }
    }
}
