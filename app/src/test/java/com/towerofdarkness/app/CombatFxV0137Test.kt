package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.CombatFx
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.EnemyKitRole
import com.towerofdarkness.app.domain.combat.FxRole
import com.towerofdarkness.app.domain.combat.FxStrokeDir
import com.towerofdarkness.app.domain.combat.FxTier
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import com.towerofdarkness.app.domain.cards.CardCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * v0.1.37-fx — tier maps, NO STROKE, Wake exclusive, Wake Echo Medium, 2x FX holds, fail-safe.
 * See docs/fx-v0137.md.
 */
class CombatFxV0137Test {

    @Test
    fun playerSmallMap_matchesLock() {
        val expected = setOf(
            "hostflint", "cinder_step", "emberbrand", "dust_veil",
            "ember_draw", "brand_mark", "cinder_vow", "grave_nail", "ash_press"
        )
        assertEquals(expected, CombatFx.PLAYER_SMALL)
        expected.forEach { assertEquals(it, FxTier.SMALL, CombatFx.tierForPlayer(it)) }
    }

    @Test
    fun playerMediumMap_matchesLock() {
        val expected = setOf("tower_pike", "ruin_seal", "spark_tithe", "wake_echo")
        assertEquals(expected, CombatFx.PLAYER_MEDIUM)
        expected.forEach { assertEquals(it, FxTier.MEDIUM, CombatFx.tierForPlayer(it)) }
    }

    @Test
    fun playerNoStroke_ironMantle_vowPlate() {
        assertEquals(setOf("iron_mantle", "vow_plate"), CombatFx.PLAYER_NO_STROKE)
        assertEquals(FxTier.NO_STROKE, CombatFx.tierForPlayer("iron_mantle"))
        assertEquals(FxTier.NO_STROKE, CombatFx.tierForPlayer("vow_plate"))
        assertNull(CombatFx.specForPlayer("iron_mantle").stroke)
        assertNull(CombatFx.specForPlayer("vow_plate").stroke)
    }

    @Test
    fun enemyMaps_smallMediumNoStroke() {
        assertEquals(setOf("shiv", "nip", "hit"), CombatFx.ENEMY_SMALL)
        assertEquals(setOf("cleave", "seal_pulse", "coal_slam"), CombatFx.ENEMY_MEDIUM)
        assertEquals(setOf("hide", "rust_guard", "cinder_hide"), CombatFx.ENEMY_NO_STROKE)
        CombatFx.ENEMY_SMALL.forEach { assertEquals(it, FxTier.SMALL, CombatFx.tierForEnemy(it)) }
        CombatFx.ENEMY_MEDIUM.forEach { assertEquals(it, FxTier.MEDIUM, CombatFx.tierForEnemy(it)) }
        CombatFx.ENEMY_NO_STROKE.forEach {
            assertEquals(it, FxTier.NO_STROKE, CombatFx.tierForEnemy(it))
            assertNull(CombatFx.specForEnemy(it, EnemyKind.ORC).stroke)
        }
    }

    @Test
    fun wakeExclusive_onlyAshbrandWake() {
        assertTrue(CombatFx.isWakeExclusiveId(CombatFx.ID_ASHBRAND_WAKE))
        assertEquals(FxTier.WAKE, CombatFx.tierForPlayer(CombatFx.ID_ASHBRAND_WAKE))
        assertTrue(CombatFx.specForWake().useWakeSlash)
        assertNull(CombatFx.specForWake().stroke)
        assertTrue(CombatFx.specForWake().shake.amplitude >= CombatFx.SHAKE_WAKE_AMP)
        assertTrue(CombatFx.noPlayerCardUsesWakeTier())
        assertFalse(CombatFx.isWakeExclusiveId("wake_echo"))
        assertFalse(CombatFx.isWakeExclusiveId("hostflint"))
        CardCatalog.all.forEach { card ->
            assertTrue(
                "card ${card.id} must not be Wake tier",
                CombatFx.tierForPlayer(card.id) != FxTier.WAKE
            )
        }
    }

