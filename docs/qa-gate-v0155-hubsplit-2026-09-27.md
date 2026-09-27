# QA gate — v0.1.55-hubsplit (2026-09-27)

**Gate result: PASS**

| Field | Measured |
|-------|----------|
| Tip SHA | `f480b889d7c7ed9e588c52447bf4b8c1c633c6d7` |
| Tip == HEAD == origin/main | **YES** |
| versionCode / versionName | **56** / **0.1.55-hubsplit** |
| Root APK size | **42425134** |
| Root APK md5 | `cb7a50ee8b23c0e09aff8c7175f2c0cd` |
| root == app-debug | **CMP_OK** |
| Suite | **51** classes / **401** tests / **0** fail / **0** err / **0** skip |
| vs Engineer claim 401 | **MATCH** |
| FAIL blockers | **none** |

Measured ET **2026-09-27 16:16 EDT**. No tag / no gh release / no messaging from this gate.

---

## A — Git

| Check | Result |
|-------|--------|
| `HEAD` | `f480b889d7c7ed9e588c52447bf4b8c1c633c6d7` |
| `origin/main` | `f480b889d7c7ed9e588c52447bf4b8c1c633c6d7` |
| Claimed tip | `f480b889d7c7ed9e588c52447bf4b8c1c633c6d7` |
| HEAD == origin/main == tip | **YES** |
| Subject | `test: fix PlateV0122 Seal-Warden PNG branch marker for hubsplit` |
| Author | Android Engineer \<android-engineer@tod.local\> |
| AuthorDate | 2026-09-27 16:13:08 -0400 |
| Parent | `04912a6c01588562b4f629d375264f40bf31b90e` (`feat(art): v0.1.55-hubsplit CoS overlay restage`) |
| `--stat` tip | `PlateV0122Test.kt` — 1 file, +7/−4 |
| Working tree | clean tracked; untracked only `docs/qa-gate-v0150-puffhold-2026-09-26.md` (pre-existing; not this tip) |

Baseline lock refs:

- v0.1.54-trollkept tag → `d6f4ed745b703d0b33aa66d608419930c443d534`
- v0.1.53-wakeblade tag present (audio baseline lineage)

---

## B — APK

| Check | Measured |
|-------|----------|
| Root | `/workspace/tower-of-darkness/tower-of-darkness-debug.apk` |
| Debug | `app/build/outputs/apk/debug/app-debug.apk` |
| Size (both) | **42425134** (matches CoS parent claim) |
| md5 (both) | `cb7a50ee8b23c0e09aff8c7175f2c0cd` (matches claim) |
| `cmp` root vs app-debug | **CMP_OK** |
| aapt badging | `versionCode='56' versionName='0.1.55-hubsplit'` · package `com.towerofdarkness.app` · sdk 26 / target 35 |
| Cert (apksigner) | Android Debug · SHA-256 `39e6fc727abe826f26c0e96932f49df626657e615ff47d652de7bdec1a500497` · SHA-1 `a60df7838c69fee28bbcf40def8e91791202329c` · MD5 `f5383b6c41cb12b6e02380b7b3e629f6` |

**APK match vs CoS kick: YES** (size + md5).

---

## C — FX/SFX + Kept locks

### FX/SFX LOCKED (vs v0.1.54-trollkept `d6f4ed7`)

| File | `git diff --numstat` vs 0.1.54 | vs parent tip |
|------|-------------------------------|---------------|
| `domain/combat/CombatFx.kt` | empty (0) | empty (0) |
| `domain/combat/PlumeGarnishKit.kt` | empty (0) | empty (0) |
| `domain/sound/IronclashSfx.kt` | empty (0) | empty (0) |
| `domain/sound/SoundBus.kt` | empty (0) | empty (0) |
| `ui/components/CombatFxOverlay.kt` | empty (0) | empty (0) |

Also empty vs `v0.1.53-wakeblade` for `IronclashSfx.kt` / `SoundBus.kt` (numstat blank). **Combat audio/FX behavior files unchanged.**

### Kept remnant formula LOCKED (vs 0.1.54)

| Check | Result |
|-------|--------|
| `ClimbKept.kt` vs `v0.1.54-trollkept` | **UNCHANGED** (`git diff --quiet` → YES) |
| `finishPayout` | +1 F1 combat · +2 Floor 2 · +3 Floor 3 · +1 Cave Troll · +5 Gate-Warden · +`HubOffers.ASH_TITHE_BONUS` (3) if `ash_tithe` owned |
| Bank rule | `kept = lines.sumOf`; no leftover `run_wallet` bank (`SummaryBankCommit.apply` → `bankBefore + payout.kept`) |

