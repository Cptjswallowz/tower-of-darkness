# Combat FX — v0.1.43-slashread (WO Elliott / Engineer)

Status: **implemented** (phone-readable CLEAVE `fx_slash_light` on busts).  
**Do not** invent new overlay architecture. **Do not** tag / gh release here.

**Aim stays v0.1.40-fxfix:** target bust only / half-stage clip / no screen flash / tamed glow / Brace Shield stamps.  
Prior art: `docs/cleavekit-v0142.md` + `docs/art-audio/CLEAVE_v0.1.42.md`.

**Frozen:** F3 graph/trolls/loadout/Gate-Warden/Hub; F1/F2 flow; Wake art/timing/math (`wake_vfx_*` / WakeStageOverlay); Brace floating pips; damage weights; new skills/Hub; full-row plate; full CLEAVE 48; **do not** wire slash-light onto Wake.

---

## Why

Elliott FAIL on 0.1.42: Tower Pike / Hostflint / Cinder Step / Emberbrand showed **ONLY damage numbers**; sampler slash not phone-readable (shy 0.44× scale + Modulate wash → faint grey).

---

## Wire map (FORCE)

| Beat | Sheet | Where | Notes |
|------|-------|-------|-------|
| **Player Small** | `fx_slash_light` gold/ember + contact hit-flash | **Foe bust only** | hostflint, cinder_step, emberbrand, … |
| **Enemy Small** | slash dirty green/rust + contact hit-flash | **You bust only** | hit / club / shiv / nip / gate_pulse |
| **Player Medium** | slash **1.3×** gold + contact hit-flash | Foe only | tower_pike, ruin_seal, spark_tithe, wake_echo |
| **Enemy Medium** | slash **1.3×** role tint + contact hit-flash | You only | cleave / seal_pulse / coal_slam |
| **Sparks (Ashbrand)** | `fx_hit_flash` (+ additive) ember | **Hit target (foe)** | Unchanged path; longer `HIT_FLASH_MS` |
| **Wake** | existing `wake_vfx_*` | Foe | **Do not** replace / do not attach slash-light |
| **Brace** | existing floating Shield pips | Owner bust | **Not** shield-block |

Kernel order: tile flash → stroke **+ overlapping contact hit-flash** → brace-pips → float → shake → log.

---

## Phone-readable locks

| Knob | Lock |
|------|------|
| Tint | Role color via `ColorFilter.tint(..., SrcIn)` — gold/ember (You) / dirty green/rust (enemy). Not Modulate wash. |
| Scale | `BUST_COVERAGE = 0.72` × `slashScale(tier)`; crescent ≈ **60–80%** of bust / stage min side. Formula: `min(w,h) * 0.72 * scale`. |
| Medium | `MEDIUM_SCALE = 1.3f` (was 1.2) + `STROKE_MEDIUM_MS = 480` (slightly longer than Small 400) |
| Peak hold | Brightest ~**200 ms** Small / **220 ms** Medium @1x on peak frame 6; then fade. Total @1x ≤ **500 ms**. |
| 2x | Halves via `CombatFx.fxHoldMs` (peak scales with hold). |
| Contact flash | `CONTACT_HIT_FLASH_MS = 200` on every Small/Medium damage stroke; overlaps cut start; additive/local to bust. |
| Sheet path | Prefer compile-linked `R.drawable.fx_slash_light` (must be in APK). Path fallback only if decode fails. |

---

## DO NOT

- Replace Wake / Brace / F3 graph / Hub / damage weights / Gate-Warden kit  
- Full-row plate / screen flash boxes / cyan ovals / shield-block  
- Tag or gh release until CoS green  
- Force-push  

---

## Files

| File | Role |
|------|------|
| `domain/combat/CleaveKit.kt` | TAG `v0.1.43-slashread`; BUST_COVERAGE; MEDIUM 1.3; peak delays; CONTACT_HIT_FLASH_MS |
| `domain/combat/CombatFx.kt` | TAG; Small/Medium `useCleaveHitFlash` contact |
| `ui/components/CombatFxOverlay.kt` | Scale / SrcIn tint / peak linger / `R.drawable.fx_slash_light` |
| `ui/screens/CombatScreen.kt` | Overlap contact hit-flash with stroke; role tint |
| `SlashReadV0143Test.kt` | New locks |
| `CleaveKitV0142Test.kt` + prior FX TAG tests | Advanced to v0.1.43 / 1.3 |
| `docs/slashread-v0143.md` | This lock |

---

## Engineer checklist

- [x] Slash-light on every Small/Medium player dmg + enemy Hit/Club/Cleave/Shiv/Nip  
- [x] Bust coverage ~72% (60–80 lock); Medium 1.3×; peak ~200ms; ≤500ms @1x  
- [x] Contact hit-flash on cut; Wake + Brace frozen  
- [x] `fx_slash_light` packaged in debug APK (verify aapt/unzip)  
- [x] No tag / no gh release this tip  
