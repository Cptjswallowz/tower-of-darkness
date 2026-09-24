# QA Gate — v0.1.21-portraits (2026-09-24)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.21-portraits  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-24 ~07:38–07:39 EDT

**Tip (HEAD / origin/main):** `41f99dd493cec96f9225b23ad00e1d6c273f8b41`  
**Claimed tip:** `41f99dd493cec96f9225b23ad00e1d6c273f8b41` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `41f99dd493cec96f9225b23ad00e1d6c273f8b41` |
| origin/main | `41f99dd493cec96f9225b23ad00e1d6c273f8b41` |
| Match claimed tip | **YES** |
| Subject | `v0.1.21-portraits: You/Ash/Seal portrait slots` |
| Author / date | Android Engineer \<android-engineer@tod.local\> — 2026-09-24 07:37:56 -0400 |
| Status | `main...origin/main` (clean tracking) |

**`--stat`:** BodyArt.kt (+29/−), Enemy.kt (DRAGON asset → `portrait_seal_warden`), HeroShowcase.kt (+32/− EnemySilhouette seal branch), `portrait_you.png` (21250→57759), `portrait_ash_warden.png` (35217→79797), `portrait_seal_warden.png` (**NEW** 101554), BodyArtV0118Test.kt (Seal supersession), PortraitsV0121Test.kt (+92), docs (`portraits-v0121.md`, `PORTRAITS_v0.1.21.md`, bodies/art-audio notes), `tools/prep_portraits_v0121.py` (+234). **13 files, +532/−21.**

**Not in tip (frozen sources):** no WakeArt / SkillGlyph.kt / CombatSpeed / MidRunSave / StatusPips / Brace / Path / CombatEngine math / VolumeArt.kt edits. Wake PNG md5s unchanged vs v0.1.20 gate.

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 18213891 | 2026-09-24 07:38:12 EDT | `9b4125ee330a2ed0ea419462d00da121` |
| `app/build/outputs/apk/debug/app-debug.apk` | 18213891 | 2026-09-24 07:38:06 EDT | `9b4125ee330a2ed0ea419462d00da121` |

Claimed: 18213891 / `9b4125ee330a2ed0ea419462d00da121` → **MATCH** (root + build). Parent pre-check re-confirmed.

---

## C) Asset inventory (portrait stills)

Design lock: `docs/portraits-v0121.md`. Prep: `tools/prep_portraits_v0121.py` (crop + transparent BG + circular alpha only). Art note: `docs/art-audio/PORTRAITS_v0.1.21.md` (rust iron Seal / coal body Ash).

| Asset | Bytes | md5 | Role |
|-------|------:|-----|------|
| `portrait_you.png` | 57759 | `ed5064c12745ad7f149f9a313b673819` | Title + combat You |
| `portrait_ash_warden.png` | 79797 | `4694b96d0f5355c878f7acebdb0141a6` | F2 Ash-Warden (coal) |
| `portrait_seal_warden.png` | 101554 | `1259b05113315ed78fc40630efc42d61` | F1 Seal-Warden (rust) **NEW** |

MD5s **MATCH** `BodyArt.PLAYER_MD5` / `ASH_WARDEN_MD5` / `SEAL_WARDEN_MD5` contract.

**Hallway / trash still Canvas circles:** `EnemySilhouette` else-branch `drawCircle` (HeroShowcase.kt:153–176); `BodyArt.enemyPortraitDrawableName` null for GOBLIN/ORC/TROLL/SPIDER; `hallwayEnemyUsesPlaceholder() == true`. No new trash stills in tip.

**Wake frozen (md5 MATCH prior v0.1.20 gate):**

| Frozen | md5 |
|--------|-----|
| `wake_vfx_charge.png` | `699ec4bf4539ed2e09922e5870d34c35` |
| `wake_vfx_slash.png` | `0ae68a58e7f3a2034cdfbd2a206ca85b` |
| `wake_vfx_impact.png` | `8caf0a1c282f99e0ce2937a6aba82c17` |
| `ashbrand_spark.png` | `36a10d0d431c029aea23b08bf8e25cfc` |

---

## D) Unit tests

Commands (measured this gate):

- `./gradlew :app:testDebugUnitTest --tests '*PortraitsV0121*' --tests '*VolumeArtV0120*' --tests '*SkillGlyphV0119*' --tests '*BodyArtV0118*' --tests '*WakeArtV0117*' --tests '*CombatSpeedV0111*' --tests '*MidRunSaveV0112*' --tests '*StatusPipsV0113*' --tests '*BraceSyncV0114*'` → **BUILD SUCCESSFUL**
- `./gradlew :app:testDebugUnitTest` → **BUILD SUCCESSFUL**

| Suite | Result |
|-------|--------|
| PortraitsV0121Test | **7/7 PASS** |
| VolumeArtV0120Test | **11/11 PASS** |
| SkillGlyphV0119Test | **8/8 PASS** |
| BodyArtV0118Test | **6/6 PASS** |
| WakeArtV0117Test | **4/4 PASS** |
| CombatSpeedV0111Test | **5/5 PASS** |
| MidRunSaveV0112Test | **8/8 PASS** |
| StatusPipsV0113Test | **8/8 PASS** |
| BraceSyncV0114Test | **7/7 PASS** |
| **Full `:app:testDebugUnitTest`** | **127/127 PASS** (21 classes, 0 fail/err/skip) |

### PortraitsV0121Test methods

