# Hallway trash packs — v0.1.26-packs (WO Elliott)

Status: **implemented** (engineer). Hallway combat **titles + looks + floor weights** only.  
**No** balance retune beyond the stated hallway spawn weights. HP / counters / Wake / bosses unchanged.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

---

## Titles (UI + combat log)

| Display title | Role |
|---------------|------|
| **Weak Goblin** | Lighter hallway trash pack |
| **Sturdy Orc** | Heavier hallway trash pack |

Old internal ids (e.g. `GOBLIN`, ash_wretch, seal_spinner, etc.) may remain in code — **UI and combat log** use the titles above.

---

## Looks

| Pack | Looks | Roll |
|------|-------|------|
| Weak Goblin | **3** look variants | Equal **1/3** each |
| Sturdy Orc | **3** look variants | Equal **1/3** each |

- Roll look on **combat node create** (when the hallway fight is authored / entered into the run graph).
- **Persist** the chosen look through mid-fight and mid-run **save / resume**.
- **Re-roll** look on the **next** hallway fight (new node create) — do not sticky one look across the whole climb unless it is the same node resumed.
- Art: PNG (or placeholder) **with no plate** behind (`nobg-v0123.md`).

Bosses (**Seal-Warden**, **Ash-Warden**) and **You** are out of this pack system.

---

## Hallway spawn weights (floor only)

Applies to **hallway / normal combat nodes only**. **Bosses never** roll these packs. **No Floor 3** invent.

| Floor | Weak Goblin | Sturdy Orc |
|-------|-------------|------------|
| **F1** | **75%** | **25%** |
| **F2** | **30%** | **70%** |

This **supersedes** the older F2 “prefer spider/troll over goblin” note in `floor2-v019.md` for **hallway pack identity / weights**. Floor 2 trash **HP 24** and counter bands stay as locked elsewhere — not retuned here.

---

## Frozen

- Trash / boss **HP** and **counters**
- Wake / SPARK / CHAIN
- Bosses (Seal-Warden, Ash-Warden) + **You** portraits
- Glyphs / plates (removed) / path generation math / 2x
- Rumor / Scout **charge** rules (`rumorcharge-v0124.md`)

---

## Engineer / QA checklist

- [x] Combat log + node UI show **Weak Goblin** / **Sturdy Orc** (old ids OK internally)
- [x] 3 looks each; equal 1/3 on node create; persist mid-fight + save; re-roll next hallway fight
- [x] No plate behind PNG (hooks ready; Art PNG drop pending)
- [x] F1 hallway 75% goblin / 25% orc; F2 30% / 70%; bosses never; no Floor 3
- [x] No HP / counter / Wake / boss / You / glyph / path / 2x / rumor-charge changes


---

## Drawable hooks (Art drop)

Expected basenames under `app/src/main/res/drawable/` (PNG, no plate):

| File | Look |
|------|------|
| `pack_weak_goblin_knife.png` | Weak Goblin / knife |
| `pack_weak_goblin_bottle.png` | Weak Goblin / bottle |
| `pack_weak_goblin_spikes.png` | Weak Goblin / spikes |
| `pack_sturdy_orc_axe.png` | Sturdy Orc / axe |
| `pack_sturdy_orc_cleaver.png` | Sturdy Orc / cleaver |
| `pack_sturdy_orc_hammer.png` | Sturdy Orc / hammer |

Engineer wires via `HallwayPacks.drawableName` + `getIdentifier` when files land. Do **not** invent art from JPG stills.
