# Ashbrand — v0.1.33-ashbrand (WO Elliott)

Status: **feature tip** (tag gated — no release tag this pass).  
Icon crop-only + tap → glossary. **Wake math frozen.**

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

---

## What ships

| Item | Lock |
|------|------|
| Drawable | Drop-in `app/src/main/res/drawable/ashbrand_icon.png` (Art crop; same `R.drawable.ashbrand_icon`) |
| Size | 192×192 RGBA transparent — **no** plate / circle chrome baked into the bitmap |
| Slot | `WakeArt.ICON_SLOT_DP` = **48** (Loadout weapon row + combat Ashbrand tile) |
| Tap | Ashbrand icon → `gc.showGlossary("ashbrand")` — glossary only; **does not** fire Wake or spend Sparks |
| Nested terms | In Ashbrand sheet (and combat log): **Ember**, **Spark**, **Wake** → glossary defs |
| Dialog | `GlossaryDialog` body uses `GlossaryText`; nested onTerm → `showGlossary`; fight pauses while open |

Art notes: `docs/art-audio/ASHBRAND_v0.1.33.md`. Stage: `/workspace/tod-ashbrand-v0133/`.

---

## Glossary — Ashbrand (EXACT)

Title: **Ashbrand**

```
Ember weapon. Each damaging skill
you resolve adds 1 Spark pip.
At 3 Sparks the next beat can
fire Wake — a heavy slash.
Wake spends the pips. A combat
win plus a Wake this fight
raises Ashbrand 1 level.
Cinder Vow checks for ≥1 pip.
```

Short linked defs (no new math):

| Key | Def |
|-----|-----|
| ember | Ember weapon nature / tag |
| spark | Spark pip toward Wake / quiet half-Wake |
| wake | Heavy slash that spends Spark pips |

---

## Freeze (do not change)

- Wake math / pip count / threshold / full+spark damage arrays (`WeaponCatalog.ashbrand`)
- Hub / path / enemy kits / 2x
- Portraits of You / goblin / orc / Wardens
- `ashbrand_spark.png` and `wake_vfx_{charge,slash,impact}` (hashes unchanged)
- Enemy tiles + player skills

---

## Explicit non-goals

- No QA gate / README release section this tip
- No git tag / gh release
- No invent crop — Art drop-in only
