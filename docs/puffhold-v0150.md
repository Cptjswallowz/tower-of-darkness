# Combat FX puff hold — v0.1.50-puffhold

Status: **implemented** (P2 polish on 0.1.49 garnish).  
**Do not** tag / gh release until CoS green after QA. Stroke **LOCKED**.

## Problem

PLUME / Kenney logs fired but phone stills were a **1-frame yellow smear**. Tip spark hold was ~250ms and cleared with the short Ashbrand spark stroke (~280ms). Peak atlas frames did not dwell.

## Fix

| Item | v0.1.49 | v0.1.50-puffhold |
|------|---------|------------------|
| Dust puff hold @1x | 350–450ms (400) | **400–500ms (450)** |
| Tip spark / ember hold @1x | 200–300ms (250) | **400–500ms (450)** |
| Dust Veil / Ash Press sheet | sand-kick | **ash-puff** (`fx_plume_ash_puff`) |
| Cinder Step land | footstep-puff | **footstep-puff** (unchanged) |
| Ashbrand tip / Wake tip | grinder-sparks rust-gold 25% | same; hold in 400–500 band |
| Soften | circle_03 under foe while >0 | same (steady mid plateau) |
| Brace GAIN | flare_01 pulse; pips kept | same |
| Stroke / Wake gold arc | LOCKED | **untouched** |

Overlay keeps tip/dust visible for `max(strokeHold, garnishHold)` so short spark strokes do not cut the still. `atlasPlayFrames` dwells on peak.

## Sheet map

| Ability / event | Sheet | Where | Hold @1x |
|-----------------|-------|-------|----------|
| Dust Veil / Ash Press | `ash-puff` | TARGET bust only | 450ms |
| Cinder Step | `footstep-puff` | TARGET bust | 450ms |
| Ashbrand SPARK + Wake tip | `grinder-sparks` additive | tip 20–30% bust | 450ms |
| Soften | Kenney `circle_03` | under foe HP | while Soften > 0 |
| Brace GAIN | Kenney `flare_01` | owner | one pulse |

Never ground-fog / both-bust / fog wall. sand-kick remains on disk unused as primary dust.

## Art

- `docs/art-audio/PLUME_ASH_PUFF_v0.1.50.md` — drawable md5 `6b60e853…`
- Curated sheets under `assets/fx/plume/sheets_{rgba,additive}/` (commit ash-puff + delta; no zips)

## Domain / UI

- `PlumeGarnishKit` — TAG `v0.1.50-puffhold`
- `CombatParticleGarnishOverlay` + `CombatScreen` hold wiring
- Stroke path / width / peaks / colors untouched

## Tests

`PuffholdV0150Test` — map, 400–500 holds, peak dwell, Soften/Brace, stroke lock, ash-puff drawable, debug.
