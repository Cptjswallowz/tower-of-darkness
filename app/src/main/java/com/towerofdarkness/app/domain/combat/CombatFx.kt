package com.towerofdarkness.app.domain.combat

/**
 * Shared combat FX kernel — v0.1.40-fxfix (no screen flash, shields back, target-only slash).
 * Aim from v0.1.38 + size/hold from v0.1.39; half-stage clip + tamed glow bloom.
 * Pure domain maps + duration + recipient-slash / Brace-pip / clip / glow helpers.
 * Compose renders flash / short recipient slash / brace pips / float / shake.
 * Never changes damage / Wake math / who gets FX. See docs/fxfix-v0140.md.
 *
 * Kernel order on skill resolve: flash → stroke|brace-pips → float → shake → log+hold.
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
 * Where a damage slash paints (recipient bust only — not attacker→target bar).
 * Player dmg → FOE; enemy dmg → YOU.
 */
enum class FxRecipient {
    YOU,
    FOE
}

/**
 * Presentation stroke spec (ARGB + relative thickness + recipient bust).
 * [thickness] 0 = no stroke; Small ~1; Medium ~2. Wake uses WakeStageOverlay instead.
 */
data class FxStrokeSpec(
    val colorArgb: Long,
    val thickness: Float,
    val dir: FxStrokeDir,
    val recipient: FxRecipient
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
    val useWakeSlash: Boolean = false,
    /** Recipient bust for damage slash (Wake also aims here — foe). */
    val recipient: FxRecipient = FxRecipient.FOE
)

/**
 * One-shot play packet for UI (v0.1.38) — beat + Brace shield pips + Soften pulse.
 * Built from [CombatFx.playSpec]; never affects damage math.
 */
data class FxPlaySpec(
    val beat: FxBeatSpec,
    /** Tiny shield pips to float around owner; 0 = none. Cap 5. */
    val bracePipCount: Int = 0,
    /** Owner bust for Brace pips (You Brace → YOU; foe Hide → FOE). */
    val braceOwner: FxRecipient? = null,
    /** Soften apply: pulse red Soften pip on foe; no plate / no extra slash. */
    val softenPipPulse: Boolean = false
)

/**
 * Normalized short-cut geometry on one bust (fractions of stage width/height).
 * Unit-testable; Compose mirrors these fracs. Must NOT span both busts.
 */
data class SlashCutGeom(
    val recipient: FxRecipient,
    val centerXFrac: Float,
    val centerYFrac: Float,
    /** Half horizontal extent — short cut (~bust diameter), not cross-stage. */
    val halfExtentXFrac: Float,
    val halfExtentYFrac: Float
) {
    val startXFrac: Float get() = centerXFrac - halfExtentXFrac
    val endXFrac: Float get() = centerXFrac + halfExtentXFrac
    val startYFrac: Float get() = centerYFrac - halfExtentYFrac
    val endYFrac: Float get() = centerYFrac + halfExtentYFrac

    /** True if this cut would look like a bust-to-bust bar (banned). */
    fun spansBothBusts(): Boolean =
        startXFrac <= CombatFx.YOU_BUST_X + 0.08f &&
            endXFrac >= CombatFx.FOE_BUST_X - 0.08f
}

object CombatFx {
    const val TAG = "v0.1.40-fxfix"

    /** Special id for Ashbrand FULL Wake (not a card id). */
    const val ID_ASHBRAND_WAKE = "ashbrand_wake"

    // --- Bust centers (stage width fracs) — You left, foe right ---
    const val YOU_BUST_X = 0.18f
    const val FOE_BUST_X = 0.82f
    const val BUST_Y = 0.42f
    /** Half-extent of short slash (~bust diameter); never reaches opposite bust. */
    const val SLASH_HALF_X_SMALL = 0.055f
    const val SLASH_HALF_X_MEDIUM = 0.07f
    const val SLASH_HALF_Y_FACTOR = 0.65f

    // --- Colors (ARGB) — role stroke lock ---
    const val COLOR_YOU = 0xFFC9A227L          // gold ember
    const val COLOR_WEAK_GOBLIN = 0xFF6B7A3AL  // dirty green
    const val COLOR_STURDY_ORC = 0xFFA05030L   // rust
    const val COLOR_SEAL_WARDEN = 0xFFB87333L  // copper
    const val COLOR_ASH_WARDEN = 0xFFC45A2DL   // coal orange
    const val COLOR_SOFTEN_PIP = 0xFFE24A3BL  // red Soften pulse

