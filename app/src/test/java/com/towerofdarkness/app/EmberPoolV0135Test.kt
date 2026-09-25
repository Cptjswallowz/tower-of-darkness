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
import com.towerofdarkness.app.domain.hub.HubOffers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * v0.1.35-emberpool — Hub Spark skills Ember Draw / Brand Mark / Spark Tithe / Wake Echo.
 * See docs/emberpool-v0135.md. Does not retune existing cards / Wake / kits.
 */
class EmberPoolV0135Test {

    private val emberIds = listOf(
        HubOffers.CARD_EMBER_DRAW,
        HubOffers.CARD_BRAND_MARK,
        HubOffers.CARD_SPARK_TITHE,
        HubOffers.CARD_WAKE_ECHO
    )

    @Test
    fun hubOffers_eleven_costsAndIdsLocked() {
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
        assertEquals(HubOffers.CARD_EMBER_DRAW, HubOffers.byId(HubOffers.ID_COLD_DRAW)!!.unlocksCardId)
        assertEquals(HubOffers.CARD_BRAND_MARK, HubOffers.byId(HubOffers.ID_BRAND_LESSON)!!.unlocksCardId)
        assertEquals(HubOffers.CARD_SPARK_TITHE, HubOffers.byId(HubOffers.ID_SPARK_LESSON)!!.unlocksCardId)
        assertEquals(HubOffers.CARD_WAKE_ECHO, HubOffers.byId(HubOffers.ID_ECHO_LESSON)!!.unlocksCardId)
        assertEquals("Cold Draw", HubOffers.byId(HubOffers.ID_COLD_DRAW)!!.title)
        assertEquals("Brand Lesson", HubOffers.byId(HubOffers.ID_BRAND_LESSON)!!.title)
        assertEquals("Spark Lesson", HubOffers.byId(HubOffers.ID_SPARK_LESSON)!!.title)
        assertEquals("Echo Lesson", HubOffers.byId(HubOffers.ID_ECHO_LESSON)!!.title)
    }

    @Test
    fun beforeBuy_noneOfFourInPool() {
        val starters = CardCatalog.starterUnlockedIds()
        val pool = CardCatalog.poolForRun(starters)
        for (id in emberIds) {
            assertFalse("$id must stay gated", pool.any { it.id == id })
            assertFalse(HubOffers.skillUnlocked(id, starters))
        }
    }

    @Test
    fun afterColdDrawOnly_emberDrawOnlyOfFourInPool() {
        val starters = CardCatalog.starterUnlockedIds()
        val after = HubOffers.applyBuy(20, starters, HubOffers.ID_COLD_DRAW)!!
        assertEquals(10, after.remnantsBank) // 20-10
        assertTrue(HubOffers.ID_COLD_DRAW in after.unlocks)
        val pool = CardCatalog.poolForRun(after.unlocks)
        assertTrue(pool.any { it.id == HubOffers.CARD_EMBER_DRAW })
        assertFalse(pool.any { it.id == HubOffers.CARD_BRAND_MARK })
        assertFalse(pool.any { it.id == HubOffers.CARD_SPARK_TITHE })
        assertFalse(pool.any { it.id == HubOffers.CARD_WAKE_ECHO })
    }

    @Test
    fun doubleBuy_coldDraw_spendsZero() {
        val after = HubOffers.applyBuy(20, emptySet(), HubOffers.ID_COLD_DRAW)!!
        assertEquals(10, after.remnantsBank)
        assertNull(HubOffers.applyBuy(after.remnantsBank, after.unlocks, HubOffers.ID_COLD_DRAW))
        assertEquals(
            HubCta.OWNED,
            HubOffers.cta(HubOffers.byId(HubOffers.ID_COLD_DRAW)!!, 99, after.unlocks)
        )
    }

