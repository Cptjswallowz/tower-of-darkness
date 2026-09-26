# QA Gate — v0.1.44-slashproof (2026-09-26)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.44-slashproof  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-26 ~17:16–17:19 EDT

**Tip (HEAD / origin/main):** `7a0c79631b05b765468a099e86000061143589d0`  
**Claimed tip:** `7a0c79631b05b765468a099e86000061143589d0` → **MATCH**  
**Base (v0.1.43-slashread):** `642e8fd1132f9cbe9fe044a68ea20ed69bfc936a` — ancestor of HEAD

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `7a0c79631b05b765468a099e86000061143589d0` |
| origin/main | `7a0c79631b05b765468a099e86000061143589d0` |
| Match claimed tip | **YES** (HEAD == origin/main == claimed) |
| Subject | `feat: v0.1.44-slashproof prove slash-light loaded and readable on busts` |
| Author / date | Android Engineer <android-engineer@tod.local> — 2026-09-26 17:15:40 -0400 |
| Status | `main` up to date with `origin/main`; tip SHA matches. WT has **untracked** (not in tip; do not fail): `docs/art-audio/ASHBRAND_v0.1.34.md`, `docs/art-audio/CLIMB_INTRO_v0.1.36.md`, `tools/prep_ashbrand_v0134.py` |

**`--stat` (7a0c796 vs parent 642e8fd):** CleaveKit.kt (TAG `v0.1.44-slashproof`, `BUST_COVERAGE=0.82`, half-stage `bustDiameterPx`, `SLASH_CROP_PX=144` @ ox=112/oy=24), CombatFx.kt (TAG; `SLASH_LIGHT_DEBUG_FMT` / `slashLightDebugLine` / `slashLightTargetLabel`), CombatFxOverlay.kt (content crop draw; bust-diameter scale; `Log.i(TodFx, line)` + `onSlashLightDebug`), CombatScreen.kt (debugTarget + combat-log echo), SlashProofV0144Test (new 13), SlashRead + CleaveKit + prior FX TAG asserts advanced, docs/slashproof-v0144.md. **10 files, +438/−41.**

**Aim / clip kept from v0.1.40-fxfix:** half-stage `recipientClipXFrac`, recipient-bust slash, Brace Shield stamps. Wake stays `WakeStageOverlay` / `wake_vfx_*` — **no** slash-light on Wake.

**Frozen (not in tip delta):** F3 graph / Cave Troll packs / Gate-Warden kits / floor-locked loadout / Hub return; F1/F2 flow; Wake art/timing/math; Brace floating pips math; damage weights; path gen; new player skills / Hub.

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 37815793 | 2026-09-26 17:15 EDT | `789d80bc5c73fe47f7eafb2415a4f3bf` |
| `app/build/outputs/apk/debug/app-debug.apk` | 37815793 | 2026-09-26 17:15 EDT | `789d80bc5c73fe47f7eafb2415a4f3bf` |

Claimed: 37815793 / `789d80bc5c73fe47f7eafb2415a4f3bf` → **MATCH** (root + build; `cmp` identical).

### HARD: `fx_slash_light.png` in APK

| Check | Result |
|-------|--------|
| `unzip -l … \| grep fx_slash_light` | `res/drawable/fx_slash_light.png` **90820** B |
| Source drawable | `app/src/main/res/drawable/fx_slash_light.png` 90820 B; md5 `e02410214790304f8d2e408472cc6cc0` |
| Presence | **PASS** (HARD requirement met; ~90820 B) |

Also in APK: `fx_hit_flash.png` (301690), `fx_hit_flash_additive.png` (230064), `wake_vfx_slash.png` (25121). **No** `shield-block` / shield drawable in APK.

---

## C) Design docs

| Doc | Present |
|-----|---------|
| `docs/slashproof-v0144.md` | **YES** (prove load + readability: crop 144, BUST 0.82, peak 200ms, debug `FX slash-light on <target>`) |
| Prior | `docs/slashread-v0143.md` (0.1.43: BUST 0.72 full-cell — unreadably thin crescent) |
| Contrast | `docs/cleavekit-v0142.md` |
| Prior gate style | `docs/qa-gate-v0143-slashread-2026-09-26.md` |

**Why (from design):** Elliott FAIL on 0.1.43 — Pike / Hostflint / Ash Press / Nip / Hit / Shiv = **numbers/log only**; atlas crescent only ~17–38% of 256 cell (heavy padding); full-cell draw at `min(w,h)×0.72` left ~30dp-thin crescent. Fix = content crop + bust-diameter × 0.82 + debug log prove.

---

## D) Measured FX wiring (v0.1.44 locks)

Sources: `CleaveKit.kt`, `CombatFx.kt`, `CombatFxOverlay.kt`, `CombatScreen.kt`.