    @Test
    fun wakeEcho_singleMedium_notTwoStrokes() {
        assertTrue(CombatFx.wakeEchoIsSingleMedium())
        assertEquals(FxTier.MEDIUM, CombatFx.tierForPlayer("wake_echo"))
        // Engine emits one damage line with combined total
        val engine = CombatEngine(Random(1))
        val cards = listOf(
            CardCatalog.byId("wake_echo")!!,
            CardCatalog.byId("hostflint")!!,
            CardCatalog.byId("emberbrand")!!,
            CardCatalog.byId("ruin_seal")!!,
            CardCatalog.byId("tower_pike")!!
        )
        var s = engine.start(
            activeCards = cards,
            enemy = Enemy.normal(EnemyKind.ORC, floor = 1),
            weapon = WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0),
            maxHp = 30,
            playerHp = 30
        )
        // Mark Wake already fired this fight so echo bonus applies
        s = s.copy(fullProcThisCombat = true, highlightedId = "wake_echo", lastFiredCard = cards[0])
        s = engine.resolveSkill(s)
        val echoLines = s.log.filter { it.message.startsWith("Wake Echo deals") }
        assertEquals(1, echoLines.size)
        assertTrue(echoLines[0].message.contains("9")) // 5+4
        assertEquals("wake_echo", echoLines[0].fxId)
        assertEquals(FxTier.MEDIUM, CombatFx.tierForPlayer(echoLines[0].fxId!!))
    }

    @Test
    fun fxHoldMs_2x_halvesDurations() {
        assertEquals(CombatFx.FLASH_MS, CombatFx.fxHoldMs(CombatFx.FLASH_MS, 1))
        assertEquals(CombatFx.FLASH_MS / 2, CombatFx.fxHoldMs(CombatFx.FLASH_MS, 2))
        assertEquals(CombatFx.STROKE_MEDIUM_MS / 2, CombatFx.fxHoldMs(CombatFx.STROKE_MEDIUM_MS, 2))
        assertEquals(CombatFx.FLOAT_MS / 2, CombatFx.fxHoldMs(CombatFx.FLOAT_MS, 2))
        assertEquals(CombatFx.SHAKE_WAKE_MS / 2, CombatFx.fxHoldMs(CombatFx.SHAKE_WAKE_MS, 2))
        // clamp like combatHoldMs — no 3x
        assertEquals(CombatFx.fxHoldMs(CombatFx.FLOAT_MS, 2), CombatFx.fxHoldMs(CombatFx.FLOAT_MS, 99))
        assertEquals(CombatFx.FLOAT_MS, CombatFx.fxHoldMs(CombatFx.FLOAT_MS, 0))
    }

    @Test
    fun colors_byRole() {
        assertEquals(CombatFx.COLOR_YOU, CombatFx.colorArgb(FxRole.YOU))
        assertEquals(CombatFx.COLOR_WEAK_GOBLIN, CombatFx.colorArgb(FxRole.WEAK_GOBLIN))
        assertEquals(CombatFx.COLOR_STURDY_ORC, CombatFx.colorArgb(FxRole.STURDY_ORC))
        assertEquals(CombatFx.COLOR_SEAL_WARDEN, CombatFx.colorArgb(FxRole.SEAL_WARDEN))
        assertEquals(CombatFx.COLOR_ASH_WARDEN, CombatFx.colorArgb(FxRole.ASH_WARDEN))
        assertEquals(FxRole.WEAK_GOBLIN, CombatFx.roleForEnemy(EnemyKind.GOBLIN))
        assertEquals(FxRole.STURDY_ORC, CombatFx.roleForEnemy(EnemyKind.ORC))
        assertEquals(FxRole.SEAL_WARDEN, CombatFx.roleForEnemy(EnemyKind.DRAGON))
        assertEquals(FxRole.ASH_WARDEN, CombatFx.roleForEnemy(EnemyKind.ASH_WARDEN))
        assertEquals(FxRole.STURDY_ORC, CombatFx.roleForEnemy(EnemyKitRole.STURDY_ORC))
    }

    @Test
    fun enemyStroke_flipsDirection() {
        val player = CombatFx.specForPlayer("hostflint")
        assertEquals(FxStrokeDir.YOU_TO_FOE, player.stroke!!.dir)
        assertEquals(CombatFx.COLOR_YOU, player.stroke!!.colorArgb)
        val enemy = CombatFx.specForEnemy("shiv", EnemyKind.GOBLIN)
        assertEquals(FxStrokeDir.FOE_TO_YOU, enemy.stroke!!.dir)
        assertEquals(CombatFx.COLOR_WEAK_GOBLIN, enemy.stroke!!.colorArgb)
        val orc = CombatFx.specForEnemy("cleave", EnemyKind.ORC)
        assertEquals(CombatFx.COLOR_STURDY_ORC, orc.stroke!!.colorArgb)
        assertEquals(FxTier.MEDIUM, orc.tier)
    }

    @Test
    fun failSafe_nullFxId_and_safeSpec() {
        assertNull(CombatFx.safeSpec(null, true, EnemyKind.GOBLIN))
        assertNull(CombatFx.safeSpec("", true, null))
        val stub = CombatFx.safeSpec("unknown_card_xyz", true, null)
        assertNotNull(stub)
        assertEquals(FxTier.SMALL, stub!!.tier) // default SMALL
        val wake = CombatFx.safeSpec(CombatFx.ID_ASHBRAND_WAKE, true, null)
        assertEquals(FxTier.WAKE, wake!!.tier)
    }

    @Test
    fun engine_wiresFxId_onPrimaryResolve_notFollowUps() {
        val engine = CombatEngine(Random(42))
        val mantle = CardCatalog.byId("iron_mantle")!!
        val host = CardCatalog.byId("hostflint")!!
        val cards = listOf(mantle, host, CardCatalog.byId("emberbrand")!!,
            CardCatalog.byId("dust_veil")!!, CardCatalog.byId("cinder_step")!!)
        var s = engine.start(
            activeCards = cards,
            enemy = Enemy.normal(EnemyKind.GOBLIN, 1),
            weapon = WeaponRuntime(WeaponCatalog.ashbrand, 1, 0),
            maxHp = 30, playerHp = 30
        )
        s = s.copy(lastFiredCard = mantle, highlightedId = mantle.id)
        s = engine.resolveSkill(s)
        val braceEv = s.log.last { it.message.contains("Iron Mantle") }
        assertEquals("iron_mantle", braceEv.fxId)
        assertTrue(braceEv.fxPlayer)
        assertEquals(FxTier.NO_STROKE, CombatFx.tierForPlayer(braceEv.fxId!!))

        s = s.copy(lastFiredCard = host, highlightedId = host.id)
        val before = s.log.size
        s = engine.resolveSkill(s)
        val hit = s.log.drop(before).first { it.fxId == "hostflint" }
        assertEquals("hostflint", hit.fxId)
        assertTrue(hit.fxPlayer)
    }

    @Test
    fun engine_wakeEvent_hasAshbrandWakeFxId() {
        val engine = CombatEngine(Random(7))
        val cards = listOf(
            CardCatalog.byId("hostflint")!!, CardCatalog.byId("emberbrand")!!,
            CardCatalog.byId("ruin_seal")!!, CardCatalog.byId("tower_pike")!!,
            CardCatalog.byId("ash_press")!!
        )
        var s = engine.start(
            activeCards = cards,
            enemy = Enemy(EnemyKind.ORC, 99, 99),
            weapon = WeaponRuntime(WeaponCatalog.ashbrand, 1, charge = 3),
            maxHp = 30, playerHp = 30
        )
        // Force pending full wake
        s = s.copy(awaitingWeapon = true, pendingFullWake = true, pendingSpark = false)
        s = engine.resolveWeapon(s)
        val wake = s.log.last { it.message.startsWith("ASHBRAND — WAKE") }
        assertEquals(CombatFx.ID_ASHBRAND_WAKE, wake.fxId)
        assertTrue(wake.fxPlayer)
        assertEquals(FxTier.WAKE, CombatFx.safeSpec(wake.fxId, true, null)!!.tier)
    }

    @Test
    fun softenFollowUp_noFxId_brandMarkHitHasSmall() {
        val engine = CombatEngine(Random(3))
        val brand = CardCatalog.byId("brand_mark")!!
        val cards = listOf(
            brand, CardCatalog.byId("hostflint")!!, CardCatalog.byId("emberbrand")!!,
            CardCatalog.byId("ruin_seal")!!, CardCatalog.byId("tower_pike")!!
        )
        var s = engine.start(
            activeCards = cards,
            enemy = Enemy.normal(EnemyKind.ORC, 1),
            weapon = WeaponRuntime(WeaponCatalog.ashbrand, 1, charge = 1), // ≥1 spark before
            maxHp = 30, playerHp = 30
        )
        s = s.copy(lastFiredCard = brand, highlightedId = brand.id)
        s = engine.resolveSkill(s)
        val hit = s.log.first { it.message.startsWith("Brand Mark deals") }
        assertEquals("brand_mark", hit.fxId)
        assertEquals(FxTier.SMALL, CombatFx.tierForPlayer("brand_mark"))
        val soften = s.log.firstOrNull { it.message.contains("softened", ignoreCase = true) }
        assertNotNull(soften)
        assertNull(soften!!.fxId) // Soften apply = pip only — no extra slash
    }
}
