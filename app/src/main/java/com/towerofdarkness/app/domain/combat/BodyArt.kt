package com.towerofdarkness.app.domain.combat

/**
 * Combat body stills — v0.1.18-bodies.
 * Pure mapping for unit tests (no Compose). Drawables:
 * [PLAYER_DRAWABLE], [ASH_WARDEN_DRAWABLE].
 * Wake art frozen. No wardrobe / poses / hit-flash.
 * Aligns with docs/bodies-v0118.md + docs/art-audio/BODIES_v0.1.18.md.
 */
object BodyArt {
    /** You — fallen-kingdom soldier circular still. */
    const val PLAYER_DRAWABLE = "portrait_you"

    /** Floor-2 Ash-Warden boss only. */
    const val ASH_WARDEN_DRAWABLE = "portrait_ash_warden"

    /** Combat player portrait slot height (dp) — under frames / pips. */
    const val PLAYER_SLOT_DP = 90

    /** Boss portrait slot when body art is present (dp). */
    const val BOSS_SLOT_DP = 160

    /** Trash / F1 boss keep Canvas silhouette (~120 / ~160). */
    const val TRASH_SLOT_DP = 120

    /** Inset fraction so sprite sits under chrome without moving pips. */
    const val SPRITE_INSET_FRACTION = 0.08f

    fun playerDrawableName(): String = PLAYER_DRAWABLE

    /**
     * Enemy still drawable name, or null → keep [EnemySilhouette] Canvas placeholder.
     * Only [EnemyKind.ASH_WARDEN] (F2 boss) is asset-backed this pass.
     */
    fun enemyPortraitDrawableName(kind: EnemyKind): String? = when (kind) {
        EnemyKind.ASH_WARDEN -> ASH_WARDEN_DRAWABLE
        else -> null
    }

    fun usesPlaceholderSilhouette(kind: EnemyKind): Boolean =
        enemyPortraitDrawableName(kind) == null

    /** Hallway / path / trash approach: You is soldier; enemy markers stay placeholders. */
    fun hallwayPlayerDrawableName(): String = PLAYER_DRAWABLE

    fun hallwayEnemyUsesPlaceholder(): Boolean = true
}
