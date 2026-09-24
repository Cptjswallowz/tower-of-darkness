# Wake art — v0.1.17-wakespeck (Art note)

**Fidelity:** PLACEHOLDER speck polish on frozen 0.1.16 crescent/icon  
**Tag:** v0.1.17-wakespeck  
**Audience:** Android Engineer (via Chief of Staff)

## What changed
Full Wake overlay only — **no** stroke restyle, **no** icon redraw, **no** Spark change, **no** timing change, **no** extra frames beyond dots/burst on existing three.

| File | Change |
| --- | --- |
| `wake_vfx_charge.png` | +8–12 gold/ash dots along forming arc |
| `wake_vfx_slash.png` | +8–12 gold/ash dots staggered along crescent (not a second arc) |
| `wake_vfx_impact.png` | trail motes + **one** ~8–12px gold impact burst on enemy side (~72% x); 1-frame hold |
| `ashbrand_icon.png` | **untouched** (blade freeze) |
| `ashbrand_spark.png` | **untouched** (half charge: no crescent / dots / burst) |

Paths: `app/src/main/res/drawable/` (same drop-in names).

## Play order (unchanged)
1. `wake_vfx_charge` → 2. `wake_vfx_slash` → 3. `wake_vfx_impact`  
Impact burst is the enemy-body hit read; hold that frame one beat with existing Wake hold math (do not retune).

## Safe margins (unchanged)
Top ≥36 px, bottom ≥48 px (skill row), sides ≥40 px. Transparent overlay; do not redraw bodies.

## Out of scope
Wake math / Spark rules / 2x / save / pips / path / SFX / new frame packs / icon restyle.
