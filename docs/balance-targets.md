# Balance targets — vertical slice

Status: **stated targets for implementation and QA review.**  
These are **not** measured playtest or sim results. Do not cite them as proven.

**Compose locks** (master build prompt). Do not change these numbers without CoS.
**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

| Parameter | Target | Notes |
|-----------|--------|--------|
| Player max / start HP | **30** | Cap for heals. |
| Enemy HP (normal) | **20** | Per normal combat node. |
| Enemy counter (normal) | **7–11** | After Brace. |
| Boss HP | **28** | *CoS temporary lock* (Elliott may override). |
| Boss counter | **8–12** | *CoS temporary lock* (distinct from normals). |
| Player damage (successful hit effect) | **4–8** | Per card design in `cards-v0.md`. |
| Shop prices | **5–15** remnants | Per offer. |
| Cheapest Hub unlock | **15** remnants | Meta & Ops floor. |
| Climb win rate | **~84%** | *Target, not measured.* QA baseline = **tutorial-default loadout** climbs only. |
| Fight length | **~3.6 rounds** | *Target, not measured.* Same QA baseline scope. |

## CoS temporary locks (2026-09-20; Elliott skipped fork widget — locks stand)

Elliott may still override later. Until then, Engineer/QA treat as slice law:

1. Boss **28 HP**, counter **8–12** (normals stay 20 / 7–11).
2. ~84% / ~3.6 apply to **tutorial-default loadout climbs** for QA baseline (not all Hub unlocks).

## How Engineer should use this

- Wire constants / card numbers to the bands above (including distinct Boss row).
- Do **not** tune live toward 84% / 3.6 in the first APK unless QA provides measured data and CoS asks for a pass.

## How QA should use this

- After first playable APK: report **measured** win rate and round length on **tutorial-default loadout** climbs, plus shop/rest/meta stalls, with method + sample size.
- Flag stalls separately from target gaps.
- Never reverse-engineer “proof” from these targets.

## Ownership

- Architect revises targets when CoS approves a design change.
- Meta & Ops owns remnant sinks/sources within shop 5–15 and unlock ≥15.
- QA owns measurement; Architect owns interpretation + proposed retune.
