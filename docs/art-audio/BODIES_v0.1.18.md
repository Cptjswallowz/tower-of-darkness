# Combat bodies — v0.1.18-bodies (Art note)

**Fidelity:** PLACEHOLDER still portraits  
**Tag:** v0.1.18-bodies  
**Audience:** Android Engineer (via Chief of Staff)  
**WO lock:** `docs/bodies-v0118.md`

Wake art stays **frozen**. No wardrobe, poses, or hit-flash.

## Assets (drop-in drawables)

| File | Role | Pixels | Combat slot today |
| --- | --- | --- | --- |
| `app/src/main/res/drawable/portrait_you.png` | **You** — flat fallen-kingdom soldier (ash cloak, dull steel, helmet slit, Ashbrand sheathed); 3–4 colors; circular transparent PNG | **256×256** | `HeroShowcase` combat height **~90 dp** (Hub may reuse same drawable later) |
| `app/src/main/res/drawable/portrait_ash_warden.png` | **Ash-Warden** — F2 boss only; cracked coal/ember, chest seal, two dark shoulders; heavier than You; keeps wide boss silhouette | **320×320** | `EnemySilhouette` boss **~160 dp** (replace orange Ember disk for Ash-Warden only) |

Compose: `R.drawable.portrait_you`, `R.drawable.portrait_ash_warden`.

## Wire hints (Engineer)

- Sit sprites **inside** existing portrait frames / columns. Do **not** move HP bars, Brace/Soften pip rows, or Wake crescent overlay (those stay on top).
- If the sprite clips the pip row or status chrome, **shrink the Image**, don’t move pips.
- Content is already inset ~6% inside a circular alpha mask for circular clips.
- **Not this tag:** Ash Wretch, Seal Spinner, F1 Seal-Warden — keep current placeholders (`EnemySilhouette` / existing stubs).
- `EnemyKind.ASH_WARDEN` currently shares `enemies/enemy_dragon.png` stub — point F2 boss UI at `portrait_ash_warden` only; leave `DRAGON` (Seal-Warden) on the orange/placeholder path.

## Out of scope

- Wardrobe system, pose sheets, hit-flash
- Trash / F1 boss art
- Wake icon / crescent / speck / SPARK (frozen)
- Timing / math / save / path
