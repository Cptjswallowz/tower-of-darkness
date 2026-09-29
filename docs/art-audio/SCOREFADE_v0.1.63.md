# SCOREFADE — v0.1.63-scorefade (Art & Audio)

**WO:** natural-tail audit for ONCE beds + confirm Engineer loop-player method.  
**Beds:** SCORE_MAP v0.1.62 (unchanged screen map / no new packs or slots).  
**Bug being fixed:** loop slam = last sample → hard seek(0) click. Do **not** replace music files unless a track has **no** usable natural tail.

Audit method: `ffprobe` duration + decode last ~2.5s to mono f32 @44.1kHz; RMS/peak envelope in 50–100ms frames (`ffmpeg` + numpy). No music files modified this pass.

---

## ONCE vs LOOP (WO rules, condensed)

| Mode | Slots | Runtime behavior |
|------|-------|------------------|
| **ONCE** (`loops=false`) | title, loadout, shop, rest, victory, defeat | Play from start; linear fade to 0 over last **1.8–2.5s** (~2.0s shipped); then **silence forever** on that screen (no restart after silence). Victory/defeat = short ENDING stingers. |
| **LOOP** (`loops=true`) | hub, path, combat, elite, boss | While screen up: seamless loop via dual-player crossfade. **Never** `MediaPlayer.isLooping` / hard seek(0). |

Shared (locked): gain ~0.40; music duck to 0.18 / ~320ms on combat SFX keys; Soften solo SFX vol **0.62** untouched; screen leave **400ms** fade; Continue / remnants / Forge / SFX / FX **LOCKED**. SCORE_MAP screen map unchanged.

---

## Per-ONCE-slot natural-tail audit

| slot | dur (s) | usable natural tail? | notes | replace? |
|------|--------:|----------------------|-------|----------|
| title | 30.77 | **thin** | Last 2.5s stays full-level mid-phrase (RMS ≈ −11 dBFS, EOF peak ≈ −2.9 dBFS / last frame peak near 0 dBFS). No natural decay, but EOF samples are clean — a 1.8–2.5s linear fade to 0 has headroom and will not sound clipped. | **no** |
| loadout | 239.20 | **yes** | Already near digital silence in last ~1s (RMS < −70 dBFS by ~237.5s; true mute by ~238.8s). Fade is insurance only. | **no** |
| shop | 179.50 | **yes** | Strong natural decay; last ~1s already soft→silent (RMS ≈ −45→mute by ~178.6s). | **no** |
| rest | 48.00 | **yes** | Declining toward EOF (last-third RMS ≈ 0.63× first-third of window; last 100ms RMS ≈ −29 dBFS, peak ≈ −19 dBFS). Usable soft landing under a 2s fade. | **no** |
| victory | 2.00 | **yes** (ENDING stinger) | Whole clip ≈ fade window. Peak at attack then natural decay to ≈ −58 dBFS by EOF. | **no** |
| defeat | 3.69 | **yes** (ENDING stinger) | Attack decays immediately; by t≈1.2s already < −50 dBFS RMS; last 2.5s are soft reverb dust. | **no** |

**NONE count:** 0. No file flagged for replace.

### Victory / defeat — Engineer fade treatment

- **victory (2.00s):** Entire clip ≤ / ≈ `ONCE_TAIL_FADE_MS` (2000). Watch will see `remaining ≤ 2000` almost immediately → fade almost the **whole** stinger (`fadeMs ≈ remaining`, floored at 200ms). Do **not** force a shorter fade; the natural decay already does most of the work — player fade is a gentle gain ramp over the clip.
- **defeat (3.69s):** Longer than the fade window. Apply the normal last-**~2.0s** linear fade; content is already very soft in that region, so the fade is light insurance over an existing ENDING tail. Optional shorter fade (e.g. 1.0–1.5s) would also be fine but is unnecessary.

---

## LOOP beds (brief — no deep tail audit)

