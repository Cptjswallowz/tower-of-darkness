package com.towerofdarkness.app.domain.sound

/**
 * v0.1.63-scorefade — app-wide music bed slots.
 * Runtime assets: `app/src/main/assets/music/<slot>.ogg`
 * Curated mirror: `assets/music/<slot>.ogg`
 *
 * ONCE (loops=false): title, loadout, shop, rest, victory, defeat —
 * play once with tail fade, then silence on that screen.
 * LOOP (loops=true): hub, path, combat, elite, boss —
 * dual-player crossfade (never MediaPlayer.isLooping).
 */
enum class MusicBed(val slot: String, val loops: Boolean) {
    TITLE("title", false),
    HUB("hub", true),
    PATH("path", true),
    LOADOUT("loadout", false),
    COMBAT("combat", true),
    ELITE("elite", true),
    BOSS("boss", true),
    SHOP("shop", false),
    REST("rest", false),
    VICTORY("victory", false),
    DEFEAT("defeat", false);

    val assetPath: String get() = "music/$slot.ogg"

    companion object {
        val ALL_SLOTS: List<String> = entries.map { it.slot }
        fun fromSlot(slot: String): MusicBed? = entries.firstOrNull { it.slot == slot }
    }
}
