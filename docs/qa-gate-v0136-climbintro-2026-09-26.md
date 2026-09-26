# QA Gate — v0.1.36-climbintro (2026-09-26)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.36-climbintro  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-26 ~13:03–13:04 EDT

**Tip (HEAD / origin/main):** `ad5785db4e0eb356a1bc6fe2ac2f85b126ff8c0c`  
**Claimed tip:** `ad5785db4e0eb356a1bc6fe2ac2f85b126ff8c0c` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `ad5785db4e0eb356a1bc6fe2ac2f85b126ff8c0c` |
| origin/main | `ad5785db4e0eb356a1bc6fe2ac2f85b126ff8c0c` |
| Match claimed tip | **YES** (HEAD == origin/main == claimed) |
| Subject | `feat: v0.1.36-climbintro Title→Climb fullscreen clip` |
| Author / date | Android Engineer <android-engineer@tod.local> — 2026-09-26 13:03:00 -0400 |
| Status | `main` up to date with `origin/main`; tip SHA matches. WT has **untracked** (not in tip; do not fail): `docs/art-audio/ASHBRAND_v0.1.34.md`, `docs/art-audio/CLIMB_INTRO_v0.1.36.md`, `tools/prep_ashbrand_v0134.py` |

**`--stat`:** `app/build.gradle.kts` (+media3 1.9.4 exoplayer+ui), `app/src/main/assets/climb_intro.mp4` (Bin 0→14122212), MainActivity.kt, ClimbIntroGate.kt, GameController.kt, NavState.kt, ClimbIntroScreen.kt, ClimbIntroV0136Test.kt, docs/climbintro-v0136.md. **9 files, +383/−3.**

**Not in tip (frozen sources):** Weapon.kt / EnemyKit cores / PathGenerator / CombatSpeed / portrait_* / FloorArt / HubOffers / Card.kt / CombatEngine / SkillGlyph — tip does not retune You portrait, combat, Wake, Hub, path, or skills.

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 37967453 | 2026-09-26 13:02 EDT | `94267444eccbdef284fa40fb93e3a110` |
| `app/build/outputs/apk/debug/app-debug.apk` | 37967453 | 2026-09-26 13:02 EDT | `94267444eccbdef284fa40fb93e3a110` |

Claimed: 37967453 / `94267444eccbdef284fa40fb93e3a110` → **MATCH** (root + build).

---

## C) Asset climb_intro.mp4

| Check | Result |
|-------|--------|
| Source path | `app/src/main/assets/climb_intro.mp4` |
| Source size | 14122212 bytes |
| Source md5 | `ead7b72c2a37c09b82f65ff015bfc29d` |
| In APK | `assets/climb_intro.mp4` (unzip -l size 14122212) |
| Extracted from APK md5 | `ead7b72c2a37c09b82f65ff015bfc29d` |
| ClimbIntroGate.EXPECTED_MD5 | `ead7b72c2a37c09b82f65ff015bfc29d` |
| Claimed | size 14122212 / md5 `ead7b72c2a37c09b82f65ff015bfc29d` → **MATCH** (source + APK + gate const) |

Design lock: `docs/climbintro-v0136.md`. Playback URI `asset:///climb_intro.mp4`. Media3 `1.9.4`.

**CoS note (not FAIL):** source oval zooms off mid-clip by design — letterbox/`RESIZE_MODE_FIT` OK; not a crop FAIL.

---

## D) Source / design notes (climbintro)

| Lock | Evidence |
|------|----------|
| NavState.ClimbIntro | `NavState.kt` data object; MainActivity `NavState.ClimbIntro -> ClimbIntroScreen(gc)` |
| Gate | `ClimbIntroGate.shouldShowIntro(freshClimb, assetPresent) = freshClimb && assetPresent` |
| Fresh Title Climb | `GameController.climb` → `enterClimbIntroOrFresh` when no mid-run slot |
| New climb wipe | `confirmNewClimb` clears slot → `enterClimbIntroOrFresh` |
| Intro present | `shouldShowIntro(true, hasClimbIntroAsset())` → `nav = NavState.ClimbIntro` |
| Intro missing | else → `beginClimbFresh()` (silent skip) |
| Finish / skip | `finishClimbIntro` → `beginClimbFresh` only if `nav == ClimbIntro` |
| Resume | `continueClimb` body: read slot / `applyMidRunSlot` / `resumeNav` — **no** ClimbIntro / enterClimbIntro |
| Screen | `ClimbIntroScreen`: ExoPlayer Media3, `REPEAT_MODE_OFF`, black Box behind, `RESIZE_MODE_FIT`, tap overlay + Skip → `finishOnce` → `finishClimbIntro`; `player.release()` on dispose |
| Asset probe | `hasClimbIntroAsset()` opens `ClimbIntroGate.ASSET_NAME` from assets; catch → false |

**Spot-check (source):**

- `ClimbIntroGate.kt`: TAG `v0.1.36-climbintro`; ASSET_NAME `climb_intro.mp4`; EXPECTED_MD5 matches measured.
- `ClimbIntroScreen.kt`: dark full-screen; no loop; release on dispose; tap/Skip finishOnce.
- `GameController.kt`: climb / confirmNewClimb / enterClimbIntroOrFresh / finishClimbIntro / beginClimbFresh as above.
- `MainActivity.kt` TowerRoot when-branch wires ClimbIntroScreen.

---

## E) Unit tests

Commands (measured this gate):

