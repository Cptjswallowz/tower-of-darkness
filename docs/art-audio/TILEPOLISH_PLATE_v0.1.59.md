# Tile polish — shared UI plate — v0.1.59-tilepolish PART B

**WO:** v0.1.59-tilepolish PART B  
**Audience:** Android Engineer (wire); Art (this deliverable)  
**Fidelity:** **SOURCE** crop from `floor_backdrop.jpg` — not PH invent  
**Tip:** `e262249fcf5b91d361ba205402541dbcaae4e591` (`git rev-parse HEAD` at deliver)

## Goal

One reusable dark painterly **UI plate** that sits **UNDER** icon / name / weight / pip / numbers for:

- Combat skill tiles (5)
- Ashbrand weapon plate
- Forge rows

Floor-map **NODE discs** may use the same plate at **lower opacity** (Engineer — e.g. ~40% of skill-slot opacity). Art does **not** edit Compose.

## Hard locks

- **LOCKED:** remnants / Forge costs / relic effects / combat FX / SFX.
- Do **NOT** touch CLEAVE / PLUME / Kenney FX assets.
- **ONE plate only** — do **not** invent 5 unique rarity scenes. Recolor-per-rarity is **Engineer border tint only**.
- No faces, no competing weapons in the plate art.
- No white fog / bright ember wash behind type.

## Drawable

| Field | Value |
| --- | --- |
| Path | `app/src/main/res/drawable/ui_tile_plate.png` |
| Mirror | `assets/ui/ui_tile_plate.png` (identical) |
| Size | **512×256** RGBA landscape (stretchable for ~72dp skill slots, Forge rows, Ashbrand bar) |
| Bytes | **241892** |
| MD5 | **20dc01b0e3b934c2bb08c8b26cf77524** |
| Edge | Soft rounded-rect falloff baked into alpha (not a hard rectangle) |

## Source & processing

1. **Crop** lower cracked-stone region of `app/src/main/res/drawable/floor_backdrop.jpg` (784×1168) — box ≈ `(78,911)–(705,1144)`. Prefer soot / dark iron; avoid amber window glows.
2. Resize **512×256** LANCZOS; slight blur + grain for parchment feel.
3. Charcoal bias: desaturate, crush highs, center luminance ≈ **29** (target 20–45).
4. Alpha: center **α ≈ 0.35** (within **25–40%**); smooth rounded-rect falloff → **0** at edges (~15% margin). Soft corners.
5. Straight RGBA PNG (not muddy premultiplied halo).

**Measured center alpha:** **0.350**  
**Edge alpha:** ≈ 0 at L/R/T/B.

## Opacity notes

- Skill / Ashbrand / Forge: use plate as-is (center 25–40%, edge → 0).
- NODE discs: Engineer applies same drawable at **lower opacity** (preview uses ~40% of plate alpha ≈ 0.14 center). Optional circular crop/mask in Compose.

## Usage (Engineer wires)

- Plate under glyph + Bone name + `wN` + roman/pip indicators.
- Do **not** invent per-rarity plate scenes — border tint only.
- Art does **not** edit Compose / Kotlin.

## Previews (stage)

Staged under `/workspace/tod-tilepolish-v0159/`:

| File | Purpose |
| --- | --- |
| `ui_tile_plate.png` | Canonical plate copy |
| `preview_skill_slot.png` | Combat SkillSlot mock — glyph `glyph_dust_veil` + "Dust Veil" + `w2` + II pips on VoidBg; name/weight/pips readable |
| `preview_forge_row.png` | Wider Forge row — "Iron Mantle" + scrap numbers readable over plate |
| `preview_node_disc.png` | Circular crop of same plate at ~40% skill opacity |

## Repro script

`tools/prep_tilepolish_plate_v0159.py`  
Run: `/tmp/artvenv/bin/python tools/prep_tilepolish_plate_v0159.py`

## MANIFEST

```
path=app/src/main/res/drawable/ui_tile_plate.png
bytes=241892
md5=20dc01b0e3b934c2bb08c8b26cf77524
center_alpha=0.350
center_lum≈29.4
tip=e262249fcf5b91d361ba205402541dbcaae4e591
```
