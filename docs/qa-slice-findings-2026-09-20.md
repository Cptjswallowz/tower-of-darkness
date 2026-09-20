# Tower of Darkness — QA slice findings (soft-locks & stalls)

**Date:** 2026-09-20 (America/New_York)  
**Reporter:** QA (code review + unit tests; no device install)  
**Audience:** Chief of Staff-ToD  
**Checklist:** `docs/qa-slice-plan.md`  
**Screen lock:** `docs/slice-screens.md`

---

## 1. Build under test

| Item | Value |
|------|--------|
| APK path | `/workspace/tower-of-darkness/tower-of-darkness-debug.apk` |
| APK size | **17 559 468 bytes** (~16.7 MiB / ~17 MB) |
| `versionName` | `0.1.0-slice` (`app/build.gradle.kts`) |
| `versionCode` | `1` |
| Method | Source review of Compose Kotlin + `./gradlew testDebugUnitTest` |
| Device/emulator playtest | **Not performed** (no APK install on this box) |

**Unit tests:** `testDebugUnitTest` — **BUILD SUCCESSFUL**  
- `CombatEngineTest.bossHasConfiguredHp` — PASS  
- `CombatEngineTest.fightTerminates` — PASS  
- Suite: 2 tests, 0 failures, 0 errors (`app/build/test-results/testDebugUnitTest/`)

**Win rate / Monte Carlo:** **Not run.** Do not treat ~84% or ~3.6 rounds as measured.

---

## 2. Method + limitations

1. Walked every checklist ID (T/L/R/C/S/E/H/V) against source: `GameController.kt`, `NavState.kt`, screens, `MetaStore.kt`, `Balance.kt`, `TowerPath.kt`, `CombatEngine.kt`, `Card.kt`, related domain.
2. Grepped skip/tutorial/confirm/loadout/flee/unlock/save/rumor/enabled.
3. Ran JVM unit tests only (no instrumented / UI tests found).
4. **Limitation:** Soft-lock conclusions are **static + unit-test evidence**. Timing, Compose recomposition, and force-stop behavior need a device/emulator pass. Results marked **BLOCKED** where mid-run save is absent by design.

Severity tags match `qa-slice-plan.md`: P0 soft-lock / P0 stall / P1 leak / P1 smoke.

---

## 3. Checklist results

