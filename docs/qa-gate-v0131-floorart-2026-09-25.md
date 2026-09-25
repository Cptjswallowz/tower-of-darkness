# QA Gate — v0.1.31-floorart (2026-09-25)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.31-floorart  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-25 ~10:47–10:48 EDT

**Tip (HEAD / origin/main):** `239a333e5475d4655f6f9d6f169fe73e370abe5c`  
**Claimed tip:** `239a333e5475d4655f6f9d6f169fe73e370abe5c` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `239a333e5475d4655f6f9d6f169fe73e370abe5c` |
| origin/main | `239a333e5475d4655f6f9d6f169fe73e370abe5c` |
| Match claimed tip | **YES** |
| Subject | `v0.1.31-floorart: path backdrop + node type tokens` |
| Author / date | Android Engineer \<android-engineer@tod.local\> — 2026-09-25 10:47:01 -0400 |
| Status | `main` up to date with `origin/main`; working tree clean |

**`--stat`:** FloorArt.kt (new domain: backdrop/token constants + mapping), PathScreen.kt (FloorBackdrop + token Image + captions), drawables `floor_backdrop.jpg` + six `node_*.png`, FloorArtV0131Test.kt (13), docs (`floorart-v0131.md` + `FLOOR_ART_v0.1.31.md`), `tools/prep_floorart_v0131.py`. **13 files, +796/−61.**

**Not in tip (frozen sources):** PathGenerator / HubOffers / CombatSpeed / CombatScreen / WakeArt / portrait_* / wake_* / glyph_* / packs / 2x retune. Tip touches PathScreen + FloorArt + floor/node drawables only (no combat portrait/Wake plate cores).

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 18966098 | 2026-09-25 10:46 EDT | `8a8f42f8b7656dc209730c1e6a66d902` |
| `app/build/outputs/apk/debug/app-debug.apk` | 18966098 | 2026-09-25 10:46 EDT | `8a8f42f8b7656dc209730c1e6a66d902` |

Claimed: 18966098 / `8a8f42f8b7656dc209730c1e6a66d902` → **MATCH** (root + build).

---

## C) Source / design notes (floorart)

Design lock: `docs/floorart-v0131.md` (Art note: `docs/art-audio/FLOOR_ART_v0.1.31.md`).

| Lock | Evidence |
|------|----------|
| Backdrop shipped + dimmed | Drawable `floor_backdrop.jpg` **268735** bytes; `FloorArt.BACKDROP_DRAWABLE=floor_backdrop`; `BACKDROP_ALPHA=0.72f`; `SCRIM_ALPHA_FLOOR1=0.28f` |
| Floor 2 ~10–15% darker | `FLOOR2_EXTRA_DARK_ALPHA=0.12f`; `floor2Darker(2)=true`; `scrimAlpha(2)=0.40f` (extra **0.12** in 0.10–0.15) |
| Combat = blades token | `TOKEN_COMBAT=node_combat`; drawable `node_combat.png` **24347** bytes |
| Fog shows rumor under token | PathScreen `rumorOrType`: when `!showType` → `node.rumor` Text under chip |
| Scout flip fog → real type; EVENT stays fog | `tokenDrawableName(COMBAT,false)=node_fog` → `(true)=node_combat`; `EVENT` true/false → `node_fog` |
| Combat screen / portraits / Wake frozen | tip `--stat` excludes CombatScreen / portrait_* / wake_* / WakeArt; frozen suites green |
| Path weights / Hub / 2x untouched | tip excludes PathGenerator / HubOffers / CombatSpeed |
| Boss letter B | `tokenDrawableName(BOSS,true)=null`; PathScreen `shortType(BOSS)="B"` |
| Captions kept | PathScreen Text under token: type name when revealed/scouted; rumor when fogged; no `stub_token` |

**Drawables present (measured sizes):**

