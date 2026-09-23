# QA Gate: v0.1.14-bracesync (Compose Android)

**Date:** 2026-09-23 (America/New_York, EDT)  
**Project root:** `/workspace/tower-of-darkness`  
**Scope:** Measure-only. Feel/draw sync (pip → absorb float −N → HP). Do **not** retune absorb math / Wake / 2x / MidRunSave / Soften / path / rest-before-boss / shop / swap.  
**Hard exclusion:** Godot / tower-of-the-world / verify_v032 not touched or cited.  
**Claimed engineer tip:** `89553c72c339faa83aba9fa90bf243c10502cf85` on `main`  
**Claimed APK:** `/workspace/tower-of-darkness/tower-of-darkness-debug.apk` — 17831862 bytes — md5 `02712757a0ea06713522e5e9c27baca7`

---

## QA GATE PASS / GREEN LIGHT

---

## 1) APK + git

| Item | Measured |
|------|----------|
| APK path (root copy) | `/workspace/tower-of-darkness/tower-of-darkness-debug.apk` |
| APK path (build) | `/workspace/tower-of-darkness/app/build/outputs/apk/debug/app-debug.apk` |
| APK size | **17831862** bytes (both copies; matches claimed) |
| APK md5 | **02712757a0ea06713522e5e9c27baca7** (both copies identical; matches claimed) |
| APK mtime (root) | **2026-09-23 17:07:18.800241265 -0400** (EDT) |
| APK mtime (build) | **2026-09-23 17:07** EDT (same size/md5) |
| `git log -1` | `89553c72 v0.1.14-bracesync: pip → absorb float → HP draw order` |
| `git rev-parse HEAD` | `89553c72c339faa83aba9fa90bf243c10502cf85` |
| HEAD vs claimed | **Matches** tip `89553c72…` |
| `origin/main` | `89553c72c339faa83aba9fa90bf243c10502cf85` (identical) |
| Commit date | 2026-09-23 17:07:23 -0400 (EDT) |
| Branch status | `main...origin/main` **in sync** (`0	0` left-right) |

### `git show --stat HEAD` (files touched)

```
README.md
app/src/main/java/.../domain/combat/CombatEngine.kt   (7 ± — braceAbsorbed field + tag only)
app/src/main/java/.../domain/combat/StatusPips.kt     (+61 presentational)
app/src/main/java/.../ui/components/StatusPipRow.kt   (69 ± float −N on Brace pip)
app/src/main/java/.../ui/screens/CombatScreen.kt      (42 ± delay HP after absorb float)
app/src/test/java/.../BraceSyncV0114Test.kt           (+217, new)
app/src/test/java/.../StatusPipsV0113Test.kt          (+3)
docs/art-audio/BRACE_ABSORB_FLOAT_v0.1.14.md
docs/brace-sync-v0114.md
docs/qa-gate-v0114-bracesync-2026-09-23.md            (engineer stub → this QA rewrite)
docs/status-pips-v0113.md
11 files changed, 482 insertions(+), 36 deletions(-)
```

In-scope presentational: `BraceDrawSync` / `BraceHitDrawPhase` in `StatusPips.kt`, `StatusPipRow` absorb float, `CombatScreen` HP staging. CombatEngine only adds `braceAbsorbed` presentation tag on existing absorb result.

---

## 2) Unit tests (QA re-run)

Command:

```bash
./gradlew :app:testDebugUnitTest \
  --tests 'com.towerofdarkness.app.BraceSyncV0114Test' \
  --tests 'com.towerofdarkness.app.StatusPipsV0113Test' \
  --tests 'com.towerofdarkness.app.CombatSpeedV0111Test' \
  --tests 'com.towerofdarkness.app.MidRunSaveV0112Test'
```

**BUILD SUCCESSFUL**. Measured from `app/build/test-results/testDebugUnitTest/`  
(timestamp `2026-09-23T21:08:14` Z ≈ **17:08:14 EDT**).

### BraceSyncV0114Test — 7/7 PASS

