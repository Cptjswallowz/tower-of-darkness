# Meta economy v0 (Tower of Darkness — Compose slice)

Status: **CoS temporary lock** (2026-09-20). Elliott can override. Numbers still **untested** / not QA-measured.

Owner: Meta & Ops. Escalate forks that break locks to Chief of Staff before implementation. Tower of Darkness is greenfield. Other projects are out of scope.

## Authoritative locks

| Lock | Value |
|------|--------|
| Shop stock prices | **5–15** remnants |
| Cheapest permanent unlock | **15** remnants |
| Hub | **Unlocks only** — no Path-style shop stock on Hub |
| Path Shop | Rotating stock (section 2) |
| Hub useful spend | Always offer a useful spend or Climb earn-path (no meta stall) |
| Save/resume | Meta required; mid-run if feasible in slice |

Aligns with `slice-screens.md` and `cards-v0.md`.

---

## 1. Remnants earn (run)

Currency banks at **Run Summary** → Hub. Mid-run spend (Shop) draws from **run wallet** (earned this climb, not yet banked). On Summary, leftover run wallet → `remnants_bank`.

### Earn table (CoS temporary lock)

| Source | Remnants | Notes |
|--------|----------|--------|
| Combat win (normal) | **5** | Equals cheapest shop price → first fight can fund a shop buy |
| Combat loss / Flee | **1** | Consolation; avoids total zero runs |
| Treasure (remnants pick) | **6** | Alt: card-swap pick grants **0** remnants |
| Event — remnants branch | **3** | Other branches: 0 remnants (heal / hitch) |
| Event — non-remnant branches | **0** | — |
| Rest | **0** | Heal / Scout only |
| Boss win | **10** | |
| Boss loss | **3** | |

### Path budget (one-floor slice, illustrative)

| Path sketch | Gross earn | After cheapest shop (5) | Bank if no shop |
|-------------|------------|-------------------------|-----------------|
| 1× combat + boss win | 5+10 = **15** | 10 | 15 → first unlock OK |
| 2× combat + boss win | 5+5+10 = **20** | 15 | 20 |
| 2× combat + treasure + boss | 5+5+6+10 = **26** | 21 | 26 |
| Boss loss after 1 combat | 5+3 = **8** | 3 | 8 — unlock not yet; shop still usable next climb if banked |

**Soft-lock guard:** Combat win ≥5 so a Shop node after one fight is never empty of affordable stock. Minimal victory path (1 combat + boss) hits exactly **15** for the cheapest Hub unlock.

### Out of scope (slice)

Elite/hard node premium, multi-floor multipliers, daily bonuses — defer.

---

## 2. Shop stock rotation

Applies to **Path Shop** nodes (`slice-screens.md`: 3–5 offers). Hub does **not** sell run stock — **CoS temporary lock:** Hub = unlocks only; Path Shop keeps rotating stock.

### Slot rules

| Rule | Value |
|------|--------|
| Slot count | **4** (within 3–5) |
| Price band | Each offer **5–15** inclusive |
| Seed | `hash(run_id, node_id)` — stable for that visit |
| Re-enter same uncleared shop | Same stock; purchased slots stay sold-out |
| Refresh mid-visit | **None** in slice |
| Cross-run | New `run_id` → new roll |

### Rarity / price mix (per shop roll)

Roll 4 offers independently, then **dedupe by offer_id** (reroll collisions once).

| Tier | Price band | Weight | Example offer stubs |
|------|------------|--------|---------------------|
| A Common | 5–8 | 50% | Small heal; rumor peek |
| B Mid | 9–12 | 35% | One-time card swap; mid heal |
| C High | 13–15 | 15% | Strong heal to cap; premium swap |

Target mix after 4 rolls (expected): ~2A / ~1–2B / ~0–1C. If zero A after roll, force slot 0 → tier A @ price 5 (anti-stall).

### Offer catalog stubs (ids for Engineer)

| offer_id | Tier | Default price | Effect stub |
|----------|------|---------------|-------------|
| `heal_small` | A | 5 | +8 HP (cap 30) |
| `rumor_peek` | A | 6 | Reveal one adjacent fogged node type |
| `heal_mid` | B | 10 | +15 HP (cap 30) |
| `card_swap` | B | 12 | Swap one loadout card ↔ unused pool |
| `heal_full` | C | 15 | HP → 30 |
| `card_swap_plus` | C | 14 | Swap + minor weight buff this floor (provisional) |

Prices stay inside 5–15. Exact effects are stubs — balance TBD with QA.

---

## 3. Hub unlock ladder (first 8)

Hub = **unlocks only** (CoS). No rotating shop stock on Hub.

### Card pool ownership (align `cards-v0.md`)

