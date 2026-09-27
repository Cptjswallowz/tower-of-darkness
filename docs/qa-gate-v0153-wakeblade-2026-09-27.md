# QA Gate — v0.1.53-wakeblade (2026-09-27)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.53-wakeblade  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-27 ~14:40–14:41 EDT  
**Engineer tag:** Do **not** tag / gh release until CoS green after this gate.

**Tip (HEAD / origin/main):** `27c5c54c862b50c3a3ccd4e013325ea483c31ea4`  
**Claimed tip:** `27c5c54c862b50c3a3ccd4e013325ea483c31ea4` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `27c5c54c862b50c3a3ccd4e013325ea483c31ea4` |
| origin/main | `27c5c54c862b50c3a3ccd4e013325ea483c31ea4` |
| Match claimed tip | **YES** (HEAD == origin/main == claimed) |
| Subject | `feat(sfx): v0.1.53-wakeblade — Wake heavy swing+impact under sting` |
| Author / date | Android Engineer <android-engineer@tod.local> — 2026-09-27 14:39:34 -0400 |

**`--stat` (27c5c54):** IronclashSfx.kt (TAG `v0.1.53-wakeblade`; Wake → `wake_swing` + sting + `wake_impact`; unmapped from Clash/Critical; SWING_LEAD_MS=60; VOL_FULL≈0.80), SoundBus.kt (log roles `wake_swing`/`wake_impact`/`sting`), WakebladeV0153Test (**12** tests), SlashlayerV0152Test trimmed (wake-clash asserts removed; **13** remain), runtime + curated `IRONCLASH_02…Heavy_04_wake_plus3db` / `IRONCLASH_24…Heavy_04_wake_plus3db` oggs, docs/wakeblade-v0153.md + art-audio/IRONCLASH_SFX_v0.1.53.md, versionCode **54** / versionName **0.1.53-wakeblade**. **14 files, +379/−73.** Tip does **not** touch GameController / CombatEngine / CombatFx / PlumeGarnishKit.

**Combat FX LOCK (HARD):** tip `git diff 27c5c54^..27c5c54` on `CombatFx.kt` / `PlumeGarnishKit.kt` = **0 lines**. CombatFx + PlumeGarnishKit TAG remain **`v0.1.50-puffhold`** (last touch `7bca621`). Tip is audio-only (no stroke / puff / PLUME / Kenney particles / CombatFx / Wake art retune).

**Frozen (not broken by tip):** F3 / Hub / Wake art / Brace pips / stroke crescent / ash-puff holds; pack zips under `/workspace/tod-sfx-v0151/` not committed. Leftover APK assets `IRONCLASH_03_Sword_Clash_04_wake380.ogg`, `IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg`, `IRONCLASH_23_Flesh_Hit_Light_05.ogg` still sit beside new files — **ROLE map / LOAD_MAP / resolveFrame do NOT point Wake at Clash/Critical/Parry** (HARD LOCK: do not fail solely on leftover unmapped oggs).

**WT:** `?? docs/qa-gate-v0150-puffhold-2026-09-26.md` (untracked prior gate doc only; tip tree clean vs origin/main for WO files). Prior gate `docs/qa-gate-v0152-slashlayer-2026-09-27.md` already present. This gate doc written as `docs/qa-gate-v0153-wakeblade-2026-09-27.md` (untracked at write time).

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | **42331873** | 2026-09-27 14:39 EDT | `030108aa3d65657c48e8c383a23e8228` |
| `app/build/outputs/apk/debug/app-debug.apk` | 42331873 | 2026-09-27 14:39 EDT | `030108aa3d65657c48e8c383a23e8228` (**IDENTICAL** to root; `cmp` OK) |

Claimed: **`030108aa3d65657c48e8c383a23e8228`** → **MATCH** (root APK). Real `assembleDebug` deliverable (root == build outputs).

**aapt badging (root):** `versionCode='54' versionName='0.1.53-wakeblade'` — matches `app/build.gradle.kts` and `output-metadata.json`.

**Cert:** apksigner — `C=US, O=Android, CN=Android Debug`; debug.keystore valid **2026-09-16 16:53 EDT → 2056-09-08**; SHA-256 `39e6fc727abe826f26c0e96932f49df626657e615ff47d652de7bdec1a500497` (colon form `39:E6:FC:72:…:04:97`; abbrev `39e6fc72…1a500497`). Same Android Debug cert family as 1.48–1.52 (from 9/16/26). Claimed cert SHA-256 → **MATCH**.

