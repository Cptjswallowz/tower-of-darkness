# QA Gate: v0.1.10-bossrest (Compose Android)

**Date:** 2026-09-22 (America/New_York, EDT)  
**Project root:** `/workspace/tower-of-darkness`  
**Scope:** Measure-only. Do **not** retune Wake / thresh / counters / boss HP / F2 trash / swap / shop / F2 persist.  
**Hard exclusion:** Godot / tower-of-the-world / verify_v032 not touched or cited.  
**Claimed engineer tip:** `9222f57ff6a6b7a8ca1199e9ae740984a8417b64` on `main`

---

## QA GATE PASS

## 1) APK + git

| Item | Measured |
|------|----------|
| APK path (build) | `/workspace/tower-of-darkness/app/build/outputs/apk/debug/app-debug.apk` |
| APK path (root copy) | `/workspace/tower-of-darkness/tower-of-darkness-debug.apk` |
| APK size | **17717500** bytes (both copies) |
| APK mtime (build) | **2026-09-22 20:09:06.810639782 -0400** (EDT) |
| APK mtime (root copy) | **2026-09-22 20:09:07.110640270 -0400** (EDT) |
| `git log -1` | `9222f57 v0.1.10-bossrest: pre-boss REST, Rest Heal to MAX, Free Scout tap` |
| `git rev-parse HEAD` | `9222f57ff6a6b7a8ca1199e9ae740984a8417b64` |
| HEAD vs claimed | **Matches** tip `9222f57` |
| Branch status | `main...origin/main` (clean; scratch measurer deleted after run) |

---

## 2) Path sims — 100 Floor 1 + 100 Floor 2 (seeds 0–99)

**Engineer suites run (re-measured by QA):**
```bash
./gradlew :app:testDebugUnitTest --tests '*BossRest*' --tests '*PathGate*' --tests '*PathGenerator*' --tests '*Floor2*' --tests '*Scout*'
```
- No separate `*Scout*` test class exists; Free Scout covered by `BossRestV0110Test`.
- Scratch helper `BossRestPathSimScratch` printed exact route counts for seeds 0–99; **deleted after** (no commit).

### Floor 1 (measured)

| Metric | Count |
|--------|------:|
| gens | 100 (seeds 0–99) |
| total S→B routes | 251 |
| **last_non_boss_not_REST** | **0** |
| **zero_combat S→B** | **0** |
| event histogram | `{0=28, 1=72}` |
| event max | 1 |
| floors_with_events > 1 | **0** |

**PathGateV015Test** printed (F1 floor-level combat/event; asserts aligned):
- `floors_with_zero_combat_before_boss=0`
- `floors_with_fightless_start_to_boss_route=0`
- `combat_before_boss_histogram={2=30, 3=48, 4=19, 5=3}`
- `event_count_histogram={0=28, 1=72}`
- `event_max=1 event_floors_gt1=0`
- `outliers_count=0`

### Floor 2 (measured)

| Metric | Count |
|--------|------:|
| gens | 100 (seeds 0–99) |
| total S→B routes | 251 |
| **last_non_boss_not_REST** | **0** |
| **zero_combat S→B** | **0** |
| event histogram | `{0=28, 1=72}` |
| event max | 1 |
| floors_with_events > 1 | **0** |

**Floor2Test** `floor2Path_seeds0to99_everyStartToBossHasCombat_eventsAtMostOne` asserts `fightless=0`, `eventsOver=0`, `missingPreBossRest=0` — **PASS**.

**PathGeneratorTest** `everyStartToBossPath_lastNonBossIsRest_f1AndF2` (seeds 0–200, floors 1+2) — **PASS**.

---

## 3) Rest HP=max + Scout reveal/decrement/noop-at-0

### Rest Heal → MAX — **PASS**

| Evidence | Detail |
|----------|--------|
| Unit test | `BossRestV0110Test.restHeal_setsHpToMax_deepBreathDoesNotOverheal` — `applyRestHealToMax(30)=30`, `applyRestHealToMax(34)=34` |
| Code | `GameController.applyRestHealToMax(maxHp: Int): Int = maxHp` (companion ~L410) |
| Runtime | `restHeal()` sets `playerHp = applyRestHealToMax(maxHp)` (~L686–689); Deep Breath cannot push past max |
| UI copy | `RestScreen.kt`: button label `"Heal (to full)"` |

