# Rumor re-roll vs Free Scout charges — v0.1.24-rumorcharge (WO Elliott)

Status: **implemented** (feature tip; no tag/release yet).  
**No** balance retune (grant amounts stay current Meta ladder / climb rules).

**Portraits PASS.** Frozen this pass: art / Wake / glyphs / plates (`nobg-v0123`) / path **math** / 2x / combat portraits / trash circles.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

---

## Two wallets (never one shared boolean)

| Wallet | Purpose | Meta source (unchanged grant) |
|--------|---------|--------------------------------|
| **(1) Rumor re-roll charges** (`rumorRerolls`) | Re-roll rumor **text** on a still-fogged `?` node | `rumor_clarity` → **1 / climb** at existing grant sites (`startNewRun` / tutorial start) |
| **(2) Free Scout perk charges** (`freeScoutCharges`) | Reveal **real node type** (Keen Eye / `scout_charge`) | `scout_charge` → **1 / climb** at the same sites |

Never collapse these into one shared flag or one shared counter. Spending one must not decrement the other.

**Grant note:** WO text said “1 per floor”; tree already grants **1 per climb** (not re-granted on `continueAfterFloorBreak`). Kept as-is — no grant retune.

Aligns with `bossrest-v0110.md` §3 (Clear Fog ≠ Free Scout) and `midrun-save-v0112.md` fields `rumor_rerolls` + `free_scout_charges`.

---

## Rumor re-roll

1. Grant unchanged (`rumor_clarity` → 1/climb).
2. Player taps a **fogged `?`** (via dispatcher when Scout wallet is empty, or explicit `↻ rumor` on a selectable fogged choice) → spend **1** → **new rumor text** on that node only (still fogged; type not revealed).
3. UI decrements the rumor counter **immediately** (`mutableStateOf` + `rumorRerollsLabel`).
4. At **0**: show **"Rumor re-rolls left: 0"** (Clear Fog unlocked); further taps are **no-op**.

---

## Free Scout

1. Own charge count (Keen Eye); separate from rumor wallet.
2. Tap fogged `?` with Scout charges ≥ 1 → reveal **real node type**; spend **1**; do not enter.
3. At **0**: hide the Scout affordance line; taps no longer route to Scout.

---

## Same node, both available

`GameController.dispatchFoggedNodeTap` / `onFoggedNodeTap`:

1. If **Scout charges > 0** and node eligible → **Scout** (type reveal).
2. Else if **rumor charges > 0** and node eligible → **rumor re-roll**.
3. Else → **no-op** (no state change).

Selectable path choices still **enter** on primary chip tap; fogged non-choice taps use the dispatcher. Explicit `↻ rumor` on selectable fogged choices spends the **rumor** wallet only.

---

## Save / resume

Persist **both** remaining counts (`free_scout_charges`, `rumor_rerolls`) — already on `MidRunSlot`; Continuity writes/reads both.

---

## Engineer / QA checklist

- [x] Two ints — never one shared boolean (`freeScoutCharges`, `rumorRerolls`)
- [x] Rumor tap: fogged + charges≥1 → new rumor text; UI −1; at 0 show "Rumor re-rolls left: 0" + no-op
- [x] Scout tap: own wallet; type reveal; at 0 hide affordance + no-op
- [x] Same node: Scout first if >0, else rumor if >0; never silent-eat wrong wallet
- [x] Save/resume restores both remaining counts
- [x] Unit: `RumorChargeV0124Test`
- [ ] Device QA: Clear Fog only — tap fogged non-choice spends rumor 1→0; second tap no-op; label shows 0
- [ ] Device QA: Keen Eye + Clear Fog — first fogged tap scouts; rumor stays 1; next tap (or ↻) spends rumor
- [ ] Device QA: mid-run Continuity after spend restores both counters
- [x] No art / Wake / glyphs / plate / path math / 2x / balance grant changes

---

**Related:** v0.1.27-hub — Hub **Extra rumor** (6) / **Scout** (8) unlock grants; wallets stay separate. See `hub-v0127.md`.

---

**Related:** v0.1.28-hubkeep — baseline 1 rumor re-roll/floor always; Hub Extra rumor stacks. See `hubkeep-v0128.md`.
