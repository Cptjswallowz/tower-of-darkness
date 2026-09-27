# QA Gate — v0.1.51-ironclash (2026-09-27)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.51-ironclash  
**Gate result:** **FAIL**  
**Measured (EDT):** 2026-09-27 ~11:02–11:04 EDT  
**Engineer tag:** Do **not** tag / gh release until CoS green after this gate.

**Tip (HEAD / origin/main):** `d01f675a0313a10d5e811fd8419f652c0c40d9b8`  
**Claimed tip:** `d01f675a0313a10d5e811fd8419f652c0c40d9b8` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `d01f675a0313a10d5e811fd8419f652c0c40d9b8` |
| origin/main | `d01f675a0313a10d5e811fd8419f652c0c40d9b8` |
| Match claimed tip | **YES** (HEAD == origin/main == claimed) |
| Subject | `feat(sfx): v0.1.51-ironclash — IRONCLASH/Lentikula/Kenney one-shots` |
| Author / date | Android Engineer <android-engineer@tod.local> — 2026-09-27 11:01:39 -0400 |

**`--stat` (d01f675):** IronclashSfx.kt (new; TAG `v0.1.51-ironclash`; role→path + `resolveFrame` duck), SoundBus.kt (`playFrame` + LOAD_MAP from IronclashSfx; paths `audio/…` not `audio/sfx/…`), GameController.kt (`playLogSounds` over all new log events), CombatEngine.kt (presentation sound remaps: hit→impact, brace/soften/ember/legendary keys; no combat math), IronclashSfxV0151Test (11), runtime `app/src/main/assets/audio/{ironclash,lentikula,kenney_ui}/` + curated `assets/sfx/…`, docs/ironclash-v0151.md + art-audio/IRONCLASH_SFX_v0.1.51.md, versionCode **51** / versionName **0.1.51-ironclash**. **29 files, +575/−52.**

**Combat FX LOCK (HARD):** tip does **not** touch `CombatFx.kt` / `PlumeGarnishKit.kt` / Cleave / Wake overlays / stroke / PLUME / Kenney particle art / math. CombatFx + PlumeGarnishKit TAG remain **`v0.1.50-puffhold`**. Tip is audio-only for FX surface.

**Frozen (not broken by tip):** F3 / Hub / Wake art / Brace pips / stroke crescent / ash-puff holds; pack zips under `/workspace/tod-sfx-v0151/` not committed.

**WT:** `?? docs/qa-gate-v0150-puffhold-2026-09-26.md` (untracked prior gate doc only; tip tree clean vs origin/main for WO files).

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | **42295854** | 2026-09-27 11:01 EDT | `ee314dc9ac664bb62b5f7700e1b2a736` |
| `app/build/outputs/apk/debug/app-debug.apk` | 42295854 | 2026-09-27 11:01 EDT | `ee314dc9ac664bb62b5f7700e1b2a736` (**IDENTICAL** to root) |

Claimed: **42295854** / `ee314dc9ac664bb62b5f7700e1b2a736` → **MATCH** (root APK). Real `assembleDebug` deliverable (root == build outputs).

**aapt badging (root):** `versionCode='51' versionName='0.1.51-ironclash'` — matches `app/build.gradle.kts` and `output-metadata.json`.

**Cert:** apksigner v2 — `C=US, O=Android, CN=Android Debug`; debug.keystore valid **2026-09-16 16:53 EDT → 2056-09-08**; SHA256 `39:E6:FC:72:7A:BE:82:6F:26:C0:E9:69:32:F4:9D:F6:26:65:7E:61:5F:F4:7D:65:2D:E7:BD:EC:1A:50:04:97`. Same Android Debug cert family as 1.48 / 1.49 / 1.50 (from 9/16/26).

Prior v0.1.50 root APK was **42299540** (−3686). Size delta consistent with audio-only tip (small oggs; no new drawables).

**APK assets (exact filenames under `assets/audio/…`):**

