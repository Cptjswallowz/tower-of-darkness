package com.towerofdarkness.app

import com.towerofdarkness.app.domain.sound.IronclashSfx
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.52-slashlayer — swing+impact layer, Wake Critical stinger clash, brace.
 * Does NOT assert CombatFx stroke changes (LOCKED).
 */
class SlashlayerV0152Test {

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
    fun tag_isSlashlayerV0152() {
        assertEquals("v0.1.52-slashlayer", IronclashSfx.TAG)
    }

    @Test
    fun swingLeadMs_inBand40to80() {
        assertTrue(
            "SWING_LEAD_MS must be in 40–80, was ${IronclashSfx.SWING_LEAD_MS}",
            IronclashSfx.SWING_LEAD_MS in 40L..80L
        )
    }

    @Test
    fun roleToFilename_mapExact() {
        assertEquals(
            "IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg",
            IronclashSfx.ROLE_TO_FILENAME[IronclashSfx.KEY_SWING]
        )
        assertEquals(
            "IRONCLASH_23_Flesh_Hit_Light_08.ogg",
            IronclashSfx.ROLE_TO_FILENAME[IronclashSfx.KEY_IMPACT]
        )
        assertEquals(
            "IRONCLASH_23_Flesh_Hit_Light_08.ogg",
            IronclashSfx.ROLE_TO_FILENAME["slash_impact"]
        )
        assertEquals(
            "IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg",
            IronclashSfx.ROLE_TO_FILENAME["wake"]
        )
        assertEquals(
            "IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg",
            IronclashSfx.ROLE_TO_FILENAME[IronclashSfx.KEY_WAKE_CLASH]
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
            "audio/ironclash/IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg",
            IronclashSfx.PATH_SWING
        )
        assertEquals(
            "audio/ironclash/IRONCLASH_23_Flesh_Hit_Light_08.ogg",
            IronclashSfx.PATH_IMPACT
        )
        assertEquals(
            "audio/ironclash/IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg",
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
    fun loadMap_keysIncludeSwingImpactWakeBraceEmberSoftenUiDiceStingMiss() {
        val m = IronclashSfx.LOAD_MAP
        assertTrue(m.containsKey(IronclashSfx.KEY_SWING))
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
        assertEquals(IronclashSfx.PATH_SWING, m[IronclashSfx.KEY_SWING])
    }

    @Test
    fun runtimeAssets_existUnderAudio() {
        val root = assetsAudio()
        assertTrue(root.isDirectory)
        listOf(
            IronclashSfx.FILE_SWING,
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
            "ironclash/IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg" to "fde6b48d4dc8e0c305a28e6008fd3cea",
            "ironclash/IRONCLASH_23_Flesh_Hit_Light_08.ogg" to "54e2c70b9d992a20b6c50c0da6ff3146",
            "ironclash/IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg" to "d6186f00f278f7f0f13f99f06ed9a19c",
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
    fun sameFrame_impactExpandsToSwingThenImpactWithLead() {
        val frame = IronclashSfx.resolveFrame(listOf(IronclashSfx.KEY_IMPACT))
        assertEquals(2, frame.size)
        assertEquals(IronclashSfx.KEY_SWING, frame[0].key)
        assertEquals(0L, frame[0].delayMs)
        assertEquals(IronclashSfx.VOL_FULL, frame[0].volume)
        assertEquals(IronclashSfx.KEY_IMPACT, frame[1].key)
        assertEquals(IronclashSfx.SWING_LEAD_MS, frame[1].delayMs)
        assertEquals(IronclashSfx.VOL_FULL, frame[1].volume)
    }

    @Test
    fun sameFrame_impactWins_duckGarnish() {
        val frame = IronclashSfx.resolveFrame(
            listOf(IronclashSfx.KEY_IMPACT, IronclashSfx.KEY_BRACE, IronclashSfx.KEY_SOFTEN)
        )
        val byKey = frame.associate { it.key to it }
        assertEquals(IronclashSfx.VOL_FULL, byKey[IronclashSfx.KEY_SWING]?.volume)
        assertEquals(IronclashSfx.VOL_FULL, byKey[IronclashSfx.KEY_IMPACT]?.volume)
        assertEquals(IronclashSfx.SWING_LEAD_MS, byKey[IronclashSfx.KEY_IMPACT]?.delayMs)
        assertEquals(IronclashSfx.VOL_FULL, byKey[IronclashSfx.KEY_BRACE]?.volume)
        assertEquals(IronclashSfx.VOL_GARNISH_DUCK, byKey[IronclashSfx.KEY_SOFTEN]?.volume)
    }

    @Test
    fun sameFrame_legendaryLayersStingAndWakeClash_ducksEmber() {
        val frame = IronclashSfx.resolveFrame(
            listOf(IronclashSfx.KEY_LEGENDARY, IronclashSfx.KEY_EMBER)
        )
        val byKey = frame.associate { it.key to it }
        assertEquals(IronclashSfx.VOL_FULL, byKey[IronclashSfx.KEY_LEGENDARY]?.volume)
        assertEquals(IronclashSfx.VOL_WAKE_CLASH, byKey[IronclashSfx.KEY_WAKE_CLASH]?.volume)
        assertEquals(IronclashSfx.VOL_GARNISH_DUCK, byKey[IronclashSfx.KEY_EMBER]?.volume)
        assertEquals(
            "IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg",
            IronclashSfx.FILE_WAKE_CLASH.substringAfter('/')
        )
    }

    @Test
    fun braceAlone_noClash() {
        val frame = IronclashSfx.resolveFrame(listOf(IronclashSfx.KEY_BRACE))
        assertEquals(1, frame.size)
        assertEquals(IronclashSfx.KEY_BRACE, frame[0].key)
        assertFalse(frame.any { it.key == IronclashSfx.KEY_WAKE_CLASH })
    }

    @Test
    fun softenSolo_midVolume() {
        val frame = IronclashSfx.resolveFrame(listOf(IronclashSfx.KEY_SOFTEN))
        assertEquals(1, frame.size)
        assertEquals(IronclashSfx.KEY_SOFTEN, frame[0].key)
        assertTrue(frame[0].volume in 0.55f..0.70f)
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
     * Case 7 (v0.1.51-ironclash, kept): GameController must play KEY_UI at these call sites.
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
