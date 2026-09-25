package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.EnemyKits
import com.towerofdarkness.app.domain.combat.WakeArt
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.glossary.Glossary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

/**
 * v0.1.34-blade — Ashbrand portrait still in weapon tile (no rembg redraw).
 * Rejected v0.1.33 knockout md5 d0997c1572907bfb28efdbe15fd2bf63.
 * Stage portrait md5 846af065acf75d5f08a08ca0bfd4173a (1408×1408).
 * See docs/blade-v0134.md.
 */
class BladeV0134Test {

    companion object {
        const val REJECTED_KNOCKOUT_MD5 = "d0997c1572907bfb28efdbe15fd2bf63"
        /** Stage / shipped still — ashbrand_portrait.jpg bytes as ashbrand_icon.jpg. */
        const val PORTRAIT_STILL_MD5 = "846af065acf75d5f08a08ca0bfd4173a"
        const val REJECTED_SIZE_PROXY = 14_000L
    }

    private fun iconFile(): File {
        val candidates = listOf(
            File("app/src/main/res/drawable/ashbrand_icon.jpg"),
            File("src/main/res/drawable/ashbrand_icon.jpg"),
            File("app/src/main/res/drawable/ashbrand_icon.png"),
            File("src/main/res/drawable/ashbrand_icon.png")
        )
        return candidates.firstOrNull { it.isFile }
            ?: error("ashbrand_icon missing from cwd=${File(".").absolutePath}")
    }

    private fun md5(f: File): String =
        MessageDigest.getInstance("MD5")
            .digest(f.readBytes())
            .joinToString("") { "%02x".format(it) }

    /** Minimal JPEG SOF0/SOF2 dimension reader (no javax.imageio on Android unit classpath). */
    private fun jpegSize(bytes: ByteArray): Pair<Int, Int> {
        var i = 2
        while (i + 9 < bytes.size) {
            if (bytes[i] != 0xFF.toByte()) {
                i++
                continue
            }
            val marker = bytes[i + 1].toInt() and 0xFF
            if (marker == 0xD8 || marker == 0xD9 || marker == 0x01 || marker in 0xD0..0xD7) {
                i += 2
                continue
            }
            if (i + 3 >= bytes.size) break
            val segLen = ((bytes[i + 2].toInt() and 0xFF) shl 8) or (bytes[i + 3].toInt() and 0xFF)
            // SOF0 / SOF2
            if (marker == 0xC0 || marker == 0xC2) {
                val h = ((bytes[i + 5].toInt() and 0xFF) shl 8) or (bytes[i + 6].toInt() and 0xFF)
                val w = ((bytes[i + 7].toInt() and 0xFF) shl 8) or (bytes[i + 8].toInt() and 0xFF)
                return w to h
            }
            i += 2 + segLen
        }
        error("JPEG SOF not found")
    }

    @Test
    fun iconDrawable_notRejectedKnockout_isPortraitStill() {
        val f = iconFile()
        val hash = md5(f)
        assertNotEquals("must not ship rejected rembg knockout", REJECTED_KNOCKOUT_MD5, hash)
        assertEquals("portrait still (stage ashbrand_portrait.jpg)", PORTRAIT_STILL_MD5, hash)
        assertTrue(
            "file larger than rejected ~14KB crop (proxy fuller/pommel still present)",
            f.length() > REJECTED_SIZE_PROXY
        )
    }

    @Test
    fun iconDrawable_squareStill_dimensions() {
        val f = iconFile()
        assertTrue("expect JPEG portrait still", f.name.endsWith(".jpg"))
        val (w, h) = jpegSize(f.readBytes())
        assertEquals("square still", w, h)
        assertTrue("not the tiny 192 crop", w >= 512)
        assertEquals(1408, w)
        assertEquals(1408, h)
    }

    @Test
    fun drawableName_andSlotDp_unchanged() {
        assertEquals("ashbrand_icon", WakeArt.ICON_DRAWABLE)
        assertEquals(48, WakeArt.ICON_SLOT_DP)
        assertEquals("ashbrand_spark", WakeArt.SPARK_DRAWABLE)
    }

    @Test
    fun ashbrandGlossary_exactBody_tapPathFrozen() {
        val body = Glossary.definition("ashbrand")
        assertNotNull(body)
        assertEquals(Glossary.ASHBRAND_BODY, body)
        assertEquals(
            """
            Ember weapon. Each damaging skill
            you resolve adds 1 Spark pip.
            At 3 Sparks the next beat can
            fire Wake — a heavy slash.
            Wake spends the pips. A combat
            win plus a Wake this fight
            raises Ashbrand 1 level.
            Cinder Vow checks for ≥1 pip.
            """.trimIndent(),
            body
        )
    }

    @Test
    fun wakeMath_threshold_andDamageArrays_frozen() {
        val w = WeaponCatalog.ashbrand
        assertEquals("ashbrand", w.id)
        assertEquals(3, w.threshold(1))
        assertEquals(2, w.threshold(2))
        assertEquals(2, w.threshold(3))
        assertEquals(listOf(4, 6, 8), w.fullDamage.toList())
        assertEquals(listOf(2, 3, 4), w.sparkDamage.toList())
        assertEquals(4, w.fullDmg(1))
        assertEquals(6, w.fullDmg(2))
        assertEquals(8, w.fullDmg(3))
        assertEquals(2, w.sparkDmg(1))
        assertEquals(3, w.sparkDmg(2))
        assertEquals(4, w.sparkDmg(3))
    }

    @Test
    fun enemyKitSize_stillThree_smoke() {
        for (kind in listOf(EnemyKind.GOBLIN, EnemyKind.ORC, EnemyKind.DRAGON, EnemyKind.ASH_WARDEN)) {
            val kit = EnemyKits.skillsFor(kind)
            assertEquals("$kind kit size", 3, kit.size)
        }
    }
}
