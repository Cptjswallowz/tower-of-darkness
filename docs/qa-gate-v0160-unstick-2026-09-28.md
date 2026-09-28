# QA Gate — v0.1.60-unstick

**Measured ET:** 2026-09-28 12:15–12:17 EDT  
**Gate runner:** QA (box)  
**Result:** **PASS**

Tip is **not** on `main`. Evaluated detached / `origin/feat/0.1.60-unstick` tip only. No tag, no merge, no APK overwrite, no messaging.

Design: `docs/unstick-v0160.md` · Plate map: `docs/art-audio/UNSTICK_PLATE_MAP_v0.1.60.md`

---

## A. Git

| Check | Measured |
|-------|----------|
| HEAD | `5281ca728e0a8e7095cd29f61485448275744054` (detached at `origin/feat/0.1.60-unstick`) |
| `origin/feat/0.1.60-unstick` tip | `5281ca728e0a8e7095cd29f61485448275744054` |
| **Tip match vs feat** | **YES** |
| `origin/main` SHA | `b98d202d70a8767e12386c9d8bf288a634b86e68` (`docs: qa-gate v0.1.59-tilepolish GREEN-LIGHT`) |
| **Tip on main** | **NO** (main ≠ feat tip; not merged) |
| Subject | `feat: v0.1.60-unstick combat-end Continue + plate allow-list` |
| Parent | `b98d202d70a8767e12386c9d8bf288a634b86e68` (= merge-base with `origin/main`) |
| `--stat` (tip vs parent) | 10 files, +310/−71: README, `build.gradle.kts`, `Balance.kt`, `GameController.kt`, `CombatScreen.kt`, `PathScreen.kt`, `TilepolishV0159Test.kt`, `UnstickV0160Test.kt` (+118), `UNSTICK_PLATE_MAP_v0.1.60.md`, `unstick-v0160.md` |
| Working tree | Clean tracked; untracked only `docs/qa-gate-v0150-puffhold-2026-09-26.md`, `release-assets/` (pre-existing; not part of tip) |

Left repo at feat tip (detached OK).

---

## B. APK / packaging

| Artifact | md5 | size | mtime ET |
|----------|-----|------|----------|
| `tower-of-darkness-debug.apk` (root) | `4deb2ab6660a0abc0147de5706e44f59` | 42700097 | 2026-09-28 12:12:06 EDT |
| `app/build/outputs/apk/debug/app-debug.apk` | `4deb2ab6660a0abc0147de5706e44f59` | 42700097 | 2026-09-28 12:12:01 EDT |
| **cmp** | **CMP_OK** | | |

- Root APK **not** overwritten by gate.
- `output-metadata.json`: versionCode **61**, versionName **0.1.60-unstick**, `app-debug.apk`
- `app/build.gradle.kts`: `versionCode = 61`, `versionName = "0.1.60-unstick"`
- `UnstickV0160Test.packaging_vc61_vnUnstick` PASS

---

## C. Hard locks (tip diff vs merge-base = parent `b98d202…`)

| Lock target | tip `--numstat` |
|-------------|-----------------|
| `domain/combat/CombatFx.kt` | **0 0** |
| `domain/combat/PlumeGarnishKit.kt` | **0 0** |
| `domain/sound/IronclashSfx.kt` | **0 0** |
| `domain/sound/SoundBus.kt` | **0 0** |
| `ui/components/CombatFxOverlay.kt` | **0 0** |
| `data/MetaStore.kt` (remnants bank) | **0 0** |
| `domain/forge/Forge.kt` (costs) | **0 0** |
| `ui/screens/ForgeSheet.kt` | **0 0** |

Tip file list has **no** CombatFx / Plume / Ironclash / SoundBus / MetaStore / Forge cost / relic / music / Title-Hub restyle hits.

**FX/SFX tip diff file count: 0** (CombatFx + PlumeGarnishKit + IronclashSfx + SoundBus + CombatFxOverlay).

Remnants / Forge costs / relics / SFX / FX / music / Title-Hub restyle: **untouched** on tip.

---

## D. Unit tests

Command: `./gradlew :app:testDebugUnitTest --rerun-tasks --offline`

| Metric | Measured |
|--------|----------|
| BUILD | **SUCCESSFUL in 9s** (ET ~12:16) |
| Classes | **56** |
| Tests | **437** |
| Failures | **0** |
| Errors | **0** |
| Skipped | **0** |
| **UnstickV0160Test** | **5/5 PASS** |

Unstick methods (all PASS):  
`continueReplacesFlee_boundToCombatEndState_notLogClick`, `plateNotOnLogWell_bottomSlot_hp_pips_foeChips`, `combatPip_lv1None_lv2II_lv3III`, `shopPurse_notRem_still`, `packaging_vc61_vnUnstick`.

