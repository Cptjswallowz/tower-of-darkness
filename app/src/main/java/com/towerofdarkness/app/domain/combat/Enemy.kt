package com.towerofdarkness.app.domain.combat

import com.towerofdarkness.app.domain.Balance
import kotlin.random.Random

enum class EnemyKind(
    val displayName: String,
    val asset: String,
    val trashCounterMin: Int,
    val trashCounterMax: Int
) {
    GOBLIN("Weak Goblin", "enemies/enemy_goblin.png", 7, 9),
    ORC("Sturdy Orc", "enemies/enemy_orc.png", 7, 9),
    // Legacy kinds — hallway consolidated to GOBLIN/ORC; titles map to Sturdy Orc path
    TROLL("Sturdy Orc", "enemies/enemy_troll.png", 7, 9),
    SPIDER("Sturdy Orc", "enemies/enemy_spider.png", 7, 9),
    DRAGON("Seal-Warden", "portrait_seal_warden", 6, 9), // Floor 1 boss; v0.1.21 Seal-Warden still
    ASH_WARDEN("Ash-Warden", "portrait_ash_warden", 6, 9) // Floor 2 boss; v0.1.18 body still
}

data class Enemy(
    val kind: EnemyKind,
    val maxHp: Int,
    val hp: Int,
    val isBoss: Boolean = false,
    /** Hallway look variant; null for bosses. Persists for this fight / save-resume. */
    val look: EnemyLook? = null
) {
    companion object {
        fun normal(
            kind: EnemyKind = EnemyKind.ORC,
            floor: Int = 1,
            look: EnemyLook? = EnemyLook.defaultFor(kind)
        ): Enemy {
            val hp = trashHpForFloor(floor)
            val resolvedLook = when {
                HallwayPacks.isBossKind(kind) -> null
                look != null -> look
                else -> EnemyLook.defaultFor(kind)
            }
            return Enemy(kind, hp, hp, false, resolvedLook)
        }

        fun boss(floor: Int = 1): Enemy {
            val hp = bossHpForFloor(floor)
            val kind = if (floor >= 2) EnemyKind.ASH_WARDEN else EnemyKind.DRAGON
            return Enemy(kind, hp, hp, true, look = null)
        }

        fun trashHpForFloor(floor: Int): Int =
            if (floor >= 2) Balance.ENEMY_FLOOR2_HP else Balance.ENEMY_BASE_HP

        fun bossHpForFloor(floor: Int): Int =
            if (floor >= 2) Balance.BOSS_FLOOR2_HP else Balance.BOSS_HP

        /**
         * Hallway trash pick — v0.1.26-packs weighted 2-role table + look 1/3.
         * [index] seeds a deterministic RNG for tests (not a cycling kind table).
         */
        fun forFloorCombat(index: Int, floor: Int = 1): Enemy {
            val rng = Random(index.toLong() * 31L + floor.toLong() * 17_771L)
            return forFloorCombat(floor, rng)
        }

        /** Hallway combat with explicit RNG (path node authoring / live start). */
        fun forFloorCombat(floor: Int, rng: Random): Enemy =
            HallwayPacks.enemyForHallway(floor, rng)
    }
}
