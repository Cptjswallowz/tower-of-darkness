package com.towerofdarkness.app.domain.climb

import com.towerofdarkness.app.domain.hub.HubMetaSnapshot

/**
 * Run Summary bank + trophy commit — v0.1.55-hubsplit PART C.
 * Pure helper; GameController awaits MetaStore writes before leave-summary nav.
 *
 * Invariant: bank_after == bank_before + Kept shown on sheet.
 * Menu and Continue both must apply this before leaving Summary.
 */
object SummaryBankCommit {
    /**
     * Apply Kept payout to bank + unlocks (pure).
     * Does not spend leftover run_wallet.
     */
    fun apply(bankBefore: Int, unlocks: Set<String>, payout: KeptPayout): HubMetaSnapshot =
        HubMetaSnapshot(
            remnantsBank = bankBefore + payout.kept,
            unlocks = unlocks + payout.newTrophyIds.toSet()
        )

    /** Post-leave invariant: bank after leave == bank before sheet + Kept shown. */
    fun bankInvariant(bankBefore: Int, keptShown: Int, bankAfter: Int): Boolean =
        bankAfter == bankBefore + keptShown

    /** v0.1.56-bankone — exact log line after MetaStore commit. source = continue|menu */
    fun bankWriteLogLine(prev: Int, add: Int, now: Int, source: String): String =
        "BANK write prev=$prev add=$add now=$now source=$source"

    fun leaveSource(destName: String): String = when (destName) {
        "MetaHub" -> "continue"
        "MainMenu" -> "menu"
        else -> "menu"
    }
}
