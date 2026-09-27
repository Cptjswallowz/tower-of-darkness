# Trollkept portrait overlays — v0.1.54 PART B (Art)

**Fidelity:** SOURCE STILLS (Elliott) — luminance key / crop / place only. **Not** redrawn PH.
**WO:** v0.1.54-trollkept (Elliott refs)
**Locks:** Combat FX/SFX untouched. **`portrait_you` not redrawn.**

Hub + title `HeroShowcase` only — not combat, not enemy busts.

## Files (256×256 RGBA)

| File | bytes | md5 | Content bbox |
|------|------:|-----|--------------|
| `assets/portraits/overlay_soot_rim.png` | 94294 | `90a077e1ef7992edf04f463b7139004b` | `(0, 0, 256, 253)` |
| `assets/portraits/overlay_ash_pauldron.png` | 13208 | `5f0f16af1083ecb210e9b7f860ca189b` | `(35, 96, 109, 199)` |
| `assets/portraits/overlay_troll_tooth.png` | 7234 | `24801321125242e99d49e09f685c17a0` | `(164, 109, 211, 220)` |
| `assets/portraits/overlay_gate_sigil.png` | 4862 | `50987c84db3e116bebbe1fb30192aa2e` | `(101, 184, 155, 226)` |

## Scale / anchor

- **Canvas:** **256×256** — same bust scale as `portrait_you.png`.
- **Anchor:** frame-aligned with Hub/title You Image (center = bust center). Outside circle alpha 0.
- **Stack (bottom → top):** soot_rim → ash_pauldron → troll_tooth → gate_sigil.

## Source → overlay

| Overlay | Elliott still | Placement |
|---------|---------------|-----------|
| soot_rim | isolated soot ring (07) + bust frame (04) | Full circular ember/soot rim |
| ash_pauldron | isolated pauldron (08) + bust (03) | Viewer-left cracked ember pauldron |
| troll_tooth | isolated tooth (06) + bust (02) | Viewer-right chest on cord |
| gate_sigil | grate-on-band (05) + bust medallion (01) | Center-lower chest portcullis |

Script: `tools/prep_trollkept_overlays_v0154.py`. Stage preview: `/workspace/tod-trollkept-v0154/preview_you_all_overlays.png`.