| File | Size |
|------|------|
| `floor_backdrop.jpg` | 268735 |
| `node_start.png` | 29168 |
| `node_combat.png` | 24347 |
| `node_treasure.png` | 24100 |
| `node_shop.png` | 26108 |
| `node_rest.png` | 26244 |
| `node_fog.png` | 19725 |

---

## D) Unit tests

Commands (measured this gate):

- `./gradlew :app:testDebugUnitTest --tests '*FloorArtV0131*'` → **BUILD SUCCESSFUL**
- `./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest` → **BUILD SUCCESSFUL**

Counts from XML under `app/build/test-results/testDebugUnitTest/`:

| Suite | Result |
|-------|--------|
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
| **Full `:app:testDebugUnitTest`** | **196/196 PASS** (29 classes, 0 fail/err/skip) |

### FloorArtV0131Test methods

1. `tag_andBackdropName` — PASS  
2. `backdropResourceFile_exists` — PASS  
3. `expectedTokens_sixTypes_noBoss` — PASS  
4. `artTokens_shippedAsNodeDrawables` — PASS  
5. `fogged_showsFogToken_untilScouted` — PASS  
6. `revealed_swapsToRealTypeToken` — PASS  
7. `boss_keepsLetterTreatment_noInventedToken` — PASS  
8. `floor2_darkerFlag_andScrim` — PASS  
9. `dimConstants_ringsStillReadable` — PASS  
10. `pathScreen_wiresBackdropAndFloor2Scrim` — PASS  
11. `tokenArt_notInventedWhenMissing` — PASS  
12. `tokenArt_overrideMarksShipped` — PASS  
13. `fogReveal_swapContract` — PASS  

**Measured (from passing assertions + source):**

- TAG=`v0.1.31-floorart`; BACKDROP=`floor_backdrop`; backdrop shipped
- Six tokens: `node_start/combat/treasure/shop/rest/fog`; no boss in expected set
- `TOKEN_COMBAT=node_combat`; all six drawables on disk; `allTokensShipped()=true`
- Fogged COMBAT/SHOP/EVENT → `node_fog`; revealed START/COMBAT/TREASURE/SHOP/REST → real tokens; EVENT revealed → `node_fog`
- BOSS showType=true → **null**; fogged BOSS → `node_fog`
- `BACKDROP_ALPHA=0.72f` (in 0.30–0.90); `SCRIM_ALPHA_FLOOR1=0.28f` (in 0.15–0.55); F2 extra **0.12** (`FLOOR2_EXTRA_DARK_ALPHA`)
- PathScreen source contains `FloorBackdrop`, `floor_backdrop`, `FLOOR2_EXTRA_DARK_ALPHA`/`floor2Darker`, `tokenDrawableName`, `resolveTokenResId`; **no** `stub_token`

---

## E) Cases 1–8 vs `docs/floorart-v0131.md`

**1) Map reads as a hall, not a white photo (backdrop dimmed; F2 darker scrim OK)**

- Drawable `floor_backdrop.jpg` shipped (268735).
- `dimConstants_ringsStillReadable`: `BACKDROP_ALPHA=0.72f`; `SCRIM_ALPHA_FLOOR1=0.28f`.
- `floor2_darkerFlag_andScrim`: `floor2Darker(2)=true`; extra dark **0.12** in ~10–15%; `FLOOR2_EXTRA_DARK_ALPHA=0.12f`.
- PathScreen `FloorBackdrop`: Image `R.drawable.floor_backdrop` @ `BACKDROP_ALPHA` + Floor1 scrim + optional Floor2 extra scrim.

**2) Combat node is blades (node_combat), not letter X**

- `expectedTokens_sixTypes_noBoss` / `TOKEN_COMBAT=node_combat`.
- `revealed_swapsToRealTypeToken`: COMBAT true → `node_combat`.
- Drawable `node_combat.png` exists (24347); PathScreen `resolveTokenResId` maps `TOKEN_COMBAT` → `R.drawable.node_combat`.

**3) Fog still shows rumor line under token**

