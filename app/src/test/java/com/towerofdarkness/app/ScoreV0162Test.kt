package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.CombatPhase
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.sound.MusicBed
import com.towerofdarkness.app.domain.sound.MusicIntent
import com.towerofdarkness.app.domain.sound.MusicMap
import com.towerofdarkness.app.domain.sound.MusicPlayer
import com.towerofdarkness.app.nav.NavState
import com.towerofdarkness.app.nav.RunSummaryData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.62-score — bed music assets + MusicPlayer / MusicMap wiring.
 * Source/asset presence style (like Unstick / Slashlayer). Does not touch SFX volumes.
 */
class ScoreV0162Test {

    private fun runtimeMusic(): File {
        val a = File("app/src/main/assets/music")
        val b = File("src/main/assets/music")
        return if (a.isDirectory) a else b
    }

    private fun curatedMusic(): File {
        val a = File("assets/music")
        val b = File("../assets/music")
        return if (a.isDirectory) a else b
    }

    @Test
    fun packaging_vc63_vnScore() {
        val g = File("build.gradle.kts").readText()
        assertTrue(g.contains("versionCode = 67"))
        assertTrue(g.contains("versionName = \"0.1.66-waketap\""))
    }

    @Test
    fun allElevenRuntimeMusicPathsExist() {
        val dir = runtimeMusic()
        assertTrue("runtime music dir missing: $dir", dir.isDirectory)
        for (slot in MusicBed.ALL_SLOTS) {
            val f = File(dir, "$slot.ogg")
            assertTrue("missing runtime music/$slot.ogg", f.isFile && f.length() > 0)
        }
        assertEquals(11, MusicBed.ALL_SLOTS.size)
    }

    @Test
    fun curatedMusicMirrorPresent() {
        val dir = curatedMusic()
        assertTrue(dir.isDirectory)
        for (slot in MusicBed.ALL_SLOTS) {
            assertTrue(File(dir, "$slot.ogg").isFile)
        }
    }

    @Test
    fun musicPlayerAndMapSymbolsPresent() {
        val player = File("src/main/java/com/towerofdarkness/app/domain/sound/MusicPlayer.kt").readText()
        assertTrue(player.contains("class MusicPlayer"))
        assertTrue(player.contains("GAIN = 0.40f") || player.contains("GAIN = 0.4"))
        assertTrue(player.contains("DUCK_GAIN"))
        assertTrue(player.contains("fun duck"))
        assertTrue(player.contains("fun apply"))
        assertTrue(player.contains("MUSIC"))

        val map = File("src/main/java/com/towerofdarkness/app/domain/sound/MusicMap.kt").readText()
        assertTrue(map.contains("object MusicMap"))
        assertTrue(map.contains("fun resolve"))
        assertTrue(map.contains("fun combatBed"))
        assertTrue(map.contains("DUCK_KEYS"))

        val main = File("src/main/java/com/towerofdarkness/app/MainActivity.kt").readText()
        assertTrue(main.contains("MusicMap.resolve"))
        assertTrue(main.contains("onAppBackground"))
        assertTrue(main.contains("onAppForeground"))
        assertTrue(main.contains("LaunchedEffect"))

        val bus = File("src/main/java/com/towerofdarkness/app/domain/sound/SoundBus.kt").readText()
        assertTrue(bus.contains("MusicPlayer"))
        assertTrue(bus.contains("music?.duck()"))
        assertTrue(bus.contains("MusicMap.DUCK_KEYS"))
    }

    @Test
    fun victoryNotFromCombatVictoryWait_continueReferencesSting() {
        // While on Combat (even if enemy dead / VICTORY phase), map keeps combat bed — not victory.
        val boss = Enemy(EnemyKind.DRAGON, 28, 0, isBoss = true)
        // combatState null path still combat bed; with boss enemy → boss bed
        val intentCombat = MusicMap.resolve(NavState.Combat, combat = null, summary = null)
        assertEquals(MusicIntent.Loop(MusicBed.COMBAT), intentCombat)
        assertEquals(MusicBed.BOSS, MusicMap.combatBed(boss))
        assertEquals(MusicBed.ELITE, MusicMap.combatBed(Enemy.normal(EnemyKind.CAVE_TROLL, 3)))
        assertEquals(MusicBed.COMBAT, MusicMap.combatBed(Enemy.normal(EnemyKind.GOBLIN, 1)))

        val winSummary = RunSummaryData(
            won = true, nodesCleared = 1, remnantsEarned = 0, floorReached = 3, nearMiss = false
        )
        val loseSummary = RunSummaryData(
            won = false, nodesCleared = 1, remnantsEarned = 0, floorReached = 1, nearMiss = false
        )
        assertEquals(
            MusicIntent.Sting(MusicBed.VICTORY),
            MusicMap.resolve(NavState.RunSummary, null, winSummary)
        )
        assertEquals(
            MusicIntent.Sting(MusicBed.DEFEAT),
            MusicMap.resolve(NavState.RunSummary, null, loseSummary)
        )
        assertEquals(MusicIntent.Silence, MusicMap.resolve(NavState.ClimbIntro, null, null))

        val gc = File("src/main/java/com/towerofdarkness/app/nav/GameController.kt").readText()
        assertTrue(gc.contains("fun continueAfterCombat"))
        assertTrue(
            "Continue path must reference victory sting / RunSummary rule",
            gc.contains("victory.ogg") || gc.contains("victory sting") || gc.contains("MusicMap")
        )
        assertTrue(gc.contains("val music = MusicPlayer"))
    }

    @Test
    fun screenMap_titleHubPathLoadoutShopRest() {
        assertEquals(MusicIntent.Loop(MusicBed.TITLE), MusicMap.resolve(NavState.MainMenu, null, null))
        assertEquals(MusicIntent.Loop(MusicBed.HUB), MusicMap.resolve(NavState.MetaHub, null, null))
        assertEquals(MusicIntent.Loop(MusicBed.PATH), MusicMap.resolve(NavState.Path, null, null))
        assertEquals(MusicIntent.Loop(MusicBed.PATH), MusicMap.resolve(NavState.FloorBreak, null, null))
        assertEquals(MusicIntent.Loop(MusicBed.LOADOUT), MusicMap.resolve(NavState.Loadout, null, null))
        assertEquals(MusicIntent.Loop(MusicBed.SHOP), MusicMap.resolve(NavState.Shop, null, null))
        assertEquals(MusicIntent.Loop(MusicBed.REST), MusicMap.resolve(NavState.Rest, null, null))
    }

    @Test
    fun victoryDefeatAreOneShot() {
        assertFalse(MusicBed.VICTORY.loops)
        assertFalse(MusicBed.DEFEAT.loops)
        assertTrue(MusicBed.COMBAT.loops)
        assertTrue(MusicBed.BOSS.loops)
        assertEquals(0.40f, MusicPlayer.GAIN, 0.001f)
        assertEquals(0.18f, MusicPlayer.DUCK_GAIN, 0.001f)
    }

    @Test
    fun combatPhaseVictoryConstantStillPresent() {
        // Sanity: VICTORY phase exists; music must not key off it for sting (map uses NavState).
        assertEquals("VICTORY", CombatPhase.VICTORY.name)
        val map = File("src/main/java/com/towerofdarkness/app/domain/sound/MusicMap.kt").readText()
        assertFalse(
            "MusicMap must not start victory from CombatPhase.VICTORY alone",
            map.contains("CombatPhase.VICTORY")
        )
    }
}
