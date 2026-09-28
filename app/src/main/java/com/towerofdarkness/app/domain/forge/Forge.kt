package com.towerofdarkness.app.domain.forge

import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.domain.cards.Card
import com.towerofdarkness.app.domain.effects.Effect
import com.towerofdarkness.app.domain.effects.Equipment
import com.towerofdarkness.app.domain.effects.MoveEffect
import com.towerofdarkness.app.domain.effects.SkillEffect

/**
 * Forge costs + choice text — v0.1.58-forge PART C/D.
 * Chips/glossary from docs/forge-choices-v0158.md exactly. Relic Shard unmapped.
 * Iron Mantle L3B: fallback chip "On Brace: deal 2" (no weight hook).
 */
enum class ForgeBranch { A, B }

data class ForgeSkillState(
    val level: Int = 1,
    val l2: ForgeBranch? = null,
    val l3: ForgeBranch? = null
) {
    fun withL2(b: ForgeBranch) = copy(level = 2, l2 = b)
    fun withL3(b: ForgeBranch) = copy(level = 3, l3 = b)
}

data class ForgeChoice(
    val branch: ForgeBranch,
    val chip: String,
    val glossaryBody: String
)

data class ForgeRow(
    val cardId: String,
    val title: String,
    val state: ForgeSkillState,
    val pip: String,
    val enabled: Boolean,
    val greyReason: String?,
    val costLine: String?
)

object Forge {
    const val COST_L2_G = 3
    const val COST_L3_G = 2
    const val COST_L3_O = 2
    const val MAX_LEVEL = 3

    const val ID_RELIC_SHARD = "relic_shard"
    const val ID_TOWER_PIKE = "tower_pike"
    const val ID_DUST_VEIL = "dust_veil"
    const val ID_IRON_MANTLE = "iron_mantle"
    const val ID_VOW_PLATE = "vow_plate"

    val DAMAGE_TEMPLATE_IDS: Set<String> = setOf(
        "hostflint", "cinder_step", "emberbrand", "ruin_seal", "ash_press",
        "shadow_latch", "cinder_vow", "grave_nail", "ember_draw", "brand_mark",
        "spark_tithe", "wake_echo"
    )
    val BRACE_TEMPLATE_IDS: Set<String> = setOf(ID_VOW_PLATE)
    val NAMED_IDS: Set<String> = setOf(ID_TOWER_PIKE, ID_DUST_VEIL, ID_IRON_MANTLE)
    val UNMAPPED_IDS: Set<String> = setOf(ID_RELIC_SHARD)

    fun pipLabel(level: Int): String = when (level.coerceIn(1, 3)) {
        1 -> "I"
        2 -> "II"
        else -> "III"
    }

    /**
     * Combat skill-tile roman pip (v0.1.59-tilepolish).
     * Lv1: none. Lv2: II. Lv3: III. Forge list still uses [pipLabel] (I/II/III).
     */
    fun combatPipLabel(level: Int): String? = when (level.coerceAtLeast(1)) {
        1 -> null
        2 -> "II"
        else -> "III"
    }

    fun levelOf(map: Map<String, ForgeSkillState>, cardId: String): Int =
        map[cardId]?.level ?: 1

    fun stateOf(map: Map<String, ForgeSkillState>, cardId: String): ForgeSkillState =
        map[cardId] ?: ForgeSkillState()

    fun canAffordL2(scrapG: Int): Boolean = scrapG >= COST_L2_G
    fun canAffordL3(scrapG: Int, scrapO: Int): Boolean =
        scrapG >= COST_L3_G && scrapO >= COST_L3_O

    fun spendL2(scrapG: Int, scrapO: Int): Pair<Int, Int>? =
        if (canAffordL2(scrapG)) (scrapG - COST_L2_G) to scrapO else null

    fun spendL3(scrapG: Int, scrapO: Int): Pair<Int, Int>? =
        if (canAffordL3(scrapG, scrapO)) (scrapG - COST_L3_G) to (scrapO - COST_L3_O) else null

    fun isForgeable(cardId: String): Boolean = cardId !in UNMAPPED_IDS

