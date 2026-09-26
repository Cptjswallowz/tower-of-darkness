# QA Gate — v0.1.43-slashread (2026-09-26)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.43-slashread  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-26 ~16:57–17:00 EDT

**Tip (HEAD / origin/main):** `642e8fd1132f9cbe9fe044a68ea20ed69bfc936a`  
**Claimed tip:** `642e8fd1132f9cbe9fe044a68ea20ed69bfc936a` → **MATCH**  
**Base (v0.1.42-cleavekit):** `8c8c25059c272d852fc865f5c1f991ce9b462d73` — ancestor of HEAD

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `642e8fd1132f9cbe9fe044a68ea20ed69bfc936a` |
| origin/main | `642e8fd1132f9cbe9fe044a68ea20ed69bfc936a` |
| Match claimed tip | **YES** (HEAD == origin/main == claimed) |
| Subject | `feat: v0.1.43-slashread make CLEAVE slash-light phone-readable on busts` |
| Author / date | Android Engineer <android-engineer@tod.local> — 2026-09-26 16:56:23 -0400 |
| Status | `main` up to date with `origin/main`; tip SHA matches. WT has **untracked** (not in tip; do not fail): `docs/art-audio/ASHBRAND_v0.1.34.md`, `docs/art-audio/CLIMB_INTRO_v0.1.36.md`, `tools/prep_ashbrand_v0134.py` |

**`--stat` (642e8fd vs parent 8c8c250):** CleaveKit.kt (TAG `v0.1.43-slashread`, `BUST_COVERAGE=0.72`, `MEDIUM_SCALE=1.3`, peak hold 200/220ms, `CONTACT_HIT_FLASH_MS=200`), CombatFx.kt (TAG; Small/Medium `useCleaveHitFlash` contact), CombatFxOverlay.kt (SrcIn tint / `slashDrawPx` / `R.drawable.fx_slash_light`), CombatScreen.kt (contact hit-flash overlaps stroke), SlashReadV0143Test (new 12), CleaveKitV0142Test + prior FX TAG asserts advanced to v0.1.43 / 1.3, docs/slashread-v0143.md + art-audio/SLASHREAD. **10 files, +467/−67.**

**Aim / clip kept from v0.1.40-fxfix:** half-stage `recipientClipXFrac`, recipient-bust slash, Brace Shield stamps. Wake stays `WakeStageOverlay` / `wake_vfx_*` — **no** slash-light on Wake.

**Frozen (not in tip delta):** F3 graph / Cave Troll packs / Gate-Warden kits / floor-locked loadout / Hub return; F1/F2 flow; Wake art/timing/math; Brace floating pips math; damage weights; path gen; new player skills / Hub.

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 37814607 | 2026-09-26 16:56 EDT | `9a3608d2a08ace968da8c26db28a102d` |
| `app/build/outputs/apk/debug/app-debug.apk` | 37814607 | 2026-09-26 16:56 EDT | `9a3608d2a08ace968da8c26db28a102d` |

Claimed: 37814607 / `9a3608d2a08ace968da8c26db28a102d` → **MATCH** (root + build; `cmp` identical).

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
| `docs/slashread-v0143.md` | **YES** (phone-readable locks: SrcIn tint, BUST 0.72, Medium 1.3×, peak ~200/220ms, contact flash, ≤500ms @1x) |
| Contrast prior | `docs/cleavekit-v0142.md` (0.1.42: shy scale / Modulate → faint; Medium was 1.2×) |
| Art notes | `docs/art-audio/SLASHREAD_v0.1.43.md` |
| Prior gate style | `docs/qa-gate-v0142-cleavekit-2026-09-26.md` |

**Why (from design):** Elliott FAIL on 0.1.42 — Tower Pike / Hostflint showed **ONLY damage numbers**; sampler slash not phone-readable.

---

## D) Measured FX wiring (v0.1.43 locks)

Sources: `CleaveKit.kt`, `CombatFx.kt`, `CombatFxOverlay.kt`, `CombatScreen.kt`.

