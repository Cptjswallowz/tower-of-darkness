# Hubsplit v0.1.55 — engineer implement note

**WO:** v0.1.55-hubsplit. Baseline tip `d6f4ed7` (feature); release docs tip may lag.
**Status:** Code WIP — PART C + Hub sections + PART A code landed. **HOLD assembleDebug / tip report until Art overlay PNG refresh.**

## Wired

### PART C — Summary bank commit
- `SummaryBankCommit.apply` pure helper; invariant `bank_after == bank_before + Kept`.
- `finishRun` applies payout snapshot locally + launches `summaryCommitJob` (MetaStore unlocks + `addRemnants`).
- `leaveRunSummary(dest)` awaits job, re-syncs bank/unlocks from store, clears mid-run, navigates.
- `goMenu` / `goHub` from Summary both use `leaveRunSummary` (Menu cannot skip bank).
- Continue **always enabled** on Summary (no 2s trophy-flash gate). Trophy names still display.

### PART B — Hub sections
- `HubRelics` — earn lines exact; owned-only rows; empty copy exact.
- `MetaHubScreen` — sticky header (bust + `HubOffers.hubBankLine`); sticky Relics → Perks → Skills.
- Perk/Skill CTAs, prices, ids, effect lines unchanged.

### PART A — overlays (code)
- `PortraitPlate.TITLE_TEAL_CIRCLE_ALLOWED = false`.
- MainMenu no longer passes `showTitleCircle = true`.
- Shared `YouPortraitComposite` for title + Hub; combat passes empty trophies.
- Overlay PNGs still current Elliott md5s (0.1.54) until Art refresh re-copy.

## Stubbed / HOLD
- Final `assembleDebug` + APK md5 tip report — wait for Art overlay PNG refresh under `assets/portraits/`.
- Do not tag/release.

## Tests
- `HubsplitV0155Test` — PART C invariant + Menu/Continue leave path; Relics empty/earn lines; section order; teal off.
- `NobgV0123Test.titleTealCircle_off_hubsplitSupersedes` updated.
