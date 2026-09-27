# QA Gate — v0.1.52-slashlayer (2026-09-27)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.52-slashlayer  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-27 ~14:24–14:26 EDT  
**Engineer tag:** Do **not** tag / gh release until CoS green after this gate.

**Tip (HEAD / origin/main):** `fcf9738a2ef0feaf0d2228b718b0e32835c23e9c`  
**Claimed tip:** `fcf9738a2ef0feaf0d2228b718b0e32835c23e9c` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `fcf9738a2ef0feaf0d2228b718b0e32835c23e9c` |
| origin/main | `fcf9738a2ef0feaf0d2228b718b0e32835c23e9c` |
| Match claimed tip | **YES** (HEAD == origin/main == claimed) |
| Subject | `feat(sfx): v0.1.52-slashlayer — swing+impact layer, heavier Wake clash` |
| Author / date | Android Engineer <android-engineer@tod.local> — 2026-09-27 14:23:24 -0400 |

**`--stat` (fcf9738):** IronclashSfx.kt (TAG `v0.1.52-slashlayer`; swing+impact expand; Wake clash → Critical_Hit_Stinger_01_wake380; SWING_LEAD_MS=60), SoundBus.kt (Handler delay + `Log.d("SFX", …)` role/file), SlashlayerV0152Test (renamed from IronclashSfxV0151Test; **15** tests), runtime + curated `IRONCLASH_01…plus3db` / `…Light_08` / `…Critical_Hit_Stinger_01_wake380` oggs, docs/slashlayer-v0152.md + art-audio/IRONCLASH_SFX_v0.1.52.md, versionCode **53** / versionName **0.1.52-slashlayer**. **15 files, +238/−55.** Tip does **not** touch GameController / CombatEngine (playFrame wiring + sound keys already from 0.1.51).

**Combat FX LOCK (HARD):** tip `git diff fcf9738^..fcf9738` on `CombatFx.kt` / `PlumeGarnishKit.kt` = **0 lines**. CombatFx + PlumeGarnishKit TAG remain **`v0.1.50-puffhold`**. Tip is audio-only for combat SFX layering (no stroke / puff / PLUME / Kenney particles / CombatFx art retune).

**Frozen (not broken by tip):** F3 / Hub / Wake art / Brace pips / stroke crescent / ash-puff holds; pack zips under `/workspace/tod-sfx-v0151/` not committed. Leftover APK assets `IRONCLASH_23_Flesh_Hit_Light_05.ogg` + `IRONCLASH_03_Sword_Clash_04_wake380.ogg` still sit beside new files — **map points at Light_08 / Critical_01** (HARD LOCK: do not fail solely on leftover Light_05).

**WT:** `?? docs/qa-gate-v0150-puffhold-2026-09-26.md` (untracked prior gate doc only; tip tree clean vs origin/main for WO files). This gate doc written as `docs/qa-gate-v0152-slashlayer-2026-09-27.md` (untracked at write time).

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | **42316309** | 2026-09-27 14:23 EDT | `34eb6425af161facc663578694d4fc88` |
| `app/build/outputs/apk/debug/app-debug.apk` | 42316309 | 2026-09-27 14:23 EDT | `34eb6425af161facc663578694d4fc88` (**IDENTICAL** to root; `cmp` OK) |

Claimed: **`34eb6425af161facc663578694d4fc88`** → **MATCH** (root APK). Real `assembleDebug` deliverable (root == build outputs).

**aapt badging (root):** `versionCode='53' versionName='0.1.52-slashlayer'` — matches `app/build.gradle.kts` and `output-metadata.json`.

**Cert:** apksigner — `C=US, O=Android, CN=Android Debug`; debug.keystore valid **2026-09-16 16:53 EDT → 2056-09-08**; SHA-256 `39e6fc727abe826f26c0e96932f49df626657e615ff47d652de7bdec1a500497` (colon form `39:E6:FC:72:…:04:97`). Same Android Debug cert family as 1.48–1.51 (from 9/16/26). Claimed cert SHA-256 → **MATCH**.

Prior v0.1.51 root APK was **42295854** (+20455). Size delta consistent with three new/replaced oggs (+ swing/impact/wake380) audio-only tip.

