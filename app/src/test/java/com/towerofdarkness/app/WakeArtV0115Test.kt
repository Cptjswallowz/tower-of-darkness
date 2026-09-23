package com.towerofdarkness.app

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatAnimStyle
import com.towerofdarkness.app.domain.combat.CombatBeat
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.CombatEvent
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.FloatingText
import com.towerofdarkness.app.domain.combat.WakeArt
import com.towerofdarkness.app.domain.combat.WakeIconPhase
import com.towerofdarkness.app.domain.combat.WakeStageFrame
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import com.towerofdarkness.app.nav.GameController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * v0.1.15-wakeart — icon + FULL crescent sequence; SPARK ember-only; 2x halves frames.
 */
class WakeArtV0115Test {

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
    fun drawables_lockedNames() {
        assertEquals("ashbrand_icon", WakeArt.ICON_DRAWABLE)
        assertEquals("ashbrand_spark", WakeArt.SPARK_DRAWABLE)
        assertEquals("wake_vfx_charge", WakeArt.VFX_CHARGE)
        assertEquals("wake_vfx_slash", WakeArt.VFX_SLASH)
        assertEquals("wake_vfx_impact", WakeArt.VFX_IMPACT)
        assertEquals(WakeArt.VFX_CHARGE, WakeArt.drawableFor(WakeStageFrame.CHARGE))
        assertEquals(WakeArt.VFX_SLASH, WakeArt.drawableFor(WakeStageFrame.SLASH))
        assertEquals(WakeArt.VFX_IMPACT, WakeArt.drawableFor(WakeStageFrame.IMPACT))
        assertNull(WakeArt.drawableFor(WakeStageFrame.NONE))
    }

    @Test
    fun iconSlot_sameSizeLoadoutAndCombat() {
        assertEquals(48, WakeArt.ICON_SLOT_DP)
    }

    @Test
    fun spark_noCrescent_emberOnIconOnly() {
        val s = afterSparkOnly()
        assertEquals(CombatBeat.AFTER_WEAPON, s.beat)
        assertTrue(s.log.any { it.message.contains("spark (", ignoreCase = true) })
        assertFalse(s.weaponFlashed)
        assertFalse(WakeArt.showCrescent(s))
        assertTrue(WakeArt.showSparkEmber(s))
        assertEquals(WakeIconPhase.SPARK_EMBER, WakeArt.iconPhase(s, 0L, 1))
        assertEquals(WakeStageFrame.NONE, WakeArt.stageFrame(s, 0L, 1))
        assertEquals(WakeStageFrame.NONE, WakeArt.stageFrame(s, 200L, 1))
        // No WAKE float / gold pin on spark
        assertTrue(s.log.none { it.floating?.text.equals("WAKE", ignoreCase = true) })
        assertTrue(s.log.none { it.goldLog })
    }

    @Test
    fun fullWake_crescentLogFloatThenClear() {
        val s = afterFullWake()
        assertEquals(CombatBeat.AFTER_WEAPON, s.beat)
        assertTrue(s.weaponFlashed)
        val wake = s.log.first { it.message.startsWith("ASHBRAND — WAKE") }
        assertTrue(wake.goldLog)
        assertEquals("WAKE", wake.floating?.text)
        assertEquals(CombatAnimStyle.CHARGE_SHAKE_SLOWMO, wake.animStyle)
        assertNotNull(s.pinnedWakeLine)
        assertTrue(WakeArt.showCrescent(s))
        assertFalse(WakeArt.showSparkEmber(s))

        // charge → slash → impact then clear
        assertEquals(WakeStageFrame.CHARGE, WakeArt.stageFrame(s, 0L, 1))
        assertEquals(WakeIconPhase.CHARGE, WakeArt.iconPhase(s, 0L, 1))
        assertEquals(WakeStageFrame.SLASH, WakeArt.stageFrame(s, 400L, 1))
        assertEquals(WakeIconPhase.CRACK, WakeArt.iconPhase(s, 400L, 1))
        assertEquals(WakeStageFrame.IMPACT, WakeArt.stageFrame(s, 900L, 1))
        // After sequence: overlay clears (hold tail)
        val afterSeq = WakeArt.stageSequenceDuration1x()
        assertEquals(1300L, afterSeq)
        assertEquals(WakeStageFrame.NONE, WakeArt.stageFrame(s, afterSeq, 1))
        assertTrue(afterSeq < WakeArt.fullWakeHold1x())
    }

