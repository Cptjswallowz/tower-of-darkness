# QA gate — v0.1.59-tilepolish (2026-09-28)

**Gate result: PASS** (cases 1–8 green; packaging CMP_OK; suite green; FX/SFX tip diff 0)

| Field | Measured |
|-------|----------|
| Tip SHA | `ff1200c52376f256595f36bb6534fa071ad1e57a` |
| Tip == HEAD == origin/main | **YES** |
| versionCode / versionName | **60** / **0.1.59-tilepolish** |
| app-debug md5 | `ce1ff245b0c20acdff0e86c6f80f4a21` — **MATCHES claim** |
| Root APK md5 | `ce1ff245b0c20acdff0e86c6f80f4a21` — **MATCH** |
| root vs app-debug | **CMP_OK** |
| Plate in APK `res/drawable/ui_tile_plate.png` | md5 `20dc01b0e3b934c2bb08c8b26cf77524` — **MATCH** |
| Suite | **55** classes / **432** tests / **0** fail / **0** err / **0** skip |
| TilepolishV0159Test | **7/7 PASS** |
| ForgeV0158Test | **13/13 PASS** |
| TitlebankV0157Test | **6/6 PASS** |
| FX/SFX tip diff | **0** |
| FAIL blockers | **none** |

Measured ET **2026-09-28 10:42 EDT**. No tag / no gh release / no messaging from this gate. Root APK left as-is (not overwritten).

---

## A — Git

| Check | Result |
|-------|--------|
| `HEAD` | `ff1200c52376f256595f36bb6534fa071ad1e57a` |
| `origin/main` | `ff1200c52376f256595f36bb6534fa071ad1e57a` |
| Claimed tip | `ff1200c52376f256595f36bb6534fa071ad1e57a` |
| HEAD == origin/main == tip | **YES** |
| Subject | `feat: v0.1.59-tilepolish combat pips + shop purse + ui_tile_plate` |
| Author | Android Engineer \<android-engineer@tod.local\> |
| AuthorDate | 2026-09-28 10:40:10 -0400 (ET) |
| Parent | `e262249fcf5b91d361ba205402541dbcaae4e591` (`docs: QA gate notes for v0.1.58-forge GREEN-LIGHT`) |
| `--stat` tip | 19 files, +673/−68 — SharedTilePlate, TilePlateBackdrop, CombatScreen pips+plate, ForgeSheet plate, PathScreen node plate, ScrapPouch.shopPriceLine, Glossary bodyOverride, GameController.showSkillGlossary, Forge.combatPipLabel, ui_tile_plate.png, TilepolishV0159Test, docs |
| Working tree | tracked clean vs HEAD; untracked: `docs/qa-gate-v0150-puffhold-2026-09-26.md` (pre-existing) + this gate doc |

---

## B — APK packaging + plate

| Artifact | Size | md5 | mtime (ET) |
|----------|------|-----|------------|
| `app/build/outputs/apk/debug/app-debug.apk` | **42700097** | `ce1ff245b0c20acdff0e86c6f80f4a21` | **2026-09-28 10:39:37 EDT** |
| `tower-of-darkness-debug.apk` (root) | **42700097** | `ce1ff245b0c20acdff0e86c6f80f4a21` | **2026-09-28 10:39:37 EDT** |

| Check | Measured |
|-------|----------|
| Claimed APK md5 | `ce1ff245b0c20acdff0e86c6f80f4a21` |
| app-debug vs claim | **YES MATCH** |
| root vs claim | **YES MATCH** |
| `cmp` root vs app-debug | **CMP_OK** (identical bytes) |
| `output-metadata.json` | versionCode **60**, versionName **0.1.59-tilepolish**, outputFile `app-debug.apk` |
| `app/build.gradle.kts` | versionCode = 60, versionName = `"0.1.59-tilepolish"` |
| Plate unzip `res/drawable/ui_tile_plate.png` | md5 **`20dc01b0e3b934c2bb08c8b26cf77524`**, size 241892 — **MATCH** claim + source drawable + `assets/ui/ui_tile_plate.png` + `SharedTilePlate.EXPECTED_MD5` |

**Packaging finding:** root deliverable matches app-debug. Plate in APK matches Art deliverable. No overwrite performed by this gate.

---

## C — Hard locks

| Lock | Measured |
|------|----------|
| FX/SFX tip diff vs parent `e262249` | **0** — no path changes for `CombatFx.kt` / `PlumeGarnishKit.kt` / `IronclashSfx.kt` / `SoundBus.kt` / `CombatFxOverlay.kt` |
| Forge costs | **unchanged** — tip adds only `combatPipLabel`; `COST_L2_G=3`, `COST_L3_G=2`, `COST_L3_O=2`, `spendL2`/`spendL3` untouched; ForgeV0158Test.`forgeCosts_l2_3g_l3_2g2o_cancelNoSpend` **PASS** |
| Remnant bank / Title | **intact** — no MetaStore / HubOffers / MainMenu / Titlebank files in tip; Hub still `hubBankLine(gc.remnantsBank)`; Title still `titleBankLine(gc.remnantsBank)`; TitlebankV0157Test **6/6**; TilepolishV0159Test.`hubRemnantsWording_unchanged` |
| Relic effects | **unchanged** — no relic / ClimbKept / HubOffers paths in tip; ForgeV0158Test sootRim / ashPauldron / trollTooth / gateSigil / relicShard still green |
| ForgeSheet tip | plate UI only (`SharedTilePlateBox` + Panel alpha); `row.costLine` / pip I/II/III wiring preserved (`Forge.pipLabel` still I/II/III) |

---

