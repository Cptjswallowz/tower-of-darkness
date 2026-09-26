# Combat FX — v0.1.46-strokefallback (WO Elliott / Engineer)

Status: **implemented** (drawn Wake-family stroke PRIMARY; CLEAVE tip optional).  
**Do not** tag / gh release here — hold until CoS green. Tag later: `v0.1.46-strokefallback`.

**Aim stays v0.1.40-fxfix:** target bust only / half-stage clip / no screen flash / Brace Shield stamps.  
Prior FAIL: `docs/slashscale-v0145.md` — Hostflint/Emberbrand/enemy Hit still numbers+log only; only Wake gold arc readable.

**Frozen:** F3 / loadout / Hub / shop / rest / enemy kits / Ashbrand ember math; Wake gold arc art/timing; Brace→shield sheet; buy/wire PLUME/Kenney/Ashen; fullscreen FX.  
**Do NOT** rescale CLEAVE crop / `BUST_WIDTH_FRAC` / `BUST_COVERAGE` as the fix (0.1.45 constants frozen).

---

## Why (Elliott FAIL on 0.1.45)

Hostflint / Emberbrand / Dust Veil / Cinder Step / enemy Hit still **numbers + log only**. Only readable cut was Wake gold arc.

**Root cause:** `CombatStrokeOverlay` preferred the CLEAVE sheet whenever decode succeeded; `drawPathFallback` only ran if the sheet was missing. On device the sheet decoded → path never showed → tiny/illegible glow blob (or invisible cut). Scaling the sheet again does not fix readability.

---

## Fix

1. **Drawn path is PRIMARY** — Wake-family glow + thick core quadratic crescent on recipient bust via `drawWakeFamilyStroke` (`CombatFxOverlay.kt`). Same family as Wake arc language (NOT flipbook cell).
2. **Geometry** — `CombatFx.slashCutGeom` / `strokeHalfXFrac` sized so full stroke ≈ **70–90% bust width** (player Small 78% / Medium 88%); enemy hits on You ≈ **60%**. Half-stage clip unchanged; never spans both busts.
3. **Timing @1x** — player peak linger **420ms** (lock 400–500) then fade; total **500ms**. Enemy peak **320ms** (lock 300–400); total 360/380ms. 2x shortens via `fxHoldMs`. **1x is pass/fail.**
4. **Color** — steel / ash / ember (`COLOR_STROKE_YOU` / `COLOR_STROKE_ENEMY`). No forced green/gold SrcIn on the stroke. Wake gold arc untouched.
5. **Ashbrand SPARK** — short drawn stroke (`STROKE_SPARK_MS=280`) + optional tip spark / hit-flash. FULL Wake stays `WakeStageOverlay` / `specForWake` only.
6. **Optional CLEAVE tip garnish** — tiny dst at stroke tip only (`drawCleaveTipGarnish`). Invisible tip is **NOT** fail if stroke is readable. Never another bust-coverage rescale of the sheet.
7. **Debug** — always `FX stroke on <target>` (path). `FX slash-light on <target>` only if tip garnish draws. Combat-log echo for both.

---

## Composable path

| Piece | File / symbol |
|-------|----------------|
| Overlay entry | `ui/components/CombatFxOverlay.kt` → `CombatStrokeOverlay` |
| Primary cut | `drawWakeFamilyStroke` |
| Optional tip | `drawCleaveTipGarnish` |
| Domain | `domain/combat/CombatFx.kt` — TAG, widths, peaks, `strokeDebugLine`, spark tier |
| CleaveKit | TAG bump only — scale constants **frozen** from 0.1.45 |
| Wake | `WakeStageOverlay` — **untouched** |

---

## Proof locks

| Knob | Lock |
|------|------|
| Primary FX | Drawn path (Wake-family), not CLEAVE flipbook |
| Player width | 70–90% bust (`STROKE_WIDTH_FRAC_PLAYER_*`) |
| Enemy width | ~60% bust |
| Player peak | 400–500ms @1x then fade |
| Enemy peak | 300–400ms @1x |
| Spark | Short stroke + tip; Wake exclusive |
| CLEAVE | No rescale; tip garnish optional |
| Debug | `FX stroke on` ≠ `FX slash-light on` |
| Wake / Brace | Untouched |

---

## DO NOT

- Another scale-CLEAVE ticket / touch Wake gold arc  
- Brace→shield sheet; F3 / loadout / rumors / Hub / shop / rest / enemy kits  
- Buy/wire PLUME / Kenney / Ashen; fullscreen / both-bust FX  
- Tag or gh release until CoS green  
- Bump README release line this tip  

---

## Tests

`StrokeFallbackV0146Test` — path primary, widths, peaks, debug strings, Wake exclusive, no CLEAVE rescale, spark stroke, enemy shorter.
