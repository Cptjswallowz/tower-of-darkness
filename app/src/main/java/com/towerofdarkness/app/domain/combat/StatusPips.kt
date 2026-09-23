package com.towerofdarkness.app.domain.combat

/**
 * Combat status pips (v0.1.13) — pure mirror of existing [CombatState].
 * v0.1.14: Brace zeroing-hit holds “0” for that beat, then hides.
 */
data class StatusPip(
    /** Glossary key (brace / soften). */
    val term: String,
    val count: Int
)

/** On-hit Brace draw phases — presentation order only (v0.1.14). */
enum class BraceHitDrawPhase {
    PIP_UPDATE,
    ABSORB_FLOAT,
    HP_BAR
}

/**
 * Brace pip / absorb-float / HP-bar draw sync (v0.1.14-bracesync).
 * Does not change absorb math — only what to show and in what order.
 */
object BraceDrawSync {
    /** Float duration on Brace pip before HP bar catches up (~300–500ms @1x). */
    const val ABSORB_FLOAT_MS = 420L

    /** Canonical on-hit draw order: pip → float −N → then HP leftover. */
    fun hitDrawOrder(): List<BraceHitDrawPhase> = listOf(
        BraceHitDrawPhase.PIP_UPDATE,
        BraceHitDrawPhase.ABSORB_FLOAT,
        BraceHitDrawPhase.HP_BAR
    )

    /** Absorb amount to float on the Brace pip for this event, if any. */
    fun absorbFloat(event: CombatEvent): Int? =
        event.braceAbsorbed.takeIf { it > 0 }

    /** True when HP bar presentation should lag behind pip + absorb float. */
    fun delayHpBarAfter(event: CombatEvent): Boolean =
        event.braceAbsorbed > 0

    /**
     * Latest enemy-hit event that absorbed Brace, if still the active hit for zero-hold.
     * Prefers the most recent “hits for” line so a later non-absorb hit clears the hold.
     */
    fun latestAbsorbHit(state: CombatState): CombatEvent? {
        val lastHit = state.log.asReversed().firstOrNull { ev ->
            ev.braceAbsorbed > 0 || ev.message.contains("hits for")
        } ?: return null
        return lastHit.takeIf { it.braceAbsorbed > 0 }
    }

    /** Show Brace pip at 0 for the resolving hit beat only. */
    fun holdBraceZero(state: CombatState): Boolean {
        if (state.brace != 0) return false
        if (latestAbsorbHit(state) == null) return false
        return when (state.beat) {
            CombatBeat.AFTER_ENEMY -> true
            // Defeat after absorb-to-zero: still that resolving beat
            CombatBeat.AWAITING_CONTINUE -> !state.playerWon
            else -> false
        }
    }
}

object StatusPips {
    /** Player pips under You HP bar. */
    fun forPlayer(state: CombatState): List<StatusPip> = buildList {
        when {
            state.brace > 0 -> add(StatusPip("brace", state.brace))
            BraceDrawSync.holdBraceZero(state) -> add(StatusPip("brace", 0))
        }
    }

    /** Enemy pips under that foe's HP bar. Soften = remaining counterPenalty. */
    fun forEnemy(state: CombatState): List<StatusPip> = buildList {
        if (state.counterPenalty > 0) add(StatusPip("soften", state.counterPenalty))
    }
}
