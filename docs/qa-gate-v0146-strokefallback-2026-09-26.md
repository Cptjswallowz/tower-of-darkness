# QA Gate — v0.1.46-strokefallback (2026-09-26)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.46-strokefallback  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-26 ~18:12–18:15 EDT  
**Engineer tag:** **CoS GREEN** — standing push OK; annotated tag `v0.1.46-strokefallback` cut on release tip (README + this gate).

**Tip (HEAD / origin/main):** `1f99e7bfcd1a6a9bd1d5cd8ad83a3cef81da3316`  
**Claimed tip:** `1f99e7bfcd1a6a9bd1d5cd8ad83a3cef81da3316` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `1f99e7bfcd1a6a9bd1d5cd8ad83a3cef81da3316` |
| origin/main | `1f99e7bfcd1a6a9bd1d5cd8ad83a3cef81da3316` |
| Match claimed tip | **YES** (HEAD == origin/main == claimed) |
| Subject | `feat(fx): v0.1.46-strokefallback — drawn Wake-family stroke primary` |
| Author / date | Android Engineer <android-engineer@tod.local> — 2026-09-26 18:11:22 -0400 |

**`--stat` (1f99e7b):** CleaveKit.kt (TAG bump only; `BUST_COVERAGE=0.85` / `BUST_WIDTH_FRAC=0.78` / `SLASH_CROP_PX=140` / OPAQUE_UNION **frozen** from 0.1.45), CombatFx.kt (drawn-stroke PRIMARY locks: widths 78/88/60%, peaks 420/320ms, steel/ash colors, `strokeDebugLine` / `slashLightDebugLine`, spark short stroke), CombatFxOverlay.kt (`CombatStrokeOverlay` → `drawWakeFamilyStroke` PRIMARY + optional `drawCleaveTipGarnish`; removed “Prefer CLEAVE sheet whenever”), CombatScreen.kt (180.dp stroke strip; `onStrokeDebug` + `onSlashLightDebug`), StrokeFallbackV0146Test (new 8), prior Fx/Slash/Cleave TAG asserts → `v0.1.46-strokefallback`, docs/strokefallback-v0146.md. **14 files, +577/−162.**

**Aim / clip kept from v0.1.40-fxfix:** half-stage `recipientClipXFrac`, recipient-bust FX, Brace Shield stamps. Wake stays `WakeStageOverlay` / `wake_vfx_*` — **not** replaced by stroke.

**Frozen (not broken by tip):** F3 graph / Cave Troll / Gate-Warden / floor-locked loadout / Hub; Wake art/timing/math; Brace floating pips; CLEAVE crop / BUST_* scale constants; no fullscreen / both-bust FX.

**WT (untracked; not in tip; do not fail):** `docs/art-audio/ASHBRAND_v0.1.34.md`, `docs/art-audio/CLIMB_INTRO_v0.1.36.md`, `tools/prep_ashbrand_v0134.py`

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 37815780 | 2026-09-26 18:11 EDT | `752d894dbfd3917452e6c827699eb4f2` |
| `app/build/outputs/apk/debug/app-debug.apk` | 37815780 | 2026-09-26 18:10 EDT | `752d894dbfd3917452e6c827699eb4f2` |

Claimed: **37815780** / `752d894dbfd3917452e6c827699eb4f2` → **MATCH** (root + build outputs identical).

---

## C) Stroke / Cleave locks (HARD)

| Check | Result |
|-------|--------|
| `CombatStrokeOverlay` | **EXISTS** — `CombatFxOverlay.kt` |
| `drawWakeFamilyStroke` | **EXISTS** — PRIMARY path (glow + thick core quadratic crescent) |
| `drawCleaveTipGarnish` | **EXISTS** — optional tip-only (`tipSize ≈ min(w,h)*0.11`) |
| Prefer CLEAVE sheet as primary? | **NO** — tip removed “Prefer CLEAVE sheet whenever”; path always draws |
| CleaveKit `BUST_COVERAGE` | **0.85** (unchanged vs `v0.1.45-slashscale`) |
| CleaveKit `BUST_WIDTH_FRAC` | **0.78** (unchanged) |
| `SLASH_CROP_PX` / OX/OY / OPAQUE_UNION | **140 / 115 / 26 / 102×133** (unchanged) |
| CleaveKit delta vs 0.1.45 | **TAG string only** (`v0.1.46-strokefallback`) |
| Docs | `docs/strokefallback-v0146.md` present; names CombatStrokeOverlay / drawWakeFamilyStroke / tip garnish |

---

## D) Unit tests (actual counts)

**Gradle note:** `./gradlew :app:testDebugUnitTest --offline` failed this gate box — AGP plugin `com.android.application:8.7.2` not resolvable (Google Maven / plugin repos). Tip bytecode already compiled @ 18:10–18:11 EDT with the claimed APK (`StrokeFallbackV0146Test.class` mtime 18:11 EDT). **QA re-ran** all **44** classes via `org.junit.runner.JUnitCore` on tip-compiled `debug` + `debugUnitTest` classes + local jars (`/tmp/junit-full-v0146.log`, `/tmp/junit-key-v0146.log`, `/tmp/junit-perclass-v0146.txt`, CP `/tmp/junit-cp-v0146/`). Per-class `TEST-*.xml` rewritten under `app/build/test-results/testDebugUnitTest/` from that run.

