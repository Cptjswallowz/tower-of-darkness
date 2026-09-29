# v0.1.63-scorefade — once beds + dual-player loop crossfade

**versionCode** 64 · **versionName** `0.1.63-scorefade`

## Bug
`MediaPlayer.isLooping = true` seeks to 0 at end → audible click on loop beds.

## Loop strategy shipped: **dual-player crossfade**
Primary path for LOOP beds: never `isLooping=true`. ~1.8s before end, start a
second `MediaPlayer` of the same file at volume 0, crossfade over that window,
release the first, repeat. Fallback (only if dual prepare fails): fade out 1.2s
then fade in the same file from 0.

## Once vs loop ([MusicBed](../app/src/main/java/com/towerofdarkness/app/domain/sound/MusicBed.kt))

| Mode | Slots | Behavior |
|---|---|---|
| **ONCE** (`loops=false`) | title, loadout, shop, rest, victory, defeat | Play from start; last **~2.0s** linear fade to 0. Title/Loadout/Shop/Rest then **silence forever** on that screen (`onceFinishedBed`); same-bed re-apply no-ops. Victory/Defeat keep post-sting handoff via MusicMap/GameController. |
| **LOOP** (`loops=true`) | hub, path, combat, elite, boss | Dual-player crossfade (no `isLooping`). |

## Shared rules (unchanged)
- Gain **0.40**; duck to **0.18** for ~320ms on combat SFX keys.
- Screen leave / bed switch: **400ms** fade-out then fade-in (never hard cut).
- Lifecycle pause/resume; ClimbIntro silence.
- No new music files or slots; SCORE_MAP / Forge / SFX / FX / remnants / Continue untouched.

## Constants ([MusicPlayer](../app/src/main/java/com/towerofdarkness/app/domain/sound/MusicPlayer.kt))
- `LOOP_CROSSFADE_MS` = 1800
- `ONCE_TAIL_FADE_MS` = 2000
- `CROSSFADE_MS` = 400
- `FALLBACK_FADE_MS` = 1200

## HOLD
Do **not** merge to main. Do **not** create official tag `v0.1.63-scorefade`.
Phone-qa install tag: `phone-qa-0.1.63-scorefade`.
