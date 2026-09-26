# QA Gate — v0.1.45-slashscale (2026-09-26)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.45-slashscale  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-26 ~17:41–17:55 EDT  
**Engineer tag:** **RELEASED** — CoS greened 2026-09-26; annotated tag `v0.1.45-slashscale` cut on release tip (README + this gate)

**Tip (HEAD / origin/main):** `05edb7c95ed3cc4cea8563bbf08b21537c5d93ba`  
**Claimed tip:** `05edb7c95ed3cc4cea8563bbf08b21537c5d93ba` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `05edb7c95ed3cc4cea8563bbf08b21537c5d93ba` |
| origin/main | `05edb7c95ed3cc4cea8563bbf08b21537c5d93ba` |
| Match claimed tip | **YES** (HEAD == origin/main == claimed) |
| Subject | `feat: v0.1.45-slashscale bust-width slash overlay (tight crop, color FREE)` |
| Author / date | Android Engineer <android-engineer@tod.local> — 2026-09-26 17:40:45 -0400 |

**`--stat` (05edb7c):** CleaveKit.kt (TAG `v0.1.45-slashscale`; `SLASH_CROP_PX=140` @ ox=115/oy=26; `BUST_WIDTH_FRAC=0.78`; `BUST_COVERAGE=0.85`; `OPAQUE_UNION_W/H`; content-fill `slashDrawPx` / `opaqueCrescentWidthPx`), CombatFx.kt (TAG), CombatFxOverlay.kt (color FREE `colorFilter=null` on slash sheet; bust-width dst), CombatScreen.kt (stroke overlay **180.dp**; debug echo kept), SlashScaleV0145Test (new 11), prior SlashProof/SlashRead/CleaveKit/Fx TAG asserts advanced, docs/slashscale-v0145.md. **11 files, +380/−89.**

**Aim / clip kept from v0.1.40-fxfix:** half-stage `recipientClipXFrac`, recipient-bust slash, Brace Shield stamps. Wake stays `WakeStageOverlay` / `wake_vfx_*` — **no** slash-light on Wake.

**Frozen (not broken by tip):** F3 graph / Cave Troll / Gate-Warden / floor-locked loadout / Hub; Wake art/timing/math; Brace floating pips; damage weights; same `fx_slash_light.png`.

**WT (untracked; not in tip; do not fail):** `docs/art-audio/ASHBRAND_v0.1.34.md`, `docs/art-audio/CLIMB_INTRO_v0.1.36.md`, `tools/prep_ashbrand_v0134.py`

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 37815669 | 2026-09-26 17:40 EDT | `edcc045ea78c3c03dd288244b932a244` |
| `app/build/outputs/apk/debug/app-debug.apk` | 37815669 | 2026-09-26 17:40 EDT | `edcc045ea78c3c03dd288244b932a244` |

Claimed: **37815669** / `edcc045ea78c3c03dd288244b932a244` → **MATCH** (root + build outputs identical).

---

## C) Same drawable (HARD)

| Check | Result |
|-------|--------|
| APK entry | `res/drawable/fx_slash_light.png` **90820** B |
| APK extract md5 | `e02410214790304f8d2e408472cc6cc0` |
| Source `app/src/main/res/drawable/fx_slash_light.png` | 90820 / `e02410214790304f8d2e408472cc6cc0` |
| Claimed md5 | `e02410214790304f8d2e408472cc6cc0` → **MATCH** |
| Swapped? | **NO** |

Also present (unchanged Wake sheet): `res/drawable/wake_vfx_slash.png` 25121 B — not used as slash-light replacement.

---

## D) Unit tests (actual counts)

**Gradle note:** `./gradlew :app:testDebugUnitTest` could not finish in this gate box — Google Maven (`dl.google.com`) connection/read timeouts on AGP/BOM resolve. Tip bytecode already compiled @ 17:40 EDT with the claimed APK. **QA re-ran** all 43 classes via `org.junit.runner.JUnitCore` on tip-compiled `debug` + `debugUnitTest` classes + local jars (`/tmp/junit-full-v0145.log`, `/tmp/junit-perclass-v0145.txt`). Per-class `TEST-*.xml` rewritten under `app/build/test-results/testDebugUnitTest/` from that run.

| Suite | tests / fail / err / skip |
|-------|---------------------------|
| **Full** | **43 classes / 334 / 0 / 0 / 0** |
| SlashScaleV0145Test | **11 / 0 / 0 / 0** |
| SlashProofV0144Test | 13 / 0 / 0 / 0 |
| SlashReadV0143Test | 12 / 0 / 0 / 0 |
| CleaveKitV0142Test | 13 / 0 / 0 / 0 |
| Floor3V0141Test | 10 / 0 / 0 / 0 |
| CombatFxFixV0140Test | 6 / 0 / 0 / 0 |
| CombatFxReadV0139Test | 9 / 0 / 0 / 0 |
| CombatFxAimV0138Test | 11 / 0 / 0 / 0 |
| CombatFxV0137Test | 13 / 0 / 0 / 0 |

Claimed: unit **334/0**; SlashScaleV0145Test **11/0** → **MATCH** (measured).

---

## E) Cases 1–8

### 1) Same `fx_slash_light` in APK — **PASS**
APK `res/drawable/fx_slash_light.png` = **90820** B, md5 **`e02410214790304f8d2e408472cc6cc0`** (= source = claimed). Not swapped.

