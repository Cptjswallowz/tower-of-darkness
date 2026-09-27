package com.towerofdarkness.app.domain.hub

import com.towerofdarkness.app.domain.climb.ClimbKept

/**
 * Hub Relics section — v0.1.55-hubsplit PART B.
 * Display-only trophies (owned only). Exact earn lines from docs/hubsplit-v0155.md.
 */
data class HubRelicRow(
    val id: String,
    val name: String,
    val earnLine: String
)

object HubRelics {
    /** Empty Relics copy — exact. */
    const val EMPTY_COPY = "Nothing kept yet. Survive a floor."

    const val SECTION_RELICS = "Relics"
    const val SECTION_PERKS = "Perks"
    const val SECTION_SKILLS = "Skills"

    /** Sticky section header order. */
    val SECTION_ORDER: List<String> = listOf(SECTION_RELICS, SECTION_PERKS, SECTION_SKILLS)

    val ALL: List<HubRelicRow> = listOf(
        HubRelicRow(ClimbKept.ID_SOOT_RIM, ClimbKept.NAME_SOOT_RIM, "Reach Floor 2"),
        HubRelicRow(ClimbKept.ID_ASH_PAULDRON, ClimbKept.NAME_ASH_PAULDRON, "Reach Floor 3"),
        HubRelicRow(ClimbKept.ID_TROLL_TOOTH, ClimbKept.NAME_TROLL_TOOTH, "Kill Cave Troll"),
        HubRelicRow(ClimbKept.ID_GATE_SIGIL, ClimbKept.NAME_GATE_SIGIL, "Beat Gate-Warden")
    )

    /** Owned trophies only — no grey unowned rows. */
    fun ownedRows(unlocks: Set<String>): List<HubRelicRow> =
        ALL.filter { it.id in unlocks }

    fun earnLine(id: String): String? = ALL.find { it.id == id }?.earnLine

    /** PERKS section order (prices/effect lines live on HubOffers). */
    val PERK_IDS: List<String> = listOf(
        HubOffers.ID_SCOUT,
        HubOffers.ID_EXTRA_RUMOR,
        HubOffers.ID_HOSTBLOOD,
        HubOffers.ID_WARM_ASH,
        HubOffers.ID_ASH_TITHE
    )

    /** SKILLS section order. */
    val SKILL_IDS: List<String> = listOf(
        HubOffers.ID_HOST_OF_EMBERS,
        HubOffers.ID_IRON_LESSON,
        HubOffers.ID_COLD_DRAW,
        HubOffers.ID_BRAND_LESSON,
        HubOffers.ID_SPARK_LESSON,
        HubOffers.ID_ECHO_LESSON
    )

    fun perkOffers(): List<HubOffer> = PERK_IDS.mapNotNull { HubOffers.byId(it) }

    fun skillOffers(): List<HubOffer> = SKILL_IDS.mapNotNull { HubOffers.byId(it) }
}
