# QA Gate — v0.1.19-glyphs (2026-09-23)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.19-glyphs  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-23 ~20:35–20:37 EDT

**Tip (final / origin/main):** `f5a42be65d8e409dff27833e1c758598fcc09a10`  
**Feature tip (glyph wiring + PNGs + tests):** `120b26bca5634c7ca1a999014e82cae00ad8e636`  
**Note:** `f5a42be` is GLYPHS doc-only; all 11 `glyph_*.png` byte-identical to `120b26b`. APK built for feature tip; identity unchanged.

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `f5a42be65d8e409dff27833e1c758598fcc09a10` |
| origin/main | `f5a42be65d8e409dff27833e1c758598fcc09a10` |
| Match claimed final tip | **YES** |
| Subject (final) | `v0.1.19-glyphs: Art polish drop-in glyphs + note` |
| Author / date (final) | Android Engineer — 2026-09-23 20:36:53 -0400 |
| Feature tip subject | `v0.1.19-glyphs: skill tile glyphs on combat + loadout` |
| Feature tip author / date | Android Engineer — 2026-09-23 20:35:35 -0400 |
| Status | `main...origin/main` (clean tracking) |

**`--stat` (`120b26b` feature):** SkillGlyph.kt (+85), SkillGlyphIcon.kt (+77), CombatScreen.kt, LoadoutScreen.kt, 11× `glyph_*.png`, SkillGlyphV0119Test.kt (+97), docs (glyphs-v0119 / GLYPHS / art-audio / cards / README), `tools/gen_glyphs_v0119.py`. **22 files, +1078/−9.** No WakeArt / BodyArt / CombatSpeed / MidRunSave / StatusPips / Brace / Path / combat-math sources.

**`--stat` (`f5a42be`):** `docs/art-audio/GLYPHS_v0.1.19.md` only (+45/−22). Drawable tree unchanged vs `120b26b`.

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 18213704 | 2026-09-23 20:35:29 EDT | `5469bb623bfbf5d957f80ea1fefd17d3` |
| `app/build/outputs/apk/debug/app-debug.apk` | 18213704 | 2026-09-23 20:35:29 EDT | `5469bb623bfbf5d957f80ea1fefd17d3` |

Claimed: 18213704 / `5469bb623bfbf5d957f80ea1fefd17d3` → **MATCH** (root + build). Unchanged vs feature tip `120b26b` (no rebuild required for note-only `f5a42be`).

---

## C) Glyph assets / Elliott map

Design lock: `docs/glyphs-v0119.md`. Generator: `tools/gen_glyphs_v0119.py` (11 placeholder PNGs — valid for gate; map matches).

| Asset | Skill (Elliott) | md5 |
|-------|-----------------|-----|
| `glyph_hostflint.png` | Hostflint — flint / spark | `695f253c7829cefe21e4b2e08b2985bd` |
| `glyph_emberbrand.png` | Emberbrand — flame | `ee8e8ad2a805d49da1aee46e16f1fac5` |
| `glyph_tower_pike.png` | Tower Pike — spear | `93610dec4dfd5c9f269c7ca7301e000d` |
| `glyph_ruin_seal.png` | Ruin Seal — cracked seal | `528b6459c9532cf9a4d7b6c2737c9072` |
| `glyph_shadow_latch.png` | Shadow Latch — hook | `8e35634ac1f1b75f296d72a7d69e0515` |
| `glyph_cinder_step.png` | Cinder Step — boot + spark | `0195e84aa3bbf14671c3688633650698` |
| `glyph_iron_mantle.png` | Iron Mantle — kite shield (Brace) | `fb3e7acfeff81fe84647aebba7cb0082` |
| `glyph_vow_plate.png` | Vow Plate — heavy plate (Brace) | `d8c75051d9261db9ba3d5ce724a5c9e9` |
| `glyph_dust_veil.png` | Dust Veil — cloak (Brace) | `7ededc774aaf06e63cb21e1b3d43d580` |
| `glyph_ash_press.png` | Ash Press — hammer + plus | `d67ce089a905bfeb5bfd0b38285c2758` |
| `glyph_relic_shard.png` | Relic Shard — shard + plus | `50d4623dcc62475acbf51ddfabde9b12` |

**Count:** exactly **11** `glyph_*.png` under `app/src/main/res/drawable/` — no extras.

**Code map (`SkillGlyph.DRAWABLE_BY_ID` / `SkillGlyphResources.resId`):** one-to-one with Elliott table + filenames above. Jobs: Damage 6 / Brace 3 / Mixed 2. Ashbrand / unknown → null (no invent). `CardCatalog.all` size 11 = mapped set.

---

## D) Unit tests

Commands:

- `./gradlew :app:testDebugUnitTest --tests '*SkillGlyphV0119*' --tests '*BodyArtV0118*' --tests '*WakeArtV0117*' --tests '*WakeArtV0116*' --tests '*WakeArtV0115*' --tests '*CombatSpeedV0111*' --tests '*MidRunSaveV0112*' --tests '*StatusPipsV0113*' --tests '*BraceSyncV0114*'` → **BUILD SUCCESSFUL**
- `./gradlew :app:testDebugUnitTest` → **BUILD SUCCESSFUL**

