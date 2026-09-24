package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.BodyArt
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.volume.VolumeArt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.1.21-portraits — You / Ash-Warden / Seal-Warden stills;
 * trash stay placeholders; overlays shrink Image not pips.
 */
class PortraitsV0121Test {

    @Test
    fun threePortraitDrawables_present() {
        val names = BodyArt.portraitDrawableNames()
        assertTrue(names.contains("portrait_you"))
        assertTrue(names.contains("portrait_ash_warden"))
        assertTrue(names.contains("portrait_seal_warden"))
        assertEquals(3, names.size)
    }

    @Test
    fun you_usesPortraitYou_titleAndCombat() {
        assertEquals("portrait_you", BodyArt.PLAYER_DRAWABLE)
        assertEquals("portrait_you", BodyArt.playerDrawableName())
        assertEquals("portrait_you", BodyArt.hallwayPlayerDrawableName())
        assertEquals(90, BodyArt.PLAYER_SLOT_DP)
        assertEquals("ed5064c12745ad7f149f9a313b673819", BodyArt.PLAYER_MD5)
    }

    @Test
    fun ashWarden_f2Boss_usesAshPortrait() {
        assertEquals("portrait_ash_warden", BodyArt.ASH_WARDEN_DRAWABLE)
        assertEquals("portrait_ash_warden", BodyArt.enemyPortraitDrawableName(EnemyKind.ASH_WARDEN))
        assertFalse(BodyArt.usesPlaceholderSilhouette(EnemyKind.ASH_WARDEN))
        assertEquals("portrait_ash_warden", EnemyKind.ASH_WARDEN.asset)
        assertEquals("Ash-Warden", EnemyKind.ASH_WARDEN.displayName)
        assertEquals(160, BodyArt.BOSS_SLOT_DP)
        assertEquals("4694b96d0f5355c878f7acebdb0141a6", BodyArt.ASH_WARDEN_MD5)
        assertTrue(VolumeArt.appliesToEnemy(EnemyKind.ASH_WARDEN))
    }

    @Test
    fun sealWarden_dragon_f1Boss_usesSealPortrait() {
        assertEquals("portrait_seal_warden", BodyArt.SEAL_WARDEN_DRAWABLE)
        assertEquals("portrait_seal_warden", BodyArt.enemyPortraitDrawableName(EnemyKind.DRAGON))
        assertFalse(BodyArt.usesPlaceholderSilhouette(EnemyKind.DRAGON))
        assertEquals("portrait_seal_warden", EnemyKind.DRAGON.asset)
        assertEquals("Seal-Warden", EnemyKind.DRAGON.displayName)
        assertEquals("1259b05113315ed78fc40630efc42d61", BodyArt.SEAL_WARDEN_MD5)
        // Volume bake stays F2-only; Seal still has no volume pass.
        assertFalse(VolumeArt.appliesToSealWarden())
        assertFalse(VolumeArt.appliesToEnemy(EnemyKind.DRAGON))
    }

    @Test
    fun trashWretchAndSealSpinner_noPortraitMapping() {
        val trash = listOf(
            EnemyKind.GOBLIN,
            EnemyKind.ORC,
            EnemyKind.TROLL,
            EnemyKind.SPIDER
        )
        trash.forEach { kind ->
            assertNull("expected no portrait for $kind", BodyArt.enemyPortraitDrawableName(kind))
            assertTrue(BodyArt.usesPlaceholderSilhouette(kind))
        }
        assertEquals("Sturdy Orc", EnemyKind.SPIDER.displayName)
        assertEquals("Weak Goblin", EnemyKind.GOBLIN.displayName)
        assertEquals(120, BodyArt.TRASH_SLOT_DP)
        assertTrue(BodyArt.hallwayEnemyUsesPlaceholder())
    }

    @Test
    fun spriteInset_shrinksUnderChrome_noPipMove() {
        assertTrue(BodyArt.SPRITE_INSET_FRACTION > 0f)
        assertTrue(BodyArt.SPRITE_INSET_FRACTION < 0.2f)
    }

    @Test
    fun volumeSurfaces_doNotClaimSealPortrait() {
        val surfaces = VolumeArt.volumeSurfaces()
        assertFalse(surfaces.contains("portrait_seal_warden"))
        assertTrue(surfaces.contains("portrait_you"))
        assertTrue(surfaces.contains("portrait_ash_warden"))
    }
}
