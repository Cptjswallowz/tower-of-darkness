# QA Gate — v0.1.24-rumorcharge (2026-09-24)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.24-rumorcharge  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-24 ~12:20–12:21 EDT

**Tip (HEAD / origin/main):** `b1cbe11e284d3a8a08c08c277d7cdbf81434111d`  
**Claimed tip:** `b1cbe11e284d3a8a08c08c277d7cdbf81434111d` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `b1cbe11e284d3a8a08c08c277d7cdbf81434111d` |
| origin/main | `b1cbe11e284d3a8a08c08c277d7cdbf81434111d` |
| Match claimed tip | **YES** |
| Subject | `v0.1.24-rumorcharge: separate rumor vs scout wallets` |
| Author / date | Android Engineer \<android-engineer@tod.local\> — 2026-09-24 12:19:48 -0400 |
| Status | `main...origin/main` (clean tracking) |

**`--stat`:** GameController.kt (+109/− fogged-tap wallets + dispatcher), PathScreen.kt (+32/− Scout/rumor labels + fogged tap wiring), RumorChargeV0124Test.kt (**NEW** +211), docs (`rumorcharge-v0124.md` + notes on bossrest / midrun-save / rumors / save). **8 files, +422/−21.**

**Not in tip (frozen sources):** no WakeArt / SkillGlyph.kt / PortraitPlate / HeroShowcase / CombatScreen / BodyArt / VolumeArt / PathGenerator math / CombatSpeed / CombatEngine / portrait_*.png / wake assets / balance grant retunes. Tip touches only GameController + PathScreen + new test + docs.

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 18327100 | 2026-09-24 12:19:42 EDT | `f45fcaafa0537ae0b90b4f31db2b0950` |
| `app/build/outputs/apk/debug/app-debug.apk` | 18327100 | 2026-09-24 12:19:27 EDT | `f45fcaafa0537ae0b90b4f31db2b0950` |

Claimed: 18327100 / `f45fcaafa0537ae0b90b4f31db2b0950` → **MATCH** (root + build). Parent pre-check re-confirmed.

---

## C) Source / design notes (two wallets)

Design lock: `docs/rumorcharge-v0124.md`.

| Lock | Evidence |
|------|----------|
| Two ints, never shared boolean | `freeScoutCharges`, `rumorRerolls` (GameController.kt:138–140); grants stay 1/climb at start sites (:248–249, :291–292, :316–317) |
| Scout-first dispatcher | `dispatchFoggedNodeTap`: if scout>0 → `applyFreeScout` else if rumor>0 → `applyRumorReroll` else null (:578–603) |
| Rumor spend: text only, still fogged | `applyRumorReroll` (:546+); PathScreen `↻ rumor` + fogged non-choice → `onFoggedNodeTap` |
| UI at 0 still reportable | `rumorRerollsLabel(charges)` = `"Rumor re-rolls left: $charges"` (:611); PathScreen always shows label (:72–74) |
| Mid-run both fields | MidRunSlot `freeScoutCharges` / `rumorRerolls` encode as `free_scout_charges` / `rumor_rerolls` (MidRunSlot.kt:75–76, :180–181, :229–230); Continuity write/read (:1139–1140, :1165–1166) |
| Scout affordance hide at 0 | PathScreen shows Free Scout line only when `freeScoutCharges > 0` (:61–64) |

---

## D) Unit tests

Commands (measured this gate):

- `./gradlew :app:testDebugUnitTest --tests '*RumorChargeV0124*' --tests '*MidRunSaveV0112*'` → **BUILD SUCCESSFUL**
- `./gradlew :app:testDebugUnitTest` → **BUILD SUCCESSFUL**

Counts from XML under `app/build/test-results/testDebugUnitTest/`:

| Suite | Result |
|-------|--------|
| RumorChargeV0124Test | **8/8 PASS** |
| MidRunSaveV0112Test | **8/8 PASS** |
| WakeArtV0117Test | **4/4 PASS** |
| SkillGlyphV0119Test | **8/8 PASS** |
| PortraitsV0121Test | **7/7 PASS** |
| NobgV0123Test | **5/5 PASS** |
| PlateV0122Test | **7/7 PASS** |
| VolumeArtV0120Test | **11/11 PASS** |
| CombatSpeedV0111Test | **5/5 PASS** |
| StatusPipsV0113Test | **8/8 PASS** |
| BraceSyncV0114Test | **7/7 PASS** |
| PathGateV015Test | **1/1 PASS** |
| PathGeneratorTest | **7/7 PASS** |
| **Full `:app:testDebugUnitTest`** | **147/147 PASS** (24 classes, 0 fail/err/skip) |

### RumorChargeV0124Test methods