**APK assets (exact filenames under `assets/audio/…`):**

| Role | APK path | size | md5 (from APK bytes) |
|------|----------|-----:|----------------------|
| slash_swing | `assets/audio/ironclash/IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg` | 5959 | `fde6b48d4dc8e0c305a28e6008fd3cea` |
| slash_impact | `assets/audio/ironclash/IRONCLASH_23_Flesh_Hit_Light_08.ogg` | 5808 | `54e2c70b9d992a20b6c50c0da6ff3146` |
| wake_clash | `assets/audio/ironclash/IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg` | 8002 | `d6186f00f278f7f0f13f99f06ed9a19c` |
| brace | `assets/audio/ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg` | 6118 | `a0350366bc961ea7e65e050a0a70a8dd` |
| dice | `assets/audio/sfx_dice.wav` | 7134 | `bb79aeb87d01ee09c32fe463c1fb4f13` |
| sting | `assets/audio/sfx_legendary_sting.wav` | 30948 | `c11ecd511963d7d86f7d6fbee44ddcd8` |
| miss | `assets/audio/sfx_miss.wav` | 9678 | `9da3ed902bef3e3c1c4d3285f0ec893b` |

Also present (superseded, not mapped): `IRONCLASH_23_Flesh_Hit_Light_05.ogg` (5527), `IRONCLASH_03_Sword_Clash_04_wake380.ogg` (6034) — allowed leftover per HARD LOCK.

**Durations (ffprobe):** swing **0.197s**, impact **0.189s**, wake380 **0.380s** (≤400ms), brace **0.351s**.

No `.zip` under APK `assets/`. Tip tree: no `.zip` / no `tod-sfx` tracked (packs remain under `/workspace/tod-sfx-v0151/` only).

---

## C) Stroke / Combat FX locks (HARD — must be unchanged)

| Check | Result |
|-------|--------|
| Tip touches CombatFx / PlumeGarnishKit / Cleave / Wake overlays | **NO** — `git diff fcf9738^..fcf9738` on CombatFx.kt + PlumeGarnishKit.kt = **0 lines** |
| `STROKE_SHAPE_FILLED_CRESCENT` | **true** |
| `STROKE_CRESCENT_BOW_MULT` / `STROKE_CRESCENT_INNER_BOW_FRAC` | **1.20** / **0.38** |
| `STROKE_CORE_BUST_FRAC_*` | **0.14 / 0.18 / 0.10** |
| Peaks | player **500ms** / enemy **450ms** |
| `COLOR_STROKE_YOU` / `ENEMY` | **0xFFFFF6E4** / **0xFFC4B8A8** |
| Plume `PUFF_MS` / `EMBER_MS` / `SPARK_MS` | **450** / **450** / **450** (still puffhold windows) |
| CombatFx TAG / PlumeGarnishKit TAG | still **`v0.1.50-puffhold`** (not retuned by tip; last touch `7bca621` puffhold) |
| IronclashSfx TAG | **`v0.1.52-slashlayer`** (audio only) |
| PuffholdV0150Test / StrokeBothV0148Test | **7/0** / **7/0** |

---

## D) Unit tests (actual counts — QA measured)

**Gradle:** `./gradlew :app:testDebugUnitTest --rerun-tasks --offline` → **BUILD SUCCESSFUL** in **21s** (22 tasks executed). Real AGP unit-test path. XML under `app/build/test-results/testDebugUnitTest/` (**48** `TEST-*.xml`). HTML counters: **378** / **0** / **0** (ignored).

| Suite | tests / fail / err / skip |
|-------|---------------------------|
| **Full** | **48 classes / 378 / 0 / 0 / 0** |
| **SlashlayerV0152Test** | **15 / 0 / 0 / 0** |
| IronclashSfxV0151Test | **ABSENT** (renamed → SlashlayerV0152Test; superseded) |
| PuffholdV0150Test | 7 / 0 / 0 / 0 |
| StrokeBothV0148Test | 7 / 0 / 0 / 0 |
| StrokeThickV0147Test | 7 / 0 / 0 / 0 |
| StrokeFallbackV0146Test | 8 / 0 / 0 / 0 |
| SlashScaleV0145Test | 11 / 0 / 0 / 0 |
| SlashProofV0144Test | 13 / 0 / 0 / 0 |
| SlashReadV0143Test | 12 / 0 / 0 / 0 |
| CleaveKitV0142Test | 13 / 0 / 0 / 0 |
| Floor3V0141Test | 10 / 0 / 0 / 0 |
| CombatFxFixV0140Test | 6 / 0 / 0 / 0 |
| CombatFxReadV0139Test | 9 / 0 / 0 / 0 |
| CombatFxAimV0138Test | 11 / 0 / 0 / 0 |
| CombatFxV0137Test | 13 / 0 / 0 / 0 |
| HubV0127Test / HubKeep / HubMore | 9 / 7 / 8 — all 0 fail |

