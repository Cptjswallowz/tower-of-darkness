# IRONCLASH SFX — v0.1.53-wakeblade (Art)

Audio-only. Visuals LOCKED.

**Why:** Wake still read as clink/parry. Clash = metal ring — wrong for Wake. Unmap Wake from Clash / Parry / Block / Critical ring.

Keeps **slash_swing / slash_impact / brace** from v0.1.52. Supersedes `wake_clash` from 1.52.

## Release-note keys (exact filenames)

```
wake_swing=IRONCLASH_02_Sword_Swing_Heavy_04_wake_plus3db.ogg
wake_impact=IRONCLASH_24_Flesh_Hit_Heavy_04_wake_plus3db.ogg
sting=sfx_legendary_sting
slash_swing=IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg
slash_impact=IRONCLASH_23_Flesh_Hit_Light_08.ogg
brace=IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg
```

## Paths + md5

| Role | Path | ms | md5 | Notes |
|------|------|---:|-----|-------|
| **wake_swing** | `assets/sfx/ironclash/IRONCLASH_02_Sword_Swing_Heavy_04_wake_plus3db.ogg` | 491 | `cd0e6084ea89232334db391c8783f61e` | Heaviest Sword Swing Heavy. Not clash/parry/block. **+3 dB baked** (~1.4×). |
| **wake_impact** | `assets/sfx/ironclash/IRONCLASH_24_Flesh_Hit_Heavy_04_wake_plus3db.ogg` | 440 | `c50e7db25867a193b28cedba44d62b4b` | Heaviest blade/flesh Heavy. Not stone/bone/blunt. No armour-impact bank in pack. **+3 dB baked**. |
| **sting** | `assets/sfx/sfx_legendary_sting.wav` (also `app/src/main/assets/audio/sfx_legendary_sting.wav`) | 350 | existing | Lead under Wake — do not replace. |
| **slash_swing** | `assets/sfx/ironclash/IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg` | 197 | `fde6b48d4dc8e0c305a28e6008fd3cea` | Unchanged 1.52 |
| **slash_impact** | `assets/sfx/ironclash/IRONCLASH_23_Flesh_Hit_Light_08.ogg` | 189 | `54e2c70b9d992a20b6c50c0da6ff3146` | Unchanged 1.52 |
| **brace** | `assets/sfx/ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg` | 351 | `a0350366bc961ea7e65e050a0a70a8dd` | Shield only. Unchanged |

## Layering (Engineer)

1. **Normal slash:** `slash_swing` starts **40–80 ms before** `slash_impact`.
2. **Wake:** `wake_swing` 40–80 ms before `wake_impact`; fire **`wake_impact` + `sfx_legendary_sting` together** (impact bed, sting lead). Tail ≤500 ms (clips already in budget).
3. **Loudness:** Wake pair baked **~+3 dB (~1.4×)** vs their dry sources — target **1.3–1.5×** vs normal slash. Further gain only if playtest needs it.
4. **Ashbrand tip:** keep `lentikula_fire_impact_5_short220.ogg`; **duck under Wake**.
5. **Do not** use Clash / Parry / Block / Critical for Wake (incl. superseded `IRONCLASH_03_*_wake380`, `IRONCLASH_28_*_wake380`).

## Art process

- wake_swing: `02_Sword_Swing_Heavy_04` (peak leader vs Axe/Heavy set) +3 dB + short out-fade.
- wake_impact: `24_Flesh_Hit_Heavy_04` (peak leader) +3 dB + out-fade. Pack has no blade-on-armour impact bank (Armour_* = move rustle only).

No Kotlin. No store zips. Source: `/workspace/tod-sfx-v0151/IRONCLASH_v1.0.zip`.
