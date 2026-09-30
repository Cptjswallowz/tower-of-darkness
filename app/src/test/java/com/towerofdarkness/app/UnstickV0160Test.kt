package com.towerofdarkness.app

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.forge.Forge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.60-unstick — combat-end Continue softlock + plate allow-list + pip levels.
 *
 * Log-well stone plate bug painter: [SharedTilePlateBox] (TilePlateBackdrop.kt).
 * Continue is bound to combat-end state (finished / AWAITING_CONTINUE / foeHp<=0),
 * NOT log click.
 */
class UnstickV0160Test {

    private fun combatSrc() =
        File("src/main/java/com/towerofdarkness/app/ui/screens/CombatScreen.kt").readText()

    private fun gcSrc() =
        File("src/main/java/com/towerofdarkness/app/nav/GameController.kt").readText()

    @Test
    fun continueReplacesFlee_boundToCombatEndState_notLogClick() {
        val combat = combatSrc()
        // Continue in the Flee bottom slot when combat ended
        assertTrue(combat.contains("combatEnded"))
        assertTrue(combat.contains("gc.continueAfterCombat()"))
        assertTrue(
            "Continue must replace Flee in same Row slot",
            combat.contains("if (combatEnded)") && combat.contains("Flee (locked)")
        )
        // No separate Victory banner / extra Continue below the Flee row
        assertFalse(
            "Victory banner Text must not sit outside the log (softlock chrome)",
            combat.contains("if (state.playerWon) \"Victory\" else \"Defeat\"")
        )
        // Continue click is continueAfterCombat — not a log onClick
        val logBlock = if ("Combat log well UNDER Ashbrand" in combat) {
            combat.substringAfter("Combat log well UNDER Ashbrand")
                .substringBefore("Continue is OWN button")
        } else {
            combat.substringAfter("Column(Modifier.weight(1f))")
                .substringBefore("Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp))")
        }
        assertFalse("Log well must not click-to-Continue", logBlock.contains("continueAfterCombat"))
        assertFalse("Log well must not be clickable for Continue", logBlock.contains("clickable"))
        val gc = gcSrc()
        assertTrue(gc.contains("fun continueAfterCombat"))
        assertTrue(gc.contains("if (!s.finished) return"))
        assertTrue(gc.contains("COMBAT_END"))
        assertTrue(gc.contains("forceCombatWinIfNeeded"))
        assertEquals(400L, Balance.COMBAT_END_FORCE_MS)
    }

    @Test
    fun plateNotOnLogWell_bottomSlot_hp_pips_foeChips() {
        val combat = combatSrc()
        // SharedTilePlateBox only inside SkillSlot + WeaponBar
        val skillSlot = combat.substringAfter("private fun SkillSlot")
            .substringBefore("private fun WeaponBar")
        val weaponBar = combat.substringAfter("private fun WeaponBar")
            .substringBefore("private fun EnemyKitSlot")
        val enemyKit = combat.substringAfter("private fun EnemyKitSlot")
            .substringBefore("private fun HpBar")
        val hpBar = combat.substringAfter("private fun HpBar")
        val logWell = combat.substringAfter("Combat log well UNDER Ashbrand")
            .substringBefore("Continue is OWN button")
        val bottomRow = combat.substringAfter("combatEnded")
            .substringBefore("@OptIn")
            .substringBefore("private fun SkillSlot")

        assertTrue("SkillSlot must keep SharedTilePlateBox", skillSlot.contains("SharedTilePlateBox"))
        assertTrue("WeaponBar must keep SharedTilePlateBox", weaponBar.contains("SharedTilePlateBox"))
        assertFalse("Log well must not call SharedTilePlateBox(", logWell.contains("SharedTilePlateBox("))
        assertFalse("Bottom Flee/Continue slot must not use SharedTilePlateBox", bottomRow.contains("SharedTilePlateBox"))
        assertFalse("EnemyKitSlot must not use SharedTilePlateBox", enemyKit.contains("SharedTilePlateBox"))
        assertFalse("HpBar must not use SharedTilePlateBox", hpBar.contains("SharedTilePlateBox"))

        val brace = File("src/main/java/com/towerofdarkness/app/ui/components/StatusPipRow.kt").readText()
        assertFalse(brace.contains("SharedTilePlateBox"))
        val forge = File("src/main/java/com/towerofdarkness/app/ui/screens/ForgeSheet.kt").readText()
        assertTrue(forge.contains("SharedTilePlateBox"))
        val path = File("src/main/java/com/towerofdarkness/app/ui/screens/PathScreen.kt").readText()
        assertFalse("Path nodes out of plate allow-list", path.contains("SharedTilePlateBox"))

        // Release-notes discovery: painter that drew stone into log well (the bug)
        val backdrop = File("src/main/java/com/towerofdarkness/app/ui/components/TilePlateBackdrop.kt").readText()
        assertTrue(backdrop.contains("fun SharedTilePlateBox"))
        assertTrue(backdrop.contains("R.drawable.ui_tile_plate"))
        assertTrue("v0.1.61: plate Image must matchParentSize", backdrop.contains("matchParentSize()"))
    }

    @Test
    fun combatPip_lv1None_lv2II_lv3III() {
        assertNull(Forge.combatPipLabel(1))
        assertEquals("II", Forge.combatPipLabel(2))
        assertEquals("III", Forge.combatPipLabel(3))
        val combat = combatSrc()
        assertTrue(combat.contains("Forge.combatPipLabel"))
        assertTrue(combat.contains("Alignment.TopEnd"))
    }

    @Test
    fun shopPurse_notRem_still() {
        val shop = File("src/main/java/com/towerofdarkness/app/ui/screens/ShopScreen.kt").readText()
        assertTrue(shop.contains("ScrapPouch.shopPriceLine"))
        assertFalse(shop.contains("\${offer.price} rem"))
    }

    @Test
    fun packaging_vc63_vnScore() {
        val gradle = File("../build.gradle.kts").takeIf { it.isFile }
            ?: File("build.gradle.kts")
        // test cwd is app/
        val g = File("build.gradle.kts").readText()
        assertTrue(g.contains("versionCode = 65"))
        assertTrue(g.contains("versionName = \"0.1.64-specials\""))
    }
}
