# Combat bodies — v0.1.18-bodies (WO Elliott)

Status: **design lock**. Portrait art only for **You** + **Ash-Warden**.  
**No** wardrobe system, poses, or hit-flash.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

---

## You (player)

| Rule | Lock |
|------|------|
| Read | Fallen-kingdom soldier: **ash cloak**, **dull steel**, **helmet slit**, **Ashbrand sheathed** |
| Frame | **Circular portrait** (existing portrait stage) |
| Scope | Replaces current player placeholder body/portrait |

---

## Ash-Warden (F2 boss only)

| Rule | Lock |
|------|------|
| Read | **Coal / ember** body + **chest seal**; keep existing silhouette |
| Weight | **Heavier** than You (mass/armor read) |
| Scope | **Floor 2 boss only** |

---

## Out of scope this pass (placeholders stay)

- Trash enemies
- Seal Spinner (if present as stub)
- **F1 Seal-Warden** boss portrait

Do not invent wardrobe slots, pose sets, or hit-flash VFX.

---

## Layering / clips

| Rule | Lock |
|------|------|
| On top | Frames, **status pips**, **HP bars**, Wake **crescent** / overlay stay **above** the sprite |
| Clip | If the sprite clips UI, **shrink the sprite** — **do not** move pips / HP / frames |

---

## Frozen systems

- Wake art (icon / crescent / speck / SPARK) — frozen
- 2x holds / save / pips **logic** / path / combat **math**

---

## Checklist

- [x] You: fallen-kingdom soldier (ash cloak, dull steel, helmet slit, Ashbrand sheathed); circular portrait
- [x] Ash-Warden: F2 only; coal/ember + chest seal; keep silhouette; heavier than You
- [x] Trash / Seal Spinner / F1 Seal-Warden stay placeholders
- [x] No wardrobe / poses / hit-flash; shrink sprite on clip, don’t move pips
- [x] Frozen systems untouched
