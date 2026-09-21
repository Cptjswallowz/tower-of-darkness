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
    DRAGON("Seal-Warden", "enemies/enemy_dragon.png", 6, 9) // boss uses Balance.BOSS_COUNTER_*
}

data class Enemy(
    val kind: EnemyKind,
    val maxHp: Int,
    val hp: Int,
    val isBoss: Boolean = false
) {
    companion object {
        fun normal(kind: EnemyKind = EnemyKind.ORC): Enemy {
            val hp = Balance.ENEMY_BASE_HP
            return Enemy(kind, hp, hp, false)
        }
        fun boss(): Enemy {
            val hp = Balance.BOSS_HP
            return Enemy(EnemyKind.DRAGON, hp, hp, true)
        }
        fun forFloorCombat(index: Int): Enemy {
            val kinds = listOf(EnemyKind.GOBLIN, EnemyKind.ORC, EnemyKind.SPIDER, EnemyKind.TROLL)
            return normal(kinds[index % kinds.size])
        }
    }
}
