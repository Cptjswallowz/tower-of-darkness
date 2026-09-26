# QA Gate — v0.1.42-cleavekit (2026-09-26)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.42-cleavekit  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-26 ~16:27–16:37 EDT

**Tip (HEAD / origin/main):** `8c8c25059c272d852fc865f5c1f991ce9b462d73`  
**Claimed tip:** `8c8c25059c272d852fc865f5c1f991ce9b462d73` → **MATCH**  
**Base (v0.1.41-floor3):** `ad0160dc21e888e394415f38db7cfcc51a319b7b` (`release: v0.1.41-floor3 README + QA gate`) — ancestor of HEAD

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `8c8c25059c272d852fc865f5c1f991ce9b462d73` |
| origin/main | `8c8c25059c272d852fc865f5c1f991ce9b462d73` |
| Match claimed tip | **YES** (HEAD == origin/main == claimed) |
| Subject | `feat: v0.1.42-cleavekit slash-light + hit-flash into FX kernel` |
| Author / date | Android Engineer <android-engineer@tod.local> — 2026-09-26 16:26:46 -0400 |
| Status | `main` up to date with `origin/main`; tip SHA matches. WT has **untracked** (not in tip; do not fail): `docs/art-audio/ASHBRAND_v0.1.34.md`, `docs/art-audio/CLIMB_INTRO_v0.1.36.md`, `tools/prep_ashbrand_v0134.py` |

**`--stat` (8c8c250 vs parent ad0160d):** CleaveKit.kt (new), CombatFx.kt (TAG `v0.1.42-cleavekit`, `ENEMY_SMALL`+club/gate_pulse, `ID_ASHBRAND_SPARK` / `specForSpark`), CombatFxOverlay.kt (slash atlas + hit-flash additive / `BlendMode.Plus`; Brace pips unchanged), CombatScreen.kt (hit-flash hold + `fxHoldMs` stroke), CombatEngine.kt (spark `fxId`), drawables `fx_slash_light` / `fx_hit_flash` / `fx_hit_flash_additive`, CleaveKitV0142Test (new), prior FX TAG asserts advanced, docs/cleavekit-v0142.md + art-audio/CLEAVE + prep tool. **16 files, +1121/−53.**

**Aim / clip kept from v0.1.40-fxfix:** half-stage `recipientClipXFrac`, recipient-bust slash, tamed glow, Brace Shield stamps. **No** shield-block import. Wake stays `WakeStageOverlay` / `wake_vfx_*`.

**Frozen (not in tip delta):** F3 graph / Cave Troll packs / Gate-Warden kits / floor-locked loadout / Hub return; F1/F2 flow; Wake art/timing/math; Brace floating pips math; damage weights; path gen; new player skills / Hub.

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 37705822 | 2026-09-26 16:26 EDT | `f25ed5bd73766dd1100d0dd891c04dd6` |
| `app/build/outputs/apk/debug/app-debug.apk` | 37705822 | 2026-09-26 16:26 EDT | `f25ed5bd73766dd1100d0dd891c04dd6` |

Claimed: 37705822 / `f25ed5bd73766dd1100d0dd891c04dd6` → **MATCH** (root + build; `cmp` identical).

---

## C) Design + art md5s

| Doc | Present |
|-----|---------|
| `docs/cleavekit-v0142.md` | **YES** (wire map / Club+Gate Pulse Small / playback locks / DO NOT) |
| Prior gate style | `docs/qa-gate-v0141-floor3-2026-09-26.md` |
| Art stage notes | `docs/art-audio/CLEAVE_v0.1.42.md` |

### Art md5s (FAIL if wrong) — measured this gate

| Asset | Claimed md5 | `app/src/main/res/drawable/` | APK `res/drawable/` (unzip) |
|-------|-------------|------------------------------|-----------------------------|
| `fx_slash_light.png` | `e02410214790304f8d2e408472cc6cc0` | **MATCH** | **MATCH** |
| `fx_hit_flash.png` | `610cf76722f5f0ea852d60b85e2be8a5` | **MATCH** | **MATCH** |
| `fx_hit_flash_additive.png` | `11635e0b56822407975eab4d884bb491` | **MATCH** | **MATCH** |