1. `threePortraitDrawables_present` — PASS  
2. `you_usesPortraitYou_titleAndCombat` — PASS  
3. `ashWarden_f2Boss_usesAshPortrait` — PASS  
4. `sealWarden_dragon_f1Boss_usesSealPortrait` — PASS  
5. `trashWretchAndSealSpinner_noPortraitMapping` — PASS  
6. `spriteInset_shrinksUnderChrome_noPipMove` — PASS  
7. `volumeSurfaces_doNotClaimSealPortrait` — PASS  

---

## E) Source (cases 1–6) vs `docs/portraits-v0121.md`

**Same You drawable (title + combat):**

- Title: `MainMenuScreen.kt:46` → `HeroShowcase(...)`.
- Combat: `CombatScreen.kt:201` → `HeroShowcase(...)`.
- Both load `R.drawable.portrait_you` (HeroShowcase.kt:78–79); `BodyArt.PLAYER_DRAWABLE = "portrait_you"` (BodyArt.kt:12). Tests: `you_usesPortraitYou_titleAndCombat`.

**F1 gate = rust Seal-Warden:**

- `Enemy.boss(floor=1)` → `EnemyKind.DRAGON` (Enemy.kt:31–34); displayName `"Seal-Warden"`; asset `"portrait_seal_warden"` (Enemy.kt:15).
- `BodyArt.enemyPortraitDrawableName(DRAGON) → SEAL_WARDEN_DRAWABLE` (BodyArt.kt:51–53).
- `EnemySilhouette` → `R.drawable.portrait_seal_warden` (HeroShowcase.kt:93, 134–152; no volume).
- Art note: seal still keeps rust iron / rune ring / lava core (`PORTRAITS_v0.1.21.md:24`). Tests: `sealWarden_dragon_f1Boss_usesSealPortrait`, BodyArtV0118 `f1SealWarden_dragon_usesSealPortrait_v0121`.

**F2 gate = coal Ash-Warden:**

- `Enemy.boss(floor>=2)` → `EnemyKind.ASH_WARDEN` (Enemy.kt:33); asset `"portrait_ash_warden"`.
- `BodyArt` → `ASH_WARDEN_DRAWABLE`; EnemySilhouette volume branch (HeroShowcase.kt:118–133).
- Art note: ash still keeps coal body / gold cracks / chest seal (`PORTRAITS_v0.1.21.md:23`). Tests: `ashWarden_f2Boss_usesAshPortrait`.

**Hallway enemies still circles:**

- Trash kinds map to null portrait → Canvas `drawCircle` placeholder (HeroShowcase.kt:153–176).
- `BodyArt.hallwayEnemyUsesPlaceholder() == true` (BodyArt.kt:63). Tests: `trashWretchAndSealSpinner_noPortraitMapping`, BodyArtV0118 `trashAndSealSpinner_stayPlaceholders` / `hallway_youIsSoldier_enemyStaysPlaceholder`.

**Wake slash reads over new art:**

- Portrait Box: Row (HeroShowcase / EnemySilhouette + pips) then `WakeStageOverlay` drawn after in same `Box` with `align(TopCenter)` height 140.dp (CombatScreen.kt:198–231) → overlay paints on top of portraits.
- `WakeStageOverlay` loads frozen `wake_vfx_slash` etc. (WakeStageOverlay.kt:26–38); wake PNG md5s MATCH prior gate; WakeStageOverlay.kt / WakeArt not in tip.
- Shrink via `SPRITE_INSET_FRACTION` only — pips/HP/Wake chrome not moved (HeroShowcase.kt:71–75, 119–120, 136).

**Frozen (Wake / glyphs / 2x / save / pips / path / math / trash):**

- Tip file list excludes those domain sources; wake md5s frozen; VolumeArt surfaces still exclude `portrait_seal_warden` (`volumeSurfaces_doNotClaimSealPortrait`).
- Frozen regression suites all green (VolumeArtV0120, SkillGlyphV0119, BodyArtV0118, WakeArtV0117, CombatSpeedV0111, MidRunSaveV0112, StatusPipsV0113, BraceSyncV0114) + full **127/127**.

---

## Cases 1–6

| # | Claim | Verdict |
|---|-------|---------|
| 1 | Title + combat You are the same soldier | **PASS** — both `HeroShowcase` → `portrait_you`; `you_usesPortraitYou_titleAndCombat` |
| 2 | Floor 1 gate = rust Seal-Warden | **PASS** — F1 boss `DRAGON` → `portrait_seal_warden`; rust still per Art note; `sealWarden_dragon_f1Boss_usesSealPortrait` |
| 3 | Floor 2 gate = coal Ash-Warden | **PASS** — F2 boss `ASH_WARDEN` → `portrait_ash_warden`; coal still per Art note; `ashWarden_f2Boss_usesAshPortrait` |
| 4 | Hallway enemies still circles | **PASS** — trash null mapping → Canvas `drawCircle`; `hallwayEnemyUsesPlaceholder`; trash tests |
| 5 | Wake slash still reads over the new art | **PASS** — `WakeStageOverlay` after portrait Row in Box (CombatScreen.kt:222–230); wake md5 frozen; inset shrink only |
| 6 | Wake/glyphs/2x/save/pips/path/math/trash frozen | **PASS** — frozen sources absent from tip; wake md5 MATCH; frozen suites + full 127/127; trash Canvas |

---

## Docs

- Spec: `docs/portraits-v0121.md`
- Art note: `docs/art-audio/PORTRAITS_v0.1.21.md`
- This gate: `docs/qa-gate-v0121-portraits-2026-09-24.md`

---

## Overall

**Gate PASS** — tip `41f99dd493cec96f9225b23ad00e1d6c273f8b41` (== origin/main == claimed); APK MATCH 18213891 / `9b4125ee330a2ed0ea419462d00da121`; PortraitsV0121 **7/7**; full suite **127/127**; cases 1–6 PASS.
