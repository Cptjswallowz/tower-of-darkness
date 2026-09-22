# QA GATE v0.1.10-routecare — 2026-09-22

Measured results only. Project: `/workspace/tower-of-darkness` (Compose Android).  
No Wake / shop prices / boss HP / F2 persist / balance retune. Read-only + unit tests + this doc.  
Hard exclusion: Godot / tower-of-the-world / verify_v032 not touched or cited.

**CARE** = `REST` or `SHOP` on a Start→Boss route.

**Claimed engineer commit:** `9e32324` (WO v0.1.10-routecare DONE)

---

## QA GATE PASS

## 1) APK + git

| Field | Measured |
|-------|----------|
| Path | `/workspace/tower-of-darkness/tower-of-darkness-debug.apk` |
| Size | **17716583** bytes |
| mtime | **2026-09-22 19:47:52.064493597 -0400** (EDT) |
| `git log -1 --oneline` | `9e32324 v0.1.10-routecare: every Start→Boss path must include ≥1 CARE (REST\|SHOP)` |
| `git rev-parse HEAD` | `9e32324b92f261d4e94d212453db5466058a4999` |
| HEAD vs claimed | **Matches** claimed commit `9e32324` |
| Branch | `## main...origin/main` (clean working tree at gate time; doc added after measurements) |

## 2) Commands run

```bash
stat -c '%s %y' tower-of-darkness-debug.apk
git log -1 --oneline && git rev-parse HEAD && git status -sb
./gradlew :app:testDebugUnitTest --tests '*PathGeneratorTest*' --tests '*PathGate*' --tests '*Floor2Test*'
```

Harness: existing `PathGateV015Test` (floor=1; includes fightless + careless route counters), `PathGeneratorTest` (CARE + combat locks), `Floor2Test.floor2Path_seeds0to99_everyStartToBossHasCombatAndCare_eventsAtMostOne`.  
No separate `RouteCare*` test class found via `rg` (CARE covered in PathGate / PathGenerator / Floor2).  

Floor 2 event histogram / exact S→B path totals: one-off temporary scratch test calling `PathGenerator.generate` (deleted after capture; not committed; no production/balance changes).

## 3) Floor 1 — seeds 0..99 (100 gens)

**Source:** `PathGateV015Test.pathGate_v015_100floorSim_seeds0to99` system-out + cross-check scratch on `PathGenerator.generate(floor=1, Random(seed))`.

| Metric | Measured |
|--------|----------|
| `S→B paths with 0 COMBAT` (`S_to_B_paths_with_0_COMBAT`) | **0** |
| `S→B paths with 0 CARE` (`S_to_B_paths_with_0_CARE`) | **0** |
| `floors_with_fightless_start_to_boss_route` | **0** |
| `floors_with_careless_start_to_boss_route` | **0** |
| `floors_with_zero_combat_before_boss` (node count) | **0** |
| `event_count_histogram` | **{0=43, 1=57}** |
| `event_max` | **1** |
| floors with events > 1 (`event_floors_gt1`) | **0** |
| `combat_before_boss_histogram` (extra) | `{1=7, 2=43, 3=33, 4=16, 5=1}` (printed `{3=33, 2=43, 4=16, 1=7, 5=1}`) |
| `route_total` (all S→B across 100 gens) | **251** |
| `outliers_count` | **0** |

CoS: zero-combat S→B = 0 → **PASS**  
CoS v0.1.10: zero-CARE S→B = 0 → **PASS**  
Events ≤1 per floor → **PASS**

## 4) Floor 2 — seeds 0..99 (100 gens)

**Source:** `Floor2Test.floor2Path_seeds0to99_everyStartToBossHasCombatAndCare_eventsAtMostOne` asserts fightless=0, careless=0, eventsOver=0; histograms from temporary scratch on `PathGenerator.generate(floor=2, Random(seed))`.

| Metric | Measured |
|--------|----------|
| `S→B paths with 0 COMBAT` | **0** |
| `S→B paths with 0 CARE` | **0** |
| `floors_with_fightless_start_to_boss_route` | **0** |
| `floors_with_careless_start_to_boss_route` | **0** |
| `floors_with_zero_combat_before_boss` | **0** |
| `event_count_histogram` | **{0=43, 1=57}** |
| `event_max` | **1** |
| floors with events > 1 | **0** |
| `combat_before_boss_histogram` (extra) | `{1=7, 2=43, 3=33, 4=16, 5=1}` |
| `route_total` | **251** |

Floor2Test: `assertEquals(0, fightless)`, `assertEquals(0, careless)`, `assertEquals(0, eventsOver)` → **PASS**

## 5) Gradle unit tests

**Result:** **BUILD SUCCESSFUL**

| Suite | Tests | Failures | Errors | Skipped | Result |
|-------|-------|----------|--------|---------|--------|
| `PathGateV015Test` | 1 | 0 | 0 | 0 | **PASS** |
| `PathGeneratorTest` | 8 | 0 | 0 | 0 | **PASS** |
| `Floor2Test` | 9 | 0 | 0 | 0 | **PASS** |

`PathGeneratorTest` cases include: `everyStartToBossPath_hasAtLeastOneCare`, `enforceFloorRules_carelessBranch_getsRestOnLastNonCombat`, `everyStartToBossPath_hasAtLeastOneCombat`, `enforceFloorRules_fightlessBranch_getsCombatPreferMerge`, `noRestOnRow1_firstTierNeverRest_unlessCareLock`, `eventsCappedAtOnePerFloor`, `atLeastOneCombat_beforeBoss`, `sharedDepth_allBranchesAlign_noRowGaps`.

`Floor2Test` path case: `floor2Path_seeds0to99_everyStartToBossHasCombatAndCare_eventsAtMostOne`.

## 6) Gate verdict

**PASS** — APK present (17716583 bytes, mtime 2026-09-22 19:47:52 EDT); HEAD=`9e32324` matches claim; F1 and F2 seeds 0–99: S→B zero-COMBAT=0, S→B zero-CARE=0, event_max=1 and floors_with_events>1=0; PathGateV015Test + PathGeneratorTest + Floor2Test all green.
