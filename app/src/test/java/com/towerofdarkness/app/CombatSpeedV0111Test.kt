package com.towerofdarkness.app

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.nav.GameController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.1.11-2x: combat hold scaling 1x ↔ 2x (no 3x, no skip).
 */
class CombatSpeedV0111Test {

    @Test
    fun holdMs_1x_keepsBudgets() {
        assertEquals(Balance.SKILL_COMMON_MS, GameController.combatHoldMs(Balance.SKILL_COMMON_MS, 1))
        assertEquals(Balance.WEAPON_FULL_HOLD_MS, GameController.combatHoldMs(Balance.WEAPON_FULL_HOLD_MS, 1))
        assertEquals(Balance.DICE_MS, GameController.combatHoldMs(Balance.DICE_MS, 1))
        assertEquals(Balance.READ_HOLD_MS, GameController.combatHoldMs(Balance.READ_HOLD_MS, 1))
        assertEquals(Balance.ENEMY_HOLD_MS, GameController.combatHoldMs(Balance.ENEMY_HOLD_MS, 1))
    }

    @Test
    fun holdMs_2x_halvesEveryHold() {
        assertEquals(Balance.SKILL_COMMON_MS / 2, GameController.combatHoldMs(Balance.SKILL_COMMON_MS, 2))
        assertEquals(Balance.WEAPON_FULL_HOLD_MS / 2, GameController.combatHoldMs(Balance.WEAPON_FULL_HOLD_MS, 2))
        assertEquals(Balance.SKILL_RARE_MS / 2, GameController.combatHoldMs(Balance.SKILL_RARE_MS, 2))
        assertEquals(Balance.DICE_MS / 2, GameController.combatHoldMs(Balance.DICE_MS, 2))
        assertEquals(Balance.READ_HOLD_MS / 2, GameController.combatHoldMs(Balance.READ_HOLD_MS, 2))
        assertEquals(Balance.WEAPON_HOLD_MS / 2, GameController.combatHoldMs(Balance.WEAPON_HOLD_MS, 2))
        assertEquals(Balance.ENEMY_HOLD_MS / 2, GameController.combatHoldMs(Balance.ENEMY_HOLD_MS, 2))
    }

    @Test
    fun holdMs_clampsAbove2_no3x() {
        assertEquals(
            GameController.combatHoldMs(Balance.SKILL_COMMON_MS, 2),
            GameController.combatHoldMs(Balance.SKILL_COMMON_MS, 3)
        )
        assertEquals(
            GameController.combatHoldMs(Balance.WEAPON_FULL_HOLD_MS, 2),
            GameController.combatHoldMs(Balance.WEAPON_FULL_HOLD_MS, 99)
        )
    }

    @Test
    fun holdMs_zeroOrNegativeSpeed_treatedAs1x() {
        assertEquals(Balance.DICE_MS, GameController.combatHoldMs(Balance.DICE_MS, 0))
        assertEquals(Balance.DICE_MS, GameController.combatHoldMs(Balance.DICE_MS, -1))
    }

    @Test
    fun wakeReadableAt1x_andHalfAt2x() {
        val wake1 = GameController.combatHoldMs(Balance.WEAPON_FULL_HOLD_MS, 1)
        val wake2 = GameController.combatHoldMs(Balance.WEAPON_FULL_HOLD_MS, 2)
        assertEquals(2300L, wake1)
        assertEquals(1150L, wake2)
        assertTrue(wake2 * 2 == wake1)
        val common1 = GameController.combatHoldMs(Balance.SKILL_COMMON_MS, 1)
        val common2 = GameController.combatHoldMs(Balance.SKILL_COMMON_MS, 2)
        assertEquals(1700L, common1)
        assertEquals(850L, common2)
    }
}
