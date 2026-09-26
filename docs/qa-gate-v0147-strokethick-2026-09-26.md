# QA Gate — v0.1.47-strokethick (2026-09-26)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.47-strokethick  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-26 ~18:36–18:39 EDT  
**Engineer tag:** hold until CoS green (do not tag / gh release here).

**Tip (HEAD / origin/main):** `935c7488398083618f30e97fb5ead552e60dcdf9`  
**Claimed tip:** `935c7488398083618f30e97fb5ead552e60dcdf9` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `935c7488398083618f30e97fb5ead552e60dcdf9` |
| origin/main | `935c7488398083618f30e97fb5ead552e60dcdf9` |
| Match claimed tip | **YES** (HEAD == origin/main == claimed) |
| Subject | `feat(fx): v0.1.47-strokethick — fat Wake-family stroke + enemy You` |
| Author / date | Android Engineer <android-engineer@tod.local> — 2026-09-26 18:36:00 -0400 |

**`--stat` (935c748):** CleaveKit.kt (TAG → `v0.1.47-strokethick`; `BUST_COVERAGE=0.85` / `BUST_WIDTH_FRAC=0.78` / `SLASH_CROP_PX=140` **frozen**), CombatFx.kt (bust-frac core/glow widths, bright steel colors, peaks 470/400ms, `resolveFxPlayer` + `enemyHitProducesYouStroke` + `strokeCoreWidthPx`), CombatFxOverlay.kt (`drawWakeFamilyStroke` uses `strokeCoreWidthPx`; CLEAVE tip garnish only), CombatScreen.kt (`resolveFxPlayer` + sync `strokeDebugLine`), StrokeThickV0147Test (new 7), prior Fx/Slash/Cleave/StrokeFallback TAG/peak asserts updated, docs/strokethick-v0147.md. **13 files, +501/−109.**

**Aim / clip kept from v0.1.40-fxfix:** half-stage `recipientClipXFrac`, recipient-bust FX, Brace Shield stamps. Wake stays `WakeStageOverlay` / `wake_vfx_*` — **not** replaced by stroke.

**Frozen (not broken by tip):** F3 graph / Cave Troll / Gate-Warden / floor-locked loadout / Hub; Wake art/timing/math (`WakeArt.WAKE_ART_FULLY_FROZEN`); Brace floating pips (`CombatBracePipsOverlay` + `iron_mantle` → `NO_STROKE`); CLEAVE crop / BUST_* scale constants; no fullscreen / both-bust FX. Tip delta does **not** touch WakeArt / Floor3 / Hub / Brace path sources.

**WT (untracked; not in tip; do not fail):** `docs/art-audio/ASHBRAND_v0.1.34.md`, `docs/art-audio/CLIMB_INTRO_v0.1.36.md`, `tools/prep_ashbrand_v0134.py`

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 35223385 | 2026-09-26 18:35 EDT | `771980cdf91e5fdefff9c409ee85d897` |
| `app/build/outputs/apk/debug/app-debug.apk` | 35223385 | 2026-09-26 18:35 EDT | `771980cdf91e5fdefff9c409ee85d897` |

Claimed: **35223385** / `771980cdf91e5fdefff9c409ee85d897` → **MATCH** (root + build outputs identical).

Prior v0.1.46 APK was **37815780** (−2592395 ≈ −2.47 MiB). CoS note: full assemble blocked (Maven timeout); APK = compile + project-dex swap into v0.1.46 base. Size delta expected; see packaging case 6.

---

## C) Stroke / Cleave locks (HARD)

| Check | Result |
|-------|--------|
| `CombatStrokeOverlay` → `drawWakeFamilyStroke` | **EXISTS** — PRIMARY; uses `strokeCoreWidthPx` / `strokeGlowWidthPx` |
| `drawCleaveTipGarnish` | **EXISTS** — tip garnish only (not primary) |
| Hairline primary? | **NO** — core = bust×frac, not `THICK_SMALL×STROKE_CORE_MULT` |
| `STROKE_CORE_BUST_FRAC_*` | **0.14 / 0.18 / 0.10** (player small/medium/enemy) |
| `STROKE_GLOW_BUST_FRAC_*` | **0.30 / 0.38 / 0.22** |
| `COLOR_STROKE_YOU` / `ENEMY` | **0xFFF2E6D0** / **0xFFC4B8A8** |
| Peaks | player **470ms** / enemy **400ms** |
| CleaveKit `BUST_COVERAGE` / `BUST_WIDTH_FRAC` | **0.85 / 0.78** (unchanged) |
| `SLASH_CROP_PX` | **140** (unchanged) |
| Docs | `docs/strokethick-v0147.md` present with lock table |