    fun greyReason(
        cardId: String,
        state: ForgeSkillState,
        scrapG: Int,
        scrapO: Int
    ): String? {
        if (cardId in UNMAPPED_IDS) return "Unmapped"
        if (state.level >= MAX_LEVEL) return "Max level"
        return when (state.level) {
            1 -> if (!canAffordL2(scrapG)) "Need ${COST_L2_G}g" else null
            2 -> if (!canAffordL3(scrapG, scrapO)) "Need ${COST_L3_G}g+${COST_L3_O}o" else null
            else -> "Max level"
        }
    }

    fun costLine(state: ForgeSkillState): String? = when (state.level) {
        1 -> "${COST_L2_G}g → II"
        2 -> "${COST_L3_G}g+${COST_L3_O}o → III"
        else -> null
    }

    fun rows(
        loadout: List<Card>,
        forge: Map<String, ForgeSkillState>,
        scrapG: Int,
        scrapO: Int
    ): List<ForgeRow> = loadout.map { card ->
        val st = stateOf(forge, card.id)
        val reason = greyReason(card.id, st, scrapG, scrapO)
        ForgeRow(
            cardId = card.id,
            title = card.title,
            state = st,
            pip = pipLabel(st.level),
            enabled = reason == null,
            greyReason = reason,
            costLine = costLine(st)
        )
    }

    /**
     * Choice chips for next upgrade. Iron Mantle L3B uses fallback chip
     * "On Brace: deal 2" (weight hook absent).
     */
    fun choicesFor(card: Card, state: ForgeSkillState): List<ForgeChoice> {
        if (!isForgeable(card.id) || state.level >= MAX_LEVEL) return emptyList()
        val next = state.level + 1
        return when (card.id) {
            ID_TOWER_PIKE -> pikeChoices(state, next)
            ID_DUST_VEIL -> dustChoices(state, next)
            ID_IRON_MANTLE -> mantleChoices(state, next)
            ID_VOW_PLATE -> braceTemplateChoices(card, state, next)
            else -> if (card.id in DAMAGE_TEMPLATE_IDS || isDamageSkill(card))
                damageTemplateChoices(card, state, next)
            else emptyList()
        }
    }

    fun glossaryBody(card: Card, state: ForgeSkillState): String {
        if (state.level <= 1 || state.l2 == null) return card.effect.description
        val l2 = state.l2
        val l3 = state.l3
        return when (card.id) {
            ID_TOWER_PIKE -> pikeGlossary(l2, l3)
            ID_DUST_VEIL -> dustGlossary(l2, l3)
            ID_IRON_MANTLE -> mantleGlossary(l2, l3)
            ID_VOW_PLATE -> braceTemplateGlossary(5, l2, l3)
            else -> {
                val base = baseDamageOf(card) ?: return card.effect.description
                damageTemplateGlossary(base, l2, l3, card)
            }
        }
    }

    /** Apply forge picks onto a catalog card for combat / glossary. */
    fun applyCard(card: Card, state: ForgeSkillState): Card {
        if (state.level <= 1 || state.l2 == null) return card
        val body = glossaryBody(card, state)
        val effect = buildEffect(card, state, body) ?: return card.copy(
            effect = withDescription(card.effect, body)
        )
        return card.copy(effect = effect)
    }

    fun applyLoadout(cards: List<Card>, forge: Map<String, ForgeSkillState>): List<Card> =
        cards.map { applyCard(it, stateOf(forge, it.id)) }

    // --- Named: Tower Pike ---
    private fun pikeChoices(state: ForgeSkillState, next: Int): List<ForgeChoice> {
        val l2 = state.l2
        return if (next == 2) listOf(
            ForgeChoice(ForgeBranch.A, "+2 damage", "Deal **10** damage."),
            ForgeChoice(ForgeBranch.B, "Soften 1", "Deal **8** damage. **Soften 1**.")
        ) else listOf(
            ForgeChoice(
                ForgeBranch.A, "+2 damage",
                if (l2 == ForgeBranch.A) "Deal **12** damage."
                else "Deal **10** damage. **Soften 1**."
            ),
            ForgeChoice(
                ForgeBranch.B, "After fire: Brace 1",
                if (l2 == ForgeBranch.A) "Deal **10** damage. After this fires, gain **Brace 1**."
                else "Deal **8** damage. **Soften 1**. After this fires, gain **Brace 1**."
            )
        )
    }

