package com.towerofdarkness.app

import com.towerofdarkness.app.domain.sound.IronclashSfx
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.1.53-wakeblade — Wake heavy swing+impact under sting; unmapped from Clash/Critical.
 * Does NOT assert CombatFx stroke / puff / Wake art (LOCKED).
 */
class WakebladeV0153Test {

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
    fun tag_isWakebladeV0153() {
        assertEquals("v0.1.53-wakeblade", IronclashSfx.TAG)
    }

    @Test
    fun swingLeadMs_inBand40to80() {
        assertTrue(
            "SWING_LEAD_MS must be in 40–80, was ${IronclashSfx.SWING_LEAD_MS}",
            IronclashSfx.SWING_LEAD_MS in 40L..80L
        )
        assertEquals(60L, IronclashSfx.SWING_LEAD_MS)
    }

    @Test
    fun roleToFilename_wakeSwingImpactNotClashOrCritical() {
        assertEquals(
            "IRONCLASH_02_Sword_Swing_Heavy_04_wake_plus3db.ogg",
            IronclashSfx.ROLE_TO_FILENAME[IronclashSfx.KEY_WAKE_SWING]
        )
        assertEquals(
            "IRONCLASH_24_Flesh_Hit_Heavy_04_wake_plus3db.ogg",
            IronclashSfx.ROLE_TO_FILENAME[IronclashSfx.KEY_WAKE_IMPACT]
        )
        assertEquals(
            "IRONCLASH_24_Flesh_Hit_Heavy_04_wake_plus3db.ogg",
            IronclashSfx.ROLE_TO_FILENAME["wake"]
        )
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
        assertEquals("sfx_legendary_sting.wav", IronclashSfx.ROLE_TO_FILENAME["sting"])
        assertEquals(
            "IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg",
            IronclashSfx.ROLE_TO_FILENAME[IronclashSfx.KEY_BRACE]
        )
        // No Sword_Clash / Critical_Hit / wake380 on Wake (or any) role map
        IronclashSfx.ROLE_TO_FILENAME.values.forEach { name ->
            assertFalse("Wake path must not use Sword_Clash: $name", name.contains("Sword_Clash"))
            assertFalse("Wake path must not use Critical_Hit: $name", name.contains("Critical_Hit"))
            assertFalse("Wake path must not use wake380 leftover: $name", name.contains("wake380"))
        }
        assertFalse(IronclashSfx.ROLE_TO_FILENAME.containsKey("wake_clash"))
    }

    @Test
    fun loadMap_includesWakeSwingImpact_notWakeClash() {
        val m = IronclashSfx.LOAD_MAP
        assertTrue(m.containsKey(IronclashSfx.KEY_WAKE_SWING))
        assertTrue(m.containsKey(IronclashSfx.KEY_WAKE_IMPACT))
        assertTrue(m.containsKey(IronclashSfx.KEY_SWING))
        assertTrue(m.containsKey(IronclashSfx.KEY_IMPACT))
        assertTrue(m.containsKey(IronclashSfx.KEY_LEGENDARY))
        assertTrue(m.containsKey(IronclashSfx.KEY_BRACE))
        assertFalse(m.containsKey("wake_clash"))
        assertEquals(IronclashSfx.PATH_WAKE_SWING, m[IronclashSfx.KEY_WAKE_SWING])
        assertEquals(IronclashSfx.PATH_WAKE_IMPACT, m[IronclashSfx.KEY_WAKE_IMPACT])
        assertEquals(IronclashSfx.PATH_STING, m[IronclashSfx.KEY_LEGENDARY])
        m.values.forEach { path ->
            assertFalse(path.contains("Critical_Hit"))
            assertFalse(path.contains("Sword_Clash"))
            assertFalse(path.contains("wake380"))
        }
    }

    @Test
    fun loadPaths_wakeAndSlash() {
        assertEquals(
            "audio/ironclash/IRONCLASH_02_Sword_Swing_Heavy_04_wake_plus3db.ogg",
            IronclashSfx.PATH_WAKE_SWING
        )
        assertEquals(
            "audio/ironclash/IRONCLASH_24_Flesh_Hit_Heavy_04_wake_plus3db.ogg",
            IronclashSfx.PATH_WAKE_IMPACT
        )
        assertEquals(
            "audio/ironclash/IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg",
            IronclashSfx.PATH_SWING
        )
        assertEquals(
            "audio/ironclash/IRONCLASH_23_Flesh_Hit_Light_08.ogg",
            IronclashSfx.PATH_IMPACT
        )
        assertEquals("audio/sfx_legendary_sting.wav", IronclashSfx.PATH_STING)
    }

