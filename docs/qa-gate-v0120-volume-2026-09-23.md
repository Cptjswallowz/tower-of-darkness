# QA Gate — v0.1.20-volume (2026-09-23)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.20-volume  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-23 ~20:56–20:57 EDT

**Tip (HEAD / origin/main):** `45cbbd89974c3d82ee34cb0939b6e585923d4f5d`  
**Claimed tip:** `45cbbd89974c3d82ee34cb0939b6e585923d4f5d` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `45cbbd89974c3d82ee34cb0939b6e585923d4f5d` |
| origin/main | `45cbbd89974c3d82ee34cb0939b6e585923d4f5d` |
| Match claimed tip | **YES** |
| Subject | `v0.1.20-volume: Art volume drop-ins + wire` |
| Author / date | Android Engineer \<android-engineer@tod.local\> — 2026-09-23 20:56:10 -0400 |
| Status | `main...origin/main` (clean tracking) |

**`--stat`:** VolumeArt.kt (+96), VolumeFrame.kt (+38), HeroShowcase.kt, SkillGlyphIcon.kt, AshbrandIcon.kt, CombatScreen.kt (dim path), Theme.kt (+GoldAsh), 14× volume-baked PNGs (`portrait_you`, `portrait_ash_warden`, `ashbrand_icon`, 11× `glyph_*.png`), VolumeArtV0120Test.kt (+114), docs (`volume-v0120.md`, `VOLUME_v0.1.20.md`, art-audio-v0), `tools/gen_volume_v0120.py` (+220). **27 files, +674/−26.**

**Not in tip (frozen sources):** no WakeArt / BodyArt / SkillGlyph.kt / CombatSpeed / MidRunSave / StatusPips / Brace / Path / CombatEngine math edits.

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 18213704 | 2026-09-23 20:56:05 EDT | `0420df3576910cd1d704f856cd82c16b` |
| `app/build/outputs/apk/debug/app-debug.apk` | 18213704 | 2026-09-23 20:56:01 EDT | `0420df3576910cd1d704f856cd82c16b` |

Claimed: 18213704 / `0420df3576910cd1d704f856cd82c16b` → **MATCH** (root + build). Parent pre-check re-confirmed.

---

## C) Asset inventory (volume bake)

Design lock: `docs/volume-v0120.md`. Generator: `tools/gen_volume_v0120.py` (regenerates flat bodies/glyphs bases, then soft shadow + gold-ash TL rim + 2-tone folds in place; same drawable names).

| Asset | Bytes | md5 |
|-------|------:|-----|
| `portrait_you.png` | 21250 | `1a1abeaaa041ad9ce9590e252f941266` |
| `portrait_ash_warden.png` | 35217 | `0f0d91cd1e4ee8bfce434be53dcb0ba6` |
| `ashbrand_icon.png` | 10887 | `f46f0cbf69fcb65df2bf35632ee54415` |
| `glyph_hostflint.png` | 6855 | `166fc538eaf70e05f6d7fa9e60904ca5` |
| `glyph_emberbrand.png` | 5925 | `b6444511e1bdda7fa1f000572d2a11a0` |
| `glyph_tower_pike.png` | 4069 | `d15a03b5ed7611f04c7e6fd853963dc0` |
| `glyph_ruin_seal.png` | 10023 | `a5ed527d044020e648623fd49d386585` |
| `glyph_shadow_latch.png` | 5441 | `fde2e0b8b29a1a69be0857b36275aa59` |
| `glyph_cinder_step.png` | 5810 | `ecd07c094c9e15380eb09eb95ebbdbc1` |
| `glyph_iron_mantle.png` | 7193 | `d727a18945cc54465494d10769cba2ad` |
| `glyph_vow_plate.png` | 7553 | `7c7c9d40cd9a277e56818425447cf95f` |
| `glyph_dust_veil.png` | 7637 | `f80b2f2e13c9ae5e82f0d20ba3d1971a` |
| `glyph_ash_press.png` | 4955 | `f7ba42dcd707b81547ecdbdc3c4d8a12` |
| `glyph_relic_shard.png` | 7840 | `e1c9669e1ccbc8c34911410783b99546` |

**Changed vs prior tip `f5a42be`:** exactly those 14 drawables (byte sizes up; md5s all differ). **Wake / spark frozen (md5 MATCH gen FROZEN + unchanged vs prior):**

