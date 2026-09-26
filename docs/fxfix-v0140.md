# Combat FX fix — v0.1.40-fxfix (WO Elliott / Engineer)

Status: **implemented** (roll aim to v0.1.38, re-apply v0.1.39 size/hold; half-stage clip + tamed glow + Brace Shield stamps).  
**Do not** invent a new overlay. **Do not** tag / gh release here.

**Frozen:** Wake **math**; Climb; Path; Hub; Soften rules; tile-flash scoping (skill/enemy tiles only); who gets FX; tiers / colors / 2x / tap-skip / log.

**Keep:** v0.1.38 recipient-only aim (`slashCutGeom`, FOE for player / YOU for enemy); v0.1.39 size/hold (thickness 1.4×, stroke +200ms, Brace 700/350 + fade last 200ms, radius 0.027); Wake foe-only via existing `WakeStageOverlay` 0.58 clip.

---

## Problem (Elliott FAIL on 0.1.39)

0.1.39 only raised thickness / holds / pip size — but glow used `stroke.thickness * 7f` on a full-width Canvas, so heavier thickness bloomed into a **full-screen green/gold plate wash**. Brace Path diamonds became unreadable / vanished.

---

## KILL LIST (must not ship)

| Ban | Fix |
|-----|-----|
| Full-screen flash | Hard `clipRect` slash/brace to recipient/owner half-stage |
| Colored rectangle over whole combat row or whole phone | No stage wash; tile flash stays `matchParentSize()` on tiles only |
| Tint covering You **and** foe at once | `recipientClipXFrac`: YOU `0f..0.5f`, FOE `0.5f..1f` |
| Unbounded glow bloom with 1.4× thickness | Glow width = `(thickness / 1.4f) * 7f` (= prior × 7); core keeps full 1.4× |

---

## SLASH

| Rule | Lock |
|------|------|
| Aim | Recipient-only from v0.1.38 (`slashCutGeom`); player → FOE, enemy → YOU |
| Thickness | `THICK_SMALL=2.1f`, `THICK_MEDIUM=4.2f` (1.4× vs 0.1.38 priors 1.5/3.0) |
| Hold | `STROKE_SMALL_MS=400`, `STROKE_MEDIUM_MS=480`; Wake `FRAME_SLASH_MS=700` |
| Clip | `CombatFx.recipientClipXFrac(recipient)` + `clipRect` in `CombatStrokeOverlay` |
| Glow | `strokeGlowWidth(t) = (t / 1.4f) * 7f` — absolute ≤ 0.1.38 glow |
| Core | `strokeCoreWidth(t) = t * 3.2f` — ≥ 1.4× prior core |

Wake: still foe-only via existing WakeStageOverlay 0.58 clip; do **not** change Wake math/art names.

---

## BRACE (must render)

| Rule | Lock |
|------|------|
| Count | Brace gained, cap 5 |
| Owner | Owner bust (You Brace → YOU; foe Hide → FOE) |
| Hold | 700ms@1x / 350@2x; fade last 200ms via `bracePipAlpha` |
| Size | `BRACE_PIP_RADIUS_FRAC = 0.027` (1.5× prior 0.018) |
| Art | **Primary:** `Icons.Filled.Shield` / Moss (StatusPipRow language) inside existing `CombatBracePipsOverlay`; Path diamond secondary |
| Clip | Owner half-stage via same `recipientClipXFrac` |
| First frame | `bracePipProgress = 0f` while visible **before** step loop (full alpha) |

Log still prints Brace +N (engine untouched).

---

## DO NOT

New skills; Wake math; Climb; Path; Hub; invent new overlay types; change who gets FX; Soften rules; move tile-flash onto the stage.

---

## Files

| File | Role |
|------|------|
| `domain/combat/CombatFx.kt` | TAG `v0.1.40-fxfix`; `recipientClipXFrac`; `strokeGlowWidth` / `strokeCoreWidth`; size/hold locks kept |
| `ui/components/CombatFxOverlay.kt` | Half-stage clip; tamed glow; Brace Shield stamps |
| `ui/screens/CombatScreen.kt` | `bracePipProgress = 0f` before pip step loop |
| `CombatFxFixV0140Test.kt` | Clip ranges, glow tame, size/hold, playSpec aim owners |
| `docs/fxfix-v0140.md` | This lock |

Prior aim / read / kernel tests stay green (TAG asserts updated where needed).

---

## Checklist (Engineer)

- [x] No full-screen flash / whole-row tint / tint over You+foe
- [x] Slash clipped to recipient half; glow ≤ 0.1.38 absolute; core ≥ 1.4× prior
- [x] Brace Shield stamps visible; clip to owner half; first frame full alpha
- [x] Size/hold from 0.1.39 kept; aim from 0.1.38 restored; who-gets-FX unchanged
- [x] No new overlay type; no tag / gh release this tip
