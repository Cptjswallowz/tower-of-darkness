package com.towerofdarkness.app

import com.towerofdarkness.app.domain.sound.MusicBed
import com.towerofdarkness.app.domain.sound.MusicPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.64-specials — once vs loop beds + dual-player crossfade (no isLooping click).
 * Source/assert style (like Score / Continue / Unstick).
 */
class ScorefadeV0163Test {

    private fun gradleSrc(): String {
        val a = File("build.gradle.kts")
        val b = File("app/build.gradle.kts")
        return when {
            a.isFile && a.readText().contains("versionCode") -> a.readText()
            else -> b.readText()
        }
    }

    private fun musicPlayerSrc(): String {
        val a = File("src/main/java/com/towerofdarkness/app/domain/sound/MusicPlayer.kt")
        val b = File("app/src/main/java/com/towerofdarkness/app/domain/sound/MusicPlayer.kt")
        return if (a.isFile) a.readText() else b.readText()
    }

    private fun musicBedSrc(): String {
        val a = File("src/main/java/com/towerofdarkness/app/domain/sound/MusicBed.kt")
        val b = File("app/src/main/java/com/towerofdarkness/app/domain/sound/MusicBed.kt")
        return if (a.isFile) a.readText() else b.readText()
    }

    @Test
    fun packaging_vc64_vnScorefade() {
        val g = gradleSrc()
        assertTrue(g.contains("versionCode = 66"))
        assertTrue(g.contains("versionName = \"0.1.65-fightclock\""))
    }

    @Test
    fun onceBeds_doNotLoop() {
        assertFalse(MusicBed.TITLE.loops)
        assertFalse(MusicBed.LOADOUT.loops)
        assertFalse(MusicBed.SHOP.loops)
        assertFalse(MusicBed.REST.loops)
        assertFalse(MusicBed.VICTORY.loops)
        assertFalse(MusicBed.DEFEAT.loops)
    }

    @Test
    fun loopBeds_loopFlagTrue() {
        assertTrue(MusicBed.HUB.loops)
        assertTrue(MusicBed.PATH.loops)
        assertTrue(MusicBed.COMBAT.loops)
        assertTrue(MusicBed.ELITE.loops)
        assertTrue(MusicBed.BOSS.loops)
    }

    @Test
    fun musicPlayer_dualCrossfade_noIsLoopingTrueForLoopBeds() {
        val src = musicPlayerSrc()
        // Dual-player symbols
        assertTrue(
            "MusicPlayer must document/implement dual-player crossfade",
            src.contains("dualCrossfade") || src.contains("startDualLoopHandoff") ||
                src.contains("handoffPlayer")
        )
        assertTrue(src.contains("LOOP_CROSSFADE_MS"))
        assertTrue(src.contains("ONCE_TAIL_FADE_MS"))
        assertTrue(src.contains("onceFinishedBed") || src.contains("onceFinished"))
        // Must never enable MediaPlayer looping (seekTo(0) click FAIL)
        assertFalse(
            "MusicPlayer must not set isLooping = true (loop beds use dual crossfade)",
            src.contains("isLooping = true") || src.contains("isLooping=true")
        )
        // Explicit false assignment present
        assertTrue(
            src.contains("isLooping = false") || src.contains("isLooping=false")
        )
        // Gain / duck / screen fade preserved
        assertEquals(0.40f, MusicPlayer.GAIN, 0.001f)
        assertEquals(0.18f, MusicPlayer.DUCK_GAIN, 0.001f)
        assertEquals(400L, MusicPlayer.CROSSFADE_MS)
        assertTrue(MusicPlayer.LOOP_CROSSFADE_MS in 1500L..2000L)
        assertTrue(MusicPlayer.ONCE_TAIL_FADE_MS in 1800L..2500L)
    }

    @Test
    fun musicBedSource_onceAndLoopSetsDocumented() {
        val src = musicBedSrc()
        assertTrue(src.contains("TITLE(\"title\", false)"))
        assertTrue(src.contains("HUB(\"hub\", true)"))
        assertTrue(src.contains("PATH(\"path\", true)"))
        assertTrue(src.contains("LOADOUT(\"loadout\", false)"))
        assertTrue(src.contains("COMBAT(\"combat\", true)"))
        assertTrue(src.contains("ELITE(\"elite\", true)"))
        assertTrue(src.contains("BOSS(\"boss\", true)"))
        assertTrue(src.contains("SHOP(\"shop\", false)"))
        assertTrue(src.contains("REST(\"rest\", false)"))
        assertTrue(src.contains("VICTORY(\"victory\", false)"))
        assertTrue(src.contains("DEFEAT(\"defeat\", false)"))
    }
}
