# Cave Troll / Floor 3 cave / Gate-Warden — v0.1.41-floor3 (Art & Audio)

**Fidelity:** PLACEHOLDER / still-crop (Elliott stills — rembg knockout + circular slot only; **no redraw**).  
**WO:** v0.1.41-floor3  
**Owner:** Art & Audio (assets) · Engineer (wire / equal 1/3 roll)  
**Script:** `tools/prep_cave_trolls_v0141.py` (idempotent)  
**Stage:** `/workspace/tod-floor3-v0141/`

Does **not** edit Kotlin. Does **not** touch FX / Wake / Climb / ashbrand / glyphs / existing pack portraits / node tokens / `floor_backdrop.jpg` / `portrait_seal_warden.png` source.

---

## 1. Cave Troll combat portraits

Sources (md5 verified):

| Source | md5 | Size |
|--------|-----|------|
| `/workspace/tod-floor3-v0141/cave_troll_a.jpg` | `ba655398c74018d36359462942a51958` | 1408×1408 |
| `/workspace/tod-floor3-v0141/cave_troll_b.jpg` | `9cb83286173ed0c373e963eda73499fa` | 1408×1408 |
| `/workspace/tod-floor3-v0141/cave_troll_c.jpg` | `f6fc4cfbd2d96236c2f27dc103553fe7` | 1408×1408 |

Pipeline (same language as `tools/prep_packs_v0126.py` / `prep_portraits_v0121.py`):

1. rembg knockout solid teal bg (u2net; stills thumbnail to ≤768 before knockout).
2. Tight subject bbox → square → **256×256** RGBA circular (sturdy-orc trash bust language; inset ~7%).
3. Corners alpha 0; no gold/plate frames.

| Variant | Drawable | Stage copy |
|---------|----------|------------|
| Cave Troll A | `app/src/main/res/drawable/portrait_cave_troll_a.png` | `tod-floor3-v0141/portrait_cave_troll_a.png` |
| Cave Troll B | `app/src/main/res/drawable/portrait_cave_troll_b.png` | `tod-floor3-v0141/portrait_cave_troll_b.png` |
| Cave Troll C | `app/src/main/res/drawable/portrait_cave_troll_c.png` | `tod-floor3-v0141/portrait_cave_troll_c.png` |

**Engineer:** when a Cave Troll rolls, pick A/B/C **equal 1/3** (Art does not edit Kotlin).

---

## 2. Floor 3 cave backdrop

| Item | Value |
|------|--------|
| Source | `app/src/main/res/drawable/floor_backdrop.jpg` (F1/F2 hall — **not overwritten**) |
| New drawable | `app/src/main/res/drawable/floor_backdrop_cave.jpg` |
| Stage | `/workspace/tod-floor3-v0141/floor_backdrop_cave.jpg` |
| Size | 784×1168 JPG |
| Treatment | Pull brightness, cool/desaturate slightly toward deep cave (teal/coal shift). |
| Tokens | **Reused unchanged** (`node_*`) — do not redraw. |

Wire hint: F3 → `floor_backdrop_cave`; F1/F2 keep `floor_backdrop`.

---

## 3. Gate-Warden (no new bust)

| Item | Value |
|------|--------|
| Source (read-only) | `portrait_seal_warden.png` (320×320) |
| Output | `app/src/main/res/drawable/portrait_gate_warden.png` (+ stage) |
| Treatment | Colder copper tint — shift warm seal-gold toward cooler copper / cyan-copper; silhouette kept. |
| Do not modify | `portrait_seal_warden.png` source file |

---

## 4. Preview

Optional contact sheet: `/workspace/tod-floor3-v0141/_preview.png` (Cave Troll A/B/C + Gate-Warden).

---

## Frozen (do not touch this WO)

| Asset class | Examples |
|-------------|----------|
| Wake VFX | `wake_*` |
| Ashbrand | `ashbrand_*` |
| Climb | climb intro assets |
| Glyphs | `glyph_*` |
| Packs | `portrait_weak_goblin_*`, `portrait_sturdy_orc_*` |
| Boss / You | `portrait_seal_warden`, `portrait_ash_warden`, `portrait_you` |
| Node tokens | `node_*` |
| F1/F2 hall | `floor_backdrop.jpg` (original) |
| Kotlin | untouched by Art |

---

## Frozen sha256 (before == after)

| File | sha256 |
|------|--------|
| `wake_vfx_charge.png` | `3c3ffbf49aa27b68b80216098fe020f14fcf5a0d834fa935aca3460728b2ae34` |
| `ashbrand_spark.png` | `2a259d97469fe045750729bb6bf93ce88a1d45c492be5b9e92582b5cbdc8d283` |
| `portrait_seal_warden.png` | `d65aa410191940f1cdb06e0531d2eb2b87db161061461269f489cc3a6d286833` |
| `portrait_you.png` | `cd1d2cdbd062734492380a005288e24797fdf5cde310ea9fbccd0b002e533019` |
| `glyph_hostflint.png` | `763a18b4ccd239edad85343f31587e12d5b0f3601972b3504823b398cba150bd` |
| `node_start.png` | `2f5ae63d0b0d57f93b643715d7999aac6a8639a853d35526a4f59bfc2e9ae1e7` |
| `floor_backdrop.jpg` | `50ba0fb95540a89b983e3cd8f6ab852cdcff3192933f9a82b83373ad7c154574` |

---

## Checklist

- [x] Cave Troll A/B/C → `portrait_cave_troll_{a,b,c}.png` (256 circular RGBA, nobg)
- [x] `floor_backdrop_cave.jpg` (784×1168); F1/F2 `floor_backdrop.jpg` unchanged
- [x] `portrait_gate_warden.png` from Seal-Warden colder copper (source untouched)
- [x] Generator `tools/prep_cave_trolls_v0141.py`
- [x] Note `docs/art-audio/CAVE_TROLL_v0.1.41.md`
- [x] Frozen sha256 before == after
