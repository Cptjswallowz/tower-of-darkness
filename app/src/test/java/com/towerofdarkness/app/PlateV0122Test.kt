package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.PortraitPlate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.22-plate — static portrait slot fill; no infinite glow under You/Wardens;
 * victory tint one-shot hold; no second painted oval behind PNG stills.
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
    fun tag_andStaticFill_locked() {
        assertEquals("v0.1.22-plate", PortraitPlate.TAG)
        assertTrue(PortraitPlate.FILL_IS_STATIC)
        assertFalse(PortraitPlate.ALLOWS_INFINITE_PULSE)
        assertFalse(PortraitPlate.ALLOWS_DICE_LOG_HIT_RECOLOR)
        assertFalse(PortraitPlate.SECOND_OVAL_BEHIND_PNG)
        // Dark Ash plate (not transparent) — one treatment
        assertEquals(0xFF2B2A28.toInt(), PortraitPlate.FILL_ARGB)
    }

    @Test
    fun victoryTint_oneshotHold_notInfinite() {
        assertTrue(PortraitPlate.VICTORY_TINT_ONESHOT_HOLD)
        assertFalse(PortraitPlate.VICTORY_TINT_INFINITE)
        val a = (PortraitPlate.VICTORY_TINT_ARGB ushr 24) and 0xFF
        assertTrue("victory tint should be visible but not opaque blowout", a in 0x20..0xB0)
    }

    @Test
    fun appliesToYouAndBothWardens() {
        assertTrue(PortraitPlate.appliesToPlayer())
        assertTrue(PortraitPlate.appliesToAshWarden())
        assertTrue(PortraitPlate.appliesToSealWarden())
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
        assertTrue(
            "static plate helper expected",
            src.contains("StaticPortraitPlate")
        )
        assertTrue(src.contains("PortraitPlate.FILL_ARGB") || src.contains("PortraitPlate"))
    }

    @Test
    fun portraitPngPaths_noSecondPaintedOvalBehindStill() {
        val src = heroShowcaseSource()
        // Seal / Ash / You PNG branches must not drawOval behind the Image.
        // Trash placeholder Canvas may still use drawOval as silhouette art.
        val sealBranch = src.substringAfter("Asset-backed boss without volume", "")
            .substringBefore("Trash / Wretch", src)
        assertFalse(
            "Seal-Warden PNG path must not drawOval behind still",
            sealBranch.contains("drawOval")
        )
        assertTrue(src.contains("StaticPortraitPlate"))
    }

    @Test
    fun combatScreen_plateNotTiedToLastFiredRarityPulse() {
        val src = combatScreenSource()
        assertFalse(
            "combat You plate must not follow lastFiredCard rarity",
            src.contains("HeroShowcase(state.lastFiredCard")
        )
        assertTrue(
            "victory hold should drive optional one-shot tint",
            src.contains("victoryHold")
        )
    }

    @Test
    fun victoryTint_notWiredAsInfiniteRepeatable() {
        val src = heroShowcaseSource() + "\n" + combatScreenSource()
        // Guard: victory path must not introduce infinite plate animation.
        assertFalse(src.contains("infiniteRepeatable"))
        assertFalse(src.contains("rememberInfiniteTransition"))
        assertTrue(PortraitPlate.VICTORY_TINT_ONESHOT_HOLD)
        assertFalse(PortraitPlate.VICTORY_TINT_INFINITE)
    }
}
