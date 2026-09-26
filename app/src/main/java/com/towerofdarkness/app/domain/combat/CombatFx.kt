package com.towerofdarkness.app.domain.combat

/**
 * Shared combat FX kernel — v0.1.37-fx.
 * Pure domain maps + duration helpers; Compose renders flash/stroke/float/shake.
 * Never changes damage / Wake math. See docs/fx-v0137.md.
 *
 * Kernel order on skill resolve: flash → stroke (unless NO STROKE) → float → shake → log+hold.
 * 2x halves FX durations via [fxHoldMs] (same pattern as combatHoldMs).
 * Fail-safe: [safeSpec] never throws; UI must still resolve+log if FX stubs.
 */
enum class FxTier {
    SMALL,
    MEDIUM,
    WAKE,
    NO_STROKE
}

enum class FxRole {
    YOU,
    WEAK_GOBLIN,
    STURDY_ORC,
    SEAL_WARDEN,
    ASH_WARDEN
}

/** Stroke direction: player skills You→foe; enemy skills foe→You. */
enum class FxStrokeDir {
    YOU_TO_FOE,
    FOE_TO_YOU
}

/**
 * Presentation stroke spec (ARGB + relative thickness).
 * [thickness] 0 = no stroke; Small ~1; Medium ~2. Wake uses WakeStageOverlay instead.
 */
data class FxStrokeSpec(
    val colorArgb: Long,
    val thickness: Float,
    val dir: FxStrokeDir
)

data class FxShakeSpec(
    /** Peak translation px-ish; 0 = none. */
    val amplitude: Float,
    /** Oscillation count at 1x. */
    val pulses: Int
)

/**
 * Full beat presentation for one resolve line that carries an [CombatEvent.fxId].
 * Durations are 1x base ms — scale with [CombatFx.fxHoldMs].
 */
data class FxBeatSpec(
    val tier: FxTier,
    val role: FxRole,
    val stroke: FxStrokeSpec?,
    val shake: FxShakeSpec,
    val flashMs: Long,
    val strokeMs: Long,
    val floatMs: Long,
    val shakeMs: Long,
    /** When true, keep WakeStageOverlay slash; do not draw kernel stroke. */
    val useWakeSlash: Boolean = false
)

object CombatFx {
    const val TAG = "v0.1.37-fx"

    /** Special id for Ashbrand FULL Wake (not a card id). */
    const val ID_ASHBRAND_WAKE = "ashbrand_wake"

    // --- Colors (ARGB) — role stroke lock ---
    const val COLOR_YOU = 0xFFC9A227L          // gold ember
    const val COLOR_WEAK_GOBLIN = 0xFF6B7A3AL  // dirty green
    const val COLOR_STURDY_ORC = 0xFFA05030L   // rust
    const val COLOR_SEAL_WARDEN = 0xFFB87333L  // copper
    const val COLOR_ASH_WARDEN = 0xFFC45A2DL   // coal orange

    // --- 1x duration budgets (presentation only) ---
    const val FLASH_MS = 120L
    const val STROKE_SMALL_MS = 200L
    const val STROKE_MEDIUM_MS = 280L
    const val FLOAT_MS = 700L
    const val SHAKE_MEDIUM_MS = 240L
    const val SHAKE_WAKE_MS = 400L

    const val THICK_SMALL = 1.5f
    const val THICK_MEDIUM = 3.0f
    const val SHAKE_SMALL_AMP = 0f
    const val SHAKE_MEDIUM_AMP = 4f
    const val SHAKE_WAKE_AMP = 12f

    /** Player SMALL ids (lock table). */
    val PLAYER_SMALL: Set<String> = setOf(
        "hostflint", "cinder_step", "emberbrand", "dust_veil",
        "ember_draw", "brand_mark", "cinder_vow", "grave_nail", "ash_press"
    )

    /** Player MEDIUM ids. Wake Echo = one Medium for combined 5+4. */
    val PLAYER_MEDIUM: Set<String> = setOf(
        "tower_pike", "ruin_seal", "spark_tithe", "wake_echo"
    )

