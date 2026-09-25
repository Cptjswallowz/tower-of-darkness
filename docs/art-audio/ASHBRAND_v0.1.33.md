# Ashbrand icon — v0.1.33 (Art & Audio)

**Fidelity:** PLACEHOLDER (sheet-crop; Elliott still knockout — no redraw)  
**WO:** v0.1.33-ashbrand  
**Owner:** Art & Audio (assets) · Engineer (wire — drop-in same `R.drawable.ashbrand_icon`)  
**Do not invent** paint, plate, or VFX — crop + rembg knockout only.

---

## Source

| Item | Value |
|------|--------|
| Still | `/workspace/tod-ashbrand-v0133/ashbrand_portrait.jpg` |
| md5 | `846af065acf75d5f08a08ca0bfd4173a` |
| Size / mode | 1408×1408 RGB |
| Motif | Diagonal painted sword: tip top-right, pommel bottom-left; dark charcoal bg with floating gold ember sparks (bg FX) |

---

## Deliverable

| Item | Value |
|------|--------|
| Drawable | `app/src/main/res/drawable/ashbrand_icon.png` → `R.drawable.ashbrand_icon` |
| Stage copy | `/workspace/tod-ashbrand-v0133/ashbrand_icon.png` (same bytes) |
| Size | **192×192 RGBA** transparent |
| Treatment | rembg knock-out dark bg; drop disconnected ember sparks (not part of solid metal silhouette); tight opaque bbox → ~10% pad → centered on transparent canvas. **No** plate / square / circle fill. Soft ~1px edge OK. Gold fuller/highlights kept opaque. |
| Compose | ContentScale.Fit ~48dp Loadout weapon slot + combat Ashbrand tile — pad keeps tip/pommel from clipping |

Generator: `tools/prep_ashbrand_v0133.py` (idempotent from staged jpg).  
Optional preview: `/workspace/tod-ashbrand-v0133/_preview.png` (icon on checkerboard).

---

## Frozen (do not touch)

Wake crescent / spark VFX and unrelated portraits/glyphs — sha256 must stay unchanged:

| File | sha256 |
|------|--------|
| `ashbrand_spark.png` | `2a259d97469fe045750729bb6bf93ce88a1d45c492be5b9e92582b5cbdc8d283` |
| `wake_vfx_charge.png` | `3c3ffbf49aa27b68b80216098fe020f14fcf5a0d834fa935aca3460728b2ae34` |
| `wake_vfx_slash.png` | `aa5f0732414abe0bd32292d86f8078f9686e3c64f7800f4c9bc58d467337d325` |
| `wake_vfx_impact.png` | `f081ef30e84c6bf56aec7d11bb28c157ce1cfb3e16cb23352ba7c32106865086` |
| `glyph_hostflint.png` | `763a18b4ccd239edad85343f31587e12d5b0f3601972b3504823b398cba150bd` |
| `portrait_you.png` | `cd1d2cdbd062734492380a005288e24797fdf5cde310ea9fbccd0b002e533019` |

Also: **no Kotlin edits** this WO.

---

## Engineer wire hint

Drop-in replace of existing `ashbrand_icon` only — same resource id for Loadout weapon slot and combat Ashbrand tile. No new drawable names. Old dark-square plate language removed; new asset is blade-only on transparent alpha.