**Measured @ stageW=360:** `bustWidth = 360×0.5×0.78 = 140.4`  
- player Small core = **19.7px** (0.14×bust) — band 18–28  
- player Medium core = **25.3px** (0.18×bust)  
- enemy core = **14.0px** (0.10×bust)  
- hairline FAIL baseline = `THICK_SMALL×STROKE_CORE_MULT` = **6.72px**  
- Fat ≫ hairline (Small core > hairline × 2.5)

---

## D) Unit tests (actual counts — QA measured)

**Gradle note:** `./gradlew :app:testDebugUnitTest --offline` **failed** this gate box — AGP plugin `com.android.application:8.7.2` not resolvable (Google Maven / plugin repos). Tip bytecode already compiled @ 18:33–18:34 EDT with the claimed APK (`StrokeThickV0147Test.class` mtime 18:34 EDT; `CombatFx.class` 18:33 EDT). **QA re-ran** all **45** classes via `org.junit.runner.JUnitCore` on tip-compiled `debug` + `debugUnitTest` classes + local jars (`/tmp/junit-full-v0147.log`, `/tmp/junit-key-v0147.log`, `/tmp/junit-stroke-v0147.log`, `/tmp/junit-perclass-v0147.txt`, CP `/tmp/junit-cp-v0147/`). Per-class `TEST-*.xml` rewritten under `app/build/test-results/testDebugUnitTest/` from that run (45 XML files; method names from source `@Test`).

| Suite | tests / fail / err / skip |
|-------|---------------------------|
| **Full** | **45 classes / 349 / 0 / 0 / 0** |
| StrokeThickV0147Test | **7 / 0 / 0 / 0** |
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

Claimed: unit **349/0**; StrokeThickV0147Test **7/0** → **MATCH** (measured). Key batch (10 classes) also **106/0** in one JUnitCore invocation. Sample size: full suite 349 tests across 45 classes.

---

## E) Cases 1–6

### 1) Hostflint/Emberbrand thick cut (~1/3–1/2 Wake fatness) bright steel on foe — **PASS**
`hostflint` / `emberbrand` ∈ `PLAYER_SMALL` → `FxTier.SMALL`, `FxRecipient.FOE`, steel `COLOR_STROKE_YOU=0xFFF2E6D0`, peak `STROKE_PLAYER_PEAK_MS=470`. Measured core @360 = **19.7px** (bust-frac 0.14) vs hairline **6.72px** — in WO lock band ~18–28; path length still Wake-family (`drawWakeFamilyStroke` quadratic crescent; path half-extent unchanged from 0.1.46). Screenshot-able still via unit/code proof (`StrokeThickV0147Test.coreGlow_*` + `playerHits_*` + overlay source). No device screenshot in this gate.

### 2) Enemy Hit/Seal Pulse: stroke on You + log `FX stroke on You` — **PASS**
`hit` / `seal_pulse` / etc. → `enemyHitProducesYouStroke` true; recipient `YOU`; `strokeDebugLine` = **`FX stroke on You`**. `resolveFxPlayer("hit", eventFxPlayer=true)` → **false** (forces player=false for enemy kit ids). CombatScreen wires `fxPlayerResolved = CombatFx.resolveFxPlayer(...)` and syncs `strokeDebugLine`. Locked by `enemyHit_recipientYou_debugFxStrokeOnYou`.

### 3) Wake gold arc largest unchanged — **PASS**
`WakeArt.kt` **not** in tip delta. `specForWake()` → null stroke + `useWakeSlash`; `WakeArt.WAKE_ART_FULLY_FROZEN`; `isWakeExclusiveId`. APK still packs `res/drawable/wake_vfx_{charge,slash,impact}.png`. Stroke does not replace Wake.

