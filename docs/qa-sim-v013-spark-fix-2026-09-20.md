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
| Sum skill beats | 8875 |
| Mean skill beats | 4.4375 |
| Mean final `state.round` | 4.4375 |
| Full Wake events | 2000 |
| Full Wake / fight (avg procs) | 1.0000 |
| Fights with ≥1 Full Wake | 2000 / 2000 (1.0000) |
| SPARK events | 1071 |
| SPARK / fight | 0.5355 |
| Fights with ≥1 SPARK | 918 / 2000 (0.4590) |
| Grey violations | 0 |

## Seal-Warden (`Enemy.boss()`, HP=28)

| Metric | Value |
|--------|-------|
| Wins | 1758 / 2000 |
| Hangs (round cap) | 0 |
| Winrate | 0.8790 |
| Sum skill beats | 14654 |
| Mean skill beats | 7.3270 |
| Mean final `state.round` | 7.4480 |
| Full Wake events | 3733 |
| Full Wake / fight (avg procs) | 1.8665 |
| Fights with ≥1 Full Wake | 2000 / 2000 (1.0000) |
| SPARK events | 1761 |
| SPARK / fight | 0.8805 |
| Fights with ≥1 SPARK | 1236 / 2000 (0.6180) |
| Grey violations | 0 |

## SIM_REPORT_JSON

```
SIM_REPORT_JSON={"fights":2000,"base_seed":1000,"round_cap":200,"ash_wretch":{"wins":2000,"hangs":0,"winrate":1.0000,"sum_skill_beats":8875,"mean_skill_beats":4.4375,"mean_final_round":4.4375,"full_wake_events":2000,"full_wake_per_fight":1.0000,"fights_with_full_wake":2000,"full_wake_fight_rate":1.0000,"spark_events":1071,"spark_per_fight":0.5355,"fights_with_spark":918,"spark_fight_rate":0.4590,"grey_violations":0},"seal_warden":{"wins":1758,"hangs":0,"winrate":0.8790,"sum_skill_beats":14654,"mean_skill_beats":7.3270,"mean_final_round":7.4480,"full_wake_events":3733,"full_wake_per_fight":1.8665,"fights_with_full_wake":2000,"full_wake_fight_rate":1.0000,"spark_events":1761,"spark_per_fight":0.8805,"fights_with_spark":1236,"spark_fight_rate":0.6180,"grey_violations":0},"assert_grey_zero":true,"assert_confirm_4_6_rejected":true,"assert_skip_default_5_ashbrand":true}
```

## Anomalies / engine vs design

Engine resolveSkill independently queues CHAIN full and SPARK via pendingFullWake / pendingSpark; resolveWeapon fires both on the same beat (full Wake first, then half-Wake SPARK).

Full Wake log match: message contains `Wake!` (e.g. `Ashbrand Wake! (4)`).
SPARK log match: message contains ` spark (` (e.g. `Ashbrand spark (2)`).

## Delta vs prior pre-fix report

Prior report values: Ash SPARK events = **912**, Seal-Warden SPARK events = **898**; winrate = **50.70% / 0.05%** (Ash / Seal).

| Cohort | Prior SPARK events | New SPARK events | Delta SPARK events | Prior winrate | New winrate | Delta winrate |
|--------|-------------------:|-----------------:|-------------------:|--------------:|------------:|--------------:|
| Ash Wretch | 912 | 1071 | 159 | 0.5070 | 1.0000 | 0.4930 |
| Seal-Warden | 898 | 1761 | 863 | 0.0005 | 0.8790 | 0.8785 |

## Method

1. `CombatEngine(Random(1000+i)).start(default5, enemy, Ashbrand lv1)`
2. Loop: `diceTumble` → grey asserts → `resolveSkill` → optional `resolveWeapon` → `resolveEnemy` → `readyNext`
3. Count skill beats, Wake!/spark events from log deltas, wins / hangs

Generated EDT: see test run timestamp in gradle output.
