# Hub skill glyphs — v0.1.27-hub (Art & Audio)

**Fidelity:** PLACEHOLDER  
**Tag:** v0.1.27-hub  
**WO lock:** `docs/hub-v0127.md` (Art slice: **two** skill glyphs only)  
**Generator:** `tools/gen_glyphs_hub_v0127.py` (`/tmp/artvenv/bin/python`)  
**Volume bake:** imports `soft_drop_shadow` + `gold_ash_rim` + `deepen_folds` from `tools/gen_volume_v0120.py` (glyph params: rim_w=2, ox=2, oy=3, blur=2)

Wake, ashbrand, existing glyphs, combat portraits, trash packs, plates, and Kotlin stay **frozen**. This pass only adds the two Hub-unlock skill drawables.

## Assets (drop-in drawables)

Transparent **96×96 RGBA PNGs** under `app/src/main/res/drawable/`. Pipeline matches `tools/gen_glyphs_v0119.py` (SCALE=4 → Lanczos, ~10% pad) then volume bake. UI reads them at **24–32 dp** (`SkillGlyph.GLYPH_SIZE_DP = 28`).

| Path | Skill id | Motif | Job tint (guess) | Size |
| --- | --- | --- | --- | --- |
| `app/src/main/res/drawable/glyph_cinder_vow.png` | `cinder_vow` | small vow-spark (ember/gold spark + tiny vow chevron) | Damage / ember (spark) | 96×96 → 24–32 dp |
| `app/src/main/res/drawable/glyph_grave_nail.png` | `grave_nail` | nail (steel/iron; bold head + shank) | Damage / steel | 96×96 → 24–32 dp |

**Not** brace-green — WO did not assign Brace tint; both read as damage motifs.

## Wire hint (Engineer — Art does **not** edit Kotlin)

| Skill id | Drawable base name |
| --- | --- |
| `cinder_vow` | `glyph_cinder_vow` |
| `grave_nail` | `glyph_grave_nail` |

Map via existing `SkillGlyph.drawableName(id)` / `R.drawable.glyph_*` once Architect/Engineer add the Hub-gated cards.

## Style

- Simple painterly: bold filled silhouettes, 3–5 colors max, transparent background
- Theme palette (same as v0.1.19): Ash `#2B2A28`, Bone `#E6D7B8`, Ember `#C45A2D`, Ember-hot `#E88C3C`, Steel `#6B7280`, Steel-dull `#4E545E`, Gold `#C9A227`, Coal `#1C1A18`
- Vow mark = small gold chevron (same language as `glyph_vow_plate`)
- ~10% padding; high-contrast so grey-out / exhaust still reads
- No text on glyphs

## Frozen (untouched this pass)

Prove with sha256 before→after unchanged:

| Asset | Role |
| --- | --- |
| `glyph_hostflint.png` | existing skill glyph sample |
| `portrait_you.png` | You combat portrait |
| `wake_vfx_charge.png` | Wake VFX |

Also do **not** touch:

- Other `glyph_*.png` (hostflint … relic_shard)
- Trash packs (`portrait_weak_*`, `portrait_sturdy_*`)
- Wake frames / `ashbrand_*`
- Plates / boss portraits / Kotlin

## Out of scope

- Final / higher-fidelity art pass (current = PLACEHOLDER)
- Scout / Extra rumor Hub offers (no skill glyphs)
- Combat math, pool gating, Hub UI wiring (Engineer)

## Checklist

- [x] `glyph_cinder_vow.png` — vow-spark, 96×96 RGBA + volume
- [x] `glyph_grave_nail.png` — nail, 96×96 RGBA + volume
- [x] Generator `tools/gen_glyphs_hub_v0127.py` (these two only)
- [x] Frozen sha256: hostflint / portrait_you / wake_vfx_charge
