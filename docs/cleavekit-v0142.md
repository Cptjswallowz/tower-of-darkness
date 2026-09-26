# Combat FX — v0.1.42-cleavekit (WO Elliott / Engineer)

Status: **implemented** (CLEAVE free-sampler slash-light + hit-flash into existing FX kernel).  
**Do not** invent new overlay architecture. **Do not** tag / gh release here.

**Aim stays v0.1.40-fxfix:** target bust only / half-stage clip / no screen flash / tamed glow / Brace Shield stamps.  
Art stage: `/workspace/tod-cleave-v0142/` · Art notes: `docs/art-audio/CLEAVE_v0.1.42.md`.

**Frozen:** F3 graph/trolls/loadout/Gate-Warden/Hub; F1/F2 flow; Wake art/timing/math; Brace floating pips; damage weights; new skills/Hub; full-row plate; **do not** import all 48 CLEAVE effects / shield-block.

---

## Wire map (order: tile flash → stroke on TARGET bust → number float → shake → log)

| Beat | Sheet | Where | Notes |
|------|-------|-------|-------|
| **Player Small** | `fx_slash_light` gold/ember | **Foe bust only** | Hostflint, Hit-class player SMALL |
| **Enemy Small** | `fx_slash_light` dirty green/rust | **You bust only** | shiv / nip / hit / **club** / **gate_pulse** |
| **Player Medium** | `fx_slash_light` **1.2×** gold | Foe only | Tower Pike, Ruin Seal, Spark Tithe, Wake Echo |
| **Enemy Medium** | `fx_slash_light` 1.2× role tint | You only | cleave / seal_pulse / coal_slam (existing Medium path) |
| **Sparks (Ashbrand)** | `fx_hit_flash` (+ additive) ember | **Hit target (foe)** | Not full-screen; icon ember (`ashbrand_spark`) unchanged |
| **Wake** | existing `wake_vfx_*` | Foe (0.58 clip) | Do **not** replace |
| **Brace** | existing floating Shield pips | Owner bust | **Not** shield-block |

Kernel order unchanged: tile flash → stroke \| brace-pips \| hit-flash → float → shake → log.

---

## Club / Gate Pulse

In **0.1.41** Club / Gate Pulse fail-soft **SMALL** (not in `ENEMY_MEDIUM`).  
**0.1.42 choice:** map both into **`ENEMY_SMALL`** → Enemy Small slash-light (WO prefer Small; kernel already had a clean Enemy Medium path for cleave/seal_pulse/coal_slam — Club/Gate Pulse stay Small).

---

## Playback locks

| Rule | Lock |
|------|------|
| Slash frames | Peak window every-other `4,6,8,10` (peak 6 in window) |
| Slash 1x | ≤ **500 ms** (`STROKE_SMALL_MS=400` / Medium 480) |
| Slash 2x | Half via `CombatFx.fxHoldMs` |
| Hit-flash | First **8** frames then fade; `HIT_FLASH_MS=320` @1x / 160 @2x |
| Input | Never blocks; existing skip / 2x still apply |
| Missing sheet | Fail-soft to prior Canvas path stroke (slash) / no-op (hit-flash) |

Atlas: row-major, cell 256. Drawables: `fx_slash_light`, `fx_hit_flash`, `fx_hit_flash_additive`.

---

## DO NOT

- New player skills / Hub / F3 graph or kit number changes  
- Full-row plate / import all 48 CLEAVE effects / **shield-block**  
- Replace Brace pips or Wake crescent / ashbrand_spark icon  
- Change damage weights / path gen / loadout lock / Gate-Warden kit numbers  
- Replace kernel architecture (tiers / playSpec / half-stage clip stay)

---

## Files

| File | Role |
|------|------|
| `domain/combat/CleaveKit.kt` | Atlas metadata, play frames, scale, cell origin |
| `domain/combat/CombatFx.kt` | TAG `v0.1.42-cleavekit`; `ENEMY_SMALL` + club/gate_pulse; `ID_ASHBRAND_SPARK` / `specForSpark` |
| `domain/combat/CombatEngine.kt` | Spark event `fxId = ashbrand_spark` |
| `ui/components/CombatFxOverlay.kt` | CLEAVE slash atlas + hit-flash overlay; Brace unchanged |
| `ui/screens/CombatScreen.kt` | Drive hit-flash hold; pass stroke `holdMs` |
| `res/drawable/fx_slash_light.png` | md5 `e02410214790304f8d2e408472cc6cc0` |
| `res/drawable/fx_hit_flash.png` | md5 `610cf76722f5f0ea852d60b85e2be8a5` |
| `res/drawable/fx_hit_flash_additive.png` | md5 `11635e0b56822407975eab4d884bb491` |
| `CleaveKitV0142Test.kt` | Wire / aim / Club / spark / Wake / 2x |
| `docs/cleavekit-v0142.md` | This lock |
| `docs/art-audio/CLEAVE_v0.1.42.md` | Art stage notes |

Prior FX tests stay green (TAG + `ENEMY_SMALL` asserts advanced).

---

## Engineer checklist

- [x] Only slash-light + hit-flash (+ additive) imported; **no** shield-block  
- [x] Player Small / Enemy Small / Player Medium 1.2× on recipient bust; half-stage clip kept  
- [x] Ashbrand spark → gold hit-flash on foe (not full-screen); Wake + Brace unchanged  
- [x] Club + Gate Pulse → Enemy Small slash-light  
- [x] 1x slash ≤500ms; 2x halves; fail-soft if sheet missing  
- [x] F1/F2/F3 flow / damage / Hub / loadout frozen  
- [x] No tag / no gh release this tip  
