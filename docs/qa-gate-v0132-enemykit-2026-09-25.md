# QA Gate — v0.1.32-enemykit (2026-09-25)

**Project:** Tower of Darkness (Compose Android only)  
**WO:** v0.1.32-enemykit  
**Gate result:** **PASS**  
**Measured (EDT):** 2026-09-25 ~13:44–13:46 EDT

**Tip (HEAD / origin/main):** `5b11e7f0a17b003d07f18a7b73916408caaccb29`  
**Claimed tip:** `5b11e7f0a17b003d07f18a7b73916408caaccb29` → **MATCH**

---

## A) Git

| Check | Result |
|-------|--------|
| HEAD | `5b11e7f0a17b003d07f18a7b73916408caaccb29` |
| origin/main | `5b11e7f0a17b003d07f18a7b73916408caaccb29` |
| Match claimed tip | **YES** |
| Subject | `v0.1.32-enemykit: live kits + Soften/Nip + enemy tiles` |
| Author / date | Android Engineer \<android-engineer@tod.local\> — 2026-09-25 13:43:46 -0400 |
| Status | `main` up to date with `origin/main`; working tree clean |

**`--stat`:** EnemyKit.kt (new), CombatEngine.kt (live kit resolve / Soften / Nip), StatusPips.kt, Glossary.kt, CombatScreen.kt (enemy tiles), EnemyKitV0132Test.kt (10), BraceSync/StatusPips/CombatSimV014 test updates, docs (`enemykit-v0132.md` + related locks). **16 files, +904/−244.**

**Not in tip (frozen sources):** PathGenerator / HubOffers / CombatSpeed / WakeArt / portrait_* / wake_* / glyph_* / FloorArt / PathScreen / floor+node drawables. Tip also touches CombatScreen (enemy kit UI — in-scope for this WO) and `docs/packs-v0126.md` (doc note only). No Hub/path-math/2x/portrait/FloorArt cores.

---

## B) APK

| Path | Size | mtime (EDT) | md5 |
|------|------|-------------|-----|
| `tower-of-darkness-debug.apk` (root) | 19014776 | 2026-09-25 13:43 EDT | `59dcaa25a4fce2d466b007bb4ae40135` |
| `app/build/outputs/apk/debug/app-debug.apk` | 19014776 | 2026-09-25 13:43 EDT | `59dcaa25a4fce2d466b007bb4ae40135` |

Claimed: 19014776 / `59dcaa25a4fce2d466b007bb4ae40135` → **MATCH** (root + build).

---

## C) Source / design notes (enemykit)

Design lock: `docs/enemykit-v0132.md`.

| Lock | Evidence |
|------|----------|
| Goblin kit w2/w2/w5 | `EnemyKits.weakGoblin`: Shiv2 dmg5, Nip2 dmg3 (NIP), Hit5 band 7–9 |
| Orc kit | `sturdyOrc`: Cleave2 dmg8, Hide2 Brace3, Hit5 band 8–10 |
| Seal-Warden / Ash-Warden | Seal Pulse2 dmg7 + Rust Guard Brace4; Coal Slam2 dmg9 + Cinder Hide Brace3; Hit5 band 6–9 each |
| Soften → next damaging only | CombatEngine `DAMAGE` subtracts `counterPenalty` then clears; `BRACE` leaves Soften; `NIP` ignores Soften |
| Nip log locked | Log append `(ignores Soften)`; Soften pip stays |
| Grey / cycle | `enemySpentIds`; reset when `spent.size >= kit.size` → `"Enemy cycle reset"` |
| Looks cosmetic | `EnemyKitRole.fromEnemy` by kind only; three goblin looks share id/weight lists |
| Glossary | `cleave` non-blank; Hide/Rust Guard/Cinder Hide = `BRACE_DEF`; Nip = `Small cut. Soften does not reduce this hit.` |
| Wake / player bar | Wake log exact `ASHBRAND — WAKE 4`; player 5-card exhaust among unspent |

**Spot-check (source):**

- `EnemyKit.kt` tables match design lock (weights 2+2+5 = 9 each role).
- `CombatEngine.resolveEnemy`: Soften on `DAMAGE` only; Nip full damage + log; Brace gains on enemy; spent/grey cycle shared machine.
- `Glossary.kt`: Nip Soften copy locked; Hide family reuses Brace def.

---

## D) Unit tests

Commands (measured this gate):

- `./gradlew :app:testDebugUnitTest --tests '*EnemyKitV0132*'` → **BUILD SUCCESSFUL**
- `./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest` → **BUILD SUCCESSFUL**

Counts from XML under `app/build/test-results/testDebugUnitTest/`:

| Suite | Result |
|-------|--------|
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
| **Full `:app:testDebugUnitTest`** | **206/206 PASS** (30 classes, 0 fail/err/skip) |

### EnemyKitV0132Test methods

1. `goblin_shivAndNipAppear_hitMostCommon` — PASS  
2. `orcHide_braceUnderOrc_playerHitEatsBrace` — PASS  
3. `softenThenCleave_minusOne_clearsSoften` — PASS  
4. `softenThenHide_softenPipStays` — PASS  
5. `nipLog_ignoresSoften_doesNotConsume` — PASS  
6. `tapCleave_glossaryDef` — PASS  
7. `greyTile_noRerollOfThatTile` — PASS  
8. `threeGoblinLooks_shareKit` — PASS  
9. `wakeAndPlayerBar_unchanged` — PASS  
10. `kits_matchLockTable` — PASS  

**Measured (from passing assertions + source + MC println spot):**

