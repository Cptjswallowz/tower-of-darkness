# QA Gate — v0.1.48-strokeboth (2026-09-26)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.48-strokeboth  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-26 ~18:54–18:56 EDT  
**Engineer tag:** hold until CoS green (do not tag / gh release here).

**Tip (HEAD / origin/main):** `d5401f8f3cd3a6083b2f94e292260cfd820ec06a`  
**Claimed tip:** `d5401f8f3cd3a6083b2f94e292260cfd820ec06a` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `d5401f8f3cd3a6083b2f94e292260cfd820ec06a` |
| origin/main | `d5401f8f3cd3a6083b2f94e292260cfd820ec06a` |
| Match claimed tip | **YES** (HEAD == origin/main == claimed) |
| Subject | `feat(fx): v0.1.48-strokeboth — filled crescent both sides + peaks 500/450` |
| Author / date | Android Engineer <android-engineer@tod.local> — 2026-09-26 18:53:06 -0400 |

**`--stat` (d5401f8):** CleaveKit.kt (TAG → `v0.1.48-strokeboth`; `peakHoldFor` tip-garnish scale; BUST_*/crop **frozen**), CombatFx.kt (filled-crescent flags, peaks **500/450**, totals 580/520–540, `COLOR_STROKE_YOU=0xFFFFF6E4`, You-Shiv core fracs **kept** 0.14/0.18/0.10), CombatFxOverlay.kt (`crescentBladePath` filled Path primary — **no** `StrokeCap.Round` stadium; glow+core+highlight filled crescents), CombatScreen.kt (resolveFxPlayer + strokeDebugLine sync), StrokeBothV0148Test (new 7), prior StrokeThick/StrokeFallback/Slash*/Cleave/CombatFx* TAG/peak asserts updated, docs/strokeboth-v0148.md. **14 files, +392/−92.**

**Aim / clip kept from v0.1.40-fxfix:** half-stage `recipientClipXFrac`, recipient-bust FX, Brace Shield stamps. Wake stays `WakeStageOverlay` / `wake_vfx_*` — **not** replaced by stroke.

**Frozen (not broken by tip):** F3 graph / Cave Troll / Gate-Warden / floor-locked loadout / Hub; Wake art/timing/math (`WakeArt.WAKE_ART_FULLY_FROZEN`); Brace floating pips (`CombatBracePipsOverlay` + `iron_mantle` → `NO_STROKE`); CLEAVE crop / BUST_* scale constants; no fullscreen / both-bust FX. Tip delta does **not** touch WakeArt / Floor3 / Hub / Brace path sources.

**WT (untracked; not in tip; do not fail):** `docs/art-audio/ASHBRAND_v0.1.34.md`, `docs/art-audio/CLIMB_INTRO_v0.1.36.md`, `tools/prep_ashbrand_v0134.py`

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 35866457 | 2026-09-26 18:53 EDT | `e471b64c156dbe788350eebf580308b3` |
| `app/build/outputs/apk/debug/app-debug.apk` | 35866457 | 2026-09-26 18:53 EDT | `e471b64c156dbe788350eebf580308b3` |

Claimed: **35866457** / `e471b64c156dbe788350eebf580308b3` → **MATCH** (root + build outputs identical).

Prior v0.1.47 APK was **35223385** (+643072 ≈ +0.61 MiB). CoS note: full assemble blocked (Maven); APK = tip-compiled project-dex swap into **0.1.47 base** (`/tmp/tod-v0148-base.apk` md5 `771980cdf91e5fdefff9c409ee85d897` = v0.1.47). Size delta expected; see packaging case 6.

---

## C) Stroke / crescent locks (HARD)

| Check | Result |
|-------|--------|
| `CombatStrokeOverlay` → `drawWakeFamilyStroke` | **EXISTS** — PRIMARY |
| Shape | **filled crescent Path** via `crescentBladePath` (outer+inner quadratic; tips meet) |
| `StrokeCap.Round` primary / stadium pill? | **NO** — overlay has **no** `cap = StrokeCap.Round`; comments + test assert NOT Round pill |
| `STROKE_SHAPE_FILLED_CRESCENT` | **true** |
| `STROKE_CRESCENT_BOW_MULT` / `INNER_BOW_FRAC` | **1.20** / **0.38** |
| `drawCleaveTipGarnish` | **EXISTS** — tip garnish only (not primary) |
| `STROKE_CORE_BUST_FRAC_*` (You-Shiv lock) | **0.14 / 0.18 / 0.10** (player small/medium/enemy) |
| `STROKE_GLOW_BUST_FRAC_*` | **0.30 / 0.38 / 0.22** |
| `COLOR_STROKE_YOU` / `ENEMY` | **0xFFFFF6E4** / **0xFFC4B8A8** (YOU brighter vs 0.1.47) |
| Peaks | player **500ms** / enemy **450ms** |
| Totals | player SMALL/MED **580**; enemy SMALL/MED **520 / 540** |
| CleaveKit `BUST_COVERAGE` / `BUST_WIDTH_FRAC` | **0.85 / 0.78** (unchanged) |
| `SLASH_CROP_PX` | **140** (unchanged) |
| Docs | `docs/strokeboth-v0148.md` present with lock table |

