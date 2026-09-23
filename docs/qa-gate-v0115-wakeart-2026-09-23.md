# QA Gate: v0.1.15-wakeart (Compose Android)

**Date:** 2026-09-23 (America/New_York, EDT)  
**Project root:** `/workspace/tower-of-darkness`  
**Scope:** Measure-only. Presentation art (Ashbrand icon + FULL Wake crescent sequence + SPARK ember). Do **not** retune Wake math / thresh / dmg / MidRunSave / StatusPips / BraceSync / CombatSpeed / path.  
**Hard exclusion:** Godot / tower-of-the-world / verify_v032 not touched or cited.  
**Claimed engineer tip:** `a9bb15d4db49c53a329e2fb6530ee57248fc0ff3` on `main`  
**Claimed APK:** `/workspace/tower-of-darkness/tower-of-darkness-debug.apk` — 18180390 bytes — md5 `d489f73bc9375a3dbe715644b7e52258`

---

## QA GATE PASS / GREEN LIGHT

---

## 1) APK + git

| Item | Measured |
|------|----------|
| APK path (root copy) | `/workspace/tower-of-darkness/tower-of-darkness-debug.apk` |
| APK path (build) | `/workspace/tower-of-darkness/app/build/outputs/apk/debug/app-debug.apk` |
| APK size | **18180390** bytes (both copies; matches claimed) |
| APK md5 | **d489f73bc9375a3dbe715644b7e52258** (both copies identical; matches claimed) |
| APK mtime (root) | **2026-09-23 19:09:30 EDT** |
| APK mtime (build) | **2026-09-23 19:09:24 EDT** (same size/md5) |
| Root vs build | **Identical** (`cmp` YES) |
| `git log -1` | `a9bb15d4 v0.1.15-wakeart: Ashbrand icon + FULL Wake crescent sequence` |
| `git rev-parse HEAD` | `a9bb15d4db49c53a329e2fb6530ee57248fc0ff3` |
| HEAD vs claimed | **Matches** tip `a9bb15d4…` |
| `origin/main` | `a9bb15d4db49c53a329e2fb6530ee57248fc0ff3` (identical) |
| Commit date | 2026-09-23 19:09:35 -0400 (EDT) |
| Branch status | `main...origin/main` **in sync** (`0	0` left-right) |

### `git show --stat HEAD` (files touched)

```
README.md
app/src/main/java/.../domain/combat/WakeArt.kt          (+150, new — presentation mapping)
app/src/main/java/.../ui/components/AshbrandIcon.kt     (+94, new)
app/src/main/java/.../ui/components/WakeStageOverlay.kt (+38, new)
app/src/main/java/.../ui/screens/CombatScreen.kt        (112 ± — stage frames + spark ember)
app/src/main/java/.../ui/screens/LoadoutScreen.kt       (16 ± — AshbrandIcon slot)
app/src/main/res/drawable/ashbrand_icon.png
app/src/main/res/drawable/ashbrand_spark.png
app/src/main/res/drawable/wake_vfx_charge.png
app/src/main/res/drawable/wake_vfx_impact.png
app/src/main/res/drawable/wake_vfx_slash.png
app/src/test/java/.../WakeArtV0115Test.kt               (+196, new)
docs/art-audio/WAKE_ART_v0.1.15.md
docs/wake-art-v0115.md
docs/wake-v014.md                                       (+2)
15 files changed, 703 insertions(+), 28 deletions(-)
```

In-scope presentational only. **No** `Balance.kt`, `CombatEngine.kt`, `Weapon.kt` / catalog, MidRunSave, StatusPips, BraceSync, PathGen, or CombatSpeed domain files in the commit.

---

## 2) Unit tests (QA re-run)

Command:

```bash
./gradlew :app:testDebugUnitTest \
  --tests 'com.towerofdarkness.app.WakeArtV0115Test' \
  --tests 'com.towerofdarkness.app.CombatSpeedV0111Test' \
  --tests 'com.towerofdarkness.app.MidRunSaveV0112Test' \
  --tests 'com.towerofdarkness.app.StatusPipsV0113Test' \
  --tests 'com.towerofdarkness.app.BraceSyncV0114Test'
```

**BUILD SUCCESSFUL**. Measured from `app/build/test-results/testDebugUnitTest/`  
(XML timestamp `2026-09-23T23:10:37` Z ≈ **19:10:37 EDT**).

