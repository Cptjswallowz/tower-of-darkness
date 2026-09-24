package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.BodyArt
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.PortraitPlate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.23-nobg — combat portraits PNG-only (no plate); title teal circle may remain;
 * trash placeholders stay as body; no victory/defeat ring behind figures.
 */
class NobgV0123Test {

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

    private fun mainMenuSource(): String {
        val candidates = listOf(
            File("app/src/main/java/com/towerofdarkness/app/ui/screens/MainMenuScreen.kt"),
            File("src/main/java/com/towerofdarkness/app/ui/screens/MainMenuScreen.kt")
        )
        val f = candidates.firstOrNull { it.exists() }
            ?: error("MainMenuScreen.kt not found")
        return f.readText()
    }

    @Test
    fun tag_andCombatPlateOff() {
        assertEquals("v0.1.23-nobg", PortraitPlate.TAG)
        assertFalse(PortraitPlate.COMBAT_PLATE_ENABLED)
        assertFalse(PortraitPlate.appliesToPlayer())
        assertFalse(PortraitPlate.appliesToAshWarden())
        assertFalse(PortraitPlate.appliesToSealWarden())
        assertFalse(PortraitPlate.appliesToTrashPlaceholders())
        assertFalse(PortraitPlate.ALLOWS_INFINITE_PULSE)
        assertFalse(PortraitPlate.SECOND_OVAL_BEHIND_PNG)
    }

    @Test
    fun titleTealCircle_allowed_notCombatPlate() {
        assertTrue(PortraitPlate.TITLE_TEAL_CIRCLE_ALLOWED)
        assertTrue(PortraitPlate.titleTealCircleAllowed())
        assertEquals(0xFF38BDF8.toInt(), PortraitPlate.TITLE_CIRCLE_ARGB)
        val menu = mainMenuSource()
        assertTrue(
            "title HeroShowcase must opt into teal circle",
            menu.contains("showTitleCircle = true")
        )
        val hero = heroShowcaseSource()
        assertTrue(hero.contains("TitleTealCircle"))
        assertTrue(hero.contains("PortraitPlate.TITLE_CIRCLE_ARGB") || hero.contains("TITLE_CIRCLE_ARGB"))
        assertFalse(
            "title circle must not use rememberInfiniteTransition",
            hero.contains("rememberInfiniteTransition")
        )
        assertFalse(hero.contains("infiniteRepeatable"))
    }

    @Test
    fun combatYouAndWardens_noPlateFillOrVictoryRing() {
        assertFalse(PortraitPlate.VICTORY_RING_BEHIND_FIGURE)
        assertFalse(PortraitPlate.DEFEAT_RING_BEHIND_FIGURE)
        assertFalse(PortraitPlate.VICTORY_TINT_ONESHOT_HOLD)
        assertFalse(PortraitPlate.VICTORY_TINT_INFINITE)

        val hero = heroShowcaseSource()
        assertFalse(
            "StaticPortraitPlate must be gone (combat plates off)",
            hero.contains("StaticPortraitPlate")
        )
        // Combat HeroShowcase default: showTitleCircle false — no plate under combat You
        assertTrue(hero.contains("showTitleCircle: Boolean = false"))

        val combat = combatScreenSource()
        assertFalse(
            "combat must not pass victoryHold into portraits",
            combat.contains("victoryHold")
        )
        assertFalse(
            "combat must not compute plateVictory",
            combat.contains("plateVictory")
        )
        // Combat You does not enable title circle
        val youCall = combat.substringAfter("HeroShowcase(", "")
            .substringBefore(")", "")
        assertFalse(youCall.contains("showTitleCircle = true"))
        assertFalse(
            "combat You must not follow lastFiredCard rarity into plate",
            combat.contains("HeroShowcase(state.lastFiredCard")
        )
    }

    @Test
    fun enemySilhouette_pngBossesNoPlate_trashPlaceholderBodyRemains() {
        val hero = heroShowcaseSource()
        // PNG paths: no plate helper, no FILL_ARGB draw under Image
        assertFalse(hero.contains("StaticPortraitPlate"))
        assertFalse(hero.contains("PortraitPlate.FILL_ARGB"))
        assertFalse(hero.contains("VICTORY_TINT_ARGB"))

        // Trash Canvas placeholder body still present
        assertTrue(hero.contains("drawCircle(body"))
        assertTrue(
            BodyArt.usesPlaceholderSilhouette(EnemyKind.GOBLIN)
        )
        assertTrue(BodyArt.usesPlaceholderSilhouette(EnemyKind.SPIDER))
        assertNull(BodyArt.enemyPortraitDrawableName(EnemyKind.GOBLIN))
        assertNull(BodyArt.enemyPortraitDrawableName(EnemyKind.ORC))
        assertNull(BodyArt.enemyPortraitDrawableName(EnemyKind.TROLL))
        assertNull(BodyArt.enemyPortraitDrawableName(EnemyKind.SPIDER))
        // Wardens still mapped to PNG stills
        assertEquals("portrait_ash_warden", BodyArt.enemyPortraitDrawableName(EnemyKind.ASH_WARDEN))
        assertEquals("portrait_seal_warden", BodyArt.enemyPortraitDrawableName(EnemyKind.DRAGON))
        assertEquals("portrait_you", BodyArt.PLAYER_DRAWABLE)
    }

    @Test
    fun combatScreen_victoryDefeat_bannerOnly_noRingBehindFigure() {
        val combat = combatScreenSource()
        assertTrue(combat.contains("\"Victory\"") || combat.contains("Victory"))
        assertTrue(combat.contains("\"Defeat\"") || combat.contains("Defeat"))
        assertTrue(combat.contains("Continue"))
        // No victory tint / plate ring wiring
        assertFalse(combat.contains("victoryHold"))
        assertFalse(combat.contains("VICTORY_TINT"))
        assertFalse(combat.contains("StaticPortraitPlate"))
    }
}