    @Test
    fun zeroPips_emberDraw_brace2_andOnePip() {
        val card = CardCatalog.byId(HubOffers.CARD_EMBER_DRAW)!!
        assertEquals("Ember Draw", card.title)
        assertEquals(3, card.weight)
        val engine = CombatEngine(Random(35))
        val enemy = Enemy.normal(EnemyKind.GOBLIN, floor = 1)
        var s = engine.start(
            listOf(card),
            enemy,
            WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0),
            maxHp = 30,
            playerHp = 30
        )
        s = engine.diceTumble(s)
        s = engine.resolveSkill(s)
        assertEquals(2, s.brace)
        assertEquals(1, s.weapon.charge)
        assertEquals(enemy.hp - 4, s.enemy.hp)
    }

    @Test
    fun onePip_brandMark_soften2_charge1to2() {
        val card = CardCatalog.byId(HubOffers.CARD_BRAND_MARK)!!
        assertEquals("Brand Mark", card.title)
        val engine = CombatEngine(Random(35))
        val enemy = Enemy.normal(EnemyKind.GOBLIN, floor = 1)
        var s = engine.start(
            listOf(card),
            enemy,
            WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 1),
            maxHp = 30,
            playerHp = 30
        )
        s = engine.diceTumble(s)
        s = engine.resolveSkill(s)
        assertEquals(2, s.counterPenalty)
        assertEquals(2, s.weapon.charge)
        assertEquals(enemy.hp - 3, s.enemy.hp)
    }

    @Test
    fun onePip_sparkTithe_logsSparkSpent_pipsBackTo1() {
        val card = CardCatalog.byId(HubOffers.CARD_SPARK_TITHE)!!
        assertEquals("Spark Tithe", card.title)
        val engine = CombatEngine(Random(35))
        val enemy = Enemy.normal(EnemyKind.GOBLIN, floor = 1)
        var s = engine.start(
            listOf(card),
            enemy,
            WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 1),
            maxHp = 30,
            playerHp = 30
        )
        s = engine.diceTumble(s)
        s = engine.resolveSkill(s)
        assertTrue(s.log.any { it.message.contains("Spark spent") })
        assertEquals(1, s.weapon.charge)
        assertEquals(enemy.hp - 6, s.enemy.hp)
    }

    @Test
    fun wakeAlreadyFired_wakeEcho_deals9_chargePlusOneOnly() {
        val card = CardCatalog.byId(HubOffers.CARD_WAKE_ECHO)!!
        assertEquals("Wake Echo", card.title)
        val engine = CombatEngine(Random(35))
        val enemy = Enemy.normal(EnemyKind.GOBLIN, floor = 1)
        var s = engine.start(
            listOf(card),
            enemy,
            WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0),
            maxHp = 30,
            playerHp = 30
        )
        s = s.copy(fullProcThisCombat = true)
        s = engine.diceTumble(s)
        val chargeBefore = s.weapon.charge
        s = engine.resolveSkill(s)
        assertTrue(
            "expected Wake Echo deals 9, got: ${s.log.map { it.message }}",
            s.log.any { it.message.contains("Wake Echo deals 9") }
        )
        assertEquals(chargeBefore + 1, s.weapon.charge)
        assertEquals(enemy.hp - 9, s.enemy.hp)
    }

    @Test
    fun ironMantle_and_hostflint_unchanged() {
        val mantle = CardCatalog.byId("iron_mantle")!!
        val host = CardCatalog.byId("hostflint")!!
        val engine = CombatEngine(Random(35))
        val enemy = Enemy.normal(EnemyKind.GOBLIN, floor = 1)

        var sMantle = engine.start(
            listOf(mantle),
            enemy,
            WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0),
            maxHp = 30,
            playerHp = 30
        )
        sMantle = engine.diceTumble(sMantle)
        sMantle = engine.resolveSkill(sMantle)
        assertEquals(3, sMantle.brace)
        assertEquals(0, sMantle.weapon.charge) // equipment is not an attack

        var sHost = engine.start(
            listOf(host),
            enemy,
            WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0),
            maxHp = 30,
            playerHp = 30
        )
        sHost = engine.diceTumble(sHost)
        sHost = engine.resolveSkill(sHost)
        assertEquals(enemy.hp - 5, sHost.enemy.hp)
        assertEquals(1, sHost.weapon.charge)
        assertEquals(0, sHost.brace)
    }

    @Test
    fun glyphs_and_catalogMapped() {
        for (id in emberIds) {
            assertTrue(SkillGlyph.hasGlyph(id))
            assertEquals(SkillJob.DAMAGE, SkillGlyph.job(id))
        }
        assertEquals("glyph_ember_draw", SkillGlyph.drawableName(HubOffers.CARD_EMBER_DRAW))
        assertEquals("glyph_brand_mark", SkillGlyph.drawableName(HubOffers.CARD_BRAND_MARK))
        assertEquals("glyph_spark_tithe", SkillGlyph.drawableName(HubOffers.CARD_SPARK_TITHE))
        assertEquals("glyph_wake_echo", SkillGlyph.drawableName(HubOffers.CARD_WAKE_ECHO))
        assertTrue(SkillGlyph.catalogFullyMapped())
        assertEquals(17, CardCatalog.all.size)
        for (name in listOf("glyph_ember_draw", "glyph_brand_mark", "glyph_spark_tithe", "glyph_wake_echo")) {
            val f = listOf(
                java.io.File("app/src/main/res/drawable/$name.png"),
                java.io.File("src/main/res/drawable/$name.png")
            ).firstOrNull { it.isFile }
            assertNotNull("$name missing", f)
            assertTrue(f!!.length() > 100)
        }
    }

    @Test
    fun cards_uncommon_w3_titlesMatch() {
        assertEquals("Ember Draw", CardCatalog.byId("ember_draw")!!.title)
        assertEquals("Brand Mark", CardCatalog.byId("brand_mark")!!.title)
        assertEquals("Spark Tithe", CardCatalog.byId("spark_tithe")!!.title)
        assertEquals("Wake Echo", CardCatalog.byId("wake_echo")!!.title)
        for (id in emberIds) {
            val c = CardCatalog.byId(id)!!
            assertEquals(3, c.weight)
            assertEquals(com.towerofdarkness.app.domain.Rarity.UNCOMMON, c.rarity)
        }
    }
}
