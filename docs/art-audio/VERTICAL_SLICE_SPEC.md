# Tower of Darkness — Vertical Slice Art & Audio Spec
**Fidelity:** PLACEHOLDER (PH) only — **no BLOCK / no FINAL** until asked  
**Style:** simple painterly · bold silhouettes · limited palette · animations carry spectacle  
**Player PH look:** Ashen Host–like remnant (ember-and-moss)  
**Owner:** Art & Audio → Chief of Staff-ToD  
**Date:** 2026-09-20

## Hard rule (Elliott)
ToD is **fully separate** from Tower of the World and Eldermark.
- Do **not** inventory, copy, symlink, cue, or ship art/music from `eldermark/` or `artifacts/tower-of-the-world/` (or any ToW path) as ToD deliverables.
- Mood borrow from those projects is discouraged — stop.
- ToD audio = silent PH stubs or ToD-local files **only** under `app/src/main/assets/`.

## Canonical paths
| What | Path |
| --- | --- |
| This spec | `/workspace/tower-of-darkness/docs/art-audio/VERTICAL_SLICE_SPEC.md` |
| Living v0 spec | `/workspace/tower-of-darkness/docs/art-audio-v0.md` |
| Art | `/workspace/tower-of-darkness/app/src/main/assets/art/` |
| Audio | `/workspace/tower-of-darkness/app/src/main/assets/audio/` |
| Manifest | `/workspace/tower-of-darkness/app/src/main/assets/MANIFEST.md` |

---

## 1. Hero — modular layers + rarity glow

Canvas **256×256** · pivot feet **(128, 240)** · bottom→top

| z | File | Role |
| --- | --- | --- |
| 0 | `art/hero/hero_shadow.png` | Soft oval |
| 1 | `art/hero/hero_body.png` | Remnant body + hood |
| 2 | `art/hero/hero_armor.png` | Chest + ember mark |
| 3 | `art/hero/hero_weapon.png` | Main-hand |
| 4 | `art/hero/hero_accessory.png` | Moss cloak |
| 5 | `art/hero/hero_glow.png` | Rarity glow (not baked into body) |

**Palette:** ash `#2B2A28` · bone `#E6D7B8` · ember `#C45A2D` · moss `#4A5C3A` · steel `#6B7280` · rim `#F0E6D0`

**Rarity glow:** common hidden · uncommon `#4ADE80` · rare `#38BDF8` · epic `#A78BFA` · legendary `#FBBF24`  
Plates: `art/ui/rarity_glow_{uncommon,rare,epic,legendary}.png`

---

## 2. Enemies (one-floor slice)

| ID | Role | File |
| --- | --- | --- |
| `chr_enemy_goblin` | trash — early nodes | `art/enemies/chr_enemy_goblin.png` |
| `chr_enemy_orc` | heavy — early nodes | `art/enemies/chr_enemy_orc.png` |
| `chr_enemy_emberdrake` | path / floor boss | `art/enemies/chr_enemy_emberdrake.png` |

---

## 3. Animation states

`idle` · `attack_windup` · `attack_strike` (contact frame 1) · `hit_react` · `die` · `victory` (hero)

- **Common hit (quick):** ~250–350 ms · `sfx_hit` + `fx_hit_common` · no shake · no slow-mo  
- **Legendary:** charge + `sfx_legendary_sting` + shake + slow-mo strike · `sfx_hit` + `fx_hit_legendary` · ~700–900 ms  

FX: `art/fx/fx_hit_common.png` · `art/fx/fx_hit_legendary.png`

---

## 4. SFX trigger map (ToD-local only)

Silent 0.1s WAV stubs today. Missing file = no-op. **No external music beds.**

| Trigger | When | File |
| --- | --- | --- |
| `sfx_dice` | Dice / resolve start | `audio/sfx_dice.wav` |
| `sfx_card_fire` | Card commits | `audio/sfx_card_fire.wav` |
| `sfx_hit` | Attack contact | `audio/sfx_hit.wav` |
| `sfx_miss` | Miss / fail-to-connect | `audio/sfx_miss.wav` |
| `sfx_legendary_sting` | Legendary charge peak | `audio/sfx_legendary_sting.wav` |

Music: none required for slice. Do not pull beds from Eldermark/ToW.

---

## 5. Holds
- **GREENLIGHT:** PH layered PNGs + silent SFX stubs (on disk under app assets)
- **NO:** BLOCK-in, FINAL art/SFX, or any ToW/Eldermark reuse