    private fun pikeGlossary(l2: ForgeBranch, l3: ForgeBranch?): String = when {
        l3 == null && l2 == ForgeBranch.A -> "Deal **10** damage."
        l3 == null && l2 == ForgeBranch.B -> "Deal **8** damage. **Soften 1**."
        l2 == ForgeBranch.A && l3 == ForgeBranch.A -> "Deal **12** damage."
        l2 == ForgeBranch.B && l3 == ForgeBranch.A -> "Deal **10** damage. **Soften 1**."
        l2 == ForgeBranch.A && l3 == ForgeBranch.B ->
            "Deal **10** damage. After this fires, gain **Brace 1**."
        else -> "Deal **8** damage. **Soften 1**. After this fires, gain **Brace 1**."
    }

    // --- Named: Dust Veil ---
    private fun dustChoices(state: ForgeSkillState, next: Int): List<ForgeChoice> {
        val l2 = state.l2
        return if (next == 2) listOf(
            ForgeChoice(ForgeBranch.A, "Brace +1", "Deal **4** damage. Gain **Brace 3**."),
            ForgeChoice(ForgeBranch.B, "Also Soften 1", "Deal **4** damage. Gain **Brace 2**. **Soften 1**.")
        ) else listOf(
            ForgeChoice(
                ForgeBranch.A, "Brace +1",
                if (l2 == ForgeBranch.A) "Deal **4** damage. Gain **Brace 4**."
                else "Deal **4** damage. Gain **Brace 3**. **Soften 1**."
            ),
            ForgeChoice(
                ForgeBranch.B, "First fire: weight +1",
                if (l2 == ForgeBranch.A)
                    "Deal **4** damage. Gain **Brace 3**. The first time this fires this fight, this skill’s **weight +1** for the rest of the fight."
                else
                    "Deal **4** damage. Gain **Brace 2**. **Soften 1**. The first time this fires this fight, this skill’s **weight +1** for the rest of the fight."
            )
        )
    }

    private fun dustGlossary(l2: ForgeBranch, l3: ForgeBranch?): String = when {
        l3 == null && l2 == ForgeBranch.A -> "Deal **4** damage. Gain **Brace 3**."
        l3 == null && l2 == ForgeBranch.B -> "Deal **4** damage. Gain **Brace 2**. **Soften 1**."
        l2 == ForgeBranch.A && l3 == ForgeBranch.A -> "Deal **4** damage. Gain **Brace 4**."
        l2 == ForgeBranch.B && l3 == ForgeBranch.A -> "Deal **4** damage. Gain **Brace 3**. **Soften 1**."
        l2 == ForgeBranch.A && l3 == ForgeBranch.B ->
            "Deal **4** damage. Gain **Brace 3**. The first time this fires this fight, this skill’s **weight +1** for the rest of the fight."
        else ->
            "Deal **4** damage. Gain **Brace 2**. **Soften 1**. The first time this fires this fight, this skill’s **weight +1** for the rest of the fight."
    }

    // --- Named: Iron Mantle (L3B fallback = On Brace: deal 2) ---
    private fun mantleChoices(state: ForgeSkillState, next: Int): List<ForgeChoice> {
        val l2 = state.l2
        return if (next == 2) listOf(
            ForgeChoice(
                ForgeBranch.A, "Brace +1",
                "Gain **Brace 4** (absorb before HP). If Brace remains at round end, clear it."
            ),
            ForgeChoice(
                ForgeBranch.B, "On Brace: deal 2",
                "Gain **Brace 3** (absorb before HP). When this grants Brace, deal **2**. If Brace remains at round end, clear it."
            )
        ) else listOf(
            ForgeChoice(
                ForgeBranch.A, "Brace +1",
                if (l2 == ForgeBranch.A)
                    "Gain **Brace 5** (absorb before HP). If Brace remains at round end, clear it."
                else
                    "Gain **Brace 4** (absorb before HP). When this grants Brace, deal **2**. If Brace remains at round end, clear it."
            ),
            // Fallback chip — no weight hook in engine
            ForgeChoice(
                ForgeBranch.B, "On Brace: deal 2",
                if (l2 == ForgeBranch.A)
                    "Gain **Brace 4** (absorb before HP). When this grants Brace, deal **2**. If Brace remains at round end, clear it."
                else
                    "Gain **Brace 3** (absorb before HP). When this grants Brace, deal **2**. If Brace remains at round end, clear it."
            )
        )
    }