Prior v0.1.52 root APK was **42316309** (+15564). Size delta consistent with two new heavy wake oggs (8059 + 7060 = 15119) + packaging overhead — audio-only tip.

**APK assets (exact filenames under `assets/audio/…`):**

| Role | APK path | size | md5 (from APK bytes) |
|------|----------|-----:|----------------------|
| wake_swing | `assets/audio/ironclash/IRONCLASH_02_Sword_Swing_Heavy_04_wake_plus3db.ogg` | 8059 | `cd0e6084ea89232334db391c8783f61e` |
| wake_impact | `assets/audio/ironclash/IRONCLASH_24_Flesh_Hit_Heavy_04_wake_plus3db.ogg` | 7060 | `c50e7db25867a193b28cedba44d62b4b` |
| slash_swing | `assets/audio/ironclash/IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg` | 5959 | `fde6b48d4dc8e0c305a28e6008fd3cea` |
| slash_impact | `assets/audio/ironclash/IRONCLASH_23_Flesh_Hit_Light_08.ogg` | 5808 | `54e2c70b9d992a20b6c50c0da6ff3146` |
| brace | `assets/audio/ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg` | 6118 | `a0350366bc961ea7e65e050a0a70a8dd` |
| dice | `assets/audio/sfx_dice.wav` | 7134 | `bb79aeb87d01ee09c32fe463c1fb4f13` |
| sting | `assets/audio/sfx_legendary_sting.wav` | 30948 | `c11ecd511963d7d86f7d6fbee44ddcd8` |
| miss | `assets/audio/sfx_miss.wav` | 9678 | `9da3ed902bef3e3c1c4d3285f0ec893b` |

Also present (superseded / leftover, **not** mapped to Wake): `IRONCLASH_03_Sword_Clash_04_wake380.ogg` (6034), `IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg` (8002), `IRONCLASH_23_Flesh_Hit_Light_05.ogg` (5527) — allowed leftover per HARD LOCK.

**Durations (ffprobe):** slash swing **0.197s**, slash impact **0.189s**, wake_swing **0.493s**, wake_impact **0.443s**, brace **0.351s**. Leftover clash/critical both **0.380s** (unmapped).

No `.zip` under APK `assets/`. Tip tree: no `.zip` / no `tod-sfx` tracked (packs remain under `/workspace/tod-sfx-v0151/` only).

---

## C) Stroke / Combat FX locks (HARD — must be unchanged)

| Check | Result |
|-------|--------|
| Tip touches CombatFx / PlumeGarnishKit / Cleave / Wake overlays | **NO** — `git diff 27c5c54^..27c5c54` on CombatFx.kt + PlumeGarnishKit.kt = **0 lines** |
| `STROKE_SHAPE_FILLED_CRESCENT` | **true** |
| `STROKE_CRESCENT_BOW_MULT` / `STROKE_CRESCENT_INNER_BOW_FRAC` | **1.20** / **0.38** |
| `STROKE_CORE_BUST_FRAC_*` | **0.14 / 0.18 / 0.10** |
| Peaks | player **500ms** / enemy **450ms** |
| `COLOR_STROKE_YOU` / `ENEMY` | **0xFFFFF6E4** / **0xFFC4B8A8** |
| Plume `PUFF_MS` / `EMBER_MS` / `SPARK_MS` | **450** / **450** / **450** (still puffhold windows) |
| CombatFx TAG / PlumeGarnishKit TAG | still **`v0.1.50-puffhold`** (not retuned by tip; last touch `7bca621` puffhold) |
| IronclashSfx TAG | **`v0.1.53-wakeblade`** (audio only) |
| PuffholdV0150Test / StrokeBothV0148Test | **7/0** / **7/0** |

---

## D) Unit tests (actual counts — QA measured)

**Gradle:** `./gradlew :app:testDebugUnitTest --rerun-tasks --offline` → **BUILD SUCCESSFUL** in **9s** (22 tasks executed). Real AGP unit-test path. XML under `app/build/test-results/testDebugUnitTest/` (**49** `TEST-*.xml`). HTML counters: **388** / **0** / **0** (ignored).

