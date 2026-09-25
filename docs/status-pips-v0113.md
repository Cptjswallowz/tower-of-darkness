# Status pips — v0.1.13-statuspips (WO Elliott)

Status: **design lock** for this WO. Combat UI only.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.  
No damage / stack / clear-timing rule changes — pips **mirror** existing combat state.

---

## Intent

Under each fighter’s HP bar, show compact **status pips** for combat values the player already has, so Brace / Soften are readable without hunting the log.

---

## Pips (this pass only)

| Pip | Fighter | Shows | Source state (existing) |
|-----|---------|-------|-------------------------|
| **Brace** | Player | Absorb **left** | Current `brace` (absorb before HP) |
| **Soften** | Enemy | Remaining **counter reduce** | Current Soften / `counterPenalty` (remaining reduce on enemy counter) |

- **Hide at 0** — no empty pip (Brace zeroing-hit exception: show “0” for that beat only — `brace-sync-v0114.md`).
- Value on the pip = current remaining amount (not a history total).
- **No new statuses** this pass unless already present in combat state. Do not invent Stun/Freeze pips unless those values are live on the fighter this fight.

---

## Interaction

- **Tap** a visible pip → open **glossary** for that term (`brace` / `soften`).
- Glossary copy must exist for both terms. Soften if missing: remaining enemy counter reduction from Soften effects (exact wording can match live glossary once added).
- Tap does **not** pause the beat clock beyond existing UI modal behavior; no new combat control.

---

## Placement

- Player: Brace pip(s) **under player HP bar**.
- Enemy: Soften pip(s) **under enemy HP bar**.
- Presentation: stub icon + number OK this pass (Art may swap art later).

---

**Brace draw sync (v0.1.14):** on-hit pip → float → HP order — see `brace-sync-v0114.md`.

## Explicit non-goals

- No new status effects or stack rules.
- No change to Brace clear-at-round-end, absorb order, Soften math, Wake, or damage.
- No save of pip UI; combat state already drives values.

---

## Frozen (do not change this pass)

- Mid-run save (`save-v0112.md` / `midrun-save-v0112.md`)
- 2x timings (`combat-2x-v0111.md`)
- Path / rest-before-boss (`bossrest-v0110.md`)
- Wake math / counters / shop / swap

---

## Engineer checklist

- [ ] Brace pip under player HP when brace > 0; hide at 0
- [ ] Soften pip under enemy HP when Soften remaining > 0; hide at 0
- [ ] Tap → glossary; Soften glossary entry present
- [ ] Values mirror existing combat state only; no rule retunes
- [ ] Leave frozen systems untouched

---

**Superseded (Soften target when kits live):** v0.1.32-enemykit — Soften reduces next damaging **enemy kit skill** (not Hide/Rust Guard/Cinder Hide); Nip ignores Soften. See `enemykit-v0132.md`.
