# QA Gate — v0.1.33-ashbrand (2026-09-25)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.33-ashbrand  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-25 ~17:38–17:39 EDT

**Tip (HEAD / origin/main):** `b2b3c604c918e09dd2729c5b9c78cfaee161ad2d`  
**Claimed tip:** `b2b3c604c918e09dd2729c5b9c78cfaee161ad2d` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `b2b3c604c918e09dd2729c5b9c78cfaee161ad2d` |
| origin/main | `b2b3c604c918e09dd2729c5b9c78cfaee161ad2d` |
| Match claimed tip | **YES** |
| Subject | `v0.1.33-ashbrand: blade icon crop + tap glossary (Wake math frozen)` |
| Author / date | Android Engineer \<android-engineer@tod.local\> — 2026-09-25 17:37:59 -0400 |
| Status | `main` up to date with `origin/main`; working tree clean |

**`--stat`:** MainActivity / GameController (glossary wiring), Glossary.kt (ASHBRAND_BODY + ember/spark/wake), AshbrandIcon.kt, GlossaryPopup.kt (title Ashbrand + GlossaryText nested onTerm), CombatScreen / LoadoutScreen (tap → showGlossary("ashbrand")), ashbrand_icon.png drop-in, AshbrandV0133Test.kt (5), docs (`ashbrand-v0133.md`, `art-audio/ASHBRAND_v0.1.33.md`), tools/prep_ashbrand_v0133.py. **12 files, +444/−15.**

**Not in tip (frozen sources):** HubOffers / PathGenerator / CombatSpeed / EnemyKit cores / portrait_* / FloorArt / PathScreen / wake math arrays / ashbrand_spark / wake_vfx_*. Tip touches CombatScreen + LoadoutScreen only for Ashbrand tap → glossary (in-scope). No Hub/path-math/2x/portrait/EnemyKit retune.

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 19015065 | 2026-09-25 17:37 EDT | `e73139811aa2ed071ad6fc41707548dd` |
| `app/build/outputs/apk/debug/app-debug.apk` | 19015065 | 2026-09-25 17:37 EDT | `e73139811aa2ed071ad6fc41707548dd` |

Claimed: 19015065 / `e73139811aa2ed071ad6fc41707548dd` → **MATCH** (root + build).

---

## C) Source / design / icon notes (ashbrand)

Design lock: `docs/ashbrand-v0133.md`. Art note: `docs/art-audio/ASHBRAND_v0.1.33.md`.

| Lock | Evidence |
|------|----------|
| Drawable 192×192 RGBA blade-only | `app/src/main/res/drawable/ashbrand_icon.png` — **192×192** RGBA; md5 `d0997c1572907bfb28efdbe15fd2bf63`; file 14073 bytes |
| No plate / circle baked | Four corners + four mid-edges alpha **0**; outer 8px border opaque **0 / 5888**; opaque pixels **3631 / 36864** (blade silhouette only) |
| Slot / drawable name | `WakeArt.ICON_DRAWABLE = "ashbrand_icon"`; `WakeArt.ICON_SLOT_DP = 48` |
| UI wires | `AshbrandIcon` → `painterResource(R.drawable.ashbrand_icon)`; LoadoutScreen + CombatScreen WeaponBar use `AshbrandIcon` |
| Tap → glossary only | Loadout: `AshbrandIcon(onClick = { gc.showGlossary("ashbrand") })`; Combat: `onAshbrandTap = { gc.showGlossary("ashbrand") }` → `AshbrandIcon(onClick = onAshbrandTap)` — no `resolveWeapon` / Spark spend on tap |
| Fight paused while sheet open | `GameController.combatHold`: while `glossaryTerm != null` delay 50ms and do not decrement remaining |
| Glossary title + exact body | `GlossaryDialog` title `"Ashbrand"` for ashbrand; `Glossary.ASHBRAND_BODY` multiline exact (Elliott copy) |
| Nested Ember/Spark/Wake | `GlossaryText` highlights + `onTerm` → `gc.showGlossary`; MainActivity `GlossaryDialog(onTerm = { gc.showGlossary(it) })`; ember/spark/wake defs non-blank in Glossary.kt |
| Wake math frozen | `WeaponCatalog.ashbrand` threshold 3/2/2; fullDamage [4,6,8]; sparkDamage [2,3,4] |
| Frozen VFX hashes (sha256) | ashbrand_spark `2a259d97…d283`; wake_vfx_charge `3c3ffbf4…2ae34`; wake_vfx_slash `aa5f0732…37d325`; wake_vfx_impact `f081ef30…65086` — **MATCH** art note |

**Spot-check (source):**

- `WakeArt.kt`: ICON_DRAWABLE / ICON_SLOT_DP / SPARK_DRAWABLE unchanged.
- `AshbrandIcon.kt`: ContentScale.Fit on transparent blade; tap opens glossary only (comment + onClick wire).
- `Glossary.kt`: ASHBRAND_BODY exact; ember/spark/wake short defs present.
- `GameController.resolveWeapon` only inside `runCombatBeats` when awaitingWeapon / pendingFullWake / pendingSpark — not on Ashbrand icon tap.

---

## D) Unit tests

Commands (measured this gate):

- `./gradlew :app:testDebugUnitTest --tests '*AshbrandV0133*'` → **BUILD SUCCESSFUL**
- `./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest` → **BUILD SUCCESSFUL**

Counts from XML under `app/build/test-results/testDebugUnitTest/`:

| Suite | Result |
|-------|--------|
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
| **Full `:app:testDebugUnitTest`** | **211/211 PASS** (31 classes, 0 fail/err/skip) |

### AshbrandV0133Test methods

