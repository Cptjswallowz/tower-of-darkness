package com.towerofdarkness.app.domain.combat

import kotlin.random.Random

/**
 * Hallway trash packs — v0.1.26-packs + v0.1.41-floor3 Cave Troll.
 * Titles Weak Goblin / Sturdy Orc / Cave Troll; look pools (1/3); floor spawn weights.
 * Bosses never use this table. See docs/packs-v0126.md + docs/floor3-v0141.md.
 */
enum class EnemyLook(val id: String, val role: HallwayRole) {
    KNIFE("knife", HallwayRole.WEAK_GOBLIN),
    BOTTLE("bottle", HallwayRole.WEAK_GOBLIN),
    SPIKES("spikes", HallwayRole.WEAK_GOBLIN),
    AXE("axe", HallwayRole.STURDY_ORC),
    CLEAVER("cleaver", HallwayRole.STURDY_ORC),
    HAMMER("hammer", HallwayRole.STURDY_ORC),
    CAVE_TROLL_A("a", HallwayRole.CAVE_TROLL),
    CAVE_TROLL_B("b", HallwayRole.CAVE_TROLL),
    CAVE_TROLL_C("c", HallwayRole.CAVE_TROLL);

    companion object {
        fun goblinLooks(): List<EnemyLook> = listOf(KNIFE, BOTTLE, SPIKES)
        fun orcLooks(): List<EnemyLook> = listOf(AXE, CLEAVER, HAMMER)
        fun caveTrollLooks(): List<EnemyLook> = listOf(CAVE_TROLL_A, CAVE_TROLL_B, CAVE_TROLL_C)

        fun forRole(role: HallwayRole): List<EnemyLook> = when (role) {
            HallwayRole.WEAK_GOBLIN -> goblinLooks()
            HallwayRole.STURDY_ORC -> orcLooks()
            HallwayRole.CAVE_TROLL -> caveTrollLooks()
        }

        fun defaultFor(kind: EnemyKind): EnemyLook? = when (HallwayRole.fromKind(kind)) {
            HallwayRole.WEAK_GOBLIN -> KNIFE
            HallwayRole.STURDY_ORC -> AXE
            HallwayRole.CAVE_TROLL -> CAVE_TROLL_A
            null -> null
        }

        fun fromId(id: String?): EnemyLook? =
            if (id == null) null else entries.firstOrNull { it.id == id || it.name.equals(id, true) }
    }
}

enum class HallwayRole(val displayTitle: String, val kind: EnemyKind) {
    WEAK_GOBLIN("Weak Goblin", EnemyKind.GOBLIN),
    STURDY_ORC("Sturdy Orc", EnemyKind.ORC),
    CAVE_TROLL("Cave Troll", EnemyKind.CAVE_TROLL);

    companion object {
        fun fromKind(kind: EnemyKind): HallwayRole? = when (kind) {
            EnemyKind.GOBLIN -> WEAK_GOBLIN
            // SPIDER / TROLL were Seal Spinner / Stone Hunger band → Sturdy Orc title path
            EnemyKind.ORC, EnemyKind.SPIDER, EnemyKind.TROLL -> STURDY_ORC
            EnemyKind.CAVE_TROLL -> CAVE_TROLL
            else -> null
        }
    }
}

data class PackAuthorship(
    val role: HallwayRole,
    val look: EnemyLook
) {
    val kind: EnemyKind get() = role.kind

    fun toEnemy(floor: Int): Enemy = Enemy.normal(kind, floor, look)

    companion object {
        fun encode(p: PackAuthorship): String = "${p.role.name}:${p.look.id}"

        fun decode(raw: String?): PackAuthorship? {
            if (raw.isNullOrBlank()) return null
            val parts = raw.split(':', limit = 2)
            if (parts.size != 2) return null
            val role = runCatching { HallwayRole.valueOf(parts[0]) }.getOrNull() ?: return null
            val look = EnemyLook.fromId(parts[1]) ?: return null
            if (look.role != role) return null
            return PackAuthorship(role, look)
        }
    }
}

