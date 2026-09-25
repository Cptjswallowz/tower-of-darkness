package com.towerofdarkness.app

import com.towerofdarkness.app.domain.path.FloorArt
import com.towerofdarkness.app.domain.path.NodeType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * v0.1.31-floorart — floor backdrop + type tokens on PathScreen;
 * fog token until scouted; Floor 2 darker; no invented stubs.
 */
class FloorArtV0131Test {

    @Before
    fun clearOverride() {
        FloorArt.tokenArtPresentOverride = null
    }

    @After
    fun resetOverride() {
        FloorArt.tokenArtPresentOverride = null
    }

    @Test
    fun tag_andBackdropName() {
        assertEquals("v0.1.31-floorart", FloorArt.TAG)
        assertEquals("floor_backdrop", FloorArt.BACKDROP_DRAWABLE)
        assertTrue(FloorArt.backdropShipped())
    }

    @Test
    fun backdropResourceFile_exists() {
        val candidates = listOf(
            File("app/src/main/res/drawable/floor_backdrop.jpg"),
            File("app/src/main/res/drawable/floor_backdrop.png"),
            File("src/main/res/drawable/floor_backdrop.jpg"),
            File("src/main/res/drawable/floor_backdrop.png")
        )
        assertTrue(
            "floor_backdrop drawable missing from ${File(".").absolutePath}",
            candidates.any { it.isFile }
        )
    }

    @Test
    fun expectedTokens_sixTypes_noBoss() {
        val names = FloorArt.expectedTokenNames()
        assertEquals(6, names.size)
        assertEquals("node_start", FloorArt.TOKEN_START)
        assertEquals("node_combat", FloorArt.TOKEN_COMBAT)
        assertEquals("node_treasure", FloorArt.TOKEN_TREASURE)
        assertEquals("node_shop", FloorArt.TOKEN_SHOP)
        assertEquals("node_rest", FloorArt.TOKEN_REST)
        assertEquals("node_fog", FloorArt.TOKEN_FOG)
        assertTrue(names.containsAll(setOf(
            "node_start", "node_combat", "node_treasure",
            "node_shop", "node_rest", "node_fog"
        )))
        assertFalse(names.any { it.contains("boss") })
    }

    @Test
    fun artTokens_shippedAsNodeDrawables() {
        FloorArt.expectedTokenNames().forEach { name ->
            val f = listOf(
                java.io.File("app/src/main/res/drawable/$name.png"),
                java.io.File("src/main/res/drawable/$name.png")
            ).firstOrNull { it.isFile }
            assertNotNull("missing drawable $name", f)
            assertTrue(FloorArt.tokenArtShipped(name))
        }
        assertTrue(FloorArt.allTokensShipped())
    }

    @Test
    fun fogged_showsFogToken_untilScouted() {
        assertEquals(
            FloorArt.TOKEN_FOG,
            FloorArt.tokenDrawableName(NodeType.COMBAT, showType = false)
        )
        assertEquals(
            FloorArt.TOKEN_FOG,
            FloorArt.tokenDrawableName(NodeType.SHOP, showType = false)
        )
        assertEquals(
            FloorArt.TOKEN_FOG,
            FloorArt.tokenDrawableName(NodeType.EVENT, showType = false)
        )
    }

    @Test
    fun revealed_swapsToRealTypeToken() {
        assertEquals(FloorArt.TOKEN_START, FloorArt.tokenDrawableName(NodeType.START, true))
        assertEquals(FloorArt.TOKEN_COMBAT, FloorArt.tokenDrawableName(NodeType.COMBAT, true))
        assertEquals(FloorArt.TOKEN_TREASURE, FloorArt.tokenDrawableName(NodeType.TREASURE, true))
        assertEquals(FloorArt.TOKEN_SHOP, FloorArt.tokenDrawableName(NodeType.SHOP, true))
        assertEquals(FloorArt.TOKEN_REST, FloorArt.tokenDrawableName(NodeType.REST, true))
        // Event mystery stays on fog/? crop (Art sheet has no separate Event token)
        assertEquals(FloorArt.TOKEN_FOG, FloorArt.tokenDrawableName(NodeType.EVENT, true))
    }