### WakeArtV0115Test — 8/8 PASS

| Test | Result |
|------|--------|
| `drawables_lockedNames` | PASS |
| `iconSlot_sameSizeLoadoutAndCombat` | PASS |
| `spark_noCrescent_emberOnIconOnly` | PASS |
| `fullWake_crescentLogFloatThenClear` | PASS |
| `speed2x_sameFrames_halfDuration` | PASS |
| `commonSkills_noCrescent` | PASS |
| `fullWake_keepsExistingLogAndFloatContract` | PASS |
| `sequenceFitsInsideFullHold_noMathChange` | PASS |

XML: `tests=8 failures=0 errors=0 skipped=0 time=0.003s`

### CombatSpeedV0111Test — 5/5 PASS

XML: `tests=5 failures=0 errors=0 skipped=0 time=0.002s`

### MidRunSaveV0112Test — 8/8 PASS

XML: `tests=8 failures=0 errors=0 skipped=0 time=0.023s`

### StatusPipsV0113Test — 8/8 PASS

XML: `tests=8 failures=0 errors=0 skipped=0 time=0.003s`

### BraceSyncV0114Test — 7/7 PASS

XML: `tests=7 failures=0 errors=0 skipped=0 time=0.035s`

**Targeted re-run total:** 8+5+8+8+7 = **36/36 PASS**.

### Optional full suite — 84/84 PASS

`./gradlew :app:testDebugUnitTest` (no filter) → **15** classes, **84** tests, **0** failures / errors / skipped. Confirms engineer claim **84/84**.

| Class | Tests |
|-------|------:|
| BossRestV0110Test | 4 |
| BraceSyncV0114Test | 7 |
| CombatEngineTest | 12 |
| CombatSimV013Test | 1 |
| CombatSimV014Test | 1 |
| CombatSpeedV0111Test | 5 |
| Floor2Test | 9 |
| MidRunSaveV0112Test | 8 |
| PathGateV015Test | 1 |
| PathGeneratorTest | 7 |
| ShopWalletTest | 6 |
| StatusPipsV0113Test | 8 |
| TreasureSwapLockTest | 3 |
| WakeArtV0115Test | 8 |
| WeaponXpTest | 4 |
| **Total** | **84** |

---

## 3) Required verdicts (WO checklist) — measured

### 1) Spark: no crescent; tiny ember on Ashbrand icon only — **PASS**

**Evidence (test):** `spark_noCrescent_emberOnIconOnly` → `showCrescent=false`, `showSparkEmber=true`, `iconPhase=SPARK_EMBER`, `stageFrame=NONE` at 0/200ms; no WAKE float / gold log.

**Evidence (code):** `WakeArt.showCrescent` / `showSparkEmber` / `iconPhase` SPARK path. `AshbrandIcon` overlays `ashbrand_spark` only when `phase == SPARK_EMBER`. `CombatScreen` sets `wakeStageFrame=NONE` on spark and never mounts crescent.

### 2) Full Wake: charge→slash→impact crescent + gold log "ASHBRAND — WAKE N" + WAKE float; overlay clears after hold — **PASS**

**Evidence (test):** `fullWake_crescentLogFloatThenClear` + `fullWake_keepsExistingLogAndFloatContract` → gold log `^ASHBRAND — WAKE \d+$`, float `WAKE`, frames CHARGE@0 → SLASH@400 → IMPACT@900 → NONE after sequence duration **1300** (inside hold **2300**).

**Evidence (code):** `WakeArt.stageSequence()` CHARGE/SLASH/IMPACT (400/500/400). `CombatScreen` LaunchedEffect advances frames then sets `wakeStageFrame=NONE`. `WakeStageOverlay` only when frame ≠ NONE. Drawables: `wake_vfx_{charge,slash,impact}`.

### 3) 2x: same frames, ~half duration (~1150 vs ~2300); does not skip frames — **PASS**

**Evidence (test):** `speed2x_sameFrames_halfDuration` → holds 400→200, 500→250, 400→200; frame order CHARGE→SLASH→IMPACT→NONE at compressed clocks; `fullWakeHold1x=2300`, `fullWakeHold2x=1150` (= `GameController.combatHoldMs(WEAPON_FULL_HOLD_MS, 2)`).