    /** Player NO STROKE (Brace pip on You). */
    val PLAYER_NO_STROKE: Set<String> = setOf(
        "iron_mantle", "vow_plate"
    )

    /** Enemy SMALL. */
    val ENEMY_SMALL: Set<String> = setOf("shiv", "nip", "hit")

    /** Enemy MEDIUM. */
    val ENEMY_MEDIUM: Set<String> = setOf("cleave", "seal_pulse", "coal_slam")

    /** Enemy NO STROKE (Brace pip on foe). */
    val ENEMY_NO_STROKE: Set<String> = setOf("hide", "rust_guard", "cinder_hide")

    /** Wake tier is exclusive — only Ashbrand Wake. */
    fun isWakeExclusiveId(id: String): Boolean = id == ID_ASHBRAND_WAKE

    fun roleForEnemy(kind: EnemyKind): FxRole = when (EnemyKitRole.fromKind(kind)) {
        EnemyKitRole.WEAK_GOBLIN -> FxRole.WEAK_GOBLIN
        EnemyKitRole.STURDY_ORC -> FxRole.STURDY_ORC
        EnemyKitRole.SEAL_WARDEN -> FxRole.SEAL_WARDEN
        EnemyKitRole.ASH_WARDEN -> FxRole.ASH_WARDEN
    }

    fun roleForEnemy(role: EnemyKitRole): FxRole = when (role) {
        EnemyKitRole.WEAK_GOBLIN -> FxRole.WEAK_GOBLIN
        EnemyKitRole.STURDY_ORC -> FxRole.STURDY_ORC
        EnemyKitRole.SEAL_WARDEN -> FxRole.SEAL_WARDEN
        EnemyKitRole.ASH_WARDEN -> FxRole.ASH_WARDEN
    }

    fun colorArgb(role: FxRole): Long = when (role) {
        FxRole.YOU -> COLOR_YOU
        FxRole.WEAK_GOBLIN -> COLOR_WEAK_GOBLIN
        FxRole.STURDY_ORC -> COLOR_STURDY_ORC
        FxRole.SEAL_WARDEN -> COLOR_SEAL_WARDEN
        FxRole.ASH_WARDEN -> COLOR_ASH_WARDEN
    }

    /**
     * Tier for a player card id. Unmapped attack-like defaults SMALL;
     * unmapped brace-only stay NO_STROKE only if in NO_STROKE set.
     * Never returns WAKE for a card id — Wake is [ID_ASHBRAND_WAKE] only.
     */
    fun tierForPlayer(cardId: String): FxTier = when {
        cardId == ID_ASHBRAND_WAKE -> FxTier.WAKE
        cardId in PLAYER_NO_STROKE -> FxTier.NO_STROKE
        cardId in PLAYER_MEDIUM -> FxTier.MEDIUM
        cardId in PLAYER_SMALL -> FxTier.SMALL
        else -> FxTier.SMALL // fail-safe default (Hub unlocks etc.)
    }

    fun tierForEnemy(skillId: String): FxTier = when {
        skillId in ENEMY_NO_STROKE -> FxTier.NO_STROKE
        skillId in ENEMY_MEDIUM -> FxTier.MEDIUM
        skillId in ENEMY_SMALL -> FxTier.SMALL
        else -> FxTier.SMALL
    }

    /** Scale a 1x FX budget by speedX (1 or 2). Mid-toggle affects NEXT beat when snapshotted. */
    fun fxHoldMs(baseMs: Long, speedX: Int): Long {
        val x = if (speedX >= 2) 2 else 1
        return (baseMs / x).coerceAtLeast(1L)
    }

    fun specForPlayer(cardId: String): FxBeatSpec =
        buildSpec(tierForPlayer(cardId), FxRole.YOU, player = true)

    fun specForEnemy(skillId: String, enemyKind: EnemyKind): FxBeatSpec =
        buildSpec(tierForEnemy(skillId), roleForEnemy(enemyKind), player = false)