    @Test
    fun boss_keepsLetterTreatment_noInventedToken() {
        assertNull(FloorArt.tokenDrawableName(NodeType.BOSS, showType = true))
        assertEquals(FloorArt.TOKEN_FOG, FloorArt.tokenDrawableName(NodeType.BOSS, showType = false))
    }

    @Test
    fun floor2_darkerFlag_andScrim() {
        assertFalse(FloorArt.floor2Darker(1))
        assertTrue(FloorArt.floor2Darker(2))
        assertEquals(FloorArt.SCRIM_ALPHA_FLOOR1, FloorArt.scrimAlpha(1), 0.001f)
        val f2 = FloorArt.scrimAlpha(2)
        assertTrue(f2 > FloorArt.SCRIM_ALPHA_FLOOR1)
        val extra = f2 - FloorArt.SCRIM_ALPHA_FLOOR1
        assertTrue("Floor2 extra dark $extra in ~10–15%", extra >= 0.10f && extra <= 0.15f)
    }

    @Test
    fun dimConstants_ringsStillReadable() {
        // Plate is Art-dimmed already; compose alpha may sit higher
        assertTrue(FloorArt.BACKDROP_ALPHA in 0.30f..0.90f)
        assertTrue(FloorArt.SCRIM_ALPHA_FLOOR1 in 0.15f..0.55f)
    }

    @Test
    fun pathScreen_wiresBackdropAndFloor2Scrim() {
        val src = pathScreenSource()
        assertTrue(src.contains("FloorBackdrop"))
        assertTrue(src.contains("R.drawable.floor_backdrop") || src.contains("floor_backdrop"))
        assertTrue(src.contains("FLOOR2_EXTRA_DARK_ALPHA") || src.contains("floor2Darker"))
        assertTrue(src.contains("tokenDrawableName"))
        assertTrue(src.contains("resolveTokenResId") || src.contains("getIdentifier"))
        // Must not invent stub drawables in-repo without Art crops
        assertFalse(src.contains("stub_token"))
    }

    @Test
    fun tokenArt_notInventedWhenMissing() {
        FloorArt.tokenArtPresentOverride = emptySet()
        FloorArt.expectedTokenNames().forEach {
            assertFalse(FloorArt.tokenArtShipped(it))
        }
        assertFalse(FloorArt.anyTokenShipped())
        assertFalse(FloorArt.allTokensShipped())
    }

    @Test
    fun tokenArt_overrideMarksShipped() {
        FloorArt.tokenArtPresentOverride = FloorArt.expectedTokenNames()
        assertTrue(FloorArt.allTokensShipped())
        assertTrue(FloorArt.tokenArtShipped(FloorArt.TOKEN_FOG))
    }

    @Test
    fun fogReveal_swapContract() {
        // Scout reveal path: fogged combat → combat token
        val fogged = FloorArt.tokenDrawableName(NodeType.COMBAT, showType = false)
        val revealed = FloorArt.tokenDrawableName(NodeType.COMBAT, showType = true)
        assertEquals(FloorArt.TOKEN_FOG, fogged)
        assertEquals(FloorArt.TOKEN_COMBAT, revealed)
        assertTrue(fogged != revealed)
    }

    private fun pathScreenSource(): String {
        val candidates = listOf(
            File("app/src/main/java/com/towerofdarkness/app/ui/screens/PathScreen.kt"),
            File("src/main/java/com/towerofdarkness/app/ui/screens/PathScreen.kt")
        )
        val f = candidates.firstOrNull { it.exists() }
            ?: error("PathScreen.kt not found from ${File(".").absolutePath}")
        return f.readText()
    }
}
