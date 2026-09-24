# Mid-run save slot — full schema v0.1.12-save

Status: **Meta & Ops authoritative** for one-slot fields / storage.  
Flow locks: [`save-v0112.md`](save-v0112.md) (Architect). F1→F2 carry: [`floor2-v019.md`](floor2-v019.md).  
Supersedes legacy `run_v0` sketch in `meta-economy-v0.md` §4 for mid-run.

**Frozen this pass:** 2x timings, path gen, boss-rest, F2 persist *rules*, Wake, counters, shop, swap, Scout *mechanics* (persist fields only).

---

## 1. Slot + lifecycle

| Rule | Lock |
|------|------|
| Count | **One** mid-run slot (`midrun_v0112`) |
| Meta | Separate store (`meta_v0`) — bank, owned perks/cards, tutorial, meta HP — **never** wiped by mid-run clear |
| Clear | Run Summary → Hub (after bank); New climb **confirm wipe**; corrupt/schema mismatch |
| Storage | Local DataStore/file — Engineer pick; not ToW schemas |

```
legal write → UPSERT midrun_v0112
Summary→Hub bank → CLEAR midrun_v0112
New climb confirm → CLEAR midrun_v0112
corrupt            → CLEAR midrun_v0112 (meta kept)
```

---

## 2. Write moments (Architect lock)

| Write | When |
|-------|------|
| **Node resolve** | Leave Combat / Shop / Rest / Event / Treasure / Boss **after** resolve applied (HP, wallet, cleared, Ashbrand XP, etc.) → typically Path |
| **Stair Continue** | Seal-Warden win interstitial **"The stair turns."** → Continue → Floor 2 path shown |
| **Loadout lock** | Loadout Confirm that locks the climb bar |

### Never write

Mid-dice, mid-round, mid-Wake (FULL/SPARK), mid-1x/2x beat hold, uncommitted shop tap before wallet apply.

**No combat snapshot.** Do not persist `enemy_hp` / `round` / dice / Wake charge mid-fight (drops legacy `run_v0.combat`).

**F1 boss dual write (CoS lock):**
1. **Node resolve / combat Continue** after Seal-Warden win → write with `pending_stair_continue=true`, `resume=FloorBreak` (still floor 1 path cleared).
2. **Stair Continue** → write `floor=2`, new F2 `path`, clear pending, `resume=Path`.
Both writes are in the WO set.

---

## 3. Payload `midrun_v0112`

```
midrun_v0112 {
  schema: "v0.1.12"
  run_id: string
  rng_seed: long                 // REQUIRED (CoS) — fog/shop rebuild; do not cut

  // --- Floor / path (F1 + F2) ---
  floor: 1 | 2
  pending_stair_continue: bool   // true after F1 boss resolve, before stair Continue applied
  path: {
    floor: 1 | 2                 // must match outer floor when on that graph
    layout_id: string            // or seed used to build this floor's graph
    revealed: string[]           // node ids fully revealed
    scouted_type_only: string[]  // Keen Eye / free Scout type-only (if tracked)
    cleared: string[]
    cursor: string | null        // Path selection / pending enter
    rumor_text_by_node: map<string,string>?  // optional; else re-derive from seed
  }
  // After stair Continue: path is the Floor 2 graph. Do not keep a parallel live F1 graph
  // unless Engineer needs it for debug — F1 is done when floor==2.

  // --- Player / climb economy ---
  player_hp: int                 // NO heal-to-full across F1→F2
  player_max_hp: int             // includes meta_hp_2 at save time
  run_wallet: int                // unbanked remnants
  run_remnants_earned: int       // gross this climb (Summary)
  nodes_cleared: int

  // --- Loadout (locked across F1→F2) ---
  loadout: {
    locked: bool                 // true after first lock; stays true on F2
    card_ids: string[]           // exactly 5 (Sixth Oath deferred)
  }

  // --- Ashbrand (floor2 persist) ---
  ashbrand: {
    weapon_id: string            // "ashbrand" starter; locked Hub weapons not buys
    level: int                   // 1..3
    charge: int                  // CHAIN charge entering next fight (usually 0 post-fight)
  }

  // --- Run-scoped meta charges / prefs ---
  free_scout_charges: int        // remaining this climb
  rumor_rerolls: int             // rumor_clarity remaining
  combat_speed_2x: bool          // 2x pref for this climb (rules frozen in combat-2x-v0111)
  unlocks_this_run: string[]     // ids gained this climb only (if any)

  // --- Shop visit fidelity (rules frozen; state only) ---
  shop_visits: [{
    node_id: string
    floor: 1 | 2
    sold_offer_ids: string[]
  }]

  // --- Resume hint (never mid-combat) ---
  resume: "Path" | "FloorBreak"
  // Default cold start / Menu Continue → Path.
  // If pending_stair_continue (or resume==FloorBreak) → FloorBreak interstitial
  // ("The stair turns." + Continue) — NOT Path with a stair CTA (Architect lock).
}
```