    fun specForEnemy(skillId: String, role: EnemyKitRole): FxBeatSpec =
        buildSpec(tierForEnemy(skillId), roleForEnemy(role), player = false)

    fun specForWake(): FxBeatSpec =
        buildSpec(FxTier.WAKE, FxRole.YOU, player = true)

    /**
     * Resolve FX from an event hint. Returns null when event has no fxId (follow-up lines).
     * Never throws — on any unexpected id still returns a SMALL/You fail-safe when fxId set.
     */
    fun safeSpec(
        fxId: String?,
        fxPlayer: Boolean,
        enemyKind: EnemyKind?
    ): FxBeatSpec? {
        if (fxId.isNullOrBlank()) return null
        return try {
            when {
                fxId == ID_ASHBRAND_WAKE -> specForWake()
                fxPlayer -> specForPlayer(fxId)
                else -> specForEnemy(fxId, enemyKind ?: EnemyKind.GOBLIN)
            }
        } catch (_: Throwable) {
            // Fail-safe stub — UI must still show float/log
            buildSpec(FxTier.SMALL, if (fxPlayer) FxRole.YOU else FxRole.WEAK_GOBLIN, fxPlayer)
        }
    }

    private fun buildSpec(tier: FxTier, role: FxRole, player: Boolean): FxBeatSpec {
        val dir = if (player) FxStrokeDir.YOU_TO_FOE else FxStrokeDir.FOE_TO_YOU
        val color = colorArgb(role)
        val stroke: FxStrokeSpec? = when (tier) {
            FxTier.SMALL -> FxStrokeSpec(color, THICK_SMALL, dir)
            FxTier.MEDIUM -> FxStrokeSpec(color, THICK_MEDIUM, dir)
            FxTier.NO_STROKE -> null
            FxTier.WAKE -> null // Wake slash from WakeStageOverlay
        }
        val shake = when (tier) {
            FxTier.SMALL, FxTier.NO_STROKE -> FxShakeSpec(SHAKE_SMALL_AMP, 0)
            FxTier.MEDIUM -> FxShakeSpec(SHAKE_MEDIUM_AMP, 4)
            FxTier.WAKE -> FxShakeSpec(SHAKE_WAKE_AMP, 8)
        }
        val strokeMs = when (tier) {
            FxTier.SMALL -> STROKE_SMALL_MS
            FxTier.MEDIUM -> STROKE_MEDIUM_MS
            FxTier.NO_STROKE, FxTier.WAKE -> 0L
        }
        val shakeMs = when (tier) {
            FxTier.MEDIUM -> SHAKE_MEDIUM_MS
            FxTier.WAKE -> SHAKE_WAKE_MS
            else -> 0L
        }
        return FxBeatSpec(
            tier = tier,
            role = role,
            stroke = stroke,
            shake = shake,
            flashMs = FLASH_MS,
            strokeMs = strokeMs,
            floatMs = FLOAT_MS,
            shakeMs = shakeMs,
            useWakeSlash = tier == FxTier.WAKE
        )
    }

    /** True when Wake Echo maps to a single Medium (combined damage line). */
    fun wakeEchoIsSingleMedium(): Boolean =
        tierForPlayer("wake_echo") == FxTier.MEDIUM &&
            "wake_echo" !in PLAYER_SMALL &&
            !isWakeExclusiveId("wake_echo")

    /** No player card id may use Wake tier. */
    fun noPlayerCardUsesWakeTier(): Boolean =
        (PLAYER_SMALL + PLAYER_MEDIUM + PLAYER_NO_STROKE).none { isWakeExclusiveId(it) } &&
            CardIdsKnown.none { tierForPlayer(it) == FxTier.WAKE && it != ID_ASHBRAND_WAKE }

    /** Catalog ids we care about for exclusive Wake check. */
    private val CardIdsKnown: Set<String> =
        PLAYER_SMALL + PLAYER_MEDIUM + PLAYER_NO_STROKE +
            setOf("shadow_latch", "relic_shard")
}