### 4) Notes list strokeWidth/color values — **PASS**
Gate table (matches design `docs/strokethick-v0147.md`):

| Constant | Value |
|----------|-------|
| `THICK_SMALL` / `THICK_MEDIUM` | `2.1` / `4.2` (relative weight; not px) |
| `STROKE_CORE_MULT` (hairline docs) | `3.2` → hairline ≈ `6.72px` |
| `STROKE_CORE_BUST_FRAC_PLAYER_SMALL` | `0.14` → ~19.7px @360 |
| `STROKE_CORE_BUST_FRAC_PLAYER_MEDIUM` | `0.18` → ~25.3px |
| `STROKE_CORE_BUST_FRAC_ENEMY` | `0.10` → ~14.0px |
| `STROKE_GLOW_BUST_FRAC_*` | `0.30` / `0.38` / `0.22` |
| `COLOR_STROKE_YOU` | `0xFFF2E6D0` |
| `COLOR_STROKE_ENEMY` | `0xFFC4B8A8` |
| `COLOR_STROKE_ENEMY_EMBER` | `0xFFD0B49A` |
| `COLOR_STROKE_CORE_HIGHLIGHT` | `0xFFFFF8EC` |
| `STROKE_PLAYER_PEAK_MS` / `STROKE_ENEMY_PEAK_MS` | `470` / `400` |
| `STROKE_SMALL_MS` / `STROKE_ENEMY_SMALL_MS` | `500` / `480` |

### 5) Brace/F3 frozen; no fullscreen; CLEAVE tip garnish only — **PASS**
`CombatBracePipsOverlay` still wired; `iron_mantle` ∈ `PLAYER_NO_STROKE`. `Floor3V0141Test` **10/0**. `recipientClipXFrac` half-stage; overlay strip not fullscreen. CleaveKit BUST_*/crop frozen; tip uses `drawCleaveTipGarnish` only — **no** BUST rescale as fix.

### 6) APK packaging OK despite dex-swap size delta — **PASS (OK)**
Unzip + `strings` on APK dex:
- `classes7.dex` contains: `v0.1.47-strokethick`, `strokeCoreWidthPx`, `FX stroke on You`, `resolveFxPlayer`, `enemyHitProducesYouStroke`, `STROKE_CORE_BUST_FRAC_*`, `COLOR_STROKE_YOU`
- Only TAG version across all dex: **`v0.1.47-strokethick`** (no leftover `v0.1.46-strokefallback` TAG string)
- Assets present: `assets/**`, `wake_vfx_*`, `climb_intro.mp4`, fx/hero/enemy art
- Size vs prior 37815780: **35223385** (−2.59M) — consistent with CoS “project-dex swap into v0.1.46 base” (full assemble Maven-blocked); contents verify — **not FLAG**

---

## F) HARD FAIL checks

| HARD FAIL if | Measured |
|--------------|----------|
| still hairline (~6.72px) as primary thickness | **NO** — primary core ~19.7/25.3px via bust-frac |
| enemy hits number-only without FX stroke on You | **NO** — `resolveFxPlayer` + debug `FX stroke on You` wired + tested |
| another CLEAVE / BUST rescale as fix | **NO** — BUST_*/crop identical; tip garnish only |
| Wake retuned | **NO** — WakeArt not in tip delta; frozen flag true |

---

## G) Verdict

**PASS** — tip SHA match, APK size+md5 match, packaging OK (dex-swap size delta noted; TAG/`strokeCoreWidthPx`/`FX stroke on You`/assets verified), suite **349/0** (StrokeThick **7/0**), cases 1–6 PASS, no HARD FAIL triggers. Hold engineer tag until CoS.

**Doc path:** `docs/qa-gate-v0147-strokethick-2026-09-26.md`

**FAIL blockers:** none

**Method note:** Gradle AGP/Google Maven blocked; JUnitCore on tip-compiled debug + debugUnitTest classes (same method as v0.1.46 gate). Sample: 45 classes / 349 tests.

**WT notes:** untracked art-audio docs + `tools/prep_ashbrand_v0134.py` (same as prior gates; not in tip)
