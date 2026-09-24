# Skill glyphs — v0.1.19-glyphs (Art note)

**Fidelity:** PLACEHOLDER  
**Tag:** v0.1.19-glyphs (Art drop-in; Engineer wiring already maps same names)  
**Audience:** Android Engineer (via Chief of Staff)  
**WO lock:** `docs/glyphs-v0119.md`  
**Generator:** `tools/gen_glyphs_v0119.py` (`/tmp/artvenv/bin/python`)

Wake art, bodies, Ashbrand blade icon, Seal-Warden, trash, combat math, and Kotlin stay **frozen**. This pass only replaces skill-tile glyph drawables.

## Assets (drop-in drawables)

Transparent **96×96 RGBA PNGs** under `app/src/main/res/drawable/`. Same base names as prior stubs so `SkillGlyph.kt` / `R.drawable.glyph_*` keep working. UI reads them at **24–32 dp** (`SkillGlyph.GLYPH_SIZE_DP = 28`).

Stub `glyph_*.xml` vectors were **deleted** (Android cannot ship both `.xml` and `.png` for one name).

| Path | Skill id | Motif | Job tint | Size |
| --- | --- | --- | --- | --- |
| `app/src/main/res/drawable/glyph_hostflint.png` | `hostflint` | flint / spark | Damage (ember/ash/steel/gold) | 96×96 → 24–32 dp |
| `app/src/main/res/drawable/glyph_emberbrand.png` | `emberbrand` | flame | Damage | 96×96 → 24–32 dp |
| `app/src/main/res/drawable/glyph_tower_pike.png` | `tower_pike` | spear | Damage | 96×96 → 24–32 dp |
| `app/src/main/res/drawable/glyph_ruin_seal.png` | `ruin_seal` | cracked seal | Damage | 96×96 → 24–32 dp |
| `app/src/main/res/drawable/glyph_shadow_latch.png` | `shadow_latch` | hook | Damage | 96×96 → 24–32 dp |
| `app/src/main/res/drawable/glyph_cinder_step.png` | `cinder_step` | boot + spark | Damage | 96×96 → 24–32 dp |
| `app/src/main/res/drawable/glyph_iron_mantle.png` | `iron_mantle` | kite shield | Brace (Moss `#4A5C3A` + `#4ADE80`) | 96×96 → 24–32 dp |
| `app/src/main/res/drawable/glyph_vow_plate.png` | `vow_plate` | heavy plate | Brace (green) | 96×96 → 24–32 dp |
| `app/src/main/res/drawable/glyph_dust_veil.png` | `dust_veil` | cloak | Brace (green) | 96×96 → 24–32 dp |
| `app/src/main/res/drawable/glyph_ash_press.png` | `ash_press` | hammer + plus | Mixed (dmg + heal) | 96×96 → 24–32 dp |
| `app/src/main/res/drawable/glyph_relic_shard.png` | `relic_shard` | shard + plus | Mixed (heal) | 96×96 → 24–32 dp |

Compose: `R.drawable.glyph_*` via `SkillGlyph.drawableName(id)`.

## Ashbrand

**No skill glyph.** Weapon slot stays **blade icon only** (`ashbrand_icon` / AshbrandIcon). Do not invent a second glyph.

## Style

- Simple painterly: bold filled silhouettes, 3–5 colors max, transparent background
- Theme palette: Ash `#2B2A28`, Bone `#E6D7B8`, Ember `#C45A2D`, Moss `#4A5C3A`, Steel `#6B7280`, Gold `#C9A227`, Panel `#1A1228`
- Brace greens: Moss + bright `#4ADE80` outline language
- ~10% padding; high-contrast so grey-out / exhaust still reads
- No text on glyphs; mixed skills use a small gold plus

## Frozen (untouched this pass)

- Wake art (`wake_vfx_*`, Wake crescent / SPARK)
- Ashbrand (`ashbrand_icon`, `ashbrand_spark`)
- Bodies (`portrait_you`, `portrait_ash_warden`)
- Seal-Warden / trash enemy art
- Combat math, pips logic, path, 2x, save, Kotlin sources

## Out of scope

- Final / higher-fidelity art pass (current = PLACEHOLDER)
- New animations on common skills
- Inventing glyphs outside the locked Elliott map
