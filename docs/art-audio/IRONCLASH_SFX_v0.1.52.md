> Wake mapping superseded by `IRONCLASH_SFX_v0.1.53.md` (wakeblade). slash_swing/slash_impact/brace still valid.

# IRONCLASH SFX — v0.1.52-slashlayer (Art)

Audio-only. Visuals LOCKED (no FX sheets / stroke / PLUME / Kenney pips).

Playtest 0.1.51: slashes read as weak hits, not cuts; Wake under-layer wrong. Pack separates **Swings / Impacts / Clashes** — wire them as layers.

Supersedes combat mapping in `IRONCLASH_SFX_v0.1.51.md` for slash + Wake + Brace. Lentikula fire tip / Soften / Kenney click unchanged (see 0.1.51).

## Release-note keys (exact filenames)

```
slash_swing=IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg
slash_impact=IRONCLASH_23_Flesh_Hit_Light_08.ogg
wake_clash=IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg
brace=IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg
```

## Paths + md5

| Role | Path | ms | md5 | Notes |
|------|------|---:|-----|-------|
| **slash_swing** | `assets/sfx/ironclash/IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg` | 197 | `fde6b48d4dc8e0c305a28e6008fd3cea` | Sword Swing Light (high-mid slice). **+3 dB baked** in file. Not axe/mace. |
| **slash_impact** | `assets/sfx/ironclash/IRONCLASH_23_Flesh_Hit_Light_08.ogg` | 189 | `54e2c70b9d992a20b6c50c0da6ff3146` | Blade/flesh impact. Not bone/stone/blunt. |
| **wake_clash** | `assets/sfx/ironclash/IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg` | 380 | `d6186f00f278f7f0f13f99f06ed9a19c` | Critical metal onset trimmed ≤400 ms. Heavier than plain Clash_04. |
| **brace** | `assets/sfx/ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg` | 351 | `a0350366bc961ea7e65e050a0a70a8dd` | Shield Block Metal only (no clash). Unchanged from 0.1.51. |

## Layering (Engineer)

1. **Slash land:** start `slash_swing` **40–80 ms before** `slash_impact` (cut whoosh then blade land).
2. **Wake:** play `wake_clash` **under** `sfx_legendary_sting` (clash = bed; sting = lead).
3. **Ashbrand / Wake tip:** keep `assets/sfx/lentikula/lentikula_fire_impact_5_short220.ogg`; **duck under** Wake layer when both fire.
4. **Brace gain:** `brace` alone — do not reuse clash.

## Art process

- Swing: source `01_Sword_Swing_Light_09` + `volume=3dB` one-shot bounce (no new mixer needed). High-mid family preferred over Heavy/Axe for slice presence.
- Wake: source `28_Critical_Hit_Stinger_01` from t=0.10 s, 380 ms + out-fade (critical >> clash e400).
- Impact/brace: pack one-shots (brace already trimmed in 0.1.51).

Leftover in folder (superseded, safe to ignore for wire): `IRONCLASH_03_Sword_Clash_04_wake380.ogg`, `IRONCLASH_23_Flesh_Hit_Light_05.ogg`.

No Kotlin. No store zips in repo. Source packs: `/workspace/tod-sfx-v0151/`.
