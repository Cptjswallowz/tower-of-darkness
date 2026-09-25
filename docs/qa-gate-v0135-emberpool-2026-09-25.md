# QA Gate — v0.1.35-emberpool (2026-09-25)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.35-emberpool  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-25 ~19:20–19:21 EDT

**Tip (HEAD / origin/main):** `91840b3f1749b61ba62c12b14a7ec5240572f3b7`  
**Claimed tip:** `91840b3f1749b61ba62c12b14a7ec5240572f3b7` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `91840b3f1749b61ba62c12b14a7ec5240572f3b7` |
| origin/main | `91840b3f1749b61ba62c12b14a7ec5240572f3b7` |
| Match claimed tip | **YES** (HEAD == origin/main == claimed) |
| Subject | `v0.1.35-emberpool: Hub Spark skills Ember Draw / Brand Mark / Spark Tithe / Wake Echo` |
| Author / date | Android Engineer \<android-engineer@tod.local\> — 2026-09-25 19:20:16 -0400 |
| Status | `main` up to date with `origin/main`; tip SHA matches. WT has **untracked** (not in tip; do not fail): `docs/art-audio/ASHBRAND_v0.1.34.md`, `tools/prep_ashbrand_v0134.py` |

**`--stat`:** Card.kt, CombatEngine.kt, Effect.kt, Glossary.kt, SkillGlyph.kt, HubOffers.kt, VolumeArt.kt, SkillGlyphIcon.kt, MetaHubScreen.kt, 4 glyph PNGs, EmberPoolV0135Test.kt, HubMoreV0129Test.kt / HubV0127Test.kt / SkillGlyphV0119Test.kt / VolumeArtV0120Test.kt (assert updates), docs. **25 files, +552/−29.**

**Not in tip (frozen sources):** Weapon.kt / EnemyKit cores / PathGenerator / CombatSpeed / portrait_* / FloorArt / ashbrand_icon.jpg / ashbrand_spark / wake_vfx_* — tip does not retune Wake math, enemy kits, path, 2x, or art stills.

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 19271518 | 2026-09-25 19:20 EDT | `1a43c716bcdbe223aa3a4e49c3bffc8a` |
| `app/build/outputs/apk/debug/app-debug.apk` | 19271518 | 2026-09-25 19:20 EDT | `1a43c716bcdbe223aa3a4e49c3bffc8a` |

Claimed: 19271518 / `1a43c716bcdbe223aa3a4e49c3bffc8a` → **MATCH** (root + build).

---

## C) Source / design notes (emberpool)

Design lock: `docs/emberpool-v0135.md`.

| Lock | Evidence |
|------|----------|
| Hub total 11; first 7 unchanged at head | `HubOffers.all.size == 11`; costs `[8,6,12,12,10,8,8,10,10,12,12]`; ids head `scout_charge…ash_tithe` then `cold_draw/brand_lesson/spark_lesson/echo_lesson` |
| Ember offer ids + costs | `cold_draw` 10 → `ember_draw`; `brand_lesson` 10 → `brand_mark`; `spark_lesson` 12 → `spark_tithe`; `echo_lesson` 12 → `wake_echo` |
| Pool gate | `CardCatalog.poolForRun` + `HubOffers.skillUnlocked` / `gatedSkillCardIds` — four cards absent until OWNED |
| Hard order | `CombatEngine.resolveSkill`: read `pipsBefore` → `resolveCard` damage → `+1` charge if attack → then Brace/Soften/Tithe |
| Wake Echo | `resolveCard` EmberPoolSkill: `dmg = damage + echoBonusIfWakeFired` when `fullProcThisCombat`; one log `"${card.title} deals $dmg"`; single +1 Spark in resolveSkill |
| Spark Tithe | after +1, if `pipsBefore >= 1` charge−1 + log `"Spark spent"` |
| Iron Mantle / Hostflint | Card.kt: Mantle Brace 3 equipment; Hostflint Deal 5; tip does not retune their numbers; EmberPool asserts Brace 3 / deal 5 / charge+1 |
| Wake math frozen | Weapon.kt **not** in tip; ashbrand threshold 3/2/2; fullDamage `[4,6,8]`; sparkDamage `[2,3,4]` |
| Art frozen | `ashbrand_icon.jpg` md5 `846af065acf75d5f08a08ca0bfd4173a` (130895 bytes) still present; tip adds only glyph_*.png (4× 5925 bytes) |
| Catalog | `CardCatalog.all.size == 17` (EmberPool glyphs_and_catalogMapped) |

**Spot-check (source):**

