package com.towerofdarkness.app.domain.combat

/**
 * Combat status pips (v0.1.13) — pure mirror of existing [CombatState].
 * No new stack rules; hide when count is 0.
 */
data class StatusPip(
    /** Glossary key (brace / soften). */
    val term: String,
    val count: Int
)

object StatusPips {
    /** Player pips under You HP bar. */
    fun forPlayer(state: CombatState): List<StatusPip> = buildList {
        if (state.brace > 0) add(StatusPip("brace", state.brace))
    }

    /** Enemy pips under that foe's HP bar. Soften = remaining counterPenalty. */
    fun forEnemy(state: CombatState): List<StatusPip> = buildList {
        if (state.counterPenalty > 0) add(StatusPip("soften", state.counterPenalty))
    }
}
