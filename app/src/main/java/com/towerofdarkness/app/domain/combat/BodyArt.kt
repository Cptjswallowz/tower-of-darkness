package com.towerofdarkness.app.domain.combat

/**
 * Combat body / portrait stills — v0.1.21-portraits + v0.1.26-packs trash looks.
 * Pure mapping for unit tests (no Compose).
 * Aligns with docs/portraits-v0121.md + docs/packs-v0126.md + docs/art-audio/PACKS_v0.1.26.md.
 */
object BodyArt {
    /** You — fallen-kingdom soldier circular still (title + combat). */
    const val PLAYER_DRAWABLE = "portrait_you"

    /** Floor-2 Ash-Warden boss only. */
    const val ASH_WARDEN_DRAWABLE = "portrait_ash_warden"

    /** Floor-1 Seal-Warden boss ([EnemyKind.DRAGON]) only. */
    const val SEAL_WARDEN_DRAWABLE = "portrait_seal_warden"

    /** Expected Art drop-in MD5s (PNG bytes) — contract with prep_portraits_v0121. */
    const val PLAYER_MD5 = "ed5064c12745ad7f149f9a313b673819"
    const val ASH_WARDEN_MD5 = "4694b96d0f5355c878f7acebdb0141a6"
    const val SEAL_WARDEN_MD5 = "1259b05113315ed78fc40630efc42d61"

    /** Pack look MD5s — contract with prep_packs_v0126 / Art READY. */
    const val GOBLIN_KNIFE_MD5 = "c1ccc07f907db3b6bfad687410bb0b4e"
    const val GOBLIN_BOTTLE_MD5 = "3caaee488d85a5d4efad5c179e660224"
    const val GOBLIN_SPIKES_MD5 = "9bdd728e852e5d9f0c4ade5b33ec9f67"
    const val ORC_AXE_MD5 = "a1d972dfe6d220420119159652a5a868"
    const val ORC_CLEAVER_MD5 = "6fc981b9e7dff416b6183def2c704c75"
    const val ORC_HAMMER_MD5 = "bc61505abd0a37a89f504e844804169a"

    /** Combat player portrait slot height (dp) — under frames / pips. */
    const val PLAYER_SLOT_DP = 90

    /** Boss portrait slot when body art is present (dp). */
    const val BOSS_SLOT_DP = 160

    /** Trash pack PNG slot (~120). Shrink Image if clips pips — do not move pips. */
    const val TRASH_SLOT_DP = 120

    /** Inset fraction so sprite sits under chrome without moving pips. */
    const val SPRITE_INSET_FRACTION = 0.08f

    fun playerDrawableName(): String = PLAYER_DRAWABLE

    /** Boss / You portrait drawable basenames (v0.1.21). */
    fun portraitDrawableNames(): Set<String> = setOf(
        PLAYER_DRAWABLE,
        ASH_WARDEN_DRAWABLE,
        SEAL_WARDEN_DRAWABLE
    )

    /**
     * Enemy still drawable name, or null → keep [EnemySilhouette] Canvas placeholder.
     * Bosses: [EnemyKind.ASH_WARDEN] (F2), [EnemyKind.DRAGON] / Seal-Warden (F1).
     * Hallway packs (v0.1.26): look → portrait_weak_goblin_* / portrait_sturdy_orc_*.
     */
    fun enemyPortraitDrawableName(kind: EnemyKind, look: EnemyLook? = null): String? = when (kind) {
        EnemyKind.ASH_WARDEN -> ASH_WARDEN_DRAWABLE
        EnemyKind.DRAGON -> SEAL_WARDEN_DRAWABLE
        else -> look?.let { HallwayPacks.drawableName(it) }
    }

    fun usesPlaceholderSilhouette(kind: EnemyKind, look: EnemyLook? = null): Boolean {
        val name = enemyPortraitDrawableName(kind, look) ?: return true
        if (kind == EnemyKind.ASH_WARDEN || kind == EnemyKind.DRAGON) return false
        return !packArtShipped(name)
    }

    /** Hallway / path: You is soldier; trash enemies use pack PNGs when look authored. */
    fun hallwayPlayerDrawableName(): String = PLAYER_DRAWABLE

    fun hallwayEnemyUsesPlaceholder(): Boolean = !anyPackArtShipped()

    /** Shipped pack basenames (Art READY v0.1.26). */
    fun packDrawableNames(): Set<String> = HallwayPacks.allDrawableNames()

    fun packMd5ByDrawable(): Map<String, String> = mapOf(
        HallwayPacks.drawableName(EnemyLook.KNIFE) to GOBLIN_KNIFE_MD5,
        HallwayPacks.drawableName(EnemyLook.BOTTLE) to GOBLIN_BOTTLE_MD5,
        HallwayPacks.drawableName(EnemyLook.SPIKES) to GOBLIN_SPIKES_MD5,
        HallwayPacks.drawableName(EnemyLook.AXE) to ORC_AXE_MD5,
        HallwayPacks.drawableName(EnemyLook.CLEAVER) to ORC_CLEAVER_MD5,
        HallwayPacks.drawableName(EnemyLook.HAMMER) to ORC_HAMMER_MD5
    )

    /**
     * Art drop detection. Override for tests; otherwise known shipped set OR file on disk.
     */
    @Volatile
    var packArtPresentOverride: Set<String>? = null

    fun packArtShipped(drawableName: String): Boolean {
        packArtPresentOverride?.let { return drawableName in it }
        if (drawableName in packDrawableNames()) return true
        val candidates = listOf(
            "app/src/main/res/drawable/$drawableName.png",
            "src/main/res/drawable/$drawableName.png"
        )
        return candidates.any { java.io.File(it).isFile }
    }

    fun anyPackArtShipped(): Boolean =
        HallwayPacks.allDrawableNames().any { packArtShipped(it) }
}