    private fun mantleGlossary(l2: ForgeBranch, l3: ForgeBranch?): String = when {
        l3 == null && l2 == ForgeBranch.A ->
            "Gain **Brace 4** (absorb before HP). If Brace remains at round end, clear it."
        l3 == null && l2 == ForgeBranch.B ->
            "Gain **Brace 3** (absorb before HP). When this grants Brace, deal **2**. If Brace remains at round end, clear it."
        l2 == ForgeBranch.A && l3 == ForgeBranch.A ->
            "Gain **Brace 5** (absorb before HP). If Brace remains at round end, clear it."
        l2 == ForgeBranch.B && l3 == ForgeBranch.A ->
            "Gain **Brace 4** (absorb before HP). When this grants Brace, deal **2**. If Brace remains at round end, clear it."
        // L3B fallback glossary
        l2 == ForgeBranch.A && l3 == ForgeBranch.B ->
            "Gain **Brace 4** (absorb before HP). When this grants Brace, deal **2**. If Brace remains at round end, clear it."
        else ->
            "Gain **Brace 3** (absorb before HP). When this grants Brace, deal **2**. If Brace remains at round end, clear it."
    }

    // --- Damage template ---
    private fun damageTemplateChoices(card: Card, state: ForgeSkillState, next: Int): List<ForgeChoice> {
        val base = baseDamageOf(card) ?: return emptyList()
        val l2 = state.l2
        val bodyA2 = damageTemplateGlossary(base, ForgeBranch.A, null, card)
        val bodyB2 = damageTemplateGlossary(base, ForgeBranch.B, null, card)
        return if (next == 2) listOf(
            ForgeChoice(ForgeBranch.A, "+2 damage", bodyA2),
            ForgeChoice(ForgeBranch.B, "Soften 1", bodyB2)
        ) else listOf(
            ForgeChoice(
                ForgeBranch.A, "+2 damage",
                damageTemplateGlossary(base, l2!!, ForgeBranch.A, card)
            ),
            ForgeChoice(
                ForgeBranch.B, "After fire: Brace 1",
                damageTemplateGlossary(base, l2, ForgeBranch.B, card)
            )
        )
    }

    private fun damageTemplateGlossary(
        base: Int,
        l2: ForgeBranch,
        l3: ForgeBranch?,
        card: Card
    ): String {
        var dmg = base
        if (l2 == ForgeBranch.A) dmg += 2
        if (l3 == ForgeBranch.A) dmg += 2
        val soften = l2 == ForgeBranch.B
        val afterBrace = l3 == ForgeBranch.B
        val extras = nonDamageClauses(card)
        val sb = StringBuilder("Deal **$dmg** damage.")
        if (extras.isNotEmpty()) sb.append(" ").append(extras)
        if (soften) sb.append(" **Soften 1**.")
        if (afterBrace) sb.append(" After this fires, gain **Brace 1**.")
        return sb.toString().replace("  ", " ").trim()
    }

    // --- Brace template (Vow Plate) ---
    private fun braceTemplateChoices(card: Card, state: ForgeSkillState, next: Int): List<ForgeChoice> {
        val base = baseBraceOf(card) ?: 5
        val l2 = state.l2
        return if (next == 2) listOf(
            ForgeChoice(ForgeBranch.A, "Brace +1", braceTemplateGlossary(base, ForgeBranch.A, null)),
            ForgeChoice(ForgeBranch.B, "Also deal 2", braceTemplateGlossary(base, ForgeBranch.B, null))
        ) else listOf(
            ForgeChoice(
                ForgeBranch.A, "Brace +1",
                braceTemplateGlossary(base, l2!!, ForgeBranch.A)
            ),
            ForgeChoice(
                ForgeBranch.B, "After fire: Soften 1",
                braceTemplateGlossary(base, l2, ForgeBranch.B)
            )
        )
    }

