package com.towerofdarkness.app.domain.cards

import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.domain.effects.Effect
import com.towerofdarkness.app.domain.effects.Equipment
import com.towerofdarkness.app.domain.effects.MoveEffect
import com.towerofdarkness.app.domain.effects.SkillEffect
import com.towerofdarkness.app.domain.hub.HubOffers

data class Card(
    val id: String,
    val title: String,
    val effect: Effect,
    val rarity: Rarity,
    val weight: Int,
    val unlockCost: Int = 0,
    val hubUnlockable: Boolean = false
)

object CardCatalog {
    /**
     * Cards #1–9 owned at install. Only Shadow Latch (15) + Relic Shard (20) are Hub card unlocks.
     * CoS / meta-economy-v0 alignment.
     */
    val all: List<Card> by lazy {
        listOf(
            Card("hostflint", "Hostflint",
                SkillEffect.Damage("hostflint", "Hostflint", "Deal 5 damage.", Rarity.COMMON, 5),
                Rarity.COMMON, 5),
            Card("cinder_step", "Cinder Step",
                MoveEffect.DamageAndSoften("cinder_step", "Cinder Step",
                    "Deal 4 damage. Next enemy counter −2 (min 1).", Rarity.COMMON, 4, counterPenalty = 2),
                Rarity.COMMON, 3),
            Card("iron_mantle", "Iron Mantle",
                Equipment.GainBrace("iron_mantle", "Iron Mantle", "Gain Brace 3 (absorb before HP).", Rarity.COMMON, 3),
                Rarity.COMMON, 4),
            Card("emberbrand", "Emberbrand",
                SkillEffect.Damage("emberbrand", "Emberbrand", "Deal 6 damage.", Rarity.COMMON, 6),
                Rarity.COMMON, 5),
            Card("dust_veil", "Dust Veil",
                MoveEffect.DamageAndSoften("dust_veil", "Dust Veil",
                    "Deal 4 damage. Gain Brace 2.", Rarity.COMMON, 4, braceGain = 2),
                Rarity.COMMON, 3),
            Card("vow_plate", "Vow Plate",
                Equipment.GainBrace("vow_plate", "Vow Plate", "Gain Brace 5.", Rarity.UNCOMMON, 5),
                Rarity.UNCOMMON, 3),
            Card("ruin_seal", "Ruin Seal",
                SkillEffect.Damage("ruin_seal", "Ruin Seal", "Deal 7 damage.", Rarity.UNCOMMON, 7),
                Rarity.UNCOMMON, 4),
            Card("ash_press", "Ash Press",
                SkillEffect.DamageAndHeal("ash_press", "Ash Press",
                    "Deal 4 damage. Heal 2 HP (cap 30).", Rarity.UNCOMMON, 4, 2),
                Rarity.UNCOMMON, 3),
            Card("tower_pike", "Tower Pike",
                SkillEffect.Damage("tower_pike", "Tower Pike", "Deal 8 damage.", Rarity.UNCOMMON, 8),
                Rarity.UNCOMMON, 4),
            Card("shadow_latch", "Shadow Latch",
                MoveEffect.DamageAndSoften("shadow_latch", "Shadow Latch",
                    "Deal 5 damage. Enemy’s next counter −2 (min 1).", Rarity.RARE, 5, counterPenalty = 2),
                Rarity.RARE, 2, unlockCost = 15, hubUnlockable = true),
            Card("relic_shard", "Relic Shard",
                Equipment.HealOrBrace("relic_shard", "Relic Shard",
                    "Heal 4 HP. If already full, gain Brace 4 instead.", Rarity.RARE, 4, 4),
                Rarity.RARE, 2, unlockCost = 20, hubUnlockable = true),
            // Hub-gated (v0.1.27) — pool only after Host of Embers / Iron Lesson
            Card("cinder_vow", "Cinder Vow",
                SkillEffect.DamageAndBraceIfAshPips(
                    "cinder_vow", "Cinder Vow",
                    "Deal 5. If Ashbrand has ≥1 pip, gain Brace 2.",
                    Rarity.UNCOMMON, damage = 5, brace = 2, minPips = 1
                ),
                Rarity.UNCOMMON, 3, unlockCost = 12),
            Card("grave_nail", "Grave Nail",
                MoveEffect.DamageAndSoften(
                    "grave_nail", "Grave Nail",
                    "Deal 4. Next enemy counter −1 (min 1). Soften 1.",
                    Rarity.UNCOMMON, 4, counterPenalty = 1
                ),
                Rarity.UNCOMMON, 3, unlockCost = 12),
            // Hub-gated ember pool (v0.1.35) — pool only after Cold Draw / Brand / Spark / Echo Lesson
            Card("ember_draw", "Ember Draw",
                SkillEffect.EmberPoolSkill(
                    "ember_draw", "Ember Draw",
                    "Deal 4. If Sparks were 0 before this skill, gain Brace 2. Still adds 1 Spark after the hit.",
                    Rarity.UNCOMMON, damage = 4, braceIfZeroBefore = 2
                ),
                Rarity.UNCOMMON, 3, unlockCost = 10),
            Card("brand_mark", "Brand Mark",
                SkillEffect.EmberPoolSkill(
                    "brand_mark", "Brand Mark",
                    "Deal 3. If Sparks were ≥1 before this skill, Soften 2. Still adds 1 Spark after the hit.",
                    Rarity.UNCOMMON, damage = 3, softenIfBeforeGte1 = 2
                ),
                Rarity.UNCOMMON, 3, unlockCost = 10),
            Card("spark_tithe", "Spark Tithe",
                SkillEffect.EmberPoolSkill(
                    "spark_tithe", "Spark Tithe",
                    "Deal 6. If Sparks were ≥1 before, spend 1 Spark after the new pip (net 0). If 0 before, keep the new pip.",
                    Rarity.UNCOMMON, damage = 6, titheSpendIfBeforeGte1 = true
                ),
                Rarity.UNCOMMON, 3, unlockCost = 12),
            Card("wake_echo", "Wake Echo",
                SkillEffect.EmberPoolSkill(
                    "wake_echo", "Wake Echo",
                    "Deal 5. If Wake already fired this fight, deal 4 more in the same line. Bonus does not add another Spark.",
                    Rarity.UNCOMMON, damage = 5, echoBonusIfWakeFired = 4
                ),
                Rarity.UNCOMMON, 3, unlockCost = 12),
        )
    }

    val defaultLoadoutIds: List<String> =
        listOf("hostflint", "cinder_step", "iron_mantle", "emberbrand", "dust_veil")

    fun starterUnlockedIds(): Set<String> =
        all.filter { it.unlockCost == 0 }.map { it.id }.toSet()

    fun byId(id: String): Card? = all.find { it.id == id }

    fun poolForRun(unlocked: Set<String>): List<Card> =
        all.filter { card ->
            when (card.id) {
                in HubOffers.gatedSkillCardIds -> HubOffers.skillUnlocked(card.id, unlocked)
                else -> card.id in unlocked || card.unlockCost == 0
            }
        }

    fun hubUnlockables(owned: Set<String>): List<Card> =
        all.filter { it.hubUnlockable && it.id !in owned }.sortedBy { it.unlockCost }
}