| Lock | Measured |
|------|----------|
| TAG | `CombatFx.TAG` = `CleaveKit.TAG` = `v0.1.43-slashread` |
| Slash sheet | `SLASH_DRAWABLE=fx_slash_light`; play frames `4,6,8,10`; peak frame **6**; cell 256 |
| Scale / bust | `BUST_COVERAGE=0.72` (in 0.60–0.80); `slashDrawPx = minSide * 0.72 * slashScale`; Medium **1.3×** (was 1.2) |
| Peak hold | Small **200 ms** / Medium **220 ms** @1x on peak (in 180–220); total stroke ≤ **500 ms** (`STROKE_SMALL_MS=400`, `STROKE_MEDIUM_MS=480`) |
| Tint | `ColorFilter.tint(..., BlendMode.SrcIn)` — gold/ember You `0xFFC9A227` / dirty green `0xFF6B7A3A` / rust `0xFFA05030` |
| Contact flash | Small/Medium damage: `useCleaveHitFlash=true`, `CONTACT_HIT_FLASH_MS=200`; overlaps stroke start in `CombatScreen` |
| Sheet path | Compile-linked `R.drawable.fx_slash_light` in overlay |
| Player Small | `hostflint`, `cinder_step`, `emberbrand`, … → FOE, gold, stroke + contact flash |
| Player Medium | `tower_pike`, `ruin_seal`, `spark_tithe`, `wake_echo` → FOE, 1.3× gold + contact |
| Enemy Small | `hit`, `club`, `shiv`, `nip`, `gate_pulse` → YOU, role tint + contact |
| Enemy Medium | `cleave`, `seal_pulse`, `coal_slam` → YOU, 1.3× + contact |
| Wake | `specForWake`: `WAKE` tier, `useWakeSlash=true`, stroke null, **no** cleave hit-flash / no slash-light |
| Brace / Iron Mantle | `iron_mantle` / `vow_plate` → `NO_STROKE`; Shield pips + Moss; `SHIELD_BLOCK_BANNED` |
| 2x | `fxHoldMs` halves: 400→200, 480→240, contact 200→100, spark flash 320→160 |
| Aim | half-stage clip FOE `0.5..1` / YOU `0..0.5`; cut does not span both busts |
| Banned | fullscreen plate / cyan ovals / shield-block (overlay KDoc + unit) |

---

## E) Unit tests

Commands (measured this gate; Aliyun Google Maven mirror used transiently for resolve after `dl.google.com` plugin resolve failure — tip / `settings.gradle.kts` restored to stock google()+mavenCentral after runs):

- `./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest` → **BUILD SUCCESSFUL** (~16:59 EDT)

Counts from XML under `app/build/test-results/testDebugUnitTest/` (**41** `TEST-*.xml` files; timestamps `2026-09-26T20:59:18Z` = **16:59 EDT**):

| Suite | Result |
|-------|--------|
| **SlashReadV0143Test** | **12/12 PASS** |
| **Floor3V0141Test** | **10/10 PASS** |
| CleaveKitV0142Test | **13/13 PASS** (TAG advanced to v0.1.43; Medium 1.3×) |
| CombatFxFixV0140Test | **6/6 PASS** |
| CombatFxReadV0139Test | **9/9 PASS** |
| CombatFxAimV0138Test | **11/11 PASS** |
| CombatFxV0137Test | **13/13 PASS** |
| **Full suite** | **41 classes / 310 tests / 0 fail / 0 err / 0 skip** |

SlashReadV0143Test methods (all green): `tag_isSlashReadV0143`, `mediumScale_is1_3`, `bustCoverage_in60to80`, `slashPlay_within500ms_peakInWindow`, `peakHold_lingersInsideStrokeBudget`, `drawableName_isFxSlashLight`, `playerSmall_containsHostflintCinderStepEmberbrand`, `playerMedium_containsTowerPike`, `enemy_hasHitClubCleaveShivNip`, `wakeAndBrace_unchanged_noSlashLightOnWake`, `aimLocks_halfStage_kept`, `speed2x_halvesHolds`.

