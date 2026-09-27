# One bank — v0.1.56-bankone (Meta P0 currency lie)

Status: **Meta & Ops design lock**. Owner: Meta & Ops.  
Baseline tip: `f480b889d7c7ed9e588c52447bf4b8c1c633c6d7` (v0.1.55-hubsplit).

**Frozen:** FX/SFX, F3 rumors, loadout lock, Hub Relics/Perks/Skills headers + prices, **Kept payout numbers** from v0.1.54 as coded (`ClimbKept`: Gate-Warden **+5**, not a retune — ignore WO draft “Gate+2”).

**Scope:** Tower of Darkness greenfield only.

---

## 1. Audit — remnant displays today

| Surface | UI copy today | Value source | Store / field |
|---------|---------------|--------------|---------------|
| **Title** (MainMenu Hub button) | `Hub · N remnants` via `HubOffers.titleBankLine` | `gc.remnantsBank` | MetaStore **`remnants_bank`** (`KEY_REMNANTS`) |
| **Hub** header | `Remnants  N` via `HubOffers.hubBankLine` | `gc.remnantsBank` | MetaStore **`remnants_bank`** |
| **Summary** | `Kept: N remnants` + breakdown | `RunSummaryData.kept` / `keptLines` | **Ephemeral** this-run payout only — **not** a bank key |
| **Floor Path HUD** | `HP X · Y rem` | `gc.runWallet` | Mid-run **`run_wallet`** (in `midrun_v0112` JSON) |
| **FloorBreak HUD** | `HP X · Y rem · Ashbrand…` | `gc.runWallet` | Mid-run **`run_wallet`** |
| **Shop HUD** | `HP X / max · Y rem` | `gc.runWallet` | Mid-run **`run_wallet`** |

### MetaStore preference keys (DataStore `tower_meta`)

| Pref key | Role | Fate |
|----------|------|------|
| `remnants_bank` | Sole meta currency | **KEEP** — only bank |
| `unlocked_cards` | Hub OWNED + trophies | **KEEP** |
| `midrun_v0112` | Climb slot (includes `run_wallet`) | **KEEP** (purse inside slot) |
| `tutorial_seen` / `meta_hp_bonus` / `hubkeep_v0128_migrated` | Unrelated | **KEEP** |

**Not found:** separate Title bank key, Hub bank key, or last-run payout cache preference. No second meta int for “display bank.”

### Lie

Floor/Path/Shop/FloorBreak label **`rem` / remnants** on **`run_wallet`** reads as the same currency as Title/Hub bank. Players see purse `0` as “bank wiped.”

---

## 2. Spec — one wallet

| Rule | Lock |
|------|------|
| Sole bank | MetaStore **`remnants_bank`** only |
| Title N | Reads `remnants_bank` via **same getter** as Hub (`meta.remnantsBank` → `gc.remnantsBank`) |
| Hub N | Same |
| Forbidden | Last-run payout / `kept` / `remnantsEarned` / optimistic cache shown **as** bank |
| Summary Kept | **This-run payout only** (`ClimbKept.finishPayout` → `kept`) |
| Continue **and** Menu | `remnants_bank += Kept`, persist trophies **same transaction**, log, **then** navigate |
| New Climb | **Must NOT** zero `remnants_bank`. May zero `run_wallet` (purse starts 0) |
| Floor HUD | **Must NOT** say `rem` / `remnants`. Use **`HP 30 · purse 0`** or **`HP 30`** only |
| Shop / FloorBreak | Same: `purse` or omit — never `rem`/`remnants` for purse |
| Debug update | Same package + signing → keep DataStore `tower_meta`; bank + trophies one transaction |
| Payout table | Unchanged vs coded 0.1.54 (F1+1 / F2+2 / F3+3 / Troll+1 / Gate+**5** / Tithe+3) |

```
Title N  ==  Hub N  ==  remnants_bank
Kept N   ==  this climb payout only (not bank)
purse    ==  run_wallet (climb shop); never labeled remnants
```

---

## 3. Summary commit (tighten)

```
@ finishRun:
  payout = ClimbKept.finishPayout(...)
  show Kept = payout.kept   // sheet only
@ Continue OR Menu leave:
  single MetaStore edit:
    remnants_bank = prev + payout.kept
    unlocked_cards += new_trophy_ids
  log: BANK write prev=X add=Y now=Z source=continue|menu
  then navigate (clear mid-run after commit)
```

| Invariant | Lock |
|-----------|------|
| Post-leave bank | `now == prev + Kept shown` |
| Menu | Same commit as Continue — **no** skip |
| Continue | Always enabled after payout computed / sheet shown |
| Dual-write race | Prefer **one** `edit { }` for bank+unlocks; avoid separate `addRemnants` then `unlockCard` without shared transaction if that can desync |

---

## 4. Floor HUD copy (Engineer)

| Screen | Replace | With (either) |
|--------|---------|----------------|
| PathScreen | `HP ${hp} · ${runWallet} rem` | `HP ${hp} · purse ${runWallet}` **or** `HP ${hp}` |
| FloorBreakScreen | `… · ${runWallet} rem · …` | `… · purse ${runWallet} · …` **or** drop purse |
| ShopScreen | `… · ${runWallet} rem` | `… · purse ${runWallet}` **or** HP only |

Treasure/Event **choice** copy (“Take remnants (+N)”) still means **purse gain** this climb — out of HUD scope; do not retune amounts.

---

## 5. Release notes (required three keys)

| Surface | Key / field found | Action |
|---------|-------------------|--------|
| Title | `remnants_bank` | **Keep** — sole getter with Hub |
| Hub | `remnants_bank` | **Keep** — sole getter with Title |
| Floor | `run_wallet` shown as **`rem`** | **Delete** `rem`/`remnants` label from floor HUD (purse field stays for shop; rename UI to `purse` or omit) |

No Title/Hub duplicate pref keys to delete — audit found both already on `remnants_bank`.

---

## 6. Explicit non-goals

- Change Kept amounts / Hub prices / Relics section / F3 / loadout / FX/SFX
- Bank leftover `run_wallet` into meta (still superseded by Kept)
- New DataStore bank key

---

## Checklist

- [ ] Title + Hub same `remnants_bank` getter; no kept-as-bank
- [ ] Summary Kept = this-run only; Continue+Menu one txn + log line
- [ ] New Climb does not zero bank; floor HUD no rem/remnants
- [ ] Debug update preserves MetaStore
- [ ] Release notes list three keys + floor rem label deleted

---

| Date | Change |
|------|--------|
| 2026-09-27 | Meta audit + lock from CoS WO v0.1.56-bankone |
