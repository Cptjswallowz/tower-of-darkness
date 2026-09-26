# Combat FX — v0.1.48-strokeboth (WO Elliott / Engineer)

Status: **implemented** (thick crescent BOTH sides; not Round-cap pill).  
**Do not** tag / gh release here — hold until CoS green. Tag later: `v0.1.48-strokeboth`.

**Aim stays v0.1.40-fxfix:** target bust only / half-stage clip / no screen flash / Brace Shield stamps.  
**Path primary stays v0.1.46+:** `CombatStrokeOverlay` → `drawWakeFamilyStroke`; CLEAVE tip optional.  
**Fat widths stay v0.1.47:** bust-frac core/glow; You-Shiv ENEMY core `0.10` / glow `0.22` **KEEP**.

Prior partial FAIL on 0.1.47: You-stroke was a **rounded pill / capsule** (Round cap + fat core on modest quadratic); Emberbrand/Dust Veil/Hostflint still **numbers-only on goblin** in stills (weak curve / short peak / cream blend).

**Frozen:** F3 / loadout / Hub / shop / rest / enemy kits / Ashbrand ember math; Wake gold arc art/timing; Brace→shield sheet; buy/wire PLUME/Kenney/Ashen; fullscreen FX; CLEAVE crop / `BUST_*`.

---

## Fix

1. **Shape — filled crescent blade** (not Round stadium pill):
   - `crescentBladePath`: outer quadratic arc + reverse inner arc; tips meet → taper like Wake.
   - Bow bulge ≥ half-chord (`STROKE_CRESCENT_BOW_MULT = 1.20`); inner bow frac `0.38`.
   - Glow + core + highlight as **filled** crescents; **no** `StrokeCap.Round` primary.
2. **Peaks @1x** — player **500ms**; enemy **450ms**. Totals 580 / 520–540 (≥ peak + short fade).
3. **Widths kept** — player core `0.14` / `0.18` (≥ enemy You `0.10`); glow `0.30` / `0.38` / `0.22`.
4. **Color** — `COLOR_STROKE_YOU = 0xFFFFF6E4` (brighter steel/ember-white for green goblin).
5. **Outgoing** Hostflint / Emberbrand / Dust Veil dmg / Pike / Ruin Seal / Ash Press / Spark → FOE + `FX stroke on <enemy>`. Soften-only stays NO_STROKE.
6. **Incoming** Hit / Nip / Shiv / Seal Pulse / Coal Slam → YOU + `FX stroke on You` (`resolveFxPlayer` kept).
7. **Wake** gold arc untouched / larger / exclusive.

---

## strokeWidth player vs enemy (lock table)

| Constant | Value | ~px @ stage 360 |
|----------|-------|-----------------|
| `STROKE_CORE_BUST_FRAC_PLAYER_SMALL` | `0.14` | **~19.7** |
| `STROKE_CORE_BUST_FRAC_PLAYER_MEDIUM` | `0.18` | **~25.3** |
| `STROKE_CORE_BUST_FRAC_ENEMY` (You-Shiv) | `0.10` | **~14.0** |
| `STROKE_GLOW_BUST_FRAC_PLAYER_SMALL` | `0.30` | ~42.1 |
| `STROKE_GLOW_BUST_FRAC_PLAYER_MEDIUM` | `0.38` | ~53.4 |
| `STROKE_GLOW_BUST_FRAC_ENEMY` | `0.22` | ~30.9 |
| `STROKE_CRESCENT_BOW_MULT` | `1.20` | bow ≥ half-chord |
| `STROKE_CRESCENT_INNER_BOW_FRAC` | `0.38` | inner < outer |
| `STROKE_SHAPE_FILLED_CRESCENT` | `true` | not Round pill |
| `COLOR_STROKE_YOU` | `0xFFFFF6E4` | |
| `COLOR_STROKE_ENEMY` | `0xFFC4B8A8` | |
| `STROKE_PLAYER_PEAK_MS` | `500` | |
| `STROKE_ENEMY_PEAK_MS` | `450` | |
| `STROKE_SMALL_MS` / `MEDIUM` | `580` | |
| `STROKE_ENEMY_SMALL_MS` / `MEDIUM` | `520` / `540` | |

Formula: `bustWidthPx = stageWidth × 0.5 × STROKE_BUST_WIDTH_FRAC(0.78)`;  
`corePx = bustWidthPx × STROKE_CORE_BUST_FRAC_*`.

**Shape note:** v0.1.47 used `StrokeCap.Round` + fat core on a modest quadratic → stadium/capsule.  
v0.1.48 draws a **filled crescent Path** (outer arc + reverse inner arc) with Wake-like bow.

---

## DO NOT

- Another scale-CLEAVE ticket / touch Wake gold arc  
- Brace→shield sheet; F3 / loadout / rumors / Hub / shop / rest / enemy kits  
- Buy/wire PLUME / Kenney / Ashen; fullscreen / both-bust FX  
- Tag or gh release until CoS green  
- Bump README release line this tip  

---

## Tests

`StrokeBothV0148Test` — player core ≥ enemy You; peaks 500/450; crescent/not-pill; Hostflint/Emberbrand→FOE; Hit/Nip/Shiv/Seal Pulse→YOU; debug strings; Wake/CLEAVE frozen.
