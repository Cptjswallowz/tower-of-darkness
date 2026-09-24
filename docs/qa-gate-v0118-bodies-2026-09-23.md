# QA Gate — v0.1.18-bodies (2026-09-23)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.18-bodies  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-23 ~20:19–20:20 EDT

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `8a3ac1b9750fb82de30bf32d578efee23a4b6378` |
| origin/main | `8a3ac1b9750fb82de30bf32d578efee23a4b6378` |
| Match claimed tip | **YES** |
| Subject | `v0.1.18-bodies: soldier + Ash-Warden F2 stills in portrait slots` |
| Author / date | Android Engineer — 2026-09-23 20:17:37 -0400 |
| Status | `main...origin/main` (clean tracking) |

**`--stat` (this commit):** BodyArt.kt (+47), Enemy.kt (ASH_WARDEN asset → `portrait_ash_warden`), HeroShowcase.kt (HeroShowcase + EnemySilhouette wiring), CombatScreen.kt (EnemySilhouette(kind)), MainMenuScreen.kt (HeroShowcase size), `portrait_ash_warden.png`, `portrait_you.png`, BodyArtV0118Test.kt, docs (bodies-v0118 / BODIES_v0.1.18 / art-audio-v0), `tools/gen_bodies_v0118.py`. **12 files, +456/−35.** No WakeArt / CombatSpeed / MidRunSave / StatusPips / Brace / Path / combat-math sources in commit.

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 18211743 | 2026-09-23 20:17:33 EDT | `dfa81ae6308304d934b326f89105cbbc` |
| `app/build/outputs/apk/debug/app-debug.apk` | 18211743 | 2026-09-23 20:17:27 EDT | `dfa81ae6308304d934b326f89105cbbc` |

Claimed: 18211743 / `dfa81ae6308304d934b326f89105cbbc` → **MATCH** (root + build).

---

## C) Body assets / bindings

| Asset / binding | File / rule | md5 / evidence |
|-----------------|-------------|----------------|
| You (soldier) | `app/src/main/res/drawable/portrait_you.png` | `6dad64e5b82bd46829915d7ee04540ea` |
| Ash-Warden (F2) | `app/src/main/res/drawable/portrait_ash_warden.png` | `186aecec73fea2cf5f51868ddbf214e6` |
| F1 Seal-Warden | **no new drawable**; Canvas Ember orange placeholder | `BodyArt.enemyPortraitDrawableName(DRAGON)==null`; `EnemyKind.DRAGON.asset` still `enemies/enemy_dragon.png` |
| Trash / hallway enemy | **no new drawable**; Canvas Steel circle | `hallwayEnemyUsesPlaceholder()==true`; `enemyPortraitDrawableName` null for GOBLIN/ORC/TROLL/SPIDER |
| Compose You | `HeroShowcase` → `R.drawable.portrait_you` | HeroShowcase.kt:75 |
| Compose F2 | `EnemySilhouette` → `R.drawable.portrait_ash_warden` when `BodyArt` returns name | HeroShowcase.kt:95–119; Enemy.kt:16 |
| No wardrobe / poses / hit-flash | docs + BodyArt comment; no such code/assets in commit | confirmed via `git show --stat` + source grep (docs only) |

---

## D) Unit tests

Commands:

- `./gradlew :app:testDebugUnitTest --tests '*BodyArtV0118*' --tests '*WakeArtV0117*' --tests '*WakeArtV0116*' --tests '*WakeArtV0115*' --tests '*CombatSpeedV0111*' --tests '*MidRunSaveV0112*' --tests '*StatusPipsV0113*' --tests '*BraceSyncV0114*'` → **BUILD SUCCESSFUL**
- `./gradlew :app:testDebugUnitTest` → **BUILD SUCCESSFUL**

