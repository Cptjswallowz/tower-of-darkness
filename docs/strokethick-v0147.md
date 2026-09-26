# Combat FX — v0.1.47-strokethick (WO Elliott / Engineer)

Status: **implemented** (fat Wake-family stroke PRIMARY; enemy→You wired).  
**Do not** tag / gh release here — hold until CoS green. Tag later: `v0.1.47-strokethick`.

**Aim stays v0.1.40-fxfix:** target bust only / half-stage clip / no screen flash / Brace Shield stamps.  
**Path primary stays v0.1.46-strokefallback:** `CombatStrokeOverlay` → `drawWakeFamilyStroke`; CLEAVE tip optional.  
Prior FAIL: Elliott on 0.1.46 — Hostflint/Emberbrand thin white hairline only; enemy Hit/Seal Pulse numbers-only (no `FX stroke on You`).

**Frozen:** F3 / loadout / Hub / shop / rest / enemy kits / Ashbrand ember math; Wake gold arc art/timing; Brace→shield sheet; buy/wire PLUME/Kenney/Ashen; fullscreen FX.  
**Do NOT** rescale CLEAVE crop / `BUST_WIDTH_FRAC` / `BUST_COVERAGE` as the fix.

---

## Why (Elliott FAIL on 0.1.46)

1. **Hairline:** path length (half-extent ~70–90% bust) was fine; **Canvas stroke WIDTH** was `THICK_SMALL(2.1) × STROKE_CORE_MULT(3.2) ≈ 6.7px` on a ~360 stage — unreadably thin vs Wake art.
2. **Enemy numbers-only:** domain already built enemy strokes with recipient YOU, but CombatScreen used `fxEvent?.fxPlayer != false`. If the flag were ever wrong/defaulted, enemy hits aimed FOE. Hardened with `CombatFx.resolveFxPlayer` (enemy kit ids always false) + sync `strokeDebugLine = "FX stroke on You"` when firing.

---

## Fix

1. **Fat canvas widths** — core/glow as **fraction of bust WIDTH** (scales with stage):
   - `STROKE_CORE_BUST_FRAC_PLAYER_SMALL = 0.14` → ~19.7px @360
   - `STROKE_CORE_BUST_FRAC_PLAYER_MEDIUM = 0.18` → ~25.3px
   - `STROKE_CORE_BUST_FRAC_ENEMY = 0.10` → ~14.0px (slightly thinner)
   - Glow: `0.30` / `0.38` / `0.22` of bust
   - Overlay: `strokeCoreWidthPx` / `strokeGlowWidthPx` in `drawWakeFamilyStroke`
2. **Bright colors** (ARGB):
   - `COLOR_STROKE_YOU = 0xFFF2E6D0` (bright steel / ember-white)
   - `COLOR_STROKE_ENEMY = 0xFFC4B8A8` (duller steel/ash, still visible)
   - `COLOR_STROKE_ENEMY_EMBER = 0xFFD0B49A`
   - `COLOR_STROKE_CORE_HIGHLIGHT = 0xFFFFF8EC`
3. **Peak holds @1x** — player **470ms** (lock 450–550); enemy **400ms** (lock 350–450). Totals 500 / 480–500 (≥ peak + fade). 2x via `fxHoldMs`.
4. **Enemy→You** — `resolveFxPlayer` + `enemyHitProducesYouStroke` proof; CombatScreen sync debug line.
5. **Wake** gold arc unchanged / exclusive. CLEAVE tip garnish only. Debug lines kept.

---

## strokeWidth / color constants (lock table)

| Constant | Value |
|----------|-------|
| `THICK_SMALL` / `THICK_MEDIUM` | `2.1` / `4.2` (relative weight; not px) |
| `THICK_ENEMY_FACTOR` | `0.78` |
| `STROKE_CORE_MULT` (hairline docs) | `3.2` → hairline core ≈ `6.72px` |
| `STROKE_CORE_BUST_FRAC_PLAYER_SMALL` | `0.14` |
| `STROKE_CORE_BUST_FRAC_PLAYER_MEDIUM` | `0.18` |
| `STROKE_CORE_BUST_FRAC_ENEMY` | `0.10` |
| `STROKE_GLOW_BUST_FRAC_PLAYER_SMALL` | `0.30` |
| `STROKE_GLOW_BUST_FRAC_PLAYER_MEDIUM` | `0.38` |
| `STROKE_GLOW_BUST_FRAC_ENEMY` | `0.22` |
| `REF_STAGE_WIDTH_PX` | `360` |
| `COLOR_STROKE_YOU` | `0xFFF2E6D0` |
| `COLOR_STROKE_ENEMY` | `0xFFC4B8A8` |
| `COLOR_STROKE_ENEMY_EMBER` | `0xFFD0B49A` |
| `COLOR_STROKE_CORE_HIGHLIGHT` | `0xFFFFF8EC` |
| `STROKE_PLAYER_PEAK_MS` | `470` |
| `STROKE_ENEMY_PEAK_MS` | `400` |
| `STROKE_SMALL_MS` / `MEDIUM` | `500` |
| `STROKE_ENEMY_SMALL_MS` / `MEDIUM` | `480` / `500` |

Formula: `bustWidthPx = stageWidth × 0.5 × STROKE_BUST_WIDTH_FRAC(0.78)`;  
`corePx = bustWidthPx × STROKE_CORE_BUST_FRAC_*`.

---

## DO NOT

- Another scale-CLEAVE ticket / touch Wake gold arc  
- Brace→shield sheet; F3 / loadout / rumors / Hub / shop / rest / enemy kits  
- Buy/wire PLUME / Kenney / Ashen; fullscreen / both-bust FX  
- Tag or gh release until CoS green  
- Bump README release line this tip  

---

## Tests

`StrokeThickV0147Test` — fat vs hairline cores; peaks; enemy→You + `FX stroke on You`; bright colors; Wake exclusive; architecture kept.
