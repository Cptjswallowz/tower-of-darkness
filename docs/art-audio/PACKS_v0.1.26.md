# Hallway trash packs — v0.1.26-packs (Art & Audio)

**Fidelity:** SOURCE STILLS (Elliott) — crop / transparent backdrop / circular slot prep only. **Not** regenerated PH. No recolor, no restyle, no redraw.

**Tag:** v0.1.26-packs  
**WO lock:** `docs/packs-v0126.md`  
**Script:** `tools/prep_packs_v0126.py`  
**Sources:** `/workspace/tod-packs-v0126/{01..06}_*.jpg`  
**Nobg:** same language as Wardens (`nobg-v0123.md`) — no plate / disc behind PNG.

---

## Drawables

| Path | Source | Band mapping |
|------|--------|--------------|
| `app/src/main/res/drawable/portrait_weak_goblin_knife.png` | `01_weak_goblin_knife.jpg` | Weak Goblin → old **Ash Wretch** band (`EnemyKind.GOBLIN`) |
| `app/src/main/res/drawable/portrait_weak_goblin_bottle.png` | `02_weak_goblin_bottle.jpg` | Weak Goblin → old **Ash Wretch** band (`EnemyKind.GOBLIN`) |
| `app/src/main/res/drawable/portrait_weak_goblin_spikes.png` | `03_weak_goblin_spikes.jpg` | Weak Goblin → old **Ash Wretch** band (`EnemyKind.GOBLIN`) |
| `app/src/main/res/drawable/portrait_sturdy_orc_axe.png` | `04_sturdy_orc_axe.jpg` | Sturdy Orc → old **Ruin Brute / Seal Spinner** band (`EnemyKind.ORC` / `SPIDER`) |
| `app/src/main/res/drawable/portrait_sturdy_orc_cleaver.png` | `05_sturdy_orc_cleaver.jpg` | Sturdy Orc → old **Ruin Brute / Seal Spinner** band (`EnemyKind.ORC` / `SPIDER`) |
| `app/src/main/res/drawable/portrait_sturdy_orc_hammer.png` | `06_sturdy_orc_hammer.jpg` | Sturdy Orc → old **Ruin Brute / Seal Spinner** band (`EnemyKind.ORC` / `SPIDER`) |

All outputs: **256×256** RGBA circular (denser than boss 320 so figures read at trash slot). Goblins = head-to-toe; orcs = bust + weapon. Corners alpha 0; no gold/plate frames.

Prep per source:

1. rembg knock dark backdrop → transparent (preserve weapons, eye glow; fringe residual only).
2. Center on alpha centroid; scale so full silhouette fits **inside** circle (shrink rather than clip feet/ears/axe).
3. ~7% inset + circular alpha (`apply_circle` / same approach as `prep_portraits_v0121.py`).

**Note:** Old pack ids may stay in code; Engineer picks which look variant per fight (equal 1/3 on node create — see WO).

---

## Wire hint (Engineer — Art does not edit Kotlin)

- Map **BodyArt trash** → these six drawables (Weak Goblin ×3 / Sturdy Orc ×3).
- Combat slot: **`TRASH_SLOT_DP = 120`** (smaller than boss ~160dp / You ~90dp context).
- If the still clips status pips / HP: **shrink the Image**, do **not** move pips.
- Bosses (`portrait_you`, `portrait_ash_warden`, `portrait_seal_warden`) and Wake / glyphs stay frozen.

---

## Frozen (do not touch this WO)

| Asset class | Examples |
|-------------|----------|
| Wake VFX | `wake_vfx_charge`, `wake_vfx_impact`, `wake_vfx_slash` |
| Ashbrand | `ashbrand_icon`, `ashbrand_spark` |
| Glyphs | `glyph_*` |
| Boss / You portraits | `portrait_you`, `portrait_ash_warden`, `portrait_seal_warden` |
| HP / counters / path / 2x / Kotlin | untouched |

---

## Checklist

- [x] Weak Goblin ×3 → `portrait_weak_goblin_{knife,bottle,spikes}.png` (256, circular, nobg)
- [x] Sturdy Orc ×3 → `portrait_sturdy_orc_{axe,cleaver,hammer}.png` (256, circular, nobg)
- [x] Reproducible script written
- [x] Wake / glyphs / ashbrand / You / Wardens frozen (sha256 before == after)

---

## Frozen sha256 (before == after)

| File | sha256 |
|------|--------|
| `wake_vfx_charge.png` | `3c3ffbf49aa27b68b80216098fe020f14fcf5a0d834fa935aca3460728b2ae34` |
| `wake_vfx_impact.png` | `f081ef30e84c6bf56aec7d11bb28c157ce1cfb3e16cb23352ba7c32106865086` |
| `wake_vfx_slash.png` | `aa5f0732414abe0bd32292d86f8078f9686e3c64f7800f4c9bc58d467337d325` |
| `glyph_ash_press.png` | `3af7c41d18ebc0324beac2f8f4b8afc46db4a0a13dbd9854dd18f5acb2d207f8` |
| `glyph_cinder_step.png` | `ee049cd4d75cbfd3b44c84646cd7d180dcd06f9ed15788080a7c9929725b5694` |
| `glyph_dust_veil.png` | `e682f4ff1370d72bdf99aa448952a19ef28fd948eb349fea90f3fe86e75b0e1d` |
| `glyph_emberbrand.png` | `74b324add6ac42ad53544956e16b3445c98c29cba1d21999006e4f46590e2cd3` |
| `glyph_hostflint.png` | `763a18b4ccd239edad85343f31587e12d5b0f3601972b3504823b398cba150bd` |
| `glyph_iron_mantle.png` | `42d0b067479d3b896ecc9d3355d380593e1061ec6fb76a12943606a1f32a48b6` |
| `glyph_relic_shard.png` | `582ed5c4b04ccc7696f01fbf04a17e7e9497fd73e852a01578e114d2332c94c2` |
| `glyph_ruin_seal.png` | `c5f38fc8856906c3a7c7bda64b8687be040e732146f8ba075268ef32af3c9086` |
| `glyph_shadow_latch.png` | `7fd07fa70709ccd9f5850fedf815a35cc6fc1f21c9e4bac0b822960cc5044bf1` |
| `glyph_tower_pike.png` | `0cae688cf30b506af4f78d38a2f5c4c57b4218169a541ba849f340007cbd6e09` |
| `glyph_vow_plate.png` | `0a3a63f87a8be95263cf6e61f65466506faec494c544cabe43be5508cee313fd` |
| `portrait_you.png` | `cd1d2cdbd062734492380a005288e24797fdf5cde310ea9fbccd0b002e533019` |
| `portrait_ash_warden.png` | `17b40a9b7e69200d24444f1517b58c991370a1bc1a7e979320ff622f81b9407b` |
| `portrait_seal_warden.png` | `d65aa410191940f1cdb06e0531d2eb2b87db161061461269f489cc3a6d286833` |
| `ashbrand_icon.png` | `2c8bb1a57398bd8a923ee377abc3d2d845a6093767a497f7abbb9325b3d2cfcb` |
| `ashbrand_spark.png` | `2a259d97469fe045750729bb6bf93ce88a1d45c492be5b9e92582b5cbdc8d283` |

**Proof:** before/after digests identical for all listed frozen assets. Prep script writes only the six new `portrait_weak_goblin_*` / `portrait_sturdy_orc_*` paths.
