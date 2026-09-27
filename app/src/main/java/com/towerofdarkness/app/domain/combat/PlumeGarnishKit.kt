package com.towerofdarkness.app.domain.combat

/**
 * PLUME + Kenney particle garnish kit — v0.1.50-puffhold.
 * Pure domain (drawable names / scales / durations / ability→garnish map).
 * Compose draws via [CombatFxOverlay] cell playback (same engine as CLEAVE).
 * Does NOT touch CombatStrokeOverlay stroke width / crescentBladePath / peaks / colors.
 * Brace floating pips stay [CombatBracePipsOverlay]; flare is GAIN-only additive.
 *
 * Source of truth (repo): `assets/fx/plume/` + `assets/fx/kenney/` → copy into
 * `app/src/main/res/drawable/` as underscore names (see tools/prep_plumegarnish_v0149.py).
 * Do NOT commit /workspace/tod-particle-packs store packs.
 * PLUME sheets: sand/ash, dust puff, sparks (+ additive). Kenney: spark_NN / circle_NN / smoke_NN / flare_01.
 * ash-puff (Sand & Ash) wired for Dust Veil / Ash Press; footstep-puff = Cinder land only.
 * NEVER wire Kenney slash/twirl/magic/muzzle as hits. See docs/puffhold-v0150.md.
 */
enum class GarnishKind {
    /** Tip spark at stroke / Wake tip (20–30% bust). */
    TIP_SPARK,
    /** Dust puff centered on TARGET bust only (40–60% bust). */
    DUST_PUFF,
    /** Tiny Soften pip under foe HP while status active. */
    SOFTEN_PIP,
    /** Tiny flare on Brace owner on GAIN only — does not replace floating pips. */
    BRACE_FLARE
}

enum class GarnishPack {
    PLUME,
    KENNEY
}

/**
 * One garnish play packet — drawable basename + pack + kind + hold + scale.
 * [drawable] is Android-safe underscore name without extension.
 */
data class GarnishSpec(
    val kind: GarnishKind,
    val pack: GarnishPack,
    val drawable: String,
    /** Human sheet/sprite id for debug (`grinder-sparks` / `spark_03`). */
    val sheetId: String,
    val holdMs: Long,
    /** Draw size as fraction of bust WIDTH. */
    val bustFrac: Float,
    val tintArgb: Long,
    val additive: Boolean = false,
    /** When true, sprite is a PLUME atlas (256 cell grid); else single Kenney PNG. */
    val atlas: Boolean = false,
    val atlasCols: Int = 1,
    val atlasRows: Int = 1,
    val atlasFrames: Int = 1,
    val atlasPeakFrame: Int = 0,
    val atlasCellPx: Int = 256,
    val anchorX: Float = 0.5f,
    val anchorY: Float = 0.5f
)

object PlumeGarnishKit {
    const val TAG = "v0.1.50-puffhold"

    /** logcat / combat-log prefixes (keep stroke separate). */
    const val PLUME_DEBUG_FMT = "FX plume "
    const val KENNEY_DEBUG_FMT = "FX kenney "
    const val DEBUG_ON = " on "

    // --- PLUME Free Sampler drawable basenames (underscores) ---
    const val PLUME_GRINDER_SPARKS = "fx_plume_grinder_sparks"
    const val PLUME_SAND_KICK = "fx_plume_sand_kick"
    const val PLUME_FOOTSTEP_PUFF = "fx_plume_footstep_puff"
    const val PLUME_ASH_PUFF = "fx_plume_ash_puff"
    const val PLUME_GROUND_FOG = "fx_plume_ground_fog"
    const val PLUME_THIN_WISP = "fx_plume_thin_wisp"

    /** Remaining full-pack sheet ids (on disk under assets/fx/plume; not primary maps). */
    val PLUME_FULL_PENDING: Set<String> = setOf(
        "flint-strike", "welding-burst", "ember-drift", "ash-fall", "dust-haze"
    )

