# Portrait stills — v0.1.21-portraits (WO Elliott)

Status: **design lock**. Three Elliott-attached **Grok stills** slotted **as-is**.  
Allowed edits only: **crop to frame** + **transparent backdrop**. **No** redraw, regenerate, recolor, or restyle.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

---

## Slots (Elliott stills)

| Portrait | Use | Replaces |
|----------|-----|----------|
| **You** | Title + combat player portrait | Prior You / volume bake in those slots |
| **Ash-Warden** | **F2 boss only** | Prior Ash-Warden portrait |
| **Seal-Warden** | **F1 boss only** | Orange blob / F1 boss placeholder |

Same **slot sizes** as existing portrait stages (do not change Compose layout sizes). If the still clips UI, **crop/shrink the still** — do not move HP / pips / frames.

---

## Stay placeholders

| Enemy | Art |
|-------|-----|
| Trash **Wretch** | Circle placeholder |
| Trash **Spinner** (Seal Spinner) | Circle placeholder |

No new trash stills this WO.

---

## Processing rules

1. Source = the three Elliott-attached Grok stills (authoritative).
2. **Crop** to the circular / existing portrait frame.
3. **Transparent** backdrop (cut solid BG only).
4. **Do not** redraw, regenerate, upscale-through-AI, or invent alternate poses.
5. Volume polish from `volume-v0120.md` is **not** re-applied as a separate restyle pass on these stills unless already baked into the attached file — treat the still as the art.

---

## Overlays (unchanged)

Frames, **status pips**, **HP bars**, Wake **crescent** / overlays stay **on top** of the portrait.

---

## Frozen

Wake art / Spark / glyphs map / 2x / save / pips **logic** / path / combat **math**.  
Bodies identity notes in `bodies-v0118.md` still describe intended reads; **this WO** is the art source for the three slots above (Seal-Warden is no longer a placeholder).

---

## Checklist

- [x] You still → title + combat (crop + transparent BG only)
- [x] Ash-Warden still → F2 boss only
- [x] Seal-Warden still → F1 boss only (orange blob gone)
- [x] Wretch + Spinner stay circle placeholders
- [x] Same slot sizes; overlays on top
- [x] No redraw / regenerate; Wake / glyphs / 2x / save / pips / path / math frozen

## Engineer wire (v0.1.21)

- `BodyArt`: `EnemyKind.DRAGON` → `portrait_seal_warden`; You / Ash unchanged names.
- `EnemySilhouette`: resolves ash vs seal drawable; trash stay Canvas.
- Title + combat You via `HeroShowcase` → `portrait_you`.
- Shrink via `SPRITE_INSET_FRACTION`; pips / HP / Wake overlays untouched.

---

**Related:** v0.1.22-plate — static combat plate fill (no pulse). See `plate-v0122.md`.

---

**Related:** v0.1.23-nobg — no combat plate behind stills. See `nobg-v0123.md`.

---

**Related:** v0.1.26-packs — hallway trash titles/looks (Weak Goblin / Sturdy Orc); still no plate. See `packs-v0126.md`.
