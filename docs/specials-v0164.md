# Specials — v0.1.64-specials P1 (WO Elliott)

Status: **implemented** on `feat/0.1.64-specials` (v0.1.64-specials / vc 65).
Implement note: A–F landed; Remnants/Forge/music/FX/SFX/Continue untouched.
Status was: **design lock** for Engineer.  
**Already locked elsewhere this tag:** Remnants / Forge costs / music / FX / SFX / Continue.  
**Out:** Pin / Hold / Mark. **No second** Legendary or Mythic this tag.

**Scope:** Tower of Darkness greenfield only.

**Supersedes (combat only):** `ashbrand-v0133.md` “tap Ashbrand → glossary only” when **Assist OFF** and Wake is **full** — tap fires Wake (below). Glossary tap still OK when not firing.  
**Supersedes (bar):** `combat-bar-v013.md` “5 Ready in dice bag / reset when all 5 spent” — dice bag is the **3 regulars** only (L/M never in bag).

---

## A) Loadout

| Rule | Lock |
|------|------|
| Slots | **5 tiles + Ashbrand** (Ashbrand never a skill tile) |
| Composition | Exactly **3** regulars (`common` \| `uncommon` \| `rare`) **+ 1 Legendary + 1 Mythic** |
| Confirm | Enabled **only** when that composition is met |
| Else | Confirm **disabled** + reason line: `Need 3 regulars + 1 Legendary + 1 Mythic.` |
| Missing unlocks | If the player lacks Legendary **or** Mythic unlocks, **grant both** as **climb-available** for this slice — **not** Hub-priced |

This tag’s only L/M: **Grave Brand** (Legendary), **Ash Vow** (Mythic). No second L/M.

Regulars = existing common/uncommon/rare catalog skills (Hostflint… ember pool, etc.). Do not invent new regular names here.

---

## B) Dice bag

| Rule | Lock |
|------|------|
| In bag | Only the **3 live regulars** |
| Never in bag | Legendary, Mythic, Wake / Ashbrand |
| Cycle reset | When the **3 regulars** are spent → reset those **3** only |

Weighted pick / grey-out among unspent regulars unchanged in spirit — pool size is **3**, not 5.

---

## C) Wake (Ashbrand)

| Assist | Behavior |
|--------|----------|
| **ON** | When Wake is **full**, auto-fire on the **next** weapon beat (existing Assist-on / old behavior) |
| **OFF** | Player **taps Ashbrand plate** to fire when full |
| Tap not-full | **No-op** |
| Log | `Ashbrand Wake (tap)` or `Ashbrand Wake (assist)` |

Wake **math** / thresholds / damage arrays stay as locked in `wake-v014.md` / `combat-bar-v013.md` unless a later WO retunes — this WO only changes **trigger** (assist vs tap) + log suffix.

---

## D) Legendary — Grave Brand

| | Lock |
|--|------|
| Rarity | Legendary |
| Weight | **None** (never in dice bag) |
| Charge | **+1** when **your regular** skill **resolves**; **cap 3** |
| UI | Show **`1/3` … `3/3`** on the tile |
| At 3, Assist ON | Auto-fire on **next** eligible beat |
| At 3, Assist OFF | **Tap** to fire |
| Effect | Deal **12** + **Soften 2** |
| FX | Existing slash / stroke FX (no new clip this WO) |
| Untapped at 3 | Sits until fired or **fight end** (no forced spend) |

Log (recommended): `Grave Brand (tap)` / `Grave Brand (assist)` — mirror Wake suffix style.

Charge does **not** rise from L/M/Wake resolves — **regulars only**.

---

## E) Mythic — Ash Vow

| | Lock |
|--|------|
| Rarity | Mythic |
| Weight | **None** (never in dice bag) |
| Uses | **Once per fight** |
| Effect | Gain **Brace 4**; your **next regular damage** skill this fight deals **+4**; then **spent** |
| Assist ON | If still **unspent** at **start of cycle 2**, fire then |
| Assist OFF | **Tap only** (no auto) |
| Log | `Ash Vow (tap)` / `Ash Vow (assist)` |

“Cycle 2” = second pass of the **3-regular** exhaust cycle (after first full reset of the three regulars).

---

## F) Settings — Assist specials

| | Lock |
|--|------|
| Row | **Assist specials** **ON \| OFF** |
| Default | **ON** |
| Snapshot | Capture value at **fight start**; **no mid-fight** change |
| Stub | Stub title OK if the row works |

Applies to Wake Assist, Grave Brand auto-at-3, and Ash Vow cycle-2 auto (all read the same snapshot).

---

## Explicit non-goals

- Pin / Hold / Mark  
- Second Legendary or Mythic this tag  
- Remnants / Forge costs / music / FX / SFX / Continue retune  
- New regular skill names  

---

## Engineer checklist

- [x] Loadout Confirm only on 3 regular + 1 L + 1 M; reason line exact; grant L+M climb-available if missing unlocks  
- [x] Dice bag = 3 regulars only; cycle reset those 3; L/M/Wake never rolled  
- [x] Wake Assist ON auto / OFF tap-when-full; not-full no-op; log `(tap)`/`(assist)`  
- [x] Grave Brand: charge on regular resolve, cap 3, UI n/3, 12+Soften2, assist/tap, sits if untapped  
- [x] Ash Vow: once/fight, Brace 4 + next regular dmg +4, cycle-2 assist auto / tap if OFF  
- [x] Settings Assist specials ON/OFF default ON; snapshot at fight start  
- [x] No Pin/Hold/Mark; no second L/M  
