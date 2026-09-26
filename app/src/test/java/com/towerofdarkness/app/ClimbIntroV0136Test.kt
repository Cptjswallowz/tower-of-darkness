package com.towerofdarkness.app

import com.towerofdarkness.app.nav.ClimbIntroGate
import com.towerofdarkness.app.nav.NavState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

/**
 * v0.1.36-climbintro — nav gate + asset presence; no instrumented video playback.
 */
class ClimbIntroV0136Test {

    @Test
    fun tag_andExpectedMd5() {
        assertEquals("v0.1.36-climbintro", ClimbIntroGate.TAG)
        assertEquals("climb_intro.mp4", ClimbIntroGate.ASSET_NAME)
        assertEquals("ead7b72c2a37c09b82f65ff015bfc29d", ClimbIntroGate.EXPECTED_MD5)
    }

    @Test
    fun freshClimb_withAsset_wantsIntro() {
        assertTrue(ClimbIntroGate.shouldShowIntro(freshClimb = true, assetPresent = true))
    }

    @Test
    fun freshClimb_missingAsset_silentSkip() {
        assertFalse(ClimbIntroGate.shouldShowIntro(freshClimb = true, assetPresent = false))
    }

    @Test
    fun resume_neverWantsIntro_evenWithAsset() {
        assertFalse(ClimbIntroGate.shouldShowIntro(freshClimb = false, assetPresent = true))
        assertFalse(ClimbIntroGate.shouldShowIntro(freshClimb = false, assetPresent = false))
    }

    @Test
    fun navState_includesClimbIntro() {
        val intro: NavState = NavState.ClimbIntro
        assertTrue(intro is NavState.ClimbIntro)
        assertFalse(intro is NavState.MainMenu)
        assertFalse(intro is NavState.Path)
    }

    @Test
    fun assetFile_presentWithConfirmMd5() {
        val f = assetFile()
        assertTrue("climb_intro.mp4 must be in app assets", f.isFile)
        assertEquals(14_122_212L, f.length())
        assertEquals(ClimbIntroGate.EXPECTED_MD5, md5Hex(f))
    }

    @Test
    fun controller_routesFreshThroughIntroGate_notContinue() {
        val gc = controllerSource()
        assertTrue(gc.contains("enterClimbIntroOrFresh"))
        assertTrue(gc.contains("finishClimbIntro"))
        assertTrue(gc.contains("ClimbIntroGate.shouldShowIntro"))
        assertTrue(gc.contains("nav = NavState.ClimbIntro"))

        // Extract continueClimb body only (brace-matched) — must not touch intro.
        val continueBody = functionBody(gc, "continueClimb")
        assertFalse(continueBody.contains("ClimbIntro"))
        assertFalse(continueBody.contains("enterClimbIntro"))
        assertTrue(continueBody.contains("resumeNav"))

        val climbBody = functionBody(gc, "climb")
        assertTrue(climbBody.contains("enterClimbIntroOrFresh"))

        val confirmBody = functionBody(gc, "confirmNewClimb")
        assertTrue(confirmBody.contains("enterClimbIntroOrFresh"))
    }

    @Test
    fun mainActivity_wiresClimbIntroScreen() {
        val main = mainActivitySource()
        assertTrue(main.contains("NavState.ClimbIntro -> ClimbIntroScreen(gc)"))
        assertTrue(main.contains("import com.towerofdarkness.app.ui.screens.ClimbIntroScreen"))
    }

    private fun functionBody(source: String, name: String): String {
        val sig = "fun $name("
        val start = source.indexOf(sig)
        require(start >= 0) { "fun $name not found" }
        val brace = source.indexOf('{', start)
        require(brace >= 0) { "fun $name missing body" }
        var depth = 0
        for (i in brace until source.length) {
            when (source[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return source.substring(brace, i + 1)
                }
            }
        }
        error("unbalanced braces for fun $name")
    }

    private fun assetFile(): File {
        val candidates = listOf(
            File("app/src/main/assets/climb_intro.mp4"),
            File("src/main/assets/climb_intro.mp4")
        )
        return candidates.firstOrNull { it.isFile }
            ?: error("climb_intro.mp4 not found from ${File(".").absolutePath}")
    }

    private fun controllerSource(): String {
        val candidates = listOf(
            File("app/src/main/java/com/towerofdarkness/app/nav/GameController.kt"),
            File("src/main/java/com/towerofdarkness/app/nav/GameController.kt")
        )
        val f = candidates.firstOrNull { it.exists() }
            ?: error("GameController.kt not found")
        return f.readText()
    }

    private fun mainActivitySource(): String {
        val candidates = listOf(
            File("app/src/main/java/com/towerofdarkness/app/MainActivity.kt"),
            File("src/main/java/com/towerofdarkness/app/MainActivity.kt")
        )
        val f = candidates.firstOrNull { it.exists() }
            ?: error("MainActivity.kt not found")
        return f.readText()
    }

    private fun md5Hex(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buf = ByteArray(8192)
            while (true) {
                val n = input.read(buf)
                if (n <= 0) break
                md.update(buf, 0, n)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