| ID | Result | Evidence | Severity if fail |
|----|--------|----------|------------------|
| **T-1** | **PASS** | `TutorialScreen.kt` L63–68: Skip button only if `gc.skipAllowed()`. `GameController.skipAllowed()` L188: `tutorialStep >= 2 && loadout.size in 5..6`. At step 0–1 Skip is absent (hint text only). | — |
| **T-2** | **PASS** | After rumor Next → `tutorialStep=1` (`tutorialNext` L143–145). Still `step < 2` → Skip blocked. Loadout not set until Confirm. | — |
| **T-3** | **PASS** | Tutorial is linear (steps 0→1→2→3); no order swap UI. Completing loadout requires having already left rumor+path beats. | — |
| **T-4** | **PARTIAL** | Skip after Confirm: `skipAllowed()` true; `skipTutorial()` L157–171 sets `tutorialSeen`, generates path, `nav=Path`. **Deviation:** if loadout already set, Skip **keeps player loadout** (`if (loadout.isEmpty())` default only L162–164). Spec/button copy say **default 5-card**; may keep 6-card custom. Flag set: `meta.setTutorialSeen(true)`. Not a soft-lock. | P1 UX vs design lock |
| **T-5** | **PASS** | Step ≥3: “Enter the Tower” → `completeTutorial()` L174–185 → Path + tutorial flag. Continue CTA present (`TutorialScreen.kt` L57–59). | — |
| **T-6** | **PASS** | `climb()` L113–119: if `tutorialSeen` → `startNewRun()` (Loadout/Path), not Tutorial. Flag from DataStore (`MetaStore` L21, L28–29). | — |
| **L-1** | **PASS** | `LoadoutScreen.kt` L51, L83: `ok = count in LOADOUT_MIN..MAX` (5–6); Confirm `enabled = ok`. | — |
| **L-2** | **PASS** | Same; Confirm enabled at exactly 5. `confirmLoadout` L195–196 rejects out-of-range. | — |
| **L-3** | **PASS** | Same for 6. | — |
| **L-4** | **PASS** | L73: `else if (selected.size < LOADOUT_MAX) selected.add` — cannot select 7th. | — |
| **L-5** | **PASS** | Deselect updates `count`; `ok` becomes false → Confirm disables. | — |
| **L-6** | **PASS** | `enterNode` L236–237: `loadoutLocked = true` on first non-START resolve. `openLoadout` L191–192: only if `!loadoutLocked`. Path UI hides Edit when locked (`PathScreen.kt` L53–55). | — |
| **L-7** | **PASS** | `selectPathNode` sets `pendingNodeId` then Loadout (L220–223). `confirmLoadout` L204–206: `enterNode(pending)` if pending set. | — |
| **R-1** | **PASS** | Fogged chip: label `"?"`, subtitle = `node.rumor` truncate (`PathScreen.kt` L91–118). Rumors from `RumorPools` are flavor-only (`TowerPath.kt` L58–98) — no HP/dmg/prices. | — |
| **R-2** | **PASS** | Combat pool strings e.g. “Steel answers steel…” — no numeric bands. | — |
| **R-3** | **PASS** | Shop/Rest/Treasure pools — no exact remnant prices or heal amounts. | — |
| **R-4** | **PASS** | `choices()` = outs from `currentId` not cleared (`TowerPath.kt` L28–30). Chip `clickable(enabled = selectable)` L108. `moveTo` reveals adjacent; fogged non-adjacent stay non-selectable. | — |
| **R-5** | **PASS** | Generator always adds Boss after merge (`TowerPath.kt` L141–146). Boss uses boss rumor pool; combat shows nameplate without mid-fog stat dump on Path. | — |
| **C-1** | **PASS*** | Win non-boss: `onCombatEnd` L286–294 → `nav = Path`, `nodesCleared++`. *Note:* `PathNode.cleared` for the combat node is set on **next** `moveTo`, not immediately — counter advances; chip ✓ delayed until leave. Not a stall. | — |
| **C-2** | **PASS** | Boss win → `finishRun(won=true)` → `NavState.RunSummary` L290–291, L418–424. | — |
| **C-3** | **PASS** | Loss → `finishRun(won=false)` L295–297. Engine sets `finished` when `playerHp <= 0` (`CombatEngine.kt` L86–91). | — |
| **C-4** | **PASS** | Flee grayed: `fleeGrayed() = true` L301; UI `OutlinedButton(..., enabled = false)` “Flee (locked)” (`CombatScreen.kt` L113). Cannot press; no dead destination. | — |
| **C-5** | **PASS** | Post-win returns Path; `selectPathNode` / `enterNode` reusable. No remount gate in controller. (Device remount not verified.) | — |
| **C-6** | **PASS** | Auto loop `combatJob` L259–271: `engine.step` on delay; no mid-round taps. Unit test `fightTerminates` asserts finish &lt; 40 steps. | — |
| **S-1** | **PASS** | `buyOffer` L332–345: spend if `runWallet >= price`; Leave `leaveShop()` always (`ShopScreen.kt` L41–43). | — |
| **S-2** | **PASS** | Buys `enabled = !sold && runWallet >= price` L31. Leave unconditional. **Broke path OK.** | — |
| **S-3** | **PASS** | Exact price: subtract `offer.price` after `>=` check; no negative. | — |
| **S-4** | **PASS** | After spend, remaining gated by wallet; Leave still present. | — |
| **S-5** | **PASS** | `openShop` always builds 4 offers L315–328; even if empty list, Leave button still rendered. | — |
| **E-1** | **PARTIAL** | `restHeal()` always callable; at full HP `coerceAtMost(maxHp)` no-ops then exits Path (`GameController.kt` L358–362). Heal **not disabled** and **no clear “already full” copy** (`RestScreen.kt` L35–37). Soft-lock avoided (Heal exits). Spec preferred disable/clear copy. | P1 UX |
| **E-2** | **PASS** | Damaged: `+ REST_HEAL_AMOUNT` (12) then Path. | — |
| **E-3** | **PASS** | Scout → `scoutAdjacent` type-only (`withReveal(..., typeOnly=true)` L372–379) then Path. | — |
| **E-4** | **PARTIAL** | Scout **not disabled** when no fogged adjacent; `scoutAdjacent` early-returns then still `nodesCleared++` / Path L365–369. Soft-lock avoided; design asked Scout disabled. **No dedicated Leave** (design lock “Leave → Path”); Heal/Scout act as exits. | P1 UX / design gap |
| **E-5** | **PASS** | Both Heal and Scout always exit → Path; cannot gray both with no Leave. No P0 soft-lock. | — |
| **H-1** | **PASS** | Unlocks ≥15; Climb always (`MetaHubScreen.kt` L95). Shadow Latch / meta_hp at 15 when bank ≥15. | — |
| **H-2** | **PASS** | Buttons show cost + deficit; `enabled = can`; Climb/Menu still work L95–97. | — |
| **H-3** | **PASS** | Purchase via `MetaStore.spendRemnants` + unlock; Climb remains. Owned rows show owned text. | — |
| **H-4** | **PASS** | Hub with 0 still lists unlock CTAs grayed + Climb. No crash path in code. | — |
| **V-1** | **PASS** | Default `nav = MainMenu` (`GameController.kt` L47). Cold start → Menu when no mid-run save (none implemented). | — |
| **V-2** | **BLOCKED (Meta)** | No mid-run path/graph persistence in `MetaStore` (only tutorial, bank, unlocks, meta HP). | — |
| **V-3** | **BLOCKED (Meta)** | No combat save/resume. | — |
| **V-4** | **BLOCKED (Meta)** | No Shop/Rest mid-run save. | — |
| **V-5** | **BLOCKED (Meta)*** | No mid-run blob → no ghost resume after Summary. *Cold start still Menu (V-1). Residual run fields are in-memory ViewModel only. | — |
| **V-6** | **PASS** | `tutorial_seen` DataStore key; `climb()` respects `tutorialSeen`. | — |

