# QA Gate — v0.1.39-fxread (2026-09-26)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.39-fxread  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-26 ~13:51–13:53 EDT

**Tip (HEAD / origin/main):** `3e4dfb3eff5c77a33d8333f40b1e2d0205ec3fde`  
**Claimed tip:** `3e4dfb3eff5c77a33d8333f40b1e2d0205ec3fde` → **MATCH**  
**Base (v0.1.38-fxaim release):** `29ab0217d7d79481e086e3f80a24e33d78534c74` (CoS earlier also cited feature tip `b30a520`; gate uses claimed tip `3e4dfb3`)

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `3e4dfb3eff5c77a33d8333f40b1e2d0205ec3fde` |
| origin/main | `3e4dfb3eff5c77a33d8333f40b1e2d0205ec3fde` |
| Match claimed tip | **YES** (HEAD == origin/main == claimed) |
| Subject | `feat: v0.1.39-fxread bigger Brace shields, heavier slash` |
| Author / date | Android Engineer <android-engineer@tod.local> — 2026-09-26 13:51:35 -0400 |
| Status | `main` up to date with `origin/main`; tip SHA matches. WT has **untracked** (not in tip; do not fail): `docs/art-audio/ASHBRAND_v0.1.34.md`, `docs/art-audio/CLIMB_INTRO_v0.1.36.md`, `tools/prep_ashbrand_v0134.py` |

**`--stat` (3e4dfb3 vs parent):** CombatFx.kt (TAG / thickness / stroke ms / Brace hold+fade+radius / bracePipAlpha), WakeArt.kt (`FRAME_SLASH_MS`), CombatFxOverlay.kt (pip radius + bracePipAlpha), CombatFxReadV0139Test.kt (new), WakeArtV0115/0116/0117Test (slash frame +200ms), docs/fxread-v0139.md. **8 files, +263/−27.**

**Not in tip (frozen cores):** Wake math / path / Hub / Climb / Soften / tile-flash scoping / who-gets-FX aim — tip retunes readability sizes/holds only. No PathGenerator / PathGate / Hub / ClimbIntro / SkillGlyph / portraits / FloorArt / Card effect math retune. No plate return.

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 37967453 | 2026-09-26 13:51 EDT | `0bc5bd245756a4c03e051b8b856f9a24` |
| `app/build/outputs/apk/debug/app-debug.apk` | 37967453 | 2026-09-26 13:51 EDT | `0bc5bd245756a4c03e051b8b856f9a24` |

Claimed: 37967453 / `0bc5bd245756a4c03e051b8b856f9a24` → **MATCH** (root + build; `cmp` identical).

---

## C) Design / prior gate

| Doc | Present |
|-----|---------|
| `docs/fxread-v0139.md` | **YES** (design lock + Engineer checklist) |
| `docs/fxaim-v0138.md` | **YES** (aim / who-gets-FX kept) |
| `docs/fx-v0137.md` | **YES** (tier maps / colors kept) |
| Prior gate style | `docs/qa-gate-v0138-fxaim-2026-09-26.md` |

---

## D) Source / measured readability constants

From `CombatFx.kt` + `CombatFxOverlay.kt` + `WakeArt.kt` + `CombatScreen.kt` + `WakeStageOverlay.kt` (read this gate):

