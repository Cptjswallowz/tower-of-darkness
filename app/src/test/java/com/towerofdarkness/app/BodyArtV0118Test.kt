package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.BodyArt
import com.towerofdarkness.app.domain.combat.EnemyKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.1.18-bodies — You soldier + F2 Ash-Warden stills; F1/trash stay placeholders.
 */
class BodyArtV0118Test {

    @Test
    fun playerPortrait_resolvesSoldierDrawable() {
        assertEquals("portrait_you", BodyArt.PLAYER_DRAWABLE)
        assertEquals("portrait_you", BodyArt.playerDrawableName())
        assertEquals(90, BodyArt.PLAYER_SLOT_DP)
    }

    @Test
    fun hallway_youIsSoldier_enemyStaysPlaceholder() {
        assertEquals("portrait_you", BodyArt.hallwayPlayerDrawableName())
        assertTrue(BodyArt.hallwayEnemyUsesPlaceholder())
    }

    @Test
    fun ashWarden_f2Boss_resolvesBodyStill() {
        assertEquals("portrait_ash_warden", BodyArt.ASH_WARDEN_DRAWABLE)
        assertEquals("portrait_ash_warden", BodyArt.enemyPortraitDrawableName(EnemyKind.ASH_WARDEN))
        assertFalse(BodyArt.usesPlaceholderSilhouette(EnemyKind.ASH_WARDEN))
        assertEquals("portrait_ash_warden", EnemyKind.ASH_WARDEN.asset)
        assertEquals(160, BodyArt.BOSS_SLOT_DP)
    }

    @Test
    fun f1SealWarden_dragon_usesSealPortrait_v0121() {
        // Superseded by v0.1.21-portraits — Seal-Warden still replaces orange blob.
        assertEquals("portrait_seal_warden", BodyArt.enemyPortraitDrawableName(EnemyKind.DRAGON))
        assertFalse(BodyArt.usesPlaceholderSilhouette(EnemyKind.DRAGON))
        assertEquals("portrait_seal_warden", EnemyKind.DRAGON.asset)
        assertEquals("Seal-Warden", EnemyKind.DRAGON.displayName)
    }

    @Test
    fun trashAndSealSpinner_stayPlaceholders() {
        val trash = listOf(
            EnemyKind.GOBLIN,
            EnemyKind.ORC,
            EnemyKind.TROLL,
            EnemyKind.SPIDER
        )
        trash.forEach { kind ->
            assertNull("expected placeholder for $kind", BodyArt.enemyPortraitDrawableName(kind))
            assertTrue(BodyArt.usesPlaceholderSilhouette(kind))
        }
        assertEquals("Seal Spinner", EnemyKind.SPIDER.displayName)
        assertEquals("enemies/enemy_spider.png", EnemyKind.SPIDER.asset)
        assertEquals(120, BodyArt.TRASH_SLOT_DP)
    }

    @Test
    fun spriteInset_shrinksUnderChrome_noPipMove() {
        assertTrue(BodyArt.SPRITE_INSET_FRACTION > 0f)
        assertTrue(BodyArt.SPRITE_INSET_FRACTION < 0.2f)
    }
}
