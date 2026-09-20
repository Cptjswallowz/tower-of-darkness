package com.towerofdarkness.app.domain

/**
 * Compose balance locks (see docs/balance-targets.md).
 * Win-rate ~84% and ~3.6 rounds are TARGETS, not measured.
 * CoS locks: Boss HP 28, boss counter 8–12; Flee grayed.
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
    const val ENEMY_COUNTER_MAX = 11
    const val BOSS_COUNTER_MIN = 8
    const val BOSS_COUNTER_MAX = 12

    const val CHEAPEST_CARD_UNLOCK = 15
    const val SHOP_PRICE_MIN = 5
    const val SHOP_PRICE_MAX = 15

    const val LOADOUT_POOL_SIZE = 11
    const val LOADOUT_MIN = 5
    const val LOADOUT_MAX = 6

    const val REST_HEAL_AMOUNT = 12
    const val COMBAT_WIN_REMNANTS = 5
    const val COMBAT_LOSS_REMNANTS = 1
    const val BOSS_WIN_REMNANTS = 10
    const val BOSS_LOSS_REMNANTS = 3
    const val TREASURE_REMNANTS = 6
    const val EVENT_REMNANTS = 3
}