1. `separateWallets_spendingScoutDoesNotChangeRumorRerolls` — PASS  
2. `separateWallets_spendingRumorDoesNotChangeScoutCharges` — PASS  
3. `rumorSpend_decrements1to0_changesOnlyThatNode_secondTapNoOp` — PASS  
4. `scoutSpend_decrements_at0NoOp` — PASS  
5. `priority_whenBothPositive_firstTapSpendsScoutNotRumor` — PASS  
6. `midRunSave_roundTrip_preservesBothRemainingCharges` — PASS  
7. `uiString_rumorCounterAtZero_stillReportableAsZero` — PASS  
8. `bothZero_dispatchIsNoOp` — PASS  

---

## E) Cases 1–5 vs `docs/rumorcharge-v0124.md`

**1) Fresh floor / Clear Fog only: rumor 1 → tap one fogged ? → counter 0, that node's rumor text changed, other ? still fogged / other rumor unchanged**

- `rumorSpend_decrements1to0_changesOnlyThatNode_secondTapNoOp`: charges 1→0; fog rumor `"rerolled A"`; other node rumor stays `"keep me"`; still fogged path (via `applyRumorReroll` + separateWallets rumor path asserts `revealed=false`).
- `separateWallets_spendingRumorDoesNotChangeScoutCharges`: FoggedTapKind.RUMOR; rumor 1→0; scout stays 0; `revealed=false`; rumor text updated.
- UI: `uiString_rumorCounterAtZero_stillReportableAsZero` → `"Rumor re-rolls left: 0"`.

**2) Second tap another ?: no change, still 0 (no-op)**

- Same method: second `applyRumorReroll(..., charges=0)` → `null`; rumor text unchanged; `dispatchFoggedNodeTap` at scout=0/rumor=0 → `null`.
- `bothZero_dispatchIsNoOp` — PASS.

**3) Scout perk on (Keen Eye + Clear Fog): scout 1 + rumor 1; first fogged tap reveals type (Scout) and scout → 0; rumor still 1**

- `priority_whenBothPositive_firstTapSpendsScoutNotRumor`: kind=SCOUT; scout 1→0; rumor stays 1; rumor text unchanged (`"before"`); `scoutedTypeOnly=true`.
- `separateWallets_spendingScoutDoesNotChangeRumorRerolls`: same wallet isolation.
- Second tap on other fogged node then spends rumor (kind=RUMOR; rumor→0).

**4) Save/resume mid-floor: remaining free_scout_charges + rumor_rerolls match UI / MidRunSlot round-trip**

- `midRunSave_roundTrip_preservesBothRemainingCharges`: encode/decode with scout=0, rumor=1 → both preserved; also both-zero shape.
- MidRunSaveV0112Test **8/8**; Continuity maps both fields (GameController :1139–1140, :1165–1166).

**5) Frozen spot-check: art / Wake / glyphs / plates (nobg) / path math / 2x untouched**

- `git diff --stat HEAD~1..HEAD`: only GameController, PathScreen, RumorChargeV0124Test, docs — **no** WakeArt / SkillGlyph / PortraitPlate / PathGenerator / CombatSpeed / BodyArt / VolumeArt / balance grant files.
- Frozen suites green: WakeArtV0117 4/4, SkillGlyph 8/8, Portraits 7/7, Nobg 5/5, Plate 7/7, VolumeArt 11/11, CombatSpeed 5/5, MidRunSave 8/8, StatusPips 8/8, BraceSync 7/7, PathGate 1/1, PathGenerator 7/7; full **147/147** (prior 139 + new RumorCharge 8).

---

## Cases 1–5

| # | Claim | Verdict |
|---|-------|---------|
| 1 | Clear Fog only: rumor 1→0; that node rumor changed; other ? fogged / rumor unchanged | **PASS** — `rumorSpend_decrements1to0_changesOnlyThatNode_secondTapNoOp` + separateWallets rumor; label at 0 |
| 2 | Second tap at 0: no-op | **PASS** — second apply/dispatch null; `bothZero_dispatchIsNoOp` |
| 3 | Keen Eye + Clear Fog: first tap Scout (type reveal), scout→0, rumor stays 1 | **PASS** — `priority_whenBothPositive_*` + `separateWallets_spendingScout*` |
| 4 | Mid-run save round-trip both remaining charges | **PASS** — `midRunSave_roundTrip_preservesBothRemainingCharges`; MidRunSave 8/8 |
| 5 | Art/Wake/glyphs/plates/path math/2x frozen | **PASS** — tip excludes frozen sources; frozen suites green; full 147/147 |

---

## Docs

- Spec: `docs/rumorcharge-v0124.md`
- Prior gate format: `docs/qa-gate-v0123-nobg-2026-09-24.md`
- This gate: `docs/qa-gate-v0124-rumorcharge-2026-09-24.md`

---

## Overall

**Gate PASS** — tip `b1cbe11e284d3a8a08c08c277d7cdbf81434111d` (== origin/main == claimed); APK MATCH 18327100 / `f45fcaafa0537ae0b90b4f31db2b0950`; RumorChargeV0124 **8/8**; full suite **147/147**; cases 1–5 PASS.
