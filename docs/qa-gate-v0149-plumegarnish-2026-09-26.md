# QA Gate — v0.1.49-plumegarnish (2026-09-26)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.49-plumegarnish  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-26 ~19:21–19:23 EDT  
**Engineer tag:** CoS GREEN-LIGHT — release tip cuts annotated tag + gh release.

**Tip (HEAD / origin/main):** `5851cc4193f2ac56619fc6851fa6e50abb48ec2c`  
**Claimed tip:** `5851cc4193f2ac56619fc6851fa6e50abb48ec2c` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `5851cc4193f2ac56619fc6851fa6e50abb48ec2c` |
| origin/main | `5851cc4193f2ac56619fc6851fa6e50abb48ec2c` |
| Match claimed tip | **YES** (HEAD == origin/main == claimed) |
| Subject | `feat(fx): v0.1.49-plumegarnish — Kenney+PLUME particle garnish on locked stroke` |
| Author / date | Android Engineer <android-engineer@tod.local> — 2026-09-26 19:19:58 -0400 |

**`--stat` (5851cc4):** PlumeGarnishKit.kt (new domain kit — maps / timings / atlas / debug / allowlist), CombatFxOverlay.kt (+CombatParticleGarnishOverlay shared drawImage engine), CombatScreen.kt (tip/dust/soften/brace-flare wiring + debug), CombatFx.kt / CleaveKit.kt (TAG → `v0.1.49-plumegarnish` only; stroke path/width/peaks/colors **untouched**), 23× `fx_kenney_*.png` + 5× `fx_plume_*.png` drawables, PlumeGarnishV0149Test (9), prior Stroke*/Slash*/Cleave/CombatFx* TAG asserts updated, docs/plumegarnish-v0149.md, tools/prep_plumegarnish_v0149.py. **46 files, +1086/−23.**

**Stroke LOCK (HARD from v0.1.48-strokeboth):** tip delta on CombatFx.kt is **TAG/comment only** — no change to `STROKE_SHAPE_FILLED_CRESCENT`, core fracs 0.14/0.18/0.10, peaks 500/450, bow 1.20/0.38, colors, totals. CleaveKit BUST_*/crop frozen (TAG only).

**Frozen (not broken by tip):** F3 graph / Cave Troll / Gate-Warden / floor-locked loadout / Hub; Wake art (`WakeArt` not in tip delta); Brace floating pips (`CombatBracePipsOverlay` kept — flare is GAIN-only additive); no fullscreen / both-bust FX; stroke primary remains filled crescent Path.

**WT (untracked):** none (clean working tree).

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | **38614426** | 2026-09-26 19:19 EDT | `26508263e4029252d99b79e86dbf99eb` |
| `app/build/outputs/apk/debug/app-debug.apk` | 35866457 | 2026-09-26 18:53 EDT | `e471b64c156dbe788350eebf580308b3` (stale **v0.1.48** build-outputs; not the tip APK) |

Claimed: **38614426** / `26508263e4029252d99b79e86dbf99eb` → **MATCH** (root APK).

Prior v0.1.48 APK was **35866457** (+2747969 ≈ +2.62 MiB). Size delta consistent with injecting 28 kenney+plume PNGs into package.

---

## C) Stroke / crescent locks (HARD — must be unchanged)

| Check | Result |
|-------|--------|
| `STROKE_SHAPE_FILLED_CRESCENT` | **true** (unchanged) |
| `STROKE_CRESCENT_BOW_MULT` / `INNER_BOW_FRAC` | **1.20** / **0.38** |
| `STROKE_CORE_BUST_FRAC_*` (You-Shiv lock) | **0.14 / 0.18 / 0.10** |
| `STROKE_GLOW_BUST_FRAC_*` | **0.30 / 0.38 / 0.22** |
| `COLOR_STROKE_YOU` / `ENEMY` | **0xFFFFF6E4** / **0xFFC4B8A8** |
| Peaks | player **500ms** / enemy **450ms** |
| Totals | player SMALL/MED **580**; enemy SMALL/MED **520 / 540** |
| CleaveKit `BUST_COVERAGE` / `BUST_WIDTH_FRAC` | **0.85 / 0.78** |
| `SLASH_CROP_PX` | **140** |
| Tip CombatFx.kt stroke constants | **no numeric/path delta** (TAG only) |
| Docs | `docs/plumegarnish-v0149.md` — stroke LOCKED callout |

**Measured @ stageW=360:** `bustWidth = 360×0.5×0.78 = 140.4` → core **19.7 / 25.3 / 14.0** px (player S/M / enemy You) — same as v0.1.48.