- `HubOffers.kt`: 11 offers; four ember unlocks; `skillUnlocked` / migrate / gated set.
- `Card.kt`: four `SkillEffect.EmberPoolSkill` uncommon w3; Hostflint Deal 5; Iron Mantle Brace 3.
- `CombatEngine.kt`: EmberPool post-charge Brace/Soften/Tithe; Wake Echo single-line damage.
- `Effect.kt`: `EmberPoolSkill` fields braceIfZeroBefore / softenIfBeforeGte1 / titheSpendIfBeforeGte1 / echoBonusIfWakeFired.
- Glyphs: `glyph_ember_draw` / `glyph_brand_mark` / `glyph_spark_tithe` / `glyph_wake_echo` mapped in SkillGlyph + drawable PNGs present.

---

## D) Unit tests

Commands (measured this gate):

- `./gradlew :app:testDebugUnitTest --tests 'com.towerofdarkness.app.EmberPoolV0135Test'` → **BUILD SUCCESSFUL**
- `./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest` → **BUILD SUCCESSFUL**

Counts from XML under `app/build/test-results/testDebugUnitTest/`:

| Suite | Result |
|-------|--------|
| EmberPoolV0135Test | **11/11 PASS** |
| BladeV0134Test | **6/6 PASS** |
| AshbrandV0133Test | **5/5 PASS** |
| EnemyKitV0132Test | **10/10 PASS** |
| FloorArtV0131Test | **13/13 PASS** |
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
| **Full `:app:testDebugUnitTest`** | **228/228 PASS** (33 classes, 0 fail/err/skip) |

### EmberPoolV0135Test methods

1. `hubOffers_eleven_costsAndIdsLocked` — PASS  
2. `beforeBuy_noneOfFourInPool` — PASS  
3. `afterColdDrawOnly_emberDrawOnlyOfFourInPool` — PASS  
4. `doubleBuy_coldDraw_spendsZero` — PASS  
5. `zeroPips_emberDraw_brace2_andOnePip` — PASS  
6. `onePip_brandMark_soften2_charge1to2` — PASS  
7. `onePip_sparkTithe_logsSparkSpent_pipsBackTo1` — PASS  
8. `wakeAlreadyFired_wakeEcho_deals9_chargePlusOneOnly` — PASS  
9. `ironMantle_and_hostflint_unchanged` — PASS  
10. `glyphs_and_catalogMapped` — PASS  
11. `cards_uncommon_w3_titlesMatch` — PASS  

**Measured (from passing assertions + source):**

- Hub 11; costs 8/6/12/12/10/8/8/10/10/12/12; cold_draw/brand_lesson/spark_lesson/echo_lesson unlock ember_draw/brand_mark/spark_tithe/wake_echo
- Before buy: none of four in `poolForRun(starters)`
- After cold_draw only: bank 20→10; ember_draw in pool; other three absent
- 0 Sparks + Ember Draw → Brace 2, charge 1, enemy −4
- 1 Spark + Brand Mark → Soften 2 (`counterPenalty` 2), charge 1→2, enemy −3
- 1 Spark + Spark Tithe → log contains `"Spark spent"`, charge back to 1, enemy −6
- Wake already fired + Wake Echo → log `"Wake Echo deals 9"`, charge +1 only, enemy −9
- Iron Mantle Brace 3 / charge 0; Hostflint deal 5 / charge 1 / brace 0
- Glyphs present; CardCatalog.all size 17; all four uncommon weight 3

---

## E) Cases 1–7 vs `docs/emberpool-v0135.md`

**1) Before buy: none of ember_draw / brand_mark / spark_tithe / wake_echo on Loadout (poolForRun with starters)**

- `beforeBuy_noneOfFourInPool`: all four absent from `CardCatalog.poolForRun(starterUnlockedIds())`; `skillUnlocked` false for each.

**2) After cold_draw only: Ember Draw appears; other three still locked out**

- `afterColdDrawOnly_emberDrawOnlyOfFourInPool`: applyBuy cost 10 (20→10); pool has `ember_draw`; not brand_mark / spark_tithe / wake_echo.

**3) 0 Sparks + Ember Draw → Brace 2 and 1 pip (deal 4)**

- `zeroPips_emberDraw_brace2_andOnePip`: brace=2, charge=1, enemy.hp −4.

**4) 1 Spark + Brand Mark → Soften 2 (order: read before → damage → +1 Spark → Soften)**

- `onePip_brandMark_soften2_charge1to2`: counterPenalty=2, charge 1→2, enemy −3.
- Source order in `resolveSkill`: pipsBefore read → resolveCard damage → charge+1 → Soften if pipsBefore≥1.

**5) 1 Spark + Spark Tithe → log "Spark spent", pips back to 1 (net 0 when before≥1)**

- `onePip_sparkTithe_logsSparkSpent_pipsBackTo1`: log contains `"Spark spent"`; charge=1; enemy −6.

