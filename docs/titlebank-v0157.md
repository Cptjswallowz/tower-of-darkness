# Title bank + seal Kept — v0.1.57-titlebank (Meta)

Status: **Meta & Ops design lock**. Owner: Meta & Ops.  
Baseline tip: `9e96464fe22288764d20035f9fcf6f3b82217b74` (v0.1.56-bankone).

**Frozen:** FX/SFX, Hub Relics/Perks/Skills sections, relic flags/prices, mid-climb purse economy, combat, overlay art.

**Scope:** Tower of Darkness greenfield only.  
**Implements from:** Engineer. This doc only.

**Related:** `bankone-v0156.md` (one bank), `trollkept-v0154.md` (Kept formula), `hubsplit-v0155.md` (Summary Continue|Menu commit).

---

## PART A (P0) — Title N = MetaStore bank

### Lie

| Surface | Copy | Binds today | Risk |
|---------|------|-------------|------|
| Title (MainMenu Hub button) | `Hub · N remnants` via `HubOffers.titleBankLine` | `gc.remnantsBank` | Same field is **optimistically** set to `prev + Kept` in `finishRun` *before* MetaStore write |
| Hub header | `Remnants  N` via `HubOffers.hubBankLine` | `gc.remnantsBank` | Same |
| Disk | — | MetaStore `remnants_bank` | Still `prev` until `leaveRunSummary` `commitSummaryBank` |

Title must never show Kept (or purse) as bank. After Summary leave and after force-close reopen, **Title N == Hub N == last disk `remnants_bank`**.

### Spec

| Rule | Lock |
|------|------|
| Sole bank | MetaStore **`remnants_bank`** (`KEY_REMNANTS`) |
| Title N | Same getter path as Hub → `meta.remnantsBank` (Flow → UI). **Not** `RunSummaryData.kept`, **not** `run_wallet`, **not** finishRun optimistic local |
| Hub N | Same getter |
| After Continue \| Menu | Commit bank+trophies (existing one txn) **then** Title/Hub recompose from **disk** (`write.now` / `meta.remnantsBank.first()` / Flow emit) |
| Force-close reopen | Title == Hub == last persisted bank |
| VM | If Title were bound to run-scoped / RunState cache, **subscribe MetaStore** instead |
| finishRun | **Do not** bump `gc.remnantsBank` to `prev+Kept` for Title/Hub. Sheet shows Kept from `summary.kept` / `keptLines` only. Local bank field stays disk value until leave commit |
| Log (Title visible / MainMenu enter) | `TITLE bank=N source=metastore` |

```
Title N  ==  Hub N  ==  remnants_bank (disk)
Kept N   ==  this-run payout only (Summary sheet)
```

### Engineer checklist (PART A)

- [ ] Remove or stop optimistic `remnantsBank = committed.remnantsBank` in `finishRun` (bankone forbidden: kept-as-bank)
- [ ] Title + Hub still `titleBankLine` / `hubBankLine` on MetaStore-backed value only
- [ ] `leaveRunSummary`: after `commitSummaryBank`, set local from `write.now` / re-read Flow; navigate after
- [ ] Log `TITLE bank=N source=metastore` when MainMenu shows bank line (or on compose enter)
- [ ] Force-kill after Summary *before* leave → reopen Title shows **prev** (disk); after leave → Title shows **now**
- [ ] New Climb still must not zero `remnants_bank` (bankone)

---

## PART B (P1) — Victory seal line + purse copy

### Kept amounts (unchanged + one victory-only line)

| Line | Amount | Predicate |
|------|--------|-----------|
| Floor 1 combat | **+1** | F1 combat won |
| Floor 2 | **+2** | Floor 2 entered |
| Floor 3 | **+3** | Floor 3 entered |
| Cave Troll | **+1** | Cave Troll killed |
| Gate-Warden | **+5** | Gate-Warden beaten |
| **The seal breaks** | **+3** | **Victory only** (`won == true` / seal breaks). **Death: omit this line** |
| Ash Tithe | **+3** | `ash_tithe` ∈ meta unlocks (OWNED; unchanged) |

```
kept = Σ amounts where predicate true
# Do NOT add run_wallet / purse to bank
remnants_bank += kept   // on Continue|Menu commit only (existing)
```

| Case | Lock |
|------|------|
| Full clear climb lines (no Tithe) | 1+2+3+1+5+**3** = **15** |
| Death | No `"The seal breaks"` line; other death totals **unchanged** vs prior (no seal +3) |
| Purse → bank | **Forbidden** (supersedes hubmore; bankone) |
| Label exact | `"The seal breaks"` (Summary breakdown under Kept) |
| Gate line | Keep existing Gate-Warden **+5** row; seal +3 is **additional**, victory-only |

Ash Tithe still stacks when owned (max then 18). Do not retune Tithe this WO.

### Summary UI

| Element | Lock |
|---------|------|
| Kept headline | `Kept: N remnants` (existing) |
| Breakdown | Existing lines + victory-only seal line when `won` |
| Under Kept list (muted) | Exact: **`Shop purse ends with the climb.`** |
| Purse | Ends with climb; never converted to bank |

### Engineer checklist (PART B)

- [ ] `ClimbKept.finishPayout(..., won: Boolean)` (or equivalent) adds `KeptLine("seal_breaks", "The seal breaks", 3)` iff `won`
- [ ] Death path: no seal line; Gate+5 still only if gate beaten (unchanged predicates otherwise)
- [ ] Full clear without Tithe → kept **15**
- [ ] `RunSummaryScreen`: muted Bone line under Kept list: `Shop purse ends with the climb.`
- [ ] No `run_wallet` / purse add into `remnants_bank`

---

## Explicit non-goals

- FX/SFX, Hub section chrome, relic flags/prices, mid-climb purse economy amounts, combat, overlay art
- Retune F1/F2/F3/Troll/Gate/Tithe amounts (only **add** victory seal +3)
- New MetaStore bank key

---

## Release notes (Meta)

| Item | Action |
|------|--------|
| Title bank | Must track MetaStore only; log `TITLE bank=N source=metastore` |
| finishRun optimistic bank bump | **Delete** as Title/Hub source |
| Kept | + victory-only `"The seal breaks" +3`; full clear 15 (no Tithe) |
| Summary | Muted: `Shop purse ends with the climb.` |

---

| Date | Change |
|------|--------|
| 2026-09-27 | Meta lock from CoS WO v0.1.57-titlebank |
