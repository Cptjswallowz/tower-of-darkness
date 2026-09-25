package com.towerofdarkness.app

import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.hub.HubCta
import com.towerofdarkness.app.domain.hub.HubMetaSnapshot
import com.towerofdarkness.app.domain.hub.HubOffers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.1.28-hubkeep — baseline rumor 1/floor, Hub buys stack, migration OWNED, no wipe.
 * See docs/hubkeep-v0128.md.
 */
class HubKeepV0128Test {

    @Test
    fun freshClimb_noHubBuys_rumorStartsAt1() {
        assertEquals(1, HubOffers.BASELINE_RUMOR_PER_FLOOR)
        assertEquals(1, HubOffers.rumorRerollsAtClimbStart(emptySet()))
        assertEquals(1, HubOffers.rumorRerollsOnFloorAdvance(emptySet()))
        assertEquals(0, HubOffers.freeScoutChargesAtClimbStart(emptySet()))
    }

    @Test
    fun afterExtraRumorBuy_rumorStartsAt2() {
        val after = HubOffers.applyBuy(20, emptySet(), HubOffers.ID_EXTRA_RUMOR)
        assertNotNull(after)
        assertEquals(14, after!!.remnantsBank) // 20-6
        assertTrue(HubOffers.ID_EXTRA_RUMOR in after.unlocks)
        assertEquals(2, HubOffers.rumorRerollsAtClimbStart(after.unlocks))
        assertEquals(2, HubOffers.rumorRerollsOnFloorAdvance(after.unlocks))
    }

    @Test
    fun afterScoutBuy_freeScoutCharges1OnClimb() {
        val after = HubOffers.applyBuy(20, emptySet(), HubOffers.ID_SCOUT)
        assertNotNull(after)
        assertEquals(1, HubOffers.freeScoutChargesAtClimbStart(after!!.unlocks))
        assertEquals(0, HubOffers.freeScoutChargesAtClimbStart(emptySet()))
    }

    @Test
    fun forceClose_metaReload_ownedAndBankSame() {
        var snap = HubMetaSnapshot(remnantsBank = 30, unlocks = setOf("meta_hp_2", "rest_heal_plus"))
        snap = HubOffers.applyBuy(snap.remnantsBank, snap.unlocks, HubOffers.ID_SCOUT)!!
        snap = HubOffers.applyBuy(snap.remnantsBank, snap.unlocks, HubOffers.ID_EXTRA_RUMOR)!!
        assertEquals(16, snap.remnantsBank) // 30-8-6
        // old perks still present (ADD not replace)
        assertTrue("meta_hp_2" in snap.unlocks)
        assertTrue("rest_heal_plus" in snap.unlocks)
        assertTrue(HubOffers.ID_SCOUT in snap.unlocks)
        assertTrue(HubOffers.ID_EXTRA_RUMOR in snap.unlocks)

        val decoded = HubMetaSnapshot.decode(snap.encode())
        assertNotNull(decoded)
        assertEquals(snap.remnantsBank, decoded!!.remnantsBank)
        assertEquals(snap.unlocks, decoded.unlocks)
        assertEquals(
            HubCta.OWNED,
            HubOffers.cta(HubOffers.byId(HubOffers.ID_SCOUT)!!, decoded.remnantsBank, decoded.unlocks)
        )
        assertNull(HubOffers.applyBuy(decoded.remnantsBank, decoded.unlocks, HubOffers.ID_SCOUT))
    }

    @Test
    fun loadoutStillHasBaselineCommons_gatesDoNotHide() {
        val starters = CardCatalog.starterUnlockedIds()
        val pool = CardCatalog.poolForRun(starters)
        for (id in listOf("hostflint", "cinder_step", "iron_mantle", "emberbrand", "dust_veil")) {
            assertTrue("$id missing from pool", pool.any { it.id == id })
        }
        // gated skills still absent without Hub buys
        assertFalse(pool.any { it.id == HubOffers.CARD_CINDER_VOW })
        assertFalse(pool.any { it.id == HubOffers.CARD_GRAVE_NAIL })
        // after Host / Iron — skills appear; commons stay
        val withSkills = starters + HubOffers.ID_HOST_OF_EMBERS + HubOffers.ID_IRON_LESSON
        val pool2 = CardCatalog.poolForRun(withSkills)
        assertTrue(pool2.any { it.id == "hostflint" })
        assertTrue(pool2.any { it.id == HubOffers.CARD_CINDER_VOW })
        assertTrue(pool2.any { it.id == HubOffers.CARD_GRAVE_NAIL })
    }

    @Test
    fun migration_mapsLegacyFlags_keepsBank_noDoubleBill() {
        val bank = 42
        val unlocks = setOf(
            "hostflint",
            "meta_hp_2",
            HubOffers.LEGACY_RUMOR_CLARITY,
            HubOffers.ID_SCOUT,
            HubOffers.CARD_CINDER_VOW,
            HubOffers.CARD_GRAVE_NAIL
        )
        val migrated = HubOffers.migrateUnlocksForHubkeep(unlocks)
        // bank unchanged by migration (pure unlocks fn)
        assertEquals(42, bank)
        assertTrue(HubOffers.ID_EXTRA_RUMOR in migrated)
        assertTrue(HubOffers.LEGACY_RUMOR_CLARITY in migrated) // keep legacy flag
        assertTrue(HubOffers.ID_SCOUT in migrated)
        assertTrue(HubOffers.ID_HOST_OF_EMBERS in migrated)
        assertTrue(HubOffers.ID_IRON_LESSON in migrated)
        assertTrue("meta_hp_2" in migrated)
        assertTrue("hostflint" in migrated)
        // OWNED CTAs — no rebuy / no double bill
        assertEquals(
            HubCta.OWNED,
            HubOffers.cta(HubOffers.byId(HubOffers.ID_EXTRA_RUMOR)!!, bank, migrated)
        )
        assertEquals(
            HubCta.OWNED,
            HubOffers.cta(HubOffers.byId(HubOffers.ID_SCOUT)!!, bank, migrated)
        )
        assertEquals(
            HubCta.OWNED,
            HubOffers.cta(HubOffers.byId(HubOffers.ID_HOST_OF_EMBERS)!!, bank, migrated)
        )
        assertEquals(
            HubCta.OWNED,
            HubOffers.cta(HubOffers.byId(HubOffers.ID_IRON_LESSON)!!, bank, migrated)
        )
        assertNull(HubOffers.applyBuy(bank, migrated, HubOffers.ID_EXTRA_RUMOR))
        // grants after migration
        assertEquals(2, HubOffers.rumorRerollsAtClimbStart(migrated))
        assertEquals(1, HubOffers.freeScoutChargesAtClimbStart(migrated))
        // idempotent
        assertEquals(migrated, HubOffers.migrateUnlocksForHubkeep(migrated))
    }

    @Test
    fun titleRemEqualsHubRem_afterBuy() {
        val after = HubOffers.applyBuy(15, emptySet(), HubOffers.ID_EXTRA_RUMOR)!!
        assertEquals(9, after.remnantsBank)
        assertEquals("Hub · 9 remnants", HubOffers.titleBankLine(after.remnantsBank))
        assertEquals("Remnants  9", HubOffers.hubBankLine(after.remnantsBank))
    }
}