1. `drawableName_andSlotDp_unchanged` — PASS  
2. `ashbrandGlossary_exactBody` — PASS  
3. `emberSparkWake_defsNonBlank` — PASS  
4. `wakeMath_threshold_andDamageArrays_frozen` — PASS  
5. `enemyKitSize_stillThree_smoke` — PASS  

**Measured (from passing assertions + source):**

- ICON_DRAWABLE `"ashbrand_icon"`; ICON_SLOT_DP **48**; SPARK_DRAWABLE `"ashbrand_spark"`
- Glossary body == `ASHBRAND_BODY` == Elliott multiline (Ember weapon… Cinder Vow checks for ≥1 pip.)
- ember / spark / wake definitions non-blank
- threshold(1/2/3) = **3/2/2**; fullDamage **[4,6,8]**; sparkDamage **[2,3,4]**; fullDmg/sparkDmg accessors match
- EnemyKits.skillsFor(GOBLIN/ORC/DRAGON/ASH_WARDEN) size **3** each

---

## E) Cases 1–5 vs `docs/ashbrand-v0133.md`

**1) Loadout and combat show the attached blade (no plate/circle)**

- Icon file md5 `d0997c1572907bfb28efdbe15fd2bf63`; **192×192** RGBA; corners + mid-edges transparent; outer 8px opaque 0 — no baked plate/circle.
- `drawableName_andSlotDp_unchanged`: ICON_DRAWABLE=ashbrand_icon; ICON_SLOT_DP=48.
- UI: `AshbrandIcon` uses `R.drawable.ashbrand_icon`; LoadoutScreen + CombatScreen WeaponBar wire AshbrandIcon.

**2) Tap Ashbrand tile → glossary sheet; fight still paused; does NOT fire Wake or spend Sparks**

- Loadout/Combat onClick → `gc.showGlossary("ashbrand")` only.
- `GlossaryDialog` shown from MainActivity when `gc.glossaryTerm` set.
- `combatHold` pauses while `glossaryTerm != null`.
- Tap path does not call `resolveWeapon` (resolveWeapon only in `runCombatBeats` on awaitingWeapon / pendingFullWake / pendingSpark).

**3) Glossary title Ashbrand + body matches Elliott exact copy; Ember/Spark/Wake links work**

- `ashbrandGlossary_exactBody`: body == ASHBRAND_BODY exact multiline.
- `emberSparkWake_defsNonBlank`: ember/spark/wake non-blank.
- `GlossaryDialog` title `"Ashbrand"`; body via `GlossaryText`; nested `onTerm` → `showGlossary`.

**4) Spark / Wake behavior unchanged (pip count + math frozen)**

- `wakeMath_threshold_andDamageArrays_frozen`: threshold 3/2/2; full [4,6,8]; spark [2,3,4].
- sha256 of ashbrand_spark + wake_vfx_{charge,slash,impact} **MATCH** art-note frozen table (tip does not touch those files).

**5) Enemy tiles and player skills unchanged; Hub/path/2x/portraits frozen**

- `enemyKitSize_stillThree_smoke`: kit size 3 for GOBLIN/ORC/DRAGON/ASH_WARDEN.
- tip `--stat` excludes HubOffers / PathGenerator / CombatSpeed / portrait_* / EnemyKit cores / FloorArt.
- Frozen suites green (see D): EnemyKit 10, FloorArt 13, HubMore 8, HubKeep 7, Hub 9, Packs 12, WakeArtV0117 4, SkillGlyph 8, Portraits 7, Nobg 5, CombatSpeed 5, MidRunSave 8, RumorCharge 8, PathGate 1, PathGenerator 7; full **211/211**.

---

## Cases 1–5

| # | Claim | Verdict |
|---|-------|---------|
| 1 | Blade icon no plate; 192×192; ICON_DRAWABLE/SLOT; UI wires | **PASS** — md5 `d0997c15…2bf63`; 192×192 RGBA; corners α0; outer8 opaque 0; ICON=ashbrand_icon SLOT=48; AshbrandIcon in loadout+combat |
| 2 | Tap → glossary only; fight paused; no Wake/Spark spend | **PASS** — showGlossary("ashbrand"); GlossaryDialog; combatHold pauses on glossaryTerm; no resolveWeapon on tap |
| 3 | Title Ashbrand + exact body; Ember/Spark/Wake links | **PASS** — ASHBRAND_BODY exact; ember/spark/wake non-blank; GlossaryText nested onTerm |
| 4 | Spark/Wake math + VFX hashes frozen | **PASS** — thresh 3/2/2; dmg [4,6,8]/[2,3,4]; spark+wake_vfx sha256 match art note |
| 5 | Enemy kits / Hub / path / 2x / portraits frozen | **PASS** — EnemyKits size 3; tip excludes frozen cores; frozen suites green; full 211/211 |

---

## Docs

- Spec: `docs/ashbrand-v0133.md`
- Art: `docs/art-audio/ASHBRAND_v0.1.33.md`
- Prior gate format: `docs/qa-gate-v0132-enemykit-2026-09-25.md`
- This gate: `docs/qa-gate-v0133-ashbrand-2026-09-25.md`

---

## Overall

**Gate PASS** — tip `b2b3c604c918e09dd2729c5b9c78cfaee161ad2d` (== origin/main == claimed); APK MATCH 19015065 / `e73139811aa2ed071ad6fc41707548dd` (root + build); icon md5 `d0997c1572907bfb28efdbe15fd2bf63` 192×192 RGBA no plate; AshbrandV0133 **5/5**; full suite **211/211** (31 classes, 0 fail/err/skip); cases 1–5 PASS.