Locked by `PlumeGarnishV0149Test.strokeLock_widthsPeaksPathUntouched` + prior `StrokeBothV0148Test` still green.

---

## D) Unit tests (actual counts — QA measured)

**Gradle note:** `./gradlew :app:testDebugUnitTest --offline` **failed** this gate box — AGP plugin `com.android.application:8.7.2` not resolvable (Google Maven / plugin repos). Tip bytecode already compiled @ 19:15 EDT (`PlumeGarnishKit.class` / `PlumeGarnishV0149Test.class` / `CombatFx.class` mtime 19:15; tip commit 19:19). **QA re-ran** all **47** classes via `org.junit.runner.JUnitCore` on tip-compiled `debug` + `debugUnitTest` classes + local jars (`/tmp/junit-full-v0149-qa.log`, `/tmp/junit-plume-v0149.log`, `/tmp/junit-perclass-v0149.txt`, CP `/tmp/junit-cp-v0149/` reused from v0148 classpath). Per-class `TEST-*.xml` rewritten under `app/build/test-results/testDebugUnitTest/` from that run (47 XML files; method names from source `@Test`).

| Suite | tests / fail / err / skip |
|-------|---------------------------|
| **Full** | **47 classes / 365 / 0 / 0 / 0** |
| PlumeGarnishV0149Test | **9 / 0 / 0 / 0** |
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

Prior v0.1.48 full = 46/356; tip adds PlumeGarnishV0149Test **+9** → **47/365**. Measured match.

---

## E) Cases 1–9

### 1) Wake still largest / gold arc; stroke readable under garnish (Hostflint + Emberbrand + enemy Hit/Shiv on You) — **PASS**
Stroke LOCKED: filled crescent Path primary (`crescentBladePath`); core fracs / peaks / colors unchanged from v0.1.48. Hostflint/Emberbrand → FOE stroke; Hit/Shiv → You stroke (`FX stroke on You`). Wake not in tip delta; `WakeArt` frozen. Garnish is additive tip/dust overlay — does not replace stroke or Wake. Locked by `strokeLock_widthsPeaksPathUntouched` + StrokeBothV0148Test still **7/0**. No device screenshot in this gate — unit/code + APK string proof.

### 2) Dust Veil / Ash Press → PLUME sand-kick soot puff on TARGET bust (~50%) — **PASS**
`dust_veil` / `ash_press` ∈ `DUST_ABILITY_IDS` → `dustPuffSpec` → pack PLUME, sheet `sand-kick`, `PUFF_BUST_FRAC=0.50`, tint `COLOR_SOOT_ASH`, hold 400ms. `CombatScreen` dust overlay `recipient = FxRecipient.FOE` (TARGET bust only; half-stage clip). `neverUsesGroundFogAsDust()` true. Locked by `mapCoverage_dustAndTipAndSoftenAndBrace` + `drawableAllowlist_*`.

### 3) Cinder Step → footstep-puff on target — **PASS**
`cinder_step` → `dustPuffSpec("cinder_step")` → `fx_plume_footstep_puff` / sheet `footstep-puff`, FOE recipient, 50% bust. Same dust overlay path as case 2.

### 4) Ashbrand spark / Wake tip → grinder-sparks ember (rust-gold), tip-sized (~25% bust) — **PASS**
`tipSparkSpec()` → PLUME `grinder-sparks`, additive, atlas, `TIP_BUST_FRAC=0.25`, tint `COLOR_RUST_GOLD`, hold 250ms. Wired at stroke tip (`atStrokeTip = true`) for SPARK / Wake / optional hit-confirm on player Small/Medium. Debug `FX plume grinder-sparks on <target>`.

### 5) Soften → Kenney circle_03 under foe HP (dirty green), while status active — **PASS**
`softenPipSpec()` → Kenney `circle_03`, `COLOR_DIRTY_GREEN`, `SOFTEN_BUST_FRAC=0.12`, `underHp = true`, recipient FOE, visible while `state.counterPenalty > 0`. Locked by `softenUnderFoe_braceFlareDoesNotReplacePips`.

### 6) Brace GAIN may show tiny flare_01; floating Brace pips unchanged — **PASS**
`braceFlareSpec()` → Kenney `flare_01`, ~14% bust, 280ms, GAIN-only alongside `CombatBracePipsOverlay` (still wired; flare cleared early, does not replace pips). `braceFlareDoesNotReplacePips()` true. APK retains `CombatBracePipsOverlay` + `CombatParticleGarnishOverlay` symbols.

