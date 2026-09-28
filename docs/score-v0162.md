# v0.1.62-score — app-wide bed music

**versionCode** 63 · **versionName** `0.1.62-score`

## Player rules
- One app-wide [MusicPlayer](../app/src/main/java/com/towerofdarkness/app/domain/sound/MusicPlayer.kt) (MediaPlayer); single bed only.
- Loop all beds except **victory** / **defeat** (one-shot stingers).
- Music gain **0.40**; duck to **0.18** for ~320ms on combat SFX keys.
- Screen change: **400ms** fade-out then fade-in. Same bed re-entry no-ops (no restart).
- App background → pause; foreground → resume same bed/position.
- ClimbIntro: silence music while `climb_intro.mp4` plays (ExoPlayer untouched).
- No music slider UI this ticket.

## Screen → slot ([MusicMap](../app/src/main/java/com/towerofdarkness/app/domain/sound/MusicMap.kt))
| Nav / kind | Slot |
|---|---|
| MainMenu | title |
| MetaHub | hub |
| Path, FloorBreak, Tutorial, Event, Treasure | path |
| Loadout | loadout |
| Combat trash | combat |
| Elite / Cave Troll | elite |
| Floor boss / Gate / Seal-Warden (`isBoss` / boss kinds) | boss |
| Shop | shop |
| Rest / Forge sheet | rest |
| ClimbIntro | silence |
| RunSummary win | victory sting (once) |
| RunSummary lose | defeat sting (once) |

## Duck keys (SFX gain unchanged)
`slash_swing`, `impact`, `wake_swing`, `wake_impact`, `brace`, `ember` (Ashbrand), `dice`

## Continue → victory (CoS)
While Combat is in **VICTORY** phase waiting for Continue: **keep** combat/boss/elite bed.
**victory.ogg only after Continue** → `finishRun(won=true)` → RunSummary. Defeat sting on summary lose entry.

## Assets
- Curated: `assets/music/<slot>.ogg` (11 files)
- Runtime: `app/src/main/assets/music/<slot>.ogg` (copied, not symlinked)
- Map: `docs/art-audio/SCORE_MAP_v0.1.62.md`

## Wiring
- `TowerRoot` `LaunchedEffect(nav, enemy, summary)` → `MusicMap.resolve` → `MusicPlayer.apply`
- `MainActivity.onPause` / `onResume` → pause / resume
- `AssetSoundBus` thin duck hook → `MusicPlayer.duck()` (does not mute/pause SFX)