**Measured @ stageW=360:** `bustWidth = 360×0.5×0.78 = 140.4`  
- player Small core = **19.7px** (0.14×bust)  
- player Medium core = **25.3px** (0.18×bust)  
- enemy You core = **14.0px** (0.10×bust)  
- glow: **42.1 / 53.4 / 30.9** px  
- player core ≥ enemy You (You-Shiv lock kept)

---

## D) Unit tests (actual counts — QA measured)

**Gradle note:** `./gradlew :app:testDebugUnitTest --offline` **failed** this gate box — AGP plugin `com.android.application:8.7.2` not resolvable (Google Maven / plugin repos). Tip bytecode already compiled @ 18:50–18:52 EDT with the claimed APK (`StrokeBothV0148Test.class` mtime 18:52 EDT; `CombatFx.class` 18:50 EDT; overlay/CleaveKit 18:52). **QA re-ran** all **46** classes via `org.junit.runner.JUnitCore` on tip-compiled `debug` + `debugUnitTest` classes + local jars (`/tmp/junit-full-v0148-qa.log`, `/tmp/junit-stroke-v0148.log`, `/tmp/junit-key-v0148-qa.log`, `/tmp/junit-perclass-v0148.txt`, CP `/tmp/junit-cp-v0148/`). Per-class `TEST-*.xml` rewritten under `app/build/test-results/testDebugUnitTest/` from that run (46 XML files; method names from source `@Test`).

| Suite | tests / fail / err / skip |
|-------|---------------------------|
| **Full** | **46 classes / 356 / 0 / 0 / 0** |
| StrokeBothV0148Test | **7 / 0 / 0 / 0** |
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

Claimed: unit **356/0**; StrokeBothV0148Test **7/0** → **MATCH** (measured). Key batch (12 classes: StrokeBoth+Thick+Fallback+Slash*3+Cleave+Floor3+CombatFx*4) also **120/0** in one JUnitCore invocation. Sample size: full suite 356 tests across 46 classes.

---

## E) Cases 1–6

### 1) Hostflint/Emberbrand thick crescent on enemy bust (still-photo hold) — **PASS**
`hostflint` / `emberbrand` ∈ `PLAYER_SMALL` → `FxTier.SMALL`, `FxRecipient.FOE`, steel `COLOR_STROKE_YOU=0xFFFFF6E4`, peak `STROKE_PLAYER_PEAK_MS=500`. Primary draw = filled `crescentBladePath` (not Round pill). Measured core @360 = **19.7px**. Locked by `outgoing_hostflintEmberbrandDustPike_strokeOnFoe` + `shape_filledCrescent_notRoundPill`. Debug `FX stroke on Goblin`. No device screenshot in this gate — unit/code proof + APK strings.

### 2) Hit/Shiv thick crescent on You — **PASS**
`hit` / `shiv` → `enemyHitProducesYouStroke` true; recipient `YOU`; `strokeDebugLine` = **`FX stroke on You`**. Enemy peak **450ms**; core **14.0px** @360 (frac 0.10). Overlay draws same filled crescent Path on You half-stage clip. Locked by `incoming_hitNipShivSealPulse_strokeOnYou`.

### 3) Wake larger gold unchanged — **PASS**
`WakeArt.kt` **not** in tip delta. `specForWake()` → null stroke + `useWakeSlash`; `WakeArt.WAKE_ART_FULLY_FROZEN`; `isWakeExclusiveId`. APK still packs `res/drawable/wake_vfx_{charge,slash,impact}.png`. Stroke does not replace Wake.

### 4) Notes strokeWidth player vs enemy (core fracs + measured px) — **PASS**
Gate table (matches design `docs/strokeboth-v0148.md`):