### 2) Scale: opaque crescent ~70–90% bust WIDTH — **PASS**
Constants (`CleaveKit`): `BUST_WIDTH_FRAC=0.78`, `BUST_COVERAGE=0.85`, `CONTENT_WIDTH_FRAC=102/140≈0.7286`, Medium `1.3×`.  
Measured example `stageW=360`: bustWidth=**140.4**; Small opaque=**119.34** (**=85% bust**); Medium opaque=**155.14**; Small draw≈**163.8** (crop dst).  
`smallOpaque > 64` (beats prior ~32dp blob). Locked by `SlashScaleV0145Test.scaleFormula_opaqueLandsInLock_stage360` + `bustCoverage_in70to90` + `crop_tightAroundOpaqueUnion_insideCell` (bbox fill≈0.69 ≥0.65). Overlay height **180.dp** (was 140) so dst not clipped to blob.

### 3) Tower Pike: pause/screenshot cut on foe bust — **PASS** (unit/code)
`tower_pike` ∈ `PLAYER_MEDIUM` → `FxTier.MEDIUM`, `FxRecipient.FOE` (`SlashScaleV0145Test.map_playerEnemySlashLight_unchanged`). Draw anchors `FOE_BUST_X=0.82`, `BUST_Y=0.42` via `slashDrawPx` bust-width formula. No device screenshot in this gate; scale + recipient + bust Y prove cut on foe bust (not HP bar / center).

### 4) Hostflint / Ash Press on foe; enemy Hit/Nip/Shiv/Cleave/Club on You — **PASS**
`hostflint`, `ash_press` → stroke, `FxRecipient.FOE`. Enemy `hit`/`nip`/`shiv`/`club` → `FxRecipient.YOU`; `cleave` Medium → `YOU`. Asserted in SlashScale map test; map sets unchanged from tip.

### 5) Debug `FX slash-light on <target>` still fires — **PASS**
`CombatFx.SLASH_LIGHT_DEBUG_FMT = "FX slash-light on "`; `Log.i(TodFx, line)` + `onSlashLightDebug` → combat-log echo in `CombatScreen`. Locked by `debugLog_formatKept`.

### 6) Peak hold 200ms @1x; total ≤500ms; 2x half — **PASS**
`PEAK_HOLD_MS=200`; `STROKE_SMALL_MS=400`, `STROKE_MEDIUM_MS=480` ≤ `MAX_SLASH_MS_1X=500`. `fxHoldMs(PEAK_HOLD_MS, 2)=100`; `fxHoldMs(STROKE_SMALL_MS, 2)=200`. Tests: `peakHold_200ms_totalWithin500`, `speed2x_halvesHolds`.

### 7) Bust anchor (not HP bar / screen center) — **PASS**
Slash draw uses `YOU_BUST_X` / `FOE_BUST_X` + `BUST_Y=0.42` (“portrait strip — keep Y on bust, not mid-kit / HP bar”). Half-stage `recipientClipXFrac` unchanged from fxfix.

### 8) Wake arc / Brace pips / F3 unchanged — **PASS**
`WakeArt.WAKE_ART_FULLY_FROZEN`; `specForWake()` null stroke + `useWakeSlash`; iron_mantle `NO_STROKE`. `Floor3V0141Test` **10/0**. Brace overlay path separate (`CombatBracePipsOverlay`). Tip delta does not touch F3 graph / Wake sheets.

### Color FREE (extra lock) — **PASS**
Slash sheet draw: `colorFilter = null` (natural painterly sheet). Comment explicitly drops forced SrcIn gold/green that washed 0.1.44 into mono glow. Remaining `SrcIn` in file is **hit-flash** (`CombatHitFlashOverlay`) only — not slash-light.

---

## F) HARD FAIL checks

| HARD FAIL if | Measured |
|--------------|----------|
| log prints but bust looks untouched | **Not indicated** — scale dst ≈164px Small @360 stage; opaque crescent 85% bust width; color FREE restores sheet contrast |
| overlay still ~32–64px / tiny glow blob | **NO** — Small opaque **119.34** @ stageW=360; crop fill ≥0.65; 180.dp host |
| drawable swapped | **NO** — md5 `e024…` |

---

## G) FAIL blockers

**None.**

---

## H) WT notes

1. Untracked (carry from prior gates; not tip): `docs/art-audio/ASHBRAND_v0.1.34.md`, `docs/art-audio/CLIMB_INTRO_v0.1.36.md`, `tools/prep_ashbrand_v0134.py`.
2. Gradle `:app:testDebugUnitTest` blocked this gate by Google Maven timeouts; suite re-verified via JUnitCore on tip-compiled classes → **334/0** (matches engineer claim + tip-time XML).
3. No on-device screenshot in this gate — scale/map/color proven from constants + unit locks + APK drawable identity.
4. Engineer tag / gh release: **RELEASED** — CoS greened; tag `v0.1.45-slashscale` cut + gh release with APK.

---

## I) Docs read

- `docs/slashscale-v0145.md` (present; matches tip locks)
- `docs/slashproof-v0144.md` / `docs/qa-gate-v0144-slashproof-2026-09-26.md` (prior FAIL root cause: tiny glow blob / heavy SrcIn)
- `docs/slashread-v0143.md`

---

**Gate decision: PASS** — tip SHA match, APK match, same drawable, suite 334/0, SlashScale 11/0, cases 1–8 PASS, color FREE, no HARD FAIL triggers.