| Frozen | md5 |
|--------|-----|
| `wake_vfx_charge.png` | `699ec4bf4539ed2e09922e5870d34c35` |
| `wake_vfx_slash.png` | `0ae68a58e7f3a2034cdfbd2a206ca85b` |
| `wake_vfx_impact.png` | `8caf0a1c282f99e0ce2937a6aba82c17` |
| `ashbrand_spark.png` | `36a10d0d431c029aea23b08bf8e25cfc` |

**Art-baked volume SoT:** `VolumeArt.artBakedDrawables()` size **14** (portraits + ashbrand + 11 glyphs); Compose `volumeChrome` / `VolumeFrame` identity no-op — **no double shadow**.

---

## D) Unit tests

Commands (measured this gate):

- `./gradlew :app:testDebugUnitTest --tests '*VolumeArtV0120*' --tests '*SkillGlyphV0119*' --tests '*BodyArtV0118*' --tests '*WakeArtV0117*' --tests '*WakeArtV0116*' --tests '*WakeArtV0115*' --tests '*CombatSpeedV0111*' --tests '*MidRunSaveV0112*' --tests '*StatusPipsV0113*' --tests '*BraceSyncV0114*'` → **BUILD SUCCESSFUL**
- `./gradlew :app:testDebugUnitTest` → **BUILD SUCCESSFUL**

| Suite | Result |
|-------|--------|
| VolumeArtV0120Test | **11/11 PASS** |
| SkillGlyphV0119Test | **8/8 PASS** |
| BodyArtV0118Test | **6/6 PASS** |
| WakeArtV0117Test | **4/4 PASS** |
| WakeArtV0116Test | **7/7 PASS** |
| WakeArtV0115Test | **8/8 PASS** |
| CombatSpeedV0111Test | **5/5 PASS** |
| MidRunSaveV0112Test | **8/8 PASS** |
| StatusPipsV0113Test | **8/8 PASS** |
| BraceSyncV0114Test | **7/7 PASS** |
| **Full `:app:testDebugUnitTest`** | **120/120 PASS** (20 classes, 0 fail/err/skip) |

### VolumeArtV0120Test methods

1. `tag_andSurfaces_locked` — PASS  
2. `playerAndAshWarden_getVolume` — PASS  
3. `sealWardenAndTrash_noVolume` — PASS  
4. `wakeVfxAndSpark_frozenNoVolume` — PASS  
5. `glyphSymbols_notRestyled_mapUnchanged` — PASS  
6. `artBake_coversPortraitsGlyphsAshbrand_notWake` — PASS  
7. `foldDarkened_portraitsAndAshbrand` — PASS  
8. `dimmedGlyph_staysReadable` — PASS  
9. `goldAshArgb_isWarmGoldFamily` — PASS  
10. `composeChrome_doesNotDoubleArtBake` — PASS  
11. `vowPlate_distinctFromIronMantle` — PASS  

---

## E) Source (cases 1–5) vs `docs/volume-v0120.md`

**Art bake + Compose no-op (engineer claim):**

- Bake SoT: `tools/gen_volume_v0120.py` — `soft_drop_shadow` / `gold_ash_rim` / `deepen_folds` on same drawable names; does not touch wake/spark (lines 1–8, 46–51, 156–165, 198–214).
- Domain: `VolumeArt.artBakedDrawables()` 14 names; `composeChromeDrawsShadowRim() == false` (VolumeArt.kt:65–92).
- UI: `Modifier.volumeChrome` = identity; `VolumeFrame` wraps Box with no-op chrome (VolumeFrame.kt:19–38).
- Wired markers only: `HeroShowcase` (You), `EnemySilhouette` Ash-Warden branch, `SkillGlyphIcon`, `AshbrandIcon` — all call `volumeChrome` but draw Art PNG once.
- Tests: `artBake_coversPortraitsGlyphsAshbrand_notWake`, `composeChrome_doesNotDoubleArtBake`, `foldDarkened_portraitsAndAshbrand`.

**Same soldier identity (title vs combat):**

- Title: `MainMenuScreen.kt:46` → `HeroShowcase(...)`.
- Combat: `CombatScreen.kt:201` → `HeroShowcase(...)`.
- Both load `R.drawable.portrait_you` (HeroShowcase.kt:77–78); BodyArt.PLAYER_DRAWABLE = `"portrait_you"`. Volume = lighting bake on same silhouette (gen regenerates bodies then shades — no new figure).

