# Tower of Darkness — Art & Audio v0
**Fidelity:** PLACEHOLDER (PH) only — **BLOCK/FINAL HELD** (Elliott: stay on PH; no further art until asked)  
**Style:** simple painterly · bold silhouettes · limited palette · animations carry spectacle  
**Owner:** Art & Audio → Chief of Staff-ToD  
**Date:** 2026-09-20

## Hard rule (Elliott)
ToD is **fully separate** from Tower of the World and Eldermark.
- **Do not** ship, copy, symlink, or cue ToW/Eldermark art, music, or other assets as ToD deliverables.
- Mood reference from those projects is **discouraged** — stop further borrow.
- All ToD paths live only under `/workspace/tower-of-darkness/`.

## Canonical paths (Android)
- Art: `/workspace/tower-of-darkness/app/src/main/assets/art/`
- Audio: `/workspace/tower-of-darkness/app/src/main/assets/audio/`
- Manifest: `/workspace/tower-of-darkness/app/src/main/assets/MANIFEST.md`
- Spec: `/workspace/tower-of-darkness/docs/art-audio-v0.md`

Mirror (non-canonical): `/workspace/tower-of-darkness/assets/` — prefer app paths above.

---

**v0.1.19 skill glyphs:** `glyphs-v0119.md` / `art-audio/GLYPHS_v0.1.19.md` (placeholder vectors; Art drop-in).

**v0.1.18 combat bodies:** `bodies-v0118.md` (You + Ash-Warden only).

## 1. Hero — modular layers + rarity glow

**Identity (PH):** Ashen Host–like remnant silhouette — **locked for PH** (Elliott widget skipped; stay on PH).  
**Palette:** ash `#2B2A28` · bone `#E6D7B8` · ember `#C45A2D` · moss `#4A5C3A` · steel `#6B7280` · rim `#F0E6D0`

### Canvas / pivot
256×256 · pivot feet **(128, 240)** · layers bottom→top

| z | File | Role | Swap |
| --- | --- | --- | --- |
| 0 | `art/hero/hero_shadow.png` | Soft oval | fixed |
| 1 | `art/hero/hero_body.png` | Remnant body + hood | rare |
| 2 | `art/hero/hero_armor.png` | Chest plate + ember mark | loadout |
| 3 | `art/hero/hero_weapon.png` | Main-hand | loadout |
| 4 | `art/hero/hero_accessory.png` | Moss cloak | loadout |
| 5 | `art/hero/hero_glow.png` | Rarity glow (additive) | rarity |

### Rarity glow
| Rarity | Color | Notes |
| --- | --- | --- |
| common | hidden | glow alpha 0 |
| uncommon | `#4ADE80` @ ~35% | `art/ui/rarity_glow_uncommon.png` |
| rare | `#38BDF8` @ ~45% | `art/ui/rarity_glow_rare.png` |
| epic | `#A78BFA` @ ~55% | `art/ui/rarity_glow_epic.png` |
| legendary | `#FBBF24` @ ~70% | `art/ui/rarity_glow_legendary.png` + charge shimmer |

Glow never baked into body/armor.

---

## 2. Enemy set (one-floor slice)

| ID | Role | File |
| --- | --- | --- |
| `chr_enemy_goblin` | trash — early nodes | `art/enemies/chr_enemy_goblin.png` |
| `chr_enemy_orc` | heavy — early nodes | `art/enemies/chr_enemy_orc.png` |
| `chr_enemy_emberdrake` | floor / path boss | `art/enemies/chr_enemy_emberdrake.png` |

**Deferred (not in one-floor wire):** troll, spider, full dragon. Emberdrake is the boss silhouette (ember-and-ash), not Cutpurse/Spearsore naming.

Same canvas/pivot as hero. Boss layout scale ~1.4–1.6× OK.

---

## 3. Animation states (shared pipeline)

### Character
`idle` (4 loop) · `attack_windup` (2) · `attack_strike` (3, contact on frame 1) · `hit_react` (3) · `die` (5) · `victory` hero-only (4)

### Common hit (quick) ~250–350 ms
windup → strike + `sfx_hit` + `fx_hit_common` → hit_react · **no** shake · **no** slow-mo

### Legendary ~700–900 ms
long windup + glow shimmer → `sfx_legendary_sting` → camera shake → strike in slow-mo (0.35–0.5×, 200–300 ms) → `sfx_hit` + `fx_hit_legendary` → hit_react

FX: `art/fx/fx_hit_common.png` · `art/fx/fx_hit_legendary.png`

---

## 4. SFX trigger map

| Trigger | When | File |
| --- | --- | --- |
| `sfx_dice` | Dice / auto-battler resolve | `audio/sfx_dice.wav` |
| `sfx_card_fire` | Card commits | `audio/sfx_card_fire.wav` |
| `sfx_hit` | Attack contact | `audio/sfx_hit.wav` |
| `sfx_miss` | Miss / fail-to-connect | `audio/sfx_miss.wav` |
| `sfx_legendary_sting` | Legendary charge peak | `audio/sfx_legendary_sting.wav` |

All current WAVs are **silent 0.1s stubs** (paths resolve). Missing = no-op.

---

## 5. Holds
- **GREENLIGHT:** PH layered PNGs + silent SFX stubs (done under `app/src/main/assets/`)
- **HOLD:** BLOCK-in and FINAL — no further art work until Chief of Staff / Elliott asks
- **NO REUSE:** ToW / Eldermark assets banned as ToD deliverables
