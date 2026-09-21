package com.towerofdarkness.app.domain

/**
 * Compose balance locks (see docs/balance-targets.md).
 * v0.1.3-combatbar: LOADOUT exactly 5 skills + separate weapon slot.
 * Boss HP 28 / counter bands unchanged.
 */
object Balance {
    const val TARGET_WIN_RATE = 0.84f
    const val TARGET_AVG_ROUNDS = 3.6f

    const val PLAYER_MAX_HP = 30
    const val ENEMY_BASE_HP = 20
    const val BOSS_HP = 28

    const val PLAYER_DMG_MIN = 4
    const val PLAYER_DMG_MAX = 8
    const val ENEMY_COUNTER_MIN = 7
    const val ENEMY_COUNTER_MAX = 9
    const val BOSS_COUNTER_MIN = 6
    const val BOSS_COUNTER_MAX = 9

    const val CHEAPEST_CARD_UNLOCK = 15
    const val SHOP_PRICE_MIN = 5
    const val SHOP_PRICE_MAX = 15

    const val LOADOUT_POOL_SIZE = 11
    const val LOADOUT_MIN = 5
    const val LOADOUT_MAX = 5

    const val REST_HEAL_AMOUNT = 12
    const val COMBAT_WIN_REMNANTS = 5
    const val COMBAT_LOSS_REMNANTS = 1
    const val BOSS_WIN_REMNANTS = 10
    const val BOSS_LOSS_REMNANTS = 3
    const val TREASURE_REMNANTS = 6
    const val EVENT_REMNANTS = 3

    // Combat pacing (ms) — phone-readable; no 2x
    const val DICE_MS = 800L
    const val SKILL_COMMON_MS = 1700L
    const val SKILL_RARE_MS = 2300L
    const val READ_HOLD_MS = 1800L
    const val WEAPON_HOLD_MS = 1500L
    /** FULL Wake only — legendary hold */
    const val WEAPON_FULL_HOLD_MS = 2300L
    const val ENEMY_HOLD_MS = 1500L
}