**Dim glyphs readable:**

- `VolumeArt.DIMMED_GLYPH_ALPHA = 0.68f` (VolumeArt.kt:30); test requires ≥0.55 / ≤0.85.
- `SkillGlyphIcon`: greyscale ColorMatrix + `.alpha(DIMMED_GLYPH_ALPHA)` when spent (SkillGlyphIcon.kt:39–51).
- Combat `SkillSlot`: removed whole-tile `.alpha(0.35f)`; passes `spent=dimmed` to glyph; title/weight use milder Bone alphas (CombatScreen.kt:315–328).

**Pips / crescent still on top:**

- `StatusPipRow` still under HP bars inside portrait columns (CombatScreen.kt:205–219).
- `WakeStageOverlay` still drawn in portrait `Box` after Row, aligned TopCenter height 140.dp (CombatScreen.kt:222–231); skill `Row` sibling below (238–249).
- `volumeChrome` no-op → no chrome inset that would shove pips; HeroShowcase still shrinks sprite via `BodyArt.SPRITE_INSET_FRACTION` only (HeroShowcase.kt:70–75). StatusPip / Wake sources not in tip.

**No layout shift:**

- Slot sizes unchanged (BodyArt / SkillGlyph.kt not edited). `volumeChrome` returns `this`. Skill slot height still `SkillGlyph.SKILL_SLOT_HEIGHT_DP`; glyph size still `GLYPH_SIZE_DP`.

**No 3D; glyph shapes not restyled; frozen + placeholders:**

- No 3D / perspective engine in tip; gen is 2D PIL shadow/rim/fold on existing alpha silhouettes.
- `VolumeArt.glyphSymbolsRestyled() == false`; SkillGlyph map names unchanged (`glyphSymbols_notRestyled_mapUnchanged`); SkillGlyph.kt not in commit.
- Seal-Warden / trash: `appliesToSealWarden/Trash == false`; `EnemySilhouette` else-branch Canvas placeholders (HeroShowcase.kt:142–164); test `sealWardenAndTrash_noVolume` + BodyArtV0118 `trashAndSealSpinner_stayPlaceholders` / `f1SealWarden_dragon_staysPlaceholder`.
- Wake/path/2x/save frozen: `appliesToWakeVfx/Spark == false`; wake PNG md5s MATCH; frozen regression suites all green; those domain sources absent from tip.

---

## Cases 1–5

| # | Claim | Verdict |
|---|-------|---------|
| 1 | Combat and title soldier same figure, just lit (volume lighting only) | **PASS** — both `HeroShowcase` → `portrait_you`; Art bake shades same silhouette (`gen_volume_v0120` + `foldDarkened` / artBake tests) |
| 2 | Glyphs still readable when dimmed | **PASS** — greyscale @ `DIMMED_GLYPH_ALPHA=0.68` (not 0.35 crush); `dimmedGlyph_staysReadable` |
| 3 | Pips / crescent still sit on top | **PASS** — StatusPipRow under HP; WakeStageOverlay in portrait Box above skill bar; chrome no-op |
| 4 | No layout shift | **PASS** — `volumeChrome` identity; BodyArt/SkillGlyph slot sizes untouched |
| 5 | No 3D; glyph shapes not restyled beyond volume; Wake/path/2x/save frozen; Seal-Warden/trash placeholders unchanged | **PASS** — 2D PNG bake only; `glyphSymbolsRestyled=false`; wake md5s frozen; Seal/trash Canvas + `sealWardenAndTrash_noVolume`; frozen suites + full 120/120 |

---

## Docs

- Spec: `docs/volume-v0120.md`
- Art note: `docs/art-audio/VOLUME_v0.1.20.md`
- This gate: `docs/qa-gate-v0120-volume-2026-09-23.md`

---

## Overall

**Gate PASS** — tip `45cbbd89974c3d82ee34cb0939b6e585923d4f5d` (== origin/main == claimed); APK MATCH 18213704 / `0420df3576910cd1d704f856cd82c16b`; VolumeArtV0120 **11/11**; full suite **120/120**; Art-baked volume + Compose chrome no-op verified; cases 1–5 PASS.
