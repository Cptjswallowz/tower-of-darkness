# Combat bar v0.1.3 — design lock

Status: **design lock** for Engineer (aligned to Elliott work order).  
Fog / rumor rules: **unchanged** (see `rumors.md`).  
Floor generator + rumor visibility: **not changed this pass**.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

No playtest or sim numbers below are measured — figures are **design targets** only.

---

## Intent

**5-slot exhaust bar** + standing weapon **Ashbrand** (CHAIN + SPARK / Wake). Loadout size is **exactly 5** (no sixth skill). While slots are live, fire pick is **weighted among UNSPENT only** — not strict L→R. Player does not tap mid-fight; bar auto-resolves on the beat clock. End of fight requires **Continue**.

---

## Loadout / bar cap

| Rule | Lock |
|------|------|
| Loadout size | **Exactly 5** skills |
| Sixth skill | **Forbidden** (UI + domain reject) |
| `loadout_flex` (Hub perk) | **Deferred** — do not sell / wire this pass |
| Duplicates on bar | Not allowed |
| Weapon | **Ashbrand** — never a 6th skill slot; always present in combat |

Pool / card table: `cards-v0.md`. Default Skip loadout: cards **1–5**.

---

## 5-cap exhaust cycle + weighted fire

1. Fight start: all 5 bar skills = **Ready** (unspent).
2. On each **skill beat**, pick **one** skill by **weight among UNSPENT (Ready) only** — normalize weights over the live pool (`cards-v0.md` Weight column). Fire it; it becomes **Exhausted** (greyed; out of pool).
3. Exhausted skills cannot be picked until cycle reset.
4. When **all 5** are Exhausted → **cycle reset**: all return to Ready. Repeat until fight ends.
5. Effect classes still apply on fire; Brace absorbs before HP.

**Do not** use strict left→right order for skill pick.

---

## Beat timing table (design targets — Elliott work order)

Timings are **design targets for pacing**, not measured playtest.

| Beat | Duration (design) | What resolves |
|------|-------------------|---------------|
| Dice / open | **~800 ms** | Show bar (5 Ready) + Ashbrand idle; dice/read-in stub |
| Skill | **~1700–2300 ms** | Weight-pick among unspent; fire; grey exhaust; apply effect |
| Read hold | **~1800 ms** | Hold so player can read outcome |
| Weapon | **~1500 ms** | Ashbrand CHAIN threshold check and/or SPARK roll → Wake if any |
| Enemy | **~1500 ms** | Enemy counter (normal 7–11 / boss 8–12 per `balance-targets.md`) |
| Cycle reset | Immediate when 5th skill spends (before next skill beat) | All 5 → Ready; 1x stub flash OK |
| End gate | On player or enemy HP ≤ 0 | Stop beats; show result; **Continue** required |

If a skill or Wake ends the fight, skip remaining beats → end gate.

**1x stub only:** a single shared VFX/SFX stub may stand in for Wake (full/half), exhaust grey, and cycle reset this pass.

---

## Weapon — Ashbrand (CHAIN + SPARK)

Ashbrand is the Ashen Host’s standing weapon. **Not** loadout-editable. **Never** occupies a bar slot.

Track: `charge` (integer) and `level` (1 / 2 / 3). Slice default **level = 1** unless meta unlock later; do not invent unlock costs here.

### CHAIN (charge → mandatory full Wake)

- Start of fight: `charge = 0`.
- After a skill resolves:
  - **Attack** skills (deal damage to enemy as part of effect): `charge += 1`.
  - **Brace / heal** (no attack damage component): `charge += 0` (do not break charge).
- When `charge ≥ threshold(level)` → **MUST** fire **Wake at FULL power**, then `charge = 0`.

| Level | CHAIN threshold | Full Wake damage |
|-------|-----------------|------------------|
| 1 | **4** | **4** |
| 2 | **3** | **6** |
| 3 | **2** | **8** |

### SPARK (per player round — half Wake, no charge reset)

- Once per **player round** (after skill resolve, during weapon beat): roll SPARK chance  
  **`8% + 4% × level`**  
  (lv1 = 12%, lv2 = 16%, lv3 = 20%).
- On success: fire **Wake at HALF power**. **Does not** reset `charge`.

| Level | Half Wake damage |
|-------|------------------|
| 1 | **2** |
| 2 | **3** |
| 3 | **4** |

### Same weapon beat

- If CHAIN threshold is met: full Wake is **mandatory** this weapon beat (`charge = 0`).
- SPARK roll still runs this weapon beat per chance above; half Wake **does not** reset charge.
- If both fire the same beat: resolve **full Wake first**, then half Wake.

### Classification for +1 charge

- **+1 (attack):** skill deals enemy damage (e.g. Hostflint, Emberbrand, Ruin Seal, Tower Pike, damaging MoveEffects).
- **+0 (Brace/heal):** Iron Mantle, Vow Plate, Relic Shard heal branch, pure Brace lines; Ash Press counts as **attack** if it deals its 4 damage (heal is extra) → **+1**.

---

## Continue on end

- Win or loss: outcome summary + **Continue** button.
- **No** auto-advance past the end gate.
- Continue → non-boss win → Path; boss win / any loss → Run Summary (`slice-screens.md`).

Flee remains **grayed** (CoS temporary lock).

---

## Explicit non-goals this pass

- Floor / path **generator**: **unchanged**.
- Rumor fog **visibility** / text rules: **unchanged** (`rumors.md`).
- No new playtest win-rate claims; do not retune ~84% / ~3.6 here.
- No sixth loadout skill; no `loadout_flex`.
- No strict L→R skill pick (weights among unspent only).

---

## Engineer checklist

- [ ] `LOADOUT_MIN = LOADOUT_MAX = 5`
- [ ] Weight pick among **unspent only**; grey exhaust; reset when all 5 spent
- [ ] Ashbrand: CHAIN thresholds 4/3/2 → full Wake 4/6/8; SPARK 8%+4%×lv → half Wake 2/3/4 without charge reset
- [ ] Beat stubs: ~800 / ~1700–2300 / ~1800 / ~1500 / ~1500 (order preserved)
- [ ] Continue CTA on end
- [ ] 1x shared stub only
- [ ] Hub: hide or disable `loadout_flex` (deferred)
