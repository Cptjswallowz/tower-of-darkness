# Vertical slice — screen flow (v0)

Status: design lock for Android Engineer implementation. One floor. Placeholders OK for art/SFX.

**Fantasy (tone):** Ashen Host climbing the Tower of Darkness in a fallen kingdom. Dice auto-combat + rumor fog are first-class. Compose locks live in `balance-targets.md`.
**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

**Combat:** v0.1.3 bar + v0.1.4 Wake in `combat-bar-v013.md` / `wake-v014.md`. Fog/rumor + floor generator unchanged.

## Flow (happy path)

```
Menu
  → Tutorial (first run; see Skip rules)
  → Path
  → Loadout
  → Node resolve (Combat | Shop | Rest | Event | Treasure)
  → …repeat until Boss node…
  → Boss
  → Run Summary
  → Hub
  → (optional) start another climb → Path
```

Back stack: each screen owns a single route. No deep linking in the slice. Cold start lands on **Menu** unless a mid-run save exists (Meta & Ops owns save/resume; if absent, always Menu).

---

## Screens

### 1. Menu
- Actions: **Climb** (start / continue run), **Hub** (meta), optional Settings stub.
- Climb with no save → Tutorial (if not yet completed this install) else Path.
- Climb with save → resume at saved screen (Meta & Ops).

### 2. Tutorial
- Beats (in order, short copy OK):
  1. Rumor fog intro (show one sample rumor card; no exact stats).
  2. Path nodes explained (icons: fight / shop / rest / event / treasure / boss).
  3. Loadout: pick **exactly 5** cards from the starter pool.
  4. Combat bar one-liner: 5 skills, weight among unspent, exhaust grey, Ashbrand CHAIN+SPARK; you watch, you don’t tap mid-fight (`combat-bar-v013.md`).
- **Skip rules:** Skip is available only **after** the player has completed the loadout step **and** seen the rumor beat. Skip jumps to **Path** with a default 5-card loadout (first 5 by table order in `cards-v0.md`).
- Completing or skipping marks tutorial done for this install (local flag).

### 3. Path
- One floor graph for the slice (fixed layout OK):
  - Entry → 2–3 branching mid nodes → merge → Boss.
  - Node types used in slice: **Combat**, **Shop**, **Rest**, **Event**, **Treasure**, **Boss**.
- Rumor fog: unrevealed nodes show rumor text (see `rumors.md`), not exact rewards or enemy stats.
- Selecting a revealed adjacent node → Loadout (if not yet locked this floor) or straight into that node’s screen.
- **Loadout lock:** first time you leave Path into any resolve screen this climb, loadout locks until Run Summary. Re-open Path between nodes does not unlock edit.

### 4. Loadout
- Pool: **10–12** starter cards (`cards-v0.md`).
- Select **exactly 5** cards. Confirm disabled until count == 5. **No sixth skill.**
- Show rarity + short effect line; no live DPS math in UI for slice.
- Confirm → return to Path (or into the pending node if entered from a node tap).
- `loadout_flex` Hub perk: **deferred** (v0.1.3).

### 5. Combat (bar v0.1.3 — see `combat-bar-v013.md`)
- Player HP start: **30** (see `balance-targets.md`).
- Enemy HP start: **20** (normal). Boss: **28 HP**, counter **6–9** (*CoS temporary lock*; see `balance-targets.md`).
- **5-cap exhaust:** weight-pick among unspent, grey exhaust, reset at 5 spent; Ashbrand CHAIN (full Wake) + SPARK (half Wake) on weapon beat.
- Player skill damage band target **4–8**; trash enemy counter **5–8**; boss counter **6–9** (locks / targets, not measured WRs).
- No player input mid-fight. **Flee: grayed**. End gate requires **Continue**.
- Continue → Path (non-boss win) or Run Summary (boss win / loss).
- Floor generator + rumor visibility: **unchanged** this pass.

### 6. Shop
- Currency: **remnants**.
- Stock: 3–5 offers, prices **5–15** remnants.
- Buys: heal stub, one-time card swap stub, or remnant→nothing flavor — keep purchase effects simple; Meta & Ops owns rotation later.
- Leave → Path.

### 7. Rest
- Choose: **Heal** (partial HP toward cap 30) or **Scout** (reveal one adjacent fogged node’s **type only** — no second rumor line; *CoS temporary lock*).
- Leave → Path.

### 8. Event
- One text choice (A/B). Outcomes: small remnants, small heal, or mild next-combat hitch (e.g. −1 weight on one card this floor) — flavor only; keep magnitudes tiny and labeled provisional.
- Leave → Path.

### 9. Treasure
- One reward: remnants **or** offer to swap one loadout card with one unused pool card (still size **5**).
- Rares may appear here (*CoS temporary lock*: Treasure **and** Hub both allow rares).
- Leave → Path.

### 10. Boss
- Same combat screen; boss presentation (nameplate / art slot).
- Stats: **28 HP**, counter **6–9** (*CoS temporary lock*).
- Win → Run Summary (victory). Loss → Run Summary (defeat).

### 11. Run Summary
- Show: win/loss, floors/nodes cleared, remnants gained this run, remnants banked (if Meta wired).
- CTA: **Hub**. Secondary: **Menu**.

### 12. Hub
- Always offer a useful spend: unlock stub costing **≥15** remnants (cheapest unlock target **15**).
- Unlock = **any pool card** and/or a simple **meta perk** (Meta & Ops) — **not** a unique signature system. 6th bar slot = any unlocked pool card. *CoS temporary lock* (pending Elliott widget).
- Rares unlockable here as well as via Treasure.
- Slice: one unlock row (e.g. “Unlock card: [name]”) even if purchase only sets a flag.
- CTA: **Climb** → Path (new run) or Menu.

---

## Navigation state machine (for Engineer)

| From | Event | To |
|------|--------|-----|
| Menu | Climb (no tutorial done) | Tutorial |
| Menu | Climb (tutorial done) | Path |
| Menu | Hub | Hub |
| Tutorial | Complete / Skip (after loadout+rumor) | Path |
| Path | Edit loadout (unlocked) | Loadout |
| Path | Enter Combat/Boss | Combat |
| Path | Enter Shop/Rest/Event/Treasure | that screen |
| Loadout | Confirm | Path (or pending node) |
| Combat | Win (non-boss) | Path |
| Combat | Win (boss) / Lose | Run Summary |
| Shop/Rest/Event/Treasure | Leave | Path |
| Run Summary | Hub | Hub |
| Hub | Climb | Path |

---

## Out of scope (slice)

- Multi-floor climb, card evolution trees, full remnant economy simulation, online, accounts.
- Proven win-rate tuning (QA owns measurement after first APK).

## CoS temporary locks (2026-09-20; Elliott skipped fork widget — locks stand; may still override later)

1. **Boss:** HP **28**, counter **6–9** (trash 20 HP / counter **5–8** — v0.1.4).
2. **Flee:** grayed (stub only); loss via death → summary.
3. **Loadout re-edit:** Path-start lock only (unchanged).
4. **Hub unlock:** any pool card and/or meta perk — not a unique signature system. (`loadout_flex` deferred in v0.1.3.)
5. **Rares:** Treasure drops and Hub both allowed.
6. **Scout:** type only (no second rumor line).
7. **84% / 3.6:** tutorial-default loadout climbs for QA baseline.
