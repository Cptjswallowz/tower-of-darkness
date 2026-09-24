package com.towerofdarkness.app.domain.combat

import kotlin.random.Random

/**
 * Hallway trash packs — v0.1.26-packs.
 * Titles Weak Goblin / Sturdy Orc; look pools (1/3); floor spawn weights.
 * Bosses never use this table. See docs/packs-v0126.md.
 */
enum class EnemyLook(val id: String, val role: HallwayRole) {
    KNIFE("knife", HallwayRole.WEAK_GOBLIN),
    BOTTLE("bottle", HallwayRole.WEAK_GOBLIN),
    SPIKES("spikes", HallwayRole.WEAK_GOBLIN),
    AXE("axe", HallwayRole.STURDY_ORC),
    CLEAVER("cleaver", HallwayRole.STURDY_ORC),
    HAMMER("hammer", HallwayRole.STURDY_ORC);

    companion object {
        fun goblinLooks(): List<EnemyLook> = listOf(KNIFE, BOTTLE, SPIKES)
        fun orcLooks(): List<EnemyLook> = listOf(AXE, CLEAVER, HAMMER)

        fun forRole(role: HallwayRole): List<EnemyLook> = when (role) {
            HallwayRole.WEAK_GOBLIN -> goblinLooks()
            HallwayRole.STURDY_ORC -> orcLooks()
        }

        fun defaultFor(kind: EnemyKind): EnemyLook? = when (HallwayRole.fromKind(kind)) {
            HallwayRole.WEAK_GOBLIN -> KNIFE
            HallwayRole.STURDY_ORC -> AXE
            null -> null
        }

        fun fromId(id: String?): EnemyLook? =
            if (id == null) null else entries.firstOrNull { it.id == id || it.name.equals(id, true) }
    }
}

enum class HallwayRole(val displayTitle: String, val kind: EnemyKind) {
    WEAK_GOBLIN("Weak Goblin", EnemyKind.GOBLIN),
    STURDY_ORC("Sturdy Orc", EnemyKind.ORC);

    companion object {
        fun fromKind(kind: EnemyKind): HallwayRole? = when (kind) {
            EnemyKind.GOBLIN -> WEAK_GOBLIN
            // SPIDER / TROLL were Seal Spinner / Stone Hunger band → Sturdy Orc title path
            EnemyKind.ORC, EnemyKind.SPIDER, EnemyKind.TROLL -> STURDY_ORC
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

    fun goblinWeightPct(floor: Int): Int =
        if (floor >= 2) F2_GOBLIN_PCT else F1_GOBLIN_PCT

    fun orcWeightPct(floor: Int): Int = 100 - goblinWeightPct(floor)

    /** Hallway roles only — never bosses. */
    fun rollRole(floor: Int, rng: Random): HallwayRole {
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
     * Naming: pack_weak_goblin_knife … pack_sturdy_orc_hammer.
     */
    fun drawableName(look: EnemyLook): String = when (look) {
        EnemyLook.KNIFE -> "pack_weak_goblin_knife"
        EnemyLook.BOTTLE -> "pack_weak_goblin_bottle"
        EnemyLook.SPIKES -> "pack_weak_goblin_spikes"
        EnemyLook.AXE -> "pack_sturdy_orc_axe"
        EnemyLook.CLEAVER -> "pack_sturdy_orc_cleaver"
        EnemyLook.HAMMER -> "pack_sturdy_orc_hammer"
    }

    fun allDrawableNames(): Set<String> = EnemyLook.entries.map { drawableName(it) }.toSet()

    /** Bosses must never come from the hallway table. */
    fun isHallwayKind(kind: EnemyKind): Boolean =
        kind == EnemyKind.GOBLIN || kind == EnemyKind.ORC ||
            kind == EnemyKind.SPIDER || kind == EnemyKind.TROLL

    fun isBossKind(kind: EnemyKind): Boolean =
        kind == EnemyKind.DRAGON || kind == EnemyKind.ASH_WARDEN
}
