package com.towerofdarkness.app

import com.towerofdarkness.app.domain.climb.ClimbKept
import com.towerofdarkness.app.domain.climb.ClimbKeptFlags
import com.towerofdarkness.app.domain.climb.SummaryBankCommit
import com.towerofdarkness.app.domain.combat.PortraitPlate
import com.towerofdarkness.app.domain.hub.HubOffers
import com.towerofdarkness.app.domain.hub.HubRelics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.55-hubsplit — PART C bank commit; Hub Relics/Perks/Skills; teal circle off.
 * See docs/hubsplit-v0155.md.
 */
class HubsplitV0155Test {

    private fun read(vararg rel: String): String {
        val f = rel.map { File(it) }.firstOrNull { it.isFile }
            ?: error("missing ${rel.toList()} from ${File(".").absolutePath}")
        return f.readText()
    }

    private fun gcSource() = read(
        "app/src/main/java/com/towerofdarkness/app/nav/GameController.kt",
        "src/main/java/com/towerofdarkness/app/nav/GameController.kt"
    )

    private fun summarySource() = read(
        "app/src/main/java/com/towerofdarkness/app/ui/screens/RunSummaryScreen.kt",
        "src/main/java/com/towerofdarkness/app/ui/screens/RunSummaryScreen.kt"
    )

    private fun hubSource() = read(
        "app/src/main/java/com/towerofdarkness/app/ui/screens/MetaHubScreen.kt",
        "src/main/java/com/towerofdarkness/app/ui/screens/MetaHubScreen.kt"
    )

    private fun heroSource() = read(
        "app/src/main/java/com/towerofdarkness/app/ui/components/HeroShowcase.kt",
        "src/main/java/com/towerofdarkness/app/ui/components/HeroShowcase.kt"
    )

    private fun menuSource() = read(
        "app/src/main/java/com/towerofdarkness/app/ui/screens/MainMenuScreen.kt",
        "src/main/java/com/towerofdarkness/app/ui/screens/MainMenuScreen.kt"
    )

    // --- PART C: bank commit ---

    @Test
    fun partC_apply_bankInvariant_menuAndContinue() {
        val flags = ClimbKeptFlags(
            f1CombatWon = true,
            floor2Entered = true,
            floor3Entered = true,
            caveTrollKilled = true,
            gateWardenBeaten = false
        )
        val bankBefore = 10
        val unlocks = emptySet<String>()
        val payout = ClimbKept.finishPayout(flags, unlocks)
        // 1+2+3+1 = 7 Kept; soot/ash/tooth trophies
        assertEquals(7, payout.kept)
        val after = SummaryBankCommit.apply(bankBefore, unlocks, payout)
        assertEquals(17, after.remnantsBank)
        assertTrue(SummaryBankCommit.bankInvariant(bankBefore, payout.kept, after.remnantsBank))
        assertTrue(ClimbKept.ID_SOOT_RIM in after.unlocks)
        assertTrue(ClimbKept.ID_ASH_PAULDRON in after.unlocks)
        assertTrue(ClimbKept.ID_TROLL_TOOTH in after.unlocks)
        assertFalse(ClimbKept.ID_GATE_SIGIL in after.unlocks)

        // Already-owned trophies: bank still increases; unlocks idempotent
        val owned = after.unlocks
        val again = ClimbKept.finishPayout(flags, owned)
        val after2 = SummaryBankCommit.apply(after.remnantsBank, owned, again)
        assertEquals(after.remnantsBank + again.kept, after2.remnantsBank)
        assertTrue(again.newTrophyIds.isEmpty())
        assertEquals(owned, after2.unlocks)
    }

    @Test
    fun partC_continueAlwaysEnabled_noAnimationGate() {
        val summary = summarySource()
        assertFalse(
            "Continue must not wait on trophy flash delay",
            summary.contains("delay(2000)")
        )
        assertFalse(
            "continueReady gate removed",
            summary.contains("continueReady")
        )
        assertTrue(summary.contains("enabled = true"))
        // Both CTAs leave via goHub/goMenu (leaveRunSummary path)
        assertTrue(summary.contains("gc.goHub()"))
        assertTrue(summary.contains("gc.goMenu()"))
    }

    @Test
    fun partC_goMenuAndGoHub_awaitLeaveRunSummary() {
        val gc = gcSource()
        assertTrue(gc.contains("fun leaveRunSummary"))
        assertTrue(gc.contains("SummaryBankCommit.apply"))
        // goMenu from Summary must not bare-nav
        val goMenu = gc.substringAfter("fun goMenu()").substringBefore("fun goHub()")
        assertTrue(goMenu.contains("leaveRunSummary(NavState.MainMenu)"))
        assertFalse(
            "goMenu must not only assign MainMenu when on Summary",
            goMenu.trim().startsWith("{ nav = NavState.MainMenu }")
        )
        val goHub = gc.substringAfter("fun goHub()").substringBefore("fun leaveRunSummary")
        assertTrue(goHub.contains("leaveRunSummary(NavState.MetaHub)"))
        // v0.1.56: leave launches one-txn commit; mid-run clear on leave
        assertTrue(gc.contains("summaryCommitJob = viewModelScope.launch"))
        assertTrue(gc.contains("meta.clearMidRunSlot()"))
        assertTrue(gc.contains("commitSummaryBank"))
        val leave = gc.substringAfter("fun leaveRunSummary").substringBefore("fun continueClimb")
        assertTrue(leave.contains("clearMidRunSlot"))
        assertTrue(leave.contains("commitSummaryBank"))
        assertTrue(leave.contains("bankWriteLogLine"))
    }

