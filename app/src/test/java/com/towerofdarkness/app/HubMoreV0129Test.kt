package com.towerofdarkness.app

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
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
 * v0.1.29-hubmore — Hostblood / Warm Ash / Ash Tithe once-buy climb perks.
 * See docs/hubmore-v0129.md.
 */
class HubMoreV0129Test {

    @Test
    fun hubList_sevenOffers_costsLocked() {
        // v0.1.35: 11 offers; first 7 costs/ids unchanged
        assertEquals(11, HubOffers.all.size)
        assertEquals(listOf(8, 6, 12, 12, 10, 8, 8, 10, 10, 12, 12), HubOffers.all.map { it.cost })
        assertEquals(
            listOf(
                HubOffers.ID_SCOUT,
                HubOffers.ID_EXTRA_RUMOR,
                HubOffers.ID_HOST_OF_EMBERS,
                HubOffers.ID_IRON_LESSON,
                HubOffers.ID_HOSTBLOOD,
                HubOffers.ID_WARM_ASH,
                HubOffers.ID_ASH_TITHE,
                HubOffers.ID_COLD_DRAW,
                HubOffers.ID_BRAND_LESSON,
                HubOffers.ID_SPARK_LESSON,
                HubOffers.ID_ECHO_LESSON
            ),
            HubOffers.all.map { it.id }
        )
        assertEquals(listOf(8, 6, 12, 12, 10, 8, 8), HubOffers.all.take(7).map { it.cost })
    }

    @Test
    fun bank6_extraRumorBuyable_othersCantAfford() {
        val bank = 6
        val unlocks = emptySet<String>()
        assertEquals(
            HubCta.BUY,
            HubOffers.cta(HubOffers.byId(HubOffers.ID_EXTRA_RUMOR)!!, bank, unlocks)
        )
        for (id in listOf(
            HubOffers.ID_SCOUT,
            HubOffers.ID_HOST_OF_EMBERS,
            HubOffers.ID_IRON_LESSON,
            HubOffers.ID_HOSTBLOOD,
            HubOffers.ID_WARM_ASH,
            HubOffers.ID_ASH_TITHE
        )) {
            assertEquals(
                "$id should Can't afford at 6 rem",
                HubCta.CANT_AFFORD,
                HubOffers.cta(HubOffers.byId(id)!!, bank, unlocks)
            )
        }
    }

    @Test
    fun buyHostblood_climbMaxIs32_startFull_baseStays30() {
        assertEquals(30, Balance.PLAYER_MAX_HP)
        val after = HubOffers.applyBuy(20, emptySet(), HubOffers.ID_HOSTBLOOD)
        assertNotNull(after)
        assertEquals(10, after!!.remnantsBank) // 20-10
        assertTrue(HubOffers.ID_HOSTBLOOD in after.unlocks)
        assertEquals(2, HubOffers.hostbloodMaxBonus(after.unlocks))
        val climb = HubOffers.climbMaxHp(Balance.PLAYER_MAX_HP, metaHpBonus = 0, after.unlocks)
        assertEquals(32, climb)
        // Once — not a stack of ten
        assertNull(HubOffers.applyBuy(after.remnantsBank, after.unlocks, HubOffers.ID_HOSTBLOOD))
        assertEquals(
            HubCta.OWNED,
            HubOffers.cta(HubOffers.byId(HubOffers.ID_HOSTBLOOD)!!, 99, after.unlocks)
        )
        // Legacy metaHpBonus still stacks once Hostblood owned
        assertEquals(34, HubOffers.climbMaxHp(Balance.PLAYER_MAX_HP, metaHpBonus = 2, after.unlocks))
        // Without Hostblood, base unchanged
        assertEquals(30, HubOffers.climbMaxHp(Balance.PLAYER_MAX_HP, 0, emptySet()))
    }

    @Test
    fun ashPressHealCap_followsClimbMax_withHostblood() {
        val unlocks = setOf(HubOffers.ID_HOSTBLOOD)
        val maxHp = HubOffers.climbMaxHp(Balance.PLAYER_MAX_HP, 0, unlocks)
        assertEquals(32, maxHp)
        val ash = CardCatalog.byId("ash_press")!!
        val engine = CombatEngine(Random(1))
        // At base-max HP under Hostblood climb max: Ash Press +2 must reach 32 (not hard-cap 30)
        var s = engine.start(
            activeCards = listOf(ash, ash, ash, ash, ash),
            enemy = Enemy.normal(EnemyKind.GOBLIN, floor = 1),
            weapon = WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = maxHp,
            playerHp = 30,
            initialBrace = 0
        )
        assertEquals(32, s.playerMaxHp)
        assertEquals(30, s.playerHp)
        s = s.copy(lastFiredCard = ash, highlightedId = ash.id)
        s = engine.resolveSkill(s)
        assertEquals("Ash Press heal must follow climbMaxHp 32, not hard 30", 32, s.playerHp)
        assertEquals(32, s.playerMaxHp)
    }

