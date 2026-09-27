# P1 Audio — v0.1.51-ironclash

Status: **implemented** (P1 one-shots).  
**Do not** tag / `gh release` until CoS green after QA. HOLD tag `v0.1.51-ironclash`.

Combat FX art **LOCKED** (stroke / puff / Wake / PLUME / Kenney particles / Floor3 / Hub / shop / combat math / Ashbrand ember math) — untouched.

## Wired

| Role / key | Trigger | Runtime path under `app/src/main/assets/` |
|------------|---------|-------------------------------------------|
| `impact` | stroke / slash-light land; player damage skills; enemy Nip/DAMAGE | `audio/ironclash/IRONCLASH_23_Flesh_Hit_Light_05.ogg` |
| `legendary` | FULL Wake (+ Victory) | `audio/sfx_legendary_sting.wav` **WITH** `audio/ironclash/IRONCLASH_03_Sword_Clash_04_wake380.ogg` under |
| `ember` | Ashbrand SPARK (+ optional Wake tip same beat) | `audio/lentikula/lentikula_fire_impact_5_short220.ogg` |
| `brace` | Brace GAIN (player or enemy) | `audio/ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg` |
| `soften` | Soften apply (~0.55–0.7 solo; duck 0.35 w/ impact) | `audio/lentikula/lentikula_heal_impact_4_soften.ogg` |
| `ui` | node tap / shop confirm / loadout lock / Continue | `audio/kenney_ui/click_001.ogg` |
| `dice` / `miss` | tumble / defeat | `audio/sfx_dice.wav` / `audio/sfx_miss.wav` (**path bugfix**: was `audio/sfx/sfx_*.wav`) |

Curated source (commit): `assets/sfx/{ironclash,lentikula,kenney_ui}/` + licenses. Art doc: `docs/art-audio/IRONCLASH_SFX_v0.1.51.md`.

## Same-frame

`GameController.playLogSounds` → `SoundBus.playFrame` over **all** new log events that beat (not only `lastOrNull`).  
`IronclashSfx.resolveFrame`: **Impact full**; garnish (`ember`/`soften`) duck to **~0.35** when Impact or legendary shares the frame. Legendary expands to sting + wake380 clash.

## Holds / locks

- No CombatStrokeOverlay / stroke / peaks / PLUME garnish / Wake art / Floor3 / Hub / shop / combat math / Ashbrand ember math changes.
- Pack zips under `/workspace/tod-sfx-v0151/` **not** committed.
- Tag **HELD**; push `origin/main` OK (no force).
- Real `./gradlew :app:assembleDebug` only.

## Debug

Logcat `SFX` once per clip: `impact|wake|brace|ember|soften|ui <clip>`.


## Case 7 fix (post-QA)

`GameController.continueClimb()` and `selectPathNode()` now call `sound.play("ui")` (Kenney `click_001`) so Menu Continue and path-node enter are audible. `enterNode` stays silent to avoid double-fire with `confirmLoadout` → pending enter. versionCode **52** / versionName **0.1.51-ironclash**. Combat FX still LOCKED.