### 7) Debug lines when FX fire: FX stroke… / FX plume… / FX kenney… — **PASS**
`CombatFx.strokeDebugLine` → `FX stroke on <target>`; `PlumeGarnishKit.plumeDebugLine` / `kenneyDebugLine` / `debugLine(spec,…)` → `FX plume grinder-sparks on Goblin`, `FX kenney circle_03 on foe`, `FX kenney flare_01 on You`. Overlay `Log.i(TodFx, …)` on visible. Locked by `debugStringFormats_plumeKenneyStroke`. APK `classes2.dex` contains `FX stroke on`, `FX plume `, `FX kenney `.

### 8) No ground-fog fog-wall; no white/cyan hit FX; Floor 3 / Gate-Warden / Hub untouched — **PASS**
`PLUME_GROUND_FOG_BANNED_AS_FOG_WALL=true`; dust/tip never select `fx_plume_ground_fog` (asset may exist on disk/APK — not wired as dust). Recolor constants rust-gold / soot-ash / dirty-green / ember-rust (never default white/cyan). Banned Kenney families slash/twirl/magic/muzzle not imported as hit FX. `Floor3V0141Test` **10/0**; Hub* **9+7+8 / 0**; WakeArt / Floor3 / Hub sources not in tip delta.

### 9) APK packages kenney+plume drawables (or note dex-swap) — **PASS (OK)**
**Packaging:** Maven assemble blocked → tip packaged as **dex-swap + resource inject** into prior base (build-outputs APK still v0.1.48 35866457). Project classes consolidated into `classes2.dex` (74320 → 921316); library `classes.dex` / `classes12` / `classes13` unchanged; prior small project dexes (classes3–11) absent from tip APK.

**Drawables in root APK `res/drawable/` (28):** all 23 `fx_kenney_{spark_01–07,circle_01–05,smoke_01–10,flare_01}.png` + all 5 `fx_plume_{grinder_sparks,sand_kick,footstep_puff,thin_wisp,ground_fog}.png`.  
**Dex TAG:** only `v0.1.49-plumegarnish` (no leftover `v0.1.48-strokeboth` TAG). Symbols present: `PlumeGarnishKit`, `STROKE_SHAPE_FILLED_CRESCENT`, peaks, core fracs, `crescentBladePath`, `drawWakeFamilyStroke`, `CombatBracePipsOverlay`, `CombatParticleGarnishOverlay`, `FX plume` / `FX kenney` / `FX stroke on`.  
→ **OK (not FLAG)** — kenney+plume drawables confirmed in APK; dex-swap documented.

---

## F) HARD FAIL checks

| HARD FAIL if | Measured |
|--------------|----------|
| Stroke path / width / peaks / colors changed from v0.1.48 | **NO** — tip CombatFx stroke delta is TAG/comment only; constants + StrokeBoth tests green |
| Ground-fog wired as dust / fullscreen fog wall | **NO** — `neverUsesGroundFogAsDust()`; dust = sand-kick / footstep-puff; FOE half-stage only |
| Banned Kenney slash/twirl/magic/muzzle as hit FX | **NO** — allowlist test; no `fx_kenney_{slash,twirl,magic,muzzle}_*` drawables |
| White/cyan default hit FX tints | **NO** — rust-gold / soot / dirty-green / ember-rust |
| Wake / F3 / Hub / Brace pips broken | **NO** — not in tip delta; Floor3+Hub+Brace overlay tests/wiring green |
| APK missing kenney+plume with no dex-swap note | **NO** — 28 drawables present; dex-swap + OK |

---

## G) Verdict

**PASS** — tip SHA match, APK size+md5 match, packaging OK (dex-swap + kenney/plume drawables in APK — not FLAG), suite **365/0** (PlumeGarnish **9/0**; prior Stroke*/Slash*/Cleave/Floor3/CombatFx* green), stroke LOCK held, cases 1–9 PASS, no HARD FAIL triggers. CoS greened; release proceeds.

**Doc path:** `docs/qa-gate-v0149-plumegarnish-2026-09-26.md`

**FAIL blockers:** none

**Method note:** Gradle AGP/Google Maven blocked; JUnitCore on tip-compiled debug + debugUnitTest classes (same method as v0.1.48 gate). Sample: 47 classes / 365 tests. XML rewritten under `app/build/test-results/testDebugUnitTest/`.

**Screenshot paths:** none (unit/code/APK proof gate; no device captures this run).

**WT notes:** leave untracked alone if present — `assets/fx/plume/*_v0150*`, `docs/art-audio/PLUME_FULL_DELTA_v0.1.50.md` (and any ASHBRAND_v0.1.34 / CLIMB_INTRO_v0.1.36 / prep_ashbrand). Not in this release commit.
