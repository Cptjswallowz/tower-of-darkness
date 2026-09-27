# Forge + scrap + relic effects — v0.1.58-forge (Meta)

Status: **Meta & Ops design lock**. Owner: Meta & Ops.  
Baseline tip: `5810af12830bfd14eb445b85454a9eb8907f6857` (v0.1.57-titlebank).

**Frozen:** `remnants_bank` / Title counter binding, combat FX/SFX, Hub section layout, Kept payout table **except** new Gate Sigil **+1** line (PART A).

**Scope:** Tower of Darkness greenfield only.  
**Implements from:** Engineer (+ Architect for any choice copy — **no PART D names invented here**).

**Related:** `trollkept-v0154.md` (trophies/Kept), `hubsplit-v0155.md` (Relics UI), `bankone-v0156.md` / `titlebank-v0157.md` (bank ≠ purse), `hubmore-v0129.md` (Warm Ash brace), `midrun-save-v0112.md` (climb slot).

---

## PART A — Relic effects

Owned check: trophy id ∈ meta `unlocked_cards` at the stated moment.  
Overlays stay **Title + Hub only**. Combat bust **clean** (no trophy overlay stack in combat).

| Id | Name | When | Effect |
|----|------|------|--------|
| `soot_rim` | Soot Rim | Climb start | `scrap_goblin += 1` (after pouch zeros) |
| `ash_pauldron` | Ash Pauldron | Climb start | `pendingStartBrace += 1` (**stacks** Warm Ash) |
| `troll_tooth` | Troll Tooth | Combat | First **damaging** hit each **floor** vs **elite OR boss**: **+2** damage once |
| `gate_sigil` | Gate Sigil | Summary (win **or** death) | Kept **+1**; extra line exact: **`Gate Sigil +1`** |

### Brace stack (climb start)

```
pendingStartBrace =
    (warm_ash in unlocks ? 2 : 0)     // hubmore WARM_ASH_BRACE
  + (ash_pauldron in unlocks ? 1 : 0)
```

Apply once into first combat brace (existing Warm Ash hook); clear pending after apply. Mid-run resume: do **not** re-grant (same as Warm Ash).

### Troll Tooth

| Rule | Lock |
|------|------|
| Target | Elite **or** boss this fight |
| Elite (today) | **Cave Troll** (`EnemyKind.CAVE_TROLL` / hallway elite) |
| Boss | Floor boss (`isBoss`): Seal-Warden, Ash-Warden, Gate-Warden |
| Trigger | First hit that deals **>0** damage to that enemy this floor |
| Once | Per floor; reset on floor advance / new floor path |
| Mid-run | Persist spent flag for current floor |

### Gate Sigil Kept line

| Rule | Lock |
|------|------|
| Amount | **+1** |
| Label | `Gate Sigil` (Summary shows `Gate Sigil +1` / `Gate Sigil  +1` consistent with other lines) |
| When | Win **or** death |
| Owned | `gate_sigil` ∈ unlocks at payout **OR** newly earned this climb (`gateWardenBeaten` / new trophy) so first clear gets the line |
| Other Kept | Unchanged (F1+1 / F2+2 / F3+3 / Troll+1 / Gate-Warden+5 / seal breaks+3 victory-only / Ash Tithe+3) |

```
# Additive only into remnants_bank on Continue|Menu commit (existing)
kept += 1  if gate_sigil owned/earned
```

---

## PART B — Scrap (run-only)

| Key | Role |
|-----|------|
| `scrap_goblin` | Goblin scrap count this climb |
| `scrap_orc` | Orc scrap count this climb |

| Rule | Lock |
|------|------|
| Climb start | Both **0**, **then** Soot Rim may `scrap_goblin += 1` |
| Bank | **NEVER** add scrap into `remnants_bank` |
| Purse | Separate (`run_wallet`); scrap ≠ purse ≠ bank |
| MetaStore | **No** scrap prefs — run / mid-run only |
| Summary clear | Scrap discarded with climb (not banked) |

### Drops (on kill / resolve)

| Source | Scrap |
|--------|-------|
| Weak Goblin | **+1g** 80% / **+2g** 20% |
| Sturdy Orc | **+1o +1g** |
| Cave Troll / elite | **+2o +2g** |
| Gate-Warden / floor boss | **+2o** |
| Treasure (scrap roll) | **50%** +2g / **30%** +1o / **20%** +1g+1o |

