# P1 Audio — v0.1.53-wakeblade

Status: **feature tip** (tag HELD — CoS / QA gate before tag). Visuals **LOCKED**.

Unmaps Wake from Clash / Parry / Block / Critical ring. Wake now reads as heavy blade swing + flesh impact under the legendary sting (not metal clink).

Keeps **1.52 slash layer volumes** (normal stroke swing+impact unchanged at VOL_FULL≈0.80).

## Release-note keys (exact)

```
wake_swing=IRONCLASH_02_Sword_Swing_Heavy_04_wake_plus3db.ogg
wake_impact=IRONCLASH_24_Flesh_Hit_Heavy_04_wake_plus3db.ogg
sting=sfx_legendary_sting
slash_swing=IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg
slash_impact=IRONCLASH_23_Flesh_Hit_Light_08.ogg
```

Brace keep: `brace=IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg` (shield only — unchanged).

## Wired

| Role / key | Trigger | Runtime path under `app/src/main/assets/` |
|------------|---------|-------------------------------------------|
| `slash_swing` + `impact` | Any stroke/slash-light land; swing leads impact by **SWING_LEAD_MS=60** | `audio/ironclash/IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg` then `…/IRONCLASH_23_Flesh_Hit_Light_08.ogg` |
| `legendary` (Wake) | FULL Wake (+ Victory) | `wake_swing` @ t=0 → **`wake_impact` + `sfx_legendary_sting` together** @ t=60ms (sting = lead, impact = bed) |
| `ember` | Ashbrand SPARK (+ optional Wake tip same beat) | `audio/lentikula/lentikula_fire_impact_5_short220.ogg` (**duck under Wake**) |
| `brace` | Brace GAIN only | `audio/ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg` |
| `soften` / `ui` / `dice` / `miss` | unchanged | Lentikula / Kenney / legacy stubs |

Loudness: Wake pair has **+3 dB baked (~1.4×)** — no extra mixer gain. Target **1.3–1.5×** vs normal slash via baked files. Mixer VOL_FULL≈0.80 for both layers.

Supersedes `wake_clash=IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg` from 1.52. Leftover `*_wake380.ogg` may remain on disk unused.

Art doc: `docs/art-audio/IRONCLASH_SFX_v0.1.53.md`. Curated: `assets/sfx/ironclash/`.

## Same-frame

`GameController.playLogSounds` → `SoundBus.playFrame`.  
`IronclashSfx.resolveFrame`: **impact** → swing @ t=0 + impact @ t=SWING_LEAD_MS. **legendary** → wake_swing @ t=0 + (sting + wake_impact) @ t=SWING_LEAD_MS. Garnish duck ~0.35 when impact/legendary shares frame.

## Holds / locks

- No CombatStrokeOverlay / puff / Kenney pips / skill math / Floor3 / Hub / shop / CombatFx stroke / PLUME / Wake art.
- Pack zips under `/workspace/tod-sfx-v0151/` **not** committed.
- Tag **`v0.1.53-wakeblade`** LIVE; push `origin/main` OK (no force).
- Real `./gradlew :app:assembleDebug` only.
- No dex-swap. Same debug keystore.

## Debug

Logcat `SFX`: `swing|impact|wake_swing|wake_impact|sting <file>`.
