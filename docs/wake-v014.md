# Ashbrand Wake v0.1.4 — design lock

Status: **design lock** (docs only). Extends `combat-bar-v013.md`.  
Fog / rumor / floor generator: **unchanged**.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

Figures below are **design locks / targets**, not measured playtest.

---

## What changes in v0.1.4

| Item | Lock |
|------|------|
| FULL Wake presentation | **Legendary beat** — distinct from normal weapon pulse |
| Combat log (FULL only) | Gold line: `ASHBRAND — WAKE N` + **pin** (stay visible / pinned in log) |
| SPARK (half Wake) | **Quiet** — no legendary beat, no gold pin line (subtle / stub OK) |
| Lv1 CHAIN threshold | **3** (was 4 in v0.1.3) |
| Trash (normal) enemy counter | **5–8** |
| Boss | **Untouched** — HP **28**, counter **6–9** (*CoS temporary lock*, Elliott PICK C) |

Full / half Wake **damage by level** and SPARK **chance** remain as in `combat-bar-v013.md` (full 4/6/8, half 2/3/4, chance `8%+4%×level`), except Lv1 threshold above.

---

**v0.1.15 art lock:** `wake-art-v0115.md` (icon + Full Wake crescent sequence; SPARK ember-only).

## FULL Wake — legendary beat

When CHAIN forces a **FULL** Wake:

1. Insert a **legendary weapon beat** (can reuse ~1500 ms weapon budget or slightly longer stub — order still before enemy counter).
2. Apply full Wake damage for current Ashbrand level.
3. Combat log: append gold text **`ASHBRAND — WAKE N`** where `N` is the Wake ordinal this fight (1, 2, 3…).
4. **Pin** that log line (or the latest FULL Wake line) so it stays readable through later beats.
5. Set `charge = 0` (unchanged CHAIN rule).

**1x stub only** for legendary VFX/SFX this pass (shared stub OK).

---

## SPARK — quiet half Wake

- SPARK half Wake: **no** legendary beat, **no** gold `ASHBRAND — WAKE N` pin.
- Resolve damage quietly during the normal weapon beat (or a short quiet pulse).
- Does **not** reset charge (unchanged).

---

## CHAIN thresholds (v0.1.4)

| Level | Threshold | Full Wake dmg | Half Wake dmg |
|-------|-----------|---------------|---------------|
| 1 | **3** | 4 | 2 |
| 2 | 3 | 6 | 3 |
| 3 | 2 | 8 | 4 |

Attack skills +1 charge; Brace/heal +0 — unchanged from v0.1.3.

---

## Enemy counters (slice)

| Foe | Counter band | HP |
|----|--------------|-----|
| Trash / normal | **5–8** | 20 |
| Boss | **6–9** (locked) | **28** (locked) |

No trash HP retune this pass. Optional later: no-counter on 0-dmg skill rounds — **not** in this lock unless CoS re-opens.

Authoritative table also in `balance-targets.md`.

---

## Explicit non-goals

- Fog / rumor visibility: unchanged.
- Floor generator: unchanged.
- Boss counter/HP: do not change.
- No invented win-rate claims.

---

## Engineer checklist

- [ ] FULL Wake → legendary beat + gold log `ASHBRAND — WAKE N` + pin
- [ ] SPARK half Wake quiet (no gold pin)
- [ ] Lv1 CHAIN thresh = **3**
- [ ] `ENEMY_COUNTER` trash = **5–8**; boss counter **6–9**, boss HP **28**
- [ ] Fog unchanged