No `fx_shield_block` / `shield-block` drawable present (confirmed listing + unit).

---

## D) Measured FX wiring

Sources: `CleaveKit.kt`, `CombatFx.kt`, `CombatFxOverlay.kt`, `CombatScreen.kt`, `CombatEngine.kt`.

| Lock | Measured |
|------|----------|
| TAG | `CombatFx.TAG` = `CleaveKit.TAG` = `v0.1.42-cleavekit` |
| Slash sheet | `CleaveKit.SLASH_DRAWABLE` = `fx_slash_light`; play frames `4,6,8,10`; peak 6 in window; cell 256; `MEDIUM_SCALE=1.2f` |
| Hit-flash | Prefer `fx_hit_flash_additive` then `fx_hit_flash`; `HIT_FLASH_PLAY_FRAMES` 0..7; `HIT_FLASH_MS=320`; draw `BlendMode.Plus` |
| Player Small | `PLAYER_SMALL` incl. `hostflint`; `specForPlayer` → FOE recipient; tint `COLOR_YOU` `0xFFC9A227` (gold ember); half-stage FOE clip `0.5..1` |
| Enemy Small | `ENEMY_SMALL` = `shiv,nip,hit,club,gate_pulse`; FOE→YOU; role colors dirty green `0xFF6B7A3A` / rust `0xFFA05030` (Cave Troll→STURDY_ORC; Gate-Warden→SEAL_WARDEN copper) |
| Player Medium | `tower_pike` etc. → `FxTier.MEDIUM`; `slashScale=1.2f`; FOE only |
| Sparks | `ID_ASHBRAND_SPARK` / `specForSpark()` → `useCleaveHitFlash=true`, FOE, no stroke, `flashMs=0`; engine wires spark `fxId` |
| Peak window ≤500ms @1x | `STROKE_SMALL_MS=400`, `STROKE_MEDIUM_MS=480`, `MAX_SLASH_MS_1X=500`; `slashPlayWithinBudget()` |
| 2x | `CombatFx.fxHoldMs` halves: 400→200, 480→240, 320→160 |
| Input | CombatScreen comment: fail-safe; **never blocks** resolve+log; skip / 2x still apply via existing speed snapshot |
| Order | tile flash → stroke \| brace-pips \| hit-flash → float → shake → log (`CombatScreen` LaunchedEffect) |
| Brace | `CombatBracePipsOverlay` — `Icons.Filled.Shield` + **Moss** (not cyan oval); `SHIELD_BLOCK_BANNED`; no shield-block sheet |
| Wake | `WakeArt.WAKE_ART_FULLY_FROZEN`; `wake_vfx_slash` / `ashbrand_spark` icon; `specForWake` unchanged; no cleave hit-flash on Wake |
| Tile flash | `CombatTileFlash` — skill tile only; banned whole-row / fullscreen plate (overlay KDoc) |
| Iron Mantle / Hide | `PLAYER_NO_STROKE` / `ENEMY_NO_STROKE`; playSpec brace pips on owner; stroke null |

---

## E) Unit tests

Commands (measured this gate; Aliyun Google Maven mirror used transiently for resolve after `dl.google.com` read timeouts — tip / `settings.gradle.kts` restored to stock google()+mavenCentral after runs):

- `./gradlew :app:testDebugUnitTest --tests 'com.towerofdarkness.app.CleaveKitV0142Test' --tests '…Floor3V0141Test' --tests '…CombatFxFixV0140Test' --tests '…CombatFxAimV0138Test'` → **BUILD SUCCESSFUL** (~16:36 EDT)
- `./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest` → **BUILD SUCCESSFUL** (~16:37 EDT)

Counts from XML under `app/build/test-results/testDebugUnitTest/` (**40** `TEST-*.xml` files):