\*Asterisk = minor design timing/UX note, not P0.

---

## 4. Soft-lock / stall summary (top issues)

| Rank | Issue | IDs | Severity | Notes |
|------|-------|-----|----------|-------|
| 1 | Mid-run save/resume not wired | V-2…V-5 | **BLOCKED** | Meta schema sketch exists; only meta prefs persist. Force-stop mid-climb loses run (expected until Meta ships). |
| 2 | Rest: no Leave; Heal/Scout never gray | E-1, E-4 | **P1** | No soft-lock (both exits work). Spec wants Leave + disable Heal-at-full / Scout-when-none. |
| 3 | Skip after Confirm may keep custom loadout | T-4 | **P1** | Spec: Skip → default first-5. Code keeps confirmed loadout if non-empty. |
| 4 | Combat node `cleared` flag delayed until next move | C-1 | note | Progress counter OK; Path ✓ may lag one transition. |
| 5 | Latent empty-loadout combat hang | (gate-mitigated) | note | `CombatEngine.step` with empty cards never sets `finished` → `combatJob` could spin. Normal path requires 5–6 cards before enter. |

**Focus gates (priority queue) — code verdict:**

| Focus | Verdict |
|-------|---------|
| Tutorial Skip only after loadout + rumor | **PASS** (`skipAllowed`: step≥2 **and** loadout size 5–6) |
| Loadout Confirm only at 5–6 | **PASS** |
| Shop broke: Leave always works | **PASS** |
| Hub useful spend ≥15 | **PASS** (cheapest 15) |
| Flee grayed | **PASS** |