| Test | Result |
|------|--------|
| `pipTickBeforeHpBar_drawOrderIsPipThenFloatThenHp` | PASS |
| `absorbFloatShown_onHitEvent` | PASS |
| `braceZero_visibleOneBeatThenGone` | PASS |
| `sameBeat_skillBeforeHit_braceAppliesBeforeAbsorb` | PASS |
| `skillAfterHit_noInventedPreBlock` | PASS |
| `absorbMathUnchanged_minBraceDmg` | PASS |
| `softenUnchanged_stillHideAtZero` | PASS |

XML: `tests=7 failures=0 errors=0 skipped=0 time=0.047s`

### StatusPipsV0113Test — 8/8 PASS

| Test | Result |
|------|--------|
| `glossary_hasBraceAndSoften` | PASS |
| `ironMantle_bracePipUnderYou` | PASS |
| `vowPlate_bracePip5` | PASS |
| `brace_afterHitSpending2_pipIsNMinus2_thenGoneAtRoundEnd` | PASS |
| `brace_fullySpent_pipGone` | PASS |
| `cinderStep_softenPipUnderEnemy_thenGoneAfterCounter` | PASS |
| `twoStatuses_bothVisible` | PASS |
| `hideAtZero_noEmptyPips` | PASS |

XML: `tests=8 failures=0 errors=0 skipped=0 time=0.005s`

### CombatSpeedV0111Test — 5/5 PASS

| Test | Result |
|------|--------|
| `wakeReadableAt1x_andHalfAt2x` | PASS |
| `holdMs_zeroOrNegativeSpeed_treatedAs1x` | PASS |
| `holdMs_2x_halvesEveryHold` | PASS |
| `holdMs_1x_keepsBudgets` | PASS |
| `holdMs_clampsAbove2_no3x` | PASS |

XML: `tests=5 failures=0 errors=0 skipped=0 time=0.003s`

### MidRunSaveV0112Test — 8/8 PASS

| Test | Result |
|------|--------|
| `stairContinuePayload_floor2_clearsPending_resumesPath` | PASS |
| `pendingStair_resumeIsFloorBreak_notPath` | PASS |
| `newClimbWipe_meansNoActiveSlot` | PASS |
| `roundTrip_preservesFloor2HpRemLoadoutAshbrandClearedAndRngSeed` | PASS |
| `noCombatSnapshotFields_inEncodedPayload` | PASS |
| `corruptOrWrongSchema_decodeReturnsNull` | PASS |
| `rngSeed_requiredAndRoundTrips` | PASS |
| `killWardenThenForceStop_onStair_resumesFloorBreak_notF1Path` | PASS |

XML: `tests=8 failures=0 errors=0 skipped=0 time=0.033s`

**Targeted re-run total:** 7+8+5+8 = **28/28 PASS**.

---

## 3) Required verdicts (WO checklist) — measured

### 1) Hit with Brace: pip ticks/appears BEFORE HP bar leftover anim — **PASS**

**Evidence (test):** `pipTickBeforeHpBar_drawOrderIsPipThenFloatThenHp` → `BraceDrawSync.hitDrawOrder() == [PIP_UPDATE, ABSORB_FLOAT, HP_BAR]`; `delayHpBarAfter` true when `braceAbsorbed>0`, false when 0.

**Evidence (code):** `StatusPips.kt` `BraceHitDrawPhase` + `BraceDrawSync.hitDrawOrder` / `delayHpBarAfter`. `CombatScreen.kt` LaunchedEffect: on absorb event sets `braceAbsorbFloat`, `delay(absorbHold)` (420ms @1x via `ABSORB_FLOAT_MS`), then updates `displayedPlayerHp` — HP bar staged after pip+float.

### 2) Absorb float (−N) on/near pip before HP move — **PASS**

**Evidence (test):** `absorbFloatShown_onHitEvent` → `BraceDrawSync.absorbFloat(event)` returns absorbed N when >0, null when 0.

**Evidence (code):** `StatusPipRow` draws `−$absorbFloat` on Brace chip (Bone tint, offset above pip). CombatScreen passes `braceAbsorbFloat` and clears it after absorb hold **before** committing `displayedPlayerHp`.