| slot | dur (s) | note |
|------|--------:|------|
| hub | 119.00 | LOOP while MetaHub up |
| path | 81.33 | LOOP (Path / FloorBreak / Tutorial / Event / Treasure) |
| combat | 68.00 | LOOP trash hallway |
| elite | 120.80 | LOOP Cave Troll / elite |
| boss | 30.77 | LOOP floor boss / Gate / Seal-Warden |

EOF→SOF hard join is the published click (esp. combat / boss). Fixed in player, not by re-encoding.

---

## Engineer player method — CONFIRMED dual-crossfade

**Verdict: dual** (preferred path already wired on `feat/0.1.63-scorefade`). Fallback fade-out/fade-in also present. FAIL path (`isLooping=true` / hard seek(0)) explicitly avoided.

### Code pointers

| What | Where |
|------|-------|
| Bed slots + `loops` flag | `app/src/main/java/com/towerofdarkness/app/domain/sound/MusicBed.kt` — `enum class MusicBed` (ONCE `loops=false`, LOOP `loops=true`) |
| Nav → intent | `…/MusicMap.kt` — `MusicMap.resolve`, `combatBed`, `DUCK_KEYS` |
| Player | `…/MusicPlayer.kt` — `class MusicPlayer` |
| Dual loop handoff | `MusicPlayer.startDualLoopHandoff` + `dualCrossfade` (~`LOOP_CROSSFADE_MS`=1800 before end; second instance @ gain 0, crossfade, release first) |
| Loop watch / never isLooping | `scheduleLoopWatch`; `createPreparedPlayer` sets `mp.isLooping = false` |
| Fallback | `fallbackLoopRestart` — fade out `FALLBACK_FADE_MS`=1200 then fade same file in from 0 |
| Once tail | `scheduleOnceTailWatch` — last `ONCE_TAIL_FADE_MS`=2000 → fade to 0 → `onceFinishedBed` (same-bed re-apply no-op) |
| Leave mid-track | `crossfadeTo` — `CROSSFADE_MS`=400 fade-out then next bed |
| Duck | `MusicPlayer.duck` → `DUCK_GAIN`=0.18 / `DUCK_MS`=320; SFX Soften solo stays `IronclashSfx.VOL_SOFTEN_SOLO`=**0.62** |
| Wiring | `GameController` constructs `MusicPlayer` + `AssetSoundBus(app, music)`; `MainActivity` calls `MusicMap.resolve` → `music.apply` |

Engineer handoff doc twin (runtime notes): `docs/scorefade-v0163.md`.

---

## Locks (do not touch this WO)

- Duck vs Soften SFX **0.62** stays.
- Continue / remnants / Forge / SFX / FX **LOCKED**.
- No new music packs or slots; leave `SCORE_MAP_v0.1.62.md` screen map alone.
- Do not replace any `.ogg` (all ONCE tails usable or thin-OK).

---

## QA checklist

- [ ] **ONCE fade:** title / loadout / shop / rest each play once; last ~2s linear fade to silence; **no restart** after silence while still on that screen.
- [ ] **Stingers:** victory (~2s) fades essentially whole clip; defeat (~3.7s) fades last ~2s over already-soft tail; both end clean, no loop.
- [ ] **LOOP no seek0 slam:** hub / path / combat / elite / boss crossfade near end (dual instance); no click at wrap.
- [ ] **Leave mid-track:** switching screens fades out ~**400ms** then next bed (never hard cut).
- [ ] **Duck:** combat SFX still ducks bed to 0.18 / ~320ms; Soften solo SFX still 0.62; SFX/FX otherwise unchanged.
- [ ] **ClimbIntro:** bed silent during intro video.
- [ ] Assets: still exactly 11 `assets/music/*.ogg` matching SCORE_MAP v0.1.62.

---

## Paths

- This doc: `docs/art-audio/SCOREFADE_v0.1.63.md`
- Screen map (unchanged): `docs/art-audio/SCORE_MAP_v0.1.62.md`
- Curated / runtime music: `assets/music/<slot>.ogg` ↔ `app/src/main/assets/music/<slot>.ogg`
