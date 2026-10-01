# Fight clock + special taps — v0.1.65-fightclock P1 (WO Elliott)

Status: **design lock** for Engineer.  
Builds on `specials-v0164.md`.

**Frozen this WO:** Remnants / Forge costs / music / Continue / loadout cap.  
**Out:** Pin / Hold. **No** log-well plates.  
**Assist OFF** never auto-fires (Wake / Grave Brand / Ash Vow).

**Scope:** Tower of Darkness greenfield only.

---

## A) Special taps

Applies to **Ashbrand** (Wake when full), **Grave Brand** (at 3/3), **Ash Vow** (unspent).

| Rule | Lock |
|------|------|
| When | Immediate while `phase == COMBAT` **and** that special is **ready** |
| Mid-FX | **QUEUE** the tap; **dump before next dice**; fire **after** current **300–600 ms** FX |
| Drop | **Never** drop a tap |
| Finger-down (ready) | Flash border + **80 ms** scale |
| Finger-down (not ready) | **Grey flash** only — **no** log, **no** spend |
| Hitbox | Full tile **+ 8 dp** |
| Ashbrand plate | Hitbox = **full Ashbrand row** |
| Debug | `TAP_SPECIAL wake\|grave\|vow accepted\|queued\|ignored` |

Assist OFF: tap paths only — **never** auto-fire (reinforces `specials-v0164.md`).

---

## B) Fight clock UI

Placement: **under Round N**, **left**, **always** visible.

### Format (exact shape)

`Cycle 2 · Beat 3/6 · You`

| Token | Lock |
|-------|------|
| **Cycle** | Increments when **regulars refresh** (3-regular cycle reset) |
| **Beat** | Actions this cycle: your regulars **+** foe **+** specials |
| **Y** in `Beat X/Y` | `Y = 3` (live regulars) **+** foe kit size; if that denominator is messy in UI, show **`Beat X` only** (omit `/Y`) |
| **Whose** | `You` / `Foe` / `Special` |

### Ready dots (same strip / adjacent — do not cover HP / Brace / names)

| Special | Display |
|---------|---------|
| Wake | `●○○` style charge (filled = pips toward full — match live Ashbrand pip count visually) |
| Grave Brand | `n/3` |
| Ash Vow | `ready` \| `spent` |

**Grey used regulars stay** (exhaust chrome unchanged).

No Pin / Hold. No log-well plates.

---

## C) Brace + Soften duration

| Rule | Lock |
|------|------|
| Duration | Brace and Soften **last the whole fight** (or until **consumed**) |
| Expiry | **Strip** “Clears at round end” / “If Brace remains at round end, clear it” **everywhere** — glossary, loadout, Forge copy |
| Gain | Brace **gain amounts unchanged** — only expiry rule changes |

### Glossary (EXACT)

| Term | Body |
|------|------|
| **Brace** | `Absorb damage before HP. Lasts the whole fight or until consumed.` |
| **Soften** | `Extra damage taken. Lasts the whole fight.` |

Iron Mantle / other card bodies: remove round-end clear clauses; keep grant numbers (`cards-v0.md`, `forge-choices-v0158.md`).

---

## D) Grave Brand / Ash Vow chrome (not Forge)

| Rule | Lock |
|------|------|
| Forge | Grave Brand and Ash Vow are **never** Forge II / III targets |
| Brand UI | **`0/3`–`3/3` only** (charge) |
| Vow UI | **`ready` \| `spent`**; spent = **grey** |

---

## Explicit non-goals

- Pin / Hold  
- Log-well plates  
- Remnants / Forge costs / music / Continue / loadout cap  
- Assist OFF auto-fire  

---

## Engineer checklist

- [ ] Special taps: COMBAT+ready immediate; mid-FX queue dump before next dice after 300–600 ms FX; never drop  
- [ ] Finger-down ready flash+80 ms scale; not-ready grey flash no log/spend  
- [ ] Hitbox tile+8 dp; Ashbrand = full row; debug `TAP_SPECIAL …`  
- [ ] Clock under Round N left: `Cycle N · Beat X/Y · Whose` (+ Beat X fallback); ready dots; no cover HP/Brace/names  
- [ ] Brace/Soften fight-long; strip round-end clear copy; glossary exact  
- [ ] L/M never Forge II/III; Brand n/3; Vow ready|spent grey  
- [ ] Assist OFF never auto-fires  