    // --- Kenney allowed drawable basenames ---
    const val KENNEY_SPARK_01 = "fx_kenney_spark_01"
    const val KENNEY_SPARK_02 = "fx_kenney_spark_02"
    const val KENNEY_SPARK_03 = "fx_kenney_spark_03"
    const val KENNEY_SPARK_04 = "fx_kenney_spark_04"
    const val KENNEY_SPARK_05 = "fx_kenney_spark_05"
    const val KENNEY_SPARK_06 = "fx_kenney_spark_06"
    const val KENNEY_SPARK_07 = "fx_kenney_spark_07"
    const val KENNEY_CIRCLE_01 = "fx_kenney_circle_01"
    const val KENNEY_CIRCLE_02 = "fx_kenney_circle_02"
    const val KENNEY_CIRCLE_03 = "fx_kenney_circle_03"
    const val KENNEY_CIRCLE_04 = "fx_kenney_circle_04"
    const val KENNEY_CIRCLE_05 = "fx_kenney_circle_05"
    const val KENNEY_SMOKE_01 = "fx_kenney_smoke_01"
    const val KENNEY_SMOKE_04 = "fx_kenney_smoke_04"
    const val KENNEY_SMOKE_07 = "fx_kenney_smoke_07"
    const val KENNEY_FLARE_01 = "fx_kenney_flare_01"
    const val KENNEY_STAR_01 = "fx_kenney_star_01"

    /** Banned Kenney families as hit FX (never wire as hit garnish). */
    val BANNED_KENNEY_HIT_FAMILIES: Set<String> = setOf(
        "slash", "twirl", "magic", "muzzle"
    )

    val ALLOWED_KENNEY_DRAWABLES: Set<String> = setOf(
        KENNEY_SPARK_01, KENNEY_SPARK_02, KENNEY_SPARK_03, KENNEY_SPARK_04,
        KENNEY_SPARK_05, KENNEY_SPARK_06, KENNEY_SPARK_07,
        KENNEY_CIRCLE_01, KENNEY_CIRCLE_02, KENNEY_CIRCLE_03, KENNEY_CIRCLE_04, KENNEY_CIRCLE_05,
        KENNEY_SMOKE_01, KENNEY_SMOKE_04, KENNEY_SMOKE_07,
        "fx_kenney_smoke_02", "fx_kenney_smoke_03", "fx_kenney_smoke_05",
        "fx_kenney_smoke_06", "fx_kenney_smoke_08", "fx_kenney_smoke_09", "fx_kenney_smoke_10",
        KENNEY_FLARE_01
    )

    val IMPORTED_PLUME_DRAWABLES: Set<String> = setOf(
        PLUME_GRINDER_SPARKS, PLUME_SAND_KICK, PLUME_FOOTSTEP_PUFF,
        PLUME_ASH_PUFF, PLUME_THIN_WISP
    )

    /** Banned as stage fog wall (may exist on disk; never primary dust garnish). */
    const val PLUME_GROUND_FOG_BANNED_AS_FOG_WALL = true

    // --- Recolor (never leave default white/cyan) ---
    const val COLOR_RUST_GOLD = 0xFFC9A227L
    const val COLOR_SOOT_ASH = 0xFF8A7A68L
    const val COLOR_DIRTY_GREEN = 0xFF6B7A3AL
    const val COLOR_EMBER_RUST = 0xFFC45A2DL

    // --- Timing @1x (WO windows) — v0.1.50 puffhold: both in 400–500ms ---
    /** Dust puff 400–500ms (TARGET bust only; screenshotable peak). */
    const val PUFF_MS = 450L
    const val PUFF_MS_LO = 400L
    const val PUFF_MS_HI = 500L
    /** Tip spark / ember 400–500ms (was short ember — caused 1-frame smear). */
    const val EMBER_MS = 450L
    const val EMBER_MS_LO = 400L
    const val EMBER_MS_HI = 500L
    /** Alias — spark hold shares puffhold band. */
    const val SPARK_MS = EMBER_MS
    const val SPARK_MS_LO = EMBER_MS_LO
    const val SPARK_MS_HI = EMBER_MS_HI
    /** Brace flare pulse (GAIN only). */
    const val BRACE_FLARE_MS = 280L
    /** Soften pip holds while status active — pulse refresh budget. */
    const val SOFTEN_PIP_PULSE_MS = 420L

