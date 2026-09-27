# v0.1.54-trollkept — combined lock

This file holds **PART A** (Architect — F3 floor rumors) and **PART B** (Meta — Kept/trophies).  
FX / SFX locked elsewhere this WO.

---

# PART A — F3 floor rumors (Architect)

Status: **design lock** (Content). Owner: Game Architect.

**When:** F3 enter — after Ash-Warden stair, **before** loadout Confirm.  
**Scope:** Tower of Darkness greenfield only.

**Related:** `floor3-v0141.md`, `floor3-save-v0141.md` (`seen_f3_explainer`), `rumorcharge-v0124.md` (wallets — **not** spent here), `rumors.md` (node fog — separate).

## Explicit non-goals (PART A)

- Reveal **node types**
- Change F3 spawn **20 / 40 / 40**
- Change Cave Troll **Club / Hide / Hit** math
- Touch F1 / F2 **rumorRerolls** / **freeScoutCharges** / Scout / fog / per-combat pick-5

## Generate (exactly 2)

On F3 enter (before Confirm), generate **exactly two** floor rumors and persist them.

| Rule | Lock |
|------|------|
| Spend wallets | **Do NOT** spend `rumorRerolls` or `freeScoutCharges` |
| Count | Always **2** lines |
| Sticky | Persist for the floor / mid-run resume; do not re-roll from node fog wallets |

### Slot 0 — ALWAYS Cave Troll kit tell

Even pick **1** of:

1. `A clubber in the dark hides behind stone before it swings.`
2. `Something down here stacks Brace, then brings a club.`
3. `The caves keep a thick one. Club first, Hide if you let it live.`

### Slot 1 — path tell

Even pick **1** of:

1. `Two fights stand before the last rest.`
2. `A stall or a cache is still open on this floor.`
3. `The halls favor orcs more than goblins.`

## UI

| Surface | Lock |
|---------|------|
| F3 loadout | Both lines **above Confirm** |
| F3 map header | **Two-line strip** (parchment chrome like node rumors) |

### First-ever F3 explainer

When `seen_f3_explainer` is false, also show (then set flag true):

`From this floor on, read the rumors, pick five + weapon, then lock until the next stair.`

Key: `seen_f3_explainer` (existing mid-run — `floor3-save-v0141.md`).

## Persistence keys (mid-run)

| Key | Type | Lock |
|-----|------|------|
| `floor_rumors` | `string[2]` | Exact two chosen lines; index **0** = troll tell, index **1** = path tell |
| `seen_f3_explainer` | `bool` | Existing; true after first F3 explainer dismiss |

Write `floor_rumors` on F3 enter before Confirm. Round-trip on mid-run resume. Clear with mid-run slot (Summary→Hub / new climb wipe).

Do **not** write into meta; do **not** decrement `rumor_rerolls` / `free_scout_charges` on generate.

## Engineer checklist (PART A)

- [ ] F3 enter → exactly 2 lines; slot0 troll pool; slot1 path pool; even pick
- [ ] Persist `floor_rumors[2]`; no wallet spend
- [ ] Loadout: both lines above Confirm; map header two-line parchment strip
- [ ] `seen_f3_explainer` + exact explainer string once
- [ ] No node-type reveal; no spawn/kit/F1–F2 wallet/fog/pick-5 changes

---

# PART B — Kept remnants + trophies (Meta & Ops)

Status: **Meta & Ops design lock**. Owner: Meta & Ops.  
Architect / Engineer implement flow + climb flags. Art owns Hub bust PNG overlays.

**Frozen this WO:** combat FX/SFX, Hub offer **prices**, skill math, Floor 4.

**Scope:** Tower of Darkness greenfield only.

---

## 1. Kept payout (win OR death)

At `finishRun(won)` → Run Summary, **always** add `kept` to `remnants_bank`.  
**Never** wipe `remnants_bank`. **Never** wipe OWNED / unlock set.

### Formula (additive lines)

| Line | Amount | Predicate (this climb) |
|------|--------|------------------------|
| F1 combat | **+1** | Any Floor **1** combat **won** |
| Floor 2 | **+2** | Floor **2** entered |
| Floor 3 | **+3** | Floor **3** entered |
| Cave Troll | **+1** | Cave Troll **killed** this climb |
| Gate-Warden | **+5** | Gate-Warden **beaten** (climb win on F3 boss) |
| Ash Tithe | **+3** | `ash_tithe` ∈ meta unlocks (Hub OWNED) |

```
kept = Σ amounts where predicate true
remnants_bank := remnants_bank + kept     // additive only
```

**Max kept** (all lines + Tithe): 1+2+3+1+5+3 = **15**.

### Supersedes hubmore summary bank path

| Was (hubmore-v0129) | Now (this WO) |
|---------------------|---------------|
| `banked = runWallet + ashTitheBonus` | `kept` = formula above; bank `+= kept` |
| Ash Tithe as separate +3 on wallet | Ash Tithe is the **+3** line inside Kept when owned |

Mid-run **`run_wallet`** stays climb spend (Path Shop). At summary: clear wallet; **do not** also bank leftover wallet (avoids double-pay). Shop spend this climb is gone with the wallet — Kept is the only bank add.

Death and win both run the same formula (Gate-Warden line only if beaten).

### Summary UX

| Element | Lock |
|---------|------|
| Headline | **"Kept: N remnants"** (`N == kept`) |
| Breakdown | List each earned line (label + amount); omit zero lines |
| New trophy | If any trophy awarded **this** summary → show **trophy display name** under Kept for **2s**, then enable / show **Continue** |
| Continue | → Hub (clear mid-run slot as today) |

