# Combat FX garnish — v0.1.49-plumegarnish

Status: **implemented** (PLUME curated + Kenney curated from `assets/fx/`).  
**Do not** tag / gh release here. Stroke **LOCKED** (v0.1.48 crescent / widths / peaks / colors).

## Asset sources (repo only)

| Path | Role |
|------|------|
| `assets/fx/kenney/` | spark_01–07, circle_01–05, smoke_01–10, flare_01 (CC0) |
| `assets/fx/plume/` | sand-kick, footstep-puff, thin-wisp, grinder-sparks (±additive), ground-fog |

Prep: `tools/prep_plumegarnish_v0149.py` → `app/src/main/res/drawable/fx_{kenney,plume}_*.png`.  
**Never** commit `/workspace/tod-particle-packs` or zip archives.  
**Never** wire `ground-fog` as fullscreen fog wall / both-bust FX.

## Sheet → skill map

| Ability / event | Pack | Sheet | Where | Scale | Hold @1x |
|-----------------|------|-------|-------|-------|----------|
| Ashbrand SPARK + Wake tip | **PLUME** additive | `grinder-sparks` | tip | 25% bust | 250ms |
| Optional hit-confirm tip | Kenney | `spark_01`–`07` rust-gold | tip | 25% bust | 250ms |
| Dust Veil / Ash Press | **PLUME** | `sand-kick` soot | **TARGET bust only** | 50% bust | 400ms |
| Cinder Step | **PLUME** | `footstep-puff` | TARGET bust | 50% bust | 400ms |
| Optional trail | PLUME | `thin-wisp` | TARGET (not fog wall) | ≤50% | 400ms |
| Soften (status active) | Kenney | `circle_03` dirty green | under foe HP | ~12% | while active |
| Brace **GAIN** only | Kenney | `flare_01` | owner | ~14% | 280ms |

Brace floating pips **kept** — flare does not replace them.

## Debug (`TodFx` + combat log)

- `FX stroke on <target>` — locked stroke
- `FX plume <name> on <target>` — e.g. `FX plume grinder-sparks on Goblin`
- `FX kenney <sprite> on <target>` — e.g. `FX kenney circle_03 on foe`, `FX kenney flare_01 on You`

## Domain / UI

- `PlumeGarnishKit` — maps, timings, atlas helpers, debug strings
- `CombatParticleGarnishOverlay` — shared `drawImage` engine with CLEAVE
- Stroke path / width / peaks / colors untouched

## Tests

`PlumeGarnishV0149Test` — map coverage, allowlist (no banned Kenney hits), timings, Soften-under-foe, Brace-flare≠pips, debug formats, stroke lock, no ground-fog dust, atlas cells.