- Weights: shiv**2** / nip**2** / hit**5**; damages Shiv5 Nip3; Hit band 7–9
- Monte Carlo 900 seeds: `{shiv=223, nip=176, hit=501}` — Hit > Shiv and Hit > Nip; both specials > 0
- Hide → enemyBrace **3**; player Hostflint 5 eats Brace → Brace 0, enemy HP −2
- Soften then Cleave: log `Sturdy Orc — Cleave 7`; Soften cleared
- Soften then Hide: Soften stays (count 2); Brace 3 applied
- Nip: log contains `Weak Goblin — Nip 3` + `ignores Soften`; Soften stays 2; player HP −3
- Glossary: `cleave` non-blank; hide/rust guard/cinder hide == brace; Nip Soften copy locked
- Grey: 3 distinct picks then `Enemy cycle reset`; spent size → 1 after reset
- Three goblin looks (KNIFE/BOTTLE/SPIKES): identical id/weight lists; role WEAK_GOBLIN
- Wake: `ASHBRAND — WAKE 4`; player bar fires 5 distinct cards before cycle

---

## E) Cases 1–8 vs `docs/enemykit-v0132.md`

**1) Goblin fight: Shiv or Nip can appear; Hit still most common**

- `goblin_shivAndNipAppear_hitMostCommon`: weights shiv2/nip2/hit5.
- MC900 measured counts: hit=**501**, shiv=**223**, nip=**176** — Hit > Shiv and Hit > Nip; both specials > 0.

**2) Orc Hide → Brace pip on orc; next player hit eats Brace first**

- `orcHide_braceUnderOrc_playerHitEatsBrace`: Hide → `enemyBrace=3`; StatusPips brace count 3; log `Sturdy Orc — Hide 3`.
- Hostflint 5 vs Brace 3 → Brace 0, enemy HP −2 (Brace before HP).

**3) Soften then Cleave: Cleave −1 and Soften clears; Soften then Hide: Soften stays**

- `softenThenCleave_minusOne_clearsSoften`: Cleave 8→**7**; Soften cleared (`counterPenalty=0`).
- `softenThenHide_softenPipStays`: Soften stays at 2; Brace 3 applied; both pips present.

**4) Nip log mentions ignores Soften**

- `nipLog_ignoresSoften_doesNotConsume`: log contains `ignores Soften` + `Weak Goblin — Nip 3`; Soften not consumed (stays 2); full Nip damage 3.

**5) Tap Cleave tile → glossary definition**

- `tapCleave_glossaryDef`: `Glossary.definition("cleave")` non-blank; Hide/Rust Guard/Cinder Hide reuse Brace; Nip contains `Soften does not reduce`.

**6) Grey tiles do not roll again**

- `greyTile_noRerollOfThatTile`: spent ids excluded until full cycle; 3 distinct then `Enemy cycle reset`; spent size 1 after reset.

**7) Three goblin looks share this kit**

- `threeGoblinLooks_shareKit`: KNIFE/BOTTLE/SPIKES identical id/weight lists; role WEAK_GOBLIN.

**8) Wake / player bar unchanged; Hub/path/2x/portraits/map tokens frozen**

- `wakeAndPlayerBar_unchanged`: Wake log exact `ASHBRAND — WAKE 4`; player 5-card exhaust.
- tip `--stat` excludes HubOffers / PathGenerator / CombatSpeed / WakeArt / portrait_* / FloorArt cores.
- Frozen suites green (see D): FloorArt 13, HubMore 8, HubKeep 7, Hub 9, Packs 12, WakeArtV0117 4, SkillGlyph 8, Portraits 7, Nobg 5, CombatSpeed 5, MidRunSave 8, RumorCharge 8, PathGate 1, PathGenerator 7; full **206/206**.

---

## Cases 1–8

| # | Claim | Verdict |
|---|-------|---------|
| 1 | Goblin Shiv/Nip appear; Hit most common | **PASS** — w2/w2/w5; MC900 hit=501 > shiv=223 > nip=176 |
| 2 | Orc Hide Brace 3; player hit eats Brace first | **PASS** — Brace 3 → Hostflint eats → Brace 0, HP −2 |
| 3 | Soften+Cleave 8→7 clears; Soften+Hide Soften stays | **PASS** — Cleave 7 + Soften clear; Hide Soften stays 2 |
| 4 | Nip log ignores Soften; Soften not consumed | **PASS** — log `(ignores Soften)`; Soften 2 stays; dmg 3 |
| 5 | Tap Cleave → glossary; Hide family = Brace; Nip copy | **PASS** — cleave non-blank; hide/rust/cinder = brace; Nip Soften locked |
| 6 | Grey tiles excluded until cycle reset | **PASS** — 3 distinct then `Enemy cycle reset` |
| 7 | Three goblin looks share kit | **PASS** — KNIFE/BOTTLE/SPIKES same id/weight |
| 8 | Wake/player bar unchanged; Hub/path/2x/portraits/tokens frozen | **PASS** — Wake exact; 5-card exhaust; tip excludes frozen cores; suites green |

---

## Docs

- Spec: `docs/enemykit-v0132.md`
- Prior gate format: `docs/qa-gate-v0131-floorart-2026-09-25.md`
- This gate: `docs/qa-gate-v0132-enemykit-2026-09-25.md`

---

## Overall

**Gate PASS** — tip `5b11e7f0a17b003d07f18a7b73916408caaccb29` (== origin/main == claimed); APK MATCH 19014776 / `59dcaa25a4fce2d466b007bb4ae40135` (root + build); EnemyKitV0132 **10/10**; full suite **206/206** (30 classes, 0 fail/err/skip); cases 1–8 PASS.