| Lock | Measured |
|------|----------|
| TAG | `CombatFx.TAG` = `CleaveKit.TAG` = `v0.1.44-slashproof` |
| Slash sheet | `SLASH_DRAWABLE=fx_slash_light`; play frames `4,6,8,10`; peak frame **6**; cell 256 |
| Scale / bust | `BUST_COVERAGE=0.82` (in 0.70–0.90); `bustDiameterPx = min(halfStage×0.55, stageH×0.72)`; `slashDrawPx = bustDiameter × 0.82 × slashScale`; Medium **1.3×** |
| Content crop | `SLASH_CROP_PX=144`, `SLASH_CROP_OX=112`, `SLASH_CROP_OY=24`; overlay draws crop rect via `slashCropOrigin` (not full 256 cell) |
| Peak hold | Small **200 ms** / Medium **220 ms** @1x on peak (in 180–220); total stroke ≤ **500 ms** (`STROKE_SMALL_MS=400`, `STROKE_MEDIUM_MS=480`) |
| Tint | `ColorFilter.tint(..., BlendMode.SrcIn)` + brief un-tinted core — gold/ember You `0xFFC9A227` / dirty green `0xFF6B7A3A` / rust `0xFFA05030` |
| Contact flash | Small/Medium damage: `useCleaveHitFlash=true`, `CONTACT_HIT_FLASH_MS=200`; overlaps stroke start; clipped to recipient half-stage (`recipientClipXFrac`) — **not** fullscreen |
| Debug (HARD) | `SLASH_LIGHT_DEBUG_FMT = "FX slash-light on "`; overlay `LaunchedEffect` when `visible && slashSheet != null` → `Log.i("TodFx", line)` + `onSlashLightDebug`; CombatScreen echoes into combat log |
| Player Small | `hostflint`, `ash_press`, … → FOE, gold, stroke + contact flash |
| Player Medium | `tower_pike`, … → FOE, 1.3× gold + contact |
| Enemy Small | `hit`, `nip`, `shiv`, `club`, … → YOU, role tint + contact |
| Enemy Medium | `cleave`, … → YOU, 1.3× + contact |
| Wake | `specForWake`: stroke null, `useWakeSlash=true`, **no** cleave hit-flash / no slash-light |
| Brace / Iron Mantle | `iron_mantle` → `NO_STROKE`; Shield pips; `SHIELD_BLOCK_BANNED` |
| 2x | `fxHoldMs` halves peak 200→100, stroke holds |
| Aim | half-stage clip FOE `0.5..1` / YOU `0..0.5` |
| Banned | fullscreen plate / cyan ovals / shield-block |

---

## E) Unit tests

Commands (measured this gate; Aliyun Google Maven mirror used transiently for resolve after `dl.google.com` plugin resolve failure — tip / `settings.gradle.kts` restored to stock google()+mavenCentral after runs):

- `./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest` → **BUILD SUCCESSFUL** (~17:18 EDT)

Counts from XML under `app/build/test-results/testDebugUnitTest/` (**42** `TEST-*.xml` files; timestamps `2026-09-26T21:18:43Z` = **17:18 EDT**):

| Suite | Result |
|-------|--------|
| **SlashProofV0144Test** | **13/13 PASS** |
| **SlashReadV0143Test** | **12/12 PASS** |
| **Floor3V0141Test** | **10/10 PASS** |
| CleaveKitV0142Test | **13/13 PASS** |
| CombatFxFixV0140Test | **6/6 PASS** |
| CombatFxReadV0139Test | **9/9 PASS** |
| CombatFxAimV0138Test | **11/11 PASS** |
| CombatFxV0137Test | **13/13 PASS** |
| **Full suite** | **42 classes / 323 tests / 0 fail / 0 err / 0 skip** |

SlashProofV0144Test methods (all green): `tag_isSlashProofV0144`, `bustCoverage_in70to90_halfStageFormula`, `contentCrop_zoomsPastEmptyPadding`, `peakHold_200ms_totalWithin500`, `playerSmall_hasHostflintAshPress`, `playerMedium_hasTowerPike`, `enemy_hasHitNipShivCleaveClub`, `debugLog_formatConstant_andHelper`, `drawableName_andFilePresent`, `rDrawable_linkageName_matchesBasename`, `wakeAndBrace_frozen`, `mediumStaysAtLeast1_3xSmall`, `speed2x_halvesHolds`.

Floor3V0141Test methods (all green): F1/F2 generator shape; floor loadout save/restore; Cave Troll art md5; F3 cave backdrop; F3 graph five mid rows / three branches / rest before boss; boss win F2 stair / F3 Hub; path combat Cave Troll pack persist; spawn weights ~20/40/40; Gate-Warden HP36 kit; Cave Troll HP28 kit.

Engineer claim: unit 323/0; SlashProofV0144Test included → **MATCH measured** (13/13 + full 323/0).

