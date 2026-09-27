# Hubsplit portrait overlays — v0.1.55 PART A (Art)

**Fidelity:** SOURCE STILLS (Elliott) — luminance key / crop / place only. **Not** redrawn PH.
**WO:** v0.1.55-hubsplit (Elliott refs under `/workspace/tod-trollkept-v0154/refs/`)
**Locks:** Combat FX/SFX untouched. **`portrait_you` not redrawn** (md5 `ed5064c12745ad7f149f9a313b673819`).

Hub + title `HeroShowcase` only — not combat, not enemy busts.

## Fix vs v0.1.54

- **`overlay_soot_rim`:** was a thick charcoal/ember **annulus wreath** (~39–52% coverage). Now **sparse warm ember SPECKs** along the existing bust silhouette edge inside the crop (~1–3% coverage). Soot ref `07_isolated_soot_ring.jpg` used **only** for ember colors/texture — **not** as a full ring border.
- **`overlay_ash_pauldron`:** enlarged; viewer-left shoulder ~x=35–120, y=85–185.
- **`overlay_troll_tooth`:** enlarged; lower-right cloak ~x=155–230, y=140–235.
- **`overlay_gate_sigil`:** moved up to chest/gorget (~cx=128, cy≈165); circular portcullis grate only (fabric band cropped); ~90–110px wide so it reads as medallion, not hem.

## Files (256×256 RGBA) — identical in assets + drawable

| File | bytes | md5 | Content bbox | coverage |
|------|------:|-----|--------------|---------:|
| `assets/portraits/overlay_soot_rim.png` (+ drawable) | 7172 | `6d9431fac60ace69e7d1cefd06055655` | `(41, 17, 213, 240)` | 1.66% |
| `assets/portraits/overlay_ash_pauldron.png` (+ drawable) | 11678 | `6a9575f95b9ec0d0cfb99dfd43e890b2` | `(58, 93, 110, 182)` | 3.52% |
| `assets/portraits/overlay_troll_tooth.png` (+ drawable) | 9409 | `aa53bba8de86e3f089bfcab5764243ea` | `(162, 138, 206, 209)` | 2.53% |
| `assets/portraits/overlay_gate_sigil.png` (+ drawable) | 10969 | `f404372aa68e583fb792d76fdd328e8c` | `(92, 140, 163, 209)` | 3.46% |

## Stack order (bottom → top)

1. `overlay_soot_rim` — sparse edge ember specks
2. `overlay_ash_pauldron` — left cracked pauldron
3. `overlay_troll_tooth` — lower-right ivory tooth
4. `overlay_gate_sigil` — chest/gorget portcullis medallion

## Placement (viewer coords on 256 canvas)

| Overlay | Anchor / region | Notes |
|---------|-----------------|-------|
| soot_rim | silhouette edge band ~3–10px inside bust | ember pixels from soot ref palette; NOT a ring |
| ash_pauldron | cx≈72, cy≈132; fit ~92×108 | glowing crack must read at Hub 120–160 |
| troll_tooth | cx≈192, cy≈188; fit ~78×100 | ivory hangs on cloak |
| gate_sigil | cx=128, cy≈165; fit ~104×104 | circular grate, fabric band removed |

## Circular clip

All overlays multiplied by `portrait_you` soft alpha (slight MinFilter) so nothing sticks outside the bust crop. Frame-aligned with content bbox ~`(41,18)–(214,238)`.

## Source → overlay

| Overlay | Elliott still | Bust placement truth |
|---------|---------------|----------------------|
| soot_rim | `07_isolated_soot_ring.jpg` (ember colors only) | silhouette of `portrait_you` |
| ash_pauldron | `08_isolated_pauldron.jpg` | `03_bust_pauldron.jpg` |
| troll_tooth | `06_isolated_tooth.jpg` | `02_bust_tooth.jpg` |
| gate_sigil | `05_isolated_grate_band.jpg` (circular grate) | `01_bust_gate_medallion.jpg` |

Script: `tools/prep_hubsplit_overlays_v0155.py`. Stage: `/workspace/tod-hubsplit-v0155/` (`preview_you_full.png`, `preview_hub_120.png`, `preview_hub_160.png`).

No glow/pulse/colored frames. Real RGBA alpha; black keyed carefully for dark metal/cloak edges.
