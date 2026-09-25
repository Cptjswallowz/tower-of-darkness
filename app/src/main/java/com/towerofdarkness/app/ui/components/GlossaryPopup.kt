package com.towerofdarkness.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.domain.glossary.Glossary
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Gold

@Composable
fun GlossaryDialog(
    term: String?,
    onDismiss: () -> Unit,
    onTerm: (String) -> Unit = {}
) {
    if (term == null) return
    val def = Glossary.definition(term) ?: "No entry."
    val title = when {
        term.equals("ashbrand", ignoreCase = true) -> "Ashbrand"
        else -> term.replaceFirstChar { it.uppercase() }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            GlossaryText(
                text = def,
                onTerm = onTerm,
                color = Bone,
                fontSizeSp = 14
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

/** Inline underlined glossary terms inside [text]; taps open definition. */
@Composable
fun GlossaryText(
    text: String,
    highlights: List<String> = Glossary.terms.keys.toList(),
    onTerm: (String) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Bone,
    fontSizeSp: Int = 14
) {
    val annotated = buildAnnotatedString {
        val lower = text.lowercase()
        var i = 0
        data class Hit(val start: Int, val end: Int, val term: String)
        val hits = mutableListOf<Hit>()
        for (term in highlights.sortedByDescending { it.length }) {
            var idx = lower.indexOf(term.lowercase())
            while (idx >= 0) {
                val end = idx + term.length
                if (hits.none { idx < it.end && end > it.start }) {
                    hits += Hit(idx, end, term)
                }
                idx = lower.indexOf(term.lowercase(), idx + 1)
            }
        }
        hits.sortBy { it.start }
        var cursor = 0
        for (h in hits) {
            if (h.start > cursor) append(text.substring(cursor, h.start))
            pushStringAnnotation(tag = "glossary", annotation = h.term)
            withStyle(
                SpanStyle(
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            ) { append(text.substring(h.start, h.end)) }
            pop()
            cursor = h.end
        }
        if (cursor < text.length) append(text.substring(cursor))
    }
    ClickableText(
        text = annotated,
        modifier = modifier,
        style = androidx.compose.ui.text.TextStyle(color = color, fontSize = fontSizeSp.sp),
        onClick = { offset ->
            annotated.getStringAnnotations("glossary", offset, offset)
                .firstOrNull()
                ?.let { onTerm(it.item) }
        }
    )
}
