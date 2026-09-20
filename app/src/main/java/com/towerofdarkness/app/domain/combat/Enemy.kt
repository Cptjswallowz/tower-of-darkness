package com.towerofdarkness.app.domain.combat

import com.towerofdarkness.app.domain.Balance

enum class EnemyKind(val displayName: String, val asset: String) {
    GOBLIN("Ash Wretch", "enemies/enemy_goblin.png"),
    ORC("Ruin Brute", "enemies/enemy_orc.png"),
    TROLL("Stone Hunger", "enemies/enemy_troll.png"),
    SPIDER("Seal Spinner", "enemies/enemy_spider.png"),
    DRAGON("Seal-Warden", "enemies/enemy_dragon.png")
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
