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
     * Trash / Wretch / Seal Spinner stay placeholders.
     */
    fun enemyPortraitDrawableName(kind: EnemyKind): String? = when (kind) {
        EnemyKind.ASH_WARDEN -> ASH_WARDEN_DRAWABLE
        EnemyKind.DRAGON -> SEAL_WARDEN_DRAWABLE
        else -> null
    }

    fun usesPlaceholderSilhouette(kind: EnemyKind): Boolean =
        enemyPortraitDrawableName(kind) == null

    /** Hallway / path / trash approach: You is soldier; enemy markers stay placeholders. */
    fun hallwayPlayerDrawableName(): String = PLAYER_DRAWABLE

    fun hallwayEnemyUsesPlaceholder(): Boolean = true
}
