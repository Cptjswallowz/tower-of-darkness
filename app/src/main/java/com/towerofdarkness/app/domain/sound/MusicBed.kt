package com.towerofdarkness.app.domain.sound

/**
 * v0.1.62-score — app-wide music bed slots.
 * Runtime assets: `app/src/main/assets/music/<slot>.ogg`
 * Curated mirror: `assets/music/<slot>.ogg`
 */
enum class MusicBed(val slot: String, val loops: Boolean) {
    TITLE("title", true),
    HUB("hub", true),
    PATH("path", true),
    LOADOUT("loadout", true),
    COMBAT("combat", true),
    ELITE("elite", true),
    BOSS("boss", true),
    SHOP("shop", true),
    REST("rest", true),
    VICTORY("victory", false),
    DEFEAT("defeat", false);

    val assetPath: String get() = "music/$slot.ogg"

    companion object {
        val ALL_SLOTS: List<String> = entries.map { it.slot }
        fun fromSlot(slot: String): MusicBed? = entries.firstOrNull { it.slot == slot }
    }
}
