package com.towerofdarkness.app

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatBeat
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.WakeArt
import com.towerofdarkness.app.domain.combat.WakeIconPhase
import com.towerofdarkness.app.domain.combat.WakeStageFrame
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * v0.1.17-wakespeck — staggered gold/ash dots + 1-frame impact burst on Full Wake;
 * SPARK clean; icon/stroke/math/2x frozen.
 */
class WakeArtV0117Test {

    private fun tank() = Enemy(EnemyKind.ORC, maxHp = 500, hp = 500, isBoss = false)
    private fun weapon(charge: Int = 0) =
        WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = charge)

    private fun afterFullWake(): com.towerofdarkness.app.domain.combat.CombatState {
        val engine = CombatEngine(Random(1))
        var s = engine.start(
            CardCatalog.defaultLoadoutIds.mapNotNull { CardCatalog.byId(it) },
            tank(),
            weapon(charge = 3),
            maxHp = 99,
            playerHp = 99
        )
        s = s.copy(
            awaitingWeapon = true,
            pendingFullWake = true,
            pendingSpark = false,
            weapon = weapon(3)
        )
        return engine.resolveWeapon(s)
    }

    private fun afterSparkOnly(): com.towerofdarkness.app.domain.combat.CombatState {
        val engine = CombatEngine(Random(1))
        var s = engine.start(
            CardCatalog.defaultLoadoutIds.mapNotNull { CardCatalog.byId(it) },
            tank(),
            weapon(charge = 1),
            maxHp = 99,
            playerHp = 99
        )
        s = s.copy(
            awaitingWeapon = true,
            pendingFullWake = false,
            pendingSpark = true,
            weapon = weapon(1)
        )
        return engine.resolveWeapon(s)
    }

    @Test
    fun drawables_dropInNames_iconSparkFrozen() {
        assertEquals("ashbrand_icon", WakeArt.ICON_DRAWABLE)
        assertEquals("ashbrand_spark", WakeArt.SPARK_DRAWABLE)
        assertEquals("wake_vfx_charge", WakeArt.VFX_CHARGE)
        assertEquals("wake_vfx_slash", WakeArt.VFX_SLASH)
        assertEquals("wake_vfx_impact", WakeArt.VFX_IMPACT)
        assertEquals(48, WakeArt.ICON_SLOT_DP)
    }

    @Test
    fun fullWake_staggeredDotsAndImpactBurst() {
        assertEquals(8, WakeArt.ASH_DOTS_MIN)
        assertEquals(12, WakeArt.ASH_DOTS_MAX)
        assertTrue(WakeArt.SPECK_DOTS_STAGGERED)
        assertTrue(WakeArt.IMPACT_IS_SPARK_BURST)
        assertEquals(8, WakeArt.IMPACT_BURST_PX_MIN)
        assertEquals(12, WakeArt.IMPACT_BURST_PX_MAX)
        // Stroke frozen — no retune this pass
        assertEquals(2, WakeArt.STROKE_THICKNESS_MULT_MIN)
        assertEquals(3, WakeArt.STROKE_THICKNESS_MULT_MAX)
        assertEquals(
            listOf(WakeStageFrame.CHARGE, WakeStageFrame.SLASH, WakeStageFrame.IMPACT),
            WakeArt.stageSequence().map { it.frame }
        )
        val s = afterFullWake()
        assertTrue(WakeArt.showCrescent(s))
        assertEquals(WakeStageFrame.CHARGE, WakeArt.stageFrame(s, 0L, 1))
        assertEquals(WakeStageFrame.SLASH, WakeArt.stageFrame(s, 400L, 1))
        assertEquals(WakeStageFrame.IMPACT, WakeArt.stageFrame(s, 900L, 1))
        assertEquals(WakeStageFrame.NONE, WakeArt.stageFrame(s, 1300L, 1))
    }

    @Test
    fun spark_noCrescentDotsOrBurst_emberOnly() {
        assertFalse(WakeArt.SPARK_HAS_CRESCENT_DOTS_OR_BURST)
        val s = afterSparkOnly()
        assertFalse(WakeArt.showCrescent(s))
        assertTrue(WakeArt.showSparkEmber(s))
        assertEquals(WakeIconPhase.SPARK_EMBER, WakeArt.iconPhase(s, 0L, 1))
        assertEquals(WakeStageFrame.NONE, WakeArt.stageFrame(s, 0L, 1))
        assertEquals(WakeStageFrame.NONE, WakeArt.stageFrame(s, 400L, 1))
        assertEquals(WakeStageFrame.NONE, WakeArt.stageFrame(s, 900L, 1))
        assertTrue(s.log.none { it.goldLog })
    }

    @Test
    fun frozen_mathAnd2xHolds_unchanged() {
        assertEquals(3, WeaponCatalog.ashbrand.threshold(1))
        assertEquals(4, WeaponCatalog.ashbrand.fullDmg(1))
        assertEquals(2, WeaponCatalog.ashbrand.sparkDmg(1))
        assertEquals(2300L, WakeArt.fullWakeHold1x())
        assertEquals(1150L, WakeArt.fullWakeHold2x())
        assertEquals(Balance.WEAPON_FULL_HOLD_MS, WakeArt.fullWakeHold1x())
        assertTrue(WakeArt.stageSequenceDuration1x() <= Balance.WEAPON_FULL_HOLD_MS)
        // Frame budgets unchanged (no extra beat timing)
        assertEquals(400L, WakeArt.FRAME_CHARGE_MS)
        assertEquals(500L, WakeArt.FRAME_SLASH_MS)
        assertEquals(400L, WakeArt.FRAME_IMPACT_MS)
        val s = afterFullWake()
        assertEquals(WakeStageFrame.CHARGE, WakeArt.stageFrame(s, 0L, 2))
        assertEquals(WakeStageFrame.SLASH, WakeArt.stageFrame(s, 200L, 2))
        assertEquals(WakeStageFrame.IMPACT, WakeArt.stageFrame(s, 450L, 2))
        assertEquals(WakeStageFrame.NONE, WakeArt.stageFrame(s, 650L, 2))
        assertEquals(CombatBeat.AFTER_WEAPON, s.beat)
        assertTrue(WakeArt.WAKE_ART_FULLY_FROZEN)
    }
}
