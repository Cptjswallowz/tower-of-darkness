# QA GATE v0.1.7-routefight — 2026-09-22

Measured results only. Project: `/workspace/tower-of-darkness` (Compose Android). No balance retune; read-only + tests + this doc.

## APK

| Field | Value |
|-------|-------|
| Path | `/workspace/tower-of-darkness/tower-of-darkness-debug.apk` |
| Size | **17710732** bytes |
| mtime | **2026-09-22 08:00:55 EDT** (`stat -c`: `2026-09-22 08:00:55.391940006 -0400`) |

## Git

| Field | Value |
|-------|-------|
| `git log -1 --oneline` | `bd7915c v0.1.7-routefight: every Start→Boss path must include ≥1 COMBAT` |
| `git rev-parse HEAD` | `bd7915c8753b61eb0efc6227a9ef66e189489035` |
| HEAD vs claimed | **Matches** claimed commit `bd7915c` |
| Branch status | `## main...origin/main` |

## Commands run

```bash
stat -c '%n %s bytes %y' tower-of-darkness-debug.apk
git log -1 --oneline && git rev-parse HEAD && git status -sb
./gradlew :app:testDebugUnitTest --tests '*PathGate*' --tests '*PathGeneratorTest*' --info
```

Harness used: existing `PathGateV015Test` (already extended for v0.1.7 routefight: fightless Start→Boss routes) + `PathGeneratorTest` (includes `everyStartToBossPath_hasAtLeastOneCombat`, `enforceFloorRules_fightlessBranch_getsCombatPreferMerge`). No separate newer PathGate* file found under `app/src/test`.

## Path gate 100-floor sim (seeds 0..99)

- **API:** `PathGenerator.generate(floor = 1, rng = Random(seed.toLong()))`
- **Combat before boss:** non-START, non-BOSS nodes with `type == COMBAT`
- **Test:** `PathGateV015Test.pathGate_v015_100floorSim_seeds0to99`

### Combat / route fight

| Metric | Value |
|--------|-------|
| `floors_with_zero_combat_before_boss` | **0** |
| `floors_with_fightless_start_to_boss_route` | **0** |
| `combat_before_boss_histogram` | `{1=7, 2=36, 3=35, 4=20, 5=2}` (printed as `{3=35, 2=36, 4=20, 1=7, 5=2}`) |

CoS: 0 floors with 0 combats before boss → **PASS**  
CoS v0.1.7: 0 floors with a fightless Start→Boss route → **PASS**

### Events per floor

| Metric | Value |
|--------|-------|
| `event_count_histogram` | `{0=24, 1=76}` (printed as `{1=76, 0=24}`) |
| `event_max` | **1** |
| floors with events > 1 (`event_floors_gt1`) | **0** |

Criterion events ≤ 1 per floor → **PASS**

### Outliers

| Metric | Value |
|--------|-------|
| `outliers_count` | **0** |

## Gradle unit tests

| Suite | Tests | Failures | Errors | Skipped | Result |
|-------|-------|----------|--------|---------|--------|
| `PathGateV015Test` | 1 | 0 | 0 | 0 | **PASS** |
| `PathGeneratorTest` | 6 | 0 | 0 | 0 | **PASS** |

`PathGeneratorTest` cases: `sharedDepth_allBranchesAlign_noRowGaps`, `noRestOnRow1_firstTierNeverRest`, `atLeastOneCombat_beforeBoss`, `eventsCappedAtOnePerFloor`, `everyStartToBossPath_hasAtLeastOneCombat`, `enforceFloorRules_fightlessBranch_getsCombatPreferMerge`.

Overall Gradle: **BUILD SUCCESSFUL**

## Gate verdict

**PASS** — `floors_with_zero_combat_before_boss=0`; fightless Start→Boss routes=0; `event_max=1` and floors with events>1=0; PathGateV015Test + PathGeneratorTest all green; HEAD=`bd7915c`; APK present at measured size/mtime above.