    @Test
    fun buyWarmAsh_firstCombatShowsBrace2BeforeSkill() {
        val after = HubOffers.applyBuy(20, emptySet(), HubOffers.ID_WARM_ASH)!!
        assertEquals(12, after.remnantsBank) // 20-8
        assertEquals(2, HubOffers.warmAshBraceAtClimbStart(after.unlocks))
        assertEquals(0, HubOffers.warmAshBraceAtClimbStart(emptySet()))
        val maxHp = HubOffers.climbMaxHp(Balance.PLAYER_MAX_HP, 0, after.unlocks)
        val engine = CombatEngine()
        val s = engine.start(
            activeCards = CardCatalog.defaultLoadoutIds.mapNotNull { CardCatalog.byId(it) },
            enemy = Enemy.normal(EnemyKind.GOBLIN, floor = 1),
            weapon = WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = maxHp,
            playerHp = maxHp,
            initialBrace = HubOffers.warmAshBraceAtClimbStart(after.unlocks)
        )
        // Before first skill — Brace 2 present
        assertEquals(2, s.brace)
        assertNull(HubOffers.applyBuy(after.remnantsBank, after.unlocks, HubOffers.ID_WARM_ASH))
    }

    @Test
    fun buyAshTithe_summaryRemIncludePlus3_winOrDeath() {
        val after = HubOffers.applyBuy(20, emptySet(), HubOffers.ID_ASH_TITHE)!!
        assertEquals(12, after.remnantsBank)
        assertEquals(3, HubOffers.ashTitheBonus(after.unlocks))
        assertEquals(0, HubOffers.ashTitheBonus(emptySet()))
        val runWallet = 11
        fun banked(unlocks: Set<String>): Int = runWallet + HubOffers.ashTitheBonus(unlocks)
        assertEquals(14, banked(after.unlocks))
        assertEquals(11, banked(emptySet()))
        assertNull(HubOffers.applyBuy(after.remnantsBank, after.unlocks, HubOffers.ID_ASH_TITHE))
    }

    @Test
    fun oldFourStayOwned_bankNeverResets_newThreeAbsentUntilBought() {
        var snap = HubMetaSnapshot(
            remnantsBank = 40,
            unlocks = setOf(
                "meta_hp_2",
                "rest_heal_plus",
                HubOffers.ID_SCOUT,
                HubOffers.ID_EXTRA_RUMOR,
                HubOffers.ID_HOST_OF_EMBERS,
                HubOffers.ID_IRON_LESSON
            )
        )
        snap = HubOffers.applyBuy(snap.remnantsBank, snap.unlocks, HubOffers.ID_HOSTBLOOD)!!
        assertEquals(30, snap.remnantsBank) // 40-10
        for (id in listOf(
            HubOffers.ID_SCOUT,
            HubOffers.ID_EXTRA_RUMOR,
            HubOffers.ID_HOST_OF_EMBERS,
            HubOffers.ID_IRON_LESSON
        )) {
            assertTrue(id in snap.unlocks)
            assertEquals(
                HubCta.OWNED,
                HubOffers.cta(HubOffers.byId(id)!!, snap.remnantsBank, snap.unlocks)
            )
        }
        assertTrue("meta_hp_2" in snap.unlocks)
        assertTrue("rest_heal_plus" in snap.unlocks)
        assertEquals(
            HubCta.OWNED,
            HubOffers.cta(HubOffers.byId(HubOffers.ID_HOSTBLOOD)!!, snap.remnantsBank, snap.unlocks)
        )
        assertFalse(HubOffers.ID_WARM_ASH in snap.unlocks)
        assertFalse(HubOffers.ID_ASH_TITHE in snap.unlocks)
        assertEquals(
            HubCta.BUY,
            HubOffers.cta(HubOffers.byId(HubOffers.ID_WARM_ASH)!!, snap.remnantsBank, snap.unlocks)
        )
        // hubkeep migration does not auto-OWN Hostblood from meta_hp_2
        val migrated = HubOffers.migrateUnlocksForHubkeep(setOf("meta_hp_2", HubOffers.ID_SCOUT))
        assertFalse(HubOffers.ID_HOSTBLOOD in migrated)
        assertFalse(HubOffers.ID_WARM_ASH in migrated)
        assertFalse(HubOffers.ID_ASH_TITHE in migrated)
        assertTrue(HubOffers.ID_SCOUT in migrated)
        assertTrue("meta_hp_2" in migrated)
        val decoded = HubMetaSnapshot.decode(snap.encode())!!
        assertEquals(30, decoded.remnantsBank)
        assertEquals(snap.unlocks, decoded.unlocks)
    }

    @Test
    fun hostbloodDoesNotWriteMetaHpBonusKey_formulaOnly() {
        assertEquals(0, HubOffers.hostbloodMaxBonus(emptySet()))
        assertEquals(2, HubOffers.hostbloodMaxBonus(setOf(HubOffers.ID_HOSTBLOOD)))
        assertEquals(
            30,
            HubOffers.climbMaxHp(Balance.PLAYER_MAX_HP, metaHpBonus = 0, setOf("meta_hp_2"))
        )
        assertEquals(
            32,
            HubOffers.climbMaxHp(Balance.PLAYER_MAX_HP, metaHpBonus = 2, setOf("meta_hp_2"))
        )
    }
}
