package com.towerofdarkness.app.domain.combat

/**
 * Enemy live kits — v0.1.32-enemykit.
 * Same weight / exhaust / grey machine as the player bar.
 * Looks are cosmetic; kit is per role. See docs/enemykit-v0132.md.
 */
enum class EnemySkillKind {
    /** Soften reduces; Soften pip clears after. */
    DAMAGE,
    /** Brace on self — Soften exempt, does not consume Soften. */
    BRACE,
    /** Deal fixed dmg; ignores Soften; Soften pip stays; log must say so. */
    NIP
}

data class EnemySkill(
    val id: String,
    val title: String,
    val weight: Int,
    val kind: EnemySkillKind,
    /** Fixed damage (DAMAGE / NIP) when [damageMax] is null. */
    val damage: Int = 0,
    /** Inclusive Hit band; when set, roll [damageMin]..[damageMax]. */
    val damageMin: Int? = null,
    val damageMax: Int? = null,
    /** Brace gain for BRACE skills. */
    val braceGain: Int = 0,
    /** Glossary key for tile / log tap. Hide family → "brace". */
    val glossaryKey: String
) {
    fun rollDamage(rng: kotlin.random.Random): Int {
        val min = damageMin
        val max = damageMax
        return if (min != null && max != null) rng.nextInt(min, max + 1) else damage
    }
}

enum class EnemyKitRole {
    WEAK_GOBLIN,
    STURDY_ORC,
    SEAL_WARDEN,
    ASH_WARDEN;

    companion object {
        fun fromEnemy(enemy: Enemy): EnemyKitRole = when (enemy.kind) {
            EnemyKind.GOBLIN -> WEAK_GOBLIN
            EnemyKind.ORC, EnemyKind.TROLL, EnemyKind.SPIDER -> STURDY_ORC
            EnemyKind.DRAGON -> SEAL_WARDEN
            EnemyKind.ASH_WARDEN -> ASH_WARDEN
        }

        fun fromKind(kind: EnemyKind): EnemyKitRole = when (kind) {
            EnemyKind.GOBLIN -> WEAK_GOBLIN
            EnemyKind.ORC, EnemyKind.TROLL, EnemyKind.SPIDER -> STURDY_ORC
            EnemyKind.DRAGON -> SEAL_WARDEN
            EnemyKind.ASH_WARDEN -> ASH_WARDEN
        }
    }
}

object EnemyKits {
    const val TAG = "v0.1.32-enemykit"

    val weakGoblin: List<EnemySkill> = listOf(
        EnemySkill("shiv", "Shiv", 2, EnemySkillKind.DAMAGE, damage = 5, glossaryKey = "shiv"),
        EnemySkill("nip", "Nip", 2, EnemySkillKind.NIP, damage = 3, glossaryKey = "nip"),
        EnemySkill(
            "hit", "Hit", 5, EnemySkillKind.DAMAGE,
            damageMin = 7, damageMax = 9, glossaryKey = "hit"
        )
    )

    val sturdyOrc: List<EnemySkill> = listOf(
        EnemySkill("cleave", "Cleave", 2, EnemySkillKind.DAMAGE, damage = 8, glossaryKey = "cleave"),
        EnemySkill("hide", "Hide", 2, EnemySkillKind.BRACE, braceGain = 3, glossaryKey = "hide"),
        EnemySkill(
            "hit", "Hit", 5, EnemySkillKind.DAMAGE,
            damageMin = 8, damageMax = 10, glossaryKey = "hit"
        )
    )

    val sealWarden: List<EnemySkill> = listOf(
        EnemySkill(
            "seal_pulse", "Seal Pulse", 2, EnemySkillKind.DAMAGE,
            damage = 7, glossaryKey = "seal pulse"
        ),
        EnemySkill(
            "rust_guard", "Rust Guard", 2, EnemySkillKind.BRACE,
            braceGain = 4, glossaryKey = "rust guard"
        ),
        EnemySkill(
            "hit", "Hit", 5, EnemySkillKind.DAMAGE,
            damageMin = 6, damageMax = 9, glossaryKey = "hit"
        )
    )

    val ashWarden: List<EnemySkill> = listOf(
        EnemySkill(
            "coal_slam", "Coal Slam", 2, EnemySkillKind.DAMAGE,
            damage = 9, glossaryKey = "coal slam"
        ),
        EnemySkill(
            "cinder_hide", "Cinder Hide", 2, EnemySkillKind.BRACE,
            braceGain = 3, glossaryKey = "cinder hide"
        ),
        EnemySkill(
            "hit", "Hit", 5, EnemySkillKind.DAMAGE,
            damageMin = 6, damageMax = 9, glossaryKey = "hit"
        )
    )

    fun skillsFor(role: EnemyKitRole): List<EnemySkill> = when (role) {
        EnemyKitRole.WEAK_GOBLIN -> weakGoblin
        EnemyKitRole.STURDY_ORC -> sturdyOrc
        EnemyKitRole.SEAL_WARDEN -> sealWarden
        EnemyKitRole.ASH_WARDEN -> ashWarden
    }

    fun skillsFor(enemy: Enemy): List<EnemySkill> = skillsFor(EnemyKitRole.fromEnemy(enemy))

    fun skillsFor(kind: EnemyKind): List<EnemySkill> = skillsFor(EnemyKitRole.fromKind(kind))

    /** Total weight of a kit (always 9 = 2+2+5). */
    fun totalWeight(role: EnemyKitRole): Int = skillsFor(role).sumOf { it.weight }
}