| Role | APK path | size | md5 (from APK bytes) |
|------|----------|-----:|----------------------|
| impact | `assets/audio/ironclash/IRONCLASH_23_Flesh_Hit_Light_05.ogg` | 5527 | `7f48fddc00f93dee839439fbd57f81bd` |
| wake clash | `assets/audio/ironclash/IRONCLASH_03_Sword_Clash_04_wake380.ogg` | 6034 | `5751cbefde0ae473f301abc846089231` |
| brace | `assets/audio/ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg` | 6118 | `a0350366bc961ea7e65e050a0a70a8dd` |
| ember | `assets/audio/lentikula/lentikula_fire_impact_5_short220.ogg` | 3984 | `1d24b7056b45b49d8897d48926874d19` |
| soften | `assets/audio/lentikula/lentikula_heal_impact_4_soften.ogg` | 8202 | `248fb43b6f65506649330185bcba0b03` |
| ui | `assets/audio/kenney_ui/click_001.ogg` | 4876 | `594234cc27bcfb903fdafc49cb728bc9` |
| dice | `assets/audio/sfx_dice.wav` | 7134 | (present; path **not** `audio/sfx/`) |
| sting | `assets/audio/sfx_legendary_sting.wav` | 30948 | present |
| miss | `assets/audio/sfx_miss.wav` | 9678 | present |

No `.zip` under APK `assets/`. Tip tree: no `.zip` / no `tod-sfx` tracked (packs remain under `/workspace/tod-sfx-v0151/` only).

---

## C) Stroke / Combat FX locks (HARD — must be unchanged)

| Check | Result |
|-------|--------|
| Tip touches CombatFx / PlumeGarnishKit / Cleave / Wake overlays | **NO** — `git diff d01f675^..d01f675` on those files = **0 lines** |
| `STROKE_SHAPE_FILLED_CRESCENT` | **true** |
| `STROKE_CRESCENT_BOW_MULT` / `STROKE_CRESCENT_INNER_BOW_FRAC` | **1.20** / **0.38** |
| `STROKE_CORE_BUST_FRAC_*` | **0.14 / 0.18 / 0.10** |
| Peaks | player **500ms** / enemy **450ms** |
| `COLOR_STROKE_YOU` / `ENEMY` | **0xFFFFF6E4** / **0xFFC4B8A8** |
| Plume `PUFF_MS` / `EMBER_MS` / `SPARK_MS` | **450** / **450** / **450** (still puffhold windows) |
| CombatFx TAG / PlumeGarnishKit TAG | still **`v0.1.50-puffhold`** (not retuned by tip) |
| IronclashSfx TAG | **`v0.1.51-ironclash`** (audio only) |
| PuffholdV0150Test / StrokeBothV0148Test | **7/0** / **7/0** |

---

## D) Unit tests (actual counts — QA measured)

**Gradle:** `./gradlew :app:testDebugUnitTest --rerun-tasks --offline` → **BUILD SUCCESSFUL** in ~11s (22 tasks executed). Real AGP unit-test path. XML under `app/build/test-results/testDebugUnitTest/` (**48** `TEST-*.xml`). HTML counters: **374** / **0** / **0** / **0**.

| Suite | tests / fail / err / skip |
|-------|---------------------------|
| **Full** | **48 classes / 374 / 0 / 0 / 0** |
| **IronclashSfxV0151Test** | **11 / 0 / 0 / 0** |
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

Prior v0.1.50 full = 47/363; tip **adds** IronclashSfxV0151Test **+11** → **48/374**. Measured match.

Ironclash methods (all ok): `tag_isIronclashV0151`, `roleToFilename_mapExact`, `loadPaths_fixedRootNotSfxSubdir`, `loadMap_keysIncludeImpactWakeBraceEmberSoftenUiDiceStingMiss`, `runtimeAssets_existUnderAudio`, `curatedSource_md5MatchesManifest`, `sameFrame_impactWins_duckGarnish`, `sameFrame_legendaryLayersStingAndWakeClash_ducksEmber`, `softenSolo_midVolume`, `blankSounds_dropped`, `garnishSet_emberAndSoftenOnly`.

---

## E) Cases 1–9

