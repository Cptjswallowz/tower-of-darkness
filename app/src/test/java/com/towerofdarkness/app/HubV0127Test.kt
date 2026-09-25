package com.towerofdarkness.app

import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import com.towerofdarkness.app.domain.glyphs.SkillGlyph
import com.towerofdarkness.app.domain.glyphs.SkillJob
import com.towerofdarkness.app.domain.hub.HubCta
import com.towerofdarkness.app.domain.hub.HubMetaSnapshot
import com.towerofdarkness.app.domain.hub.HubOffers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * v0.1.27-hub — Hub offers, remnant spend, Scout / Extra rumor grants,
 * Cinder Vow / Grave Nail loadout gates, meta_v0 bank+unlocks persist.
 */
class HubV0127Test {

    @Test
    fun titleAndHubBankWording() {
        assertEquals("Hub", HubOffers.hubScreenTitle())
        assertEquals("Hub · 0 remnants", HubOffers.titleBankLine(0))
        assertEquals("Hub · 12 remnants", HubOffers.titleBankLine(12))
        assertEquals("Remnants  0", HubOffers.hubBankLine(0))
        assertEquals("Remnants  7", HubOffers.hubBankLine(7))
        assertEquals(4, HubOffers.all.size)
        assertEquals(listOf(8, 6, 12, 12), HubOffers.all.map { it.cost })
        assertEquals(
            listOf("scout_charge", "extra_rumor", "host_of_embers", "iron_lesson"),
            HubOffers.all.map { it.id }
        )
    }

    @Test
    fun scoutBuy_nextClimbFreeScoutPlusOne() {
        val after = HubOffers.applyBuy(bank = 20, unlocks = emptySet(), offerId = HubOffers.ID_SCOUT)
        assertNotNull(after)
        assertEquals(12, after!!.remnantsBank)
        assertTrue(HubOffers.ID_SCOUT in after.unlocks)
        assertEquals(1, HubOffers.freeScoutChargesAtClimbStart(after.unlocks))
        assertEquals(0, HubOffers.freeScoutChargesAtClimbStart(emptySet()))
    }

    @Test
    fun extraRumor_stacksOnBaseline_perFloor() {
        // v0.1.28-hubkeep: baseline 1 + Extra → 2; bare unlocks → baseline 1
        assertEquals(1, HubOffers.rumorRerollsAtClimbStart(emptySet()))
        assertEquals(1, HubOffers.rumorRerollsOnFloorAdvance(emptySet()))
        val owned = setOf(HubOffers.ID_EXTRA_RUMOR)
        assertEquals(2, HubOffers.rumorRerollsAtClimbStart(owned))
        assertEquals(2, HubOffers.rumorRerollsOnFloorAdvance(owned))
        // legacy Clear Fog alone (pre-migration): still baseline only until migrate maps → Extra
        val legacy = setOf("rumor_clarity")
        assertEquals(1, HubOffers.rumorRerollsAtClimbStart(legacy))
        val migrated = HubOffers.migrateUnlocksForHubkeep(legacy)
        assertEquals(2, HubOffers.rumorRerollsAtClimbStart(migrated))
        assertEquals(2, HubOffers.rumorRerollsOnFloorAdvance(migrated))
    }

    @Test
    fun cinderUnlockGate_notInLoadoutUntilHostOfEmbers() {
        val starters = CardCatalog.starterUnlockedIds()
        val before = CardCatalog.poolForRun(starters)
        assertFalse(before.any { it.id == HubOffers.CARD_CINDER_VOW })
        assertFalse(before.any { it.id == HubOffers.CARD_GRAVE_NAIL })
        assertNull(CardCatalog.byId(HubOffers.CARD_CINDER_VOW)?.let {
            if (HubOffers.skillUnlocked(it.id, starters)) it else null
        })

        val afterHost = starters + HubOffers.ID_HOST_OF_EMBERS
        val poolHost = CardCatalog.poolForRun(afterHost)
        assertTrue(poolHost.any { it.id == HubOffers.CARD_CINDER_VOW })
        assertFalse(poolHost.any { it.id == HubOffers.CARD_GRAVE_NAIL })

        val afterIron = starters + HubOffers.ID_IRON_LESSON
        val poolIron = CardCatalog.poolForRun(afterIron)
        assertTrue(poolIron.any { it.id == HubOffers.CARD_GRAVE_NAIL })
        assertFalse(poolIron.any { it.id == HubOffers.CARD_CINDER_VOW })
    }

