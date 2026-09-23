# Wake art — v0.1.16-wakeicon (Art note)

**Fidelity:** PLACEHOLDER polish / design freeze Wake art  
**Tag:** v0.1.16-wakeicon  
**Scope:** Presentation art only; Wake math, thresholds, timing, save, pips, path, and combat logic remain frozen.

## Frozen asset set

All files are under `app/src/main/res/drawable/` and are still-frame PNG overlays:

| File | Role | Size |
| --- | --- | --- |
| `ashbrand_icon.png` | Ashbrand slot icon: bold cracked ash-iron longsword, bright gold fuller seam, dark card; no hall or hand | 192×192 RGBA |
| `ashbrand_spark.png` | Existing quiet half-charge ember overlay on the icon only; **unchanged** | 192×192 RGBA |
| `wake_vfx_charge.png` | Wake frame 1: forming gold crescent plus ash motes | 512×288 RGBA |
| `wake_vfx_slash.png` | Wake frame 2: one bold gold crescent slash plus ash motes | 512×288 RGBA |
| `wake_vfx_impact.png` | Wake frame 3: thinner trail, one small enemy-side spark burst, impact ash cluster | 512×288 RGBA |

## Frame order and placement

Play the full Wake chain once, in this order:

1. `wake_vfx_charge` — crescent forms left-to-right.
2. `wake_vfx_slash` — one broad gold crescent sweep.
3. `wake_vfx_impact` — one compact gold burst near x≈72% (enemy side), with ash clustered only at impact.

The frames are **transparent overlays only**. Do not add hero/enemy bodies, portraits, hall backgrounds, black fills, or a second slash. Keep the existing combat portrait stage and existing hold/rate math.

Baked safe margins are preserved: content is inside approximately **top ≥36 px, bottom ≥48 px, sides ≥40 px**. Charge/slash core strokes are approximately 14–18 px; impact trail is approximately 10–12 px. Each crescent carries 8–12 grey/brown ash motes along its arc; impact confines the motes to its burst.

## Icon polish

The icon is deliberately bold at ~48dp: dark charcoal rounded card, thick ash-grey blade edges, three readable blade fractures maximum, continuous 6–8 px gold fuller seam, and an optional small ember-orange pommel. It contains no hand or hall. `ashbrand_spark.png` remains the quiet SPARK-only overlay: no crescent.

## Out of scope

- Wake math, CHAIN/SPARK rules, damage, timing, or 2× behavior
- BLOCK/FINAL polish, status pips, Brace draw sync, or common skill bodies
- ToW/Eldermark assets
- Video/Lottie, extra frames, or new SFX
