# Pre-boss REST + Free Scout — v0.1.10-bossrest (WO Elliott)

Status: **implemented** in tree (WIP; no tag/release yet) (supersedes withdrawn CARE-on-every-S→B / `routecare-v0110`).  
Applies to **Floor 1 and Floor 2**.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.  
No playtest/sim numbers invented here.

---

## 1. Pre-boss REST (path generator)

After the existing combat lock (every Start→Boss path already has **≥1 COMBAT**):

> On every S→B path, the **last non-boss node must be `REST`**.  
> Convert that node’s type to `REST` if it is not already.

Also keep:

- **≥1 COMBAT** somewhere **before** that pre-boss REST (existing v0.1.7 lock).
- **Event ≤1** per floor (unchanged).
- Do **not** require CARE (REST|SHOP) on every earlier segment — that CARE guarantee is **withdrawn**.

Order of floor rules:

1. Generate floor graph (existing).
2. Enforce **≥1 COMBAT** on every S→B (v0.1.7).
3. Enforce **last non-boss node = REST** on every S→B (this WO) via convert-if-needed.
4. Keep **event ≤1**.

No new nodes. Prefer converting the existing last non-boss node rather than inserting.

---

## 2. Rest node behavior (this WO)

| Action | Behavior |
|--------|----------|
| **Heal** | Set player HP → **MAX HP** (full heal). |
| **Deep Breath** (`rest_heal_plus`) | Does **not** overheal past MAX. If Heal already fills to MAX, Deep Breath adds **0** HP this pass (no over-max). |
| **Leave** | Exit Rest → Path; works with no remnant cost. |
| Cost | Rest actions cost **0** remnants. |

Rest may still offer **Scout** (adjacent type-only) as today; that is separate from Free Scout below.

---

## 3. Free Scout (Keen Eye)

Meta perk **Keen Eye** / id `scout_charge`: grants **Free Scout charges** each climb (Hub unlock; Meta & Ops owns cost — currently **20** remnants in ladder).

Rules:

1. Player taps a **fogged (`?`) node** while `charges ≥ 1`.
2. Reveal that node’s **real type** (icon + name, same presentation as a row-1 / revealed type).
3. Spend **1** charge (`charges − 1`).
4. Do **not** enter the node; node **type unchanged**; stay on Path.
5. If `charges = 0`, tap is a **no-op** (no reveal, no spend).

### Separate: Clear Fog / rumor re-roll

Any **Clear Fog** (or similar) that only **re-rolls rumor copy** on a still-fogged node is **not** Free Scout. Do **not** spend a Scout charge on rumor re-roll. Free Scout spends a charge only on the type-reveal tap above.

Scout (Rest or Free) still reveals **type only** — no second rumor line (`rumors.md` CoS lock).

---

## 4. Frozen (do not change this pass)

- Wake / CHAIN thresholds / SPARK
- Trash & boss **counters**
- Boss HP: Seal-Warden **28** / Ash-Warden **32**
- F2 trash HP **24**
- Swap rules
- Shop prices / stock
- F2 persist (`floor2-v019.md`)

---

## 5. Engineer checklist

- [x] F1+F2: every S→B last non-boss node is **REST** (convert if needed)
- [x] Every S→B still has **≥1 COMBAT** before that REST; event ≤1
- [x] Rest Heal → MAX HP; Deep Breath no overheal; Leave free; no remnant cost
- [x] Keen Eye: tap fogged node with charges≥1 → reveal type (icon+name), charge−1, no enter
- [x] charges=0 → no-op; Clear Fog rumor re-roll does not spend Scout
- [x] Leave frozen systems untouched

---

## Open (not in this WO)

- Exact Free Scout charge count if Hub grants more than +1/climb later — Meta ladder owns; design assumes Keen Eye grants the climb’s charge pool as implemented.

---

**Related:** v0.1.24-rumorcharge — two wallets + tap priority + persist. See `rumorcharge-v0124.md`.
