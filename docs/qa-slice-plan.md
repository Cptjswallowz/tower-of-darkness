# Tower of Darkness — QA slice plan (soft-locks & stalls)

**Owner:** QA & Playtest → report only to Chief of Staff-ToD  
**Source of truth for screens:** `docs/slice-screens.md` (Compose vertical slice)  
**Evidence scope:** Compose APK and this repo (`/workspace/tower-of-darkness`) only.  
**Balance targets (Architect, unmeasured):** ~84% win rate, ~3.6 round fights — do not cite as measured  
**Status:** Checklist ready. Execute on first buildable Compose APK (Android Engineer).  
**Date:** 2026-09-20

---

## How to use this doc

- **Pass:** Every row’s pass criteria met with no soft-lock / stall / dead-end.  
- **Fail:** Any path where the player cannot progress, Confirm stays forever disabled incorrectly, Skip appears too early, rumor leaks exact stats, or Hub has no useful spend when remnants allow one.  
- **Sample:** Manual playtest on device/emulator for each row. Log build id, OS, and steps to reproduce. Numbers only from runs/logs — never guess.  
- **Out of scope until CoS releases sim queue:** win-rate Monte Carlo, round-length distributions.

### Severity tags

| Tag | Meaning |
|-----|---------|
| **P0 soft-lock** | No legal way forward without force-stop / clear data |
| **P0 stall** | Progress possible only via obscure/back-nav or wait; feels stuck |
| **P1 leak** | Info or UX violates slice design (rumor stats, wrong gating) |
| **P1 smoke** | Save/resume / afford edge; broken but rare |

---

## 1. Tutorial skip timing

**Design lock:** Skip only after loadout step **and** rumor beat completed. Skip → Path with default 5-card loadout (first 5 by `cards-v0.md` table order). Completing or skipping sets tutorial-done local flag.

| ID | Case | Steps | Pass | Fail / stall to flag |
|----|------|-------|------|----------------------|
| T-1 | Skip hidden early | Fresh install → Climb → Tutorial; stop before rumor **and** before loadout | Skip control absent or disabled | Skip enabled → Path (**P0**) |
| T-2 | Skip after rumor only | Complete rumor beat; stop before loadout | Skip still blocked | Skip works (**P0**) |
| T-3 | Skip after loadout only | Complete loadout; skip rumor beat if UI allows order swap | Skip still blocked until both done | Skip works without both (**P0**) |
| T-4 | Skip after both | Finish loadout + rumor → Skip | Lands on Path; loadout = first 5 by table order; tutorial marked done | Soft-lock on Tutorial; wrong loadout count; flag not set (**P0**) |
| T-5 | Complete (no Skip) | Finish all tutorial beats without Skip | Path; tutorial done; loadout = player’s 5–6 | Stuck on last beat with no Continue (**P0**) |
| T-6 | Second Climb | After tutorial done → Menu → Climb | No Tutorial; Path (or resume) | Tutorial repeats forever (**P1 stall**) |

---

## 2. Loadout confirm gating

**Design lock:** Pool 10–12 starters. Select **5 or 6**. Confirm disabled until count in range. First leave Path into resolve locks loadout for the climb.

| ID | Case | Steps | Pass | Fail / stall to flag |
|----|------|-------|------|----------------------|
| L-1 | 0–4 selected | Open Loadout; select 0, then 4 | Confirm disabled | Confirm enabled (**P0** bad lock-in) |
| L-2 | Exactly 5 | Select 5 | Confirm enabled → Path (or pending node) | Confirm stays disabled (**P0 soft-lock**) |
| L-3 | Exactly 6 | Select 6 | Confirm enabled | Confirm disabled (**P0**) |
| L-4 | 7+ | Attempt 7th select | Blocked or auto-deselect; Confirm only valid at 5–6 | Can confirm 7+ (**P1**) |
| L-5 | Deselect below 5 | From 5, deselect to 4 | Confirm disables again | Confirm stays on (**P1**) |
| L-6 | Lock after first resolve | Enter any node once → return Path → open Loadout | Edit locked until Run Summary | Can change mid-climb contrary to lock (**P1**; note open fork on Rest edit) |
| L-7 | From node tap | Path → node while unlocked → Loadout → Confirm | Enters that node (or returns Path then node per nav table) | Dead-end: Confirm returns nowhere (**P0**) |

