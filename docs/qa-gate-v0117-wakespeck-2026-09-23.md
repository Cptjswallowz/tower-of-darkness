# QA Gate — v0.1.17-wakespeck (2026-09-23)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.17-wakespeck  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-23 ~20:03–20:05 EDT

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `9281aa8c660b5b02cf745a19397eb406a1be8f47` |
| origin/main | `9281aa8c660b5b02cf745a19397eb406a1be8f47` |
| Match claimed tip | **YES** |
| Subject | `v0.1.17-wakespeck: staggered gold/ash dots + impact burst` |
| Author / date | Android Engineer — 2026-09-23 20:02:11 -0400 |
| Status | `main...origin/main` (clean tracking) |

**`--stat` (this commit):** WakeArt.kt, WakeStageOverlay.kt (comment), wake_vfx_{charge,slash,impact}.png, WakeArtV0117Test.kt, docs, gen script. **ashbrand_icon / ashbrand_spark not in commit.**

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 17868837 | 2026-09-23 20:02:23 EDT | `3bc58f1094edf513a0158dc5fdeee7cf` |
| `app/build/outputs/apk/debug/app-debug.apk` | 17868837 | 2026-09-23 20:02:16 EDT | `3bc58f1094edf513a0158dc5fdeee7cf` |

Claimed: 17868837 / `3bc58f1094edf513a0158dc5fdeee7cf` → **MATCH** (root + build).

---

## C) Frozen icons

| Asset | md5 | vs claimed | in this commit? |
|-------|-----|------------|-----------------|
| `ashbrand_icon.png` | `e5bb4e3a9530929c1f26f1737a5cc7f8` | claimed `e5bb4e3a…` **MATCH** | **NO** (`git show --stat` / `git diff HEAD~1` empty) |
| `ashbrand_spark.png` | `36a10d0d431c029aea23b08bf8e25cfc` | claimed `36a10d0d…` **MATCH** | **NO** |

---

## D) Unit tests

Command (targeted + frozen suites) → **BUILD SUCCESSFUL**.

| Suite | Result |
|-------|--------|
| WakeArtV0117Test | **4/4 PASS** |
| WakeArtV0116Test | 7/7 PASS |
| WakeArtV0115Test | 8/8 PASS |
| CombatSpeedV0111Test | 5/5 PASS |
| MidRunSaveV0112Test | 8/8 PASS |
| StatusPipsV0113Test | 8/8 PASS |
| BraceSyncV0114Test | 7/7 PASS |
| Targeted aggregate | 47/47 PASS |
| **Full `:app:testDebugUnitTest`** | **95/95 PASS** (17 classes, 0 fail/err/skip) |

### WakeArtV0117Test methods

1. `drawables_dropInNames_iconSparkFrozen`
2. `fullWake_staggeredDotsAndImpactBurst`
3. `spark_noCrescentDotsOrBurst_emberOnly`
4. `frozen_mathAnd2xHolds_unchanged`

---

## E) Source (cases)

**WakeArt constants (v0.1.17):**

- `ASH_DOTS_MIN/MAX` = 8/12; `SPECK_DOTS_STAGGERED` = true
- `IMPACT_IS_SPARK_BURST` = true; burst px 8–12
- `STROKE_THICKNESS_MULT` 2–3 (frozen; no retune)
- `SPARK_HAS_CRESCENT_DOTS_OR_BURST` = false
- `WAKE_ART_FULLY_FROZEN` = true
- Sequence CHARGE→SLASH→IMPACT; clear @1300ms 1x (NONE after 400+500+400)
- Holds: 2300 / 1150; thresh/dmg 3/4/2 unchanged

**UI:** `WakeStageOverlay` still maps CHARGE/SLASH/IMPACT → same drawable names; comment updated for speck. Gen script overlays staggered dots + impact burst **without restyling crescent stroke**; slash PNG binary updated only for speck dots (stroke style frozen per WO/docs).

---

## Cases 1–5

| # | Claim | Verdict |
|---|-------|---------|
| 1 | Full Wake: 8–12 staggered dots + ~8–12px enemy impact burst; slash stroke not restyled; overlay clears | **PASS** |
| 2 | Spark: no crescent / dots / burst | **PASS** |
| 3 | Icon still 0.1.16 cracked blade (unchanged hashes) | **PASS** |
| 4 | 2x: same frames, half duration | **PASS** |
| 5 | Frozen: Wake math, save, pips, path, Brace green | **PASS** |

---

## Docs

- Spec: `docs/wake-speck-v0117.md`
- This gate: `docs/qa-gate-v0117-wakespeck-2026-09-23.md`

---

## Overall

**Gate PASS** — tip, APK, frozen icons, WakeArtV0117 4/4, frozen suites, and full 95/95 all green; cases 1–5 PASS.