**No P0 soft-lock found in reviewed resolve paths** (Shop Leave, combat end, Rest dual exits, Hub Climb, loadout Confirm range).

---

## 5. Hub price findings (actual vs expected **15 / 15 / 20 / 20**)

Expected mid-patch ladder (`meta-economy-v0.md`): Shadow Latch **15**, meta_hp_2 **15**, scout_charge **20**, Relic Shard **20**.

| Unlock | Expected | Actual in code | Source |
|--------|----------|----------------|--------|
| Shadow Latch | 15 | **15** | `Card.kt` L59 `unlockCost = 15` |
| meta_hp_2 | 15 | **15** | `GameController.hubUnlockMetaHp` L440; UI L63–71 |
| scout_charge | 20 | **20** | `hubUnlockScoutCharge` L450; UI L79–87 |
| Relic Shard | 20 | **20** | `Card.kt` L63 `unlockCost = 20` |
| Floor constant | ≥15 | **15** | `Balance.CHEAPEST_CARD_UNLOCK = 15` |

**Result: MATCH mid-patch 15/15/20/20. No old (pre-patch) cheaper prices found.**

UI list order is cards-by-cost then perks (15, 20, 15, 20 visually if both rares unowned + both perks), not strictly ladder row order 1–4 — prices themselves are correct.

---

## 6. Proposed Monte Carlo sample size (proposal only — **not run**)

Target proportion ~0.84 (Architect target, **unmeasured**).

| Goal | Approx. n | Rationale |
|------|-----------|-----------|
| ±3 pp @ ~95% CI | **~600** runs | \(n \approx 1.96^2 p(1-p)/E^2\) with \(p=0.84\), \(E=0.03\) |
| ±2 pp @ ~95% CI | **~1300** runs | Same formula, \(E=0.02\) |
| Slice first pass (QA recommendation) | **n = 1000** | Balance of CI width (~±2.3 pp) vs sim cost; default 5-card loadout only |

**Hold:** Do not run until CoS releases sim queue. Report measured win rate only from that queue.

---

## 7. Open questions for CoS / Engineer

1. **T-4:** Should Skip **always** force `defaultLoadoutIds` (first 5), even after Confirm, or is “keep confirmed loadout + skip remaining beats” acceptable?
2. **Rest Leave:** Add explicit Leave, or keep “pick Heal or Scout once → auto Path”? Align `slice-screens.md` if auto-exit is intentional.
3. **E-1 / E-4:** Disable Heal at full HP and Scout when no fogged adjacent (with copy), or keep always-enabled exit semantics?
4. **V-\*:** When does Meta ship mid-run schema? Until then treat V-2…V-5 as blocked; confirm cold-start Menu-only is shippable for slice.
5. **Loadout pool size:** Starter unlocked pool is **9** cards (`unlockCost == 0`); design cites **10–12**. Is 9 intentional until rares unlock?
6. **C-1 cleared flag:** Should combat (and shop/rest) nodes set `cleared=true` on resolve exit, not only on next `moveTo`?
7. **Dead API:** `canSkipTutorial()` vs UI `skipAllowed()` — consolidate to one gate to avoid future drift.
8. **Hub display order:** Sort unlocks to ladder order 15/15/20/20 for clearer “useful spend” CTA?

---

## Sign-off

- Every checklist ID addressed: **yes** (PASS / PARTIAL / BLOCKED as above).  
- Hub prices called out: **15/15/20/20 MATCH**.  
- No invented win rates.  
- No messaging to external parties from this QA pass.  
- Device playtest still recommended before calling the slice soft-lock–clean on hardware.
