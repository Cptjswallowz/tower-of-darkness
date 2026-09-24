# Volume polish — v0.1.20-volume (WO Elliott)

Status: **implemented** (Compose VolumeFrame + optional PNG fold darkening). Shading pass only on existing flat art.  
**Same art** — no restyle of glyph shapes, no new silhouettes, no 3D engine / perspective models.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

---

## Intent

Add a little **volume** so You, Ash-Warden, skill tiles, and the Ashbrand icon read less flat: soft drop shadow, one gold-ash rim light, and a darker fold on cloak / seal where those forms already exist.

---

## Targets (this pass)

| Asset | Polish |
|-------|--------|
| **You** (player portrait) | Soft drop shadow; one **gold-ash** rim (top-left); darker **cloak** fold |
| **Ash-Warden** (F2 boss) | Soft drop shadow; one gold-ash rim (top-left); darker **seal** / body fold |
| **Skill tiles** (glyph tiles) | Soft drop shadow + one gold-ash top-left rim on the tile / glyph plate |
| **Ashbrand icon** | Soft drop shadow + one gold-ash top-left rim (blade icon stays; no second glyph) |

Rim = single top-left edge highlight in gold-ash (not a full outline ring). Shadow = soft, shallow drop — not a hard cutout.

---

## Out of scope / stay as-is

- **Seal-Warden** (F1) + **trash** — placeholders; no volume pass this WO
- Glyph **shapes** from `glyphs-v0119.md` — do not redraw or swap motifs
- Bodies lock from `bodies-v0118.md` — no wardrobe / poses / hit-flash
- Wake art / Spark / crescent / speck freeze (`wake-speck-v0117.md` and prior)
- Path, 2x, save, combat math, pips logic

---

## Optional (Art)

**Vow Plate** plate-glyph PNG swap — allowed if Art drops a better plate glyph under the same drawable name / slot. Not required for this lock; no job/tint change.

---

## Frozen

Wake art, Spark, bodies silhouettes, glyph map, 2x, save, path, combat math, status pips.

---

## Checklist

- [x] You: soft shadow + gold-ash top-left rim + darker cloak fold
- [x] Ash-Warden: soft shadow + gold-ash top-left rim + darker seal/fold
- [x] Non-Ashbrand skill tiles: soft shadow + gold-ash top-left rim
- [x] Ashbrand icon: soft shadow + gold-ash top-left rim (blade only)
- [x] Seal-Warden + trash unchanged (placeholders)
- [x] No glyph shape restyle; no 3D / perspective models
- [x] Optional: Vow Plate plate-glyph PNG swap only if Art ships it


---

## Implementation (Android + Art)

- **Art bake is SoT:** `tools/gen_volume_v0120.py` — soft shadow + gold-ash TL rim + 2-tone folds on You, Ash-Warden, 11 glyphs, Ashbrand icon (Wake/spark frozen).
- Optional Vow Plate: heavy plate / cuirass clarify under same `glyph_vow_plate.png` (distinct from Iron Mantle kite).
- Compose: `VolumeFrame` / `Modifier.volumeChrome` is **no-op** (no double shadow/rim on Art bake; no layout shift).
- Wired markers: `HeroShowcase` (You), `EnemySilhouette` (Ash-Warden only), `SkillGlyphIcon`, `AshbrandIcon` (blade only; not spark).
- Dimmed glyphs: greyscale @ `VolumeArt.DIMMED_GLYPH_ALPHA` (readable, no 0.35 crush).
- Seal-Warden / trash stay Canvas placeholders (no volume).
- Tests: `VolumeArtV0120Test`.
