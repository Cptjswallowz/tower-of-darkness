package com.towerofdarkness.app.domain.combat

enum class WeaponTag { Ember, Notch, Guard }

/**
 * Weapon is the 6th loadout slot — NOT in the skill dice pool.
 * Level is this-run only (1..3). Threshold: Lv1=4, Lv2=3, Lv3=2.
 */
data class Weapon(
    val id: String,
    val title: String,
    val statusTag: WeaponTag,
    val abilityTitle: String,
    val abilityText: String,
    /** Full-proc damage by level index 0..2 */
    val fullDamage: IntArray,
    /** Spark (half-power) damage by level index 0..2 */
    val sparkDamage: IntArray
) {
    fun threshold(level: Int): Int = when (level.coerceIn(1, 3)) {
        1 -> 4
        2 -> 3
        else -> 2
    }

    fun fullDmg(level: Int): Int = fullDamage[(level.coerceIn(1, 3) - 1)]
    fun sparkDmg(level: Int): Int = sparkDamage[(level.coerceIn(1, 3) - 1)]
}

data class WeaponRuntime(
    val def: Weapon,
    val level: Int = 1,
    val charge: Int = 0
) {
    val threshold: Int get() = def.threshold(level)
    val pipsFilled: Int get() = charge.coerceIn(0, threshold)
}

object WeaponCatalog {
    val ashbrand = Weapon(
        id = "ashbrand",
        title = "Ashbrand",
        statusTag = WeaponTag.Ember,
        abilityTitle = "Wake",
        abilityText = "Ember wake — full charge erupts for Wake damage.",
        fullDamage = intArrayOf(4, 6, 8),
        sparkDamage = intArrayOf(2, 3, 4)
    )
    val notchPike = Weapon(
        id = "notch_pike",
        title = "Notch Pike",
        statusTag = WeaponTag.Notch,
        abilityTitle = "Breach",
        abilityText = "Data-only stub — Breach.",
        fullDamage = intArrayOf(5, 7, 9),
        sparkDamage = intArrayOf(2, 3, 4)
    )
    val vowEdge = Weapon(
        id = "vow_edge",
        title = "Vow Edge",
        statusTag = WeaponTag.Guard,
        abilityTitle = "Oath",
        abilityText = "Data-only stub — Oath.",
        fullDamage = intArrayOf(3, 5, 7),
        sparkDamage = intArrayOf(2, 3, 4)
    )

    val all = listOf(ashbrand, notchPike, vowEdge)
    fun byId(id: String): Weapon? = all.find { it.id == id }
    fun default(): Weapon = ashbrand
}