    @Test
    fun speed2x_sameFrames_halfDuration() {
        val s = afterFullWake()
        val seq1 = WakeArt.stageSequence()
        assertEquals(3, seq1.size)
        assertEquals(listOf(WakeStageFrame.CHARGE, WakeStageFrame.SLASH, WakeStageFrame.IMPACT),
            seq1.map { it.frame })

        assertEquals(400L, WakeArt.holdMs(WakeArt.FRAME_CHARGE_MS, 1))
        assertEquals(200L, WakeArt.holdMs(WakeArt.FRAME_CHARGE_MS, 2))
        assertEquals(500L, WakeArt.holdMs(WakeArt.FRAME_SLASH_MS, 1))
        assertEquals(250L, WakeArt.holdMs(WakeArt.FRAME_SLASH_MS, 2))
        assertEquals(400L, WakeArt.holdMs(WakeArt.FRAME_IMPACT_MS, 1))
        assertEquals(200L, WakeArt.holdMs(WakeArt.FRAME_IMPACT_MS, 2))

        // Same frame order at 2x, compressed clocks
        assertEquals(WakeStageFrame.CHARGE, WakeArt.stageFrame(s, 0L, 2))
        assertEquals(WakeStageFrame.SLASH, WakeArt.stageFrame(s, 200L, 2))
        assertEquals(WakeStageFrame.IMPACT, WakeArt.stageFrame(s, 450L, 2))
        assertEquals(WakeStageFrame.NONE, WakeArt.stageFrame(s, 650L, 2))

        assertEquals(2300L, WakeArt.fullWakeHold1x())
        assertEquals(1150L, WakeArt.fullWakeHold2x())
        assertEquals(
            GameController.combatHoldMs(Balance.WEAPON_FULL_HOLD_MS, 2),
            WakeArt.fullWakeHold2x()
        )
    }

    @Test
    fun commonSkills_noCrescent() {
        assertFalse(WakeArt.commonSkillShowsCrescent())
        val engine = CombatEngine(Random(42))
        var s = engine.start(
            CardCatalog.defaultLoadoutIds.mapNotNull { CardCatalog.byId(it) },
            tank(),
            weapon(0),
            maxHp = 99,
            playerHp = 99
        )
        s = engine.diceTumble(s)
        s = engine.resolveSkill(s)
        assertEquals(CombatBeat.AFTER_SKILL, s.beat)
        // Even if somehow inspecting during skill beat — no crescent from WakeArt
        assertEquals(WakeStageFrame.NONE, WakeArt.stageFrame(s, 0L, 1))
        assertFalse(WakeArt.showCrescent(s))
        assertEquals(WakeIconPhase.IDLE, WakeArt.iconPhase(s, 0L, 1))
    }

    @Test
    fun fullWake_keepsExistingLogAndFloatContract() {
        val s = afterFullWake()
        val wake = s.log.first { it.message.startsWith("ASHBRAND — WAKE") }
        assertTrue(wake.message.matches(Regex("^ASHBRAND — WAKE \\d+$")))
        assertEquals(FloatingText("WAKE", true, true), wake.floating)
        assertEquals("legendary", wake.sound)
    }

    @Test
    fun sequenceFitsInsideFullHold_noMathChange() {
        assertTrue(WakeArt.stageSequenceDuration1x() <= Balance.WEAPON_FULL_HOLD_MS)
        assertEquals(3, Balance.WEAPON_FULL_HOLD_MS.let { /* thresh frozen */ WeaponCatalog.ashbrand.threshold(1) })
        assertEquals(4, WeaponCatalog.ashbrand.fullDmg(1))
        assertEquals(2, WeaponCatalog.ashbrand.sparkDmg(1))
    }
}