object HallwayPacks {
    const val TAG = "v0.1.26-packs"

    /** F1 Weak Goblin weight (percent). */
    const val F1_GOBLIN_PCT = 75

    /** F2 Weak Goblin weight (percent). */
    const val F2_GOBLIN_PCT = 30

    /** F3 Weak Goblin / Sturdy Orc / Cave Troll (percent). */
    const val F3_GOBLIN_PCT = 20
    const val F3_ORC_PCT = 40
    const val F3_CAVE_TROLL_PCT = 40

    fun goblinWeightPct(floor: Int): Int = when {
        floor >= 3 -> F3_GOBLIN_PCT
        floor >= 2 -> F2_GOBLIN_PCT
        else -> F1_GOBLIN_PCT
    }

    fun orcWeightPct(floor: Int): Int = when {
        floor >= 3 -> F3_ORC_PCT
        else -> 100 - goblinWeightPct(floor)
    }

    fun caveTrollWeightPct(floor: Int): Int =
        if (floor >= 3) F3_CAVE_TROLL_PCT else 0

    /** Hallway roles only — never bosses. */
    fun rollRole(floor: Int, rng: Random): HallwayRole {
        if (floor >= 3) {
            val roll = rng.nextInt(100)
            return when {
                roll < F3_GOBLIN_PCT -> HallwayRole.WEAK_GOBLIN
                roll < F3_GOBLIN_PCT + F3_ORC_PCT -> HallwayRole.STURDY_ORC
                else -> HallwayRole.CAVE_TROLL
            }
        }
        val goblinPct = goblinWeightPct(floor)
        return if (rng.nextInt(100) < goblinPct) HallwayRole.WEAK_GOBLIN else HallwayRole.STURDY_ORC
    }

    /** Equal 1/3 among the role's look pool. */
    fun rollLook(role: HallwayRole, rng: Random): EnemyLook {
        val pool = EnemyLook.forRole(role)
        return pool[rng.nextInt(pool.size)]
    }

    /** Roll role + look when a hallway combat node / fight is authored. */
    fun roll(floor: Int, rng: Random): PackAuthorship {
        val role = rollRole(floor, rng)
        return PackAuthorship(role, rollLook(role, rng))
    }

    fun enemyForHallway(floor: Int, rng: Random): Enemy = roll(floor, rng).toEnemy(floor)

    /**
     * Drawable basename for a look (Art drop). Wired only when PNG is present under res/drawable.
     * Naming: portrait_weak_goblin_knife … portrait_cave_troll_c.
     */
    fun drawableName(look: EnemyLook): String = when (look) {
        EnemyLook.KNIFE -> "portrait_weak_goblin_knife"
        EnemyLook.BOTTLE -> "portrait_weak_goblin_bottle"
        EnemyLook.SPIKES -> "portrait_weak_goblin_spikes"
        EnemyLook.AXE -> "portrait_sturdy_orc_axe"
        EnemyLook.CLEAVER -> "portrait_sturdy_orc_cleaver"
        EnemyLook.HAMMER -> "portrait_sturdy_orc_hammer"
        EnemyLook.CAVE_TROLL_A -> "portrait_cave_troll_a"
        EnemyLook.CAVE_TROLL_B -> "portrait_cave_troll_b"
        EnemyLook.CAVE_TROLL_C -> "portrait_cave_troll_c"
    }

    fun allDrawableNames(): Set<String> = EnemyLook.entries.map { drawableName(it) }.toSet()

    /** Bosses must never come from the hallway table. */
    fun isHallwayKind(kind: EnemyKind): Boolean =
        kind == EnemyKind.GOBLIN || kind == EnemyKind.ORC ||
            kind == EnemyKind.SPIDER || kind == EnemyKind.TROLL ||
            kind == EnemyKind.CAVE_TROLL

    fun isBossKind(kind: EnemyKind): Boolean =
        kind == EnemyKind.DRAGON || kind == EnemyKind.ASH_WARDEN ||
            kind == EnemyKind.GATE_WARDEN
}
