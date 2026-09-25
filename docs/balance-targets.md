# Balance targets — vertical slice

Status: **stated targets for implementation and QA review.**  
These are **not** measured playtest or sim results. Do not cite them as proven.

**Compose locks** (master build prompt). Do not change these numbers without CoS.
**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

| Parameter | Target | Notes |
|-----------|--------|--------|
| Player max / start HP | **30** | Cap for heals. |
| Enemy HP (normal / trash) | **20** | Per normal combat node. |
| Enemy counter (normal / trash) | **7–9** | *v0.1.4 lock* — After Brace. |
| Boss HP | **28** | *CoS temporary lock* (Elliott PICK C — locked). |
| Boss counter | **6–9** | *CoS temporary lock* (Elliott PICK C — locked). |
| Player damage (successful hit effect) | **4–8** | Per card design in `cards-v0.md`. |
| Shop prices | **5–15** remnants | Per offer. |
| Cheapest Hub unlock | **15** remnants | Meta & Ops floor. |
| Climb win rate | **~84%** | *Target, not measured.* QA baseline = **tutorial-default loadout** climbs only. |
| Fight length | **~3.6 rounds** | *Target, not measured.* Same QA baseline scope. |

## CoS temporary locks

Elliott may still override later. Until then, Engineer/QA treat as slice law:

1. Boss **28 HP**, counter **6–9** (Elliott PICK C — locked). Trash/normal counter **7–9** (v0.1.4). Trash HP **20** (no trash retune).
2. ~84% / ~3.6 apply to **tutorial-default loadout climbs** for QA baseline (not all Hub unlocks).
3. Ashbrand Wake presentation: see `wake-v014.md` (FULL legendary + gold pin; SPARK quiet; Lv1 thresh **3**).

## How Engineer should use this

- Wire constants to the bands above (including distinct Boss vs trash rows).
- Do **not** tune live toward 84% / 3.6 unless QA provides measured data and CoS asks for a pass.

## How QA should use this

- Report **measured** win rate and round length on **tutorial-default loadout** climbs with method + sample size.
- Flag stalls separately from target gaps.
- Never reverse-engineer “proof” from these targets.

## Ownership

- Architect revises targets when CoS approves a design change.
- Meta & Ops owns remnant sinks/sources within shop 5–15 and unlock ≥15.
- QA owns measurement; Architect owns interpretation + proposed retune.

---

**Related:** v0.1.29-hubmore — Hostblood +2 max HP per climb (start full); base max unchanged; no trash/boss retune. See `hubmore-v0129.md`.
