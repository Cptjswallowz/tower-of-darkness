# QA sim v0.1.3 — 2026-09-20

Status: **measured** headless sims (CoS GREENLIGHT).

- Project: `/workspace/tower-of-darkness`
- Test: `com.towerofdarkness.app.CombatSimV013Test`
- Fights per cohort: **2000**
- Seed: `seed = 1000 + i` (i = 0..1999)
- Round cap: **200** skill beats (hangs counted as failures / not wins)
- Loadout: `CardCatalog.defaultLoadoutIds` (5) + Ashbrand lv1 charge 0
- Rounds metric: completed player skill beats (`diceTumble` → `resolveSkill`) until finished

## Asserts

| Check | Result |
|-------|--------|
| Grey-pick violations == 0 | **PASS** (ash=0, seal=0) |
| Confirm reject 4 and 6 (`LOADOUT_MIN/MAX==5`, `confirmAccepts`) | **PASS** |
| `GameController.confirmLoadout` rejects `selected.size != LOADOUT_MAX` | **PASS** (source line verified) |
| Skip → 5 skills + Ashbrand lv1 | **PASS** (catalog + `skipTutorial` source) |

## Ash Wretch (`Enemy.normal(GOBLIN)`, HP=20)

| Metric | Value |
|--------|-------|
| Wins | 1014 / 2000 |
| Hangs (round cap) | 0 |
| Winrate | 0.5070 |
| Sum skill beats | 8614 |
| Mean skill beats | 4.3070 |
| Mean final `state.round` | 4.8000 |
| Full Wake events | 1005 |
| Full Wake / fight (avg procs) | 0.5025 |
| Fights with ≥1 Full Wake | 1005 / 2000 (0.5025) |
| SPARK events | 1046 |
| SPARK / fight | 0.5230 |
| Fights with ≥1 SPARK | 848 / 2000 (0.4240) |
| Grey violations | 0 |

## Seal-Warden (`Enemy.boss()`, HP=28)

| Metric | Value |
|--------|-------|
| Wins | 469 / 2000 |
| Hangs (round cap) | 0 |
| Winrate | 0.2345 |
| Sum skill beats | 10648 |
| Mean skill beats | 5.3240 |
| Mean final `state.round` | 6.0895 |
| Full Wake events | 1984 |
| Full Wake / fight (avg procs) | 0.9920 |
| Fights with ≥1 Full Wake | 1984 / 2000 (0.9920) |
| SPARK events | 1270 |
| SPARK / fight | 0.6350 |
| Fights with ≥1 SPARK | 970 / 2000 (0.4850) |
| Grey violations | 0 |

## SIM_REPORT_JSON

```
SIM_REPORT_JSON={"fights":2000,"base_seed":1000,"round_cap":200,"ash_wretch":{"wins":1014,"hangs":0,"winrate":0.5070,"sum_skill_beats":8614,"mean_skill_beats":4.3070,"mean_final_round":4.8000,"full_wake_events":1005,"full_wake_per_fight":0.5025,"fights_with_full_wake":1005,"full_wake_fight_rate":0.5025,"spark_events":1046,"spark_per_fight":0.5230,"fights_with_spark":848,"spark_fight_rate":0.4240,"grey_violations":0},"seal_warden":{"wins":469,"hangs":0,"winrate":0.2345,"sum_skill_beats":10648,"mean_skill_beats":5.3240,"mean_final_round":6.0895,"full_wake_events":1984,"full_wake_per_fight":0.9920,"fights_with_full_wake":1984,"full_wake_fight_rate":0.9920,"spark_events":1270,"spark_per_fight":0.6350,"fights_with_spark":970,"spark_fight_rate":0.4850,"grey_violations":0},"assert_grey_zero":true,"assert_confirm_4_6_rejected":true,"assert_skip_default_5_ashbrand":true}
```

## Anomalies / engine vs design

Engine resolveSkill independently queues CHAIN full and SPARK via pendingFullWake / pendingSpark; resolveWeapon fires both on the same beat (full Wake first, then half-Wake SPARK).

Full Wake log match: message contains `Wake!` (e.g. `Ashbrand Wake! (4)`).
SPARK log match: message contains ` spark (` (e.g. `Ashbrand spark (2)`).

## Delta vs prior pre-fix report

Prior report values: Ash SPARK events = **912**, Seal-Warden SPARK events = **898**; winrate = **50.70% / 0.05%** (Ash / Seal).

| Cohort | Prior SPARK events | New SPARK events | Delta SPARK events | Prior winrate | New winrate | Delta winrate |
|--------|-------------------:|-----------------:|-------------------:|--------------:|------------:|--------------:|
| Ash Wretch | 912 | 1046 | 134 | 0.5070 | 0.5070 | 0.0000 |
| Seal-Warden | 898 | 1270 | 372 | 0.0005 | 0.2345 | 0.2340 |

## Method

1. `CombatEngine(Random(1000+i)).start(default5, enemy, Ashbrand lv1)`
2. Loop: `diceTumble` → grey asserts → `resolveSkill` → optional `resolveWeapon` → `resolveEnemy` → `readyNext`
3. Count skill beats, Wake!/spark events from log deltas, wins / hangs

Generated EDT: see test run timestamp in gradle output.