| Const | Measured value | Design lock |
|-------|----------------|-------------|
| TAG | `v0.1.39-fxread` | yes |
| BRACE_PIP_RADIUS_FRAC | `0.027f` (prior `0.018f` × 1.5) | 1.5× |
| BRACE_PIP_MS | `700L` | 700ms@1x |
| BRACE_PIP_FADE_MS | `200L` | fade last 200ms only |
| bracePipAlpha | full `0.95f` until fadeStart `(700-200)/700≈0.714`; then linear to floor `0.15f` | yes |
| Overlay pipR | `min(w,h) * CombatFx.BRACE_PIP_RADIUS_FRAC` + `bracePipAlpha(progress)` | yes |
| THICK_SMALL / THICK_MEDIUM | `2.1f` / `4.2f` (prior 1.5/3.0 × 1.4) | 1.4× |
| STROKE_SMALL_MS / STROKE_MEDIUM_MS | `400L` / `480L` | +200ms@1x |
| fxHoldMs(…, 2) | brace `350`; stroke small `200`; stroke medium `240` | halves; still positive |
| WakeArt.FRAME_SLASH_MS | `700L` | +200; foe-only clip kept |
| Aim / recipient | unchanged (YOU_BUST_X `0.18`, FOE_BUST_X `0.82`, slashCutGeom, braceOwner) | frozen |

**Tower Pike geom (FOE, MEDIUM):** centerX=`0.82`, startX=`0.75`, endX=`0.89`, span=`0.14`, **`spansBothBusts()=false`**.

**Flash:** `CombatTileFlash` only inside `SkillSlot` / `EnemyKitSlot` with `Modifier.matchParentSize()` — comment "Fired tile flash — this skill tile only (not whole combat row)". Overlay docs ban stage `fillMaxSize` flash.

**Wake clip:** `WakeStageOverlay` — `fillMaxWidth(0.58f)` + `clipToBounds` + CenterEnd — right ~58% only; You left bust clear. Kernel stroke gated `!spec.useWakeSlash`.

**Mantle/Vow NO_STROKE set:** `iron_mantle`, `vow_plate` (WO “Cinder Vow” colloquial → lock id `vow_plate`; `cinder_vow` remains PLAYER_SMALL per frozen aim maps).

---

## E) Unit tests

Commands (measured this gate):

- `./gradlew :app:testDebugUnitTest --tests 'com.towerofdarkness.app.CombatFxReadV0139Test' --tests 'com.towerofdarkness.app.CombatFxAimV0138Test' --tests 'com.towerofdarkness.app.CombatFxV0137Test'` → **BUILD SUCCESSFUL** (~13:52 EDT)
- `./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest` → **BUILD SUCCESSFUL** (~13:52 EDT)

Counts from XML under `app/build/test-results/testDebugUnitTest/` (37 `TEST-*.xml` files):

| Suite | Result |
|-------|--------|
| CombatFxReadV0139Test | **9/9 PASS** |
| CombatFxAimV0138Test | **11/11 PASS** |
| CombatFxV0137Test | **13/13 PASS** |
| ClimbIntroV0136Test | **8/8 PASS** |
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
| **Full `:app:testDebugUnitTest`** | **269/269 PASS** (37 classes, 0 fail/err/skip) |

Claimed suite **269** → **MATCH**. Claimed Aim **11** + V0137 **13** + new Read **~9** → **MATCH** (9/9).

### CombatFxReadV0139Test methods (9/9)

1. `wakeSlashFrame_plus200Ms` — PASS  
2. `slashThickness_is1_4xPrior` — PASS  
3. `aimAndRecipient_unchangedFromV0138` — PASS  
4. `bracePipAlpha_fullUntilFadeThenDrops` — PASS  
5. `braceHold_700At1x_350At2x` — PASS  
6. `bracePipRadius_is1_5xPrior` — PASS  
7. `bracePipCount_stillCap5` — PASS  
8. `tag_isFxreadV0139` — PASS  
9. `strokeHolds_plus200Ms_at1x` — PASS

### CombatFxAimV0138Test methods (11/11)