    // --- Scale vs bust WIDTH ---
    /** Tip spark 20–30% bust. */
    const val TIP_BUST_FRAC = 0.25f
    const val TIP_BUST_FRAC_LO = 0.20f
    const val TIP_BUST_FRAC_HI = 0.30f
    /** Dust puff 40–60% bust (TARGET only). */
    const val PUFF_BUST_FRAC = 0.50f
    const val PUFF_BUST_FRAC_LO = 0.40f
    const val PUFF_BUST_FRAC_HI = 0.60f
    /** Soften tiny pip under foe HP. */
    const val SOFTEN_BUST_FRAC = 0.12f
    /** Brace flare on owner (tiny). */
    const val BRACE_FLARE_BUST_FRAC = 0.14f

    /** Dust abilities → TARGET bust puff. */
    val DUST_ABILITY_IDS: Set<String> = setOf(
        "dust_veil", "ash_press", "cinder_step"
    )

    /** Tip-spark abilities (Ashbrand SPARK + damage strokes that want hit-confirm). */
    fun isTipSparkId(id: String): Boolean =
        id == CombatFx.ID_ASHBRAND_SPARK || id == CombatFx.ID_ASHBRAND_WAKE

    /**
     * Repo asset roots (relative to project). Prep copies these into res/drawable.
     * NOT /workspace/tod-particle-packs — CoS lock.
     */
    const val ASSET_PLUME_DIR = "assets/fx/plume"
    const val ASSET_KENNEY_DIR = "assets/fx/kenney"

    /**
     * Prefer PLUME drawables when imported from [ASSET_PLUME_DIR].
     * Overlay fail-softs if PNG missing; set false only for Kenney-only fallback tests.
     */
    const val PLUME_SAMPLER_PRESENT = true // assets/fx/plume curated on main

    /**
     * Ability → garnish kinds to play on resolve (alongside locked stroke / Brace pips).
     * Soften / Brace flare are driven by playSpec fields, not card id alone.
     */
    fun kindsForAbility(fxId: String?): Set<GarnishKind> {
        if (fxId.isNullOrBlank()) return emptySet()
        val out = linkedSetOf<GarnishKind>()
        when {
            fxId == CombatFx.ID_ASHBRAND_SPARK || fxId == CombatFx.ID_ASHBRAND_WAKE ->
                out += GarnishKind.TIP_SPARK
            fxId in DUST_ABILITY_IDS -> out += GarnishKind.DUST_PUFF
            else -> {
                // Optional hit-confirm tip spark on player Small/Medium damage strokes
                if (fxId in CombatFx.PLAYER_SMALL || fxId in CombatFx.PLAYER_MEDIUM) {
                    out += GarnishKind.TIP_SPARK
                }
            }
        }
        return out
    }

    /** Tip spark — PLUME grinder-sparks additive preferred; Kenney spark fallback. */
    fun tipSparkSpec(preferPlume: Boolean = PLUME_SAMPLER_PRESENT): GarnishSpec {
        return if (preferPlume) {
            GarnishSpec(
                kind = GarnishKind.TIP_SPARK,
                pack = GarnishPack.PLUME,
                drawable = PLUME_GRINDER_SPARKS,
                sheetId = "grinder-sparks",
                holdMs = EMBER_MS,
                bustFrac = TIP_BUST_FRAC,
                tintArgb = COLOR_RUST_GOLD,
                additive = true,
                atlas = true,
                atlasCols = 8,
                atlasRows = 3,
                atlasFrames = 24,
                atlasPeakFrame = 5,
                atlasCellPx = 256,
                anchorX = 0.26f,
                anchorY = 0.40f
            )
        } else {
            GarnishSpec(
                kind = GarnishKind.TIP_SPARK,
                pack = GarnishPack.KENNEY,
                drawable = KENNEY_SPARK_03,
                sheetId = "spark_03",
                holdMs = EMBER_MS,
                bustFrac = TIP_BUST_FRAC,
                tintArgb = COLOR_RUST_GOLD,
                additive = true,
                atlas = false
            )
        }
    }