### Free Scout tap reveal / decrement / noop-at-0 — **PASS**

| Evidence | Detail |
|----------|--------|
| Unit test | `BossRestV0110Test.freeScout_tapFogged_revealsTypeAndDecrements_secondTapNoOpAtZero` |
| Behavior asserted | charges 1→0; `scoutedTypeOnly=true`; type unchanged; second tap returns `null` at charges=0; other fog node stays unrevealed |
| Code | `applyFreeScout`: `if (charges <= 0) return null`; else `withReveal(..., typeOnly=true) to (charges - 1)` (~L416–425); `useFreeScoutOn` wires charges (~L707–713) |
| Extra | `freeScout_doesNotSpendOnAlreadyRevealed_orStartBoss`, `freeScout_doesNotChangeType` — **PASS** |

---

## 4) Unit test suite results (QA re-run)

**Command:** `./gradlew :app:testDebugUnitTest --tests '*BossRest*' --tests '*PathGate*' --tests '*PathGenerator*' --tests '*Floor2*' --tests '*Scout*'`  
**Gradle:** **BUILD SUCCESSFUL** (~1–2s)

| Suite | tests | failures | errors | Result |
|-------|------:|---------:|-------:|--------|
| `BossRestV0110Test` | 4 | 0 | 0 | **PASS** |
| `PathGateV015Test` | 1 | 0 | 0 | **PASS** |
| `PathGeneratorTest` | 7 | 0 | 0 | **PASS** |
| `Floor2Test` | 9 | 0 | 0 | **PASS** |
| `*Scout*` (standalone) | — | — | — | N/A (covered by BossRest) |
| **Total** | **21** | **0** | **0** | **PASS** |

JUnit XML under `app/build/test-results/testDebugUnitTest/`.

---

## 5) Frozen spot-check (`git show 9222f57 --stat`)

**Files in tip:**  
`README.md`, `TowerPath.kt`, `GameController.kt`, `PathScreen.kt`, `RestScreen.kt`, `BossRestV0110Test.kt`, `Floor2Test.kt`, `PathGeneratorTest.kt`, `docs/bossrest-v0110.md`, `docs/floor2-v019.md`

| Frozen system | Touched as subject of this change? |
|---------------|-------------------------------------|
| Wake / thresh | **No** — not in commit |
| Counters / boss HP | **No** — `Balance` / `Enemy` not in commit; Floor2Test still asserts BOSS_HP=28 / BOSS_FLOOR2_HP=32 / counters 6–9 |
| F2 trash | **No** |
| Swap | **No** |
| Shop | **No** (GameController shop helpers unchanged in this tip’s intent; only rest/scout APIs + path rules) |
| F2 persist | **No** — FloorBreak persist tests still pass unchanged |

`GameController.kt` **was** edited, but only for Rest Heal→MAX and Free Scout tap apply — not Wake/thresh/counters/boss HP/trash/swap/shop wallet/F2 persist.

---

## 6) Overall

| Gate item | Verdict |
|-----------|---------|
| APK present + tip match | **PASS** |
| F1 last_non_boss_not_REST=0 | **PASS** (0) |
| F1 zero_combat=0 | **PASS** (0) |
| F1 events ≤1 / floors_gt1=0 | **PASS** (hist `{0=28,1=72}`, max=1) |
| F2 last_non_boss_not_REST=0 | **PASS** (0) |
| F2 zero_combat=0 | **PASS** (0) |
| F2 events ≤1 / floors_gt1=0 | **PASS** (hist `{0=28,1=72}`, max=1) |
| Rest HP=max | **PASS** |
| Scout reveal/decrement/noop-at-0 | **PASS** |
| Unit tests | **PASS** (21/21) |
| Frozen systems not retuned | **PASS** (spot-check) |

### Overall Gate: **PASS**

No failures.