1. `brandMark_softenApplied_noFxIdOnFollowUp` — PASS  
2. `mapsUnchanged_fromV0137` — PASS  
3. `recipientSlash_playerHitsFoe_enemyHitsYou` — PASS  
4. `noStroke_stillNoSlash_butBracePipsOnOwner` — PASS  
5. `bracePipCount_equalsGainCappedAt5` — PASS  
6. `engine_wiresBraceGained_andSoftenApplied` — PASS  
7. `slashGeometry_shortCut_doesNotSpanBothBusts` — PASS  
8. `wake_recipientIsFoe_noKernelStroke` — PASS  
9. `dustVeil_keepsSmallStroke_braceFollowUpHasPips` — PASS  
10. `softenPipPulse_flag_noExtraStrokeFromFollowUp` — PASS  
11. `engine_enemyHide_braceGainedOnFoe` — PASS

### CombatFxV0137Test (13/13) — tiers / colors / 2x kept

All 13 methods PASS (playerSmall/Medium/NoStroke, enemyMaps, wakeExclusive, wakeEcho, fxHoldMs_2x, colors_byRole, enemyStroke_flipsDirection, failSafe, engine wires, softenFollowUp).

---

## F) Cases 1–5 (+ Soften / aim / plate) vs `docs/fxread-v0139.md`

**1) Mantle/Vow: shields readable on You for a beat** — **PASS**  
- `iron_mantle` / `vow_plate` ∈ PLAYER_NO_STROKE → **NO_STROKE**; `stroke=null`  
- `playSpec(..., braceGained=3, …)` → `bracePipCount` from gain (cap 5), `braceOwner=YOU`  
- Size/hold/fade: `BRACE_PIP_RADIUS_FRAC=0.027f`, `BRACE_PIP_MS=700`, `BRACE_PIP_FADE_MS=200`, `bracePipAlpha` full until last 200ms  
- Overlay: `CombatBracePipsOverlay` uses radius frac + `bracePipAlpha(progress)`; centers on `YOU_BUST_X`  
- Unit: Read `bracePipRadius_is1_5xPrior`, `braceHold_700At1x_350At2x`, `bracePipAlpha_fullUntilFadeThenDrops`, `aimAndRecipient_unchangedFromV0138`; Aim `noStroke_stillNoSlash_butBracePipsOnOwner`; V0137 `playerNoStroke_ironMantle_vowPlate`

**2) Rust Guard: shields on Warden / orc Hide** — **PASS**  
- ENEMY_NO_STROKE = `{hide, rust_guard, cinder_hide}` → tier NO_STROKE; `stroke=null`  
- `playSpec(hide/…, braceGained=…, fxPlayer=false)` → `braceOwner=FOE`  
- Unit: Aim `noStroke_stillNoSlash_butBracePipsOnOwner`, `engine_enemyHide_braceGainedOnFoe`, `mapsUnchanged_fromV0137`; Read `aimAndRecipient_unchangedFromV0138`

**3) Tower Pike: thicker gold on foe only; You clean** — **PASS**  
- `tower_pike` ∈ PLAYER_MEDIUM → MEDIUM; role YOU; recipient **FOE**; gold `COLOR_YOU=0xFFC9A227`  
- stroke thickness **`4.2f`** (1.4× prior 3.0); strokeMs **`480`**  
- `slashCutGeom(FOE, MEDIUM)`: centerX `0.82`, span `0.14`, **`spansBothBusts()=false`**  
- Unit: Read `slashThickness_is1_4xPrior`, `strokeHolds_plus200Ms_at1x`, `aimAndRecipient_unchangedFromV0138`; Aim `recipientSlash_*`, `slashGeometry_shortCut_doesNotSpanBothBusts`

**4) 2x: shorter but still visible** — **PASS**  
- `fxHoldMs` halves: brace `350`, stroke small `200`, stroke medium `240`, Wake slash frame hold `350` — all **> 0**  
- Unit: Read `braceHold_700At1x_350At2x`, `strokeHolds_plus200Ms_at1x`, `wakeSlashFrame_plus200Ms`; V0137 `fxHoldMs_2x_halvesDurations`