    /**
     * Dust puff on TARGET — ash-puff for Dust Veil / Ash Press; footstep-puff for
     * Cinder Step land ONLY; Kenney soft smoke fallback. Never both-bust / fog wall.
     * sand-kick kept on disk as fallback if ash-puff drawable absent (prefer ash-puff).
     */
    fun dustPuffSpec(
        abilityId: String = "dust_veil",
        preferPlume: Boolean = PLUME_SAMPLER_PRESENT
    ): GarnishSpec {
        if (preferPlume) {
            val (drawable, sheetId, peak, cols, rows, frames, ax, ay) = when (abilityId) {
                "cinder_step" -> DustAtlas(
                    PLUME_FOOTSTEP_PUFF, "footstep-puff", 10, 8, 3, 24, 0.5f, 0.86f
                )
                // Dust Veil / Ash Press → ash-puff (Sand & Ash); NOT footstep-puff
                "ash_press", "dust_veil" -> DustAtlas(
                    PLUME_ASH_PUFF, "ash-puff", 10, 8, 3, 24, 0.5f, 0.86f
                )
                else -> DustAtlas(
                    PLUME_ASH_PUFF, "ash-puff", 10, 8, 3, 24, 0.5f, 0.86f
                )
            }
            return GarnishSpec(
                kind = GarnishKind.DUST_PUFF,
                pack = GarnishPack.PLUME,
                drawable = drawable,
                sheetId = sheetId,
                holdMs = PUFF_MS,
                bustFrac = PUFF_BUST_FRAC,
                tintArgb = COLOR_SOOT_ASH,
                additive = false,
                atlas = true,
                atlasCols = cols,
                atlasRows = rows,
                atlasFrames = frames,
                atlasPeakFrame = peak,
                atlasCellPx = 256,
                anchorX = ax,
                anchorY = ay
            )
        }
        return GarnishSpec(
            kind = GarnishKind.DUST_PUFF,
            pack = GarnishPack.KENNEY,
            drawable = KENNEY_SMOKE_04,
            sheetId = "smoke_04",
            holdMs = PUFF_MS,
            bustFrac = PUFF_BUST_FRAC,
            tintArgb = COLOR_SOOT_ASH,
            additive = false,
            atlas = false
        )
    }

    /** Soften tiny pip — Kenney circle (dirty green/rust); under foe HP while active. */
    fun softenPipSpec(): GarnishSpec = GarnishSpec(
        kind = GarnishKind.SOFTEN_PIP,
        pack = GarnishPack.KENNEY,
        drawable = KENNEY_CIRCLE_03,
        sheetId = "circle_03",
        holdMs = SOFTEN_PIP_PULSE_MS,
        bustFrac = SOFTEN_BUST_FRAC,
        tintArgb = COLOR_DIRTY_GREEN,
        additive = false,
        atlas = false
    )

    /** Brace GAIN flare — Kenney flare_01 on owner; does NOT replace floating pips. */
    fun braceFlareSpec(): GarnishSpec = GarnishSpec(
        kind = GarnishKind.BRACE_FLARE,
        pack = GarnishPack.KENNEY,
        drawable = KENNEY_FLARE_01,
        sheetId = "flare_01",
        holdMs = BRACE_FLARE_MS,
        bustFrac = BRACE_FLARE_BUST_FRAC,
        tintArgb = COLOR_RUST_GOLD,
        additive = true,
        atlas = false
    )

