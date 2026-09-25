package com.towerofdarkness.app.domain.hub

/**
 * Hub remnant shop — v0.1.27-hub offers + v0.1.28-hubkeep grant/migration.
 * Four fixed offers; persist unlock ids in meta unlocked set + remnants_bank.
 * See docs/hub-v0127.md and docs/hubkeep-v0128.md.
 */
data class HubOffer(
    val id: String,
    val title: String,
    val effectLine: String,
    val cost: Int,
    /** Card id gated into loadout pool when this offer is owned (skills only). */
    val unlocksCardId: String? = null
)

enum class HubCta { BUY, OWNED, CANT_AFFORD }

object HubOffers {
    const val ID_SCOUT = "scout_charge"
    const val ID_EXTRA_RUMOR = "extra_rumor"
    const val ID_HOST_OF_EMBERS = "host_of_embers"
    const val ID_IRON_LESSON = "iron_lesson"

    const val CARD_CINDER_VOW = "cinder_vow"
    const val CARD_GRAVE_NAIL = "grave_nail"

    /** Legacy Clear Fog perk id (pre-Hub ladder). */
    const val LEGACY_RUMOR_CLARITY = "rumor_clarity"

    /** Always-on free rumor re-rolls per floor (hubkeep). Never charged. */
    const val BASELINE_RUMOR_PER_FLOOR = 1

    val all: List<HubOffer> = listOf(
        HubOffer(ID_SCOUT, "Scout", "+1 Free Scout / climb", 8),
        HubOffer(ID_EXTRA_RUMOR, "Extra rumor", "+1 rumor re-roll / floor", 6),
        HubOffer(ID_HOST_OF_EMBERS, "Host of Embers", "Unlock skill Cinder Vow", 12, CARD_CINDER_VOW),
        HubOffer(ID_IRON_LESSON, "Iron Lesson", "Unlock skill Grave Nail", 12, CARD_GRAVE_NAIL)
    )

    fun byId(id: String): HubOffer? = all.find { it.id == id }

    /** Title screen bank line — matches MainMenu Hub button. */
    fun titleBankLine(bank: Int): String = "Hub · $bank remnants"

    /** Hub screen top bank line. */
    fun hubBankLine(bank: Int): String = "Remnants  $bank"

    fun hubScreenTitle(): String = "Hub"

    fun cta(offer: HubOffer, bank: Int, owned: Set<String>): HubCta = when {
        offer.id in owned -> HubCta.OWNED
        bank < offer.cost -> HubCta.CANT_AFFORD
        else -> HubCta.BUY
    }

    fun ctaLabel(cta: HubCta): String = when (cta) {
        HubCta.BUY -> "Buy"
        HubCta.OWNED -> "OWNED"
        HubCta.CANT_AFFORD -> "Can't afford"
    }

    /**
     * Pure buy: spend bank, append unlock id. Null if owned or can't afford.
     * Mirrors MetaStore remnants_bank + unlocked_cards write.
     */
    fun applyBuy(bank: Int, unlocks: Set<String>, offerId: String): HubMetaSnapshot? {
        val offer = byId(offerId) ?: return null
        if (offerId in unlocks) return null
        if (bank < offer.cost) return null
        return HubMetaSnapshot(bank - offer.cost, unlocks + offerId)
    }

    /** Climb-start Free Scout charges from owned Hub Scout (no free baseline). */
    fun freeScoutChargesAtClimbStart(unlocks: Set<String>): Int =
        if (ID_SCOUT in unlocks) 1 else 0

    /** Hub Extra rumor stack amount (+1 / floor when owned). */
    fun extraRumorPerFloor(unlocks: Set<String>): Int =
        if (ID_EXTRA_RUMOR in unlocks) 1 else 0

    /**
     * Rumor re-rolls at a floor start (F1 climb start and FloorBreak→F2 grant amount).
     * Baseline 1 always + Extra rumor stack. See hubkeep-v0128.md.
     */
    fun rumorRerollsAtFloorStart(unlocks: Set<String>): Int =
        BASELINE_RUMOR_PER_FLOOR + extraRumorPerFloor(unlocks)

    /** Climb / F1 start — same as floor start (baseline + Extra). */
    fun rumorRerollsAtClimbStart(unlocks: Set<String>): Int =
        rumorRerollsAtFloorStart(unlocks)

    /** FloorBreak → F2: add (baseline + Extra) to remaining wallet. */
    fun rumorRerollsOnFloorAdvance(unlocks: Set<String>): Int =
        rumorRerollsAtFloorStart(unlocks)

    /**
     * One-shot unlock migration (hubkeep). Bank unchanged by caller.
     * Maps legacy / card ids → Hub OWNED rows; never removes other unlocks.
     */
    fun migrateUnlocksForHubkeep(unlocks: Set<String>): Set<String> {
        val out = unlocks.toMutableSet()
        if (LEGACY_RUMOR_CLARITY in out) out += ID_EXTRA_RUMOR
        if (CARD_CINDER_VOW in out) out += ID_HOST_OF_EMBERS
        if (CARD_GRAVE_NAIL in out) out += ID_IRON_LESSON
        return out
    }

    fun skillUnlocked(cardId: String, unlocks: Set<String>): Boolean = when (cardId) {
        CARD_CINDER_VOW -> ID_HOST_OF_EMBERS in unlocks || CARD_CINDER_VOW in unlocks
        CARD_GRAVE_NAIL -> ID_IRON_LESSON in unlocks || CARD_GRAVE_NAIL in unlocks
        else -> true
    }
}

/**
 * Pure meta_v0 bank + unlocks snapshot for unit tests (kill-app persist round-trip).
 * Live path: MetaStore KEY_REMNANTS + KEY_UNLOCKED.
 */
data class HubMetaSnapshot(
    val remnantsBank: Int,
    val unlocks: Set<String>
) {
    fun encode(): String {
        val ids = unlocks.sorted().joinToString(",")
        return "meta_v0|bank=$remnantsBank|unlocks=$ids"
    }

    companion object {
        fun decode(raw: String): HubMetaSnapshot? {
            if (!raw.startsWith("meta_v0|")) return null
            val parts = raw.split("|")
            val bank = parts.getOrNull(1)?.removePrefix("bank=")?.toIntOrNull() ?: return null
            val unlocksRaw = parts.getOrNull(2)?.removePrefix("unlocks=") ?: ""
            val unlocks = if (unlocksRaw.isBlank()) emptySet()
            else unlocksRaw.split(",").filter { it.isNotBlank() }.toSet()
            return HubMetaSnapshot(bank, unlocks)
        }
    }
}