Treasure **choice wording** = Architect (do not invent names here). Existing purse remnant take unchanged unless a later Architect WO says otherwise; when scrap is granted from treasure, use the table above.

### Shop piles (when shop generates)

| Offer title | Cost | Grant | Stock |
|-------------|------|-------|-------|
| `Goblin pile` | **8** purse | **+3g** | **One** per shop generation |
| `Orc pile` | **14** purse | **+2o** | **One** per shop generation |

Spend purse (`run_wallet`); grant scrap. Still never bank scrap.

### HUD

Exact floor HUD form:

```
HP N · purse N · gN oN
```

Path / FloorBreak / Shop (and Rest if HP line shown): include `g` / `o` scrap. No `rem` / `remnants` on purse (bankone).

---

## PART C — Forge (Rest + Shop only)

| Rule | Lock |
|------|------|
| Surfaces | **Rest** and **Shop** only (not Path, not combat, not Hub) |
| Targets | The **5 loadout skills** only |
| Forbidden | Deck cards outside loadout; **Ashbrand** / weapon; Hub meta skills list as forge targets |
| Levels | Run-scoped; start **Lv1**; max **Lv3** |
| Lv1 → Lv2 | **3g** |
| Lv2 → Lv3 | **2g + 2o** |
| Persist MetaStore | **No** scrap, **no** forge levels in meta |
| Mid-run | Persist scrap + per-skill forge level in `midrun_v0112` so Continue works; clear with slot |
| Level power | **Not** Meta this WO — Architect / combat owns what Lv2/Lv3 *do*; Meta locks costs + eligibility + surfaces |

```
forge(skill):
  require skill in current 5 loadout
  Lv1→2: scrap_goblin >= 3 → spend 3g → level 2
  Lv2→3: scrap_goblin >= 2 && scrap_orc >= 2 → spend 2g+2o → level 3
  else: cannot forge
```

### Explicit non-goals (PART C)

- Invent Rest/Shop **choice button names** / flavor (Architect)
- Forge on Event nodes
- Persist forge levels across climbs
- Retune Warm Ash amount (still 2); Pauldron only adds +1

---

## Mid-run additive keys (Engineer)

| Key | Type | Notes |
|-----|------|-------|
| `scrap_goblin` | int | Run pouch |
| `scrap_orc` | int | Run pouch |
| `forge_levels` | map `card_id → 1..3` | Loadout skills only; default missing = 1 |
| `troll_tooth_floor` | int or flag | Floor index last tooth proc / spent this floor |

Pointer: extend `midrun-save-v0112.md` changelog when wired. **No** new MetaStore bank keys.

---

## State machine (climb)

```
New Climb:
  scrap_g = 0; scrap_o = 0; forge_levels clear (all Lv1)
  if soot_rim: scrap_g += 1
  pendingStartBrace = warm_ash?2:0 + ash_pauldron?1:0
  // remnants_bank untouched

Combat kill → apply drop table
Treasure scrap path → roll treasure table
Shop gen → include Goblin pile + Orc pile (one each)
Rest|Shop → Forge UI (loadout skills, costs above)

Floor advance → reset Troll Tooth once-flag

finishRun:
  Kept lines += Gate Sigil +1 if owned/earned
  // scrap NOT added to kept / bank
leave Summary:
  bank += kept (existing txn); clear mid-run (scrap/levels die)
```

---

## Explicit non-goals

- PART D / Event / Rest choice **names** (Architect)
- Change Title/`remnants_bank` binding, Hub Relics/Perks/Skills chrome, combat FX/SFX
- Retune Kept amounts other than Gate Sigil +1
- Bank scrap or leftover purse

---

## Checklist

- [ ] Relic effects: Soot +1g start; Pauldron +1 brace stacks Warm Ash; Tooth +2 once/floor elite|boss; Gate Sigil Kept +1 line
- [ ] Combat bust no trophy overlays; Title/Hub overlays unchanged
- [ ] Scrap pouch run-only; drop + shop pile tables; HUD `HP N · purse N · gN oN`
- [ ] Forge Rest+Shop; 5 loadout skills; costs 3g / 2g+2o; no MetaStore scrap/levels
- [ ] No invented PART D choice names

---

| Date | Change |
|------|--------|
| 2026-09-27 | Meta lock from CoS WO v0.1.58-forge |