    private fun braceTemplateGlossary(base: Int, l2: ForgeBranch, l3: ForgeBranch?): String {
        var brace = base
        if (l2 == ForgeBranch.A) brace += 1
        if (l3 == ForgeBranch.A) brace += 1
        val onDeal = l2 == ForgeBranch.B
        val afterSoften = l3 == ForgeBranch.B
        val sb = StringBuilder("Gain **Brace $brace**.")
        if (onDeal) sb.append(" When this grants Brace, deal **2**.")
        if (afterSoften) sb.append(" After this fires, **Soften 1**.")
        return sb.toString()
    }

    // --- Effect builders ---
    private fun buildEffect(card: Card, state: ForgeSkillState, body: String): Effect? {
        val l2 = state.l2 ?: return null
        val l3 = state.l3
        val plain = body.replace("**", "")
        return when (val e = card.effect) {
            is SkillEffect.Damage -> {
                val dmg = damageAfter(e.damage, l2, l3, damageTree = card.id != ID_DUST_VEIL)
                val soften = softenAfter(card.id, l2, l3)
                val afterBrace = afterBraceDamageTree(card.id, l3)
                if (soften > 0 || afterBrace > 0) {
                    MoveEffect.DamageAndSoften(
                        e.id, e.name, plain, e.rarity, dmg,
                        counterPenalty = soften, braceGain = afterBrace
                    )
                } else {
                    SkillEffect.Damage(e.id, e.name, plain, e.rarity, dmg)
                }
            }
            is MoveEffect.DamageAndSoften -> {
                var dmg = e.damage
                var brace = e.braceGain
                var soften = e.counterPenalty
                when (card.id) {
                    ID_DUST_VEIL -> {
                        // L2A/L3A brace +1; L2B soften 1; L3B weight handled in combat
                        if (l2 == ForgeBranch.A) brace += 1
                        if (l3 == ForgeBranch.A) brace += 1
                        if (l2 == ForgeBranch.B) soften += 1
                    }
                    else -> {
                        if (l2 == ForgeBranch.A) dmg += 2
                        if (l3 == ForgeBranch.A) dmg += 2
                        if (l2 == ForgeBranch.B) soften += 1
                        if (l3 == ForgeBranch.B) brace += 1 // after fire brace via braceGain
                    }
                }
                MoveEffect.DamageAndSoften(
                    e.id, e.name, plain, e.rarity, dmg,
                    counterPenalty = soften, braceGain = brace
                )
            }
            is SkillEffect.DamageAndHeal -> {
                var dmg = e.damage
                if (l2 == ForgeBranch.A) dmg += 2
                if (l3 == ForgeBranch.A) dmg += 2
                val soften = if (l2 == ForgeBranch.B) 1 else 0
                val afterBrace = if (l3 == ForgeBranch.B) 1 else 0
                if (soften > 0 || afterBrace > 0) {
                    MoveEffect.DamageAndSoften(
                        e.id, e.name, plain, e.rarity, dmg,
                        counterPenalty = soften, braceGain = afterBrace
                    )
                } else {
                    SkillEffect.DamageAndHeal(e.id, e.name, plain, e.rarity, dmg, e.heal)
                }
            }
            is SkillEffect.DamageAndBraceIfAshPips -> {
                var dmg = e.damage
                if (l2 == ForgeBranch.A) dmg += 2
                if (l3 == ForgeBranch.A) dmg += 2
                SkillEffect.DamageAndBraceIfAshPips(
                    e.id, e.name, plain, e.rarity, dmg, e.brace, e.minPips,
                    afterFireBrace = if (l3 == ForgeBranch.B) 1 else 0,
                    extraSoften = if (l2 == ForgeBranch.B) 1 else 0
                )
            }
            is SkillEffect.EmberPoolSkill -> {
                var dmg = e.damage
                if (l2 == ForgeBranch.A) dmg += 2
                if (l3 == ForgeBranch.A) dmg += 2
                e.copy(
                    description = plain,
                    damage = dmg,
                    afterFireBrace = if (l3 == ForgeBranch.B) 1 else 0,
                    extraSoften = if (l2 == ForgeBranch.B) 1 else 0
                )
            }
            is Equipment.GainBrace -> {
                var brace = e.brace
                var onDeal = 0
                var afterSoften = 0
                when (card.id) {
                    ID_IRON_MANTLE -> {
                        if (l2 == ForgeBranch.A) brace += 1
                        if (l3 == ForgeBranch.A) brace += 1
                        // L2B or L3B fallback → on brace deal 2
                        if (l2 == ForgeBranch.B || l3 == ForgeBranch.B) onDeal = 2
                    }
                    else -> {
                        if (l2 == ForgeBranch.A) brace += 1
                        if (l3 == ForgeBranch.A) brace += 1
                        if (l2 == ForgeBranch.B) onDeal = 2
                        if (l3 == ForgeBranch.B) afterSoften = 1
                    }
                }
                Equipment.GainBrace(
                    e.id, e.name, plain, e.rarity, brace,
                    onBraceDeal = onDeal, afterFireSoften = afterSoften
                )
            }
            else -> withDescription(e, plain)
        }
    }