### 1) Stroke/slash land plays `IRONCLASH_23_Flesh_Hit_Light_05` (not old thin tick) — **PASS**
`IronclashSfx.PATH_IMPACT` = `audio/ironclash/IRONCLASH_23_Flesh_Hit_Light_05.ogg`. CombatEngine default / damage skills / Nip → `sound = "impact"` (was `"hit"`). LOAD_MAP loads that ogg. Present in source + APK (md5 `7f48fddc…`). Locked by `roleToFilename_mapExact` + `runtimeAssets_existUnderAudio` + `curatedSource_md5MatchesManifest`.

### 2) Wake = `sfx_legendary_sting` PLUS `IRONCLASH_03_Sword_Clash_04_wake380` under it — **PASS**
Full Wake event `sound = "legendary"`. `resolveFrame` expands `KEY_LEGENDARY` → `(legendary, 0.80)` + `(wake_clash, 0.70)`. Paths: `audio/sfx_legendary_sting.wav` + `audio/ironclash/IRONCLASH_03_Sword_Clash_04_wake380.ogg`. Locked by `sameFrame_legendaryLayersStingAndWakeClash_ducksEmber`.

### 3) Brace gain → `IRONCLASH_14_Shield_Block_Metal_02_brace350` — **PASS**
Brace GAIN events remapped to `sound = "brace"`; `PATH_BRACE` = `audio/ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg`. In APK + curated md5 `a0350366…`. Locked by role/path tests.

### 4) `sfx_dice` still dice; sting/miss kept (`audio/sfx_*.wav` NOT `audio/sfx/sfx_*`) — **PASS**
`PATH_DICE` / `PATH_STING` / `PATH_MISS` = `audio/sfx_dice.wav` / `audio/sfx_legendary_sting.wav` / `audio/sfx_miss.wav`. Assert `!PATH_DICE.contains("audio/sfx/")`. Prior SoundBus wrongly used `audio/sfx/sfx_*.wav` (missing); tip fixed. Locked by `loadPaths_fixedRootNotSfxSubdir`. APK lists files at `assets/audio/sfx_*.wav`.

### 5) Same-frame Wake+spark: Impact/legendary full; ember/soften ducked (~0.35) — **PASS**
`VOL_FULL=0.80`, `VOL_GARNISH_DUCK=0.35`, `VOL_WAKE_CLASH=0.70`. `playLogSounds` → `playFrame` over **all** new log events (not `lastOrNull`). Impact+soften → soften **0.35**; legendary+ember → sting full + wake clash + ember **0.35**. Soften solo `0.62` ∈ 0.55–0.70. Locked by `sameFrame_*` + `softenSolo_midVolume`. GameController tip delta is playFrame wiring only.

### 6) Soften → `lentikula_heal_impact_4_soften`; Ashbrand/Wake tip → `lentikula_fire_impact_5_short220` — **PASS**
`PATH_SOFTEN` = `audio/lentikula/lentikula_heal_impact_4_soften.ogg`; `PATH_EMBER` = `audio/lentikula/lentikula_fire_impact_5_short220.ogg`. Engine: Soften apply → `"soften"`; Ashbrand spark → `"ember"`. Both in APK with art-doc md5s. Locked by role/path + curated md5 tests.

### 7) UI node/shop/loadout/Continue → kenney `click_001` — **FAIL**
**Asset mapping OK:** `KEY_UI` → `audio/kenney_ui/click_001.ogg` (APK present, md5 `594234cc…`); `sound.play("ui")` loads Kenney click (prior tip had **no** ui asset — tone only).

**Call-site evidence:**
| Trigger (design) | Code | plays `ui`? |
|------------------|------|-------------|
| shop confirm | `GameController.buyOffer` L1023 | **YES** |
| loadout lock | `GameController.confirmLoadout` L405 | **YES** |
| node tap (path enter) | `selectPathNode` / `enterNode` | **NO** — no `sound.play("ui")` |
| Continue | `continueClimb` L222 | **NO** — no `sound.play("ui")` |

Fogged-node / scout / rumor / hub unlock / treasure **do** play `"ui"` (extra sites), but design-listed **path node enter** and **Continue** are unwired. IronclashSfxV0151Test does not cover GameController UI call sites.

