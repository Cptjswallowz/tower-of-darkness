# Tile polish — v0.1.59-tilepolish

Status: **implemented** (Android). Art plate: `docs/art-audio/TILEPOLISH_PLATE_v0.1.59.md`.

**Frozen:** remnants_bank / Title, Forge costs/A-B text, scrap drops, relic effects, combat FX/SFX. No Hub/Title/Summary restyle. No new skills. No persist skill levels.

## PART A

1. Combat skill tiles: gold roman pip TOP-RIGHT **inside** border (not on glyph). Lv1 none, Lv2 II, Lv3 III (~12sp). Forge list keeps I/II/III.
2. Shop climb-wallet prices: `purse` not `rem` (`ScrapPouch.shopPriceLine`). Hub remnants unchanged.
3. Long-press combat skill → glossary body = Forge upgraded sentence for that skill level.

## PART B

- Drawable `ui_tile_plate.png` (md5 `20dc01b0e3b934c2bb08c8b26cf77524`) under 5 combat skill tiles, Ashbrand weapon bar, Forge rows.
- Floor node discs: same plate at lower opacity (`NODE_DISC_PLATE_ALPHA` ≈ 0.40 of skill).
- Baked center α ≈ 0.35. No FX packs on plate.

## Packaging

versionCode **60** · versionName **0.1.59-tilepolish**. HOLD tag — no gh release.