| Suite | tests / fail / err / skip |
|-------|---------------------------|
| **Full** | **49 classes / 388 / 0 / 0 / 0** |
| **WakebladeV0153Test** | **12 / 0 / 0 / 0** |
| **SlashlayerV0152Test** | **13 / 0 / 0 / 0** (still present; wake-clash asserts removed/trimmed from 15→13) |
| IronclashSfxV0151Test | **ABSENT** (superseded earlier) |
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

Prior v0.1.52 full = 48/378 (Slashlayer **15**); tip adds Wakeblade **12**, trims Slashlayer **15→13**, +1 class → **49/388** (= 378 − 15 + 13 + 12). Measured match.

Wakeblade methods (all ok): `tag_isWakebladeV0153`, `swingLeadMs_inBand40to80`, `roleToFilename_wakeSwingImpactNotClashOrCritical`, `loadMap_includesWakeSwingImpact_notWakeClash`, `loadPaths_wakeAndSlash`, `runtimeAssets_wakeAndSlashExist`, `curatedSource_wakeMd5MatchesManifest`, `sameFrame_legendary_wakeSwingThenStingAndWakeImpactTogether`, `sameFrame_legendary_ducksEmber`, `sameFrame_normalSlash_unchanged152Volumes`, `braceAlone_noWakeLayers`, `wakeFiles_notCriticalOrClashFilenames`.

---

## E) Cases 1–7

### 1) Ash Press / Ruin Seal / Hostflint still sound like cuts (1.52 pair) — **PASS**
`CombatEngine.resolveCard` sets `sound = "impact"` for skill damage / DamageAndHeal paths (Hostflint / Ruin Seal / Ash Press). `IronclashSfx.resolveFrame` expands `impact` → `slash_swing` @ delay 0 + `impact` @ `SWING_LEAD_MS=60`, both `VOL_FULL=0.80`. Paths: `audio/ironclash/IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg` then `…/IRONCLASH_23_Flesh_Hit_Light_08.ogg`. Locked by `sameFrame_normalSlash_unchanged152Volumes` + Slashlayer `sameFrame_impactExpandsToSwingThenImpactWithLead` + APK md5s. Leftover Light_05 in APK is **not** mapped.

### 2) Wake = heavier cut (heavy swing+impact), NOT a clang/clash/parry — **PASS**
Full Wake `sound = "legendary"`. `resolveFrame` → `wake_swing` @ 0 + (`legendary`/sting + `wake_impact`) @ `SWING_LEAD_MS=60`, all `VOL_FULL=0.80`. Paths: `IRONCLASH_02_Sword_Swing_Heavy_04_wake_plus3db.ogg` + `sfx_legendary_sting.wav` + `IRONCLASH_24_Flesh_Hit_Heavy_04_wake_plus3db.ogg`. Filenames are Sword_Swing_Heavy / Flesh_Hit_Heavy — not Clash/Parry/Critical. Locked by `sameFrame_legendary_wakeSwingThenStingAndWakeImpactTogether` + `wakeFiles_notCriticalOrClashFilenames` + `roleToFilename_wakeSwingImpactNotClashOrCritical`.

### 3) Legendary sting still audible on Wake — **PASS**
`LOAD_MAP[KEY_LEGENDARY] = PATH_STING` (`audio/sfx_legendary_sting.wav`). Legendary frame emits sting at lead with wake_impact (same delay). APK sting present (30948 bytes, md5 `c11ecd51…`). SoundBus logs role `sting`. Locked by `sameFrame_legendary_wakeSwingThenStingAndWakeImpactTogether` + `loadMap_includesWakeSwingImpact_notWakeClash`.

### 4) Incoming Hit/Seal Pulse quieter than player Wake — **PASS**
Enemy kit hit paths use `sound = "impact"` (CombatEngine ~L326 / L349) → Light_09 + Light_08 at `VOL_FULL=0.80`. Player Wake uses Heavy_04 pair with **+3 dB baked** (~1.4×) at same mixer `VOL_FULL=0.80` — Wake louder by design (no extra mixer gain). Locked by map/paths + docs loudness note + `sameFrame_normalSlash_unchanged152Volumes` vs Wake heavy files.

### 5) Docs name wake_swing / wake_impact / sting / slash files — **PASS**
`docs/wakeblade-v0153.md` + `docs/art-audio/IRONCLASH_SFX_v0.1.53.md` both list exact:
`wake_swing=IRONCLASH_02_Sword_Swing_Heavy_04_wake_plus3db.ogg`,
`wake_impact=IRONCLASH_24_Flesh_Hit_Heavy_04_wake_plus3db.ogg`,
`sting=sfx_legendary_sting`,
`slash_swing=IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg`,
`slash_impact=IRONCLASH_23_Flesh_Hit_Light_08.ogg`
(+ brace in art doc). No store zips in tip/APK.