- `./gradlew :app:testDebugUnitTest --tests 'com.towerofdarkness.app.ClimbIntroV0136Test'` → **BUILD SUCCESSFUL**
- `./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest` → **BUILD SUCCESSFUL**

Counts from XML under `app/build/test-results/testDebugUnitTest/`:

| Suite | Result |
|-------|--------|
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
| **Full `:app:testDebugUnitTest`** | **236/236 PASS** (34 classes, 0 fail/err/skip) |

### ClimbIntroV0136Test methods

1. `tag_andExpectedMd5` — PASS  
2. `freshClimb_withAsset_wantsIntro` — PASS  
3. `freshClimb_missingAsset_silentSkip` — PASS  
4. `resume_neverWantsIntro_evenWithAsset` — PASS  
5. `navState_includesClimbIntro` — PASS  
6. `assetFile_presentWithConfirmMd5` — PASS (size 14122212; md5 EXPECTED)  
7. `controller_routesFreshThroughIntroGate_notContinue` — PASS (continueClimb body has no ClimbIntro; climb/confirmNewClimb call enterClimbIntroOrFresh)  
8. `mainActivity_wiresClimbIntroScreen` — PASS  

**Measured (from passing assertions + source):**

- shouldShowIntro(true, true) = true  
- shouldShowIntro(true, false) = false  
- shouldShowIntro(false, *) = false  
- asset present with confirm md5 / size  
- continueClimb never routes intro; climb + confirmNewClimb do  

Play-path / device video: soft (design: unit covers gate+md5). No instrumented video playthrough this gate.

---

## F) Cases 1–5 vs `docs/climbintro-v0136.md`

**1) Fresh title Climb → clip plays full-screen dark behind**

- NavState.ClimbIntro; ClimbIntroScreen Media3 ExoPlayer + `Color.Black` / PlayerView black bg; `RESIZE_MODE_FIT`.
- climb / confirmNewClimb → enterClimbIntroOrFresh → nav=ClimbIntro when asset present.
- ClimbIntroGate.shouldShowIntro(true, true)=true (unit).

**2) Tap mid-clip → Floor 1, no hang**

- Transparent tap overlay + Skip → finishOnce → finishClimbIntro → beginClimbFresh.
- DisposableEffect onDispose: removeListener + player.release().
- repeatMode = REPEAT_MODE_OFF (no loop); finished guard prevents double finish.

**3) Resume mid-run → no clip**

- continueClimb body (brace-matched in test): no ClimbIntro / enterClimbIntro; uses resumeNav.
- shouldShowIntro(false, true)=false; shouldShowIntro(false, false)=false.

**4) Missing file silent skip still lets Climb work**

- shouldShowIntro(true, false)=false.
- enterClimbIntroOrFresh else branch → beginClimbFresh (no crash/toast path in gate).

**5) Frozen: You portrait, combat, Wake, Hub, path, skills — tip should not retune those**

- Tip `--stat` touches only climbintro + media3 deps + asset + test/doc; not Weapon/EnemyKit/Path/CombatSpeed/portraits/FloorArt/HubOffers/Card/CombatEngine.
- Frozen suites all green (table above).

---

## Cases 1–5

| # | Claim | Verdict |
|---|-------|---------|
| 1 | Fresh Title Climb → ClimbIntro fullscreen dark behind | **PASS** — NavState.ClimbIntro; Media3+black; enterClimbIntroOrFresh; shouldShowIntro(true,true)=true |
| 2 | Tap mid-clip → Floor 1, no hang / no loop | **PASS** — finishClimbIntro→beginClimbFresh; release on dispose; REPEAT_MODE_OFF |
| 3 | Resume mid-run → no clip | **PASS** — continueClimb body no ClimbIntro; shouldShowIntro(false,*)=false |
| 4 | Missing asset silent skip; Climb still works | **PASS** — shouldShowIntro(true,false)=false; fall through beginClimbFresh |
| 5 | Frozen portrait/combat/Wake/Hub/path/skills | **PASS** — tip does not retune; frozen suites green |

---

## Frozen spot checklist

| Suite | Count |
|-------|-------|
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

Also green (other *V0* / related): BodyArtV0118 6, BossRestV0110 4, BraceSyncV0114 7, CombatEngineTest 12, CombatSimV013 1, CombatSimV014 1, Floor2Test 9, PlateV0122 7, ShopWalletTest 6, StatusPipsV0113 8, TreasureSwapLockTest 3, VolumeArtV0120 11, WakeArtV0115 8, WakeArtV0116 7, WeaponXpTest 4.

---

## FAIL blockers

**none**

---

## Docs

- Spec: `docs/climbintro-v0136.md`
- Prior gate format: `docs/qa-gate-v0135-emberpool-2026-09-25.md`
- This gate: `docs/qa-gate-v0136-climbintro-2026-09-26.md`

---

## Overall

**Gate PASS** — tip `ad5785db4e0eb356a1bc6fe2ac2f85b126ff8c0c` (== origin/main == claimed); APK MATCH 37967453 / `94267444eccbdef284fa40fb93e3a110` (root + build); asset climb_intro.mp4 MATCH 14122212 / `ead7b72c2a37c09b82f65ff015bfc29d` (source + APK extract); ClimbIntroV0136 **8/8**; full suite **236/236** (34 classes, 0 fail/err/skip); cases 1–5 PASS; frozen suites green; oval mid-clip zoom = design note not FAIL; WT untracked ASHBRAND_v0.1.34.md + CLIMB_INTRO_v0.1.36.md + prep_ashbrand_v0134.py noted, not fail.
