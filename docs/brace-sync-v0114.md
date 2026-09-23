# Brace pip draw sync — v0.1.14-bracesync (WO Elliott)

Status: **implemented** (v0.1.14-bracesync). Presentation / draw order only.  
Builds on `status-pips-v0113.md`. **No** absorb math, stack, or clear-rule changes.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

---

## On hit (player takes damage while Brace can absorb)

Draw order for that resolving beat:

1. **Pip count** updates / appears to the post-absorb remaining Brace.
2. **Float** on the Brace pip: absorbed amount as `−N`.
3. **Then** the player **HP bar** moves by leftover damage only (after Brace).

Do not animate HP first then “catch up” the pip.

---

## Same-beat skill vs hit (resolver order unchanged)

| Log / resolve order | Brace vs hit |
|---------------------|--------------|
| Shield / Brace skill **before** the hit in the log | Brace applies **before** the hit (existing resolver) |
| Skill **after** the hit in the log | Leave that order — **do not** invent a pre-block |

This WO does not reorder combat resolution; it only syncs pip / float / HP **draw** to the existing resolve sequence.

---

## Brace reaches 0 on a hit

- For the **resolving beat**, show the pip at **“0”** (still visible) so the absorb float can land.
- **Hide** the Brace pip on the **next** beat (same hide-at-0 rule as v0.1.13, delayed one beat after a zeroing hit).

---

## Soften

**Unchanged.** Soften enemy pip keeps current v0.1.13 behavior / timing. Do **not** force Soften onto this Brace draw-order pattern unless a later WO requires it.

---

## Frozen

- Absorb / Brace clear rules (`cards-v0.md` / combat resolver)
- Wake math
- 2x timings (`combat-2x-v0111.md`)
- Mid-run save (`save-v0112.md` / `midrun-save-v0112.md`)

---

## Checklist

- [x] On hit: pip update → float `−N` on pip → then HP bar leftover
- [x] Same-beat: honor existing log/resolver order; no invented pre-block
- [x] Brace 0: show “0” this beat; hide next beat
- [ ] Soften unchanged; frozen systems untouched
