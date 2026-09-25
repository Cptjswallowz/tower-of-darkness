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

    /** Deal damage; if Ashbrand pips ≥ minPips, gain Brace. */
    data class DamageAndBraceIfAshPips(
        override val id: String,
        override val name: String,
        override val description: String,
        override val rarity: Rarity,
        val damage: Int,
        val brace: Int,
        val minPips: Int = 1
    ) : SkillEffect()

    /**
     * Ember pool (v0.1.35): damage (+ optional Wake Echo bonus) in resolveCard;
     * Brace / Soften / Tithe after +1 Spark in resolveSkill, keyed off pipsBefore.
     * See docs/emberpool-v0135.md.
     */
    data class EmberPoolSkill(
        override val id: String,
        override val name: String,
        override val description: String,
        override val rarity: Rarity,
        val damage: Int,
        /** Added to [damage] in the same skill when fullProcThisCombat (Wake Echo). */
        val echoBonusIfWakeFired: Int = 0,
        /** After spark charge: if pipsBefore == 0, gain this Brace. */
        val braceIfZeroBefore: Int = 0,
        /** After spark charge: if pipsBefore ≥ 1, add Soften (counterPenalty). */
        val softenIfBeforeGte1: Int = 0,
        /** After spark charge: if pipsBefore ≥ 1, spend 1 Spark and log "Spark spent". */
        val titheSpendIfBeforeGte1: Boolean = false
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
