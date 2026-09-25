# QA Gate — v0.1.29-hubmore (2026-09-25)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.29-hubmore  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-25 ~07:37–07:38 EDT

**Tip (HEAD / origin/main):** `71448b768aef6d617d9b39bcf0344dbf762ded06`  
**Claimed tip:** `71448b768aef6d617d9b39bcf0344dbf762ded06` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `71448b768aef6d617d9b39bcf0344dbf762ded06` |
| origin/main | `71448b768aef6d617d9b39bcf0344dbf762ded06` |
| Match claimed tip | **YES** |
| Subject | `v0.1.29-hubmore: Hostblood / Warm Ash / Ash Tithe once-buy climb perks` |
| Author / date | Android Engineer \<android-engineer@tod.local\> — 2026-09-25 07:36:35 -0400 |
| Status | `main` up to date with `origin/main`; working tree clean |

**`--stat`:** HubOffers.kt (+Hostblood/Warm Ash/Ash Tithe + climb helpers), GameController.kt (climbMaxHp, pendingStartBrace, finishRun tithe), CombatEngine.kt (`initialBrace` param only), MetaHubScreen / RestScreen / ShopScreen (climbMaxHp heal clamps), HubMoreV0129Test.kt (new), HubV0127Test.kt (assert flips), docs (`hubmore-v0129.md` + notes). **13 files, +447/−27.**

**Not in tip (frozen sources):** no WakeArt / PathGenerator / CombatSpeed / hallway-boss-You portrait packs / 2x retune / trash-boss HP numbers / Cinder-Grave effect bodies. CombatEngine change is brace-init wiring only (no art / pace / pack / path).

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 18839331 | 2026-09-25 07:36:24 EDT | `e901f2b89e7edc107f3d305e49fab14f` |
| `app/build/outputs/apk/debug/app-debug.apk` | 18839331 | 2026-09-25 07:36:12 EDT | `e901f2b89e7edc107f3d305e49fab14f` |

Claimed: 18839331 / `e901f2b89e7edc107f3d305e49fab14f` → **MATCH** (root + build).

---

## C) Source / design notes (hubmore)

Design lock: `docs/hubmore-v0129.md`.