Prior v0.1.51 full = 48/374 (IronclashSfxV0151Test **11**); tip renames + expands suite → Slashlayer **15** → **48/378** (= 374 − 11 + 15). Measured match.

Slashlayer methods (all ok): `tag_isSlashlayerV0152`, `swingLeadMs_inBand40to80`, `roleToFilename_mapExact`, `loadPaths_fixedRootNotSfxSubdir`, `loadMap_keysIncludeSwingImpactWakeBraceEmberSoftenUiDiceStingMiss`, `runtimeAssets_existUnderAudio`, `curatedSource_md5MatchesManifest`, `sameFrame_impactExpandsToSwingThenImpactWithLead`, `sameFrame_impactWins_duckGarnish`, `sameFrame_legendaryLayersStingAndWakeClash_ducksEmber`, `braceAlone_noClash`, `softenSolo_midVolume`, `blankSounds_dropped`, `garnishSet_emberAndSoftenOnly`, `uiCallSites_documentedForCase7`.

---

## E) Cases 1–9

### 1) Ash Press / Hostflint / Ruin Seal sound like a cut (swing+impact), not a tap — **PASS**
`CombatEngine.resolveCard` sets `sound = "impact"` for `SkillEffect.Damage` (Hostflint / Ruin Seal) and `DamageAndHeal` (Ash Press). `IronclashSfx.resolveFrame` expands `impact` → `slash_swing` @ delay 0 + `impact` @ `SWING_LEAD_MS=60` (band 40–80), both `VOL_FULL=0.80`. Paths: `audio/ironclash/IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg` then `…/IRONCLASH_23_Flesh_Hit_Light_08.ogg`. `GameController.playLogSounds` → `sound.playFrame` (Handler schedules lead). Locked by `sameFrame_impactExpandsToSwingThenImpactWithLead` + `roleToFilename_mapExact` + APK md5s. Leftover Light_05 in APK is **not** mapped.

### 2) Incoming Shiv/Hit same cut, quieter (~0.8) — **PASS**
Enemy kit damage events use `sound = "impact"` (`CombatEngine` foe hit paths ~L326 / L349). Same `resolveFrame` swing+impact expand; volume `VOL_FULL=0.80` (~0.8). Same Light_09 + Light_08 files. No separate quieter mixer — design “~0.8” matches `VOL_FULL`.

### 3) Wake = sting + metal crash; sting still recognizable; clash ≤400ms — **PASS**
Full Wake `sound = "legendary"`. `resolveFrame` → `(legendary, 0.80)` + `(wake_clash, 0.70)`. Paths: `audio/sfx_legendary_sting.wav` + `audio/ironclash/IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg`. ffprobe wake clash **0.380s ≤ 400ms**. Locked by `sameFrame_legendaryLayersStingAndWakeClash_ducksEmber`. Sting remains `KEY_LEGENDARY` → `PATH_STING` (lead); clash is under-layer.

### 4) Dice still sounds like dice — **PASS**
`PATH_DICE = audio/sfx_dice.wav` (not `audio/sfx/…`). Dice tumble event `sound = "dice"`. APK `assets/audio/sfx_dice.wav` present (7134 bytes). Locked by `loadPaths_fixedRootNotSfxSubdir` + `runtimeAssets_existUnderAudio`.

### 5) No muddy stack when Wake + spark fire (ember ducked) — **PASS**
`VOL_GARNISH_DUCK=0.35`. Legendary+ember same frame → sting full + wake_clash 0.70 + ember **0.35**. Locked by `sameFrame_legendaryLayersStingAndWakeClash_ducksEmber`. Spark alone still `ember` @ full when no legendary/impact.