**5) Suite green; no full-row plate; aim unchanged** — **PASS**  
- CombatFxAimV0138 **11/11**; CombatFxV0137 **13/13**; CombatFxReadV0139 **9/9**; full suite **269/269**  
- `CombatTileFlash` tile-scoped only (`matchParentSize`); no stage fillMaxSize flash wash  
- Soften: follow-up `softenPipPulse` / no plate (Aim soften tests green)  
- Aim frozen: Read `aimAndRecipient_unchangedFromV0138`; Aim `mapsUnchanged_fromV0137`  
- Tip `--stat` has no plate return / no Wake math / Climb / new skills

### Soften / aim / plate notes

| Note | Verdict |
|------|---------|
| Soften: red pip pulse, no plate | **PASS** — Aim soften tests; `COLOR_SOFTEN_PIP`; no extra stroke/plate |
| Aim / who-gets-FX from v0.1.38 | **PASS** — recipient / braceOwner / slashCutGeom / mapsUnchanged |
| No full-row plate regression | **PASS** — tile flash only; short recipient slash; Wake 0.58 clip |
| Wake FRAME_SLASH_MS 700 foe-only | **PASS** — WakeArt 700; useWakeSlash; stroke null; clip unchanged |

---

## Cases 1–5 summary

| # | Claim | Verdict |
|---|-------|---------|
| 1 | Mantle/Vow shields on You; size/hold/fade | **PASS** — NO_STROKE; braceOwner YOU; 0.027 / 700 / fade 200 / bracePipAlpha |
| 2 | Rust Guard / Hide shields on foe | **PASS** — ENEMY_NO_STROKE; braceOwner FOE; stroke null |
| 3 | Tower Pike thicker gold foe only | **PASS** — recipient FOE; gold; thick 4.2f; geom FOE; spansBothBusts=false |
| 4 | 2x shorter but visible | **PASS** — fxHoldMs halves; brace/stroke/Wake holds still positive |
| 5 | Suite green; no plate; aim frozen | **PASS** — Aim 11/11; tile flash only; Soften no plate; maps/aim unchanged |

---

## Frozen spot checklist

| Suite | Count |
|-------|-------|
| CombatFxAimV0138Test | 11/11 |
| CombatFxV0137Test | 13/13 |
| ClimbIntroV0136Test | 8/8 |
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

Also green (other *V0* / related): BodyArtV0118 6, BossRestV0110 4, BraceSyncV0114 7, CombatEngineTest 12, CombatSimV013 1, CombatSimV014 1, Floor2Test 9, PlateV0122 7, ShopWalletTest 6, StatusPipsV0113 8, TreasureSwapLockTest 3, VolumeArtV0120 11, WakeArtV0115 8, WakeArtV0116 7, WeaponXpTest 4. (+ CombatFxReadV0139 **9** + Aim 11 + V0137 13 = **269** total)

---

## FAIL blockers

**none**

---

## Docs

- Spec: `docs/fxread-v0139.md`  
- Aim lock (kept): `docs/fxaim-v0138.md`  
- Tier/color lock (kept): `docs/fx-v0137.md`  
- Prior gate format: `docs/qa-gate-v0138-fxaim-2026-09-26.md`  
- This gate: `docs/qa-gate-v0139-fxread-2026-09-26.md`

---

## Overall

**Gate PASS** — tip `3e4dfb3eff5c77a33d8333f40b1e2d0205ec3fde` (== origin/main == claimed); APK MATCH 37967453 / `0bc5bd245756a4c03e051b8b856f9a24` (root + build, cmp identical); CombatFxReadV0139 **9/9**; CombatFxAimV0138 **11/11**; CombatFxV0137 **13/13**; full suite **269/269** (37 classes, 0 fail/err/skip); cases 1–5 + Soften/aim/plate PASS with measured constants, geom, and XML evidence; frozen suites green; WT untracked ASHBRAND_v0.1.34.md + CLIMB_INTRO_v0.1.36.md + prep_ashbrand_v0134.py noted, not fail.
