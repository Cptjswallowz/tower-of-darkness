# Combat FX readability — v0.1.39-fxread (WO Elliott / CoS)

Status: **implemented** (readability pass on v0.1.38-fxaim aim/paint).  
Design lock + Engineer checklist below.

**Frozen:** Wake **math**; path / Hub / Climb; Soften; tile flash; tiers / colors / 2x fail-open / tap-skip; combat log; who gets FX (recipient slash, Brace owner pips).  
**Keep from v0.1.38:** target-only slash (no plate across both busts); Brace pips = gain capped at 5; kernel order flash → stroke|brace-pips → float → shake → log; role colors; Wake exclusive foe clip.

**Scope:** Tower of Darkness greenfield only. **Do not** bump README tag line here (release commit owns that).

---

## Problem (locked)

Brace shields too small / die too fast; slashes need more weight. Target-only slash PASS; no plate across both busts.

---

## BRACE SHIELDS (owner bust only)

| Rule | Lock |
|------|------|
| Pip size | **1.5×** prior (`pipR` was `min(w,h)*0.018f` → `CombatFx.BRACE_PIP_RADIUS_FRAC = 0.027f`) |
| Hold | **700ms at 1x** (350ms at 2x via `CombatFx.fxHoldMs`) — `BRACE_PIP_MS` 560 → **700** |
| Fade | **Last 200ms only** (`BRACE_PIP_FADE_MS = 200`); full opacity until then via `CombatFx.bracePipAlpha(progress)` |
| Alpha floor | ~0.15 (unchanged floor) |
| Count | Still Brace gained, cap 5 (`bracePipCount`) |

---

## SLASH (same target-only rule)

| Rule | Lock |
|------|------|
| Thickness | **1.4×**: `THICK_SMALL` 1.5 → **2.1f**, `THICK_MEDIUM` 3.0 → **4.2f** |
| Hold | **+200ms at 1x**: `STROKE_SMALL_MS` 200 → **400**, `STROKE_MEDIUM_MS` 280 → **480** (2x halves → +100ms at 2x) |
| Aim | Unchanged — recipient bust only; geometry via `slashCutGeom` |

### Wake slash

Same extra hold: `WakeArt.FRAME_SLASH_MS` 500 → **700**. Still foe only via existing `WakeStageOverlay`; do **not** change aim/clip.

---

## DO NOT

- Full-row plate; new skills; Wake math; Climb clip; change aim / who gets FX
- Soften; tile flash; tiers / colors / 2x fail-open / tap-skip; combat log; frozen systems

---

## Files

| File | Role |
|------|------|
| `domain/combat/CombatFx.kt` | TAG `v0.1.39-fxread`, thickness / stroke ms / Brace hold+fade+radius / `bracePipAlpha` |
| `ui/components/CombatFxOverlay.kt` | 1.5× pip size + `bracePipAlpha(progress)` |
| `domain/combat/WakeArt.kt` | `FRAME_SLASH_MS` += 200 |
| `CombatFxReadV0139Test.kt` | Thickness 1.4×, stroke/brace holds, fade alpha, Wake 700, aim frozen |
| `docs/fxread-v0139.md` | This lock |

Prior aim tests (`CombatFxAimV0138Test`, `CombatFxV0137Test`) stay green. WakeArt timing tests updated for +200ms slash frame only.

---

## Checklist (Engineer)

- [x] Brace pips 1.5× size; hold 700ms / 350 at 2x; fade last 200ms only
- [x] Slash 1.4× thick; stroke holds +200ms; Wake FRAME_SLASH_MS 700
- [x] Who-gets-FX / aim / Soften / tile flash / tiers / colors unchanged
- [x] No README tag bump; no GitHub tag/release this feature tip
