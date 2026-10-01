package com.towerofdarkness.app.domain.combat

/**
 * Fight clock — v0.1.65-fightclock P1.
 * Format: `Cycle N · Beat X/Y · Whose` (or `Beat X` when denom messy).
 * See docs/fightclock-v0165.md.
 */
enum class FightWhose(val label: String) {
    YOU("You"),
    FOE("Foe"),
    SPECIAL("Special")
}

object FightClock {
    /** Y = 3 live regulars + foe kit size. */
    fun beatDenom(state: CombatState): Int {
        val foeKit = EnemyKits.skillsFor(state.enemy).size
        return 3 + foeKit
    }

    /**
     * Exact shape: `Cycle 2 · Beat 3/6 · You`
     * When X > Y (specials pushed past denom), omit `/Y`.
     */
    fun line(state: CombatState): String {
        val cycle = state.regularCycleIndex.coerceAtLeast(1)
        val x = state.cycleBeat.coerceAtLeast(0)
        val y = beatDenom(state)
        val beat = if (y > 0 && x <= y) "Beat $x/$y" else "Beat $x"
        return "Cycle $cycle · $beat · ${state.lastWhose.label}"
    }

    /** Wake charge dots matching Ashbrand pips (threshold slots). */
    fun wakeDots(state: CombatState): String {
        val filled = state.weapon.pipsFilled.coerceIn(0, state.weapon.threshold)
        val total = state.weapon.threshold.coerceAtLeast(1)
        return buildString {
            repeat(total) { i ->
                append(if (i < filled) '●' else '○')
            }
        }
    }

    fun graveLabel(state: CombatState): String =
        "${state.graveBrandCharge.coerceIn(0, 3)}/3"

    fun vowLabel(state: CombatState): String =
        if (state.ashVowSpent) "spent" else "ready"
}