### 6) Brace gain = shield block only (no clash) — **PASS**
Brace GAIN → `sound = "brace"`; `PATH_BRACE = audio/ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg`. `resolveFrame(["brace"])` size 1, no `wake_clash`. Locked by `braceAlone_noClash` + role/path tests. APK md5 `a0350366…` (unchanged from 0.1.51).

### 7) Miss + legendary sting still present — **PASS**
`LOAD_MAP` keeps `KEY_MISS` → `audio/sfx_miss.wav`, `KEY_LEGENDARY` → `audio/sfx_legendary_sting.wav`. Defeat event `sound = "miss"`; Wake `sound = "legendary"`. Both wavs in APK (9678 / 30948). Locked by loadMap + runtimeAssets tests.

### 8) Notes/docs name the four files — **PASS**
`docs/slashlayer-v0152.md` + `docs/art-audio/IRONCLASH_SFX_v0.1.52.md` both list exact:
`slash_swing=IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg`,
`slash_impact=IRONCLASH_23_Flesh_Hit_Light_08.ogg`,
`wake_clash=IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg`,
`brace=IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg`.
No store zips in tip/APK.

### 9) Optional: log `SFX swing|impact|clash|sting <file>` — **PASS**
`AssetSoundBus.logPlay`: `Log.d("SFX", "$role $clip")` with roles `swing` / `impact` / `clash` / `sting` and filenames from `FILE_*`. Present in tip SoundBus.kt (not absent).

---

## F) HARD FAIL checks / Fail blockers

| HARD FAIL if | Measured |
|--------------|----------|
| Tip retunes CombatFx stroke / puff / Wake art / math | **NO** — CombatFx/Plume untouched (0-line tip diff); TAG still puffhold |
| Map still points at Light_05 / Clash_04 for impact/wake | **NO** — map = Light_08 + Critical_01_wake380 |
| Dice/sting/miss still `audio/sfx/sfx_*` | **NO** — fixed `audio/sfx_*.wav` |
| Store zips committed / in APK assets | **NO** |
| versionCode/Name wrong / wrong cert | **NO** — 53 / 0.1.52-slashlayer; claimed Debug cert SHA-256 |
| Swing lead outside 40–80 / Wake clash >400ms | **NO** — SWING_LEAD_MS=60; clash ffprobe 380ms |
| Unit suite red | **NO** — 378/0 |

**FAIL blockers:** none

---

## G) Verdict

**PASS** — tip SHA match, APK size+md5 match, packaging **OK** (real assembleDebug; versionCode **53** / versionName **0.1.52-slashlayer**; Android Debug cert from 9/16/26; four IRONCLASH + dice/sting/miss in APK), suite **378/0** (SlashlayerV0152Test **15/0**; Puffhold/Stroke*/prior FX green; IronclashSfxV0151Test superseded/absent), Combat FX LOCK held (0-line tip diff; TAG puffhold), cases **1–9 all PASS**. Do **not** tag/release from this gate until CoS green.

**Doc path:** `docs/qa-gate-v0152-slashlayer-2026-09-27.md`

**FAIL blockers:** none

**Method note:** Gradle `./gradlew :app:testDebugUnitTest --rerun-tasks --offline` succeeded (real AGP unit-test path). Counts from Gradle XML under `app/build/test-results/testDebugUnitTest/` + HTML index (48 classes / 378 tests / 0 fail / 0 err / 0 skip). APK proof via aapt badging, apksigner cert SHA-256 + debug.keystore validity, unzip asset listing + md5 of extracted bytes, `cmp` root APK == `app/build/outputs/apk/debug/app-debug.apk`. Combat FX lock via tip `git diff` line count on CombatFx/Plume + constant/TAG reads + Puffhold/Stroke suite green. Wake clash duration via ffprobe.

**Screenshot paths:** none (unit/code/APK proof gate; phone listening optional / not required; no device captures this run).

**WT notes:** untracked `docs/qa-gate-v0150-puffhold-2026-09-26.md` at gate time; this gate doc written as `docs/qa-gate-v0152-slashlayer-2026-09-27.md`.