    @Test
    fun runtimeAssets_wakeAndSlashExist() {
        val root = assetsAudio()
        assertTrue(root.isDirectory)
        listOf(
            IronclashSfx.FILE_SWING,
            IronclashSfx.FILE_IMPACT,
            IronclashSfx.FILE_WAKE_SWING,
            IronclashSfx.FILE_WAKE_IMPACT,
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
    fun curatedSource_wakeMd5MatchesManifest() {
        val expected = mapOf(
            "ironclash/IRONCLASH_02_Sword_Swing_Heavy_04_wake_plus3db.ogg" to "cd0e6084ea89232334db391c8783f61e",
            "ironclash/IRONCLASH_24_Flesh_Hit_Heavy_04_wake_plus3db.ogg" to "c50e7db25867a193b28cedba44d62b4b",
            "ironclash/IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg" to "fde6b48d4dc8e0c305a28e6008fd3cea",
            "ironclash/IRONCLASH_23_Flesh_Hit_Light_08.ogg" to "54e2c70b9d992a20b6c50c0da6ff3146",
            "ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg" to "a0350366bc961ea7e65e050a0a70a8dd"
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
    fun sameFrame_legendary_wakeSwingThenStingAndWakeImpactTogether() {
        val frame = IronclashSfx.resolveFrame(listOf(IronclashSfx.KEY_LEGENDARY))
        assertEquals(3, frame.size)
        assertEquals(IronclashSfx.KEY_WAKE_SWING, frame[0].key)
        assertEquals(0L, frame[0].delayMs)
        assertEquals(IronclashSfx.VOL_FULL, frame[0].volume)
        assertEquals(IronclashSfx.KEY_LEGENDARY, frame[1].key)
        assertEquals(IronclashSfx.SWING_LEAD_MS, frame[1].delayMs)
        assertEquals(IronclashSfx.VOL_FULL, frame[1].volume)
        assertEquals(IronclashSfx.KEY_WAKE_IMPACT, frame[2].key)
        assertEquals(IronclashSfx.SWING_LEAD_MS, frame[2].delayMs)
        assertEquals(IronclashSfx.VOL_FULL, frame[2].volume)
        // No Clash / Critical keys on Wake path
        assertFalse(frame.any { it.key == "wake_clash" })
        assertFalse(frame.any { it.key.contains("clash", ignoreCase = true) })
    }

    @Test
    fun sameFrame_legendary_ducksEmber() {
        val frame = IronclashSfx.resolveFrame(
            listOf(IronclashSfx.KEY_LEGENDARY, IronclashSfx.KEY_EMBER)
        )
        val byKey = frame.associate { it.key to it }
        assertEquals(IronclashSfx.VOL_FULL, byKey[IronclashSfx.KEY_WAKE_SWING]?.volume)
        assertEquals(IronclashSfx.VOL_FULL, byKey[IronclashSfx.KEY_LEGENDARY]?.volume)
        assertEquals(IronclashSfx.VOL_FULL, byKey[IronclashSfx.KEY_WAKE_IMPACT]?.volume)
        assertEquals(IronclashSfx.VOL_GARNISH_DUCK, byKey[IronclashSfx.KEY_EMBER]?.volume)
        assertEquals(IronclashSfx.SWING_LEAD_MS, byKey[IronclashSfx.KEY_LEGENDARY]?.delayMs)
        assertEquals(IronclashSfx.SWING_LEAD_MS, byKey[IronclashSfx.KEY_WAKE_IMPACT]?.delayMs)
    }

    @Test
    fun sameFrame_normalSlash_unchanged152Volumes() {
        val frame = IronclashSfx.resolveFrame(listOf(IronclashSfx.KEY_IMPACT))
        assertEquals(2, frame.size)
        assertEquals(IronclashSfx.KEY_SWING, frame[0].key)
        assertEquals(0L, frame[0].delayMs)
        assertEquals(0.80f, frame[0].volume)
        assertEquals(IronclashSfx.KEY_IMPACT, frame[1].key)
        assertEquals(IronclashSfx.SWING_LEAD_MS, frame[1].delayMs)
        assertEquals(0.80f, frame[1].volume)
    }

    @Test
    fun braceAlone_noWakeLayers() {
        val frame = IronclashSfx.resolveFrame(listOf(IronclashSfx.KEY_BRACE))
        assertEquals(1, frame.size)
        assertEquals(IronclashSfx.KEY_BRACE, frame[0].key)
        assertFalse(frame.any { it.key == IronclashSfx.KEY_WAKE_SWING })
        assertFalse(frame.any { it.key == IronclashSfx.KEY_WAKE_IMPACT })
        assertFalse(frame.any { it.key == IronclashSfx.KEY_LEGENDARY })
    }

    @Test
    fun wakeFiles_notCriticalOrClashFilenames() {
        assertFalse(IronclashSfx.FILE_WAKE_SWING.contains("Critical"))
        assertFalse(IronclashSfx.FILE_WAKE_SWING.contains("Clash"))
        assertFalse(IronclashSfx.FILE_WAKE_IMPACT.contains("Critical"))
        assertFalse(IronclashSfx.FILE_WAKE_IMPACT.contains("Clash"))
        assertTrue(IronclashSfx.FILE_WAKE_SWING.contains("Sword_Swing_Heavy"))
        assertTrue(IronclashSfx.FILE_WAKE_IMPACT.contains("Flesh_Hit_Heavy"))
    }
}
