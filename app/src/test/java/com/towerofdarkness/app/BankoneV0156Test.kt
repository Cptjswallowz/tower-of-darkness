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
 * v0.1.56-bankone — one remnants_bank wallet; floor HUD purse; leave commit.
 * See docs/bankone-v0156.md.
 */
class BankoneV0156Test {

    private fun read(vararg rel: String): String {
        val f = rel.map { File(it) }.firstOrNull { it.isFile }
            ?: error("missing ${rel.toList()} from ${File(".").absolutePath}")
        return f.readText()
    }

    private fun gcSource() = read(
        "app/src/main/java/com/towerofdarkness/app/nav/GameController.kt",
        "src/main/java/com/towerofdarkness/app/nav/GameController.kt"
    )

    private fun metaSource() = read(
        "app/src/main/java/com/towerofdarkness/app/data/MetaStore.kt",
        "src/main/java/com/towerofdarkness/app/data/MetaStore.kt"
    )

    private fun pathSource() = read(
        "app/src/main/java/com/towerofdarkness/app/ui/screens/PathScreen.kt",
        "src/main/java/com/towerofdarkness/app/ui/screens/PathScreen.kt"
    )

    private fun floorBreakSource() = read(
        "app/src/main/java/com/towerofdarkness/app/ui/screens/FloorBreakScreen.kt",
        "src/main/java/com/towerofdarkness/app/ui/screens/FloorBreakScreen.kt"
    )

    private fun shopSource() = read(
        "app/src/main/java/com/towerofdarkness/app/ui/screens/ShopScreen.kt",
        "src/main/java/com/towerofdarkness/app/ui/screens/ShopScreen.kt"
    )

    private fun menuSource() = read(
        "app/src/main/java/com/towerofdarkness/app/ui/screens/MainMenuScreen.kt",
        "src/main/java/com/towerofdarkness/app/ui/screens/MainMenuScreen.kt"
    )

    private fun hubSource() = read(
        "app/src/main/java/com/towerofdarkness/app/ui/screens/MetaHubScreen.kt",
        "src/main/java/com/towerofdarkness/app/ui/screens/MetaHubScreen.kt"
    )

    @Test
    fun titleAndHub_sameRemnantsBankGetter() {
        val bank = 42
        assertEquals("Hub · 42 remnants", HubOffers.titleBankLine(bank))
        assertEquals("Remnants  42", HubOffers.hubBankLine(bank))
        // Both screens read gc.remnantsBank (MetaStore remnants_bank) — no separate cache.
        val menu = menuSource()
        val hub = hubSource()
        assertTrue(menu.contains("HubOffers.titleBankLine(gc.remnantsBank)"))
        assertTrue(hub.contains("HubOffers.hubBankLine(gc.remnantsBank)"))
        assertFalse("no last-summary payout as title bank", menu.contains("summary?.kept"))
        assertFalse(menu.contains("remnantsEarned"))
    }

    @Test
    fun climbStart_doesNotZeroRemnantsBank() {
        val gc = gcSource()
        val start = gc.substringAfter("fun startNewRun()").substringBefore("private fun resetRunIdentity")
        assertTrue(start.contains("runWallet = 0"))
        assertTrue(start.contains("remnants_bank MUST NOT be zeroed"))
        assertFalse(
            "startNewRun must not assign remnantsBank = 0",
            start.contains("remnantsBank = 0")
        )
        // Skip/complete tutorial climb entry likewise
        for (fn in listOf("fun skipTutorial()", "fun completeTutorial()")) {
            val body = gc.substringAfter(fn).substringBefore("fun ", "")
            assertFalse("$fn must not zero remnantsBank", body.contains("remnantsBank = 0"))
        }
    }

    @Test
    fun summaryMenuAndContinue_oneTxnCommitAndLog() {
        val gc = gcSource()
        val meta = metaSource()
        assertTrue(meta.contains("suspend fun commitSummaryBank"))
        // One edit block writes KEY_REMNANTS and KEY_UNLOCKED together
        val commit = meta.substringAfter("suspend fun commitSummaryBank").substringBefore("suspend fun spendRemnants")
        assertTrue(commit.contains("KEY_REMNANTS"))
        assertTrue(commit.contains("KEY_UNLOCKED") || commit.contains("trophyIds"))
        val leave = gc.substringAfter("fun leaveRunSummary").substringBefore("fun continueClimb")
        assertTrue(leave.contains("commitSummaryBank"))
        assertTrue(leave.contains("bankWriteLogLine"))
        assertTrue(leave.contains("\"continue\""))
        assertTrue(leave.contains("\"menu\""))
        // Both CTAs
        val goMenu = gc.substringAfter("fun goMenu()").substringBefore("fun goHub()")
        val goHub = gc.substringAfter("fun goHub()").substringBefore("fun leaveRunSummary")
        assertTrue(goMenu.contains("leaveRunSummary(NavState.MainMenu)"))
        assertTrue(goHub.contains("leaveRunSummary(NavState.MetaHub)"))
        // finishRun must NOT dual-write unlock then addRemnants
        val finish = gc.substringAfter("private fun finishRun").substringBefore("fun hubBuyOffer")
        assertFalse(finish.contains("meta.addRemnants"))
        assertFalse(finish.contains("meta.unlockCard"))
        assertTrue(finish.contains("summaryBankWritten = false"))
    }

    @Test
    fun bankWriteLogLine_formatAndSources() {
        assertEquals(
            "BANK write prev=10 add=7 now=17 source=continue",
            SummaryBankCommit.bankWriteLogLine(10, 7, 17, "continue")
        )
        assertEquals(
            "BANK write prev=3 add=0 now=3 source=menu",
            SummaryBankCommit.bankWriteLogLine(3, 0, 3, "menu")
        )
        assertEquals("continue", SummaryBankCommit.leaveSource("MetaHub"))
        assertEquals("menu", SummaryBankCommit.leaveSource("MainMenu"))
        val flags = ClimbKeptFlags(f1CombatWon = true)
        val payout = ClimbKept.finishPayout(flags, emptySet())
        val after = SummaryBankCommit.apply(5, emptySet(), payout)
        assertTrue(SummaryBankCommit.bankInvariant(5, payout.kept, after.remnantsBank))
    }

    @Test
    fun floorHud_noRemOrRemnants_usesPurse() {
        val path = pathSource()
        val brk = floorBreakSource()
        val shop = shopSource()
        // v0.1.58: HUD via scrapHudLine / scrapHudLineWithMax — still purse, never rem/remnants
        assertTrue("Path HUD uses scrapHudLine (purse)", path.contains("scrapHudLine()"))
        assertTrue("FloorBreak HUD uses scrapHudLine", brk.contains("scrapHudLine()"))
        assertTrue("Shop HUD uses scrapHudLineWithMax", shop.contains("scrapHudLineWithMax()"))
        assertFalse(path.contains(" rem"))
        assertFalse(brk.contains(" rem"))
        assertFalse(path.lowercase().contains(" remnant"))
        assertFalse(brk.lowercase().contains(" remnant"))
        val scrap = java.io.File("app/src/main/java/com/towerofdarkness/app/domain/climb/ScrapPouch.kt").takeIf { it.isFile }
            ?: java.io.File("src/main/java/com/towerofdarkness/app/domain/climb/ScrapPouch.kt")
        val scrapText = scrap.readText()
        assertTrue(scrapText.contains("HP \$hp · purse \$purse · g\$goblin o\$orc"))
    }

}