---

## F) Cases (1–9)

| # | Case | Verdict | Method / sample |
|---|------|---------|-----------------|
| 1 | APK contains `res/drawable/fx_slash_light.png` (~90820 B) | **PASS** | `unzip -l` both root + build APK → **90820** B; source PNG 90820 B; `drawableName_andFilePresent` asserts file >10kB |
| 2 | Tower Pike: gold crescent 70–90% foe bust, hold ~200ms, THEN number — screenshot-able | **PASS** | `playerMedium_hasTowerPike`: MEDIUM, FOE, stroke, `useCleaveHitFlash`, scale 1.3; `bustCoverage_in70to90_halfStageFormula` → `BUST_COVERAGE=0.82`, `small/bust in 0.70..0.90`; `peakHold_200ms_totalWithin500` → peak frame delays = 200ms; crop zooms past padding so crescent fills bust (not thin padded cell) |
| 3 | Hostflint / Ash Press: gold crescent visible | **PASS** | `playerSmall_hasHostflintAshPress`: both in `PLAYER_SMALL`; SMALL, FOE, stroke non-null, `COLOR_YOU` gold, `useCleaveHitFlash` |
| 4 | Enemy Hit / Nip / Shiv / Cleave / Club: dirty green/rust on You only + debug log | **PASS** | `enemy_hasHitNipShivCleaveClub`: hit/nip/shiv/club → `ENEMY_SMALL` / YOU; cleave → `ENEMY_MEDIUM` / YOU; role colors dirty green / rust; debug helper labels YOU → `You` |
| 5 | Debug log format includes `FX slash-light on ` on Small/Medium (HARD) | **PASS** | `debugLog_formatConstant_andHelper`: `SLASH_LIGHT_DEBUG_FMT == "FX slash-light on "`; helper builds `FX slash-light on You|Goblin|foe`; overlay source `Log.i(CombatFx.LOG_TAG_TOD_FX, line)` when `visible && slashSheet != null`; CombatScreen `onSlashLightDebug` → combat-log echo |
| 6 | hit-flash ON cut; no fullscreen | **PASS** | CombatScreen overlaps contact flash with stroke (`useCleaveHitFlash`); `CombatHitFlashOverlay` clips via `recipientClipXFrac` on recipient bust; KDoc “Not full-screen”; banned plate/fullscreen in overlay |
| 7 | Wake gold arc unchanged; Iron Mantle Brace pips only | **PASS** | `wakeAndBrace_frozen`: `WakeArt.WAKE_ART_FULLY_FROZEN`; wake stroke null, `useWakeSlash`, no cleave flash; `iron_mantle` → `NO_STROKE` / stroke null; `wake_vfx_slash` still in APK; `SHIELD_BLOCK_BANNED` |
| 8 | F3 regression green (Floor3V0141Test) | **PASS** | Floor3V0141Test **10/10** (graph / trolls / loadout / Gate-Warden / Hub) |
| 9 | Scale: `BUST_COVERAGE~0.82`; content crop applied (not full padded 256 cell) | **PASS** | `BUST_COVERAGE=0.82` asserted; `SLASH_CROP_PX=144` / OX=112 / OY=24; `contentCrop_zoomsPastEmptyPadding`; overlay `drawImage` uses crop origin+size not full cell |

---

## G) FAIL blockers

**None.** Gate **PASS**.

HARD checks cleared: tip SHA match; APK size+md5 match; `fx_slash_light.png` present @ 90820 B; debug prefix wired (`SLASH_LIGHT_DEBUG_FMT` + `Log.i`); Pike not numbers-only (stroke + crop + bust 0.82 + peak 200ms locked by SlashProof).

---

## H) WT untracked notes

Present on disk, **not** in tip (same as v0.1.42/0.1.43 gates; do not fail tip match):

- `docs/art-audio/ASHBRAND_v0.1.34.md`
- `docs/art-audio/CLIMB_INTRO_v0.1.36.md`
- `tools/prep_ashbrand_v0134.py`

`settings.gradle.kts` restored to stock after mirror use; `git status` clean for tracked tip files.

---

## I) Summary for parent

- Tip SHA **MATCH** `7a0c79631b05b765468a099e86000061143589d0`
- APK **MATCH** 37815793 / `789d80bc5c73fe47f7eafb2415a4f3bf` (root ≡ build)
- `fx_slash_light` in APK **90820 B** — HARD PASS
- Suite: SlashProof **13/13**; SlashRead **12/12**; Floor3 **10/10**; CleaveKit **13/13**; FX Fix/Read/Aim/V0137 **6/9/11/13**; full **323/0** (42 classes)
- Cases 1–9 all **PASS**
- Doc: `docs/qa-gate-v0144-slashproof-2026-09-26.md`
