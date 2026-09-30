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
| Wins | 2000 / 2000 |
| Hangs (round cap) | 0 |
| Winrate | 1.0000 |
| Sum skill beats | 8284 |
| Mean skill beats | 4.1420 |
| Mean final `state.round` | 4.1420 |
| Full Wake events | 0 |
| Full Wake / fight (avg procs) | 0.0000 |
| Fights with ≥1 Full Wake | 0 / 2000 (0.0000) |
| SPARK events | 983 |
| SPARK / fight | 0.4915 |
| Fights with ≥1 SPARK | 832 / 2000 (0.4160) |
| Grey violations | 0 |

## Seal-Warden (`Enemy.boss()`, HP=28)

| Metric | Value |
|--------|-------|
| Wins | 2000 / 2000 |
| Hangs (round cap) | 0 |
| Winrate | 1.0000 |
| Sum skill beats | 8777 |
| Mean skill beats | 4.3885 |
| Mean final `state.round` | 4.3885 |
| Full Wake events | 0 |
| Full Wake / fight (avg procs) | 0.0000 |
| Fights with ≥1 Full Wake | 0 / 2000 (0.0000) |
| SPARK events | 1041 |
| SPARK / fight | 0.5205 |
| Fights with ≥1 SPARK | 839 / 2000 (0.4195) |
| Grey violations | 0 |

## SIM_REPORT_JSON

```
SIM_REPORT_JSON={"fights":2000,"base_seed":1000,"round_cap":200,"ash_wretch":{"wins":2000,"hangs":0,"winrate":1.0000,"sum_skill_beats":8284,"mean_skill_beats":4.1420,"mean_final_round":4.1420,"full_wake_events":0,"full_wake_per_fight":0.0000,"fights_with_full_wake":0,"full_wake_fight_rate":0.0000,"spark_events":983,"spark_per_fight":0.4915,"fights_with_spark":832,"spark_fight_rate":0.4160,"grey_violations":0},"seal_warden":{"wins":2000,"hangs":0,"winrate":1.0000,"sum_skill_beats":8777,"mean_skill_beats":4.3885,"mean_final_round":4.3885,"full_wake_events":0,"full_wake_per_fight":0.0000,"fights_with_full_wake":0,"full_wake_fight_rate":0.0000,"spark_events":1041,"spark_per_fight":0.5205,"fights_with_spark":839,"spark_fight_rate":0.4195,"grey_violations":0},"assert_grey_zero":true,"assert_confirm_4_6_rejected":true,"assert_skip_default_5_ashbrand":true}
```

## Anomalies / engine vs design

Engine resolveSkill independently queues CHAIN full and SPARK via pendingFullWake / pendingSpark; resolveWeapon fires both on the same beat (full Wake first, then half-Wake SPARK).

Full Wake log match: message contains `Wake!` (e.g. `Ashbrand Wake! (4)`).
SPARK log match: message contains ` spark (` (e.g. `Ashbrand spark (2)`).

## Delta vs prior pre-fix report

Prior report values: Ash SPARK events = **912**, Seal-Warden SPARK events = **898**; winrate = **50.70% / 0.05%** (Ash / Seal).

| Cohort | Prior SPARK events | New SPARK events | Delta SPARK events | Prior winrate | New winrate | Delta winrate |
|--------|-------------------:|-----------------:|-------------------:|--------------:|------------:|--------------:|
| Ash Wretch | 912 | 983 | 71 | 0.5070 | 1.0000 | 0.4930 |
| Seal-Warden | 898 | 1041 | 143 | 0.0005 | 1.0000 | 0.9995 |

## Method

1. `CombatEngine(Random(1000+i)).start(default5, enemy, Ashbrand lv1)`
2. Loop: `diceTumble` → grey asserts → `resolveSkill` → optional `resolveWeapon` → `resolveEnemy` → `readyNext`
3. Count skill beats, Wake!/spark events from log deltas, wins / hangs

Generated EDT: see test run timestamp in gradle output.
