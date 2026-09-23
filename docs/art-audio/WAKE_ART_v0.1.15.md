# Wake art — v0.1.15-wakeart (Art note)
**Fidelity:** PLACEHOLDER bitmaps (blade + crescent **design locked** by Elliott)  
**Tag:** v0.1.15-wakeart  
**Audience:** Android Engineer (via Chief of Staff)  
**Frozen elsewhere:** Wake math, thresh, 2x, save, pips, path — do not touch.

Placeholder **bodies** stay. Do **not** redraw You / enemy portraits. Spark stays small.

---

## Assets (in tree)

All under `app/src/main/res/drawable/`:

| File | Role | Size |
| --- | --- | --- |
| `ashbrand_icon.png` | Ashbrand **slot icon** — cracked ash-iron longsword, gold fuller seam, dark card, no hall/hand | 192×192 |
| `ashbrand_spark.png` | **Half-charge spark** — tiny ember overlay on the **icon only** (no crescent) | 192×192 |
| `wake_vfx_charge.png` | FULL Wake frame 1 — charge | 512×288 |
| `wake_vfx_slash.png` | FULL Wake frame 2 — slash | 512×288 |
| `wake_vfx_impact.png` | FULL Wake frame 3 — impact | 512×288 |

Compose resource names: `R.drawable.ashbrand_icon`, `ashbrand_spark`, `wake_vfx_charge`, `wake_vfx_slash`, `wake_vfx_impact`.

---

## 1) ICON — Ashbrand slot
- Replaces empty Ashbrand plate art. **Same slot size as today** (scale 192² into existing weapon/skill plate).
- Cracked ash-iron blade + **gold seam down the fuller**. Dark card chrome. No hand, no hall BG.

### Spark (half charge)
- Show `ashbrand_spark` **on the icon only** (overlay same bounds).
- **No crescent** on SPARK. Quiet — matches wake-v014 SPARK rules.

---

## 2) WAKE VFX — full chain only
Overlay on **existing combat portrait stage**. Transparent BG.

### Frame order (play once on FULL Wake)
1. `wake_vfx_charge` — gold crescent forming + ash motes  
2. `wake_vfx_slash` — one gold crescent slash  
3. `wake_vfx_impact` — single impact spark on enemy side + ash burst  

Suggested timing @1x (halve under existing 2x hold rules): ~400 / 500 / 400 ms (fit inside FULL Wake hold; do not change hold math).

### Safe margins (baked into art)
Content stays inside approx:
- **Top ≥ 36 px** — clear of status bar / HP / pips  
- **Bottom ≥ 48 px** — clear of skill row  
- **Sides ≥ 40 px**  
Impact spark biased to **right ~72%** (enemy portrait side). Do not redraw bodies under the overlay.

---

## Out of scope
- BLOCK/FINAL polish  
- Soften / Brace pip redesign  
- ToW / Eldermark assets  
- Video / Lottie — still frames only
