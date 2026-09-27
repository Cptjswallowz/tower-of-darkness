package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.PortraitPlate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.22-plate — historical static-plate lock.
 * **Superseded (combat)** by v0.1.23-nobg: combat plates removed.
 * Kept to assert no infinite pulse / no dice-log-hit recolor / trash not plated.
 * See [NobgV0123Test] for the active combat contract.
 */
class PlateV0122Test {

    private fun heroShowcaseSource(): String {
        val candidates = listOf(
            File("app/src/main/java/com/towerofdarkness/app/ui/components/HeroShowcase.kt"),
            File("src/main/java/com/towerofdarkness/app/ui/components/HeroShowcase.kt")
        )
        val f = candidates.firstOrNull { it.exists() }
            ?: error("HeroShowcase.kt not found from ${File(".").absolutePath}")
        return f.readText()
    }

    private fun combatScreenSource(): String {
        val candidates = listOf(
            File("app/src/main/java/com/towerofdarkness/app/ui/screens/CombatScreen.kt"),
            File("src/main/java/com/towerofdarkness/app/ui/screens/CombatScreen.kt")
        )
        val f = candidates.firstOrNull { it.exists() }
            ?: error("CombatScreen.kt not found")
        return f.readText()
    }

    @Test
    fun supersededByNobg_combatPlateOff() {
        // Active tag is nobg; combat plate flags stay off
        assertEquals("v0.1.23-nobg", PortraitPlate.TAG)
        assertFalse(PortraitPlate.COMBAT_PLATE_ENABLED)
        assertFalse(PortraitPlate.ALLOWS_INFINITE_PULSE)
        assertFalse(PortraitPlate.ALLOWS_DICE_LOG_HIT_RECOLOR)
        assertFalse(PortraitPlate.SECOND_OVAL_BEHIND_PNG)
        assertTrue(PortraitPlate.FILL_IS_STATIC)
    }

    @Test
    fun victoryTint_noLongerDrawnInCombat() {
        assertFalse(PortraitPlate.VICTORY_TINT_ONESHOT_HOLD)
        assertFalse(PortraitPlate.VICTORY_TINT_INFINITE)
        assertFalse(PortraitPlate.VICTORY_RING_BEHIND_FIGURE)
    }

    @Test
    fun combatSlots_noPlate_trashNotPlated() {
        assertFalse(PortraitPlate.appliesToPlayer())
        assertFalse(PortraitPlate.appliesToAshWarden())
        assertFalse(PortraitPlate.appliesToSealWarden())
        assertFalse(PortraitPlate.appliesToTrashPlaceholders())
    }

    @Test
    fun heroShowcase_noInfiniteGlowPulseUnderPortrait() {
        val src = heroShowcaseSource()
        assertFalse(
            "portrait plate must not use rememberInfiniteTransition",
            src.contains("rememberInfiniteTransition")
        )
        assertFalse(
            "portrait plate must not use infiniteRepeatable",
            src.contains("infiniteRepeatable")
        )
        assertFalse(
            "combat static plate helper must be removed under nobg",
            src.contains("StaticPortraitPlate")
        )
    }

    @Test
    fun portraitPngPaths_noSecondPaintedOvalBehindStill() {
        val src = heroShowcaseSource()
        // Asset-backed PNG branch (portraitRes != null, no volume): Image + CircleShape clip only.
        // drawOval may exist only in the placeholder Canvas fallback (no portraitRes).
        val pngBranch = src.substringAfter("} else if (portraitRes != null) {", "")
            .substringBefore("} else {", "")
        assertTrue("expected asset-backed PNG branch", pngBranch.isNotBlank())
        assertFalse(
            "Seal-Warden / Gate-Warden PNG path must not drawOval behind still",
            pngBranch.contains("drawOval")
        )
        assertFalse(PortraitPlate.SECOND_OVAL_BEHIND_PNG)
    }

    @Test
    fun combatScreen_plateNotTiedToLastFiredRarityPulse() {
        val src = combatScreenSource()
        assertFalse(
            "combat You plate must not follow lastFiredCard rarity",
            src.contains("HeroShowcase(state.lastFiredCard")
        )
        assertFalse(
            "nobg: no victoryHold plate wiring in combat",
            src.contains("victoryHold")
        )
    }

    @Test
    fun victoryTint_notWiredAsInfiniteRepeatable() {
        val src = heroShowcaseSource() + "\n" + combatScreenSource()
        assertFalse(src.contains("infiniteRepeatable"))
        assertFalse(src.contains("rememberInfiniteTransition"))
        assertFalse(PortraitPlate.VICTORY_TINT_INFINITE)
    }
}
