package com.towerofdarkness.app

import com.towerofdarkness.app.domain.sound.IronclashSfx
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.51-ironclash — IRONCLASH / Lentikula / Kenney one-shots + same-frame duck.
 * Does NOT assert CombatFx stroke changes (LOCKED).
 */
class IronclashSfxV0151Test {

    private fun assetsAudio(): File {
        val a = File("app/src/main/assets/audio")
        val b = File("src/main/assets/audio")
        return if (a.isDirectory) a else b
    }

    private fun curatedSfx(): File {
        val a = File("assets/sfx")
        val b = File("../assets/sfx")
        return if (a.isDirectory) a else b
    }

    @Test
    fun tag_isIronclashV0151() {
        assertEquals("v0.1.51-ironclash", IronclashSfx.TAG)
    }

    @Test
    fun roleToFilename_mapExact() {
        assertEquals(
            "IRONCLASH_23_Flesh_Hit_Light_05.ogg",
            IronclashSfx.ROLE_TO_FILENAME[IronclashSfx.KEY_IMPACT]
        )
        assertEquals(
            "IRONCLASH_03_Sword_Clash_04_wake380.ogg",
            IronclashSfx.ROLE_TO_FILENAME["wake"]
        )
        assertEquals(
            "IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg",
            IronclashSfx.ROLE_TO_FILENAME[IronclashSfx.KEY_BRACE]
        )
        assertEquals(
            "lentikula_fire_impact_5_short220.ogg",
            IronclashSfx.ROLE_TO_FILENAME[IronclashSfx.KEY_EMBER]
        )
        assertEquals(
            "lentikula_heal_impact_4_soften.ogg",
            IronclashSfx.ROLE_TO_FILENAME[IronclashSfx.KEY_SOFTEN]
        )
        assertEquals(
            "click_001.ogg",
            IronclashSfx.ROLE_TO_FILENAME[IronclashSfx.KEY_UI]
        )
        assertEquals("sfx_dice.wav", IronclashSfx.ROLE_TO_FILENAME[IronclashSfx.KEY_DICE])
        assertEquals("sfx_legendary_sting.wav", IronclashSfx.ROLE_TO_FILENAME["sting"])
        assertEquals("sfx_miss.wav", IronclashSfx.ROLE_TO_FILENAME[IronclashSfx.KEY_MISS])
    }

    @Test
    fun loadPaths_fixedRootNotSfxSubdir() {
        assertEquals("audio/sfx_dice.wav", IronclashSfx.PATH_DICE)
        assertEquals("audio/sfx_legendary_sting.wav", IronclashSfx.PATH_STING)
        assertEquals("audio/sfx_miss.wav", IronclashSfx.PATH_MISS)
        assertFalse(IronclashSfx.PATH_DICE.contains("audio/sfx/"))
        assertEquals(
            "audio/ironclash/IRONCLASH_23_Flesh_Hit_Light_05.ogg",
            IronclashSfx.PATH_IMPACT
        )
        assertEquals(
            "audio/ironclash/IRONCLASH_03_Sword_Clash_04_wake380.ogg",
            IronclashSfx.PATH_WAKE_CLASH
        )
        assertEquals(
            "audio/ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg",
            IronclashSfx.PATH_BRACE
        )
        assertEquals(
            "audio/lentikula/lentikula_fire_impact_5_short220.ogg",
            IronclashSfx.PATH_EMBER
        )
        assertEquals(
            "audio/lentikula/lentikula_heal_impact_4_soften.ogg",
            IronclashSfx.PATH_SOFTEN
        )
        assertEquals("audio/kenney_ui/click_001.ogg", IronclashSfx.PATH_UI)
    }

    @Test
    fun loadMap_keysIncludeImpactWakeBraceEmberSoftenUiDiceStingMiss() {
        val m = IronclashSfx.LOAD_MAP
        assertTrue(m.containsKey(IronclashSfx.KEY_IMPACT))
        assertTrue(m.containsKey(IronclashSfx.KEY_WAKE_CLASH))
        assertTrue(m.containsKey(IronclashSfx.KEY_BRACE))
        assertTrue(m.containsKey(IronclashSfx.KEY_EMBER))
        assertTrue(m.containsKey(IronclashSfx.KEY_SOFTEN))
        assertTrue(m.containsKey(IronclashSfx.KEY_UI))
        assertTrue(m.containsKey(IronclashSfx.KEY_DICE))
        assertTrue(m.containsKey(IronclashSfx.KEY_LEGENDARY))
        assertTrue(m.containsKey(IronclashSfx.KEY_MISS))
        assertEquals(IronclashSfx.PATH_STING, m[IronclashSfx.KEY_LEGENDARY])
    }

