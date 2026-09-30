package com.towerofdarkness.app.domain.specials

import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.domain.cards.Card

/**
 * Specials — v0.1.64-specials P1.
 * Loadout gate, bag filter, L/M ids. See docs/specials-v0164.md.
 */
object Specials {
    const val ID_GRAVE_BRAND = "grave_brand"
    const val ID_ASH_VOW = "ash_vow"

    const val GRAVE_BRAND_CAP = 3
    const val GRAVE_BRAND_DAMAGE = 12
    const val GRAVE_BRAND_SOFTEN = 2

    const val ASH_VOW_BRACE = 4
    const val ASH_VOW_NEXT_REGULAR_BONUS = 4

    /** Exact Confirm disabled reason (WO lock). */
    const val LOADOUT_REASON =
        "Need 3 regulars + 1 Legendary + 1 Mythic."

    val SPECIAL_IDS: Set<String> = setOf(ID_GRAVE_BRAND, ID_ASH_VOW)

    fun isRegular(card: Card): Boolean =
        card.rarity == Rarity.COMMON ||
            card.rarity == Rarity.UNCOMMON ||
            card.rarity == Rarity.RARE

    fun isLegendary(card: Card): Boolean = card.rarity == Rarity.LEGENDARY

    fun isMythic(card: Card): Boolean = card.rarity == Rarity.MYTHIC

    fun isSpecial(card: Card): Boolean =
        card.id in SPECIAL_IDS || isLegendary(card) || isMythic(card)

    /** Dice bag = live regulars only (never L/M/Wake/Ashbrand). */
    fun bagCards(activeCards: List<Card>): List<Card> =
        activeCards.filter { isRegular(it) }

    fun regularIds(activeCards: List<Card>): Set<String> =
        bagCards(activeCards).map { it.id }.toSet()

    /**
     * Confirm gate: exactly 3 regulars + 1 Legendary + 1 Mythic (size 5).
     * Ashbrand is the separate weapon slot — never a skill tile.
     */
    fun loadoutMeetsGate(cards: List<Card>): Boolean {
        if (cards.size != 5) return false
        val regs = cards.count { isRegular(it) }
        val legs = cards.count { isLegendary(it) }
        val myths = cards.count { isMythic(it) }
        return regs == 3 && legs == 1 && myths == 1
    }

    fun loadoutGateReason(cards: List<Card>): String? =
        if (loadoutMeetsGate(cards)) null else LOADOUT_REASON

    /** Grant Grave Brand + Ash Vow when either L or M unlock is missing. */
    fun grantClimbSpecialsIfMissing(unlocked: Set<String>): Set<String> {
        val hasL = unlocked.any { id ->
            id == ID_GRAVE_BRAND
        }
        val hasM = unlocked.any { id ->
            id == ID_ASH_VOW
        }
        if (hasL && hasM) return unlocked
        return unlocked + ID_GRAVE_BRAND + ID_ASH_VOW
    }
}
