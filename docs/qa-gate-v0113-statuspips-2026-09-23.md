# QA Gate: v0.1.13-statuspips (Compose Android)

**Date:** 2026-09-23 (America/New_York, EDT)  
**Project root:** `/workspace/tower-of-darkness`  
**Scope:** Measure-only. Do **not** retune save / 2x / path / rest-before-boss / Wake math / counters / shop / swap.  
**Hard exclusion:** Godot / tower-of-the-world / verify_v032 not touched or cited.  
**Claimed engineer tip:** `30230b6cbe5eff9a5f58aab16998f5839729c7b4` on `main`

---

## QA GATE PASS / GREEN LIGHT

---

## 1) APK + git

| Item | Measured |
|------|----------|
| APK path (root copy) | `/workspace/tower-of-darkness/tower-of-darkness-debug.apk` |
| APK path (build) | `/workspace/tower-of-darkness/app/build/outputs/apk/debug/app-debug.apk` |
| APK size | **17830215** bytes (both copies; md5 `c59676a2643798f2b69491f5332cefb0` **identical**) |
| APK mtime (root) | **2026-09-23 13:18:46.493203671 -0400** (EDT) |
| APK mtime (build) | **2026-09-23 13:18:39.809196776 -0400** (EDT) |
| `git log -1` | `30230b6 v0.1.13-statuspips: Brace/Soften pips under fighter HP bars` |
| `git rev-parse HEAD` | `30230b6cbe5eff9a5f58aab16998f5839729c7b4` |
| HEAD vs claimed | **Matches** tip `30230b6…` |
| Commit date | 2026-09-23 13:18:52 -0400 (EDT) |
| Branch status | `main...origin/main` **[ahead 1]** (`0	1` left-right vs origin/main) |

### `git show --stat HEAD` (files touched)

```
README.md
app/src/main/java/.../domain/combat/CombatEngine.kt   (12 ±)
app/src/main/java/.../domain/combat/StatusPips.kt     (+23, new)
app/src/main/java/.../domain/glossary/Glossary.kt     (+1 Soften)
app/src/main/java/.../ui/components/StatusPipRow.kt   (+83, new)
app/src/main/java/.../ui/screens/CombatScreen.kt      (17 ±)
app/src/test/java/.../StatusPipsV0113Test.kt          (+179, new)
docs/art-audio/STATUS_PIPS_v0.1.13.md
docs/status-pips-v0113.md
9 files changed, 398 insertions(+), 9 deletions(-)
```

In-scope new files: `StatusPips.kt`, `StatusPipRow.kt`.

---

## 2) Unit tests (QA re-run)

Command:

```bash
./gradlew :app:testDebugUnitTest \
  --tests 'com.towerofdarkness.app.StatusPipsV0113Test'
```

**BUILD SUCCESSFUL** (`:app:testDebugUnitTest` executed). Measured from  
`app/build/test-results/testDebugUnitTest/TEST-com.towerofdarkness.app.StatusPipsV0113Test.xml`  
(timestamp `2026-09-23T17:21:39` Z ≈ **13:21:39 EDT**): **tests=8 failures=0 errors=0 skipped=0 time=0.054s**.

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

---

## 3) Required verdicts (WO checklist) — measured

### 1) Iron Mantle / Vow Plate: Brace N under You; after spend-2 hit → N-2; at 0 pip gone — **PASS**

**Evidence (tests):**
- `ironMantle_bracePipUnderYou` → after Iron Mantle resolve, `brace==3`, `StatusPips.forPlayer` single pip `brace` count 3
- `vowPlate_bracePip5` → Brace 5 mirrored as pip count 5
- `brace_afterHitSpending2_pipIsNMinus2_thenGoneAtRoundEnd` → N=5, after counter with spend-2: pip count `N-2`; after `readyNext` brace 0 and player pips empty
- `brace_fullySpent_pipGone` → brace fully absorbed → `StatusPips.forPlayer` empty
- `hideAtZero_noEmptyPips` → brace 0 / counterPenalty 0 → both pip lists empty

**Evidence (code):** `StatusPips.forPlayer` adds `StatusPip("brace", state.brace)` only when `brace > 0`. CombatScreen composes `StatusPipRow(pips = StatusPips.forPlayer(state), …)` under You HP.

### 2) Cinder Step Soften: pip under enemy while live; gone after consume — **PASS**

