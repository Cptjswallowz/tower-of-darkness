> Combat slash/Wake/Brace roles superseded by `IRONCLASH_SFX_v0.1.52.md` (slashlayer). Lentikula Soften/fire + Kenney click still valid.

# IRONCLASH SFX — v0.1.51-ironclash (Art)

P1 audio curation. Combat FX art LOCKED (stroke / puff / Wake) — not touched.

Import **only** used one-shots. Full store packs stay outside the public repo under `/workspace/tod-sfx-v0151/` (do not commit zips/7z).

## Role → file

| Role | Trigger | Path | ms | md5 |
|------|---------|------|---:|-----|
| **impact** | stroke / slash land | `assets/sfx/ironclash/IRONCLASH_23_Flesh_Hit_Light_05.ogg` | 187 | `7f48fddc00f93dee839439fbd57f81bd` |
| **clash/heavy** | Wake under sting | `assets/sfx/ironclash/IRONCLASH_03_Sword_Clash_04_wake380.ogg` | 380 | `5751cbefde0ae473f301abc846089231` |
| **block/shield** | Brace gain | `assets/sfx/ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg` | 351 | `a0350366bc961ea7e65e050a0a70a8dd` |
| **fire impact short** | Ashbrand / Wake tip | `assets/sfx/lentikula/lentikula_fire_impact_5_short220.ogg` | 220 | `1d24b7056b45b49d8897d48926874d19` |
| **heal Soften** | Soften (pitched-down, quiet) | `assets/sfx/lentikula/lentikula_heal_impact_4_soften.ogg` | 366 | `248fb43b6f65506649330185bcba0b03` |
| **Kenney click** | node tap / shop confirm / loadout lock / Continue | `assets/sfx/kenney_ui/click_001.ogg` | 100 | `594234cc27bcfb903fdafc49cb728bc9` |

## Process notes (Art)

- **Clash:** source `IRONCLASH_03_Sword_Clash_04.ogg` (667 ms) trimmed to 380 ms + out-fade (Wake layer ≤400 ms).
- **Brace block:** source `IRONCLASH_14_Shield_Block_Metal_02.ogg` (705 ms) trimmed to 350 ms + out-fade.
- **Fire tip:** Lentikula `Fire Spell Impact 5.wav` attack window @ ~0.505 s, 220 ms mono vorbis.
- **Soften:** Lentikula `Healing Spell Impact 4.wav` attack @ ~0.455 s; pitch ≈0.82 (`asetrate`), volume 0.32, mono vorbis ~366 ms.
- **Impact / click:** unprocessed pack one-shots (already in budget).

No loops. No Kotlin. Engineer wires playback / pool.

## Licenses (ship inside game, not as packs)

| Pack | Path note | License |
|------|-----------|---------|
| IRONCLASH v1.0 (godboyhappy) | `assets/sfx/ironclash/LICENSE.txt` | Free use in projects; do not redistribute as a sound pack |
| Lentikula fire + healing | `assets/sfx/lentikula/SOURCE.txt` | Source archives only under `/workspace/tod-sfx-v0151/` |
| Kenney Interface Sounds | `assets/sfx/kenney_ui/LICENSE.txt` | CC0 |

## Source archive md5 (workspace only — not in repo)

| Archive | md5 |
|---------|-----|
| `IRONCLASH_v1.0.zip` | `a5273101d565d4304f5848f1d663920a` |
| `Basic_Spell_Impacts.zip` | `cb6a281ffa548bc3b02f0a22522af539` |
| `Healing_Spell_Impacts_Pack_by_Lentikula.7z` | `9c304e1c6e3d434d793d813403e58f28` |
| `kenney_interface-sounds.zip` | `763b97e426354c8f8e2bcf900927244b` |

## Engineer handoff

Wire the six paths above. Existing placeholders under `assets/sfx/sfx_*.wav` remain until replaced. Do not copy full IRONCLASH / Lentikula / Kenney packs onto main.
