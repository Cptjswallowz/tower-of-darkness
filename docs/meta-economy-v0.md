# Meta economy v0 (Tower of Darkness — Compose slice)

Status: **CoS temporary lock** (2026-09-20). Elliott can override. Numbers still **untested** / not QA-measured.

Owner: Meta & Ops. Escalate forks that break locks to Chief of Staff before implementation. Tower of Darkness is greenfield. Other projects are out of scope.

## Authoritative locks

| Lock | Value |
|------|--------|
| Shop stock prices | **5–15** remnants (**Elliott exception v0.1.8:** `heal_small` = **3**) |
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
| Combat win (normal) | **5** | Funds shop after first fight (covers `heal_small` @3 and most A-tier) |
| Combat loss / Flee | **1** | Consolation; avoids total zero runs |
| Treasure (remnants pick) | **6** | Alt: card-swap pick grants **0** remnants |
| Event — remnants branch | **3** | Other branches: 0 remnants (heal / hitch) |
| Event — non-remnant branches | **0** | — |
| Rest | **0** | Heal / Scout only |
| Boss win | **10** | |
| Boss loss | **3** | |

### Path budget (one-floor slice, illustrative)

| Path sketch | Gross earn | After `heal_small` (3) | Bank if no shop |
|-------------|------------|-------------------------|-----------------|
| 1× combat + boss win | 5+10 = **15** | 12 | 15 → first unlock OK |
| 2× combat + boss win | 5+5+10 = **20** | 17 | 20 |
| 2× combat + treasure + boss | 5+5+6+10 = **26** | 23 | 26 |
| Boss loss after 1 combat | 5+3 = **8** | 5 | 8 — unlock not yet; shop still usable next climb if banked |

**Soft-lock guard:** Combat win **5** and `heal_small` **3** so a Shop after one fight always has an affordable heal (when not full HP). Minimal victory path (1 combat + boss) hits exactly **15** for the cheapest Hub unlock.

### Out of scope (slice)

Elite/hard node premium, multi-floor multipliers, daily bonuses — defer.

---

## 2. Shop stock rotation

Applies to **Path Shop** nodes (`slice-screens.md`: 3–5 offers). Hub does **not** sell run stock — **CoS temporary lock:** Hub = unlocks only; Path Shop keeps rotating stock.

### Slot rules

| Rule | Value |
|------|--------|
| Slot count | **4** (within 3–5) |
| Price band | **5–15** inclusive, except Elliott WO **v0.1.8-shopwallet:** `heal_small` = **3** |
| Seed | `hash(run_id, node_id)` — stable for that visit |
| Re-enter same uncleared shop | Same stock; purchased slots stay sold-out |
| Refresh mid-visit | **None** in slice |
| Cross-run | New `run_id` → new roll |

### Shop UX (v0.1.8-shopwallet — Elliott)

| Case | Behavior |
|------|----------|
| `runWallet` ≤ 0 (empty wallet) | Show **"Nothing you can buy."** + **Leave** only — **no** greyed stock rows |
| Heal offers (`heal_small` / `heal_mid` / `heal_full`) at full HP | **Blocked** (same spirit as Rest Heal at cap) — omit or disable; do not sell no-op heals |
| Path / Wake / swap / counters / HP caps | **Frozen** this WO — no number changes beyond Small Heal price |

### Rarity / price mix (per shop roll)

Roll 4 offers independently, then **dedupe by offer_id** (reroll collisions once).

| Tier | Price band | Weight | Example offer stubs |
|------|------------|--------|---------------------|
| A Common | 3–8 (`heal_small` @3; else 5–8) | 50% | Small heal; rumor peek |
| B Mid | 9–12 | 35% | One-time card swap; mid heal |
| C High | 13–15 | 15% | Strong heal to cap; premium swap |

Target mix after 4 rolls (expected): ~2A / ~1–2B / ~0–1C. If zero A after roll, force slot 0 → `heal_small` @ **3** (anti-stall).

### Offer catalog (ids for Engineer)

| offer_id | Tier | Default price | Effect stub |
|----------|------|---------------|-------------|
| `heal_small` | A | **3** | +8 HP (cap); blocked at full HP |
| `rumor_peek` | A | 6 | Reveal one adjacent fogged node type |
| `heal_mid` | B | 10 | +15 HP (cap); blocked at full HP |
| `card_swap` | B | 12 | Swap one loadout card ↔ unused pool (**frozen** this WO) |
| `heal_full` | C | 15 | HP → cap; blocked at full HP |
| `card_swap_plus` | C | 14 | Swap + minor weight buff this floor (provisional; **frozen**) |

Other offers remain in **5–15**. Only `heal_small` may sit at **3** (Elliott). Untested / not QA-measured as balance proof.

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

### Title map (v0.1.3 Hub copy — costs unchanged)

Hub UI shows **human title — short effect**, never raw `unlock_id`. Ids remain stable for save/Engineer.

| # | Title | unlock_id | Cost | Hub UI effect line | Notes |
|---|-------|-----------|------|--------------------|-------|
| 1 | **Shadow Latch** | `shadow_latch` / card id | **15** | Unlock Shadow Latch — rare MoveEffect | Cheapest **card** unlock |
| 2 | **Iron Blood** | `meta_hp_2` | **15** | +2 max HP | Parallel perk CTA |
| 3 | **Pathseer** | `scout_charge` | **20** | +1 free Scout each climb | |
| 4 | **Relic Shard** | `relic_shard` / card id | **20** | Unlock Relic Shard — rare Equipment | |
| 5 | **Deep Rest** | `rest_heal_plus` | **25** | Rest Heal +4 HP | |
| 6 | **Warden's Tithe** | `boss_bonus_2` | **25** | +2 remnants on boss win | |
| 7 | **Sixth Oath** | `loadout_flex` | **30** | Prefer 6-card loadout default | **Hidden / disabled** until later order — do not show buy CTA |
| 8 | **Fogbreak** | `rumor_clarity` | **35** | 1 rumor re-roll per climb | |

