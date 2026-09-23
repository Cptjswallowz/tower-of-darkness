package com.towerofdarkness.app.domain.combat

import com.towerofdarkness.app.domain.Balance

/**
 * Wake presentation art (v0.1.16-wakeicon polish on v0.1.15) — pure mapping; no Wake math.
 * Drawables: ashbrand_icon, ashbrand_spark, wake_vfx_{charge,slash,impact} (Art drop-in names).
 * Aligns with docs/wake-icon-v0116.md + docs/art-audio/WAKE_ICON_v0.1.16.md.
 * After this polish: Wake art frozen unless Elliott reopens.
 */
enum class WakeIconPhase {
    /** Idle Ashbrand plate. */
    IDLE,
    /** FULL Wake: gold fuller seam brightens. */
    CHARGE,
    /** FULL Wake: gold crack line reads fuller. */
    CRACK,
    /** SPARK half: tiny ember on icon only. */
    SPARK_EMBER
}

/** Portrait-stage crescent frames — FULL Wake only. */
enum class WakeStageFrame {
    NONE,
    CHARGE,
    SLASH,
    IMPACT
}

data class WakeStageStep(
    val frame: WakeStageFrame,
    /** 1x budget ms; halved at 2x via [WakeArt.holdMs]. */
    val baseMs: Long
)

object WakeArt {
    /** Slot / icon bitmap (loadout + combat weapon plate). */
    const val ICON_DRAWABLE = "ashbrand_icon"
    /** Half-charge ember overlay — icon bounds only. */
    const val SPARK_DRAWABLE = "ashbrand_spark"
    const val VFX_CHARGE = "wake_vfx_charge"
    const val VFX_SLASH = "wake_vfx_slash"
    const val VFX_IMPACT = "wake_vfx_impact"

    /** Art note @1x budgets inside FULL Wake hold (do not change hold math). */
    const val FRAME_CHARGE_MS = 400L
    const val FRAME_SLASH_MS = 500L
    const val FRAME_IMPACT_MS = 400L

    /** Same slot size as today's weapon plate chrome (~48 dp readable). */
    const val ICON_SLOT_DP = 48

    /** Polish lock: slash stroke ~2–3× thin stub; ash dots on arc. */
    const val STROKE_THICKNESS_MULT_MIN = 2
    const val STROKE_THICKNESS_MULT_MAX = 3
    const val ASH_DOTS_MIN = 8
    const val ASH_DOTS_MAX = 12

    /** Impact is one small spark burst — not a second slash frame. */
    const val IMPACT_IS_SPARK_BURST = true

    fun stageSequence(): List<WakeStageStep> = listOf(
        WakeStageStep(WakeStageFrame.CHARGE, FRAME_CHARGE_MS),
        WakeStageStep(WakeStageFrame.SLASH, FRAME_SLASH_MS),
        WakeStageStep(WakeStageFrame.IMPACT, FRAME_IMPACT_MS)
    )

    fun drawableFor(frame: WakeStageFrame): String? = when (frame) {
        WakeStageFrame.NONE -> null
        WakeStageFrame.CHARGE -> VFX_CHARGE
        WakeStageFrame.SLASH -> VFX_SLASH
        WakeStageFrame.IMPACT -> VFX_IMPACT
    }

    /** Scale a presentation hold by combat speed — 2x halves; no skip. */
    fun holdMs(baseMs: Long, speedX: Int): Long {
        val x = if (speedX >= 2) 2 else 1
        return (baseMs / x).coerceAtLeast(1L)
    }

    fun fullWakeHold1x(): Long = Balance.WEAPON_FULL_HOLD_MS
    fun fullWakeHold2x(): Long = holdMs(Balance.WEAPON_FULL_HOLD_MS, 2)

    /** Frames play once; remaining FULL hold is clear/idle (no skip). */
    fun stageSequenceDuration1x(): Long =
        stageSequence().sumOf { it.baseMs }

    /**
     * Icon phase for the weapon beat after resolve.
     * SPARK → ember only (no crescent). FULL → charge then crack.
     * Common skills never enter here (no weapon flash / spark).
     */
    fun iconPhase(
        state: CombatState,
        elapsedInWeaponBeatMs: Long,
        speedX: Int = 1
    ): WakeIconPhase {
        if (!isWeaponBeat(state)) return WakeIconPhase.IDLE
        if (isSparkOnly(state)) return WakeIconPhase.SPARK_EMBER
        if (!isFullWakeBeat(state)) return WakeIconPhase.IDLE
        val chargeBudget = holdMs(FRAME_CHARGE_MS, speedX)
        return if (elapsedInWeaponBeatMs < chargeBudget) WakeIconPhase.CHARGE
        else WakeIconPhase.CRACK
    }

    /** Portrait crescent frame at elapsed time; NONE for SPARK / idle / after sequence. */
    fun stageFrame(
        state: CombatState,
        elapsedInWeaponBeatMs: Long,
        speedX: Int = 1
    ): WakeStageFrame {
        if (!isFullWakeBeat(state) || !isWeaponBeat(state)) return WakeStageFrame.NONE
        var t = 0L
        for (step in stageSequence()) {
            val budget = holdMs(step.baseMs, speedX)
            if (elapsedInWeaponBeatMs < t + budget) return step.frame
            t += budget
        }
        return WakeStageFrame.NONE // hold tail / clear
    }

    fun showCrescent(state: CombatState): Boolean =
        isFullWakeBeat(state) && isWeaponBeat(state)

    fun showSparkEmber(state: CombatState): Boolean =
        isSparkOnly(state) && isWeaponBeat(state)

    /** Common skill fire: never crescent. */
    fun commonSkillShowsCrescent(): Boolean = false

    fun isWeaponBeat(state: CombatState): Boolean =
        state.beat == CombatBeat.AFTER_WEAPON ||
            (state.beat == CombatBeat.AWAITING_CONTINUE &&
                (state.weaponFlashed || lastIsWakeOrSpark(state)))

    fun isFullWakeBeat(state: CombatState): Boolean {
        if (state.weaponFlashed) return true
        if (state.pinnedWakeLine != null && state.beat == CombatBeat.AFTER_WEAPON) return true
        return state.beat == CombatBeat.AFTER_WEAPON &&
            state.log.takeLast(4).any {
                it.goldLog && it.message.startsWith("ASHBRAND — WAKE") &&
                    it.floating?.text.equals("WAKE", ignoreCase = true)
            }
    }

    fun isSparkOnly(state: CombatState): Boolean {
        if (isFullWakeBeat(state)) return false
        return lastIsSpark(state)
    }

    private fun lastIsWakeOrSpark(state: CombatState): Boolean =
        state.log.takeLast(4).any {
            it.message.startsWith("ASHBRAND — WAKE") ||
                it.message.contains("spark (", ignoreCase = true)
        }

    private fun lastIsSpark(state: CombatState): Boolean =
        state.log.takeLast(3).any { it.message.contains("spark (", ignoreCase = true) } &&
            state.log.takeLast(3).none { it.message.startsWith("ASHBRAND — WAKE") }
}
