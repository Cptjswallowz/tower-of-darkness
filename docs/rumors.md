# Rumors — text rules (vertical slice)

Status: design lock for Path fog copy. Rumors are **vague hints only**. Never reveal exact HP, damage, prices, remnant amounts, or card weights.

Voice: fallen kingdom / Ashen Host / tower seals. Never put exact combat/economy stats in rumor text.
**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

## Purpose

On the Path screen, fogged (unrevealed) nodes show a **rumor** instead of the true node type label and instead of reward/combat stats. Rumors create tension and light planning without spoiling the dice fight or shop math.

## Hard bans (never put in rumor text)

- Exact numbers: HP, damage, brace, heal, remnant prices, unlock costs, weights, round counts.
- Exact identity when it would spoil: “Boss has 20 HP”, “Shop sells X for 7”.
- Meta certainty: “guaranteed rare”, “you will die”, “safe rest”.
- Spoilers of Event branch outcomes.

## Allowed

- Mood / sensory: ash, bells, cold iron, whispering stone.
- Soft threat level: “something hungry”, “a quiet watch”, “a brighter gate”.
- Soft reward type without amount: “a trader’s latch”, “a place to breathe”, “a sealed coffer”.
- Directional / tower flavor: stairs, seals, wardens, cinders — **Tower of Darkness** voice only.

## Node-type rumor pools (pick 1 at fog reveal; can rotate later)

### Combat
- “Steel answers steel beyond the fog.”
- “A watchful shape; not the seal-warden yet.”
- “The stones remember a fight.”

### Shop
- “A latch and a ledger — someone still sells.”
- “Coin-light, remnant-warm.”
- “Wares behind a half-drawn curtain.”

### Rest
- “Quiet enough to bind a wound.”
- “A cold niche out of the wind.”
- “Ash settles; breath comes easier.”

### Event
- “A choice waits where the corridor forks.”
- “Someone left a question on the wall.”
- “Not a fight — not a shop — something else.”

### Treasure
- “A coffer that forgot its owner.”
- “Something small, sealed, and heavy with luck.”
- “Glint without a shopkeep.”

### Boss
- “The seal hums. This is the end of the floor.”
- “A name you do not know yet, already angry.”
- “No trader, no rest — only the gate.”

## UX rules

1. Fogged node: show **icon silhouette** (optional) + **one rumor string**; do not show type name until revealed.
2. Reveal timing: when the player moves adjacent (Path rule) or Rest → Scout; then show **true type only** and drop the rumor line. Scout does **not** add a second rumor (*CoS temporary lock*).
3. Boss node: may use boss rumor pool even if type is known by position; still no stats.
4. Localization: keep strings short (≤90 chars) for mobile.
5. Do not procedurally inject numbers into these templates.

## CoS temporary locks

- Scout reveals **type only** (no second rumor line).

## Charge wallets (v0.1.24)

See `rumorcharge-v0124.md`: rumor re-roll charges and Free Scout charges are **two separate wallets**. Re-roll spends rumor charges and replaces rumor text on a still-fogged node; does not reveal type.

## Still open

1. Default rumor string sticky for the node until re-roll — confirm no auto-rotate on leave/return without spending a charge (assumed: sticky until re-roll or reveal).