| Set | Cards | Start state |
|-----|-------|-------------|
| Open climb pool | Hostflint, Cinder Step, Iron Mantle, Emberbrand, Dust Veil, Vow Plate, Ruin Seal, Ash Press, Tower Pike (#1–9) | **Owned** at install — not Hub-sold |
| Hub / Treasure rares | **Shadow Latch**, **Relic Shard** (#10–11) | Locked until Hub unlock **or** Treasure rare offer (`cards-v0` CoS lock) |

Do **not** Hub-sell cards #1–9 (would duplicate the open pool). Future pool-expansion cards beyond these 11 = stubs only when Architect adds names.

Permanent unlocks. All costs **≥15**. Hub always shows the **lowest-cost unowned** unlock as primary CTA (plus list of next ones). If bank &lt; cheapest remaining, show it grayed with deficit and surface **Climb** as the useful action.

| # | unlock_id | Cost | Effect |
|---|-----------|------|--------|
| 1 | `unlock_shadow_latch` | **15** | Add **Shadow Latch** to unlocked pool — **cheapest card unlock** (master prompt) |
| 2 | `meta_hp_2` | **15** | Permanent +2 max HP (slice base 30 → 32) — parallel perk CTA |
| 3 | `scout_charge` | **20** | +1 free Scout at first Rest each run |
| 4 | `unlock_relic_shard` | **20** | Add **Relic Shard** to unlocked pool |
| 5 | `rest_heal_plus` | **25** | Rest Heal restores +4 more HP (cap still applies) |
| 6 | `boss_bonus_2` | **25** | +2 remnants on boss win |
| 7 | `loadout_flex` | **30** | Prefer 6-card loadout default in UI (still 5–6 legal) |
| 8 | `rumor_clarity` | **35** | One free rumor re-roll per climb (Path fog) |

Card ids: **Shadow Latch** @15 (cheapest card), **Relic Shard** @20. Rows 2–3 and 5–8 are non-card meta perks. Hub may show both 15-cost CTAs (card + perk) so spend is never empty.

If Treasure already granted a rare, Hub row for that card shows **owned** / skipped.

**Stock rotation at Hub:** none — fixed ladder order. No shop-tier (5–14) permanent unlocks.

---

## 4. Save / resume schema sketch

Local only (no accounts in slice). Suggest JSON or DataStore keys; Engineer picks storage.

### Meta (`meta_v0`) — always persist

```
meta_v0 {
  schema: 0
  remnants_bank: int
  unlocks: string[]          // unlock_id owned
  tutorial_done: bool
  last_run_summary: {        // optional UX
    won: bool
    remnants_gained: int
  } | null
}
```

### Mid-run (`run_v0`) — write on screen enter / node resolve / shop buy; clear on Run Summary → Hub

```
run_v0 {
  schema: 0
  run_id: string
  rng_seed: long
  route: enum                // Menu|Tutorial|Path|Loadout|Combat|Shop|Rest|Event|Treasure|Boss|Summary
  path: {
    layout_id: string        // fixed slice graph
    revealed: string[]       // node ids
    cleared: string[]
    cursor: string | null    // current / pending node
  }
  loadout: {
    locked: bool
    card_ids: string[]       // 5–6
  }
  combat: {
    player_hp: int
    enemy_hp: int | null
    round: int
  } | null
  wallet_run: int            // unbanked remnants this climb
  shop_state: {
    node_id: string
    sold: string[]           // offer_ids bought
  }[]
  flags: {
    floor_loadout_locked: bool
  }
}
```

### Resume rules

| Cold start | Behavior |
|------------|----------|
| `run_v0` present | Menu **Climb** → resume at `route` (per slice-screens) |
| No `run_v0` | Menu; Climb starts Tutorial or Path |
| Corrupt / schema mismatch | Discard `run_v0`, keep `meta_v0`; log once |

**Feasible in slice:** yes if Engineer serializes on each navigation event. Minimum viable: persist on leaving Combat/Shop/Rest/Event/Treasure and on app background.

---

## Forks that would break master / slice locks

Flag to CoS before shipping if anyone proposes:

1. **Shop price &lt;5 or &gt;15** — breaks shop lock.
2. **Permanent unlock &lt;15** — breaks cheapest-unlock lock.
3. **Combat win &lt;5** while Shop can appear after first mid-node — early shop soft-lock (hub/path “useful spend” spirit).
4. **Minimal victory earn (1 combat + boss) &lt;15** — first Hub unlock unreachable → meta stall.
5. **Hub with no affordable unlock and no Climb CTA** when bank &lt;15 — meta stall.
6. **Importing external shop/skill cost tables** into ToD shop or unlocks — rejected by CoS decision (ToD greenfield only).
7. **Mid-run save without `rng_seed`** — desync / non-reproducible resume (quality fork; escalate if cut).
8. **Banking only on win with 0 on loss and no combat consolation** — can starve Hub after learning deaths (soft meta stall); current table avoids via loss crumbs.

---

## Resolved (CoS 2026-09-20)

1. Earn table accepted as written (incl. boss-loss **3**).
2. Hub = unlocks only; Path Shop keeps rotation.
3. Card ladder: rares **Shadow Latch** / **Relic Shard** only; #1–9 open at start; remaining rows = meta perks.
4. Cheapest **card** unlock = **Shadow Latch @15**; `meta_hp_2` also @15 as parallel perk; Relic Shard @20.
5. Stand by — no further economy edits until APK or Elliott changes locks.

## Open questions

1. Exact Treasure rare offer rates for Shadow Latch / Relic Shard (provisional in `cards-v0`) — Meta may set later; not measured.

---

## Changelog

| Date | Change |
|------|--------|
| 2026-09-20 | v0 initial — Meta & Ops from CoS task |
| 2026-09-20 | CoS temporary lock; Hub unlocks-only; ladder → meta perks + Shadow Latch / Relic Shard |
| 2026-09-20 | Override: Shadow Latch @15 (cheapest card); Relic Shard @20; meta_hp_2 @15 parallel; stand by |
