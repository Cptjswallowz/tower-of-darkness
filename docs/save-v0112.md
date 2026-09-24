# Mid-run save — v0.1.12-save (WO Elliott)

Status: **design lock** (player / flow rules).  
**Meta & Ops owns** the full one-slot schema and storage detail — see [`midrun-save-v0112.md`](midrun-save-v0112.md). This note is the Architect pointer + WO locks only.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

---

## Player rules (locked)

| Rule | Lock |
|------|------|
| Slots | **One** mid-run save slot |
| Write moments | **Node resolve** (leave Combat / Shop / Rest / Event / Treasure / Boss after resolve); **stair Continue** (Seal-Warden win → F2); **loadout lock** |
| Do **not** write | Mid-beat / mid-Wake / mid-dice hold |
| Cold start | If climb in progress → **never mid-beat combat**. Default → **Path**. Exception: `pending_stair_continue` → **FloorBreak** interstitial (**"The stair turns."** + Continue) — not Path with a stair CTA |
| Menu | **Continue** resumes the same way (Path, or FloorBreak if stair pending) |
| New climb | Requires **confirm wipe** of the mid-run slot before starting fresh |

Clear the mid-run slot on Run Summary → Hub (existing meta sketch). Meta (`meta_v0`) always persists separately.

**Stair resume:** Path CTA for pending stair is **wrong** vs `floor2-v019.md`. Use Meta’s `resume: FloorBreak` / `pending_stair_continue` → FloorBreak screen.

---

## Ownership

- **Architect (this note):** flow locks above; frozen systems list.
- **Meta & Ops:** slot fields, schema version, corrupt discard, bank vs wallet, F1→F2 fields alignment with `floor2-v019.md` persist.
- **Engineer:** serialize at write moments; cold-start → Path (default) or FloorBreak if pending stair; Menu Continue / New-climb confirm.

---

## Frozen (do not change this pass)

- 2x combat timings (`combat-2x-v0111.md`)
- Path / rest-before-boss (`bossrest-v0110.md`)
- Floor persist rules (`floor2-v019.md`)
- Wake / counters / shop / swap / Scout

---

## Checklist

- [ ] One mid-run slot; write on node resolve / stair Continue / loadout lock only
- [ ] Never write mid-beat / Wake
- [ ] Cold start → **Path** (default) or **FloorBreak** if pending stair; Menu **Continue**; New climb confirm wipe
- [ ] Meta owns full slot spec; frozen systems untouched

## Meta schema

Full fields / F1→F2 alignment: [`midrun-save-v0112.md`](midrun-save-v0112.md).

---

**Related:** v0.1.24-rumorcharge — persist both Scout + rumor re-roll counts. See `rumorcharge-v0124.md`.