- PathScreen L206–218: when not `showType` (fogged), caption Text = `node.rumor` (maxLines=2) under the token chip.
- Fogged path uses `tokenDrawableName(..., showType=false)` → `node_fog` (`fogged_showsFogToken_untilScouted`).

**4) Scout flip swaps node_fog/? → real type token; EVENT starts as fog (revealed EVENT stays node_fog)**

- `fogReveal_swapContract`: fogged COMBAT=`node_fog` → revealed=`node_combat`.
- `fogged_showsFogToken_untilScouted`: EVENT false → `node_fog`.
- `revealed_swapsToRealTypeToken`: EVENT true → `node_fog` (no separate Event crop).

**5) Combat screen unchanged (no new plates; portraits/Wake frozen)**

- tip `--stat` file list: FloorArt / PathScreen / floor+node drawables / FloorArtV0131Test / docs / prep tool only — **no** CombatScreen, portrait_*, wake_*, WakeArt, glyph_*, packs plates.
- Frozen suites green: Packs 12/12, WakeArtV0117 4/4, SkillGlyph 8/8, Portraits 7/7, Nobg 5/5, CombatSpeed 5/5, MidRunSave 8/8, RumorCharge 8/8; full **196/196**.

**6) Path layout/weights/tap targets unchanged; Hub/2x untouched**

- tip excludes PathGenerator / HubOffers / CombatSpeed (name-only check: not in tip).
- PathGeneratorTest **7/7**, PathGateV015 **1/1**, HubMore **8/8**, HubKeep **7/7**, HubV0127 **9/9**, CombatSpeed **5/5** green.

**7) BOSS still letter B (intentional)**

- `boss_keepsLetterTreatment_noInventedToken`: `tokenDrawableName(BOSS, true)=null`.
- PathScreen `shortType`: `NodeType.BOSS -> "B"`; `resolveTokenResId(null)` → 0 → letter Text path.

**8) Captions kept under tokens**

- PathScreen caption Text still under chip: type lowercase when revealed/scouted; `node.rumor` while fogged; optional `↻ rumor` affordance.
- `pathScreen_wiresBackdropAndFloor2Scrim`: asserts **no** `stub_token` in PathScreen source.

---

## Cases 1–8

| # | Claim | Verdict |
|---|-------|---------|
| 1 | Hall backdrop dimmed; F2 darker ~10–15% | **PASS** — `floor_backdrop` shipped; ALPHA **0.72** / SCRIM **0.28**; F2 extra **0.12** |
| 2 | Combat = `node_combat` blades, not X | **PASS** — `TOKEN_COMBAT=node_combat`; drawable present; resolve maps |
| 3 | Fog still shows rumor under token | **PASS** — PathScreen fog branch → `node.rumor` Text under chip |
| 4 | Scout fog→type; EVENT stays `node_fog` | **PASS** — COMBAT fog→combat; EVENT true/false=`node_fog` |
| 5 | Combat/portraits/Wake frozen | **PASS** — tip excludes cores; frozen suites + full 196/196 |
| 6 | Path weights / Hub / 2x untouched | **PASS** — tip excludes PathGenerator/HubOffers/CombatSpeed; suites green |
| 7 | BOSS letter B | **PASS** — `tokenDrawableName(BOSS,true)=null`; shortType=`B` |
| 8 | Captions kept under tokens | **PASS** — type/rumor Text under token; no `stub_token` |

---

## Docs

- Spec: `docs/floorart-v0131.md`
- Art note: `docs/art-audio/FLOOR_ART_v0.1.31.md`
- Prior gate format: `docs/qa-gate-v0129-hubmore-2026-09-25.md`
- This gate: `docs/qa-gate-v0131-floorart-2026-09-25.md`

---

## Overall

**Gate PASS** — tip `239a333e5475d4655f6f9d6f169fe73e370abe5c` (== origin/main == claimed); APK MATCH 18966098 / `8a8f42f8b7656dc209730c1e6a66d902` (root + build); FloorArtV0131 **13/13**; full suite **196/196** (29 classes, 0 fail/err/skip); cases 1–8 PASS.
