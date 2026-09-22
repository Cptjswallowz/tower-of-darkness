# Floor 2 climb — v0.1.9 design note (WO Elliott)

Status: **implemented** in v0.1.9-floor2 (code + tests).  
**Frozen this pass:** Path generator rules (reuse 0.1.7), Wake, swap, shop — do not retune.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.  
No playtest/sim numbers invented here.

---

## Climb shape

Two floors only. **No Floor 3.**

```
… Floor 1 path → Seal-Warden (boss)
  → win beat: copy **"The stair turns."**
  → Floor 2 path (same run — **no Hub**)
  → … Floor 2 path → Ash-Warden (boss)
  → win: Run Summary **"Victory — The seal breaks."** → Hub
```

Loss on either floor → existing Run Summary (defeat) → Hub / Menu per current nav. No mid-climb Hub between F1 and F2.

---

## Persist across F1 → F2 (no reset)

Carry into Floor 2 **as-is**:

| State | Rule |
|-------|------|
| Player HP | Persist — **no heal-to-full** |
| Run remnants / wallet | Persist |
| Loadout (5 cards) | Persist — **no loadout unlock / re-edit** (stay locked) |
| Ashbrand (level, charge, XP if any) | Persist |
| Unlocks / meta flags already owned | Persist |

Do **not** re-open Loadout, Tutorial, or Hub between floors.

---

## Floor 1 (unchanged combat targets)

| Item | Lock |
|------|------|
| Trash HP | **20** |
| Trash counter | **5–8** (`balance-targets.md`) |
| Boss | **Seal-Warden**, HP **28**, counter **6–9** |
| Interstitial on Seal-Warden **win** | **"The stair turns."** → generate/show **Floor 2** path |

---

## Floor 2 path rules (= v0.1.7 routefight + v0.1.10 CARE)

Same generator constraints as Floor 1:

- Every Start→Boss route has **≥1 COMBAT** (v0.1.7)
- Every Start→Boss route has **≥1 CARE** (REST|SHOP) — see `routecare-v0110.md`
- Events on the floor: **≤1**
- Fog / rumor visibility: unchanged (`rumors.md`)

---

## Floor 2 combat

| Item | Lock |
|------|------|
| Trash HP | **24** (F1 stays 20) |
| Trash counter | **5–8** (same band; no separate retune) |
| Trash kind preference | Prefer **spider / troll** over goblin (when rolling trash) |
| Boss | **Ash-Warden** — **same kit** as Seal-Warden, HP **32**, counter **6–9** |
| Clear | Summary title/copy **"Victory — The seal breaks."** → **Hub** |

Ash-Warden = same combat kit/behavior as Seal-Warden; distinct nameplate / HP only unless Art delivers a new stub later.

---

## Explicit non-goals (frozen)

- Path algorithm changes beyond applying 0.1.7 rules on `floor = 2`
- Wake / CHAIN / SPARK retune
- Swap / shop / meta economy retune
- Floor 3, heal-on-stair, loadout unlock between floors

---

## Engineer checklist

- [x] Seal-Warden win → "The stair turns." → F2 path (no Hub)
- [x] Persist HP / rem / loadout / Ashbrand / unlocks; no heal-to-full; loadout stays locked
- [x] F2 path: event ≤1; every S→B ≥1 COMBAT
- [x] F2 trash HP 24; prefer spider/troll over goblin
- [x] Ash-Warden HP 32, counters 6–9, same kit
- [x] F2 clear → "Victory — The seal breaks." → Hub; no Floor 3
