# Brace absorb float — v0.1.14-bracesync (Art note)
**Fidelity:** PLACEHOLDER / Compose Text+glyph OK — **no new bitmap required**  
**Tag:** v0.1.14-bracesync  
**Audience:** Android Engineer (via Chief of Staff)  
**Scope:** Brace pip only — do **not** redesign Soften unless Engineer asks for shared float treatment.

## Absorb float (on Brace pip)
When a hit is **absorbed** by Brace:
1. Float **`−N`** (e.g. `−2`) **on/near the Brace pip** (shield glyph + number from v0.1.13).
2. Then the **HP bar** updates / moves.

Read order: pip float first → HP second. Keep the float short (~300–500 ms fade/rise).

## Style
- Same painterly-simple / dark skill-card language as `STATUS_PIPS_v0.1.13.md`.
- Bone or ember-tint on the `−N` text for absorb read (not soft red HP-damage float — this is blocked damage).
- Compose `Text` + existing shield glyph fine.

## Zero pip beat
When Brace stacks hit **0**: keep pip visible showing **`0`** for that beat, then **hide**.

## Out of scope
- Soften pip / Soften floats — unchanged unless Engineer requests shared float treatment.
- BLOCK/FINAL art — still held. No ToW/Eldermark assets.
