# QA Gate — v0.1.16-wakeicon (2026-09-23)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** presentation polish; Wake math frozen  
**Measured by:** Grok Bot QA gate (executor)  
**Gate time:** Wed Sep 23, 2026 ~19:49 EDT  
**Overall: PASS**

---

## A) Git

| Check | Result |
|-------|--------|
| Claimed tip | `22bc3aec57f798161cbd3cdb519ea74ea1a7cc0a` |
| `git rev-parse HEAD` | `22bc3aec57f798161cbd3cdb519ea74ea1a7cc0a` **MATCH** |
| `origin/main` | same SHA **MATCH** |
| Branch | `main...origin/main` (clean track) |
| AuthorDate | Wed Sep 23 19:47:26 2026 -0400 (EDT) |
| Subject | `v0.1.16-wakeicon: Ashbrand polish + thicker Wake crescents` |

`git show --stat` (15 files): drop-in PNGs `ashbrand_icon`, `wake_vfx_{charge,slash,impact}`; `WakeArt.kt` polish consts; `WakeArtV0116Test.kt`; docs `wake-icon-v0116.md` + art-audio; `tools/gen_wake_art_v0116.py`. **No** `Balance.kt`, save, pips, path, Brace domain edits. `ashbrand_spark.png` **not** in commit.

---

## B) APK

| | Claimed | Measured root | Measured build |
|-|---------|---------------|----------------|
| Path | `/workspace/tower-of-darkness/tower-of-darkness-debug.apk` | same | `app/build/outputs/apk/debug/app-debug.apk` |
| Size | 17793854 | **17793854 MATCH** | **17793854 MATCH** |
| md5 | `7a3b303add8056574b8de68c705351a1` | **MATCH** | **MATCH** |
| mtime EDT | — | 2026-09-23 19:48:11 EDT | 2026-09-23 19:48:11 EDT |

Root APK == build APK (byte-identical md5).

---

## C) Unit tests (re-measure)

Command:

```bash
./gradlew :app:testDebugUnitTest \
  --tests 'com.towerofdarkness.app.WakeArtV0116Test' \
  --tests 'com.towerofdarkness.app.WakeArtV0115Test' \
  --tests 'com.towerofdarkness.app.CombatSpeedV0111Test' \
  --tests 'com.towerofdarkness.app.MidRunSaveV0112Test' \
  --tests 'com.towerofdarkness.app.StatusPipsV0113Test' \
  --tests 'com.towerofdarkness.app.BraceSyncV0114Test'
```

BUILD SUCCESSFUL. XML parse (`app/build/test-results/testDebugUnitTest/`):

| Suite | Result |
|-------|--------|
| WakeArtV0116Test | **7/7 PASS** |
| WakeArtV0115Test | **8/8 PASS** |
| CombatSpeedV0111Test | **5/5 PASS** |
| MidRunSaveV0112Test | **8/8 PASS** |
| StatusPipsV0113Test | **8/8 PASS** |
| BraceSyncV0114Test | **7/7 PASS** |
| Targeted subtotal | **43/43 PASS** |

### WakeArtV0116Test methods (all PASS)

1. `drawables_dropInNames_unchanged`
2. `icon_crackedBlade_same48dpSlot_loadoutAndCombat`
3. `crescentPolish_thickStrokeAndAshDots_impactIsBurst` — STROKE 2–3×, ASH_DOTS 8–12, IMPACT_IS_SPARK_BURST; CHARGE→SLASH→IMPACT
4. `spark_noCrescent_emberOnIconOnly`
5. `fullWake_thickArcThenGoldLogThenClear`
6. `speed2x_sameFrames_halfDuration` — holds 2300 / 1150
7. `wakeMathFrozen` — thresh 3, full 4, spark 2

### Full suite (optional)

`./gradlew :app:testDebugUnitTest` → **91/91 PASS** (16 XML classes, 0 failures/errors). Matches claimed units.

---

## D) Evidence — not sticker sword; clear of skill row

**Cracked ash-iron blade (not sticker sword):**

- Drawable: `app/src/main/res/drawable/ashbrand_icon.png` — **192×192**, 8484 bytes (was 1550 stub in prior tip).
- Generator `tools/gen_wake_art_v0116.py` paints faceted ash-iron blade, three fractures, continuous gold fuller seam, dark rounded card — not a flat emoji/sticker glyph.
- Docs: `docs/wake-icon-v0116.md`, `docs/art-audio/WAKE_ICON_v0.1.16.md`, `docs/art-audio/WAKE_ART_v0.1.16.md` lock “cracked ash-iron + gold fuller on dark square; ~48 dp”.
- Commit message: “cracked ash-iron+gold icon”.
- UI: `AshbrandIcon` default `slotSize = WakeArt.ICON_SLOT_DP.dp` (**48**); used on `LoadoutScreen` and combat `WeaponBar`.

**Spark / VFX assets:**

| File | Size | Note |
|------|------|------|
| `ashbrand_icon.png` | 192×192 / 8484 B | replaced |
| `ashbrand_spark.png` | 192×192 / 387 B | **unchanged** (not in commit) |
| `wake_vfx_charge.png` | 512×288 / 19807 B | replaced; 10 ash motes |
| `wake_vfx_slash.png` | 512×288 / 25565 B | replaced; stroke width 17; 10 ash motes (8–12) |
| `wake_vfx_impact.png` | 512×288 / 22705 B | replaced; burst at ~72% x, not second slash |

**Clear of skill row:**

- `CombatScreen.kt`: `WakeStageOverlay` lives in portrait `Box` only — `Modifier.fillMaxWidth().height(140.dp).align(Alignment.TopCenter)` with comment “safe of status bar / skill row (art margins)”.
- Skill row (`Text("Skills")` + 5 `SkillSlot`s) is a **sibling below** that Box (after float strip), not under the overlay.
- Art margins baked in gen/docs: top ≥36 px, **bottom ≥48 px**, sides ≥40 px (`WAKE_ART_v0.1.16.md`; gen arc “inside the 40/36/48 safe margins”).

---

## E) Frozen systems

`git diff HEAD~1` domain touch = presentation only (`WakeArt.kt` polish consts + comments). **No** `Balance.kt`, thresh/dmg/hold retunes, save, pips, path, Brace logic.

Frozen suites green (see C). `wakeMathFrozen` asserts thresh=3, fullDmg=4, sparkDmg=2; sequence ≤ `Balance.WEAPON_FULL_HOLD_MS`.

---

## F) Cases 1–5

| # | Claim | Verdict |
|---|-------|---------|
| 1 | Loadout + combat slot = cracked ash-iron blade (not sticker), ~48 dp | **PASS** |
| 2 | Spark: no crescent; ember on icon only | **PASS** |
| 3 | Full Wake: thicker gold arc + 8–12 ash dots + gold log; impact = one burst; overlay clears; clear of skill row | **PASS** |
| 4 | 2x: same frames, half duration (2300/1150) | **PASS** |
| 5 | Frozen: Wake math, save, pips, path, Brace still green | **PASS** |

---

## Docs cited

- `docs/wake-icon-v0116.md`
- `docs/art-audio/WAKE_ICON_v0.1.16.md`
- `docs/art-audio/WAKE_ART_v0.1.16.md`
- This gate: `docs/qa-gate-v0116-wakeicon-2026-09-23.md`

---

## Overall Gate: **PASS**