| Suite | Result |
|-------|--------|
| SkillGlyphV0119Test | **8/8 PASS** |
| BodyArtV0118Test | **6/6 PASS** (no regress) |
| WakeArtV0117Test | **4/4 PASS** |
| WakeArtV0116Test | 7/7 PASS |
| WakeArtV0115Test | 8/8 PASS |
| CombatSpeedV0111Test | 5/5 PASS |
| MidRunSaveV0112Test | 8/8 PASS |
| StatusPipsV0113Test | 8/8 PASS |
| BraceSyncV0114Test | 7/7 PASS |
| **Full `:app:testDebugUnitTest`** | **109/109 PASS** (19 classes, 0 fail/err/skip) |

### SkillGlyphV0119Test methods

1. `everyCatalogSkill_hasGlyph` — PASS
2. `ashbrandAndWeapon_haveNoGlyph` — PASS
3. `braceJobs_locked` — PASS
4. `mixedJobs_locked` — PASS
5. `damageJobs_restOfCatalog` — PASS
6. `drawableNames_matchArtDropInContract` — PASS
7. `noInventedIds_unknownReturnsNull` — PASS
8. `glyphSize_inBuriedbornesRange` — PASS

---

## E) Source (cases 1–7)

**Shared binding (loadout + combat same drawables):**

- Domain: `SkillGlyph.drawableName` / `DRAWABLE_BY_ID` (SkillGlyph.kt:39–67)
- UI resolve: `SkillGlyphResources.resId` → `R.drawable.glyph_*` (SkillGlyphIcon.kt:61–76)
- Combat: `SkillSlot` → `SkillGlyphIcon(cardId=…)` (CombatScreen.kt:328)
- Loadout: `CardRow` → `SkillGlyphIcon(cardId=…)` (LoadoutScreen.kt:154)
- Size: `GLYPH_SIZE_DP = 28` (24–32); slot height 72 dp (SkillGlyph.kt:23–26; CombatScreen.kt:319)

**Exhaust dims glyph + name:**

- Combat `SkillSlot`: `dimmed = spent && !current` → whole Column `.alpha(0.35f)` including glyph Image + title Text (CombatScreen.kt:316–330)

**Wake crescent above bar:**

- `WakeStageOverlay` drawn in portrait `Box` after portrait Row (CombatScreen.kt:199–232); skill `Row` is a sibling **below** that Box (CombatScreen.kt:239–250) → crescent remains above the skill bar

**Ashbrand blade only:**

- Weapon bar / loadout weapon plate: `AshbrandIcon` only (CombatScreen.kt:354–356; LoadoutScreen.kt:113–114)
- `SkillGlyph.hasGlyph("ashbrand")==false`; `isWeaponWithoutGlyph` (SkillGlyph.kt:71–73; test `ashbrandAndWeapon_haveNoGlyph`)

**Green outline + glossary unchanged:**

- Brace → `Moss` border when not current/spent (CombatScreen.kt:307–311; LoadoutScreen.kt:137–139); `skillJobIsBrace` (SkillGlyphIcon.kt:45–47)
- `GlossaryText` / `gc.showGlossary` still wired (CombatScreen.kt:273–276; LoadoutScreen.kt:157); Glossary.kt not in feature commit

**Frozen (case 7):**

- Feature commit does not touch WakeArt / BodyArt / CombatEngine math / Path / StatusPips / MidRunSave / CombatSpeed sources
- Regression suites above green; full **109/109**

---

## Cases 1–7

| # | Claim | Verdict |
|---|-------|---------|
| 1 | All current cards show mapped glyph per Elliott map | **PASS** — 11/11 DRAWABLE_BY_ID + PNGs + `drawableNames_matchArtDropInContract` / `everyCatalogSkill_hasGlyph`; no invent (`noInventedIds_unknownReturnsNull`) |
| 2 | Loadout + combat match (same glyph per skill) | **PASS** — both call `SkillGlyphIcon` → same `SkillGlyphResources.resId` |
| 3 | Exhausted tile: glyph + name both dim | **PASS** — tile-level alpha 0.35 on glyph+name Column when spent |
| 4 | Wake crescent still above the bar | **PASS** — `WakeStageOverlay` in portrait Box; skill bar below |
| 5 | Ashbrand: blade only, no second glyph | **PASS** — `AshbrandIcon` only; no `glyph_*` for ashbrand |
| 6 | Green outline skills still green; tap/glossary unchanged | **PASS** — Brace → Moss; GlossaryText unchanged; Glossary not in commit |
| 7 | Frozen green: Wake / You / Ash-Warden / Seal-Warden F1 orange / 2x / save / pips / path / math | **PASS** — frozen suites + BodyArtV0118 6/6 + full 109/109; no frozen sources in feature commit |

---

## Docs

- Spec: `docs/glyphs-v0119.md`
- Art note: `docs/art-audio/GLYPHS_v0.1.19.md`
- This gate: `docs/qa-gate-v0119-glyphs-2026-09-23.md`

---

## Overall

**Gate PASS** — final tip `f5a42be` (== origin/main); feature tip `120b26b`; APK MATCH; 11 glyphs map-locked; SkillGlyphV0119 **8/8**; full suite **109/109**; cases 1–7 PASS.
