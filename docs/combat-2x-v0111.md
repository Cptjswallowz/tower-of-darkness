# Combat speed 1x ↔ 2x — v0.1.11-2x (WO Elliott)

Status: **implemented** in app (toggle + hold scaling); tag/release not cut this pass.  
Applies to **combat only** (F1 trash, F1 boss, F2 trash, F2 boss).

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.  
No playtest/sim numbers invented here.

---

## Control

The existing **1x** combat control becomes a **2-state toggle**: `1x` ↔ `2x`.

| State | Holds |
|-------|--------|
| **1x** | Current holds. WO reference: common beat **~1.7 s**, Wake **~2.3 s**. Other beats stay at their existing 1x budgets (`combat-bar-v013.md` / `wake-v014.md`). |
| **2x** | **Every combat hold × 0.5.** Same order; nothing skipped. |

Label shows the **ACTIVE** rate (`1x` or `2x`), not the rate you would switch to.

**No 3x.** **No skip-animations** control. Toggle does not change damage, card weights, CHAIN/SPARK, Wake rules, or resolution order.

---

## Sync

At 2x, **dice, combat log, float text, and exhaust grey-out** stay **in sync** with the shortened holds. Do not leave 1x-timed VFX/SFX on a 2x clock (or the reverse).

---

## Mid-fight toggle

If the player flips 1x ↔ 2x **during** a fight, the new rate applies to the **NEXT beat**. The current beat finishes at the rate it started.

---

## Persist

Persist the active rate **for combat**. Run-scoped persist is OK if cheap (same climb / F1→F2). Do not invent a Hub setting this pass.

---

## Frozen (do not change this pass)

- Path generator / rest-before-boss (`bossrest-v0110.md`)
- F2 persist (`floor2-v019.md`)
- Wake / CHAIN / SPARK **rules** (presentation length scales with rate only)
- Counters and HP (Seal-Warden 28 / Ash-Warden 32 / F2 trash 24)
- Shop / swap
- Keen Eye Free Scout

---

## Engineer checklist

- [x] Existing 1x control is a 2-state toggle 1x ↔ 2x
- [x] Label shows **active** rate
- [x] 1x = current holds (common ~1.7 s, Wake ~2.3 s)
- [x] 2x = holds × 0.5; dice / log / float / grey-out in sync
- [x] Mid-fight toggle applies to **next** beat
- [x] Persist for combat (run OK if cheap)
- [x] No 3x; no skip-animations; no damage / weight / Wake **rule** changes
- [x] Leave frozen systems untouched
