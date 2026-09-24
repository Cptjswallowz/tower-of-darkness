package com.towerofdarkness.app.domain.combat

/**
 * Combat body / portrait stills — v0.1.21-portraits.
 * Pure mapping for unit tests (no Compose). Drawables:
 * [PLAYER_DRAWABLE], [ASH_WARDEN_DRAWABLE], [SEAL_WARDEN_DRAWABLE].
 * Wake art frozen. No wardrobe / poses / hit-flash.
 * Aligns with docs/portraits-v0121.md + docs/art-audio/PORTRAITS_v0.1.21.md.
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

    /** Combat player portrait slot height (dp) — under frames / pips. */
    const val PLAYER_SLOT_DP = 90

    /** Boss portrait slot when body art is present (dp). */
    const val BOSS_SLOT_DP = 160

    /** Trash keep Canvas silhouette (~120). */
    const val TRASH_SLOT_DP = 120

    /** Inset fraction so sprite sits under chrome without moving pips. */
    const val SPRITE_INSET_FRACTION = 0.08f

    fun playerDrawableName(): String = PLAYER_DRAWABLE

    /** All portrait drawable basenames shipped this WO. */
    fun portraitDrawableNames(): Set<String> = setOf(
        PLAYER_DRAWABLE,
        ASH_WARDEN_DRAWABLE,
        SEAL_WARDEN_DRAWABLE
    )

    /**
     * Enemy still drawable name, or null → keep [EnemySilhouette] Canvas placeholder.
     * Bosses: [EnemyKind.ASH_WARDEN] (F2), [EnemyKind.DRAGON] / Seal-Warden (F1).
     * Hallway packs (v0.1.26): look → pack_* drawable when Art PNG is shipped; else null.
     */
    fun enemyPortraitDrawableName(kind: EnemyKind, look: EnemyLook? = null): String? = when (kind) {
        EnemyKind.ASH_WARDEN -> ASH_WARDEN_DRAWABLE
        EnemyKind.DRAGON -> SEAL_WARDEN_DRAWABLE
        else -> look?.let { HallwayPacks.drawableName(it) }
    }

    fun usesPlaceholderSilhouette(kind: EnemyKind, look: EnemyLook? = null): Boolean {
        val name = enemyPortraitDrawableName(kind, look) ?: return true
        if (kind == EnemyKind.ASH_WARDEN || kind == EnemyKind.DRAGON) return false
        // Trash pack PNG: placeholder until Art drops the file
        return !packArtShipped(name)
    }

    /** Hallway / path / trash approach: You is soldier; enemy markers placeholders until pack PNGs. */
    fun hallwayPlayerDrawableName(): String = PLAYER_DRAWABLE

    fun hallwayEnemyUsesPlaceholder(): Boolean = !anyPackArtShipped()

    /**
     * Art drop detection — true when the named pack PNG exists under res/drawable.
     * Unit tests override via [packArtPresentOverride]; production checks classpath file list
     * is not available, so default is false until PNGs are committed (hooks stay ready).
     */
    @Volatile
    var packArtPresentOverride: Set<String>? = null

    fun packArtShipped(drawableName: String): Boolean {
        packArtPresentOverride?.let { return drawableName in it }
        val candidates = listOf(
            "app/src/main/res/drawable/$drawableName.png",
            "src/main/res/drawable/$drawableName.png"
        )
        return candidates.any { java.io.File(it).isFile }
    }

    fun anyPackArtShipped(): Boolean =
        HallwayPacks.allDrawableNames().any { packArtShipped(it) }

    /** Expected drawable basenames for Art drop (no plate behind PNG). */
    fun packDrawableNames(): Set<String> = HallwayPacks.allDrawableNames()
}