    // --- PART B: Hub Relics / sections ---

    @Test
    fun hubRelics_earnLinesExact_ownedOnly_emptyCopy() {
        assertEquals(
            listOf(
                ClimbKept.ID_SOOT_RIM to "Reach Floor 2",
                ClimbKept.ID_ASH_PAULDRON to "Reach Floor 3",
                ClimbKept.ID_TROLL_TOOTH to "Kill Cave Troll",
                ClimbKept.ID_GATE_SIGIL to "Beat Gate-Warden"
            ),
            HubRelics.ALL.map { it.id to it.earnLine }
        )
        assertEquals(
            listOf("Soot Rim", "Ash Pauldron", "Troll Tooth", "Gate Sigil"),
            HubRelics.ALL.map { it.name }
        )
        assertEquals("Nothing kept yet. Survive a floor.", HubRelics.EMPTY_COPY)
        assertTrue(HubRelics.ownedRows(emptySet()).isEmpty())
        assertEquals(
            listOf(ClimbKept.ID_SOOT_RIM, ClimbKept.ID_TROLL_TOOTH),
            HubRelics.ownedRows(
                setOf(ClimbKept.ID_SOOT_RIM, ClimbKept.ID_TROLL_TOOTH, "scout_charge")
            ).map { it.id }
        )
        // Unowned must not appear
        assertFalse(
            HubRelics.ownedRows(setOf(ClimbKept.ID_SOOT_RIM))
                .any { it.id == ClimbKept.ID_GATE_SIGIL }
        )
    }

    @Test
    fun hubSectionOrder_relicsPerksSkills_andOfferOrders() {
        assertEquals(
            listOf("Relics", "Perks", "Skills"),
            HubRelics.SECTION_ORDER
        )
        assertEquals(
            listOf(
                HubOffers.ID_SCOUT,
                HubOffers.ID_EXTRA_RUMOR,
                HubOffers.ID_HOSTBLOOD,
                HubOffers.ID_WARM_ASH,
                HubOffers.ID_ASH_TITHE
            ),
            HubRelics.PERK_IDS
        )
        assertEquals(
            listOf(
                HubOffers.ID_HOST_OF_EMBERS,
                HubOffers.ID_IRON_LESSON,
                HubOffers.ID_COLD_DRAW,
                HubOffers.ID_BRAND_LESSON,
                HubOffers.ID_SPARK_LESSON,
                HubOffers.ID_ECHO_LESSON
            ),
            HubRelics.SKILL_IDS
        )
        // Prices unchanged vs HubOffers
        assertEquals(
            listOf(8, 6, 10, 8, 8),
            HubRelics.perkOffers().map { it.cost }
        )
        assertEquals(
            listOf(12, 12, 10, 10, 12, 12),
            HubRelics.skillOffers().map { it.cost }
        )

        val hub = hubSource()
        assertTrue(hub.contains("HubRelics.SECTION_RELICS"))
        assertTrue(hub.contains("HubRelics.SECTION_PERKS"))
        assertTrue(hub.contains("HubRelics.SECTION_SKILLS"))
        assertTrue(hub.contains("HubRelics.EMPTY_COPY"))
        assertTrue(hub.contains("HubOffers.hubBankLine"))
        // Relics have no Buy CTA
        val relicsBlock = hub.substringAfter("SECTION_RELICS").substringBefore("SECTION_PERKS")
        assertFalse(relicsBlock.contains("HubCta.BUY"))
        assertFalse(relicsBlock.contains("hubBuyOffer"))
        // Section order in source: Relics before Perks before Skills
        val iRelics = hub.indexOf("SECTION_RELICS")
        val iPerks = hub.indexOf("SECTION_PERKS")
        val iSkills = hub.indexOf("SECTION_SKILLS")
        assertTrue(iRelics < iPerks && iPerks < iSkills)
    }

    // --- PART A: teal off + shared composite ---

    @Test
    fun partA_tealCircleOff_sharedComposite() {
        assertFalse(PortraitPlate.TITLE_TEAL_CIRCLE_ALLOWED)
        assertFalse(PortraitPlate.titleTealCircleAllowed())
        val menu = menuSource()
        assertFalse(menu.contains("showTitleCircle = true"))
        val hero = heroSource()
        assertTrue(hero.contains("fun YouPortraitComposite"))
        assertTrue(hero.contains("YouPortraitComposite("))
        // Overlay stack order mentioned / used via ClimbKept
        assertTrue(hero.contains("ClimbKept.unlockedOverlayDrawables") || hero.contains("trophyUnlocks"))
    }
}