### 3) Brace→0: shows "0" that beat, gone next beat — **PASS**

**Evidence (test):** `braceZero_visibleOneBeatThenGone` → after full spend, `StatusPips.forPlayer` has brace count 0 and `holdBraceZero` true on `AFTER_ENEMY`; after `readyNext`, player pips empty and `holdBraceZero` false.

**Evidence (code):** `StatusPips.forPlayer` adds `StatusPip("brace", 0)` when `BraceDrawSync.holdBraceZero(state)`; hold only on `AFTER_ENEMY` (or defeat awaiting continue) while latest absorb hit present.

### 4) Same-beat log order respected (no invent pre-block) — **PASS**

**Evidence (tests):**
- `sameBeat_skillBeforeHit_braceAppliesBeforeAbsorb` → Iron Mantle then hit absorbs with pre-applied Brace; pip mirrors post-absorb.
- `skillAfterHit_noInventedPreBlock` → naked hit `braceAbsorbed==0`; applying Brace after does not rewrite past hit / HP.

**Evidence (code):** No resolver reorder in CombatEngine; `braceAbsorbed` only tags existing absorb result. Draw sync reads event fields — does not invent pre-block.

### 5) Absorb math unchanged — **PASS**

**Evidence (test):** `absorbMathUnchanged_minBraceDmg` → Brace 5 / Soften 5 / absorb 2 → brace 3, HP unchanged, message `hits for 0`.

**Evidence (code):** `CombatEngine` still `absorbed = minOf(brace, dmg); brace -= absorbed; dmg -= absorbed`. Diff only adds `braceAbsorbed = absorbed` on the event (presentation tag). Soften subtract path untouched.

### 6) Frozen: Wake, 2x, MidRunSave still green; Soften unchanged — **PASS**

**Evidence (tests):** `CombatSpeedV0111Test` 5/5; `MidRunSaveV0112Test` 8/8; `softenUnchanged_stillHideAtZero` + StatusPips Soften cases still green.

**Evidence (code):** `StatusPips.forEnemy` unchanged (`counterPenalty > 0` → soften pip; no `BraceDrawSync` on enemy). Soften path has no absorb-float / HP-delay wiring.

---

## 4) Frozen check

`git diff HEAD~1 --name-only` (11 files): CombatEngine (braceAbsorbed tag only), StatusPips / StatusPipRow / CombatScreen presentational, BraceSyncV0114Test, StatusPipsV0113Test (+3), docs, README. **No** Balance, PathGen, BossRest, Wake, shop, swap, or MidRunSlot files.

| System | Verdict | Evidence |
|--------|---------|----------|
| Absorb / Brace clear math | Untouched (tag only) | Same `minOf(brace,dmg)` formula; leftover clear still via readyNext; `absorbMathUnchanged_minBraceDmg` PASS |
| Soften | Untouched | `forEnemy` hide-at-0 unchanged; `softenUnchanged_stillHideAtZero` PASS; no BraceDrawSync on enemy |
| Wake math | Untouched | No Wake / hold budget domain retunes; `wakeReadableAt1x_andHalfAt2x` PASS |
| 2x timings | Untouched | CombatScreen uses existing `combatHoldMs` for absorb float; `CombatSpeedV0111Test` **5/5 PASS** |
| Mid-run save | Untouched | Not in diff name-only; `MidRunSaveV0112Test` **8/8 PASS** |
| Path / Boss-rest / shop / swap | Untouched | No PathGen / BossRest / shop / swap files in commit |

---

## 5) Doc path

`/workspace/tower-of-darkness/docs/qa-gate-v0114-bracesync-2026-09-23.md`

---

## Overall

**Gate PASS** — HEAD matches claimed tip `89553c72…` (main == origin/main); APK 17831862 B md5 `02712757…` identical both paths @ 17:07 EDT; BraceSyncV0114Test **7/7**, StatusPips **8/8**, CombatSpeed **5/5**, MidRunSave **8/8** on QA re-run; WO cases 1–6 PASS with test + code evidence; frozen systems not retuned beyond bracesync presentational files + `braceAbsorbed` event tag.