    // --- 1x duration budgets (presentation only) ---
    const val FLASH_MS = 120L
    const val STROKE_SMALL_MS = 400L
    const val STROKE_MEDIUM_MS = 480L
    const val FLOAT_MS = 700L
    const val SHAKE_MEDIUM_MS = 240L
    const val SHAKE_WAKE_MS = 400L
    const val BRACE_PIP_MS = 700L
    /** Fade only in the last [BRACE_PIP_FADE_MS] of the hold (full opacity before). */
    const val BRACE_PIP_FADE_MS = 200L
    const val SOFTEN_PULSE_MS = 420L

    const val THICK_SMALL = 2.1f
    const val THICK_MEDIUM = 4.2f
    const val SHAKE_SMALL_AMP = 0f
    const val SHAKE_MEDIUM_AMP = 4f
    const val SHAKE_WAKE_AMP = 12f
    const val BRACE_PIP_CAP = 5
    /** Pip radius as fraction of min(stage w,h); was 0.018, now 1.5×. */
    const val BRACE_PIP_RADIUS_FRAC = 0.027f
    /** Prior pip radius (v0.1.38) — tests assert 1.5×. */
    const val BRACE_PIP_RADIUS_FRAC_PRIOR = 0.018f
    /** Prior stroke thicknesses (v0.1.38) — tests assert 1.4×. */
    const val THICK_SMALL_PRIOR = 1.5f
    const val THICK_MEDIUM_PRIOR = 3.0f
    /** v0.1.39 readability multiplier on core stroke thickness. */
    const val THICKNESS_READ_MULT = 1.4f
    /** v0.1.38 glow multiplier applied to *prior* thickness (absolute glow lock). */
    const val STROKE_GLOW_PRIOR_MULT = 7f
    /** Core stroke width = thickness × this (heavier cut keeps full 1.4×). */
    const val STROKE_CORE_MULT = 3.2f

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
        // v0.1.41: reuse existing colors — no new FxRole / no retune (FX frozen 0.1.40)
        EnemyKitRole.CAVE_TROLL -> FxRole.STURDY_ORC
        EnemyKitRole.GATE_WARDEN -> FxRole.SEAL_WARDEN
    }

    fun roleForEnemy(role: EnemyKitRole): FxRole = when (role) {
        EnemyKitRole.WEAK_GOBLIN -> FxRole.WEAK_GOBLIN
        EnemyKitRole.STURDY_ORC -> FxRole.STURDY_ORC
        EnemyKitRole.SEAL_WARDEN -> FxRole.SEAL_WARDEN
        EnemyKitRole.ASH_WARDEN -> FxRole.ASH_WARDEN
        EnemyKitRole.CAVE_TROLL -> FxRole.STURDY_ORC
        EnemyKitRole.GATE_WARDEN -> FxRole.SEAL_WARDEN
    }

    fun colorArgb(role: FxRole): Long = when (role) {
        FxRole.YOU -> COLOR_YOU
        FxRole.WEAK_GOBLIN -> COLOR_WEAK_GOBLIN
        FxRole.STURDY_ORC -> COLOR_STURDY_ORC
        FxRole.SEAL_WARDEN -> COLOR_SEAL_WARDEN
        FxRole.ASH_WARDEN -> COLOR_ASH_WARDEN
    }

    /** Damage slash recipient: player skills hit foe; enemy skills hit You. */
    fun recipientFor(dir: FxStrokeDir): FxRecipient = when (dir) {
        FxStrokeDir.YOU_TO_FOE -> FxRecipient.FOE
        FxStrokeDir.FOE_TO_YOU -> FxRecipient.YOU
    }

    fun recipientForPlayer(player: Boolean): FxRecipient =
        if (player) FxRecipient.FOE else FxRecipient.YOU

    /** Brace shield-pip owner: You Brace → YOU; foe Hide/Guard → FOE. */
    fun braceOwnerForPlayer(player: Boolean): FxRecipient =
        if (player) FxRecipient.YOU else FxRecipient.FOE

    /**
     * Half-stage X clip for recipient/owner FX so glow cannot wash both busts.
     * YOU → left half `0f..0.5f`; FOE → right half `0.5f..1f`. Ranges are disjoint
     * except the shared mid edge at 0.5.
     */
    fun recipientClipXFrac(recipient: FxRecipient): ClosedFloatingPointRange<Float> = when (recipient) {
        FxRecipient.YOU -> 0f..0.5f
        FxRecipient.FOE -> 0.5f..1f
    }

    /**
     * Absolute glow stroke width ≈ v0.1.38 (priorThickness × [STROKE_GLOW_PRIOR_MULT]),
     * not unbounded with 1.4× core thickness. Prefer `(thickness / 1.4f) * 7f`.
     * Core still uses full thickness × [STROKE_CORE_MULT] (heavier cut, no plate wash).
     */
    fun strokeGlowWidth(thickness: Float): Float =
        (thickness / THICKNESS_READ_MULT) * STROKE_GLOW_PRIOR_MULT

    /** Core stroke width for Canvas (full 1.4× readability thickness). */
    fun strokeCoreWidth(thickness: Float): Float = thickness * STROKE_CORE_MULT

    /**
     * Drawn Brace shield pip count = Brace gained, capped at [BRACE_PIP_CAP].
     * Gain 0 → 0; gain 1 → 1; …; gain 6+ → 5.
     */
    fun bracePipCount(gained: Int): Int =
        gained.coerceAtLeast(0).coerceAtMost(BRACE_PIP_CAP)

    /**
     * Brace pip alpha over hold progress 0→1.
     * Full opacity until fade window starts at (BRACE_PIP_MS - BRACE_PIP_FADE_MS) / BRACE_PIP_MS,
     * then linear fade to floor ~0.15.
     */
    fun bracePipAlpha(progress: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        val fadeStart = (BRACE_PIP_MS - BRACE_PIP_FADE_MS).toFloat() / BRACE_PIP_MS.toFloat()
        return if (p < fadeStart) {
            0.95f
        } else {
            val t = ((p - fadeStart) / (1f - fadeStart)).coerceIn(0f, 1f)
            (0.95f * (1f - t)).coerceAtLeast(0.15f)
        }
    }

    /**
     * Short diagonal cut on [recipient] bust only.
     * Guaranteed not to span You↔foe (see [SlashCutGeom.spansBothBusts]).
     */
    fun slashCutGeom(recipient: FxRecipient, tier: FxTier = FxTier.SMALL): SlashCutGeom {
        val cx = when (recipient) {
            FxRecipient.YOU -> YOU_BUST_X
            FxRecipient.FOE -> FOE_BUST_X
        }
        val halfX = when (tier) {
            FxTier.MEDIUM, FxTier.WAKE -> SLASH_HALF_X_MEDIUM
            else -> SLASH_HALF_X_SMALL
        }
        return SlashCutGeom(
            recipient = recipient,
            centerXFrac = cx,
            centerYFrac = BUST_Y,
            halfExtentXFrac = halfX,
            halfExtentYFrac = halfX * SLASH_HALF_Y_FACTOR
        )
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

    /**
     * Build UI play packet from a primary FX event + optional Brace/Soften presentation fields.
     * Follow-up-only Brace/Soften (null fxId): pass [braceGained] / [softenApplied] with a synthetic
     * NO_STROKE-ish beat via [playBraceOrSoftenOnly].
     */
    fun playSpec(
        beat: FxBeatSpec,
        braceGained: Int = 0,
        softenApplied: Int = 0,
        fxPlayer: Boolean = true
    ): FxPlaySpec {
        val pips = bracePipCount(braceGained)
        return FxPlaySpec(
            beat = beat,
            bracePipCount = pips,
            braceOwner = if (pips > 0) braceOwnerForPlayer(fxPlayer) else null,
            softenPipPulse = softenApplied > 0
        )
    }

    /**
     * Follow-up Brace / Soften lines have null fxId — still need pips / red Soften pulse, no slash.
     */
    fun playBraceOrSoftenOnly(
        braceGained: Int = 0,
        softenApplied: Int = 0,
        fxPlayer: Boolean = true
    ): FxPlaySpec? {
        val pips = bracePipCount(braceGained)
        if (pips <= 0 && softenApplied <= 0) return null
        val stub = buildSpec(FxTier.NO_STROKE, if (fxPlayer) FxRole.YOU else FxRole.WEAK_GOBLIN, fxPlayer)
        return FxPlaySpec(
            beat = stub,
            bracePipCount = pips,
            braceOwner = if (pips > 0) braceOwnerForPlayer(fxPlayer) else null,
            softenPipPulse = softenApplied > 0
        )
    }

    private fun buildSpec(tier: FxTier, role: FxRole, player: Boolean): FxBeatSpec {
        val dir = if (player) FxStrokeDir.YOU_TO_FOE else FxStrokeDir.FOE_TO_YOU
        val recipient = recipientFor(dir)
        val color = colorArgb(role)
        val stroke: FxStrokeSpec? = when (tier) {
            FxTier.SMALL -> FxStrokeSpec(color, THICK_SMALL, dir, recipient)
            FxTier.MEDIUM -> FxStrokeSpec(color, THICK_MEDIUM, dir, recipient)
            FxTier.NO_STROKE -> null
            FxTier.WAKE -> null // Wake slash from WakeStageOverlay (foe only)
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
            useWakeSlash = tier == FxTier.WAKE,
            // Wake aims at foe; damage slash recipient as above; NO_STROKE unused for slash
            recipient = if (tier == FxTier.WAKE) FxRecipient.FOE else recipient
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