Engineer claim 437/437 + Unstick 5/5: **confirmed**.

---

## E. Checklist 1–8

**Method:** unit tests + source audit on tip `5281ca7` (no device playtest this gate). Manual ~1s Continue timing inferred from code path (`Balance.COMBAT_END_FORCE_MS = 600L` mid-skill force-win cap → `AWAITING_CONTINUE` / Continue button).

| # | Item | Verdict | Evidence |
|---|------|---------|----------|
| 1 | Kill foe → Victory line + Continue within ~1s (bound to combatEnded/foeHp≤0, **not** log click) | **PASS** | `CombatScreen`: `combatEnded = state.finished \|\| beat == AWAITING_CONTINUE`; Continue `onClick = gc.continueAfterCombat()` in Flee slot; log well has no `continueAfterCombat`/`clickable`. `GameController.forceCombatWinIfNeeded`: appends `"Victory!"` if missing, sets `finished/playerWon/AWAITING_CONTINUE`. Mid-skill hold `minOf(..., COMBAT_END_FORCE_MS)` = **600ms** then force-win. `Log.d("COMBAT", "COMBAT_END …")`. Test: `continueReplacesFlee_boundToCombatEndState_notLogClick`. |
| 2 | Continue → floor map, scrap up as 0.58 | **PASS** | `continueAfterCombat` → `onCombatEnd` → win non-boss: `grantScrap(ScrapPouch.dropForKill(...))` + `returnToPathAfterResolve()` → `nav = NavState.Path`. Same scrap pouch path as 0.58. |
| 3 | Die → Run Summary still works | **PASS** | Lose branch of `onCombatEnd`: `finishRun(won = false)` → `nav = NavState.RunSummary` / `RunSummaryData`. Death/summary path not in tip diff; regression via existing suite + source. |
| 4 | Log lines visible during fight (not blank stone field) | **PASS** | Log Column: `state.log.takeLast(6)` Text events. **No** `SharedTilePlateBox` / `ui_tile_plate` on log well or bottom slot (`UnstickV0160Test.plateNotOnLogWell_…`). Log Column itself has no `Panel`/`VoidBg` wrapper in audited snippet — plate bug painter remains `SharedTilePlateBox` (`TilePlateBackdrop.kt`); Art “no ui_tile_plate on log” confirmed. No stone-field flag raised from source. |
| 5 | Leveled Pike (Lv2/3) shows II/III on combat bar; Lv1 no pip | **PASS** | `Forge.combatPipLabel`: Lv1 `null`, Lv2 `"II"`, Lv3 `"III"`. `CombatScreen` SkillSlot: `Forge.combatPipLabel(forgeLevel)` + `Alignment.TopEnd`. Test: `combatPip_lv1None_lv2II_lv3III`. |
| 6 | Shop pile rows say purse not rem | **PASS** | `ScrapPouch.SHOP_PRICE_UNIT = "purse"`; `shopPriceLine` → `"$title · $price purse"`. Shop uses `ScrapPouch.shopPriceLine`; no `` ${offer.price} rem ``. Test: `shopPurse_notRem_still`. |
| 7 | Title remnants still match Hub (bank frozen — regression) | **PASS** | Title `MainMenuScreen`: `HubOffers.titleBankLine(gc.remnantsBank)`; Hub `MetaHubScreen`: `HubOffers.hubBankLine(gc.remnantsBank)` — both MetaStore `remnantsBank`. Tip does **not** touch MainMenu/MetaHub/MetaStore/HubOffers (`MetaStore` tip diff 0). Bank frozen. |
| 8 | Plate only under skill tiles / Ashbrand / Forge; Path node discs no longer plated | **PASS** | `SharedTilePlateBox` kept in SkillSlot + WeaponBar + `ForgeSheet`; **absent** from log/bottom/HP/EnemyKit/StatusPipRow/`PathScreen`. Path nodes: plain `Panel`+`CircleShape` disc (`// v0.1.60-unstick: … no node discs`). Test: `plateNotOnLogWell_…`. |

---

## F. Result

**PASS** — tip matches feat, packaging vc61 / md5 / CMP_OK, hard locks tip-diff 0 (FX/SFX count 0), unit suite **56 classes / 437 tests / 0 fail / 0 err / 0 skip** in **9s**, UnstickV0160Test **5/5**, checklist 1–8 PASS (source/unit method; Continue timing code-bound ≤600ms force path).

**FAIL blockers:** none

**Not done (per kick):** no tag, no merge to main, no root APK overwrite, no SendToUser/SendToAgent/release/pay/delete.

**Repo left at:** detached `5281ca728e0a8e7095cd29f61485448275744054` (`origin/feat/0.1.60-unstick`).
