# Portraits — v0.1.21-portraits (Art & Audio)

**Fidelity:** SOURCE STILLS (Elliott) — crop / transparent backdrop / circular slot prep only. **Not** regenerated PH. No armor repaint, no style match pass.

**Tag:** v0.1.21-portraits  
**WO lock:** `docs/portraits-v0121.md`  
**Script:** `tools/prep_portraits_v0121.py`  
**Sources:** `/workspace/tod-portraits-v0121/{you_soldier,ash_warden,seal_warden}.jpg`

---

## Drawables

| Path | Px | Role |
|------|----|------|
| `app/src/main/res/drawable/portrait_you.png` | **256×256** RGBA circular | **You** — title + combat player |
| `app/src/main/res/drawable/portrait_ash_warden.png` | **320×320** RGBA circular | **F2 Ash-Warden** boss only |
| `app/src/main/res/drawable/portrait_seal_warden.png` | **320×320** RGBA circular (**NEW**) | **F1 Seal-Warden** boss (same boss slot ~160dp as Ash-Warden) |

Prep per source:

1. **you_soldier.jpg** — crop content **inside** thin gold oval (exclude gold rim + outer black); knock teal/dark backdrop; keep soldier / cloak / ember sword.
2. **ash_warden.jpg** — knock solid dark navy/black backdrop; keep coal body, gold cracks, chest seal.
3. **seal_warden.jpg** — knock dark circular vignette / black backdrop; keep rust iron, rune ring, lava core.

After knockout: center subject, scale with ~7% inset, circular alpha (`apply_circle` same approach as `gen_bodies_v0118`), LANCZOS to target.

---

## Wire hint (Engineer — Art does not edit Kotlin)

- Map **`EnemyKind.DRAGON`** (Seal-Warden / F1 boss) → `R.drawable.portrait_seal_warden`.
- If the still clips status pips / HP: **shrink the Image**, do **not** move pips.
- Ash-Warden stays on existing `portrait_ash_warden` for F2 only.

---

## Frozen (do not touch this WO)

| Asset class | Examples |
|-------------|----------|
| Wake VFX | `wake_vfx_charge`, `wake_vfx_impact`, `wake_vfx_slash` |
| Ashbrand | `ashbrand_icon`, `ashbrand_spark` |
| Glyphs | `glyph_*` |
| Trash placeholders | Wretch / Spinner (Canvas circles) |

Record sha256 of `wake_vfx_*` and `glyph_*` before/after prep to prove untouched.

---

## Checklist

- [x] You still → `portrait_you.png` (256, no gold oval, circular alpha)
- [x] Ash-Warden still → `portrait_ash_warden.png` (320, F2)
- [x] Seal-Warden still → `portrait_seal_warden.png` (320, F1, new)
- [x] Reproducible script written
- [x] Wake / glyphs / trash frozen


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

**Proof:** before/after digests identical for all `wake_vfx_*` and `glyph_*` (also ashbrand_* unchanged). Prep script writes only the three `portrait_*.png` paths.