**Evidence (code):** `WakeArt.holdMs` halves when `speedX>=2`; no frame drop. `CombatScreen` delays each step via `WakeArt.holdMs(step.baseMs, speed)`. GameController still holds full wake on frozen `Balance.WEAPON_FULL_HOLD_MS`.

### 4) Icon visible on loadout + combat slot — **PASS**

**Evidence (test):** `iconSlot_sameSizeLoadoutAndCombat` → `ICON_SLOT_DP=48`; `drawables_lockedNames` → `ashbrand_icon`.

**Evidence (code):** `LoadoutScreen` renders `AshbrandIcon()` on weapon plate. `CombatScreen` / `WeaponBar` renders `AshbrandIcon(phase = iconPhase)`. Shared `WakeArt.ICON_SLOT_DP=48`. Drawable `R.drawable.ashbrand_icon` present.

### 5) Frozen: Wake math/thresh, save, pips, path, Brace sync still green — **PASS**

**Evidence (tests):** `sequenceFitsInsideFullHold_noMathChange` → thresh **3**, fullDmg **4**, sparkDmg **2** (L1); CombatSpeed **5/5**, MidRunSave **8/8**, StatusPips **8/8**, BraceSync **7/7**; full suite PathGeneratorTest **7/7**, PathGateV015Test **1/1**.

**Evidence (code):** `git diff HEAD~1 --name-only` has **no** Balance / CombatEngine / Weapon / MidRunSave / StatusPips / Brace / PathGen files. `Balance.WEAPON_FULL_HOLD_MS` remains **2300L**. Ashbrand catalog arrays untouched.

---

## 4) Source summary (D)

| Item | Measured |
|------|----------|
| Drawable names | `ashbrand_icon`, `ashbrand_spark`, `wake_vfx_charge`, `wake_vfx_slash`, `wake_vfx_impact` |
| Crescent gate | `WakeArt.showCrescent` / `stageFrame` only on FULL weapon beat; SPARK → NONE |
| Spark ember | `AshbrandIcon` spark overlay iff `SPARK_EMBER`; no stage overlay |
| Hold wrappers | `WakeArt.fullWakeHold1x/2x` → `Balance.WEAPON_FULL_HOLD_MS` (2300 / 1150); CombatScreen frame delays via `WakeArt.holdMs`; GameController combat hold still uses frozen `WEAPON_FULL_HOLD_MS` |

---

## 5) Frozen check

`git diff HEAD~1 --name-only` (15 files): WakeArt + UI components/screens + drawables + WakeArtV0115Test + docs/README. **No** Balance, CombatEngine, Weapon catalog, MidRunSave, StatusPips, BraceSync, PathGen, BossRest, shop, or swap files.

| System | Verdict | Evidence |
|--------|---------|----------|
| Wake math / thresh / dmg | Untouched | Not in diff; thresh 3 / full 4 / spark 2 asserted; `sequenceFitsInsideFullHold_noMathChange` PASS |
| Full hold budgets | Untouched | `WEAPON_FULL_HOLD_MS=2300` unchanged; art frames fit inside (1300 ≤ 2300) |
| 2x timings | Untouched | CombatSpeed **5/5**; WakeArt.holdMs mirrors half-hold contract |
| Mid-run save | Untouched | Not in diff; MidRunSave **8/8** |
| Status pips | Untouched | Not in diff; StatusPips **8/8** |
| Brace draw sync | Untouched | Not in diff; BraceSync **7/7** |
| Path generators | Untouched | Not in diff; PathGeneratorTest **7/7**, PathGate **1/1** |

---

## 6) Doc path

`/workspace/tower-of-darkness/docs/qa-gate-v0115-wakeart-2026-09-23.md`

Design refs: `docs/wake-art-v0115.md`, `docs/art-audio/WAKE_ART_v0.1.15.md`

---

## Overall

**Gate PASS** — HEAD matches claimed tip `a9bb15d4…` (main == origin/main); APK 18180390 B md5 `d489f73b…` identical both paths @ 19:09 EDT; WakeArtV0115Test **8/8**, frozen suites CombatSpeed **5/5** / MidRunSave **8/8** / StatusPips **8/8** / BraceSync **7/7** (targeted **36/36**); optional full suite **84/84**; WO cases 1–5 PASS with test + code evidence; Wake math / thresh / dmg / path / save / pips / brace sync not retuned (presentation-only commit).