**Evidence (test):** `cinderStep_softenPipUnderEnemy_thenGoneAfterCounter` → after Cinder Step, `counterPenalty==2`, `StatusPips.forEnemy` single `soften` count 2; after `resolveEnemy`, counterPenalty 0 and enemy pips empty.

**Evidence (code):** `StatusPips.forEnemy` mirrors `counterPenalty` as Soften when > 0. CombatScreen: `StatusPipRow(pips = StatusPips.forEnemy(state), …)` under enemy HP. Soften cleared on counter (`counterPenalty = 0` in `resolveEnemy`).

### 3) Two statuses at once both visible — **PASS**

**Evidence (test):** `twoStatuses_bothVisible` → player pips `brace` 3 and enemy pips `soften` 2 simultaneously.

**Evidence (code):** Independent `forPlayer` / `forEnemy` lists; both rows composed under respective HP bars.

### 4) No damage or stack rule change — **PASS** (with documented clear-timing move)

**Evidence (StatusPips):** `StatusPips.kt` is a pure mirror of `CombatState.brace` / `counterPenalty`; no new stack math.

**Evidence (`git diff HEAD~1` CombatEngine):** Absorb formula unchanged (`dmg -= counterPenalty`; `absorbed = minOf(brace, dmg)`; `brace -= absorbed`). Soften still applied as `counterPenalty` subtract before Brace absorb. Only changes:
1. Leftover Brace kept after counter (`brace = brace` instead of forced `0`) then cleared in `readyNext` — enables N−2 pip visibility / cards-v0 round-end clear (glossary: “Clears at round end if unused leftover”). Same absorb on the hit; no extra absorb window between counter and readyNext in the beat flow.
2. Soften combat log gains `glossaryHints = listOf("soften")` (UI wiring only).

**Frozen domain files:** `git diff HEAD~1 --name-only` contains **no** Balance / PathGen / BossRest / Wake / shop / Swap / MidRunSlot files.

### 5) 2x does not skip pip updates; tap → glossary (Brace + Soften) — **PASS**

**Evidence (2x / derived pips):** `StatusPipRow` receives `StatusPips.forPlayer(state)` / `forEnemy(state)` each composition from live `CombatState` — no tick-based pip animation that 2x could skip. Optional frozen re-run: `CombatSpeedV0111Test` **5/5 PASS** (timestamp `2026-09-23T17:21:24` Z ≈ **13:21:24 EDT**; failures=0 errors=0 skipped=0).

**Evidence (tap → glossary):** `StatusPipChip` is `clickable` → `onTerm(pip.term)`; CombatScreen wires `onTerm = { gc.showGlossary(it) }` for both player and enemy rows. Test `glossary_hasBraceAndSoften` asserts Glossary definitions for `brace` and `soften` (Soften mentions counter).

---

## 4) Frozen check

`git diff HEAD~1 --stat` (9 files): StatusPips / StatusPipRow / CombatScreen / Glossary Soften / CombatEngine leftover-Brace clear timing + soften glossaryHints / StatusPipsV0113Test / docs / README. **No** Balance, PathGen, BossRest, Wake timing, shop, swap, or MidRunSlot retunes.

| System | Verdict | Evidence |
|--------|---------|----------|
| Save / MidRunSlot | Untouched | Not in `git diff HEAD~1 --name-only`; optional `MidRunSaveV0112Test` **8/8 PASS** @ 13:21:24 EDT |
| 2x timings | Untouched | No CombatSpeed / Balance hold diffs; `CombatSpeedV0111Test` **5/5 PASS** |
| Path / Boss-rest | Untouched | No PathGen / BossRest files in commit |
| Wake math | Untouched | No Wake / `WEAPON_FULL_HOLD_MS` diffs |
| Counters / shop / swap | Untouched | Counter soft/brace **absorb math** unchanged; no shop/swap files |

---

## 5) Doc path

`/workspace/tower-of-darkness/docs/qa-gate-v0113-statuspips-2026-09-23.md`

---

## Overall

**Gate PASS** — HEAD matches claimed tip `30230b6…` (main ahead 1 of origin); APK 17830215 B md5 `c59676a2…` identical both paths @ ~13:18 EDT; `StatusPipsV0113Test` 8/8 PASS on QA re-run; WO cases 1–5 PASS with test + code evidence; frozen systems not retuned (optional MidRunSave 8/8 + CombatSpeed 5/5 PASS).
