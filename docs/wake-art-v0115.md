# Wake presentation art — v0.1.15-wakeart (WO Elliott)

Status: **design lock**. Presentation only — **no** Wake math / threshold / damage changes.  
**v0.1.16 polish + freeze:** `art-audio/WAKE_ART_v0.1.16.md`.  
Builds on `wake-v014.md` (FULL legendary + SPARK quiet) and `combat-2x-v0111.md` (rate).

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

---

## Ashbrand icon

| Rule | Lock |
|------|------|
| Look | Cracked **ash-iron longsword** with a **gold fuller seam** |
| Slot | Replaces the empty Ashbrand plate; **same slot size** as today’s weapon icon |
| Common skills | Bodies stay **placeholders**; **no** crescent on common skill fires |

No new SFX pack this pass (reuse existing stubs).

---

## Full Wake only (legendary beat)

Sequence on FULL Wake (CHAIN mandatory wake):

1. **Charge** — gold fuller seam brightens on the Ashbrand icon.
2. **Crack** — gold crack line reads on the blade.
3. **Crescent** — gold crescent sweeps **L→R** over the portrait stage, with existing combat log **`ASHBRAND — WAKE N`** (pinned) + **WAKE** float.
4. **Hold** — **1x ≈ 2300 ms** / **2x ≈ 1150 ms** (half of 1x per `combat-2x-v0111.md`).
5. **Clear** — crescent / charge read clears; return to idle Ashbrand icon.

Do not skip steps; 2x shortens holds only (no skip-animations).

---

## SPARK (half Wake)

- **Ember** pulse on the Ashbrand icon only.
- **No crescent.** No legendary gold pin change beyond existing quiet SPARK rules (`wake-v014.md`).

---

## Frozen

- Wake math / CHAIN thresholds / SPARK chance / damage
- Mid-run save
- Status pips / Brace draw sync
- Path / rest-before-boss / counters / shop / swap

---

## Checklist

- [x] Ashbrand icon = cracked ash-iron longsword + gold fuller; same slot size
- [x] Full Wake: charge → crack → crescent L→R + log/float → hold 1x~2300 / 2x~1150 → clear
- [x] SPARK: ember on icon only; no crescent
- [x] Common skills: no crescent; placeholder bodies; no new SFX pack
- [x] No Wake math changes; frozen systems untouched
