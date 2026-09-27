package com.towerofdarkness.app

import com.towerofdarkness.app.domain.climb.ClimbKept
import com.towerofdarkness.app.domain.climb.ClimbKeptFlags
import com.towerofdarkness.app.domain.climb.SummaryBankCommit
import com.towerofdarkness.app.domain.hub.HubOffers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.57-titlebank — Title bank from MetaStore; victory seal +3; purse muted.
 */
class TitlebankV0157Test {

    private fun read(vararg rel: String): String {
        val f = rel.map { File(it) }.firstOrNull { it.isFile }
            ?: error("missing ${rel.toList()} from ${File(".").absolutePath}")
        return f.readText()
    }

    private fun menuSource() = read(
        "app/src/main/java/com/towerofdarkness/app/ui/screens/MainMenuScreen.kt",
        "src/main/java/com/towerofdarkness/app/ui/screens/MainMenuScreen.kt"
    )

    private fun hubSource() = read(
        "app/src/main/java/com/towerofdarkness/app/ui/screens/MetaHubScreen.kt",
        "src/main/java/com/towerofdarkness/app/ui/screens/MetaHubScreen.kt"
    )

    private fun gcSource() = read(
        "app/src/main/java/com/towerofdarkness/app/nav/GameController.kt",
        "src/main/java/com/towerofdarkness/app/nav/GameController.kt"
    )

    private fun summarySource() = read(
        "app/src/main/java/com/towerofdarkness/app/ui/screens/RunSummaryScreen.kt",
        "src/main/java/com/towerofdarkness/app/ui/screens/RunSummaryScreen.kt"
    )

    @Test
    fun titleRemnantSource_isMetaStore_notPurseOrLastPayout() {
        val menu = menuSource()
        val hub = hubSource()
        // Title composable remnant source (must paste in release notes):
        assertTrue(menu.contains("HubOffers.titleBankLine(gc.remnantsBank)"))
        assertTrue(hub.contains("HubOffers.hubBankLine(gc.remnantsBank)"))
        assertTrue(menu.contains("TITLE bank="))
        assertTrue(menu.contains("source=metastore"))
        assertFalse("Title must not use runWallet/purse", menu.contains("runWallet"))
        assertFalse(menu.contains("lastPayout =") || menu.contains("lastPayout)"))
        assertFalse(menu.contains("summary?.kept"))
        assertFalse(menu.contains("remnantsEarned"))
        assertEquals("Hub · 7 remnants", HubOffers.titleBankLine(7))
    }

    @Test
    fun finishRun_doesNotInflateRemnantsBankWithLastPayout() {
        val finish = gcSource().substringAfter("private fun finishRun").substringBefore("fun hubBuyOffer")
        assertFalse(
            "finishRun must not assign remnantsBank = committed (lastPayout pollution)",
            finish.contains("remnantsBank = committed.remnantsBank")
        )
        assertTrue(finish.contains("finishPayout(climbKept, unlockedCards, won = won)"))
        val leave = gcSource().substringAfter("fun leaveRunSummary").substringBefore("fun continueClimb")
        assertTrue(leave.contains("meta.remnantsBank.first()"))
        assertTrue(leave.contains("TITLE bank="))
        assertTrue(leave.contains("source=metastore"))
    }

    @Test
    fun afterSummaryLeave_titleBankUpdatesFromMetaStoreCommit() {
        val flags = ClimbKeptFlags(f1CombatWon = true)
        val payout = ClimbKept.finishPayout(flags, emptySet(), won = false)
        assertEquals(1, payout.kept)
        val after = SummaryBankCommit.apply(bankBefore = 10, unlocks = emptySet(), payout = payout)
        assertEquals(11, after.remnantsBank)
        assertTrue(SummaryBankCommit.bankInvariant(10, payout.kept, after.remnantsBank))
        assertEquals("Hub · 11 remnants", HubOffers.titleBankLine(after.remnantsBank))
        assertEquals("Remnants  11", HubOffers.hubBankLine(after.remnantsBank))
    }

    @Test
    fun victorySealPlus3_onlyOnWin_clearTotal15() {
        val flags = ClimbKeptFlags(
            f1CombatWon = true,
            floor2Entered = true,
            floor3Entered = true,
            caveTrollKilled = true,
            gateWardenBeaten = true
        )
        val win = ClimbKept.finishPayout(flags, emptySet(), won = true)
        // v0.1.58: clear 15 + Gate Sigil +1 = 16
        assertEquals(16, win.kept)
        assertTrue(win.lines.any { it.id == "gate_sigil" && it.amount == 1 && it.label == "Gate Sigil" })
        assertTrue(win.lines.any { it.id == "seal_breaks" && it.amount == 3 && it.label == "The seal breaks" })

        val death = ClimbKept.finishPayout(flags.copy(gateWardenBeaten = false), emptySet(), won = false)
        assertEquals(7, death.kept) // 1+2+3+1 — death totals unchanged (no seal)
        assertFalse(death.lines.any { it.id == "seal_breaks" })
        assertFalse(death.lines.any { it.label == "The seal breaks" })
    }

    @Test
    fun death_noSealLine_totalsUnchangedWithTithe() {
        val flags = ClimbKeptFlags(
            f1CombatWon = true,
            floor2Entered = true,
            floor3Entered = true,
            caveTrollKilled = true,
            gateWardenBeaten = false
        )
        val death = ClimbKept.finishPayout(flags, setOf(HubOffers.ID_ASH_TITHE), won = false)
        assertEquals(10, death.kept) // 1+2+3+1+3 tithe — unchanged vs pre-titlebank death
        assertFalse(death.lines.any { it.id == "seal_breaks" })
    }

    @Test
    fun summary_mutedShopPurseLine_andNoPurseToBank() {
        val summary = summarySource()
        assertTrue(summary.contains("Shop purse ends with the climb."))
        val kept = read(
            "app/src/main/java/com/towerofdarkness/app/domain/climb/ClimbKept.kt",
            "src/main/java/com/towerofdarkness/app/domain/climb/ClimbKept.kt"
        )
        val payoutBody = kept.substringAfter("fun finishPayout").substringBefore("fun unlockedOverlayDrawables")
        assertFalse("finishPayout must not take purse", payoutBody.contains("runWallet"))
        assertFalse(payoutBody.contains("run_wallet +"))
        assertTrue(payoutBody.contains("won: Boolean"))
        val finish = gcSource().substringAfter("private fun finishRun").substringBefore("fun hubBuyOffer")
        assertTrue(finish.contains("do NOT bank leftover run_wallet") || finish.contains("do NOT bank leftover"))
    }
}
