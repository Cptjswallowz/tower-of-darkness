package com.towerofdarkness.app.domain

enum class Rarity(val displayName: String) {
    COMMON("Common"),
    UNCOMMON("Uncommon"),
    RARE("Rare"),
    EPIC("Epic"),
    LEGENDARY("Legendary"),
    /** v0.1.64-specials — Ash Vow tier; never in dice bag. */
    MYTHIC("Mythic")
}
