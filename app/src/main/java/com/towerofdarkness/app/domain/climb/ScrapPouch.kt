package com.towerofdarkness.app.domain.climb

import com.towerofdarkness.app.domain.combat.EnemyKind
import kotlin.random.Random

/**
 * Run-only scrap pouch — v0.1.58-forge PART B.
 * Never banks into remnants_bank. See docs/forge-v0158.md.
 */
data class ScrapGrant(val goblin: Int = 0, val orc: Int = 0) {
    operator fun plus(other: ScrapGrant) =
        ScrapGrant(goblin + other.goblin, orc + other.orc)
}

object ScrapPouch {
    const val SHOP_GOBLIN_PILE_ID = "goblin_pile"
    const val SHOP_ORC_PILE_ID = "orc_pile"
    const val SHOP_GOBLIN_PILE_TITLE = "Goblin pile"
    const val SHOP_ORC_PILE_TITLE = "Orc pile"
    const val SHOP_GOBLIN_PILE_COST = 8
    const val SHOP_ORC_PILE_COST = 14
    const val SHOP_GOBLIN_PILE_GRANT_G = 3
    const val SHOP_ORC_PILE_GRANT_O = 2

    /** Climb-wallet shop price unit — never "rem" (Hub remnants wording unchanged). */
    const val SHOP_PRICE_UNIT = "purse"

    /** Exact shop button line: "Goblin pile · 8 purse". */
    fun shopPriceLine(title: String, price: Int): String = "$title · $price $SHOP_PRICE_UNIT"

    /** Exact HUD form: HP N · purse N · gN oN */
    fun hudLine(hp: Int, purse: Int, goblin: Int, orc: Int): String =
        "HP $hp · purse $purse · g$goblin o$orc"

    fun hudLineWithMax(hp: Int, maxHp: Int, purse: Int, goblin: Int, orc: Int): String =
        "HP $hp / $maxHp · purse $purse · g$goblin o$orc"

    fun climbStartGoblin(unlocks: Set<String>): Int =
        if (ClimbKept.ID_SOOT_RIM in unlocks) 1 else 0

    /** Weak Goblin: +1g 80% / +2g 20%. */
    fun dropGoblin(rng: Random): ScrapGrant =
        if (rng.nextFloat() < 0.20f) ScrapGrant(goblin = 2) else ScrapGrant(goblin = 1)

    /** Sturdy Orc: +1o +1g. */
    fun dropOrc(): ScrapGrant = ScrapGrant(goblin = 1, orc = 1)

    /** Cave Troll / elite: +2o +2g. */
    fun dropElite(): ScrapGrant = ScrapGrant(goblin = 2, orc = 2)

    /** Gate-Warden / floor boss: +2o. */
    fun dropBoss(): ScrapGrant = ScrapGrant(orc = 2)

    /** Treasure scrap roll: 50% +2g / 30% +1o / 20% +1g+1o. */
    fun dropTreasure(rng: Random): ScrapGrant {
        val r = rng.nextFloat()
        return when {
            r < 0.50f -> ScrapGrant(goblin = 2)
            r < 0.80f -> ScrapGrant(orc = 1)
            else -> ScrapGrant(goblin = 1, orc = 1)
        }
    }

    fun dropForKill(kind: EnemyKind, isBoss: Boolean, rng: Random): ScrapGrant = when {
        isBoss -> dropBoss()
        kind == EnemyKind.CAVE_TROLL -> dropElite()
        kind == EnemyKind.GOBLIN -> dropGoblin(rng)
        kind == EnemyKind.ORC || kind == EnemyKind.TROLL || kind == EnemyKind.SPIDER -> dropOrc()
        else -> ScrapGrant()
    }

    fun applyGrant(goblin: Int, orc: Int, grant: ScrapGrant): Pair<Int, Int> =
        (goblin + grant.goblin) to (orc + grant.orc)
}
