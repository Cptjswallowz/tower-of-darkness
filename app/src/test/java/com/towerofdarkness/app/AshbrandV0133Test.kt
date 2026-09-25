package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.EnemyKits
import com.towerofdarkness.app.domain.combat.WakeArt
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.glossary.Glossary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.1.33-ashbrand — blade icon crop + tap glossary; Wake math frozen.
 * See docs/ashbrand-v0133.md.
 */
class AshbrandV0133Test {

    @Test
    fun drawableName_andSlotDp_unchanged() {
        assertEquals("ashbrand_icon", WakeArt.ICON_DRAWABLE)
        assertEquals(48, WakeArt.ICON_SLOT_DP)
        assertEquals("ashbrand_spark", WakeArt.SPARK_DRAWABLE)
    }

    @Test
    fun ashbrandGlossary_exactBody() {
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
    fun emberSparkWake_defsNonBlank() {
        for (key in listOf("ember", "spark", "wake")) {
            val def = Glossary.definition(key)
            assertNotNull("$key missing", def)
            assertTrue("$key blank", def!!.isNotBlank())
        }
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
