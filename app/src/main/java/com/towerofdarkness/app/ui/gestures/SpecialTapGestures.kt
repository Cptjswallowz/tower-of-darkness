package com.towerofdarkness.app.ui.gestures

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration

/** Architect lock: long-press glossary opens at 350ms (not default ~500ms). */
const val SPECIAL_LONG_PRESS_MS = 350L

/**
 * Wrap so [combinedClickable] / tap detectors use 350ms long-press timeout.
 */
@Composable
fun WithSpecialLongPressTimeout(content: @Composable () -> Unit) {
    val base = LocalViewConfiguration.current
    val conf = remember(base) {
        object : ViewConfiguration by base {
            override val longPressTimeoutMillis: Long = SPECIAL_LONG_PRESS_MS
        }
    }
    CompositionLocalProvider(LocalViewConfiguration provides conf, content = content)
}

/**
 * v0.1.66-waketap — short tap vs long-press glossary split.
 * SHORT = [onShortTap] (dump if ready / flash if not) — NEVER glossary.
 * LONG 350ms = [onLongGlossary] — combat keeps running; close does not spend.
 *
 * Call inside [WithSpecialLongPressTimeout] so timeout is 350ms.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.specialTapSplit(
    onShortTap: () -> Unit,
    onLongGlossary: () -> Unit
): Modifier = this.then(
    Modifier.combinedClickable(
        onClick = onShortTap,
        onLongClick = onLongGlossary
    )
)
