# Hub sections — v0.1.55-hubsplit PART B (Meta)

Status: **Meta & Ops design lock**. Owner: Meta & Ops.  
Engineer implements UI + bank commit from this doc. Art owns bust PNG overlays.

**Baseline:** v0.1.54-trollkept tip `d6f4ed745b703d0b33aa66d608419930c443d534`.  
**Frozen:** Kept formula / remnant payouts (`trollkept-v0154.md`), Hub **prices**, unlock **ids**, skill **text**, FX/SFX, F3 rumors, loadout lock, combat, shop.

**Scope:** Tower of Darkness greenfield only.

---

## 1. Screen chrome

| Element | Lock |
|---------|------|
| Layout | **One** Hub scroll screen |
| Header | Bust + **"Remnants N"** (`remnants_bank`) — stays **visible** while sections scroll (sticky header) |
| Sections | Sticky section headers; order below |
| Buy rules | Perks + Skills only — same Buy / OWNED / Can't afford as today |

---

## 2. Section order (sticky headers)

```
Relics  →  Perks  →  Skills
```

---

## 3. RELICS (display-only trophies)

**Not for sale.** No Buy / Can't afford / cost. List **owned only** (ids in `meta_v0.unlocks` / `unlocked_cards`).

| unlock_id | Display name | Earn line (exact) |
|-----------|--------------|-------------------|
| `soot_rim` | Soot Rim | Reach Floor 2 |
| `ash_pauldron` | Ash Pauldron | Reach Floor 3 |
| `troll_tooth` | Troll Tooth | Kill Cave Troll |
| `gate_sigil` | Gate Sigil | Beat Gate-Warden |

Row UX: **name** + earn line (how earned). No CTA button.

### Empty state (exact copy)

When **none** of the four trophies are owned:

```
Nothing kept yet. Survive a floor.
```

Do **not** show greyed unowned Relic rows.

Award rules unchanged: once on first earn (win or death) — `trollkept-v0154.md`.

---

## 4. PERKS (buyable — unchanged)

Same CTA: **Buy** / **OWNED** / **Can't afford**. Prices & effect lines **unchanged**.

| Order | Title | unlock_id | Cost |
|-------|-------|-----------|------|
| 1 | Scout | `scout_charge` | 8 |
| 2 | Extra rumor | `extra_rumor` | 6 |
| 3 | Hostblood | `hostblood` | 10 |
| 4 | Warm Ash | `warm_ash` | 8 |
| 5 | Ash Tithe | `ash_tithe` | 8 |

---

## 5. SKILLS (buyable Hub-gated — unchanged)

Same CTA. Prices / unlock ids / gated card ids **unchanged**. Effect lines stay current Hub copy (do not rewrite skill combat text).

| Order | Title | unlock_id | Cost | Unlocks skill |
|-------|-------|-----------|------|---------------|
| 1 | Host of Embers | `host_of_embers` | 12 | Cinder Vow (`cinder_vow`) |
| 2 | Iron Lesson | `iron_lesson` | 12 | Grave Nail (`grave_nail`) |
| 3 | Cold Draw | `cold_draw` | 10 | Ember Draw (`ember_draw`) |
| 4 | Brand Lesson | `brand_lesson` | 10 | Brand Mark (`brand_mark`) |
| 5 | Spark Lesson | `spark_lesson` | 12 | Spark Tithe (`spark_tithe`) |
| 6 | Echo Lesson | `echo_lesson` | 12 | Wake Echo (`wake_echo`) |

Pool-gated until OWNED (existing `HubOffers.skillUnlocked`).

---

## 6. PART C — bank commit (confirm for Engineer)

Kept formula **unchanged** (`trollkept-v0154.md`). Semantics:

| Rule | Lock |
|------|------|
| Payout write | On Run Summary enter / `finishRun`: compute Kept → `remnants_bank += kept`; award new trophy ids into unlocks |
| Continue | **Always enabled** after payout written (no gate that blocks Continue waiting on animation alone beyond the 2s trophy flash — payout already committed) |
| Menu **and** Continue | Both leave Summary only **after** bank + new trophy flags are committed |
| Invariant | Bank after leave **==** bank before sheet **+** Kept total shown on that sheet |
| Forbidden | **"Menu skips the bank"** — Menu must not bypass `addRemnants` / trophy unlock writes |

```
@ finishRun / Summary show:
  kept = formula(...)
  bank_before = remnants_bank
  remnants_bank += kept
  unlocks += new_trophy_ids
  show "Kept: N" + breakdown (+ trophy flash 2s if new)
@ Continue OR Menu from Summary:
  assert committed; clear mid-run; navigate
  // post-leave: remnants_bank == bank_before + kept
```

Hub buys still spend `remnants_bank` immediately (unchanged).

---

## 7. Explicit non-goals

- Retune Kept amounts, Hub prices, unlock ids, skill combat/glossary text
- FX / SFX
- F3 rumors / loadout lock / combat / Path Shop
- Selling Relics or showing unowned Relic rows with costs

---

## Checklist

- [ ] Sticky header: bust + Remnants N
- [ ] Sections Relics → Perks → Skills
- [ ] Relics owned-only; empty copy exact; earn lines exact
- [ ] Perks 5 + Skills 6; CTA unchanged
- [ ] Summary Menu + Continue both commit bank + trophies; Continue always after payout write

---

| Date | Change |
|------|--------|
| 2026-09-27 | Meta lock from CoS WO v0.1.55-hubsplit PART B |

**Related:** v0.1.56-bankone — Title/Hub bank getter + Summary commit txn. See `bankone-v0156.md`.

---

**Follow-on:** Scrap pouch + relic combat/summary effects + Forge Rest/Shop — [`forge-v0158.md`](forge-v0158.md).