---

## 3. Path node rumor (no exact stats)

**Design lock:** Unrevealed nodes show rumor text (`rumors.md`), **not** exact rewards or enemy stats. Revealed adjacent selectable.

| ID | Case | Steps | Pass | Fail / stall to flag |
|----|------|-------|------|----------------------|
| R-1 | Fogged node UI | Inspect unrevealed mid nodes | Rumor / fog copy only; no HP, dmg bands, remnant amounts, card IDs as rewards | Exact enemy HP/dmg or reward numbers shown (**P1 leak**) |
| R-2 | Combat rumor | Fogged Combat node | Flavor threat only; no “20 HP” / band numbers | Stats leaked (**P1 leak**) |
| R-3 | Shop / Rest / Treasure rumor | Fogged economy nodes | No exact remnant prices or heal amounts in fog | Exact economy leaked (**P1 leak**) |
| R-4 | Reveal consistency | Clear prior node; adjacent unlocks | Only legal adjacent selectable; fogged stay non-enterable | Can enter fogged or no legal next node (**P0 soft-lock**) |
| R-5 | Boss presentation | Reach boss approach | Boss readable; still no unintended mid-fog stat dump | Soft-lock before Boss (**P0**) |

---

## 4. Combat end → next node

**Design lock:** Win (non-boss) → Path with node cleared. Boss win / lose / Flee → Run Summary. No mid-round input required.

| ID | Case | Steps | Pass | Fail / stall to flag |
|----|------|-------|------|----------------------|
| C-1 | Normal win | Win a Combat node | Auto or clear CTA → Path; node marked cleared; adjacent progress | Stuck on Combat after win (**P0 soft-lock**) |
| C-2 | Boss win | Win Boss | → Run Summary (victory) | Stuck on Boss / Path with nowhere to go (**P0**) |
| C-3 | Loss | Lose HP to 0 | → Run Summary (defeat) | Combat hangs / black screen (**P0**) |
| C-4 | Flee stub | If Flee enabled: Flee | → Run Summary (loss). If grayed: cannot press | Flee enabled but no destination (**P0**); escalate open fork if unclear |
| C-5 | Multi-node chain | Win combat → Path → next node | Flow repeats without remount stall | Must force-stop between nodes (**P0 stall**) |
| C-6 | Dice auto | During round | No required taps mid-round; fight completes | Waiting forever for input that never comes (**P0 stall**) |

---

## 5. Shop afford / broke paths

**Design lock:** Remnants currency; 3–5 offers at **5–15** remnants. Leave → Path always.

| ID | Case | Steps | Pass | Fail / stall to flag |
|----|------|-------|------|----------------------|
| S-1 | Afford one | Enter Shop with remnants ≥ one price | Buy succeeds; remnants decrement; Leave → Path | Buy no-ops with no feedback (**P1**); cannot Leave (**P0**) |
| S-2 | Broke vs all | Enter with 0 (or < cheapest) | All buys disabled/failed clearly; **Leave still works** | Must buy to exit (**P0 soft-lock**) |
| S-3 | Exact price | Remnants == one offer price | Buy that one; others still gated | Crash or negative remnants (**P0**) |
| S-4 | After spend broke | Buy down to < all remaining | Remaining buys gated; Leave works | Soft-lock in Shop (**P0**) |
| S-5 | Empty / stub stock | If stock stubs fail to load | Leave → Path still available | Blank Shop with no exit (**P0**) |

---

## 6. Rest options

**Design lock:** Choose **Heal** (partial toward HP cap 30) or **Scout** (reveal one adjacent fogged node type). Leave → Path.