Floor3V0141Test methods (all green): F1/F2 generator shape; floor loadout save/restore; Cave Troll art md5; F3 cave backdrop; F3 graph five mid rows / three branches / rest before boss; boss win F2 stair / F3 Hub; path combat Cave Troll pack persist; spawn weights ~20/40/40; Gate-Warden HP36 kit; Cave Troll HP28 kit.

Engineer claim: unit 310/0; SlashReadV0143Test 12/12 → **MATCH measured**.

---

## F) Cases (1–9)

| # | Case | Verdict | Method / sample |
|---|------|---------|-----------------|
| 1 | Tower Pike: gold cut on foe THEN −N — must SEE cut (~60–80% bust, Medium 1.3×) | **PASS** | `playerMedium_containsTowerPike`: MEDIUM, FOE, gold `COLOR_YOU`, `slashScale=1.3`, contact flash; `bustCoverage_in60to80` → 0.72; draw `minSide*0.72*1.3`; SrcIn tint in overlay (not Modulate wash) |
| 2 | Hostflint: smaller gold cut visible | **PASS** | `playerSmall_containsHostflintCinderStepEmberbrand`: SMALL, FOE, gold, stroke + contact; scale 1.0× × 0.72 bust (smaller than Medium) |
| 3 | Enemy Hit / Club / Cleave / Shiv / Nip: dirty green/rust cut on You only | **PASS** | `enemy_hasHitClubCleaveShivNip`: hit/club/shiv/nip in `ENEMY_SMALL` → YOU; cleave in `ENEMY_MEDIUM` → YOU 1.3×; role colors dirty green / rust |
| 4 | Wake: unchanged gold arc (not slash-light) | **PASS** | `wakeAndBrace_unchanged_noSlashLightOnWake` + CleaveKit `wakeUnchanged_artAndTier`: WAKE tier, `useWakeSlash`, stroke null, no cleave hit-flash; `wake_vfx_slash` still in APK |
| 5 | Iron Mantle: Brace pips only (no shield-block) | **PASS** | Mantle `NO_STROKE` / no stroke / no contact flash; overlay Shield+Moss; `SHIELD_BLOCK_BANNED`; no shield drawable in APK |
| 6 | 2x: cut still visible, no drag (half durations) | **PASS** | `speed2x_halvesHolds`: stroke 400→200, 480→240; contact 200→100; peak hold scales with `fxHoldMs` |
| 7 | hit-flash on cut at contact; no fullscreen plate / cyan ovals | **PASS** | CombatScreen overlaps contact flash with stroke start; `CONTACT_HIT_FLASH_MS=200`; overlay “Not full-screen” + banned plate; Brace uses Shield/Moss (not cyan oval) |
| 8 | F3 regression still green (graph/trolls/loadout/Gate-Warden→Hub) | **PASS** | Floor3V0141Test **10/10**; methods cover graph, trolls, loadout, Gate-Warden, Hub nav |
| 9 | APK contains `res/drawable/fx_slash_light.png` (~90820 B) | **PASS** | unzip: **90820** B at `res/drawable/fx_slash_light.png` (HARD met) |

---

## G) FAIL blockers

**None.** Gate **PASS**.

---

## H) WT untracked notes

Present on disk, **not** in tip (same as v0.1.42 gate; do not fail tip match):

- `docs/art-audio/ASHBRAND_v0.1.34.md`
- `docs/art-audio/CLIMB_INTRO_v0.1.36.md`
- `tools/prep_ashbrand_v0134.py`

`settings.gradle.kts` restored to stock after mirror use; `git status` clean for tracked tip files.

---

## I) Summary for parent

- Tip SHA **MATCH** `642e8fd1132f9cbe9fe044a68ea20ed69bfc936a`
- APK **MATCH** 37814607 / `9a3608d2a08ace968da8c26db28a102d` (root ≡ build)
- `fx_slash_light` in APK **90820 B** — HARD PASS
- Suite: SlashRead **12/12**; Floor3 **10/10**; CleaveKit **13/13**; FX Fix/Read/Aim/V0137 **6/9/11/13**; full **310/0** (41 classes)
- Cases 1–9 all **PASS**
- Doc: `docs/qa-gate-v0143-slashread-2026-09-26.md`
