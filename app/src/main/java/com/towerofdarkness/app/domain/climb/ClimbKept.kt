package com.towerofdarkness.app.domain.climb

import com.towerofdarkness.app.domain.hub.HubOffers

/**
 * Kept remnants + trophies — v0.1.54-trollkept PART B.
 * Replaces hubmore banked = runWallet + ashTitheBonus.
 * See docs/trollkept-v0154.md.
 */
data class ClimbKeptFlags(
    val f1CombatWon: Boolean = false,
    val floor2Entered: Boolean = false,
    val floor3Entered: Boolean = false,
    val caveTrollKilled: Boolean = false,
    val gateWardenBeaten: Boolean = false
) {
    companion object {
        /** Migrate missing entered flags from floor when decoding older slots. */
        fun migrate(flags: ClimbKeptFlags, floor: Int): ClimbKeptFlags = flags.copy(
            floor2Entered = flags.floor2Entered || floor >= 2,
            floor3Entered = flags.floor3Entered || floor >= 3
        )
    }
}

data class KeptLine(
    val id: String,
    val label: String,
    val amount: Int
)

data class KeptPayout(
    val kept: Int,
    val lines: List<KeptLine>,
    /** Display names newly awarded this summary (already filtered to first-time). */
    val newTrophyNames: List<String>,
    /** Unlock ids to add into meta unlocks (idempotent). */
    val newTrophyIds: List<String>
)

object ClimbKept {
    const val ID_SOOT_RIM = "soot_rim"
    const val ID_ASH_PAULDRON = "ash_pauldron"
    const val ID_TROLL_TOOTH = "troll_tooth"
    const val ID_GATE_SIGIL = "gate_sigil"

    const val NAME_SOOT_RIM = "Soot Rim"
    const val NAME_ASH_PAULDRON = "Ash Pauldron"
    const val NAME_TROLL_TOOTH = "Troll Tooth"
    const val NAME_GATE_SIGIL = "Gate Sigil"

    /** Stack bottom → top for Hub/title HeroShowcase overlays. */
    val OVERLAY_STACK: List<Pair<String, String>> = listOf(
        ID_SOOT_RIM to "overlay_soot_rim",
        ID_ASH_PAULDRON to "overlay_ash_pauldron",
        ID_TROLL_TOOTH to "overlay_troll_tooth",
        ID_GATE_SIGIL to "overlay_gate_sigil"
    )

    val TROPHY_DISPLAY: Map<String, String> = mapOf(
        ID_SOOT_RIM to NAME_SOOT_RIM,
        ID_ASH_PAULDRON to NAME_ASH_PAULDRON,
        ID_TROLL_TOOTH to NAME_TROLL_TOOTH,
        ID_GATE_SIGIL to NAME_GATE_SIGIL
    )

    fun headline(kept: Int): String = "Kept: $kept remnants"

    /**
     * Pure payout: kept formula + first-time trophies.
     * Does not bank leftover run_wallet.
     */
    /**
     * @param won victory-only "The seal breaks +3". Death: no seal line; other totals unchanged.
     * Clear win (all climb lines, no Tithe) = 15.
     */
    fun finishPayout(flags: ClimbKeptFlags, unlocks: Set<String>, won: Boolean = false): KeptPayout {
        val lines = mutableListOf<KeptLine>()
        if (flags.f1CombatWon) lines += KeptLine("f1_combat", "Floor 1 combat", 1)
        if (flags.floor2Entered) lines += KeptLine("floor2", "Floor 2", 2)
        if (flags.floor3Entered) lines += KeptLine("floor3", "Floor 3", 3)
        if (flags.caveTrollKilled) lines += KeptLine("cave_troll", "Cave Troll", 1)
        if (flags.gateWardenBeaten) lines += KeptLine("gate_warden", "Gate-Warden", 5)
        // v0.1.57-titlebank: victory-only seal break +3 (not on death)
        if (won) lines += KeptLine("seal_breaks", "The seal breaks", 3)
        if (HubOffers.ID_ASH_TITHE in unlocks) {
            lines += KeptLine("ash_tithe", "Ash Tithe", HubOffers.ASH_TITHE_BONUS)
        }
        val kept = lines.sumOf { it.amount }

        val candidates = mutableListOf<Pair<String, String>>()
        if (flags.floor2Entered) candidates += ID_SOOT_RIM to NAME_SOOT_RIM
        if (flags.floor3Entered) candidates += ID_ASH_PAULDRON to NAME_ASH_PAULDRON
        if (flags.caveTrollKilled) candidates += ID_TROLL_TOOTH to NAME_TROLL_TOOTH
        if (flags.gateWardenBeaten) candidates += ID_GATE_SIGIL to NAME_GATE_SIGIL

        val newIds = candidates.map { it.first }.filter { it !in unlocks }
        val newNames = candidates.filter { it.first !in unlocks }.map { it.second }

        return KeptPayout(
            kept = kept,
            lines = lines,
            newTrophyNames = newNames,
            newTrophyIds = newIds
        )
    }

    /** Overlay drawable basenames unlocked for Hub/title bust (stack order). */
    fun unlockedOverlayDrawables(unlocks: Set<String>): List<String> =
        OVERLAY_STACK.filter { it.first in unlocks }.map { it.second }

    fun anyTrophyOwned(unlocks: Set<String>): Boolean =
        TROPHY_DISPLAY.keys.any { it in unlocks }
}