### Weapons (data-only / locked — not remnant buys in v0.1.3)

| Title | Role | Hub |
|-------|------|-----|
| **Ashbrand** | Starter weapon | **Not** a Hub buy — owned at start |
| **Notch Pike** | Future weapon | Data-only row; **locked** / disabled in Hub |
| **Vow Edge** | Future weapon | Data-only row; **locked** / disabled in Hub |

If Treasure already granted a rare card, Hub card row shows **owned** / skipped.

**Stock rotation at Hub:** none — fixed ladder order. No shop-tier (5–14) permanent unlocks.

---

## 4. Save / resume schema sketch

> **v0.1.12-save:** Mid-run write points, payload, and Menu Continue rules are authoritative in [`midrun-save-v0112.md`](midrun-save-v0112.md). Sketch below remains for meta keys; do not contradict that WO.

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

### Mid-run (`midrun_v0112`) — full fields in [`midrun-save-v0112.md`](midrun-save-v0112.md)

Legacy name `run_v0` retired for mid-run. One slot. Writes: **node resolve** / **stair Continue** / **loadout lock** only. No mid-combat snapshot. Clear on Summary→Hub / New-climb confirm.

### Resume rules (v0.1.12-save — see `save-v0112.md`)

| Cold start | Behavior |
|------------|----------|
| Mid-run slot present (climb in progress) | Resume on **Path** (not mid-beat combat) |
| No mid-run slot | Menu; Climb / New climb starts Tutorial or Path |
| Corrupt / schema mismatch | Discard mid-run slot, keep `meta_v0`; log once |

**Menu:** **Continue** resumes saved climb. **New climb** requires confirm wipe of the mid-run slot.

**Write moments (locked):** node resolve; stair Continue (F1→F2); loadout lock. **Do not** write mid-beat / mid-Wake.

**Feasible in slice:** yes. Full field list: [`midrun-save-v0112.md`](midrun-save-v0112.md).

---

## Forks that would break master / slice locks

Flag to CoS before shipping if anyone proposes:

1. **Shop price &lt;5 or &gt;15** — breaks shop lock (**except** Elliott-approved `heal_small` @3).
2. **Permanent unlock &lt;15** — breaks cheapest-unlock lock.
3. **Combat win &lt;5** while Shop can appear after first mid-node — early shop soft-lock (hub/path “useful spend” spirit).
4. **Minimal victory earn (1 combat + boss) &lt;15** — first Hub unlock unreachable → meta stall.
5. **Hub with no affordable unlock and no Climb CTA** when bank &lt;15 — meta stall.
6. **Importing external shop/skill cost tables** into ToD shop or unlocks — rejected by CoS decision (ToD greenfield only).
7. **Mid-run save without `rng_seed`** — **forbidden** (CoS lock v0.1.12: `rng_seed` required).
8. **Banking only on win with 0 on loss and no combat consolation** — can starve Hub after learning deaths (soft meta stall); current table avoids via loss crumbs.

---

## Resolved (CoS 2026-09-20)

1. Earn table accepted as written (incl. boss-loss **3**).
2. Hub = unlocks only; Path Shop keeps rotation.
3. Card ladder: rares **Shadow Latch** / **Relic Shard** only; #1–9 open at start; remaining rows = meta perks.
4. Cheapest **card** unlock = **Shadow Latch @15**; `meta_hp_2` also @15 as parallel perk; Relic Shard @20.
5. Stand by — no further economy edits until APK or Elliott changes locks.
6. **v0.1.8-shopwallet (Elliott):** `heal_small` **3**; empty-wallet copy + Leave (no grey stock); heal offers blocked at full HP; Path/Wake/swap/counters/HP frozen.

## Open questions

1. Exact Treasure rare offer rates for Shadow Latch / Relic Shard (provisional in `cards-v0`) — Meta may set later; not measured.

---

## Changelog

| Date | Change |
|------|--------|
| 2026-09-20 | v0 initial — Meta & Ops from CoS task |
| 2026-09-20 | CoS temporary lock; Hub unlocks-only; ladder → meta perks + Shadow Latch / Relic Shard |
| 2026-09-20 | Override: Shadow Latch @15 (cheapest card); Relic Shard @20; meta_hp_2 @15 parallel; stand by |
| 2026-09-20 | v0.1.3 Hub copy: human titles; hide loadout_flex; weapon rows Ashbrand/Notch Pike/Vow Edge notes |
| 2026-09-22 | v0.1.8-shopwallet: heal_small 3; empty-wallet UX; heal blocked at full HP; note Elliott exception to 5–15 |
| 2026-09-23 | Point §4 mid-run rules to midrun-save-v0112.md (WO v0.1.12-save) |
| 2026-09-23 | Expand midrun_v0112 vs Architect save-v0112 + floor2 persist; retire run_v0 combat snapshot |
| 2026-09-23 | CoS: rng_seed required in mid-run slot |

---

**Superseded (Hub offer set):** v0.1.27-hub — live Hub = 4 offers (Scout **8**, Extra rumor **6**, Host of Embers **12**, Iron Lesson **12**). See `hub-v0127.md`. Older Pathseer 20 / Fogbreak 35 ladder rows yield to this set when Hub v0.1.27 is live. Meta owns bank + OWNED persist.

---

**Related:** v0.1.28-hubkeep — Hub ADD not replace; migration keeps bank/OWNED. See `hubkeep-v0128.md`.
