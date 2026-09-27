# P1 Audio — v0.1.52-slashlayer

Status: **implemented** (P1 Audio remap).  
**Do not** tag / `gh release` until CoS green after QA. HOLD tag `v0.1.52-slashlayer`.

Combat FX art **LOCKED** (stroke / puff / Wake / PLUME / Kenney particles / Floor3 / Hub / shop / combat math / Ashbrand ember math) — untouched.

## Release-note keys (exact)

```
slash_swing=IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg
slash_impact=IRONCLASH_23_Flesh_Hit_Light_08.ogg
wake_clash=IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg
brace=IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg
```

## Wired

| Role / key | Trigger | Runtime path under `app/src/main/assets/` |
|------------|---------|-------------------------------------------|
| `slash_swing` + `impact` | Any stroke/slash-light land (player damage OR foe→You); swing leads impact by **SWING_LEAD_MS=60** (40–80 band) | `audio/ironclash/IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg` then `…/IRONCLASH_23_Flesh_Hit_Light_08.ogg` |
| `legendary` | FULL Wake (+ Victory) | `audio/sfx_legendary_sting.wav` **WITH** `audio/ironclash/IRONCLASH_28_Critical_Hit_Stinger_01_wake380.ogg` under (sting = lead) |
| `ember` | Ashbrand SPARK (+ optional Wake tip same beat) | `audio/lentikula/lentikula_fire_impact_5_short220.ogg` (duck under Wake when both fire) |
| `brace` | Brace GAIN only (no clash) | `audio/ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg` |
| `soften` / `ui` / `dice` / `miss` | unchanged from 0.1.51 | Lentikula / Kenney / legacy stubs |

Volume: swing+impact at **VOL_FULL ≈ 0.80** (foe→You and player). Swing file has **+3 dB baked** — no extra mixer gain.

Curated source: `assets/sfx/ironclash/` (+ licenses). Art doc: `docs/art-audio/IRONCLASH_SFX_v0.1.52.md`.

## Same-frame

`GameController.playLogSounds` → `SoundBus.playFrame`.  
`IronclashSfx.resolveFrame`: **impact** → swing @ t=0 + impact @ t=SWING_LEAD_MS (Handler on main looper). Legendary → sting + wake_clash under. Garnish duck ~0.35 when impact/legendary shares frame.

## Holds / locks

- No CombatStrokeOverlay / puff hold / Kenney particle pips / skill math / Floor3 / Hub / shop / CombatFx stroke / PLUME / Wake art.
- Pack zips under `/workspace/tod-sfx-v0151/` **not** committed.
- Tag **HELD**; push `origin/main` OK (no force).
- Real `./gradlew :app:assembleDebug` only.

## Debug

Logcat `SFX`: `swing|impact|clash|sting <file>`.
