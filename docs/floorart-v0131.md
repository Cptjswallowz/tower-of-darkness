# Floor art — v0.1.31-floorart

Status: **implemented** (feature tip; **no tag / no GitHub release**). Owner: Engineer + Art.  
Baseline: tip of v0.1.29-hubmore (`71448b7` / release `9fe5da0`).

Path map visuals only. **Do not** change graph layout, weights, tap targets, Hub, Wake, 2x, path math, or packs. **Do not** paint a second plate behind combat portraits. **No** new skills / scenes.

Art note: `docs/art-audio/FLOOR_ART_v0.1.31.md`.

---

## Backdrop

| Item | Value |
|------|--------|
| Source | `/workspace/tod-floorart-v0131/floor_backdrop.jpg` → Art-dimmed `floor_backdrop_dim.jpg` |
| Drawable | `app/src/main/res/drawable/floor_backdrop.jpg` → `R.drawable.floor_backdrop` |
| Screen | `PathScreen` (Floor 1 + Floor 2) |
| Dim | Art midtone pull + compose `BACKDROP_ALPHA` / scrim so gold/purple rings still read |
| Floor 2 | Same asset; **~10–15% darker** via `FloorArt.FLOOR2_EXTRA_DARK_ALPHA` overlay |

---

## Type tokens (replace letter glyphs)

Art crops from `node_sheet.jpg` (128×128 RGBA). Drawable basenames:

| Basename | NodeType | Motif |
|----------|----------|-------|
| `node_start` | START | Stairs / gold ring |
| `node_combat` | COMBAT | Crossed blades / purple ring |
| `node_treasure` | TREASURE | Chest |
| `node_shop` | SHOP | Coin + ledger |
| `node_rest` | REST | Bandage roll / teal ring |
| `node_fog` | fogged + EVENT | `?` / grey ring |

Stage: `/workspace/tod-floorart-v0131/tokens/node_*.png` → `app/src/main/res/drawable/`.

### Reveal rules

- **Fogged** (`!showType`): always `node_fog` inside the circle.
- **Scout / reveal**: swap to the real type token.
- **Tiny captions** under tokens (type name when revealed/scouted; rumor text while fogged / gold-ring rumor affordance).
- **Boss**: no crop this pass — keep letter **B**.
- **EVENT** revealed: `node_fog` (? motif; no separate Event crop).

Domain: `FloorArt`. UI: `PathScreen` → `R.drawable.node_*`.

---

## Frozen

- Path generator weights / edges / tap targets
- Hub offers / Wake / 2x / packs / combat portraits / plates / skills
- Frozen sha256 (Art proof): `glyph_hostflint` / `portrait_you` / `wake_vfx_charge`

---

## Engineer checklist

- [x] `floor_backdrop` behind PathScreen (both floors; F2 darker)
- [x] Six `node_*.png` from Art `tokens/`
- [x] Fog → type token swap; captions kept; Boss letter B
- [x] Unit tests `FloorArtV0131Test`
- [x] Docs this file + Art `FLOOR_ART_v0.1.31.md`
- [ ] No tag / no GitHub release