| Suite | tests / fail / err / skip |
|-------|---------------------------|
| **Full** | **44 classes / 342 / 0 / 0 / 0** |
| StrokeFallbackV0146Test | **8 / 0 / 0 / 0** |
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

Claimed: unit **342/0**; StrokeFallbackV0146Test **8** → **MATCH** (measured). Key batch (10 classes) also **106/0** in one JUnitCore invocation.

---

## E) Cases 1–9

### 1) Hostflint or Pike — thick drawn stroke 70–90% foe bust, peak ~420ms — **PASS**
`hostflint` ∈ `PLAYER_SMALL` → `FxTier.SMALL`, `FxRecipient.FOE`, steel `COLOR_STROKE_YOU`, `strokeMs=500`, peak `STROKE_PLAYER_PEAK_MS=420` (lock 400–500). `tower_pike` ∈ `PLAYER_MEDIUM` → width frac **0.88**. Measured @ stageW=360: bustWidth=**140.4**; Small full stroke=**109.5** (=**78%** bust); Medium=**123.6** (=**88%**). Path primary via `drawWakeFamilyStroke`; half-stage FOE clip. No device screenshot in this gate; geometry + mapping + overlay path prove screenshot-able crescent on foe bust (not numbers-only / not sheet blob). Locked by `StrokeFallbackV0146Test.widths_*` + `peakHolds_*` + `map_*`.

### 2) Emberbrand — same — **PASS**
`emberbrand` ∈ `PLAYER_SMALL` → same Small/FOE/steel/420ms peak path as Hostflint. Asserted in `map_playerEnemyStrokes_steelAsh_notForcedGoldGreen`.

### 3) Enemy Hit on You — ~60% shorter steel/ash ~320ms — **PASS**
`hit` → `FxRecipient.YOU`, `STROKE_WIDTH_FRAC_ENEMY=0.60`, `STROKE_ENEMY_PEAK_MS=320`, `STROKE_ENEMY_SMALL_MS=360`, color `COLOR_STROKE_ENEMY` (not forced green). Full stroke @360=**84.2** (=**60%** bust). Locked by widths + peak + map tests.

### 4) Wake gold arc UNCHANGED — **PASS**
`WakeStageOverlay` / `wake_vfx_slash` **not** in tip file delta. `specForWake()` → null stroke + `useWakeSlash`; `WakeArt.WAKE_ART_FULLY_FROZEN`; `isWakeExclusiveId`. Stroke does not replace Wake.

### 5) Brace pips still under owner — **PASS**
`CombatBracePipsOverlay` still wired in CombatScreen (140.dp strip); `iron_mantle` → `NO_STROKE` / null stroke; brace owner clip via `recipientClipXFrac`. Tip does not retarget Brace→shield sheet.

### 6) No fullscreen / both-bust cover — **PASS**
`recipientClipXFrac` YOU `0..0.5` / FOE `0.5..1`; `slashCutGeom.spansBothBusts()` false for foe Small/Med and You hit (StrokeFallback widths test). Overlay is 180.dp stage strip, not fullscreen.

### 7) Debug distinguishes FX stroke vs FX slash-light — **PASS**
`STROKE_DEBUG_FMT = "FX stroke on "` (always on path); `SLASH_LIGHT_DEBUG_FMT = "FX slash-light on "` (only when tip garnish sheet present). Distinct strings; CombatScreen `onStrokeDebug` / `onSlashLightDebug`. Locked by `pathIsPrimary_debugStringsDiffer`.

### 8) Notes name stroke composable — **PASS**
`docs/strokefallback-v0146.md` table: `CombatFxOverlay.kt` → `CombatStrokeOverlay` / `drawWakeFamilyStroke` / `drawCleaveTipGarnish` / domain `CombatFx.kt`. Test `composablePath_CombatStrokeOverlay_drawWakeFamilyStroke`.

### 9) F3 / loadout / Hub frozen green — **PASS**
Tip delta does not touch Floor3 / Hub / loadout / shop / rest / path sources. `Floor3V0141Test` **10/0**; Hub suite **9+7+8 / 0**.

---

## F) HARD FAIL checks

| HARD FAIL if | Measured |
|--------------|----------|
| only Wake readable (Hostflint/Emberbrand/enemy Hit numbers+log only) | **NO** — drawn path is PRIMARY for those cards; Wake exclusive untouched |
| stroke is another ~32px glow blob | **NO** — player Small path length **~110px** (78% bust @360); tip garnish ≤~40px optional only |
| another CLEAVE scale change | **NO** — BUST_* / crop / OPAQUE_UNION identical to 0.1.45; TAG-only |
| Invisible CLEAVE tip spark | **NOT fail** (per WO) — path readable |

---

## G) Verdict

**PASS** — tip SHA match, APK size+md5 match, suite **342/0** (StrokeFallback **8/0**), cases 1–9 PASS, no HARD FAIL triggers. Hold engineer tag until CoS.

**Doc path:** `docs/qa-gate-v0146-strokefallback-2026-09-26.md`

**FAIL blockers:** none

**WT notes:** untracked art-audio docs + `tools/prep_ashbrand_v0134.py` (same as prior gates; not in tip)
