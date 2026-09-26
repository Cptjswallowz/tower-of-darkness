# Combat FX — v0.1.44-slashproof (WO Elliott / Engineer)

Status: **implemented** (prove `fx_slash_light` loaded + readable on busts).  
**Do not** invent new overlay architecture. **Do not** tag / gh release here.

**Aim stays v0.1.40-fxfix:** target bust only / half-stage clip / no screen flash / tamed glow / Brace Shield stamps.  
Prior art: `docs/slashread-v0143.md` + `docs/cleavekit-v0142.md`.

**Frozen:** F3 graph/trolls/loadout/Gate-Warden/Hub; F1/F2 flow; Wake art/timing/math (`wake_vfx_*` / WakeStageOverlay); Brace floating pips; damage weights; new skills/Hub; full-row plate; full CLEAVE 48; **do not** wire slash-light onto Wake; Force Shield / shield-block.

---

## Why (third FAIL)

Elliott FAIL on 0.1.43: Tower Pike / Hostflint / Ash Press / Nip / Hit / Shiv = **numbers/log only**; Wake old gold arc; slash not readable.

**Map audit (what was actually broken):**

| Layer | Status @ 0.1.43 | Finding |
|-------|-----------------|---------|
| `CombatEngine` `fxId` | OK | Hostflint / Pike / Ash Press / Hit / Nip / Shiv / Cleave / Club set `fxId` correctly |
| `CombatFx.playSpec` / `stroke` | OK | Small/Medium ids produce non-null `stroke` |
| `CombatScreen` `strokeVisible` | OK | Beats fire (floats prove resolve path) |
| Sheet in APK | OK | `res/drawable/fx_slash_light.png` packaged |
| `R.drawable.fx_slash_light` | OK | Compile-linked load path |
| **Draw readability** | **BROKEN** | Atlas crescent occupies only ~17–38% of each 256px cell (heavy padding). Drawing the **full cell** at `min(w,h)×0.72` left a ~30dp-thin crescent — unreadably tiny on busts. Not a missing map. |

**Fix:** content **crop** (`SLASH_CROP_PX=144` biased into the opaque region) + size from **bust diameter** (half-stage based) × `BUST_COVERAGE=0.82` + stronger tint (SrcIn + brief un-tinted core) + debug log proving draw.

---

## Proof locks

| Knob | Lock |
|------|------|
| Sheet | `fx_slash_light` in APK (`unzip -l` → `res/drawable/fx_slash_light.png`) |
| Map | Small+Medium player hits + enemy Hit/Nip/Shiv/Cleave/Club → `stroke` → `CombatStrokeOverlay` draws sheet |
| Scale | `BUST_COVERAGE = 0.82` (0.70–0.90 of **bust**). Formula: `bustDiameter = min(halfStageW×0.55, stageH×0.72)`; `dst = bustDiameter × 0.82 × slashScale`. Medium ≥ Small (1.3×). |
| Crop | `SLASH_CROP_PX=144` @ ox=112,oy=24 inside each 256 cell — zoom past empty padding |
| Tint | Gold/ember player; dirty green/rust enemy; SrcIn + brief un-tinted core |
| Peak | Brightest **200 ms** @1x on frame 6; total 1x ≤ **500 ms**; 2x halves via `fxHoldMs` |
| Hit-flash | Contact flash ON the cut (recipient bust), not fullscreen |
| Debug (this tip) | On every Small/Medium slash-light **sheet** draw: `FX slash-light on <target>` in combat log + `Log.i("TodFx", …)`. Target = `You` / foe name / `foe`. |

---

## Wire map (unchanged intent)

| Beat | Sheet | Where |
|------|-------|-------|
| Player Small | slash gold/ember + contact hit-flash | Foe bust |
| Player Medium | slash 1.3× gold + contact hit-flash | Foe bust |
| Enemy Small | slash dirty green/rust + contact hit-flash | You bust |
| Enemy Medium | slash 1.3× role tint + contact hit-flash | You bust |
| Wake | existing `wake_vfx_*` | Foe — **no** slash-light |
| Brace | floating Shield pips | Owner — **not** shield-block |

---

## Debug log wiring

- Helper: `CombatFx.slashLightDebugLine(target)` → `"FX slash-light on " + target`
- Overlay: `CombatStrokeOverlay` `LaunchedEffect` when `visible && slashSheet != null` → `Log.i(TodFx, line)` + `onSlashLightDebug`
- Screen: merges line into player-visible combat log (gold) while stroke holds
- Unit test: `SlashProofV0144Test.debugLog_formatConstant_andHelper`

If Pike/Hostflint fire floats but this line never prints → map broken (re-audit `fxId` → `playSpec` → `strokeVisible` → sheet decode).

---

## DO NOT

- Reskin damage numbers as a fake fix  
- Replace Wake / Brace / F3 / Hub / damage weights  
- Tag or gh release until CoS green  
- Force-push  
- Bump README release  

---

## Files

| File | Role |
|------|------|
| `domain/combat/CleaveKit.kt` | TAG; BUST_COVERAGE 0.82; bustDiameter; crop; peak |
| `domain/combat/CombatFx.kt` | TAG; debug helpers |
| `ui/components/CombatFxOverlay.kt` | Crop draw; bust scale; tint; TodFx log |
| `ui/screens/CombatScreen.kt` | debugTarget + combat-log echo |
| `SlashProofV0144Test.kt` | New locks |
| Prior FX TAG tests | Advanced to v0.1.44 |
| `docs/slashproof-v0144.md` | This lock |

---

## Engineer checklist

- [x] `fx_slash_light` in APK (prove unzip path+bytes)  
- [x] Bust coverage 0.70–0.90; Medium 1.3×; peak 200ms; ≤500ms  
- [x] Content crop so crescent fills bust (not empty padding)  
- [x] Debug log format + overlay/screen wiring  
- [x] Contact hit-flash on cut; Wake + Brace frozen  
- [x] No tag / no gh release this tip  
