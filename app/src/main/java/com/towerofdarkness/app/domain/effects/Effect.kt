package com.towerofdarkness.app.domain.effects

import com.towerofdarkness.app.domain.Rarity

sealed class Effect {
    abstract val id: String
    abstract val name: String
    abstract val description: String
    abstract val rarity: Rarity
}

/** Positioning / tempo — often lower weight; utility or setup. */
sealed class MoveEffect : Effect() {
    data class DamageAndSoften(
        override val id: String,
        override val name: String,
        override val description: String,
        override val rarity: Rarity,
        val damage: Int,
        val counterPenalty: Int = 0,
        val braceGain: Int = 0
    ) : MoveEffect()
}

/** Active strike when dice fire. */
sealed class SkillEffect : Effect() {
    data class Damage(
        override val id: String,
        override val name: String,
        override val description: String,
        override val rarity: Rarity,
        val damage: Int
    ) : SkillEffect()

    data class DamageAndHeal(
        override val id: String,
        override val name: String,
        override val description: String,
        override val rarity: Rarity,
        val damage: Int,
        val heal: Int
    ) : SkillEffect()
}

/** Passive / triggered gear — brace or heal shards. */
sealed class Equipment : Effect() {
    data class GainBrace(
        override val id: String,
        override val name: String,
        override val description: String,
        override val rarity: Rarity,
        val brace: Int
    ) : Equipment()

    data class HealOrBrace(
        override val id: String,
        override val name: String,
        override val description: String,
        override val rarity: Rarity,
        val heal: Int,
        val braceIfFull: Int
    ) : Equipment()
}