### Hub prices / unlock ids / skill text frozen

| Check | Result |
|-------|--------|
| `HubOffers.kt` vs 0.1.54 | **UNCHANGED** |
| Perks costs | Scout 8 · Extra rumor 6 · Hostblood 10 · Warm Ash 8 · Ash Tithe 8 |
| Skills costs | Host of Embers 12 · Iron Lesson 12 · Cold Draw 10 · Brand Lesson 10 · Spark Lesson 12 · Echo Lesson 12 |
| CTA labels | Buy / OWNED / Can't afford |

---

## D — Unit tests

Command: `./gradlew :app:testDebugUnitTest --rerun-tasks --offline`

| Field | Measured |
|-------|----------|
| BUILD | **SUCCESSFUL** |
| Wall clock | **9s** (START 16:15:13 ET → END 16:15:22 ET; gradle reported `BUILD SUCCESSFUL in 9s`) |
| XML dir | `app/build/test-results/testDebugUnitTest/` |
| Classes | **51** |
| Tests | **401** |
| Failures | **0** |
| Errors | **0** |
| Skipped | **0** |
| vs Engineer claim 401 | **MATCH** |

### Named suites

| Class | Result | Methods |
|-------|--------|---------|
| `HubsplitV0155Test` | **6/6** | `partC_apply_bankInvariant_menuAndContinue`; `partC_continueAlwaysEnabled_noAnimationGate`; `partC_goMenuAndGoHub_awaitLeaveRunSummary`; `hubRelics_earnLinesExact_ownedOnly_emptyCopy`; `hubSectionOrder_relicsPerksSkills_andOfferOrders`; `partA_tealCircleOff_sharedComposite` |
| `TrollkeptV0154Test` | **7/7** | (all PASS) |
| `WakebladeV0153Test` | **12/12** | (all PASS) |
| `SlashlayerV0152Test` | **13/13** | (all PASS) |
| `PuffholdV0150Test` | **7/7** | (all PASS) |
| `StrokeBothV0148Test` | **7/7** | (all PASS) |

---

## E — Cases 1–10

### 1. Title bust: NO cyan/blue/aqua ring — dark crop only — **PASS**

- `PortraitPlate.TITLE_TEAL_CIRCLE_ALLOWED = false`; `titleTealCircleAllowed()` returns false.
- `MainMenuScreen` uses `HeroShowcase(..., trophyUnlocks = gc.unlockedCards)` — does **not** pass `showTitleCircle = true`.
- `HeroShowcase` only draws `TitleTealCircle()` when `showTitleCircle && PortraitPlate.titleTealCircleAllowed()` — both false → dark circular crop only (`YouPortraitComposite` + `CircleShape` clip).
- Covered by `HubsplitV0155Test.partA_tealCircleOff_sharedComposite`.

### 2. Hub bust uses SAME composite as title — **PASS**

- Shared `YouPortraitComposite` in `HeroShowcase.kt`.
- Title: `MainMenuScreen` → `HeroShowcase(..., trophyUnlocks = gc.unlockedCards)`.
- Hub: `MetaHubScreen` → `HeroShowcase(..., size 72.dp, trophyUnlocks = gc.unlockedCards)`.
- Both routes through `YouPortraitComposite` with the same overlay stack.

### 3. With trophies: pauldron OR tooth readable; soot = edge ember specks; gate_sigil chest/gorget — **PASS** (source/art; phone visual unmeasured)

Evidence from `docs/art-audio/HUBSPLIT_OVERLAYS_v0.1.55.md` + `tools/prep_hubsplit_overlays_v0155.py` + drawable md5s:

| Overlay | Placement lock | Measured drawable md5 |
|---------|----------------|----------------------|
| soot_rim | sparse edge ember SPECKs (~1.76% cov); **not** wreath | `6d9431fac60ace69e7d1cefd06055655` |
| ash_pauldron | viewer-left shoulder ~x=35–120, y=85–185; cx≈72, cy≈132 | `bd1bf237f3174b870ecd573a70e6830d` |
| troll_tooth | lower-right cloak ~x=155–230, y=140–235; cx≈192, cy≈188 | `ef34f2f6d31975d2e674fec65d5e0e4c` |
| gate_sigil | chest/gorget ~cx=128, cy≈165; not hem | `3d0add7b710e6d8cf7d7677e5f4f3976` |

assets/portraits md5s identical to drawable. Art notes: pauldron “must read at Hub 120–160”. **Phone on-device visual not measured this gate** — static source/art evidence only.

