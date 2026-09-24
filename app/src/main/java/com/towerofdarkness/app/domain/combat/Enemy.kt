package com.towerofdarkness.app.domain.combat

import com.towerofdarkness.app.domain.Balance

enum class EnemyKind(
    val displayName: String,
    val asset: String,
    val trashCounterMin: Int,
    val trashCounterMax: Int
) {
    GOBLIN("Ash Wretch", "enemies/enemy_goblin.png", 7, 9),
    ORC("Ruin Brute", "enemies/enemy_orc.png", 7, 9),
    TROLL("Stone Hunger", "enemies/enemy_troll.png", 7, 9),
    SPIDER("Seal Spinner", "enemies/enemy_spider.png", 7, 9),
    DRAGON("Seal-Warden", "enemies/enemy_dragon.png", 6, 9), // Floor 1 boss; counters via Balance.BOSS_COUNTER_*
    ASH_WARDEN("Ash-Warden", "portrait_ash_warden", 6, 9) // Floor 2 boss; v0.1.18 body still
}

data class Enemy(
    val kind: EnemyKind,
    val maxHp: Int,
    val hp: Int,
    val isBoss: Boolean = false
) {
    companion object {
        fun normal(kind: EnemyKind = EnemyKind.ORC, floor: Int = 1): Enemy {
            val hp = trashHpForFloor(floor)
            return Enemy(kind, hp, hp, false)
        }

        fun boss(floor: Int = 1): Enemy {
            val hp = bossHpForFloor(floor)
            val kind = if (floor >= 2) EnemyKind.ASH_WARDEN else EnemyKind.DRAGON
            return Enemy(kind, hp, hp, true)
        }

        fun trashHpForFloor(floor: Int): Int =
            if (floor >= 2) Balance.ENEMY_FLOOR2_HP else Balance.ENEMY_BASE_HP

        fun bossHpForFloor(floor: Int): Int =
            if (floor >= 2) Balance.BOSS_FLOOR2_HP else Balance.BOSS_HP

        /**
         * Floor trash pick. Floor 2 prefers spider/troll over goblin (weighted pool).
         * [index] keeps deterministic cycling for tests; optional [rng] unused for index cycle.
         */
        fun forFloorCombat(index: Int, floor: Int = 1): Enemy {
            val kinds = if (floor >= 2) {
                // Prefer spider/troll: 2× each, 1× orc, 1× goblin
                listOf(
                    EnemyKind.SPIDER, EnemyKind.TROLL,
                    EnemyKind.SPIDER, EnemyKind.TROLL,
                    EnemyKind.ORC, EnemyKind.GOBLIN
                )
            } else {
                listOf(EnemyKind.GOBLIN, EnemyKind.ORC, EnemyKind.SPIDER, EnemyKind.TROLL)
            }
            return normal(kinds[index % kinds.size], floor)
        }
    }
}