| Suite | Result |
|-------|--------|
| CleaveKitV0142Test | **13/13 PASS** |
| Floor3V0141Test | **10/10 PASS** |
| CombatFxFixV0140Test | **6/6 PASS** |
| CombatFxAimV0138Test | **11/11 PASS** |
| CombatFxReadV0139Test | **9/9 PASS** |
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
| **Full `:app:testDebugUnitTest`** | **298/298 PASS** (40 classes, 0 fail / 0 err / 0 skip) |

Claimed CleaveKit **13** / Floor3 **10** / suite **298/0** → **MATCH** (was 285 @ v0.1.41 + CleaveKitV0142 **13** = **298**).

### CleaveKitV0142Test methods (13/13)

1. `tag_isCleaveKitV0142` — PASS  
2. `onlySlashLightAndHitFlashImported_noShieldBlock` — PASS  
3. `atlasMetadata_matchesArtLock` — PASS  
4. `rowMajorCellOrigin` — PASS  
5. `hostflintAndHit_oneBustFoeOrYou` — PASS  
6. `towerPike_heavier1_2x_foeOnly` — PASS  
7. `mantleAndHide_bracePips_noSlash_noShieldBlock` — PASS  
8. `ashbrandSpark_hitFlashOnFoe_notFullScreen` — PASS  
9. `engine_sparkWiresFxId_hitFlash` — PASS  
10. `wakeUnchanged_artAndTier` — PASS  
11. `speed2x_halvesSlashAndHitFlash` — PASS  
12. `clubAndGatePulse_mapEnemySmall_notMedium` — PASS  
13. `aimLocks_fromV0140_kept` — PASS  

---

## F) Cases 1–8 vs `docs/cleavekit-v0142.md`

**1) Player Small = slash-light gold/ember on foe bust only (tile → stroke → number → shake → log; no fullscreen plate)** — **PASS**  
- `hostflint` → SMALL / FOE / `COLOR_YOU`; `slashCutGeom` does not span both busts; half-stage FOE clip  
- Overlay: `fx_slash_light` tint Modulate on recipient bust inside `clipRect`  
- Order in `CombatScreen`; tile flash is `CombatTileFlash` on skill tile only  
- Unit: `hostflintAndHit_oneBustFoeOrYou`, `aimLocks_fromV0140_kept`

**2) Enemy Small = slash-light dirty green/rust on You bust (Club + Gate Pulse included)** — **PASS**  
- `ENEMY_SMALL` includes `club`,`gate_pulse`; tier SMALL not MEDIUM; recipient YOU  
- Role colors: WEAK_GOBLIN dirty green / STURDY_ORC rust (Cave Troll maps STURDY_ORC)  
- Unit: `clubAndGatePulse_mapEnemySmall_notMedium`, `hostflintAndHit_oneBustFoeOrYou` (hit→YOU)

**3) Player Medium = slash-light 1.2× thicker gold foe only (Tower Pike)** — **PASS**  
- `tower_pike` → MEDIUM / FOE / `THICK_MEDIUM`; `CleaveKit.slashScale(MEDIUM)=1.2f`  
- Unit: `towerPike_heavier1_2x_foeOnly`, `atlasMetadata_matchesArtLock`

**4) Sparks (Ashbrand) = hit-flash additive on hit target only; prefer additive / BlendMode.Plus** — **PASS**  
- `specForSpark`: `useCleaveHitFlash`, FOE, no stroke, half-stage FOE clip; not Wake  
- Overlay prefers `HIT_FLASH_ADDITIVE_DRAWABLE` + `BlendMode.Plus`  
- Engine spark log `fxId=ashbrand_spark`  
- Unit: `ashbrandSpark_hitFlashOnFoe_notFullScreen`, `engine_sparkWiresFxId_hitFlash`

**5) Peak window ≤500ms @1x (2x half); never block input** — **PASS**  
- Small 400 / Medium 480 ≤ `MAX_SLASH_MS_1X=500`; hit-flash 320  
- `fxHoldMs` halves; CombatScreen: never blocks resolve+log; skip/2x kept  
- Unit: `atlasMetadata_matchesArtLock`, `speed2x_halvesSlashAndHitFlash`