    @Test
    fun zeroRem_cantAfford_noSpend() {
        for (offer in HubOffers.all) {
            assertEquals(HubCta.CANT_AFFORD, HubOffers.cta(offer, bank = 0, owned = emptySet()))
            assertEquals("Can't afford", HubOffers.ctaLabel(HubCta.CANT_AFFORD))
            assertNull(HubOffers.applyBuy(0, emptySet(), offer.id))
        }
        // partial: 5 rem can't buy Scout (8) but Extra rumor (6) still blocked at 5
        assertNull(HubOffers.applyBuy(5, emptySet(), HubOffers.ID_SCOUT))
        assertNull(HubOffers.applyBuy(5, emptySet(), HubOffers.ID_EXTRA_RUMOR))
        val buyRumor = HubOffers.applyBuy(6, emptySet(), HubOffers.ID_EXTRA_RUMOR)
        assertNotNull(buyRumor)
        assertEquals(0, buyRumor!!.remnantsBank)
    }

    @Test
    fun killAppPersist_ownedAndBank_roundTrip() {
        var snap = HubMetaSnapshot(remnantsBank = 40, unlocks = emptySet())
        snap = HubOffers.applyBuy(snap.remnantsBank, snap.unlocks, HubOffers.ID_SCOUT)!!
        snap = HubOffers.applyBuy(snap.remnantsBank, snap.unlocks, HubOffers.ID_HOST_OF_EMBERS)!!
        assertEquals(20, snap.remnantsBank) // 40-8-12
        assertTrue(HubOffers.ID_SCOUT in snap.unlocks)
        assertTrue(HubOffers.ID_HOST_OF_EMBERS in snap.unlocks)
        // owned → no rebuy
        assertEquals(
            HubCta.OWNED,
            HubOffers.cta(HubOffers.byId(HubOffers.ID_SCOUT)!!, snap.remnantsBank, snap.unlocks)
        )
        assertNull(HubOffers.applyBuy(snap.remnantsBank, snap.unlocks, HubOffers.ID_SCOUT))

        val encoded = snap.encode()
        val decoded = HubMetaSnapshot.decode(encoded)
        assertNotNull(decoded)
        assertEquals(snap.remnantsBank, decoded!!.remnantsBank)
        assertEquals(snap.unlocks, decoded.unlocks)
        // pool still gated after "kill-app" restore
        assertTrue(
            CardCatalog.poolForRun(decoded.unlocks + CardCatalog.starterUnlockedIds())
                .any { it.id == HubOffers.CARD_CINDER_VOW }
        )
    }

    @Test
    fun cinderVow_braceOnlyWhenAshbrandHasPip() {
        val card = CardCatalog.byId(HubOffers.CARD_CINDER_VOW)!!
        val engine = CombatEngine(Random(27))
        val enemy = Enemy.normal(EnemyKind.GOBLIN)
        val noPip = WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0)
        var s = engine.start(listOf(card), enemy, noPip, maxHp = 30, playerHp = 30)
        s = engine.diceTumble(s)
        s = engine.resolveSkill(s)
        assertEquals(0, s.brace)
        assertEquals(enemy.hp - 5, s.enemy.hp)

        val withPip = WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 1)
        var s2 = engine.start(listOf(card), enemy, withPip, maxHp = 30, playerHp = 30)
        s2 = engine.diceTumble(s2)
        s2 = engine.resolveSkill(s2)
        assertEquals(2, s2.brace)
    }

    @Test
    fun graveNail_soften1() {
        val card = CardCatalog.byId(HubOffers.CARD_GRAVE_NAIL)!!
        val engine = CombatEngine(Random(27))
        val enemy = Enemy.normal(EnemyKind.GOBLIN)
        val w = WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0)
        var s = engine.start(listOf(card), enemy, w, maxHp = 30, playerHp = 30)
        s = engine.diceTumble(s)
        s = engine.resolveSkill(s)
        assertEquals(1, s.counterPenalty)
        assertEquals(enemy.hp - 4, s.enemy.hp)
    }

    @Test
    fun glyphs_artWired_cinderVowAndGraveNail() {
        assertEquals("glyph_cinder_vow", SkillGlyph.drawableName(HubOffers.CARD_CINDER_VOW))
        assertEquals("glyph_grave_nail", SkillGlyph.drawableName(HubOffers.CARD_GRAVE_NAIL))
        assertEquals(SkillJob.DAMAGE, SkillGlyph.job(HubOffers.CARD_CINDER_VOW))
        assertEquals(SkillJob.DAMAGE, SkillGlyph.job(HubOffers.CARD_GRAVE_NAIL))
        assertTrue(SkillGlyph.hasGlyph(HubOffers.CARD_CINDER_VOW))
        assertTrue(SkillGlyph.hasGlyph(HubOffers.CARD_GRAVE_NAIL))
        // Art PNGs on disk
        for (name in listOf("glyph_cinder_vow", "glyph_grave_nail")) {
            val f = listOf(
                java.io.File("app/src/main/res/drawable/$name.png"),
                java.io.File("src/main/res/drawable/$name.png")
            ).firstOrNull { it.isFile }
            assertNotNull("$name missing", f)
            assertTrue(f!!.length() > 100)
        }
    }
}