    private fun damageAfter(base: Int, l2: ForgeBranch, l3: ForgeBranch?, damageTree: Boolean): Int {
        if (!damageTree) return base
        var d = base
        if (l2 == ForgeBranch.A) d += 2
        if (l3 == ForgeBranch.A) d += 2
        return d
    }

    private fun softenAfter(cardId: String, l2: ForgeBranch, l3: ForgeBranch?): Int =
        if (cardId != ID_DUST_VEIL && l2 == ForgeBranch.B) 1 else 0

    private fun afterBraceDamageTree(cardId: String, l3: ForgeBranch?): Int =
        if (cardId != ID_DUST_VEIL && l3 == ForgeBranch.B) 1 else 0

    private fun baseDamageOf(card: Card): Int? = when (val e = card.effect) {
        is SkillEffect.Damage -> e.damage
        is SkillEffect.DamageAndHeal -> e.damage
        is SkillEffect.DamageAndBraceIfAshPips -> e.damage
        is SkillEffect.EmberPoolSkill -> e.damage
        is MoveEffect.DamageAndSoften -> e.damage
        else -> null
    }

    private fun baseBraceOf(card: Card): Int? = when (val e = card.effect) {
        is Equipment.GainBrace -> e.brace
        else -> null
    }

    private fun isDamageSkill(card: Card): Boolean = baseDamageOf(card) != null

    /** Keep non-damage clauses from catalog (e.g. heal, spark lines) when formatting template. */
    private fun nonDamageClauses(card: Card): String {
        val d = card.effect.description
        // Strip leading "Deal N damage." / "Deal N."
        val stripped = d
            .replace(Regex("""^Deal \d+ damage\.?\s*"""), "")
            .replace(Regex("""^Deal \d+\.\s*"""), "")
            .trim()
        return stripped
    }

    private fun withDescription(e: Effect, desc: String): Effect = when (e) {
        is SkillEffect.Damage -> e.copy(description = desc)
        is SkillEffect.DamageAndHeal -> e.copy(description = desc)
        is SkillEffect.DamageAndBraceIfAshPips -> e.copy(description = desc)
        is SkillEffect.EmberPoolSkill -> e.copy(description = desc)
        is MoveEffect.DamageAndSoften -> e.copy(description = desc)
        is Equipment.GainBrace -> e.copy(description = desc)
        is Equipment.HealOrBrace -> e.copy(description = desc)
    }

    // --- Mid-run encode helpers ---
    fun encodeLevels(map: Map<String, ForgeSkillState>): Map<String, Int> =
        map.mapValues { it.value.level.coerceIn(1, 3) }

    fun encodeBranches(map: Map<String, ForgeSkillState>): Map<String, String> =
        map.mapNotNull { (id, st) ->
            val l2 = st.l2?.name ?: return@mapNotNull null
            val l3 = st.l3?.name ?: ""
            id to "$l2$l3"
        }.toMap()

    fun decodeState(level: Int, branch: String?): ForgeSkillState {
        val l = level.coerceIn(1, 3)
        if (branch.isNullOrBlank()) return ForgeSkillState(level = l)
        val l2 = branch.getOrNull(0)?.let { runCatching { ForgeBranch.valueOf(it.toString()) }.getOrNull() }
        val l3 = branch.getOrNull(1)?.let { runCatching { ForgeBranch.valueOf(it.toString()) }.getOrNull() }
        return ForgeSkillState(level = l, l2 = l2, l3 = l3)
    }
}