### 8) Combat FX LOCKED — stroke/PLUME/Kenney particles/Wake art/math unchanged — **PASS**
Tip file list = audio assets + IronclashSfx/SoundBus/GameController playFrame + CombatEngine **sound string remaps only** + docs/version. CombatFx stroke constants + Plume PUFF/EMBER/SPARK holds unchanged; TAG still puffhold; Puffhold **7/0** + StrokeBoth **7/0** + prior Slash*/Cleave/CombatFx* green. **HARD FAIL not triggered.**

### 9) Notes list exact filenames; no store zips in APK/repo tip — **PASS**
Design + art doc list exact filenames (table above). Tip: `git ls-tree` no `.zip` / no `tod-sfx`. APK: no `assets/**/*.zip`. Workspace packs only under `/workspace/tod-sfx-v0151/` (IRONCLASH_v1.0.zip, Basic_Spell_Impacts.zip, Healing…7z, kenney_interface-sounds.zip) — not committed.

---

## F) HARD FAIL checks

| HARD FAIL if | Measured |
|--------------|----------|
| Tip retunes CombatFx stroke / puff / Wake art / math | **NO** — CombatFx/Plume untouched; audio-only tip |
| Impact still old thin tick / wrong file | **NO** — IRONCLASH_23_Flesh_Hit_Light_05.ogg wired + in APK |
| Dice/sting/miss still `audio/sfx/sfx_*` | **NO** — fixed to `audio/sfx_*.wav` |
| Store zips committed / in APK assets | **NO** |
| versionCode/Name wrong / wrong cert | **NO** — 51 / 0.1.51-ironclash; same Debug cert |
| UI Continue + path node play Kenney click | **YES — FAIL case 7** (shop/loadout only) |

---

## G) Verdict

**FAIL** — tip SHA match, APK size+md5 match, packaging **OK** (real assembleDebug; versionCode **51** / versionName **0.1.51-ironclash**; Android Debug cert from 9/16/26; all required ogg/wav in APK), suite **374/0** (IronclashSfxV0151Test **11/0**; Puffhold/Stroke*/prior FX green), Combat FX LOCK held, cases **1–6, 8–9 PASS**, case **7 FAIL** (Continue + path node enter do not `play("ui")` despite design). Do **not** tag/release from this gate.

**Doc path:** `docs/qa-gate-v0151-ironclash-2026-09-27.md`

**FAIL blockers:**
1. **Case 7:** `continueClimb()` never calls `sound.play("ui")` — menu Continue silent (no Kenney click).
2. **Case 7:** `selectPathNode` / `enterNode` never call `sound.play("ui")` — path node enter silent (design lists “node tap”).

**Method note:** Gradle `./gradlew :app:testDebugUnitTest --rerun-tasks --offline` succeeded (real AGP unit-test path). Counts from Gradle XML under `app/build/test-results/testDebugUnitTest/` + HTML index (48 classes / 374 tests / 0 fail / 0 err / 0 skip). APK proof via aapt badging, apksigner cert (v2) + debug.keystore validity, unzip asset listing + md5 of extracted bytes, `cmp` root APK == `app/build/outputs/apk/debug/app-debug.apk`. Combat FX lock via tip `git diff` name-only + constant/TAG reads + Puffhold/Stroke suite green.

**Screenshot paths:** none (unit/code/APK proof gate; no device captures this run).

**WT notes:** untracked `docs/qa-gate-v0150-puffhold-2026-09-26.md` only at gate time; this gate doc written as `docs/qa-gate-v0151-ironclash-2026-09-27.md`.


---

## FIX note (Android Engineer — case 7)

**Applied on tip after gate:** `continueClimb()` plays `sound.play("ui")` at start; `selectPathNode` plays `sound.play("ui")` once after validation (before loadout redirect or `enterNode`). `enterNode` remains silent (no double-fire with `confirmLoadout`). versionCode bumped to **52**, versionName stays `0.1.51-ironclash`. Combat FX LOCK untouched. Re-gate required for Continue + path node Kenney click.