| ID | Case | Steps | Pass | Fail / stall to flag |
|----|------|-------|------|----------------------|
| E-1 | Heal at full | HP already at cap | Heal disabled or no-op with clear copy; Leave works | Soft-lock requiring Heal (**P0**) |
| E-2 | Heal damaged | HP < 30 | Heal increases HP toward 30; then Leave → Path | Heal does nothing and blocks Leave (**P0**) |
| E-3 | Scout available | ≥1 adjacent fogged | Scout reveals type (not full exact stats); Leave → Path | Scout required with 0 fogged and no cancel (**P0**) |
| E-4 | Scout none left | No fogged adjacent | Scout disabled; Heal and/or Leave still viable | Must Scout to exit (**P0**) |
| E-5 | One pick then leave | Pick Heal **or** Scout once per design | Cannot soft-lock by refusing both if Leave exists; if must pick one, both always valid or Cancel | No Leave and both options gray (**P0**) |

---

## 7. Hub always has a useful spend

**Design lock:** Always offer a useful spend: unlock stub **≥15** remnants (cheapest target **15**). Slice: one unlock row even if purchase only sets a flag. CTA Climb → Path / Menu.

| ID | Case | Steps | Pass | Fail / stall to flag |
|----|------|-------|------|----------------------|
| H-1 | Can afford unlock | Bank ≥15 after a run | Unlock row visible, purchasable, useful (flag/content); Climb available | Hub with only dead buttons (**P0 stall**) |
| H-2 | Cannot afford | Bank <15 | Unlock visible but gated; copy shows cost; Climb / Menu still work | Must purchase to leave Hub (**P0**) |
| H-3 | After purchase | Buy unlock once | State persists; no mandatory second spend; Climb works | Soft-lock empty Hub (**P0**) |
| H-4 | Zero remnants first Hub | Open Hub with 0 | Still shows unlock goal; no crash; can Climb | Blank Hub / crash (**P0**) |

---

## 8. Save / resume smoke

**Design lock:** Meta & Ops owns save/resume. Cold start → Menu unless mid-run save. Climb + save → resume at saved screen.

| ID | Case | Steps | Pass | Fail / stall to flag |
|----|------|-------|------|----------------------|
| V-1 | No save cold start | Kill app; relaunch; no mid-run save | Menu | Crash loop (**P0**) |
| V-2 | Mid-Path resume | On Path with save → force-stop → Climb/continue | Resume Path; graph + fog + cleared nodes consistent | Lost run or illegal node (**P0**) |
| V-3 | Mid-Combat resume | Force-stop in Combat → resume | Combat or safe Path recovery per Meta spec; not soft-locked | Stuck loading Combat (**P0 stall**) |
| V-4 | Mid-Shop / Rest | Force-stop in Shop or Rest → resume | That screen or Path; Leave still works | Soft-lock in overlay (**P0**) |
| V-5 | Post–Run Summary | Finish run → Summary → kill before Hub | No phantom mid-run; Menu or Hub clean | Ghost save forces broken resume (**P0**) |
| V-6 | Tutorial flag | Complete/skip tutorial → kill → Climb | Tutorial stays done | Tutorial every launch (**P1 stall**) |

*If save/resume not wired yet:* mark V-* as **blocked (Meta)**; do not invent persistence. Still verify force-stop does not brick next cold start (V-1).

---

## Execution order (first APK)

1. T-* (tutorial gating) + L-* (loadout Confirm) — highest soft-lock risk on first session.  
2. R-* + C-* — path graph and combat exit.  
3. S-* + E-* — afford/broke and Rest dead-ends.  
4. H-* — meta spend always meaningful.  
5. V-* — smoke only; expand when Meta & Ops confirms schema.

Report format to CoS: build id, checklist IDs pass/fail, repro steps, severity. No win-rate claims until sim queue is assigned.

---

## Held until Compose APK + CoS sim queue

- Monte Carlo / measured win rate vs ~84%  
- Measured mean round length vs ~3.6  
- Shop/rest/meta economy sims beyond manual stall cases above  
