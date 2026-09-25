# QA Gate — v0.1.28-hubkeep (2026-09-24)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.28-hubkeep  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-24 ~20:46–20:47 EDT

**Tip (HEAD / origin/main):** `dea7282a92a8ee1164d8aef7ed0ab3dd37cbdbdd`  
**Claimed tip:** `dea7282a92a8ee1164d8aef7ed0ab3dd37cbdbdd` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `dea7282a92a8ee1164d8aef7ed0ab3dd37cbdbdd` |
| origin/main | `dea7282a92a8ee1164d8aef7ed0ab3dd37cbdbdd` |
| Match claimed tip | **YES** |
| Subject | `v0.1.28-hubkeep: Hub ADD not replace; baseline rumor 1/floor` |
| Author / date | Android Engineer \<android-engineer@tod.local\> — 2026-09-24 20:45:20 -0400 |
| Status | `main` up to date with `origin/main`; working tree clean |

**`--stat`:** MetaStore.kt (+migration), HubOffers.kt (baseline rumor + stack + migrateUnlocksForHubkeep), GameController.kt, PathScreen.kt (always show rumor counter), HubKeepV0128Test.kt (new), HubV0127Test.kt (assert flips), docs (`hubkeep-v0128.md`, hub-v0127 / meta-economy / rumorcharge notes). **10 files, +369/−31.**

**Not in tip (frozen sources):** no WakeArt / PathGenerator / CombatSpeed / hallway-boss-You portrait packs / 2x retune. PathScreen only un-gates rumor counter display (in-scope hubkeep: always show baseline rumor).

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 18839331 | 2026-09-24 20:45:44 EDT | `7115d2a826939a5e845cba259ff08f2f` |
| `app/build/outputs/apk/debug/app-debug.apk` | 18839331 | 2026-09-24 20:45:33 EDT | `7115d2a826939a5e845cba259ff08f2f` |

Claimed: 18839331 / `7115d2a826939a5e845cba259ff08f2f` → **MATCH** (root + build).

---

## C) Source / design notes (hubkeep)

Design lock: `docs/hubkeep-v0128.md`.

| Lock | Evidence |
|------|----------|
| Baseline rumor **1/floor** (no Hub buy) | `HubOffers.BASELINE_RUMOR_PER_FLOOR=1`; `freshClimb_noHubBuys_rumorStartsAt1` → rumorRerollsAtClimbStart/OnFloorAdvance = **1**; freeScout = **0** |
| Extra rumor stacks → **2/floor** | `afterExtraRumorBuy_rumorStartsAt2`: bank 20→**14** (−6); rumor = **2** climb + floor-advance |
| Scout ownership → Free Scout **1** on climb | `afterScoutBuy_freeScoutCharges1OnClimb`: freeScoutChargesAtClimbStart = **1** (empty = 0) |
| Force-close: OWNED + bank same; ADD not wipe | `forceClose_metaReload_ownedAndBankSame`: bank 30−8−6=**16**; meta_hp_2 + rest_heal_plus kept; encode/decode; CTA OWNED; rebuy null |
| Loadout commons still present; gates do not hide | `loadoutStillHasBaselineCommons_gatesDoNotHide`: hostflint, cinder_step, iron_mantle, emberbrand, dust_veil in pool; Cinder Vow / Grave Nail gated until Host/Iron |
| Migration: bank kept; legacy → Hub OWNED | `migration_mapsLegacyFlags_keepsBank_noDoubleBill`: bank 42; rumor_clarity→extra_rumor; cinder_vow→host; grave_nail→iron; OWNED CTAs; rumor=2 scout=1; idempotent |
| Title rem == Hub rem | `titleRemEqualsHubRem_afterBuy`: bank 15→9; `Hub · 9 remnants` / `Remnants  9` |
| Frozen: combat art / packs / Wake / path / 2x | tip `--stat` excludes those cores; frozen suites green |

---

## D) Unit tests

Commands (measured this gate):

- `./gradlew :app:testDebugUnitTest --tests '*HubKeepV0128*'` → **BUILD SUCCESSFUL**
- `./gradlew :app:testDebugUnitTest` → **BUILD SUCCESSFUL**

Counts from XML under `app/build/test-results/testDebugUnitTest/`:

| Suite | Result |
|-------|--------|
| HubKeepV0128Test | **7/7 PASS** |
| HubV0127Test | **9/9 PASS** |
| PacksV0126Test | **12/12 PASS** |
| WakeArtV0117Test | **4/4 PASS** |
| SkillGlyphV0119Test | **8/8 PASS** |
| PortraitsV0121Test | **7/7 PASS** |
| NobgV0123Test | **5/5 PASS** |
| CombatSpeedV0111Test | **5/5 PASS** |
| MidRunSaveV0112Test | **8/8 PASS** |
| RumorChargeV0124Test | **8/8 PASS** |
| PathGateV015Test | **1/1 PASS** |
| PathGeneratorTest | **7/7 PASS** |
| **Full `:app:testDebugUnitTest`** | **175/175 PASS** (27 classes, 0 fail/err/skip) |

