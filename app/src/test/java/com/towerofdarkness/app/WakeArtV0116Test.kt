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
 * v0.1.16-wakeicon — cracked blade polish; thick arc + ash dots; impact spark burst;
 * SPARK no crescent; 2x same frames half duration. Wake math frozen.
 */
class WakeArtV0116Test {

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
    fun drawables_dropInNames_unchanged() {
        assertEquals("ashbrand_icon", WakeArt.ICON_DRAWABLE)
        assertEquals("ashbrand_spark", WakeArt.SPARK_DRAWABLE)
        assertEquals("wake_vfx_charge", WakeArt.VFX_CHARGE)
        assertEquals("wake_vfx_slash", WakeArt.VFX_SLASH)
        assertEquals("wake_vfx_impact", WakeArt.VFX_IMPACT)
    }

    @Test
    fun icon_crackedBlade_same48dpSlot_loadoutAndCombat() {
        assertEquals(48, WakeArt.ICON_SLOT_DP)
    }

    @Test
    fun crescentPolish_thickStrokeAndAshDots_impactIsBurst() {
        assertEquals(2, WakeArt.STROKE_THICKNESS_MULT_MIN)
        assertEquals(3, WakeArt.STROKE_THICKNESS_MULT_MAX)
        assertEquals(8, WakeArt.ASH_DOTS_MIN)
        assertEquals(12, WakeArt.ASH_DOTS_MAX)
        assertTrue(WakeArt.IMPACT_IS_SPARK_BURST)
        // Same three frames — impact stays IMPACT (burst art), not a second slash
        assertEquals(
            listOf(WakeStageFrame.CHARGE, WakeStageFrame.SLASH, WakeStageFrame.IMPACT),
            WakeArt.stageSequence().map { it.frame }
        )
    }

    @Test
    fun spark_noCrescent_emberOnIconOnly() {
        val s = afterSparkOnly()
        assertFalse(WakeArt.showCrescent(s))
        assertTrue(WakeArt.showSparkEmber(s))
        assertEquals(WakeIconPhase.SPARK_EMBER, WakeArt.iconPhase(s, 0L, 1))
        assertEquals(WakeStageFrame.NONE, WakeArt.stageFrame(s, 0L, 1))
        assertTrue(s.log.none { it.goldLog })
    }

    @Test
    fun fullWake_thickArcThenGoldLogThenClear() {
        val s = afterFullWake()
        assertTrue(s.weaponFlashed)
        val wake = s.log.first { it.message.startsWith("ASHBRAND — WAKE") }
        assertTrue(wake.goldLog)
        assertEquals("WAKE", wake.floating?.text)
        assertEquals(WakeStageFrame.CHARGE, WakeArt.stageFrame(s, 0L, 1))
        assertEquals(WakeStageFrame.SLASH, WakeArt.stageFrame(s, 400L, 1))
        assertEquals(WakeStageFrame.IMPACT, WakeArt.stageFrame(s, 900L, 1))
        assertEquals(WakeStageFrame.NONE, WakeArt.stageFrame(s, 1300L, 1))
    }

    @Test
    fun speed2x_sameFrames_halfDuration() {
        val s = afterFullWake()
        assertEquals(WakeStageFrame.CHARGE, WakeArt.stageFrame(s, 0L, 2))
        assertEquals(WakeStageFrame.SLASH, WakeArt.stageFrame(s, 200L, 2))
        assertEquals(WakeStageFrame.IMPACT, WakeArt.stageFrame(s, 450L, 2))
        assertEquals(WakeStageFrame.NONE, WakeArt.stageFrame(s, 650L, 2))
        assertEquals(2300L, WakeArt.fullWakeHold1x())
        assertEquals(1150L, WakeArt.fullWakeHold2x())
    }

    @Test
    fun wakeMathFrozen() {
        assertEquals(3, WeaponCatalog.ashbrand.threshold(1))
        assertEquals(4, WeaponCatalog.ashbrand.fullDmg(1))
        assertEquals(2, WeaponCatalog.ashbrand.sparkDmg(1))
        assertTrue(WakeArt.stageSequenceDuration1x() <= Balance.WEAPON_FULL_HOLD_MS)
        assertEquals(CombatBeat.AFTER_WEAPON, afterFullWake().beat)
    }
}
