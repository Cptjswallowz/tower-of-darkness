# Cards v0 — starter pool (vertical slice)

Status: design lock for Android Engineer implementation. **11 cards.** Loadout picks **5–6**.

Fantasy frame: player is **Ashen Host** — last loyal blade of a fallen kingdom — climbing the **Tower of Darkness**. Names evoke ash, oath, and ruin.
**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

Effect classes (Engineer enums):
- `MoveEffect` — positioning / tempo (weight often lower; utility or setup).
- `SkillEffect` — active strike or combat verb when dice fire.
- `Equipment` — passive or triggered gear; usually modifies a hit or grants brace/heal shard.

**Weight:** relative chance the card is selected when the auto-battler rolls the loadout. Higher = more often. Normalize at runtime (sum weights on bar).

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

## Default tutorial loadout (Skip / first confirm)

Cards **1–5**: Hostflint, Cinder Step, Iron Mantle, Emberbrand, Dust Veil.

## Bar rules

- Size: **5 or 6** cards from this pool only (slice).
- Duplicates: **not allowed** on the bar.
- Evolution: **out of scope** for v0 (no evolve UI). Flag for later Architect pass.
- Shop/Treasure may offer a **swap** with an unused pool card; still no duplicates, still 5–6 size.

## Implementation notes

- Dice fire: pick one loadout card weighted by `Weight`, apply effect, resolve Brace → HP, then enemy counter if player still alive.
- `Equipment` that only grants Brace/Heal still consumes the “fire” slot that round (it is the rolled card).
- Do not show exact enemy HP/damage in rumor fog; combat UI may show live HP once fight starts.

## CoS temporary locks

- Bar 6th card: **any unlocked pool card** (no unique signature system).
- Rares (**Shadow Latch**, **Relic Shard**): unlockable via **Treasure and Hub** both.
- Exact rare drop/offer rates: still provisional — Meta & Ops may set tables; do not treat as measured.