| Lock | Evidence |
|------|----------|
| Hub live = **7** offers; costs 8/6/12/12/10/8/8 | `hubList_sevenOffers_costsLocked`: `HubOffers.all.size=7`; ids scout/extra_rumor/host_of_embers/iron_lesson/**hostblood**/**warm_ash**/**ash_tithe** |
| 6 rem: Extra Buyable; others Can't afford | `bank6_extraRumorBuyable_othersCantAfford`: Extra=`BUY`; Scout/Host/Iron/Hostblood/Warm Ash/Ash Tithe=`CANT_AFFORD` |
| Hostblood → climbMax **32** (base 30); stacks with metaHpBonus → **34** | `buyHostblood_climbMaxIs32_startFull_baseStays30`: bank 20→**10**; climb=`32`; with metaHpBonus=2 →`34`; empty=`30` |
| Hostblood does **not** write `meta_hp_bonus` | `hostbloodDoesNotWriteMetaHpBonusKey_formulaOnly`: bonus from unlock set only; `meta_hp_2` alone with metaHpBonus=0 → climb **30**; hubBuyOffer → `unlockCard` only (no `setMetaHpBonus`) |
| Ash Press heal follows climbMaxHp | `ashPressHealCap_followsClimbMax_withHostblood`: start HP 30/max 32 → after Ash Press HP **32** |
| Warm Ash → Brace **2** first combat | `buyWarmAsh_firstCombatShowsBrace2BeforeSkill`: bank 20→**12**; `warmAshBraceAtClimbStart=2`; `engine.start(...).brace=2` |
| Warm Ash **not** FloorBreak / midrun resume | Source: `pendingStartBrace` set only in `startNewRun` / `skipTutorial` / `completeTutorial`; `continueAfterFloorBreak` does **not** set it; `applyMidRunSlot` forces `pendingStartBrace=0` |
| Ash Tithe → summary **+3** win or death | `buyAshTithe_summaryRemIncludePlus3_winOrDeath`: bonus=**3**; runWallet 11 → banked **14**; source `finishRun`: `banked = earned + ashTitheBonus` |
| Prior four OWNED; bank never resets | `oldFourStayOwned_bankNeverResets_newThreeAbsentUntilBought`: bank 40→**30**; Scout/Extra/Host/Iron OWNED; Warm Ash/Ash Tithe absent; migrate does not auto-OWN hostblood |
| Baseline rumor 1/floor; hubkeep intact | HubKeepV0128 **7/7** green (baseline=1, Extra→2, Scout→1, migration/bank) |
| Frozen combat art / Wake / path / 2x / packs | tip `--stat` excludes those cores; frozen suites green |

---

## D) Unit tests

Commands (measured this gate):

- `./gradlew :app:testDebugUnitTest --tests '*HubMoreV0129*'` → **BUILD SUCCESSFUL**
- `./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest` → **BUILD SUCCESSFUL**

Counts from XML under `app/build/test-results/testDebugUnitTest/`:

| Suite | Result |
|-------|--------|
| HubMoreV0129Test | **8/8 PASS** |
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
| **Full `:app:testDebugUnitTest`** | **183/183 PASS** (28 classes, 0 fail/err/skip) |

### HubMoreV0129Test methods

1. `hubList_sevenOffers_costsLocked` — PASS  
2. `bank6_extraRumorBuyable_othersCantAfford` — PASS  
3. `buyHostblood_climbMaxIs32_startFull_baseStays30` — PASS  
4. `ashPressHealCap_followsClimbMax_withHostblood` — PASS  
5. `buyWarmAsh_firstCombatShowsBrace2BeforeSkill` — PASS  
6. `buyAshTithe_summaryRemIncludePlus3_winOrDeath` — PASS  
7. `oldFourStayOwned_bankNeverResets_newThreeAbsentUntilBought` — PASS  
8. `hostbloodDoesNotWriteMetaHpBonusKey_formulaOnly` — PASS  

**Measured (from passing assertions):**

- Offers: **7**; costs **[8,6,12,12,10,8,8]**; ids include `hostblood` / `warm_ash` / `ash_tithe`
- At bank **6**: Extra=`BUY`; other six=`CANT_AFFORD`
- Hostblood buy: bank **20→10**; climbMax **32** (base 30); + metaHpBonus 2 → **34**; OWNED once
- Ash Press under Hostblood: playerHp **30→32** (cap climbMaxHp, not hard 30)
- Warm Ash buy: bank **20→12**; first combat brace=**2**
- Ash Tithe: bonus **+3**; wallet 11 → banked **14** (empty unlocks → 11)
- Old four + meta_hp_2 / rest_heal_plus stay; bank **40→30** after Hostblood; new three absent until bought; migrate does not auto-OWN them

---

## E) Cases 1–8 vs `docs/hubmore-v0129.md`

**1) 6 rem: Extra rumor Buyable; Scout/Hostblood/Warm Ash/Ash Tithe/Host of Embers/Iron Lesson Can't afford**

- `bank6_extraRumorBuyable_othersCantAfford`: Extra=`BUY`; Scout / Host of Embers / Iron Lesson / Hostblood / Warm Ash / Ash Tithe = `CANT_AFFORD` at bank **6**.

**2) Hostblood owned → next climb HP 32/32 (base 30); Ash Press/heal respects climbMaxHp; Hostblood NOT via meta_hp_bonus**

- `buyHostblood_climbMaxIs32_startFull_baseStays30`: `Balance.PLAYER_MAX_HP=30`; climbMax=`32`; with metaHpBonus=2 →`34`.
- `ashPressHealCap_followsClimbMax_withHostblood`: maxHp=32, start 30 → after Ash Press **32**.
- `hostbloodDoesNotWriteMetaHpBonusKey_formulaOnly`: grant from `hostblood` ∈ unlocks; `meta_hp_2` alone does not add Hostblood; hub buy path unlocks id only.

**3) Warm Ash → first fight Brace 2 before skill; NOT re-granted on FloorBreak / midrun resume**

- `buyWarmAsh_firstCombatShowsBrace2BeforeSkill`: `s.brace=2` before skill.
- Source: grant sites = climb start only (`startNewRun` L268, `skipTutorial` L312, `completeTutorial` L338); `continueAfterFloorBreak` has no brace grant; `applyMidRunSlot` L1255 sets `pendingStartBrace=0`.

**4) Ash Tithe → summary rem +3 on win AND death (finishRun hook)**

- `buyAshTithe_summaryRemIncludePlus3_winOrDeath`: `ashTitheBonus=3`; banked = runWallet + 3.
- Source `finishRun` L1103–1113: `tithe = HubOffers.ashTitheBonus(unlockedCards)`; `banked = earned + tithe` (both win and death call `finishRun`).

**5) Prior OWNED four still OWNED; rem bank never jumps/resets**

- `oldFourStayOwned_bankNeverResets_newThreeAbsentUntilBought`: Scout/Extra/Host/Iron OWNED after Hostblood buy; bank **40→30** (spend only); encode/decode bank **30**; migrate keeps bank/unlocks without wiping.

**6) Baseline free rumor=1/floor without Extra; Cinder/Grave unlock when owned (hubkeep/hub stays intact)**

- HubKeepV0128 **7/7** + HubV0127 **9/9** green: baseline rumor **1**, Extra → **2**, Scout → freeScout **1**, Host/Iron gate Cinder/Grave.

**7) Combat art / Wake / path / 2x / packs untouched**

- tip `--stat` excludes WakeArt / PathGenerator / CombatSpeed / packs portraits / 2x retune. CombatEngine only adds `initialBrace` param.
- Frozen suites green: Packs 12/12, WakeArtV0117 4/4, SkillGlyph 8/8, Portraits 7/7, Nobg 5/5, CombatSpeed 5/5, MidRunSave 8/8, RumorCharge 8/8, PathGate 1/1, PathGenerator 7/7; full **183/183**.

**8) Hub shows 7 offers; unlock ids hostblood / warm_ash / ash_tithe in unlocked_cards**

- `hubList_sevenOffers_costsLocked`: size **7**; ids include `hostblood`, `warm_ash`, `ash_tithe`.
- Buy path: `applyBuy` / `hubBuyOffer` → `unlockCard(offerId)` into existing `unlocked_cards` set (no new DataStore keys).

---

## Cases 1–8

| # | Claim | Verdict |
|---|-------|---------|
| 1 | 6 rem: Extra Buyable; others Can't afford | **PASS** — Extra=`BUY`; six others=`CANT_AFFORD` at bank 6 |
| 2 | Hostblood → HP 32/32; Ash Press cap; not meta_hp_bonus | **PASS** — climbMax **32** (34 w/ meta+2); Ash Press 30→**32**; formula-only |
| 3 | Warm Ash Brace 2 first fight; not FloorBreak/midrun | **PASS** — brace=**2**; source climb-start only; resume clears |
| 4 | Ash Tithe +3 summary win and death | **PASS** — bonus **+3**; finishRun hook both paths |
| 5 | Prior four OWNED; bank never resets | **PASS** — four OWNED; bank 40→30 spend-only; encode/decode |
| 6 | Baseline rumor 1; Cinder/Grave when owned | **PASS** — HubKeep 7/7 + HubV0127 9/9 |
| 7 | Frozen combat art / Wake / path / 2x / packs | **PASS** — tip excludes cores; frozen suites + full 183/183 |
| 8 | 7 offers; ids hostblood/warm_ash/ash_tithe | **PASS** — size **7**; three ids in HubOffers + unlock path |

---

## Docs

- Spec: `docs/hubmore-v0129.md`
- Prior gate format: `docs/qa-gate-v0128-hubkeep-2026-09-24.md`
- This gate: `docs/qa-gate-v0129-hubmore-2026-09-25.md`

---

## Overall

**Gate PASS** — tip `71448b768aef6d617d9b39bcf0344dbf762ded06` (== origin/main == claimed); APK MATCH 18839331 / `e901f2b89e7edc107f3d305e49fab14f` (root + build); HubMoreV0129 **8/8**; full suite **183/183** (28 classes, 0 fail/err/skip); cases 1–8 PASS.
