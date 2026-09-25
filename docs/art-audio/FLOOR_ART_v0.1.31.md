# Floor art — v0.1.31 (Art & Audio)

**Fidelity:** PLACEHOLDER (sheet-crop; Elliott motifs)  
**WO:** v0.1.31-floorart  
**Owner:** Art & Audio (assets) · Engineer (wire)  
**Do not invent** tokens or a new hall scene — crop + dim only.

---

## Backdrop

| Item | Value |
|------|--------|
| Source | `/workspace/tod-floorart-v0131/floor_backdrop.jpg` (md5 `a2a23bd78b88277674e9b2817234074c`, 784×1168 RGB) |
| Dimmed drawable | `app/src/main/res/drawable/floor_backdrop.jpg` → `R.drawable.floor_backdrop` |
| Stage copy | `/workspace/tod-floorart-v0131/floor_backdrop_dim.jpg` |
| Size | 784×1168 JPG |
| Treatment | Midtones/highlights pulled ~12–20% (Brightness 0.82 + light contrast). Silhouette kept; no new scene. |
| Floors | Same file for Floor 1 + Floor 2. Engineer may darken F2 ~10–15% in code (`FloorArt.FLOOR2_EXTRA_DARK_ALPHA`). |

Generator: `tools/prep_floorart_v0131.py` (idempotent).

---

## Node tokens (128×128 RGBA)

Cropped from `/workspace/tod-floorart-v0131/node_sheet.jpg` (md5 `bc6f1a6ee3ace3804b540fb16b7e132e`, 784×1168, 2×3 circular tokens on dark bg). Outside the painted ring knocked to full transparency (soft ~1–2px rim). PathScreen chips are 52.dp; tokens authored at **128×128**.

| Drawable basename | Motif (Elliott sheet) | Stage path |
|-------------------|----------------------|------------|
| `node_start` | Gold ring + stone steps | `tod-floorart-v0131/tokens/node_start.png` |
| `node_combat` | Purple ring + crossed rust blades | `…/node_combat.png` |
| `node_treasure` | Grey ring + coffer | `…/node_treasure.png` |
| `node_shop` | Purple ring + coin + ledger | `…/node_shop.png` |
| `node_rest` | Teal/green ring + bandage roll | `…/node_rest.png` |
| `node_fog` | Grey ring + ? | `…/node_fog.png` |

Also copied to `app/src/main/res/drawable/<name>.png`.

Contact sheet (optional): `/workspace/tod-floorart-v0131/tokens/_preview.png`.

---

## Engineer wire hint

Map NodeType → drawable basename (update `FloorArt` constants if they still say `token_*`):

| NodeType / state | Drawable |
|------------------|----------|
| `NodeType.START` | `node_start` |
| `NodeType.COMBAT` | `node_combat` |
| `NodeType.TREASURE` | `node_treasure` |
| `NodeType.SHOP` | `node_shop` |
| `NodeType.REST` | `node_rest` |
| EVENT unrevealed / fogged (`!showType`) | `node_fog` |
| EVENT after Scout reveal | real type token (`node_shop` / `node_rest` / …) — fog only while hidden |
| `NodeType.BOSS` | **no token this pass** — keep letter **B** or combat letter treatment (sheet has no boss crop) |

Captions stay under tokens (Engineer owns text). Revealed gold-ring rumor nodes keep rumor caption under the token.

---

## Frozen (do not touch)

- `portrait_*`
- `wake_*`
- `glyph_*`
- packs / plates

### Frozen sha256 proof (before → after, unchanged)

| File | sha256 |
|------|--------|
| `glyph_hostflint.png` | `763a18b4ccd239edad85343f31587e12d5b0f3601972b3504823b398cba150bd` |
| `portrait_you.png` | `cd1d2cdbd062734492380a005288e24797fdf5cde310ea9fbccd0b002e533019` |
| `wake_vfx_charge.png` | `3c3ffbf49aa27b68b80216098fe020f14fcf5a0d834fa935aca3460728b2ae34` |

Re-check after Art drop must match exactly (before == after).

---

## Re-run

```bash
/tmp/artvenv/bin/python tools/prep_floorart_v0131.py
```
