# Route CARE — v0.1.10 design note (WO Elliott)

Status: **implemented** in PathGenerator (v0.1.10-routecare).  
Applies to **Floor 1 and Floor 2** path generation.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.  
No playtest/sim numbers invented here.

---

## Rule (after existing combat lock)

On every generated floor, **after** the v0.1.7 combat lock (every Start→Boss route already has ≥1 COMBAT):

> Every Start→Boss path must also include **≥1 CARE** node.  
> **CARE** = `REST` or `SHOP`.

Event cap remains **≤1** per floor (unchanged).

**Do not** add new nodes to the graph. Fix by **converting** an existing node’s type when needed.

---

## Enforcement when a route has zero CARE

For each Start→Boss path that still has **0 CARE** after combat lock:

1. Find the **last** node on that path that is:
   - not Boss
   - not COMBAT
2. Convert that node’s type → **`REST`**.
3. Prefer **REST over SHOP** when choosing the conversion target type (always convert to REST in this WO — do not convert to SHOP for the zero-CARE fix).

If multiple S→B paths lack CARE, apply the same rule per path (convert that path’s last eligible node). If one conversion fixes several paths that share the node, that is fine.

If somehow no eligible non-boss non-COMBAT node exists on a fightless-care route (should be rare given shops/rest/events/treasure): escalate to CoS — do not invent a new node or strip the boss.

---

## Order of floor rules

1. Generate floor graph (existing).
2. Enforce **≥1 COMBAT** on every S→B (v0.1.7).
3. Enforce **≥1 CARE** on every S→B (this WO) via convert-last-eligible → REST.
4. Keep **event ≤1**.

---

## Frozen (do not change this pass)

- Wake / CHAIN thresholds / SPARK
- Trash & boss **counters**
- Boss **HP** (Seal-Warden 28 / Ash-Warden 32)
- Swap rules
- Shop **prices**
- F2 persist rules (`floor2-v019.md`)

---

## Engineer checklist

- [x] Post-combat-lock: every S→B has ≥1 CARE (REST|SHOP)
- [x] If zero CARE: convert last non-boss non-COMBAT → **REST** (prefer REST over SHOP)
- [x] No new nodes; event cap 1; runs on F1 and F2
- [x] Leave frozen systems untouched
