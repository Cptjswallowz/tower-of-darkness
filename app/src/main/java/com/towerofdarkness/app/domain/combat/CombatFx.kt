package com.towerofdarkness.app.domain.combat

/**
 * Shared combat FX kernel — v0.1.47-strokethick (fat Wake-family stroke PRIMARY).
 * Aim / clip / Brace pips stay v0.1.40-fxfix (target bust only / half-stage / no screen flash).
 * Readable cut = Canvas path (glow + thick core crescent) on recipient bust — NOT CLEAVE flipbook.
 * v0.1.47: canvas stroke WIDTH fattened (bust-frac core/glow); bright steel/ember-white;
 * enemy hits MUST stroke on You (`FX stroke on You`). Peak holds raised.
 * Optional CLEAVE tip garnish at stroke tip only; never another bust-coverage rescale of the sheet.
 * Pure domain maps + duration + recipient-stroke / Brace-pip / clip / glow / spark helpers.
 * Compose: [CombatStrokeOverlay] draws path primary; Wake gold arc stays [WakeStageOverlay].
 * Never changes damage / Wake math / who gets FX. See docs/strokethick-v0147.md.
 *
 * Kernel order on skill resolve: flash → stroke|brace-pips|hit-flash → float → shake → log+hold.
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
    val recipient: FxRecipient = FxRecipient.FOE,
    /**
     * Ashbrand SPARK only — CLEAVE hit-flash on [recipient] bust (additive, not full-screen).
     * Does not replace Wake crescent or ashbrand_spark icon ember.
     */
    val useCleaveHitFlash: Boolean = false,
    /** 1x hit-flash hold ms; scaled via [CombatFx.fxHoldMs]. */
    val hitFlashMs: Long = 0L
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
    const val TAG = "v0.1.47-strokethick"

    /** logcat tag for stroke / slash-light debug. */
    const val LOG_TAG_TOD_FX = "TodFx"

    /** Exact combat-log / logcat line — drawn path stroke (primary readable cut). */
    const val STROKE_DEBUG_FMT = "FX stroke on "

    /** Optional tip-garnish only — existing sheet debug; not the primary cut. */
    const val SLASH_LIGHT_DEBUG_FMT = "FX slash-light on "

    /** Special id for Ashbrand FULL Wake (not a card id). */
    const val ID_ASHBRAND_WAKE = "ashbrand_wake"

    /** Special id for Ashbrand SPARK half — short drawn stroke + tip spark (not a card id). */
    const val ID_ASHBRAND_SPARK = "ashbrand_spark"

    // --- Bust centers (stage width fracs) — You left, foe right ---
    const val YOU_BUST_X = 0.18f
    const val FOE_BUST_X = 0.82f
    const val BUST_Y = 0.42f
    /**
     * Half-stage → bust WIDTH factor (matches CleaveKit.BUST_WIDTH_FRAC lock; frozen).
     * Used only to size the *drawn path* stroke relative to bust — not a CLEAVE rescale.
     */
    const val STROKE_BUST_WIDTH_FRAC = 0.78f
    /**
     * Drawn-stroke full width as fraction of bust WIDTH.
     * Player 70–90% (WO lock); enemy ~60%; never spans both busts.
     */
    const val STROKE_WIDTH_FRAC_PLAYER_SMALL = 0.78f
    const val STROKE_WIDTH_FRAC_PLAYER_MEDIUM = 0.88f
    const val STROKE_WIDTH_FRAC_ENEMY = 0.60f
    /** Legacy half-extent names — derived from width fracs for call-site / test compat. */
    val SLASH_HALF_X_SMALL: Float
        get() = strokeHalfXFrac(FxRecipient.FOE, FxTier.SMALL)
    val SLASH_HALF_X_MEDIUM: Float
        get() = strokeHalfXFrac(FxRecipient.FOE, FxTier.MEDIUM)
    const val SLASH_HALF_Y_FACTOR = 0.65f

    // --- Colors (ARGB) — tile flash / role tint (unchanged legacy) ---
    const val COLOR_YOU = 0xFFC9A227L          // gold ember (tile / Wake language)
    const val COLOR_WEAK_GOBLIN = 0xFF6B7A3AL  // dirty green
    const val COLOR_STURDY_ORC = 0xFFA05030L   // rust
    const val COLOR_SEAL_WARDEN = 0xFFB87333L  // copper
    const val COLOR_ASH_WARDEN = 0xFFC45A2DL   // coal orange
    const val COLOR_SOFTEN_PIP = 0xFFE24A3BL  // red Soften pulse

    // --- Drawn stroke colors — bright steel / ember-white (pop on dark armor) ---
    /** Player cut — bright steel / ember-white (was muddy 0xFFC9B8A0). */
    const val COLOR_STROKE_YOU = 0xFFF2E6D0L
    /** Enemy cut on You — duller steel/ash but still visible (was 0xFF8A8680). */
    const val COLOR_STROKE_ENEMY = 0xFFC4B8A8L
    /** Warden enemy cut — dull ash-ember, still readable (was 0xFF9A8070). */
    const val COLOR_STROKE_ENEMY_EMBER = 0xFFD0B49AL
    /** Hot core highlight along the cut (overlay tip stripe). */
    const val COLOR_STROKE_CORE_HIGHLIGHT = 0xFFFFF8ECL

    // --- 1x duration budgets (presentation only) ---
    const val FLASH_MS = 120L
    /** Player Small/Medium total hold @1x (peak + short fade); WO peak 450–550 then fade. */
    const val STROKE_SMALL_MS = 500L
    const val STROKE_MEDIUM_MS = 500L
    /** Peak linger @1x for player drawn stroke (pass/fail lock 450–550). */
    const val STROKE_PLAYER_PEAK_MS = 470L
    /** Enemy hit stroke shorter — total / peak @1x (WO 350–450 peak). */
    const val STROKE_ENEMY_SMALL_MS = 480L
    const val STROKE_ENEMY_MEDIUM_MS = 500L
    const val STROKE_ENEMY_PEAK_MS = 400L
    /** Ashbrand SPARK non-Wake — short drawn stroke (+ optional tip spark). */
    const val STROKE_SPARK_MS = 280L
    const val STROKE_SPARK_PEAK_MS = 200L
    const val FLOAT_MS = 700L
    const val SHAKE_MEDIUM_MS = 240L
    const val SHAKE_WAKE_MS = 400L
    const val BRACE_PIP_MS = 700L
    /** Fade only in the last [BRACE_PIP_FADE_MS] of the hold (full opacity before). */
    const val BRACE_PIP_FADE_MS = 200L
    const val SOFTEN_PULSE_MS = 420L

    /**
     * Relative thickness weights in [FxStrokeSpec] (unchanged vs 0.1.46).
     * Canvas px width is NOT thickness×[STROKE_CORE_MULT] anymore — that was the hairline bug
     * (~6.7px on a ~360 stage). See [STROKE_CORE_BUST_FRAC_*] / [strokeCoreWidthPx].
     */
    const val THICK_SMALL = 2.1f
    const val THICK_MEDIUM = 4.2f
    /** Enemy stroke relative factor vs player same-tier (slightly thinner). */
    const val THICK_ENEMY_FACTOR = 0.78f
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
    /** v0.1.38 glow multiplier applied to *prior* thickness (hairline absolute glow lock). */
    const val STROKE_GLOW_PRIOR_MULT = 7f
    /**
     * Hairline core multiplier (v0.1.46 FAIL): thickness × 3.2 ≈ 6.7px @ THICK_SMALL.
     * Kept for docs / fat-vs-hairline tests; draw path uses bust-frac instead.
     */
    const val STROKE_CORE_MULT = 3.2f
    /** Reference stage width for absolute px locks in unit tests / docs. */
    const val REF_STAGE_WIDTH_PX = 360f
    /** bustWidthPx @ ref = stage × 0.5 × [STROKE_BUST_WIDTH_FRAC] ≈ 140.4. */
    val REF_BUST_WIDTH_PX: Float get() = REF_STAGE_WIDTH_PX * 0.5f * STROKE_BUST_WIDTH_FRAC
    /**
     * v0.1.47 fat lock — core stroke width as fraction of bust WIDTH.
     * Player Small ~0.14 → ~20px @360; Medium ~0.18 → ~25px; enemy ~0.10 → ~14px.
     * Target: player readable ~1/3–1/2 Wake fatness; core ~18–28px at stage scale.
     */
    const val STROKE_CORE_BUST_FRAC_PLAYER_SMALL = 0.14f
    const val STROKE_CORE_BUST_FRAC_PLAYER_MEDIUM = 0.18f
    const val STROKE_CORE_BUST_FRAC_ENEMY = 0.10f
    /** Glow halo as fraction of bust WIDTH (soft outer ring; still < Wake). */
    const val STROKE_GLOW_BUST_FRAC_PLAYER_SMALL = 0.30f
    const val STROKE_GLOW_BUST_FRAC_PLAYER_MEDIUM = 0.38f
    const val STROKE_GLOW_BUST_FRAC_ENEMY = 0.22f
    /** Hairline core px @ ref (THICK_SMALL × STROKE_CORE_MULT) — FAIL baseline. */
    val HAIRLINE_CORE_PX_SMALL: Float get() = THICK_SMALL * STROKE_CORE_MULT

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
    /** Enemy SMALL — Club / Gate Pulse land as Small slash-light (v0.1.42; were fail-soft Small in 0.1.41). */
    val ENEMY_SMALL: Set<String> = setOf("shiv", "nip", "hit", "club", "gate_pulse")

    /** Enemy MEDIUM. */
    val ENEMY_MEDIUM: Set<String> = setOf("cleave", "seal_pulse", "coal_slam")

    /** Enemy NO STROKE (Brace pip on foe). */
    val ENEMY_NO_STROKE: Set<String> = setOf("hide", "rust_guard", "cinder_hide")

    /** Wake tier is exclusive — only Ashbrand Wake. */
    fun isWakeExclusiveId(id: String): Boolean = id == ID_ASHBRAND_WAKE

    /** SPARK half — short drawn stroke (+ optional tip); never Wake tier / never FULL Wake. */
    fun isSparkId(id: String): Boolean = id == ID_ASHBRAND_SPARK

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

    /**
     * Drawn-stroke paint — steel/ash/ember family (no forced green/gold role SrcIn).
     * Player → steel ash ember; enemy → dull steel ash (wardens slightly ember).
     */
    fun strokeColorArgb(role: FxRole): Long = when (role) {
        FxRole.YOU -> COLOR_STROKE_YOU
        FxRole.WEAK_GOBLIN, FxRole.STURDY_ORC -> COLOR_STROKE_ENEMY
        FxRole.SEAL_WARDEN, FxRole.ASH_WARDEN -> COLOR_STROKE_ENEMY_EMBER
    }

    /**
     * Half horizontal extent (stage-width frac) for drawn stroke on [recipient].
     * bustWidthFracOfStage = 0.5 × [STROKE_BUST_WIDTH_FRAC];
     * fullStrokeFrac = bustWidthFracOfStage × widthFrac; halfX = full / 2.
     */
    fun strokeHalfXFrac(recipient: FxRecipient, tier: FxTier = FxTier.SMALL): Float {
        val widthFrac = when {
            recipient == FxRecipient.YOU -> STROKE_WIDTH_FRAC_ENEMY
            tier == FxTier.MEDIUM || tier == FxTier.WAKE -> STROKE_WIDTH_FRAC_PLAYER_MEDIUM
            else -> STROKE_WIDTH_FRAC_PLAYER_SMALL
        }
        val bustFracOfStage = 0.5f * STROKE_BUST_WIDTH_FRAC
        return (bustFracOfStage * widthFrac) / 2f
    }

    /** Peak linger ms within already-scaled [holdMs] (2x shortens via caller). */
    fun strokePeakMs(holdMs: Long, recipient: FxRecipient, spark: Boolean = false): Long {
        val basePeak = when {
            spark -> STROKE_SPARK_PEAK_MS
            recipient == FxRecipient.YOU -> STROKE_ENEMY_PEAK_MS
            else -> STROKE_PLAYER_PEAK_MS
        }
        val baseTotal = when {
            spark -> STROKE_SPARK_MS
            recipient == FxRecipient.YOU -> STROKE_ENEMY_SMALL_MS
            else -> STROKE_SMALL_MS
        }
        // Proportional when speedX halves holdMs; peak is majority of hold then fade.
        val scaled = (basePeak * holdMs / baseTotal.coerceAtLeast(1L)).coerceAtLeast(1L)
        val lo = (holdMs * 55L / 100L).coerceAtLeast(1L)
        val hi = (holdMs * 90L / 100L).coerceAtLeast(lo)
        return scaled.coerceIn(lo, hi)
    }

    /** Stroke full width as fraction of bust width (test lock). */
    fun strokeWidthFracOfBust(recipient: FxRecipient, tier: FxTier = FxTier.SMALL): Float =
        when {
            recipient == FxRecipient.YOU -> STROKE_WIDTH_FRAC_ENEMY
            tier == FxTier.MEDIUM || tier == FxTier.WAKE -> STROKE_WIDTH_FRAC_PLAYER_MEDIUM
            else -> STROKE_WIDTH_FRAC_PLAYER_SMALL
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
     * Canvas glow width in px for [recipient]/[tier] given measured [bustWidthPx].
     * Fat lock (v0.1.47) — fraction of bust, not hairline thickness×mult.
     */
    fun strokeGlowWidthPx(recipient: FxRecipient, tier: FxTier, bustWidthPx: Float): Float {
        val frac = when {
            recipient == FxRecipient.YOU -> STROKE_GLOW_BUST_FRAC_ENEMY
            tier == FxTier.MEDIUM || tier == FxTier.WAKE -> STROKE_GLOW_BUST_FRAC_PLAYER_MEDIUM
            else -> STROKE_GLOW_BUST_FRAC_PLAYER_SMALL
        }
        return (bustWidthPx * frac).coerceAtLeast(1f)
    }

    /**
     * Canvas core stroke width in px for [recipient]/[tier] given measured [bustWidthPx].
     * Player Small/Medium in ~18–28px @360-stage; enemy slightly thinner.
     */
    fun strokeCoreWidthPx(recipient: FxRecipient, tier: FxTier, bustWidthPx: Float): Float {
        val frac = when {
            recipient == FxRecipient.YOU -> STROKE_CORE_BUST_FRAC_ENEMY
            tier == FxTier.MEDIUM || tier == FxTier.WAKE -> STROKE_CORE_BUST_FRAC_PLAYER_MEDIUM
            else -> STROKE_CORE_BUST_FRAC_PLAYER_SMALL
        }
        return (bustWidthPx * frac).coerceAtLeast(1f)
    }

    /**
     * Absolute glow @ reference stage (compat). Fat lock — much wider than hairline
     * `(thickness / 1.4) * 7`. Prefer [strokeGlowWidthPx] when stage size is known.
     */
    fun strokeGlowWidth(thickness: Float): Float {
        val tier = if (thickness >= THICK_MEDIUM * 0.95f) FxTier.MEDIUM else FxTier.SMALL
        return strokeGlowWidthPx(FxRecipient.FOE, tier, REF_BUST_WIDTH_PX)
    }

    /**
     * Absolute core @ reference stage (compat). Fat lock — replaces hairline
     * `thickness × [STROKE_CORE_MULT]`. Prefer [strokeCoreWidthPx] when stage size known.
     */
    fun strokeCoreWidth(thickness: Float): Float {
        val tier = if (thickness >= THICK_MEDIUM * 0.95f) FxTier.MEDIUM else FxTier.SMALL
        return strokeCoreWidthPx(FxRecipient.FOE, tier, REF_BUST_WIDTH_PX)
    }

    /** Enemy core @ reference stage (slightly thinner than player Small). */
    fun strokeCoreWidthEnemyRef(): Float =
        strokeCoreWidthPx(FxRecipient.YOU, FxTier.SMALL, REF_BUST_WIDTH_PX)

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
        val halfX = strokeHalfXFrac(recipient, tier)
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
        cardId == ID_ASHBRAND_SPARK -> FxTier.SMALL // short drawn stroke via specForSpark
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
     * Ashbrand SPARK — short drawn stroke on foe (+ optional tip spark / hit-flash).
     * FULL Wake stays [WakeStageOverlay] / [specForWake] only. Icon ember separate.
     */
    fun specForSpark(): FxBeatSpec {
        val base = buildSpec(FxTier.SMALL, FxRole.YOU, player = true)
        val thin = base.stroke?.copy(thickness = THICK_SMALL * 0.75f)
        return base.copy(
            stroke = thin,
            strokeMs = STROKE_SPARK_MS,
            useCleaveHitFlash = true,
            hitFlashMs = CleaveKit.HIT_FLASH_MS,
            recipient = FxRecipient.FOE,
            flashMs = 0L
        )
    }

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
                fxId == ID_ASHBRAND_SPARK -> specForSpark()
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
        val color = strokeColorArgb(role)
        val thickSmall = if (player) THICK_SMALL else THICK_SMALL * THICK_ENEMY_FACTOR
        val thickMedium = if (player) THICK_MEDIUM else THICK_MEDIUM * THICK_ENEMY_FACTOR
        val stroke: FxStrokeSpec? = when (tier) {
            FxTier.SMALL -> FxStrokeSpec(color, thickSmall, dir, recipient)
            FxTier.MEDIUM -> FxStrokeSpec(color, thickMedium, dir, recipient)
            FxTier.NO_STROKE -> null
            FxTier.WAKE -> null // Wake slash from WakeStageOverlay (foe only)
        }
        val shake = when (tier) {
            FxTier.SMALL, FxTier.NO_STROKE -> FxShakeSpec(SHAKE_SMALL_AMP, 0)
            FxTier.MEDIUM -> FxShakeSpec(SHAKE_MEDIUM_AMP, 4)
            FxTier.WAKE -> FxShakeSpec(SHAKE_WAKE_AMP, 8)
        }
        val strokeMs = when (tier) {
            FxTier.SMALL -> if (player) STROKE_SMALL_MS else STROKE_ENEMY_SMALL_MS
            FxTier.MEDIUM -> if (player) STROKE_MEDIUM_MS else STROKE_ENEMY_MEDIUM_MS
            FxTier.NO_STROKE, FxTier.WAKE -> 0L
        }
        val shakeMs = when (tier) {
            FxTier.MEDIUM -> SHAKE_MEDIUM_MS
            FxTier.WAKE -> SHAKE_WAKE_MS
            else -> 0L
        }
        // Damage strokes (Small/Medium): contact hit-flash on recipient at cut (not Wake/Brace)
        val contactFlash = tier == FxTier.SMALL || tier == FxTier.MEDIUM
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
            recipient = if (tier == FxTier.WAKE) FxRecipient.FOE else recipient,
            useCleaveHitFlash = contactFlash,
            hitFlashMs = if (contactFlash) CleaveKit.CONTACT_HIT_FLASH_MS else 0L
        )
    }


    /** True when [id] is an enemy kit skill (Small/Medium/NoStroke sets). */
    fun isEnemyKitFxId(id: String): Boolean =
        id in ENEMY_SMALL || id in ENEMY_MEDIUM || id in ENEMY_NO_STROKE

    /** True when [id] is a known player card / Wake / Spark id. */
    fun isPlayerFxId(id: String): Boolean =
        id == ID_ASHBRAND_WAKE || id == ID_ASHBRAND_SPARK ||
            id in PLAYER_SMALL || id in PLAYER_MEDIUM || id in PLAYER_NO_STROKE

    /**
     * Harden fxPlayer for UI: enemy kit ids always false; player/Wake/Spark always true.
     * Fixes CombatScreen `fxEvent?.fxPlayer != false` defaulting enemy→player when flag wrong.
     */
    fun resolveFxPlayer(fxId: String?, eventFxPlayer: Boolean): Boolean {
        if (fxId.isNullOrBlank()) return eventFxPlayer
        if (isEnemyKitFxId(fxId)) return false
        if (isPlayerFxId(fxId)) return true
        return eventFxPlayer
    }

    /**
     * Proof helper — enemy damage skill produces drawn stroke on You + debug `FX stroke on You`.
     * Used by StrokeThickV0147Test / CombatScreen wiring audit.
     */
    fun enemyHitProducesYouStroke(skillId: String, kind: EnemyKind = EnemyKind.GOBLIN): Boolean {
        val beat = safeSpec(skillId, resolveFxPlayer(skillId, eventFxPlayer = true), kind)
            ?: return false
        val stroke = beat.stroke ?: return false
        if (beat.recipient != FxRecipient.YOU) return false
        if (stroke.recipient != FxRecipient.YOU) return false
        if (stroke.dir != FxStrokeDir.FOE_TO_YOU) return false
        val label = strokeTargetLabel(FxRecipient.YOU, foeName = null)
        return strokeDebugLine(label) == "FX stroke on You"
    }

    /**
     * Debug line for drawn path stroke: `FX stroke on <target>`.
     * Target is `You` or foe display name / `foe`.
     */
    fun strokeDebugLine(targetLabel: String): String =
        STROKE_DEBUG_FMT + targetLabel

    /**
     * Optional tip-garnish debug: `FX slash-light on <target>`.
     * Only when CLEAVE tip cell actually draws — not the primary cut.
     */
    fun slashLightDebugLine(targetLabel: String): String =
        SLASH_LIGHT_DEBUG_FMT + targetLabel

    /** Label for stroke / slash-light debug: YOU → `You`; FOE → foe name or `foe`. */
    fun slashLightTargetLabel(recipient: FxRecipient, foeName: String?): String =
        when (recipient) {
            FxRecipient.YOU -> "You"
            FxRecipient.FOE -> foeName?.takeIf { it.isNotBlank() } ?: "foe"
        }

    /** Alias — same labels for stroke debug. */
    fun strokeTargetLabel(recipient: FxRecipient, foeName: String?): String =
        slashLightTargetLabel(recipient, foeName)

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
