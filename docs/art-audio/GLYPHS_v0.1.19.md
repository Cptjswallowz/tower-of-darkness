# Skill glyphs — v0.1.19-glyphs (Art note)

**Fidelity:** PLACEHOLDER painterly PNGs (`tools/gen_glyphs_v0119.py`)  
**Tag:** none this pass (NO TAG per WO)  
**Audience:** Art + Android Engineer (via Chief of Staff)  
**WO lock:** `docs/glyphs-v0119.md`

Wake art / bodies / pips / path / combat math stay **frozen**.

## Assets (drop-in drawables)

Replace these with final art using the **same resource names**. Current: 96×96 RGBA PNGs from Art gen; readable at 24–32 dp.

| File | Skill | Glyph |
| --- | --- | --- |
| `glyph_hostflint` | Hostflint | flint / spark |
| `glyph_emberbrand` | Emberbrand | flame |
| `glyph_tower_pike` | Tower Pike | spear |
| `glyph_ruin_seal` | Ruin Seal | cracked seal |
| `glyph_shadow_latch` | Shadow Latch | hook |
| `glyph_cinder_step` | Cinder Step | boot + spark |
| `glyph_iron_mantle` | Iron Mantle | kite shield (Brace green) |
| `glyph_vow_plate` | Vow Plate | heavy plate (Brace green) |
| `glyph_dust_veil` | Dust Veil | cloak (Brace green) |
| `glyph_ash_press` | Ash Press | hammer + plus |
| `glyph_relic_shard` | Relic Shard | shard + plus |

Compose: `R.drawable.glyph_*` via `SkillGlyphResources`.

## Out of scope

- Ashbrand second glyph (blade icon only)
- New animations on common skills
- Wake / bodies / Seal-Warden / 2x / save / pips / path / combat math