### Alignment with `floor2-v019.md` persist

| Floor2 carry | Slot field |
|--------------|------------|
| Player HP (no full heal) | `player_hp` |
| Run remnants / wallet | `run_wallet` |
| Loadout 5 + locked | `loadout` |
| Ashbrand level/charge | `ashbrand` |
| Unlocks already owned | Meta store + `unlocks_this_run` |

### Dropped vs legacy `run_v0`

| Removed | Why |
|---------|-----|
| `combat.{enemy_hp,round}` | Never mid-beat; resume Path or FloorBreak |
| Generic `route` mid-screen enums for Combat/Shop/… | Legal writes end on Path / FloorBreak / loadout→Path |

### Optional extras

| Field | Note |
|-------|------|
| Per-floor layout seeds | May derive from `rng_seed` + floor; outer `rng_seed` still **required** |
| `treasure_gain_taken` | If treasure Gain is one-shot per node |

---

## 4. Resume / Menu

| Case | Behavior |
|------|----------|
| Cold start + valid slot (default) | → **Path**. Never mid-beat combat |
| Cold start / Continue + `pending_stair_continue` (or `resume=FloorBreak`) | → **FloorBreak** (**"The stair turns."** + Continue) — **not** Path + stair CTA (CoS + Architect) |
| Slot is Hub / Menu / finished Summary | Treat as no mid-run; clear or ignore; land **Menu** (WO) |
| Menu **Continue** | Same as cold start resume (Path or FloorBreak) |
| Menu **Continue** hidden | No valid slot |
| **New climb** | Confirm → CLEAR slot → Tutorial/Path; meta untouched |
| Summary → Hub | Bank wallet → CLEAR slot |
| Corrupt | CLEAR slot; keep meta; Menu |

---

## 5. Forks — CoS LOCK (2026-09-23)

No Elliott escalate; stays inside WO.

| # | Lock |
|---|------|
| 1 | `pending_stair_continue` → resume **FloorBreak** ("The stair turns."). Do **not** Path + stair CTA. Else cold start → Path (or Hub/Menu/summary rules). |
| 2 | `rng_seed` **INCLUDE** — required for fog/shop rebuild. Not optional/cut. |
| 3 | Dual write on F1 boss **YES** — node resolve / combat Continue may set pending_stair; stair Continue writes floor=2 path. Both in WO write set. |

No economy number changes in this WO.

---

## 6. Engineer checklist

- [ ] One slot; writes only at §2 moments  
- [ ] Never mid-beat / Wake  
- [ ] All §3 fields (esp. `rng_seed`, floor2 carry, Ashbrand, loadout lock)  
- [ ] Cold start → Path (default) or FloorBreak if pending stair; Continue iff slot; New climb confirm wipe  
- [ ] F1 boss dual write (pending_stair then floor=2)  
- [ ] Summary→Hub clears mid-run; meta separate  
- [ ] Frozen systems untouched  

---

## Changelog

| Date | Change |
|------|--------|
| 2026-09-23 | Initial brief from CoS WO |
| 2026-09-23 | Full schema vs Architect `save-v0112.md` + `floor2-v019.md`; drop mid-combat snapshot |
| 2026-09-23 | Architect: pending stair → FloorBreak (not Path CTA) |
| 2026-09-23 | CoS lock: FloorBreak resume; rng_seed required; dual F1-boss write |

---

**Related:** v0.1.24-rumorcharge — both `free_scout_charges` and `rumor_rerolls` must persist (never one shared boolean). See `rumorcharge-v0124.md`.