Suggested breakdown labels: `Floor 1 combat` / `Floor 2` / `Floor 3` / `Cave Troll` / `Gate-Warden` / `Ash Tithe`.

---

## 2. Trophy unlocks (`meta_v0.unlocks`)

Award **once** the **first** time earned (win **or** death). Persist in the same unlock string set as Hub OWNED (`unlocked_cards` / `meta_v0.unlocks`). Idempotent: if already owned, do not re-award / do not re-show as new.

| unlock_id | Display name | Earn when (first time) |
|-----------|--------------|------------------------|
| `soot_rim` | Soot Rim | Reach **Floor 2** |
| `ash_pauldron` | Ash Pauldron | Reach **Floor 3** |
| `troll_tooth` | Troll Tooth | Kill **Cave Troll** |
| `gate_sigil` | Gate Sigil | Beat **Gate-Warden** |

```
@ finishRun:
  for each trophy whose earn predicate true:
    if id not in unlocks → unlocks += id; new_trophies += display name
  remnants_bank += kept
  show Kept + breakdown; if new_trophies non-empty → flash names 2s under Kept
```

Trophies are **not** Hub buy rows. **Do not** invent remnant costs for them.

---

## 3. Climb flags (mid-run / finishRun inputs)

Track on the climb (mid-run slot and/or controller). Persist across save/resume so Kept/trophies stay correct after Continue.

```
// Additive on midrun_v0112 (or equivalent climb state)
climb_kept: {
  f1_combat_won: bool      // any F1 combat win
  floor2_entered: bool     // true once floor becomes 2 (stair Continue / enter F2)
  floor3_entered: bool     // true once floor becomes 3
  cave_troll_killed: bool  // this climb
  gate_warden_beaten: bool // true on Gate-Warden win path into finishRun(won=true)
}
```

| Flag | Set when |
|------|----------|
| `f1_combat_won` | Resolve a Floor 1 combat win (hallway or boss if counted as combat — **any** F1 combat win) |
| `floor2_entered` | Stair Continue → Floor 2 (or `floor` becomes 2) |
| `floor3_entered` | Enter Floor 3 |
| `cave_troll_killed` | Cave Troll combat ends in player win |
| `gate_warden_beaten` | Gate-Warden win → `finishRun(won=true)` |

Defaults missing → all false. Clear with mid-run slot on Summary→Hub / New climb.

Derivation note: `floor >= 2` / `floor >= 3` may set entered flags on write/resume if flags missing (migration), but Cave Troll / F1 combat / Gate still need explicit flags.

---

## 4. Save keys (Engineer)

### Meta (`meta_v0`) — existing keys

| Key | Type | Use |
|-----|------|-----|
| `remnants_bank` | int | `+= kept` at summary; Hub spend; **never wipe** |
| `unlocked_cards` (unlocks set) | string set | Hub OWNED **and** trophy ids (`soot_rim`, `ash_pauldron`, `troll_tooth`, `gate_sigil`, `ash_tithe`, …) |

**No new DataStore key required** for trophies (reuse unlock set).

Optional UX (not required for lock):

```
last_run_summary: {
  won: bool
  kept: int
  breakdown: [{ line_id, amount }]
  new_trophy_ids: string[]
} | null
```

### Mid-run — additive flags

| Field | Persist |
|-------|---------|
| `climb_kept.*` (or flat `f1_combat_won`, …) | Yes — see §3 |
| Existing `floor`, loadout, ashbrand, wallet, … | Unchanged |

Hub prices / offer ids **unchanged** (Scout…Echo Lesson ladder).

---

## 5. Hub bust (rules only)

| Rule | Lock |
|------|------|
| Hub copy | Still **"Remnants N"** (`remnants_bank`) |
| Bust | Baseline bust when **no** trophy owned |
| Any trophy owned | Art overlay(s) on Hub bust — **Art owns PNGs**; Meta: if `soot_rim` ∪ `ash_pauldron` ∪ `troll_tooth` ∪ `gate_sigil` intersects unlocks → "trophy bust" mode |
| Stacking overlays | Art decides layering when multiple trophies owned; Meta does not invent PNG names |

---

## 6. Explicit non-goals

- Combat FX / SFX
- Hub offer prices / skill math
- Floor 4
- Changing Path Shop wallet earn table mid-climb (shop still spends `run_wallet`)

---

## Checklist

- [ ] `kept` formula win + death; bank additive only
- [ ] Summary "Kept: N" + breakdown; new trophy name 2s under Kept
- [ ] Trophy ids once into unlocks; never wipe bank/OWNED
- [ ] Climb flags survive mid-run save/resume
- [ ] Hub Remnants N; trophy bust when any trophy owned
- [ ] FX/SFX / Hub prices / Floor 4 untouched

---

| Date | Change |
|------|--------|
| 2026-09-27 | Meta lock from CoS WO v0.1.54-trollkept PART B |

**Related (Hub UI):** v0.1.55-hubsplit — Relics / Perks / Skills sections; Relics display-only. See `hubsplit-v0155.md`. Kept formula unchanged.

**Related:** v0.1.56-bankone — one `remnants_bank`; floor HUD must not say rem. See `bankone-v0156.md`.

---

**Follow-on (v0.1.57):** Victory-only `"The seal breaks" +3` (full clear 15); Title bank from disk — [`titlebank-v0157.md`](titlebank-v0157.md).

---

**Follow-on:** Scrap pouch + relic combat/summary effects + Forge Rest/Shop — [`forge-v0158.md`](forge-v0158.md).
