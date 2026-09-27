# bankone v0.1.56 — Android audit (Engineer)

Baseline: Meta PART `docs/bankone-v0156.md` (present; followed).
Tip work vs hubsplit `f480b889d7c7ed9e588c52447bf4b8c1c633c6d7`.

## Display → key → action

| Surface | UI | Key / field | Action |
|---------|-----|-------------|--------|
| Title (MainMenu Hub button) | `Hub · N remnants` via `HubOffers.titleBankLine` | MetaStore `remnants_bank` (`KEY_REMNANTS`) via `gc.remnantsBank` | **Kept** — same getter as Hub |
| Hub header | `Remnants  N` via `HubOffers.hubBankLine` | MetaStore `remnants_bank` via `gc.remnantsBank` | **Kept** — same getter as Title |
| Run Summary | `Kept: N` + breakdown | Ephemeral `RunSummaryData.kept` / `ClimbKept.finishPayout` | **Kept** — this-run payout only (not bank) |
| Floor Path HUD | was `HP X · Y rem` | mid-run `run_wallet` (`gc.runWallet`) | **Renamed** → `HP X · purse Y` (deleted rem/remnants label) |
| FloorBreak HUD | was `… · Y rem · …` | `run_wallet` | **Renamed** → `… · purse Y · …` |
| Shop HUD | was `… · Y rem` | `run_wallet` | **Renamed** → `… · purse Y` |
| Shop offer price lines | `N rem` | purse spend copy | Unchanged (not bank; out of HUD rename scope) |

## Pref keys

| Pref | Fate |
|------|------|
| `remnants_bank` | Sole meta bank — KEEP |
| `unlocked_cards` | Trophies / Hub OWNED — KEEP |
| `midrun_v0112` / `run_wallet` | Climb purse — KEEP |

No separate Title/Hub bank keys found (audit agrees with Meta PART).

## Fixes in this build

1. **Partial write:** `MetaStore.commitSummaryBank` — one `edit { }` for bank+=Kept + trophy unlocks (replaces separate `unlockCard` loop then `addRemnants`).
2. **Climb wipe:** `startNewRun` / tutorial climb paths already zeroed only `runWallet`; documented MUST NOT zero `remnants_bank`; tests assert no `remnantsBank = 0` on climb start.
3. **Floor HUD:** Path / FloorBreak / Shop status lines use `purse`, not `rem`/`remnants`.
4. **Leave commit:** Continue (`MetaHub`) and Menu (`MainMenu`) both call `leaveRunSummary` → one-txn write + `BANK write prev=X add=Y now=Z source=continue|menu` then clear mid-run and navigate.