**6) Wake already fired this fight + Wake Echo → one log "Wake Echo deals 9" (5+4); bonus no second Spark**

- `wakeAlreadyFired_wakeEcho_deals9_chargePlusOneOnly`: `fullProcThisCombat=true`; log `"Wake Echo deals 9"`; chargeBefore+1; enemy −9.
- `resolveCard` adds echoBonus into same `"deals $dmg"` line; single +1 in resolveSkill.

**7) Iron Mantle / Hostflint unchanged (no retune of existing cards)**

- `ironMantle_and_hostflint_unchanged`: Mantle brace=3 charge=0; Hostflint enemy −5 charge=1 brace=0.
- Card.kt: Mantle GainBrace 3; Hostflint Damage 5 — same defs; EmberPool asserts lock.

---

## Cases 1–7

| # | Claim | Verdict |
|---|-------|---------|
| 1 | Before buy: none of four in poolForRun(starters) | **PASS** — beforeBuy_noneOfFourInPool; gatedSkillCardIds |
| 2 | After cold_draw only: Ember Draw in; other three out | **PASS** — bank 20→10; ember_draw only of four |
| 3 | 0 Sparks + Ember Draw → Brace 2 + 1 pip (deal 4) | **PASS** — brace=2 charge=1 enemy−4 |
| 4 | 1 Spark + Brand Mark → Soften 2; order lock | **PASS** — Soften 2 charge 1→2 enemy−3; resolveSkill order |
| 5 | 1 Spark + Spark Tithe → "Spark spent", pips→1 | **PASS** — log Spark spent; charge=1; enemy−6 |
| 6 | Wake fired + Wake Echo → "Wake Echo deals 9"; +1 Spark only | **PASS** — log deals 9; charge+1; enemy−9 |
| 7 | Iron Mantle / Hostflint unchanged | **PASS** — Brace 3 / Deal 5; charge behavior unchanged |

---

## Frozen spot checklist

| Suite | Count |
|-------|-------|
| EmberPoolV0135Test | 11/11 |
| BladeV0134Test | 6/6 |
| AshbrandV0133Test | 5/5 |
| EnemyKitV0132Test | 10/10 |
| FloorArtV0131Test | 13/13 |
| HubMoreV0129Test | 8/8 |
| HubKeepV0128Test | 7/7 |
| HubV0127Test | 9/9 |
| PacksV0126Test | 12/12 |
| WakeArtV0117Test | 4/4 |
| SkillGlyphV0119Test | 8/8 |
| PortraitsV0121Test | 7/7 |
| NobgV0123Test | 5/5 |
| CombatSpeedV0111Test | 5/5 |
| MidRunSaveV0112Test | 8/8 |
| RumorChargeV0124Test | 8/8 |
| PathGateV015Test | 1/1 |
| PathGeneratorTest | 7/7 |

Also green (other *V0* / related): BodyArtV0118 6, BossRestV0110 4, BraceSyncV0114 7, CombatEngineTest 12, CombatSimV013 1, CombatSimV014 1, Floor2Test 9, PlateV0122 7, ShopWalletTest 6, StatusPipsV0113 8, TreasureSwapLockTest 3, VolumeArtV0120 11, WakeArtV0115 8, WakeArtV0116 7, WeaponXpTest 4.

**Frozen locks measured:**

- Wake math: threshold 3/2/2; full `[4,6,8]`; spark `[2,3,4]` (Weapon.kt not in tip; BladeV0134 / Ashbrand suites green)
- Enemy kits size 3: EnemyKitV0132 10/10 (e.g. goblin kit ids shiv/nip/hit)
- Hub first 7 head + costs unchanged; total 11 with +4 ember offers
- ashbrand_icon.jpg md5 `846af065acf75d5f08a08ca0bfd4173a` present
- Path / 2x / CombatSpeed: PathGateV015 1/1, PathGenerator 7/7, CombatSpeedV0111 5/5

---

## FAIL blockers

**none**

---

## Docs

- Spec: `docs/emberpool-v0135.md`
- Prior gate format: `docs/qa-gate-v0134-blade-2026-09-25.md`
- This gate: `docs/qa-gate-v0135-emberpool-2026-09-25.md`

---

## Overall

**Gate PASS** — tip `91840b3f1749b61ba62c12b14a7ec5240572f3b7` (== origin/main == claimed); APK MATCH 19271518 / `1a43c716bcdbe223aa3a4e49c3bffc8a` (root + build); EmberPoolV0135 **11/11**; full suite **228/228** (33 classes, 0 fail/err/skip); cases 1–7 PASS; frozen suites green; WT untracked ASHBRAND_v0.1.34.md + prep_ashbrand_v0134.py noted, not fail.
