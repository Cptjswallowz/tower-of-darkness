# Ashbrand — v0.1.34-blade (WO Elliott)

Status: **feature tip** (tag gated — no release tag this pass).  
Painted portrait **still** in the weapon tile. **No rembg / knockout redraw.**  
Glossary body + Wake math **frozen.**

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

---

## What ships

| Item | Lock |
|------|------|
| Drawable | `app/src/main/res/drawable/ashbrand_icon.jpg` — same `R.drawable.ashbrand_icon` / `WakeArt.ICON_DRAWABLE` |
| Bytes | Stage portrait still md5 **`846af065acf75d5f08a08ca0bfd4173a`** (1408×1408 JPEG) |
| Rejected | v0.1.33 rembg knockout md5 `d0997c1572907bfb28efdbe15fd2bf63` (~14KB flat glyph) — **not** shipped |
| Visible | Engraved fuller + diamond pommel + charcoal bg / ember sparks (the attached still) |
| Slot | `WakeArt.ICON_SLOT_DP` = **48** (Loadout + combat); `ContentScale.Fit` |
| Tap | Ashbrand icon → `gc.showGlossary("ashbrand")` only — does **not** fire Wake / spend Sparks |
| Chrome | `volumeChrome(circular = false)` remains a no-op; no new glyph-plate chrome |

Art stage: `/workspace/tod-blade-v0134/` — portrait only at lock (`ashbrand_portrait.jpg`); no separate Art `ashbrand_icon.png` drop-in (md5 ≠ rejected).  
No new `docs/art-audio/` note from Art this pass.

---

## Glossary — Ashbrand (EXACT, frozen)

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

---

## Freeze (do not change)

- Glossary body `ASHBRAND_BODY` (exact)
- Wake math / pip count / threshold / full+spark damage arrays (`WeaponCatalog.ashbrand`)
- Hub / path / enemy kits (size 3) / 2x
- Portraits of You / goblin / orc / Wardens
- `ashbrand_spark.png` and `wake_vfx_{charge,slash,impact}` (hashes unchanged)
- Enemy tiles + player skills

---

## Explicit non-goals

- No rembg / knockout / silhouette redraw of the still
- No QA gate / README release section this tip
- No git tag / gh release