### HubKeepV0128Test methods

1. `freshClimb_noHubBuys_rumorStartsAt1` — PASS  
2. `afterExtraRumorBuy_rumorStartsAt2` — PASS  
3. `afterScoutBuy_freeScoutCharges1OnClimb` — PASS  
4. `forceClose_metaReload_ownedAndBankSame` — PASS  
5. `loadoutStillHasBaselineCommons_gatesDoNotHide` — PASS  
6. `migration_mapsLegacyFlags_keepsBank_noDoubleBill` — PASS  
7. `titleRemEqualsHubRem_afterBuy` — PASS  

**Measured (from passing assertions):**

- Fresh climb empty unlocks: `rumorRerolls` = **1** (climb + floor-advance); `freeScoutCharges` = **0**
- After Extra rumor buy: bank **20 → 14**; `rumorRerolls` = **2**
- After Scout buy: `freeScoutCharges` = **1**
- Force-close round-trip: bank **30 → 16**; unlocks keep meta_hp_2 + rest_heal_plus + scout + extra_rumor; OWNED; rebuy null
- Loadout commons present: hostflint, cinder_step, iron_mantle, emberbrand, dust_veil

---

## E) Cases 1–6 vs `docs/hubkeep-v0128.md`

**1) Fresh climb no Hub buys: rumor re-rolls left starts at 1**

- `freshClimb_noHubBuys_rumorStartsAt1`: BASELINE_RUMOR_PER_FLOOR=1; rumorRerollsAtClimbStart(empty)=**1**; rumorRerollsOnFloorAdvance(empty)=**1**; freeScoutCharges=**0**.

**2) After Extra rumor OWNED: starts at 2**

- `afterExtraRumorBuy_rumorStartsAt2`: applyBuy Extra → bank 20→**14**; rumorRerollsAtClimbStart/OnFloorAdvance = **2**.

**3) After Scout OWNED: Free Scout charges 1 on map**

- `afterScoutBuy_freeScoutCharges1OnClimb`: freeScoutChargesAtClimbStart(after)=**1**; empty=**0**.

**4) Force-close: still owned, bank same**

- `forceClose_metaReload_ownedAndBankSame`: buys Scout+Extra → bank **16**; old perks kept (ADD); encode/decode equal; CTA OWNED; applyBuy Scout null.

**5) Loadout still has original commons (Hostflint, Cinder Step, …)**

- `loadoutStillHasBaselineCommons_gatesDoNotHide`: hostflint, cinder_step, iron_mantle, emberbrand, dust_veil present; Cinder Vow / Grave Nail absent until Host/Iron; after Host+Iron skills appear and hostflint still present.

**6) Frozen: combat art / packs / Wake / path / 2x untouched**

- tip `--stat` does **not** touch WakeArt / PathGenerator / CombatSpeed / packs portraits / 2x. **What DID change:** MetaStore migration, HubOffers baseline+stack+migrate, GameController grant wiring, PathScreen always-show rumor counter (hubkeep in-scope), HubKeepV0128Test + HubV0127Test assert flips, docs.
- Frozen suites green: HubV0127 9/9, PacksV0126 12/12, WakeArtV0117 4/4, SkillGlyph 8/8, Portraits 7/7, Nobg 5/5, CombatSpeed 5/5, MidRunSave 8/8, RumorChargeV0124 8/8, PathGate 1/1, PathGenerator 7/7; full **175/175**.

---

## Cases 1–6

| # | Claim | Verdict |
|---|-------|---------|
| 1 | Fresh climb no Hub buys: rumor starts at 1 | **PASS** — measured rumorRerolls=**1** |
| 2 | After Extra rumor OWNED: starts at 2 | **PASS** — measured rumorRerolls=**2** (bank 20→14) |
| 3 | After Scout OWNED: Free Scout charges 1 on map | **PASS** — measured freeScoutCharges=**1** |
| 4 | Force-close: still owned, bank same | **PASS** — bank 30→16; encode/decode; OWNED; old perks kept |
| 5 | Loadout still has baseline commons | **PASS** — Hostflint, Cinder Step, Iron Mantle, Emberbrand, Dust Veil present |
| 6 | Frozen combat art / packs / Wake / path / 2x | **PASS** — tip excludes frozen cores; PathScreen rumor chrome only; frozen suites + full 175/175 |

---

## Docs

- Spec: `docs/hubkeep-v0128.md`
- Prior gate format: `docs/qa-gate-v0127-hub-2026-09-24.md`
- This gate: `docs/qa-gate-v0128-hubkeep-2026-09-24.md`

---

## Overall

**Gate PASS** — tip `dea7282a92a8ee1164d8aef7ed0ab3dd37cbdbdd` (== origin/main == claimed); APK MATCH 18839331 / `7115d2a826939a5e845cba259ff08f2f` (root + build); HubKeepV0128 **7/7**; full suite **175/175**; cases 1–6 PASS.