| Constant | Value | ~px @ stage 360 |
|----------|-------|-----------------|
| `STROKE_CORE_BUST_FRAC_PLAYER_SMALL` | `0.14` | **~19.7** |
| `STROKE_CORE_BUST_FRAC_PLAYER_MEDIUM` | `0.18` | **~25.3** |
| `STROKE_CORE_BUST_FRAC_ENEMY` | `0.10` | **~14.0** |
| `STROKE_GLOW_BUST_FRAC_*` | `0.30` / `0.38` / `0.22` | ~42.1 / 53.4 / 30.9 |
| `STROKE_CRESCENT_BOW_MULT` | `1.20` | bow ≥ half-chord |
| `STROKE_CRESCENT_INNER_BOW_FRAC` | `0.38` | inner < outer |
| `STROKE_SHAPE_FILLED_CRESCENT` | `true` | not Round pill |
| `COLOR_STROKE_YOU` | `0xFFFFF6E4` | |
| `COLOR_STROKE_ENEMY` | `0xFFC4B8A8` | |
| `STROKE_PLAYER_PEAK_MS` / `STROKE_ENEMY_PEAK_MS` | `500` / `450` | |
| `STROKE_SMALL_MS` / `MEDIUM` | `580` | |
| `STROKE_ENEMY_SMALL_MS` / `MEDIUM` | `520` / `540` | |

Formula: `bustWidthPx = stageWidth × 0.5 × STROKE_BUST_WIDTH_FRAC(0.78)`; `corePx = bustWidthPx × STROKE_CORE_BUST_FRAC_*`.

### 5) Nip/Seal Pulse also fire on You — **PASS**
`nip` (ENEMY_SMALL) / `seal_pulse` (ENEMY_MEDIUM) / `coal_slam` → YOU + `FX stroke on You`; `resolveFxPlayer("hit", true)` → false. Locked by same incoming test covering hit/nip/shiv/seal_pulse/coal_slam.

### 6) Brace/F3 frozen; no fullscreen; packaging OK — **PASS (OK)**
`CombatBracePipsOverlay` still wired; `iron_mantle` ∈ `PLAYER_NO_STROKE`. `Floor3V0141Test` **10/0**. `recipientClipXFrac` half-stage; overlay strip not fullscreen. CleaveKit BUST_*/crop frozen; tip uses `drawCleaveTipGarnish` only.

**Packaging (dex-swap into 0.1.47 base):** Unzip + `strings` on APK dex:
- `classes7.dex` contains: `v0.1.48-strokeboth`, `STROKE_SHAPE_FILLED_CRESCENT`, `STROKE_CRESCENT_BOW_MULT`, `STROKE_PLAYER_PEAK_MS`, `STROKE_ENEMY_PEAK_MS`, `COLOR_STROKE_YOU`, `FX stroke on You`, `strokeCoreWidthPx`
- `classes11.dex` contains: `crescentBladePath`, `drawWakeFamilyStroke`
- Only CombatFx/CleaveKit TAG version across dex: **`v0.1.48-strokeboth`** (no leftover `v0.1.47-strokethick` / `v0.1.46-strokefallback` TAG string)
- Assets present: `wake_vfx_*`, `climb_intro.mp4`, fx/hero/enemy art
- Size vs prior 35223385: **35866457** (+643072) — consistent with CoS “dex-swap into 0.1.47 base” (full assemble Maven-blocked); project dex (`classes7` 163980→164528; `classes11` 248680→249508) carry tip symbols — contents verify — **OK (not FLAG)**

---

## F) HARD FAIL checks

| HARD FAIL if | Measured |
|--------------|----------|
| outgoing (player→FOE) still numbers-only (no stroke / no FX stroke on enemy) | **NO** — Hostflint/Emberbrand/Dust Veil/Pike/Ruin Seal/Ash Press/Spark → FOE + `FX stroke on <enemy>`; tested |
| stroke is only pill bar (`StrokeCap.Round` thick stroke) — must be filled crescent Path | **NO** — `crescentBladePath` filled Path primary; no `cap = StrokeCap.Round`; `STROKE_SHAPE_FILLED_CRESCENT=true` |
| Wake retuned | **NO** — WakeArt not in tip delta; frozen flag true |
| another CLEAVE / BUST rescale as fix | **NO** — BUST_*/crop identical; tip garnish only |

---

## G) Verdict

**PASS** — tip SHA match, APK size+md5 match, packaging OK (dex-swap into 0.1.47 base; TAG + `crescentBladePath` / `STROKE_SHAPE_FILLED_CRESCENT` / peaks / assets verified — not FLAG), suite **356/0** (StrokeBoth **7/0**), cases 1–6 PASS, no HARD FAIL triggers. Hold engineer tag until CoS.

**Doc path:** `docs/qa-gate-v0148-strokeboth-2026-09-26.md`

**FAIL blockers:** none

**Method note:** Gradle AGP/Google Maven blocked; JUnitCore on tip-compiled debug + debugUnitTest classes (same method as v0.1.47 gate). Sample: 46 classes / 356 tests.

**WT notes:** untracked art-audio docs + `tools/prep_ashbrand_v0134.py` (same as prior gates; not in tip)
