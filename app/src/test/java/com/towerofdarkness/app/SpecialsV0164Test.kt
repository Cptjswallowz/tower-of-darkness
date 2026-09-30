package com.towerofdarkness.app

import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import com.towerofdarkness.app.domain.specials.Specials
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

/**
 * v0.1.64-specials P1 — loadout gate, bag excludes L/M, MYTHIC, Grave Brand,
 * Ash Vow once, Assist default ON, vc 65.
 */
class SpecialsV0164Test {

    private fun gradleSrc(): String {
        val a = File("build.gradle.kts")
        val b = File("app/build.gradle.kts")
        return when {
            a.isFile && a.readText().contains("versionCode") -> a.readText()
            else -> b.readText()
        }
    }

    private fun tank() = Enemy(EnemyKind.ORC, maxHp = 500, hp = 500, isBoss = false)

    private fun specialsLoadout() = listOf(
        "hostflint", "cinder_step", "iron_mantle", "grave_brand", "ash_vow"
    ).mapNotNull { CardCatalog.byId(it) }

    @Test
    fun packaging_vc65_vnSpecials() {
        val g = gradleSrc()
        assertTrue(g.contains("versionCode = 65"))
        assertTrue(g.contains("versionName = \"0.1.64-specials\""))
    }

    @Test
    fun rarity_hasMythic() {
        assertEquals("Mythic", Rarity.MYTHIC.displayName)
        assertTrue(Rarity.entries.any { it == Rarity.MYTHIC })
    }

    @Test
    fun catalog_hasGraveBrandAndAshVow() {
        val gb = CardCatalog.byId(Specials.ID_GRAVE_BRAND)
        val av = CardCatalog.byId(Specials.ID_ASH_VOW)
        assertNotNull(gb)
        assertNotNull(av)
        assertEquals(Rarity.LEGENDARY, gb!!.rarity)
        assertEquals(Rarity.MYTHIC, av!!.rarity)
        assertEquals(0, gb.weight)
        assertEquals(0, av.weight)
    }

    @Test
    fun loadoutGate_requires3RegularPlusLPlusM() {
        val ok = specialsLoadout()
        assertTrue(Specials.loadoutMeetsGate(ok))
        assertEquals(null, Specials.loadoutGateReason(ok))

        val fiveRegs = listOf(
            "hostflint", "cinder_step", "iron_mantle", "emberbrand", "dust_veil"
        ).mapNotNull { CardCatalog.byId(it) }
        assertFalse(Specials.loadoutMeetsGate(fiveRegs))
        assertEquals(Specials.LOADOUT_REASON, Specials.loadoutGateReason(fiveRegs))
        assertEquals(
            "Need 3 regulars + 1 Legendary + 1 Mythic.",
            Specials.LOADOUT_REASON
        )
    }

    @Test
    fun grantClimbSpecials_addsBothWhenMissing() {
        val base = setOf("hostflint")
        val granted = Specials.grantClimbSpecialsIfMissing(base)
        assertTrue(Specials.ID_GRAVE_BRAND in granted)
        assertTrue(Specials.ID_ASH_VOW in granted)
        val both = base + Specials.ID_GRAVE_BRAND + Specials.ID_ASH_VOW
        assertEquals(both, Specials.grantClimbSpecialsIfMissing(both))
    }

    @Test
    fun diceBag_excludesLegendaryAndMythic() {
        val cards = specialsLoadout()
        val bag = Specials.bagCards(cards)
        assertEquals(3, bag.size)
        assertTrue(bag.all { Specials.isRegular(it) })
        assertFalse(bag.any { it.id == Specials.ID_GRAVE_BRAND })
        assertFalse(bag.any { it.id == Specials.ID_ASH_VOW })

        val engine = CombatEngine(Random(1))
        var s = engine.start(cards, tank(), WeaponRuntime(WeaponCatalog.ashbrand), maxHp = 99, playerHp = 99)
        repeat(3) {
            s = engine.diceTumble(s)
            val hid = s.highlightedId
            assertNotNull(hid)
            assertTrue("bag must not roll L/M", hid in bag.map { it.id })
            s = engine.resolveSkill(s)
            if (s.awaitingWeapon && !s.awaitingWakeTap) s = engine.resolveWeapon(s)
            if (!s.finished) {
                s = engine.resolveEnemy(s)
                s = engine.readyNext(s)
            }
        }
        // After 3 regulars spent → cycle reset
        s = engine.diceTumble(s)
        assertTrue(s.log.any { it.message == "Cycle reset" })
    }

    @Test
    fun graveBrand_chargesOnRegularResolve_capsAt3() {
        val cards = specialsLoadout()
        val engine = CombatEngine(Random(2))
        var s = engine.start(
            cards, tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, assistSpecials = false
        )
        assertEquals(0, s.graveBrandCharge)
        repeat(3) {
            s = engine.diceTumble(s)
            s = engine.resolveSkill(s)
            if (s.awaitingWeapon && !s.awaitingWakeTap) {
                s = engine.resolveWeapon(s, wakeViaAssist = false)
            } else if (s.awaitingWakeTap) {
                s = engine.resolveWeapon(s, wakeViaAssist = false)
            }
            if (!s.finished && s.enemy.hp > 0) {
                s = engine.resolveEnemy(s)
                s = engine.readyNext(s)
            }
        }
        assertEquals(3, s.graveBrandCharge)
        s = engine.fireGraveBrand(s, viaAssist = false)
        assertEquals(0, s.graveBrandCharge)
        assertTrue(s.log.any { it.message == "Grave Brand (tap)" })
    }

    @Test
    fun ashVow_oncePerFight() {
        val cards = specialsLoadout()
        val engine = CombatEngine(Random(3))
        var s = engine.start(
            cards, tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, assistSpecials = false
        )
        assertFalse(s.ashVowSpent)
        s = engine.fireAshVow(s, viaAssist = false)
        assertTrue(s.ashVowSpent)
        assertTrue(s.ashVowBonusPending)
        assertEquals(4, s.brace)
        assertTrue(s.log.any { it.message == "Ash Vow (tap)" })
        val braceBefore = s.brace
        s = engine.fireAshVow(s, viaAssist = false)
        assertEquals("second fire is no-op", braceBefore, s.brace)
    }

    @Test
    fun assistSpecials_defaultOn_inMetaStoreSource() {
        val meta = File(
            "src/main/java/com/towerofdarkness/app/data/MetaStore.kt"
        ).let { if (it.isFile) it else File("app/src/main/java/com/towerofdarkness/app/data/MetaStore.kt") }
            .readText()
        assertTrue(meta.contains("KEY_ASSIST_SPECIALS"))
        assertTrue(
            meta.contains("[KEY_ASSIST_SPECIALS] ?: true") ||
                meta.contains("KEY_ASSIST_SPECIALS] ?: true")
        )
    }

    @Test
    fun defaultLoadout_meetsGate() {
        val ids = CardCatalog.defaultLoadoutIds
        assertEquals(5, ids.size)
        val cards = ids.mapNotNull { CardCatalog.byId(it) }
        assertTrue(Specials.loadoutMeetsGate(cards))
    }
}
