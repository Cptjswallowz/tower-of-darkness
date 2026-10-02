# Wake tap split — v0.1.66-waketap P1

Status: **implemented** on `feat/0.1.66-waketap`.  
Builds on `fightclock-v0165.md`. CoS WO authoritative until Architect expands.

**Frozen:** Remnants / Forge costs / music / Continue / loadout. Wake costs 3. Assist OFF never auto. No log-well art.

## Phone FAIL on 0.65 (fixed)

1. Ashbrand **short-tap opened glossary** — nested / leftover glossary clickable.  
2. Wake **never dumped at 3 sparks** — short tap did not reliably fire dump.  
3. **Cycle/Beat line missing** under Round N — clock strip hardened (brighter, always composed, semantics).

## A) Gesture split (Ashbrand + Grave Brand + Ash Vow)

| Gesture | Lock |
|---------|------|
| **SHORT TAP** | Dump if ready (queue mid-stroke); **NEVER** open dialog |
| **LONG-PRESS 350ms** | Glossary; combat **keeps running**; close does **not** spend |
| **Not ready** | Short tap **flash only** — no glossary, no spend |

Debug: `TAP_SPECIAL wake\|grave\|vow accepted\|queued\|ignored` ; `LONG_GLOSSARY ashbrand` (grave/vow analogous).

## B) Fight clock

Under Round N, left, always: `Cycle · Beat · You|Foe|Special` + Wake/Brand/Vow ready. Does not cover HP/names.

## C) Brace/Soften

Fight-long (unchanged from 0.65). Log `BRACE_EXPIRE fight` when fight ends.
