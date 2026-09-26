# Floor 3+ loadout lock — mid-run save (v0.1.41-floor3)

Status: **Meta & Ops schema lock** (short).  
Flow / F3 rules: Architect + Engineer. Base slot: [`midrun-save-v0112.md`](midrun-save-v0112.md).

**Out of scope this WO:** Hub perks, remnant costs/earn, FX.

---

## Player rules (persist only)

| Rule | Lock |
|------|------|
| F1 / F2 | Unchanged vs `midrun-save-v0112.md`: pick **exactly 5**; climb loadout + Ashbrand persist as today |
| F3+ | After Confirm, **five skills + weapon** stay locked **for the current floor** |
| Floor advance | Unlock → player re-picks → Confirm locks again for the new floor |
| Save / resume | Floor lock + weapon + explainer flag **must** survive mid-run slot round-trip |
| Explainer | First F3 entry shows once per climb; flag prevents re-show after resume |

Write moments unchanged: node resolve / stair Continue / **loadout Confirm**. Never mid-beat.

---

## Additive fields on `midrun_v0112`

Keep existing `loadout` + `ashbrand`. Add:

```
midrun_v0112 {
  // ... existing fields ...
  floor: int                    // was 1|2; now 1..N (F3+)

  // --- v0.1.41-floor3 ---
  seen_f3_explainer: bool       // default false; true after first F3 explainer dismiss; clear only with mid-run slot

  // Floor-scoped lock (authoritative when floor >= 3)
  floor_loadout: {
    floor: int                  // floor this lock applies to (must match outer floor when locked)
    locked: bool                // false between unlock and Confirm (re-pick window)
    card_ids: string[]          // exactly 5 when locked; empty / prior ok while unlocked for re-pick
    weapon_id: string           // weapon locked with the five (mirror ashbrand.weapon_id while locked)
  } | null                      // null on F1/F2 (use climb `loadout` + `ashbrand` only)
}
```

### Behavior

| Case | Persist / restore |
|------|-------------------|
| F1 / F2 | Ignore `floor_loadout` (or leave null). `loadout` + `ashbrand` as v0.1.12 |
| F3+ locked | `floor_loadout.locked=true`, `floor==outer floor`, five ids + `weapon_id`; also keep `loadout`/`ashbrand` in sync for combat |
| F3+ re-pick window | `floor_loadout.locked=false` (or clear card_ids); resume must open Loadout, not Path with a stale lock |
| Resume mid-F3 | Restore five + weapon + `seen_f3_explainer`; never re-show explainer if flag true |
| New climb / Summary→Hub | CLEAR entire mid-run slot (flag + floor_loadout go with it) |

### Defaults (old slots)

Missing keys → `seen_f3_explainer=false`, `floor_loadout=null`. Corrupt / schema mismatch still CLEAR mid-run only (meta kept).

Engineer may bump `schema` string when codec requires it; field semantics above are the Meta lock.

---

## Explicit non-goals

- Hub offer ids / costs / OWNED
- Remnant bank or earn table
- FX / Wake math / enemy kits / path gen (frozen elsewhere)

---

## Checklist

- [x] `seen_f3_explainer` round-trips; no re-show after resume
- [x] F3+ locked five + `weapon_id` round-trip; re-pick window restores unlocked
- [x] F1/F2 path unchanged (no Hub/remnant touch)
- [x] Write only at existing legal moments

---

| Date | Change |
|------|--------|
| 2026-09-26 | Meta schema note from CoS WO v0.1.41-floor3 |