## D — Unit tests

Command: `./gradlew :app:testDebugUnitTest --rerun-tasks --offline`

| Metric | Measured |
|--------|----------|
| BUILD | **SUCCESSFUL** |
| Gradle wall | **19s** |
| XML dir | `app/build/test-results/testDebugUnitTest/` |
| Classes | **55** |
| Tests | **432** |
| Failures | **0** |
| Errors | **0** |
| Skipped | **0** |
| XML time sum | **1.050s** |
| Measured clock | **2026-09-28 10:41 EDT** |

### Named suites

| Class | Result |
|-------|--------|
| TilepolishV0159Test | **7/7** PASS — combatPip lv1/II/III, forgeList I/II/III, shopPriceLine purse, glossaryUpgraded, combatSkillSlot wire, ui_tile_plate md5, hubRemnantsWording |
| ForgeV0158Test | **13/13** PASS |
| TitlebankV0157Test | **6/6** PASS |
| BankoneV0156Test | **5/5** PASS |
| HubsplitV0155Test | **6/6** PASS |
| TrollkeptV0154Test | **7/7** PASS |

---

## E — Cases 1–8

### 1. Combat skill tiles gold II/III top-right inside border; Lv1 no pip — **PASS**

- `Forge.combatPipLabel`: Lv1 `null`, Lv2 `"II"`, Lv3 `"III"`.
- `CombatScreen.SkillSlot`: Gold Bold 12.sp, `Alignment.TopEnd`, padding inside bordered Box (not on glyph).
- TilepolishV0159Test.`combatPip_lv1None_lv2II_lv3III` + `combatSkillSlot_wiresPipAndLongPress`; Forge list keeps I/II/III via `pipLabel`.

### 2. Shop climb prices say purse not rem; Hub remnants unchanged — **PASS**

- `ScrapPouch.SHOP_PRICE_UNIT = "purse"`; `shopPriceLine("Goblin pile", 8)` → `"Goblin pile · 8 purse"`.
- `ShopScreen` uses `ScrapPouch.shopPriceLine`; no `${offer.price} rem`.
- Hub/Title still remnants_bank wording; TilepolishV0159Test.`shopPriceLine_saysPurseNotRem` + `hubRemnantsWording_unchanged`.

### 3. Long-press combat skill → glossary upgraded Forge sentence — **PASS**

- Long-press → `gc.showSkillGlossary(card.title, card.effect.description)` (loadout already Forge-applied).
- `GlossaryDialog(bodyOverride=…)`; MainActivity wires `gc.glossaryBodyOverride`.
- TilepolishV0159Test.`glossaryUpgraded_matchesForgeBody_forSkillLevel` (Pike L2A → `"Deal 10 damage."`).

### 4. Plate under combat tiles + Ashbrand + Forge rows; under content; opacity ~25–40%; no FX packs — **PASS**

- `SharedTilePlateBox` wraps skill slots, WeaponBar (Ashbrand), ForgeSheet rows; Path node discs at `NODE_DISC_PLATE_ALPHA=0.40f`.
- Baked center α `CENTER_ALPHA_DOC=0.35f` within OPACITY_MIN/MAX 0.25–0.40; Compose skill/Ashbrand/Forge alpha 1f (use baked).
- Plate Image under content Box; TilepolishV0159Test.`uiTilePlate_md5MatchesArtDeliverable` asserts no plume/kenney on backdrop; APK plate md5 **MATCH**.

### 5. Name + w-cost + pip readable; HUD readable; rumor text still readable — **PASS**

- Skill: title Bone 9.sp, `wN` Bone 8.sp, pip Gold 12.sp over semi Panel (`PANEL_OVER_PLATE_ALPHA=0.55`) — plate behind content.
- Combat HUD / enemy names / round labels unchanged Bone/Gold sizes.
- Path: rumor caption still `Bone.copy(0.65f)`, wrap 2 lines (no hard mid-word clip); floor rumor strip `Bone.copy(0.9f)` 12.sp; node plate lower opacity only on disc Token.

### 6. Title remnants still match Hub after death (titlebank lock) — **PASS**

- Tip touches no Title/Hub/MetaStore remnant paths; `finishRun` still clears scrap/forge only (does not inflate `remnantsBank`).
- TitlebankV0157Test **6/6**; ForgeV0158Test.`remnantsBank_titleBinding_untouched_inForgeSources`.

### 7. New climb: pips gone (Lv1 reset) — **PASS**

- `applyClimbStartRelicsAndScrap`: `forgeStates = emptyMap()` then Soot Rim scrap only; also cleared in `finishRun`.
- Combat pips derive from `Forge.levelOf(gc.forgeStates, id)` → Lv1 → `combatPipLabel` null.
- ForgeV0158Test mid-run / climb-start green; no MetaStore forge-level persist (design frozen).

### 8. TilepolishV0159Test + full suite green; FX/SFX tip diff 0; packaging root==app-debug — **PASS**

- TilepolishV0159Test **7/7**; suite **55/432/0/0/0**; BUILD **19s**.
- FX/SFX tip diff **0**; root + app-debug md5 `ce1ff245b0c20acdff0e86c6f80f4a21`, **CMP_OK**; plate md5 **MATCH**.

---

## F — Result / blockers

**FAIL blockers: none.**

Gate result: **PASS** — tip match, packaging CMP_OK + plate MATCH, cases 1–8, unit suite green, FX/SFX tip diff 0, hard locks held (Forge costs / remnant bank / relic effects / FX/SFX).

CoS: tip holds at `ff1200c52376f256595f36bb6534fa071ad1e57a`; this gate did not tag or message.