### 4. Combat bust CLEAN — no overlays — **PASS**

- `CombatScreen` calls `HeroShowcase(rarity = COMMON, modifier = height(90.dp))` — **no** `trophyUnlocks` arg → default `emptySet()`.
- `YouPortraitComposite` with empty unlocks → `ClimbKept.unlockedOverlayDrawables` empty → no overlay Images.
- KDoc: “Combat must pass empty trophyUnlocks (no overlays).”

### 5. Hub sticky sections order: Relics → Perks → Skills — **PASS**

- `HubRelics.SECTION_ORDER = ["Relics","Perks","Skills"]`.
- `MetaHubScreen` LazyColumn stickyHeader order: SECTION_RELICS → SECTION_PERKS → SECTION_SKILLS.
- `HubsplitV0155Test.hubSectionOrder_relicsPerksSkills_andOfferOrders` PASS.

### 6. Relics: owned only; exact names/earn lines; empty copy; NO Buy — **PASS**

| unlock_id | name | earn line |
|-----------|------|-----------|
| soot_rim | Soot Rim | Reach Floor 2 |
| ash_pauldron | Ash Pauldron | Reach Floor 3 |
| troll_tooth | Troll Tooth | Kill Cave Troll |
| gate_sigil | Gate Sigil | Beat Gate-Warden |

- Empty: `HubRelics.EMPTY_COPY = "Nothing kept yet. Survive a floor."`
- `ownedRows` filters to unlocks only; MetaHub Relics block has name+earnLine Text only — no `HubCta.BUY` / `hubBuyOffer` in relics section (asserted in test).
- `hubRelics_earnLinesExact_ownedOnly_emptyCopy` PASS.

### 7. Scout under Perks; Cinder Vow (Host of Embers) under Skills; CTA + prices unchanged — **PASS**

- `PERK_IDS` starts with `scout_charge` (Scout @8, "+1 Free Scout / climb").
- `SKILL_IDS` starts with `host_of_embers` (Host of Embers @12, "Unlock skill Cinder Vow").
- Perk costs `[8,6,10,8,8]`; skill costs `[12,12,10,10,12,12]` — match hubsplit-v0155.md and HubOffers (unchanged vs 0.1.54).
- CTA via `HubBuyableRow`: Buy / OWNED / Can't afford.
- `HubOffers.kt` zero-diff vs 0.1.54.

### 8. PART C: Menu OR Continue banks Kept; Continue always enabled; Menu does not skip — **PASS**

- `finishRun`: `ClimbKept.finishPayout` → `SummaryBankCommit.apply` → optimistic local bank; `summaryCommitJob` writes `addRemnants(kept)` + trophy unlocks.
- `leaveRunSummary`: `summaryCommitJob?.join()` then clear mid-run then navigate — used by both `goMenu` and `goHub` when on Summary.
- `RunSummaryScreen`: Continue `enabled = true`; no `delay(2000)` / `continueReady` gate.
- Invariant helper: `bankAfter == bankBefore + keptShown`.
- Tests: `partC_apply_bankInvariant_menuAndContinue`, `partC_continueAlwaysEnabled_noAnimationGate`, `partC_goMenuAndGoHub_awaitLeaveRunSummary` all PASS.
- Example measured in test: F1+F2+F3+Cave Troll → kept **7**; bank 10→17.

### 9. Unit suite green — **PASS**

Measured XML: **51 / 401 / 0 / 0 / 0**. Engineer claim 401 **confirmed** (not invented).

### 10. Combat audio/FX unchanged vs 0.1.54 — **PASS**

Tip-diff **0** on CombatFx / PlumeGarnishKit / IronclashSfx / SoundBus (+ CombatFxOverlay). Wakeblade lineage SFX files also 0 vs 0.1.53. WakebladeV0153Test 12/12 + Slashlayer 13/13 still green.

---

## F — Fail blockers

**none**

---

## Packaging / docs note

- Docs present: `docs/hubsplit-v0155.md`, `docs/hubsplit-implement-v0155.md`, `docs/art-audio/HUBSPLIT_OVERLAYS_v0.1.55.md`.
- Tip commit is a PlateV0122Test marker fix on top of CoS overlay restage; APK already matched claimed md5 before that test-only tip (reconfirmed identical).
- This gate does **not** tag or release.

---

| Date | Actor | Note |
|------|-------|------|
| 2026-09-27 | QA gate runner | PASS measured; tip `f480b88`; APK md5 `cb7a50ee…`; suite 401 |
