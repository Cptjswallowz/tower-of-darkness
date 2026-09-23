# Wake icon polish — v0.1.16-wakeicon (Art / Engineer note)
**Fidelity:** Engineer placeholder bitmaps (polish lock from Elliott `docs/wake-icon-v0116.md`)  
**Tag intent:** v0.1.16-wakeicon (no release tag this pass)  
**Audience:** Android Engineer  
**Frozen elsewhere:** Wake math, thresh, 2x holds, save, pips, path — do not touch.

After this polish ships: **freeze Wake art** unless Elliott reopens.

## Assets (same names — Art drop-in)

All under `app/src/main/res/drawable/`:

| File | Role | Size |
| --- | --- | --- |
| `ashbrand_icon.png` | Cracked ash-iron + gold fuller on **dark square**; ~48 dp readable | 192×192 |
| `ashbrand_spark.png` | SPARK ember on icon only (no crescent) — unchanged intent | 192×192 |
| `wake_vfx_charge.png` | FULL Wake — forming thick gold crescent + ash motes | 512×288 |
| `wake_vfx_slash.png` | FULL Wake — **2–3× thicker** gold stroke + **8–12 ash dots** on arc | 512×288 |
| `wake_vfx_impact.png` | FULL Wake — **one** small spark burst (not a second slash); residual tip only | 512×288 |

Compose still uses `R.drawable.ashbrand_icon`, `ashbrand_spark`, `wake_vfx_charge`, `wake_vfx_slash`, `wake_vfx_impact` — replace PNGs in place to drop in Art finals.

## Polish lock (summary)
- Stroke 2–3× thicker than thin stub; 8–12 ash dots on slash arc
- Impact = one small burst at end of sweep (enemy side ~72%)
- Clear of skill row (bottom margin); same beat/holds as v0.1.15
- SPARK ember-only; no crescent

## Out of scope
- New SFX / extra frames / beat changes
- BLOCK/FINAL polish; body redraws
