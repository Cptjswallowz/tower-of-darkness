# Volume pass — v0.1.20-volume (Art note)

**Fidelity:** PLACEHOLDER  
**Tag:** v0.1.20-volume  
**Audience:** Android Engineer (via Chief of Staff)  
**Generator:** `tools/gen_volume_v0120.py` (`/tmp/artvenv/bin/python`)

Same drawable names. No Kotlin / Compose / combat-math changes. Silhouettes and glyph motifs preserved (soldier / Ash-Warden identity unchanged).

## What changed

Same art, more volume:

1. **Soft drop shadow** — dark offset under each figure (down-right), Gaussian-soft, clipped to stay outside the opaque fill so folds stay clean.
2. **Gold-ash rim light** — one soft top-left rim in gold-ash `#C9A227` / bone `#E6D7B8` along the silhouette edge (stronger on TL, fades toward BR).
3. **2-tone folds** — slightly darker recesses on cloaks, seals, plate halves, flame/shaft shade sides so shapes read as **two tones**, not one flat fill.

Circular alpha preserved on portraits (You 256×256, Ash-Warden 320×320). Glyphs stay 96×96. Ashbrand icon stays 192×192.

## Assets (overwritten in place)

| Path | Notes |
| --- | --- |
| `app/src/main/res/drawable/portrait_you.png` | Soldier + volume; circular |
| `app/src/main/res/drawable/portrait_ash_warden.png` | Coal/ember boss + volume; circular |
| `app/src/main/res/drawable/glyph_hostflint.png` | flint / spark |
| `app/src/main/res/drawable/glyph_emberbrand.png` | flame |
| `app/src/main/res/drawable/glyph_tower_pike.png` | spear |
| `app/src/main/res/drawable/glyph_ruin_seal.png` | cracked seal |
| `app/src/main/res/drawable/glyph_shadow_latch.png` | hook |
| `app/src/main/res/drawable/glyph_cinder_step.png` | boot + spark |
| `app/src/main/res/drawable/glyph_iron_mantle.png` | kite shield (Brace green) |
| `app/src/main/res/drawable/glyph_vow_plate.png` | **heavy plate / cuirass** (Brace green) — motif clarified |
| `app/src/main/res/drawable/glyph_dust_veil.png` | cloak |
| `app/src/main/res/drawable/glyph_ash_press.png` | hammer + plus |
| `app/src/main/res/drawable/glyph_relic_shard.png` | shard + plus |
| `app/src/main/res/drawable/ashbrand_icon.png` | blade card + volume; size unchanged |

## Vow Plate motif note

`glyph_vow_plate.png` was clarified from a second-shield-adjacent silhouette to a clearer **heavy plate / breastplate**: squared torso with neck scoop, pauldrons, waist flange, rivets, center ridge + gold vow chevron. Still green brace tint, still 96×96. Iron Mantle remains the kite shield.

## Frozen (untouched — hashes verified)

| File | md5 |
| --- | --- |
| `wake_vfx_charge.png` | `699ec4bf4539ed2e09922e5870d34c35` |
| `wake_vfx_slash.png` | `0ae68a58e7f3a2034cdfbd2a206ca85b` |
| `wake_vfx_impact.png` | `8caf0a1c282f99e0ce2937a6aba82c17` |
| `ashbrand_spark.png` | `36a10d0d431c029aea23b08bf8e25cfc` |

Also frozen this pass: Seal-Warden / trash enemies, Kotlin, WO design locks (bodies/glyphs docs), Wake crescent overlays.

## Reproduce

```bash
/tmp/artvenv/bin/python tools/gen_volume_v0120.py
```

## Out of scope

- Final / higher-fidelity art
- Poses, 3D, wardrobe, hit-flash
- Wake VFX restyle, Spark, Compose wiring


## Android wiring

Compose `volumeChrome` / `VolumeFrame` is a **no-op** marker (Art bake already has shadow/rim — do not double). Wired on You, Ash-Warden, skill tiles, Ashbrand icon (not Wake VFX / spark). Dimmed glyphs stay readable via greyscale + `VolumeArt.DIMMED_GLYPH_ALPHA`. See `docs/volume-v0120.md`.