### 6) Brace still shield only; dice+miss intact; ember ducked under Wake — **PASS**
Brace GAIN → `sound = "brace"`; `PATH_BRACE = …IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg`. `resolveFrame(["brace"])` size 1, no wake layers. Dice/miss keep `audio/sfx_dice.wav` / `audio/sfx_miss.wav` in LOAD_MAP + APK. Legendary+ember → ember `VOL_GARNISH_DUCK=0.35`. Locked by `braceAlone_noWakeLayers` + `sameFrame_legendary_ducksEmber` + runtimeAssets + Slashlayer brace/dice paths.

### 7) No Clash/Parry/Critical mapped to Wake in APK/code — **PASS**
`ROLE_TO_FILENAME` / `LOAD_MAP` / `resolveFrame` use only Heavy swing+impact + sting for Wake. Values scanned: no `Sword_Clash`, no `Critical_Hit`, no `wake380`, no `wake_clash` key. Brace Block file maps to **brace key only** (OK). Leftover Clash/Critical oggs in APK remain unmapped. Locked by `roleToFilename_wakeSwingImpactNotClashOrCritical` + `loadMap_includesWakeSwingImpact_notWakeClash` + `wakeFiles_notCriticalOrClashFilenames`.

---

## F) HARD FAIL checks / Fail blockers

| HARD FAIL if | Measured |
|--------------|----------|
| Tip retunes CombatFx stroke / puff / Wake art / math | **NO** — CombatFx/Plume untouched (0-line tip diff); TAG still puffhold |
| Wake ROLE/LOAD/resolveFrame points at Clash / Critical / Parry / Block (except brace→Block file) | **NO** — Wake → Heavy_04 swing+impact + sting only |
| Map still points normal slash at Light_05 | **NO** — slash = Light_09 + Light_08 |
| Dice/sting/miss still `audio/sfx/sfx_*` | **NO** — fixed `audio/sfx_*.wav` |
| Store zips committed / in APK assets | **NO** |
| versionCode/Name wrong / wrong cert | **NO** — 54 / 0.1.53-wakeblade; claimed Debug cert SHA-256 |
| Swing lead outside 40–80 / 1.52 slash volumes broken | **NO** — SWING_LEAD_MS=60; slash VOL_FULL=0.80 |
| Unit suite red | **NO** — 388/0 |

**FAIL blockers:** none

---

## G) Verdict

**PASS** — tip SHA match, APK size+md5 match, packaging **OK** (real assembleDebug; versionCode **54** / versionName **0.1.53-wakeblade**; Android Debug cert from 9/16/26; wake heavy + slash light + brace + dice/sting/miss in APK), suite **388/0** (WakebladeV0153Test **12/0**; SlashlayerV0152Test **13/0** still present; Puffhold/Stroke*/prior FX green), Combat FX LOCK held (0-line tip diff; TAG puffhold), cases **1–7 all PASS**. Do **not** tag/release from this gate until CoS green.

**Doc path:** `docs/qa-gate-v0153-wakeblade-2026-09-27.md`

**FAIL blockers:** none

**Method note:** Gradle `./gradlew :app:testDebugUnitTest --rerun-tasks --offline` succeeded (real AGP unit-test path). Counts from Gradle XML under `app/build/test-results/testDebugUnitTest/` + HTML index (49 classes / 388 tests / 0 fail / 0 err / 0 skip). APK proof via aapt badging, apksigner cert SHA-256 + debug.keystore validity, unzip asset listing + md5 of extracted bytes, `cmp` root APK == `app/build/outputs/apk/debug/app-debug.apk`. Combat FX lock via tip `git diff` line count on CombatFx/Plume + constant/TAG reads + Puffhold/Stroke suite green. Durations via ffprobe.

**Screenshot paths:** none (unit/code/APK proof gate; phone listening optional / not required; no device captures this run).

**WT notes:** untracked `docs/qa-gate-v0150-puffhold-2026-09-26.md` at gate time; this gate doc written as `docs/qa-gate-v0153-wakeblade-2026-09-27.md`.