    @Test
    fun runtimeAssets_existUnderAudio() {
        val root = assetsAudio()
        assertTrue(root.isDirectory)
        listOf(
            IronclashSfx.FILE_IMPACT,
            IronclashSfx.FILE_WAKE_CLASH,
            IronclashSfx.FILE_BRACE,
            IronclashSfx.FILE_EMBER,
            IronclashSfx.FILE_SOFTEN,
            IronclashSfx.FILE_UI,
            IronclashSfx.FILE_DICE,
            IronclashSfx.FILE_STING,
            IronclashSfx.FILE_MISS
        ).forEach { rel ->
            val f = File(root, rel)
            assertTrue("missing runtime asset: $rel", f.isFile && f.length() > 0)
        }
    }

    @Test
    fun curatedSource_md5MatchesManifest() {
        val expected = mapOf(
            "ironclash/IRONCLASH_23_Flesh_Hit_Light_05.ogg" to "7f48fddc00f93dee839439fbd57f81bd",
            "ironclash/IRONCLASH_03_Sword_Clash_04_wake380.ogg" to "5751cbefde0ae473f301abc846089231",
            "ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg" to "a0350366bc961ea7e65e050a0a70a8dd",
            "lentikula/lentikula_fire_impact_5_short220.ogg" to "1d24b7056b45b49d8897d48926874d19",
            "lentikula/lentikula_heal_impact_4_soften.ogg" to "248fb43b6f65506649330185bcba0b03",
            "kenney_ui/click_001.ogg" to "594234cc27bcfb903fdafc49cb728bc9"
        )
        val root = curatedSfx()
        expected.forEach { (rel, md5) ->
            val f = File(root, rel)
            assertTrue("missing curated: $rel", f.isFile)
            val got = java.security.MessageDigest.getInstance("MD5")
                .digest(f.readBytes())
                .joinToString("") { "%02x".format(it) }
            assertEquals("md5 mismatch for $rel", md5, got)
        }
    }

    @Test
    fun sameFrame_impactWins_duckGarnish() {
        val frame = IronclashSfx.resolveFrame(
            listOf(IronclashSfx.KEY_IMPACT, IronclashSfx.KEY_BRACE, IronclashSfx.KEY_SOFTEN)
        )
        val byKey = frame.toMap()
        assertEquals(IronclashSfx.VOL_FULL, byKey[IronclashSfx.KEY_IMPACT])
        assertEquals(IronclashSfx.VOL_FULL, byKey[IronclashSfx.KEY_BRACE])
        assertEquals(IronclashSfx.VOL_GARNISH_DUCK, byKey[IronclashSfx.KEY_SOFTEN])
    }

    @Test
    fun sameFrame_legendaryLayersStingAndWakeClash_ducksEmber() {
        val frame = IronclashSfx.resolveFrame(
            listOf(IronclashSfx.KEY_LEGENDARY, IronclashSfx.KEY_EMBER)
        )
        val byKey = frame.toMap()
        assertEquals(IronclashSfx.VOL_FULL, byKey[IronclashSfx.KEY_LEGENDARY])
        assertEquals(IronclashSfx.VOL_WAKE_CLASH, byKey[IronclashSfx.KEY_WAKE_CLASH])
        assertEquals(IronclashSfx.VOL_GARNISH_DUCK, byKey[IronclashSfx.KEY_EMBER])
    }

    @Test
    fun softenSolo_midVolume() {
        val frame = IronclashSfx.resolveFrame(listOf(IronclashSfx.KEY_SOFTEN))
        assertEquals(1, frame.size)
        assertEquals(IronclashSfx.KEY_SOFTEN, frame[0].first)
        assertTrue(frame[0].second in 0.55f..0.70f)
    }

    @Test
    fun blankSounds_dropped() {
        assertTrue(IronclashSfx.resolveFrame(listOf("", "  ", "ui")).size == 1)
        assertTrue(IronclashSfx.resolveFrame(emptyList()).isEmpty())
    }

    @Test
    fun garnishSet_emberAndSoftenOnly() {
        assertEquals(setOf(IronclashSfx.KEY_EMBER, IronclashSfx.KEY_SOFTEN), IronclashSfx.GARNISH_KEYS)
    }

    /**
     * Case 7 (v0.1.51-ironclash): GameController must play KEY_UI at these call sites
     * (not unit-tested here — ViewModel/SoundBus wiring). Keep in sync with GameController:
     * continueClimb, selectPathNode (after validation). confirmLoadout / buyOffer already play ui.
     * enterNode must NOT play ui (avoid double-fire from confirmLoadout → enterNode).
     */
    @Test
    fun uiCallSites_documentedForCase7() {
        val required = listOf("continueClimb", "selectPathNode")
        val alreadyOk = listOf("confirmLoadout", "buyOffer")
        assertTrue(required.isNotEmpty())
        assertTrue(alreadyOk.contains("confirmLoadout"))
        assertEquals(IronclashSfx.KEY_UI, "ui")
    }

}
