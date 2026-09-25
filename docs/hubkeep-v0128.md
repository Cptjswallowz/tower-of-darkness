# Hub keep — v0.1.28-hubkeep (WO LOCKED)

Status: **implemented** (feature tip; no tag until QA PASS + CoS green). Owner: **Meta & Ops** — baseline + stack + migration.  
Bug: v0.1.27 Hub UI **wiped** older perks. Fix = **ADD / stack**, never replace or wipe.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

Offers / costs / screen chrome stay as [`hub-v0127.md`](hub-v0127.md). This WO only locks **grant math**, **migration**, and **OWNED UI** rules after the wipe.

**Frozen:** combat art, packs, Wake, path, 2x.

---

## Principle

| Rule | Value |
|------|--------|
| Bank | **Never** reset `remnants_bank` |
| Owned flags | **Never** wipe unlock set to install Hub rows |
| Hub list | **ADD** the four Hub offers; do **not** delete prior meta unlocks (`meta_hp_2`, `rest_heal_plus`, `boss_bonus_2`, `rumor_clarity`, card rares, etc.) |
| Title rem == Hub rem | After **every** buy, both screens read the same `remnants_bank` |

---

## Baseline (always on, free — do **not** charge rem)

| Wallet | Baseline grant | Notes |
|--------|----------------|--------|
| Rumor re-roll | **1 / floor** | Pre-0.1.27 feel restored for **every** climb (F1 start + FloorBreak→F2). No Hub buy required. |
| Free Scout | **0** unless save already owned Scout | Only if unlock set already has `scout_charge` (older build / migration). **Do not** bill rem to restore that charge. |

Baseline ≠ Hub row. Players do **not** see a free “restore” Buy for these.

---

## Hub purchases **stack** on baseline

| Hub offer | unlock_id | Cost | Stack effect |
|-----------|-----------|------|--------------|
| Scout | `scout_charge` | 8 | **+1** Free Scout / climb (on top of migrated baseline Scout if already owned — owned = no rebuy) |
| Extra rumor | `extra_rumor` | 6 | **+1** rumor re-roll / floor **on top of** baseline 1 → **2 / floor** when owned |
| Host of Embers | `host_of_embers` | 12 | Cinder Vow in pool |
| Iron Lesson | `iron_lesson` | 12 | Grave Nail in pool |

### Grant formulas (Engineer)

```
freeScoutCharges @ climb start =
  1 if scout_charge in unlocks else 0
  // migrated old Scout → same flag; no extra baseline Scout beyond ownership

rumorRerolls @ floor start (F1 and FloorBreak→F2) =
  1                          // baseline always
  + (1 if extra_rumor in unlocks else 0)
```

Legacy `rumor_clarity` (Clear Fog): keep flag in unlock set if present; **migration** maps it → Extra rumor OWNED (below). Do **not** double-grant climb-only + Extra if both ids exist after migration — prefer the Extra-rumor floor stack once migrated.

Wallets stay separate (`rumorcharge-v0124.md`).

---

## Migration (first launch this tag)

Run **once** on meta load before Hub UI. Idempotent.

| Step | Action |
|------|--------|
| 1 | Keep current `remnants_bank` unchanged |
| 2 | If unlocks contain `scout_charge` → Scout row **OWNED** (already) |
| 3 | If unlocks contain `rumor_clarity` **or** `extra_rumor` → ensure `extra_rumor` in unlocks (mark Extra rumor **OWNED**); keep `rumor_clarity` flag if present or coalesce — **no rem charge** |
| 4 | If unlocks contain `cinder_vow` **or** `host_of_embers` → ensure `host_of_embers` (Host **OWNED**); no double bill |
| 5 | If unlocks contain `grave_nail` **or** `iron_lesson` → ensure `iron_lesson` (Iron **OWNED**); no double bill |
| 6 | Do **not** remove other unlock ids (`meta_hp_2`, rares, etc.) |

Pseudo:

```
migrateHubkeep(bank, unlocks) -> (bank, unlocks')  // bank unchanged
  unlocks' = unlocks
  if rumor_clarity in unlocks' → unlocks' += extra_rumor
  if cinder_vow in unlocks' → unlocks' += host_of_embers
  if grave_nail in unlocks' → unlocks' += iron_lesson
  // scout_charge / host_of_embers / iron_lesson / extra_rumor already OWNED if present
```

---

## OWNED UI (Hub)

| State | CTA |
|-------|-----|
| Owned | **OWNED** — button dead / non-purchasing |
| Unowned, bank &lt; cost | **Can't afford** — grey |
| Unowned, bank ≥ cost | **Buy** — spend immediately; then row → OWNED |

Title `Hub · N remnants` and Hub `Remnants  N` must match after every buy (same bank).

---

## Engineer checklist

- [x] Baseline rumor **1/floor** for all climbs (no rem)
- [x] Scout grant only from `scout_charge` ownership (migrated or bought); no rem to restore
- [x] Extra rumor stacks → **2/floor** when owned
- [x] Migration: bank kept; scout/rumor/skill flags → Hub rows OWNED; no double bill
- [x] Unlock set **additive** — never wipe old perks to show the four offers
- [x] OWNED / Can't afford / Buy; title rem == Hub rem
- [x] No combat art / packs / Wake / path / 2x changes

---

## Related

- Offers/costs/skills: [`hub-v0127.md`](hub-v0127.md)
- Cards #12–13: `cards-v0.md`
- Wallets: `rumorcharge-v0124.md`
- Persist store: `meta-economy-v0.md` / `MetaStore`

---

---

## Meta green (2026-09-24)

| Check | Confirmed |
|-------|-----------|
| Bank | Never reset; Buy spends `remnants_bank` only; title rem == Hub rem |
| OWNED | Additive unlock set; migration maps scout/rumor/skill → Hub rows; no wipe of `meta_hp_2` / rares / etc. |
| Baseline rumor | **1/floor** always (free) |
| Baseline Scout | Only if `scout_charge` already owned (migrated/bought); no rem restore |
| Stack | Extra rumor → **2/floor**; Scout → +1/climb; Host/Iron → pool |
| Migration | Keep bank; `rumor_clarity`→`extra_rumor`; card ids→Host/Iron; no double bill |

Spec lock = **green**. Engineer wired: baseline + Extra stack in `HubOffers`; one-shot `MetaStore.ensureHubkeepV0128Migrated`; `HubKeepV0128Test`.

## Changelog

| Date | Change |
|------|--------|
| 2026-09-24 | v0.1.28-hubkeep — baseline rumor 1/floor; stack Hub buys; migration OWNED; Meta & Ops |
| 2026-09-24 | Meta green — bank/OWNED/grants confirmed vs Architect lock |
| 2026-09-24 | Engineer — baseline rumor + migrate + HubKeepV0128Test |