| Suite | Result |
|-------|--------|
| BodyArtV0118Test | **6/6 PASS** |
| WakeArtV0117Test | **4/4 PASS** (no regress) |
| WakeArtV0116Test | 7/7 PASS |
| WakeArtV0115Test | 8/8 PASS |
| CombatSpeedV0111Test | 5/5 PASS |
| MidRunSaveV0112Test | 8/8 PASS |
| StatusPipsV0113Test | 8/8 PASS |
| BraceSyncV0114Test | 7/7 PASS |
| **Full `:app:testDebugUnitTest`** | **101/101 PASS** (18 classes, 0 fail/err/skip) |

### BodyArtV0118Test methods

1. `playerPortrait_resolvesSoldierDrawable` — PASS
2. `hallway_youIsSoldier_enemyStaysPlaceholder` — PASS
3. `ashWarden_f2Boss_resolvesBodyStill` — PASS
4. `f1SealWarden_dragon_staysPlaceholder` — PASS
5. `trashAndSealSpinner_stayPlaceholders` — PASS
6. `spriteInset_shrinksUnderChrome_noPipMove` — PASS

---

## E) Source (cases 1–5)

**BodyArt mapping (`BodyArt.kt`):**

- `PLAYER_DRAWABLE` / `hallwayPlayerDrawableName()` = `portrait_you` (L12–13, L44)
- `enemyPortraitDrawableName`: only `EnemyKind.ASH_WARDEN` → `portrait_ash_warden`; else `null` (L35–38)
- `hallwayEnemyUsesPlaceholder()` = true (L46)
- `SPRITE_INSET_FRACTION` = 0.08f; slots 90 / 160 / 120 dp (L17–27)
- Explicit: no wardrobe / poses / hit-flash (L7)

**UI:**

- `HeroShowcase` always paints `R.drawable.portrait_you`, circular clip, inset shrink (HeroShowcase.kt:56–81)
- `EnemySilhouette(kind)`: Ash-Warden Image; else Canvas `drawCircle` — boss uses `Ember` (orange), trash uses `Steel` (circle) (HeroShowcase.kt:90–143)
- `CombatScreen`: `HeroShowcase` + `EnemySilhouette(state.enemy.kind, …)`; `WakeStageOverlay` drawn **after** portrait Row in same `Box` → z-order above sprites (CombatScreen.kt:196–229); `HpBar` / `StatusPipRow` under portraits in Column — commit does **not** move pip/HP layout (CombatScreen diff = EnemySilhouette signature only)
- `Enemy.kt`: F2 `ASH_WARDEN.asset = "portrait_ash_warden"`; F1 `DRAGON` unchanged stub path
- Frozen: WakeArt / speed / save / pips / Brace / path sources **not** in this commit; regression suites green

---

## Cases 1–5

| # | Claim | Verdict |
|---|-------|---------|
| 1 | Hallway: You = soldier portrait, enemy = old circle placeholder | **PASS** — `hallway_youIsSoldier_enemyStaysPlaceholder`; trash → Steel `drawCircle` |
| 2 | Floor 2 boss: You + Ash-Warden sprite | **PASS** — `HeroShowcase` + `ashWarden_f2Boss_resolvesBodyStill` / `portrait_ash_warden` |
| 3 | Floor 1 Warden: You + OLD orange placeholder (no new F1 body) | **PASS** — `f1SealWarden_dragon_staysPlaceholder`; Ember Canvas boss; no F1 drawable added |
| 4 | Wake crescent still reads over both portraits (frames/pips/HP/Wake above sprite; clip → shrink sprite) | **PASS** — `WakeStageOverlay` after Row in Box; `SPRITE_INSET_FRACTION` + `spriteInset_shrinksUnderChrome_noPipMove` |
| 5 | Pips/HP on top; no layout shift; frozen Wake/Spark/2x/save/path/math green | **PASS** — CombatScreen chrome untouched; WakeArtV0117 4/4 + frozen suites + full 101/101 |

---

## Docs

- Spec: `docs/bodies-v0118.md`
- This gate: `docs/qa-gate-v0118-bodies-2026-09-23.md`

---

## Overall

**Gate PASS** — tip, APK, body assets/bindings, BodyArtV0118 6/6, WakeArtV0117 4/4 no regress, frozen suites, and full **101/101** all green; cases 1–5 PASS.