**6) Brace = existing pips only (NO shield-block). Wake clip unchanged** — **PASS**  
- No shield-block drawable; `SHIELD_BLOCK_BANNED`; Brace = Moss Shield pips + half-stage owner clip  
- Wake: `WAKE_ART_FULLY_FROZEN`, `wake_vfx_slash`, FRAME_SLASH_MS 700; no cleave hit-flash on Wake  
- Unit: `onlySlashLightAndHitFlashImported_noShieldBlock`, `mantleAndHide_bracePips_noSlash_noShieldBlock`, `wakeUnchanged_artAndTier`

**7) F3 graph / Cave Troll / loadout lock / Gate-Warden → Hub still green (regression)** — **PASS**  
- Floor3V0141Test **10/10**; FX Fix/Aim/Read/V0137 green; full suite **298/298**  
- Club/Gate Pulse now explicit Enemy Small (was fail-soft Small in 0.1.41) — not a F3 kit/number change

**8) 2x halves durations correctly** — **PASS**  
- Measured: `fxHoldMs(400,2)=200`, `(480,2)=240`, `(320,2)=160`, Brace `(700,2)=350`  
- Unit: `speed2x_halvesSlashAndHitFlash`; CombatFxV0137 `fxHoldMs_2x_halvesDurations`

### Also checked

| Check | Verdict |
|-------|---------|
| Hostflint / Hit one-bust | **PASS** — FOE / YOU only; no span-both (`hostflintAndHit_oneBustFoeOrYou`) |
| Iron Mantle / Hide shields no cyan oval | **PASS** — NO_STROKE + Moss Shield pips; no cyan fill (`mantleAndHide_*`) |
| Suite green | **PASS** — 298/298, 0 fail/err/skip |

---

## Cases 1–8 summary

| # | Claim | Verdict |
|---|-------|---------|
| 1 | Player Small slash-light gold foe only; order; no fullscreen | **PASS** |
| 2 | Enemy Small dirty green/rust on You; Club+Gate Pulse | **PASS** |
| 3 | Player Medium 1.2× gold foe (Tower Pike) | **PASS** |
| 4 | Sparks hit-flash additive on foe; Plus blend | **PASS** |
| 5 | Peak ≤500ms @1x; 2x half; never block input | **PASS** |
| 6 | Brace pips only; no shield-block; Wake unchanged | **PASS** |
| 7 | F3 / Cave Troll / loadout / Gate-Warden→Hub regression green | **PASS** |
| 8 | 2x halves durations | **PASS** |

---

## G) Frozen F3 / Wake / Brace + FX spot

| Suite | Count |
|-------|-------|
| CleaveKitV0142Test | 13/13 |
| Floor3V0141Test | 10/10 |
| CombatFxFixV0140Test | 6/6 |
| CombatFxAimV0138Test | 11/11 |
| CombatFxReadV0139Test | 9/9 |
| CombatFxV0137Test | 13/13 |
| ClimbIntroV0136Test | 8/8 |
| EmberPoolV0135Test | 11/11 |
| BladeV0134Test | 6/6 |
| AshbrandV0133Test | 5/5 |
| EnemyKitV0132Test | 10/10 |
| FloorArtV0131Test | 13/13 |
| WakeArtV0117Test | 4/4 |
| MidRunSaveV0112Test | 8/8 |
| **Full suite** | **298/298** (40 classes, 0 fail/err/skip) |

F3 graph / packs / kits / loadout lock / Hub return frozen (Floor3 **10/10**). Wake art frozen. Brace floating pips unchanged (no shield-block).

---

## Overall

**PASS**

**FAIL blockers:** none

**WT untracked (not in tip):** `docs/art-audio/ASHBRAND_v0.1.34.md`, `docs/art-audio/CLIMB_INTRO_v0.1.36.md`, `tools/prep_ashbrand_v0134.py`
