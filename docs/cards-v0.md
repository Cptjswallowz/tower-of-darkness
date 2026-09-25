# Cards v0 — starter pool (vertical slice)

Status: design lock for Android Engineer implementation. **11 cards.** Loadout picks **exactly 5** (v0.1.3 combat bar). See `combat-bar-v013.md`.

Fantasy frame: player is **Ashen Host** — last loyal blade of a fallen kingdom — climbing the **Tower of Darkness**. Names evoke ash, oath, and ruin.
**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

Effect classes (Engineer enums):
- `MoveEffect` — positioning / tempo (weight often lower; utility or setup).
- `SkillEffect` — active strike or combat verb when dice fire.
- `Equipment` — passive or triggered gear; usually modifies a hit or grants brace/heal shard.

**Weight:** used in combat v0.1.3 — each skill beat picks among **UNSPENT** skills only (normalize weights over the live pool). Exhausted skills are greyed out of the pool until cycle reset (`combat-bar-v013.md`).

Damage numbers below are **design targets** aligned to player dmg **4–8** (`balance-targets.md`). Compose locks only. Not playtested.

Rarity: `Common` | `Uncommon` | `Rare` (slice economy only; no craft).

| # | Name | Rarity | Class | Weight | Effect (combat) |
|---|------|--------|-------|--------|-----------------|
| 1 | Hostflint | Common | SkillEffect | 5 | Deal **5** damage. |
| 2 | Cinder Step | Common | MoveEffect | 3 | Deal **4** damage. Next enemy counter this round deals **−2** (min 1). |
| 3 | Iron Mantle | Common | Equipment | 4 | Gain **Brace 3** (absorb before HP). If Brace remains at round end, clear it. |
| 4 | Emberbrand | Common | SkillEffect | 5 | Deal **6** damage. |
| 5 | Dust Veil | Common | MoveEffect | 3 | Deal **4** damage. Gain **Brace 2**. |
| 6 | Vow Plate | Uncommon | Equipment | 3 | Gain **Brace 5**. |
| 7 | Ruin Seal | Uncommon | SkillEffect | 4 | Deal **7** damage. |
| 8 | Ash Press | Uncommon | SkillEffect | 3 | Deal **4** damage. Heal **2** HP (cap 30). |
| 9 | Tower Pike | Uncommon | SkillEffect | 4 | Deal **8** damage. |
| 10 | Shadow Latch | Rare | MoveEffect | 2 | Deal **5** damage. Enemy’s next counter: **−2** damage (min 1). |
| 11 | Relic Shard | Rare | Equipment | 2 | Heal **4** HP (cap 30). If HP was already full, gain **Brace 4** instead. |

**v0.1.19 skill glyphs:** `glyphs-v0119.md` (Buriedbornes tile pictures; Ashbrand blade only).

## Default tutorial loadout (Skip / first confirm)

Cards **1–5**: Hostflint, Cinder Step, Iron Mantle, Emberbrand, Dust Veil.

## Bar rules (v0.1.3)

- Size: **exactly 5** cards from this pool (no sixth skill).
- Duplicates: **not allowed** on the bar.
- Exhaust cycle + weighted pick among unspent + Ashbrand CHAIN/SPARK: `combat-bar-v013.md`.
- Evolution: **out of scope** for v0 (no evolve UI).
- Shop/Treasure may offer a **swap** with an unused pool card; still no duplicates, still size **5**.
- Hub perk `loadout_flex`: **deferred** (do not enable 6-card loadout).

## Implementation notes

- v0.1.3: weight-pick among Ready/unspent each skill beat; exhaust (grey); reset when all 5 spent. Then Ashbrand weapon beat (CHAIN full Wake and/or SPARK half Wake); then enemy. See `combat-bar-v013.md`.
- `Equipment` that only grants Brace/Heal still consumes its bar slot (exhausts) when fired.
- Do not show exact enemy HP/damage in rumor fog; combat UI may show live HP once fight starts.
- Floor generator + rumor visibility: **unchanged** this pass.

## CoS temporary locks / v0.1.3

- Loadout: **exactly 5**; no sixth skill; `loadout_flex` deferred.
- Hub unlocks still: any pool card and/or meta perk (no signature system) — rarer cards via Treasure **and** Hub.
- Exact rare drop/offer rates: provisional — not measured.

## Hub unlock skills (v0.1.27)

Only after Hub buy. See `hub-v0127.md`. **No** further skills this tag.

| # | Name | Rarity | Class | Weight | Effect (combat) |
|---|------|--------|-------|--------|-----------------|
| 12 | Cinder Vow | Uncommon | SkillEffect | 3 | Deal **5**. If Ashbrand ≥1 pip → **Brace 2**. |
| 13 | Grave Nail | Uncommon | SkillEffect | 3 | Deal **4**. Next enemy counter **−1** (min 1). **Soften 1**. |

Glyphs: Art **vow-spark** / **nail**. Same chrome + glossary as pool cards.