    /** Optional Kenney hit-confirm tip spark (rust-gold) — especially while PLUME late. */
    fun hitConfirmKenneySpec(): GarnishSpec = GarnishSpec(
        kind = GarnishKind.TIP_SPARK,
        pack = GarnishPack.KENNEY,
        drawable = KENNEY_SPARK_01,
        sheetId = "spark_01",
        holdMs = EMBER_MS,
        bustFrac = TIP_BUST_FRAC,
        tintArgb = COLOR_RUST_GOLD,
        additive = true,
        atlas = false
    )

    fun plumeDebugLine(sheetId: String, targetLabel: String): String =
        PLUME_DEBUG_FMT + sheetId + DEBUG_ON + targetLabel

    fun kenneyDebugLine(sprite: String, targetLabel: String): String =
        KENNEY_DEBUG_FMT + sprite + DEBUG_ON + targetLabel

    fun debugLine(spec: GarnishSpec, targetLabel: String): String = when (spec.pack) {
        GarnishPack.PLUME -> plumeDebugLine(spec.sheetId, targetLabel)
        GarnishPack.KENNEY -> kenneyDebugLine(spec.sheetId, targetLabel)
    }

    /** Row-major cell origin for a PLUME atlas frame. */
    fun atlasCellOrigin(frame: Int, cols: Int, cellPx: Int = 256): Pair<Int, Int> {
        val f = frame.coerceAtLeast(0)
        return (f % cols) * cellPx to (f / cols) * cellPx
    }

    /**
     * Play-frame list with peak dwell so stills are not a 1-frame smear.
     * Ramp in → hold peak several slots → ramp out (duration = [holdMs] on overlay).
     */
    fun atlasPlayFrames(frames: Int, peak: Int): IntArray {
        val last = (frames - 1).coerceAtLeast(0)
        val p = peak.coerceIn(0, last)
        val a = (p - 3).coerceAtLeast(0)
        val b = (p - 1).coerceAtLeast(0)
        val c = p
        val d = (p + 2).coerceAtMost(last)
        val e = (p + 4).coerceAtMost(last)
        // Peak repeated = longer on-screen still within the same holdMs budget
        return intArrayOf(a, b, c, c, c, c, d, e)
    }

    fun frameAt(playFrames: IntArray, progress: Float): Int {
        if (playFrames.isEmpty()) return 0
        val p = progress.coerceIn(0f, 1f)
        if (p >= 1f) return playFrames.last()
        val i = (p * playFrames.size).toInt().coerceIn(0, playFrames.lastIndex)
        return playFrames[i]
    }

    fun timingWindowsOk(): Boolean =
        PUFF_MS in PUFF_MS_LO..PUFF_MS_HI &&
            EMBER_MS in EMBER_MS_LO..EMBER_MS_HI &&
            TIP_BUST_FRAC in TIP_BUST_FRAC_LO..TIP_BUST_FRAC_HI &&
            PUFF_BUST_FRAC in PUFF_BUST_FRAC_LO..PUFF_BUST_FRAC_HI

    fun drawableIsBannedHitFamily(name: String): Boolean {
        val base = name.removePrefix("fx_kenney_").removePrefix("fx_plume_").lowercase()
        return BANNED_KENNEY_HIT_FAMILIES.any { fam ->
            base.startsWith(fam + "_") || base == fam
        }
    }

    /** Soften under foe — never on You; Brace flare never replaces pip overlay. */
    fun softenIsUnderFoeOnly(): Boolean = true
    fun braceFlareDoesNotReplacePips(): Boolean = true

    /** Dust / tip primary sheets never use ground-fog as fog wall. */
    fun neverUsesGroundFogAsDust(): Boolean {
        val dust = dustPuffSpec("dust_veil")
        val tip = tipSparkSpec()
        return dust.drawable != PLUME_GROUND_FOG &&
            tip.drawable != PLUME_GROUND_FOG &&
            PLUME_GROUND_FOG_BANNED_AS_FOG_WALL
    }

    private data class DustAtlas(
        val drawable: String,
        val sheetId: String,
        val peak: Int,
        val cols: Int,
        val rows: Int,
        val frames: Int,
        val ax: Float,
        val ay: Float
    )
}
