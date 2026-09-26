# CLEAVE melee VFX — v0.1.42-cleavekit (Art & Audio)

**Fidelity:** PLACEHOLDER / pack drop-in (CLEAVE free sampler sheets — **lossless copy**, no re-encode, no redraw).  
**WO:** v0.1.42-cleavekit  
**Owner:** Art & Audio (assets + playback notes) · Engineer (Compose atlas playback / blend)  
**Script:** `tools/prep_cleave_v0142.py` (idempotent)  
**Stage:** `/workspace/tod-cleave-v0142/`  
**Sampler:** `/workspace/tod-cleavekit-v0142/`

Does **not** edit Kotlin. Does **not** touch Wake / ashbrand_spark / F3 cave-troll / Gate-Warden / Brace / status pips / glyphs / packs / portraits / node tokens / `floor_backdrop.jpg`.  
**Do NOT install or wire `shield-block`** — Brace pips stay frozen; shield-block is ignored from this pack.

---

## Source pack (2 effects only)

Pack: **CLEAVE - Melee Impact & Weapon VFX (free sampler)** (`effects.json` md5 `fa6b2ead40ebcf9da304faaf2116234c`).

| Effect id | Source path | Source md5 | Used as |
|-----------|-------------|------------|---------|
| `slash-light` | `sheets_rgba/slash-light.png` | `e02410214790304f8d2e408472cc6cc0` | primary slash arc |
| `hit-flash` | `sheets_rgba/hit-flash.png` | `610cf76722f5f0ea852d60b85e2be8a5` | contact flash (RGBA atlas) |
| `hit-flash` (additive) | `sheets_additive/hit-flash.png` | `11635e0b56822407975eab4d884bb491` | optional Compose additive sheet |

**Ignored completely:** `engine/`, `gif/`, and effects `shield-block`, `crush-slam`, `stab-thrust`, `stagger-wobble`, `trail-arc`, `crit-burst`.

---

## Drawables (installed)

Full atlas sheets (Wake-style — **no per-frame slice**). Naming: `fx_` + snake_case (Android drawable names cannot contain hyphens).

| Drawable | Bytes | md5 | Pixels |
|----------|------:|-----|--------|
| `app/src/main/res/drawable/fx_slash_light.png` | 90820 | `e02410214790304f8d2e408472cc6cc0` | 2048×512 |
| `app/src/main/res/drawable/fx_hit_flash.png` | 301690 | `610cf76722f5f0ea852d60b85e2be8a5` | 1536×512 |
| `app/src/main/res/drawable/fx_hit_flash_additive.png` | 230064 | `11635e0b56822407975eab4d884bb491` | 1536×512 |

Drawable md5s for the two main assets **match** `sheets_rgba/` sources (lossless `copy2`).

### Additive sheet decision

- **Default atlas:** `fx_hit_flash.png` (RGBA from `sheets_rgba/`) — correct RGB+A for inspection / alpha compositing.
- **Compose additive playback:** prefer `fx_hit_flash_additive.png` when applying an additive / `BlendMode.Plus` (or equivalent) layer. Pack marks hit-flash `blend: additive`; the additive sheet is the pack’s pre-authored additive variant.
- If Engineer already converts RGBA → additive in shader/Compose, RGBA alone is enough — keep the additive drawable as an optional drop-in, do not delete it.

---

## Atlas layout (horizontal-row major)

Cell size **256×256** for both. Index: `frame = row * cols + col`  
**Row-major:** row0 = frames `0 .. cols-1`, row1 continues (`cols .. 2*cols-1`), etc. Same shipping pattern as Wake single-PNG sheets.

### slash-light → `fx_slash_light`

| Field | Value |
|-------|-------|
| Grid | **8×2** (cols×rows) |
| Frames | 16 |
| FPS | 24 |
| Cell | 256 |
| Peak frame | **6** |
| Blend | **alpha** |
| Anchor xy | **[0.46, 0.5]** |
| Loop | false |

### hit-flash → `fx_hit_flash` (+ optional additive)

| Field | Value |
|-------|-------|
| Grid | **6×2** |
| Frames | 12 |
| FPS | 24 |
| Cell | 256 |
| Peak frame | **2** |
| Blend | **additive** (Compose) |
| Anchor xy | **[0.5, 0.5]** |
| Loop | false |

---

## Playback guidance (WO)

### slash-light
- Emphasize the **peak window frames ~4–10**, **or** subsample every-other frame for a snappier read.
- Target **1× duration ≤ 500 ms** (at 24 fps, 16 frames ≈ 667 ms full; trim / skip to stay under 500 ms).
- Alpha blend; anchor slightly left of center (`0.46, 0.5`) so the arc reads across the strike.

### hit-flash
- Play the **first 6–8 frames**, then fade out (do not grind the full 12 if the tail is muddy).
- Use **additive blend in Compose** (`fx_hit_flash_additive` or RGBA + Plus).
- Peak at frame **2** — time contact / damage float to that beat.

### Explicit bans
- **Do NOT use `shield-block` for Brace** (or anything else this WO). Brace / status pips remain frozen.
- **Do NOT replace or restyle** Wake (`wake_vfx_*`), `ashbrand_spark`, F3 assets (`portrait_cave_troll_*`, `floor_backdrop_cave`, `portrait_gate_warden`).

---

## Stage + preview

| Path | Notes |
|------|-------|
| `/workspace/tod-cleave-v0142/fx_slash_light.png` | stage copy (= drawable bytes) |
| `/workspace/tod-cleave-v0142/fx_hit_flash.png` | stage copy |
| `/workspace/tod-cleave-v0142/fx_hit_flash_additive.png` | stage copy |
| `/workspace/tod-cleave-v0142/slash-light.png` | original-name mirror |
| `/workspace/tod-cleave-v0142/hit-flash.png` | original-name mirror |
| `/workspace/tod-cleave-v0142/hit-flash_additive.png` | additive mirror |
| `/workspace/tod-cleave-v0142/_preview.png` | contact sheet: slash f4/6/8/10 + hit-flash f0/2/4/6 |

Re-run: `/tmp/artvenv/bin/python tools/prep_cleave_v0142.py`

---

## Frozen (spot-check — do not modify this WO)

| Asset | Status |
|-------|--------|
| `wake_vfx_slash.png` | frozen |
| `wake_vfx_impact.png` | frozen |
| `wake_vfx_charge.png` | frozen |
| `ashbrand_spark.png` | frozen |
| F3 cave trolls / cave backdrop / Gate-Warden | frozen |
| Brace / status pips | frozen — **no shield-block** |
| Glyphs, packs, portraits, node tokens, `floor_backdrop.jpg` | frozen |

No Kotlin files touched.
