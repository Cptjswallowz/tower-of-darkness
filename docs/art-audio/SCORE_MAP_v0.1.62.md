# SCORE MAP — v0.1.62-score (bed music)

Audio beds only. Plates / SFX / FX / Compose **untouched**.

**Encode:** `ffmpeg -y -i SRC -c:a libvorbis -q:a 5` → `tod-score-v0162/music/<slot>.ogg` then `cp -a` → `assets/music/<slot>.ogg`.

**Vendors (do NOT commit zips):**
- Brunet Vol.1 → `/workspace/tod-score-v0162/vendor/brunet/` (from `Dark_Fantasy_RPG_Music_Pack_Vol.1.zip`)
- Ashes → `/workspace/tod-score-v0162/vendor/ashes/` (Tracks/)

Prefer Brunet **MP3/** per CoS. No fake tones. Missing dedicated → copy `path.ogg` (none missing this rev).

## Slot → source → ogg

| slot | source filename | pack | ogg bytes | md5 | dur (s) |
|------|-----------------|------|----------:|-----|--------:|
| title | `Before the Winter Gate.mp3` | Ashes Tracks | 560354 | `af2db015eb70e1986e8c2eaa06105ad7` | 30.77 |
| hub | `Paved Streets.mp3` | Brunet MP3 | 1995351 | `3c7325cb49233960622e907398d5c2c4` | 119.00 |
| path | `Don't Stray From The Path.mp3` | Brunet MP3 | 1329974 | `a4cbf70cd36e51f14cb7eee3214e8803` | 81.33 |
| loadout | `Magical Investigation.mp3` | Brunet MP3 | 4470069 | `c694345a2c0c03847ee944c6b9ed62ae` | 239.20 |
| combat | `Silver Sword (LOOP).mp3` | Brunet MP3 | 1150198 | `d4f8b27d62320eb96965737565fc0bd6` | 68.00 |
| elite | `Feeding Ground (LOOP).mp3` | Brunet MP3 | 2004256 | `8d5cdca5f85ec47854a638719ced3ac6` | 120.80 |
| boss | `Siege of the Black Rampart.mp3` | Ashes Tracks | 595851 | `c81ba9a40cd7e4915a92aa6b19c61d21` | 30.77 |
| shop | `Beer & Games!.mp3` | Brunet MP3 | 3034278 | `6fa8d766b09190a7b7ae280a8f2b3d0c` | 179.50 |
| rest | `Deserted Island (LOOP).mp3` | Brunet MP3 | 834320 | `a88b6c49570f732c7c131c44aa4539f7` | 48.00 |
| victory | `Silver Sword (ENDING).mp3` | Brunet MP3 | 39174 | `5c3272ba70b639cc0cbc3a9054e833f1` | 2.00 |
| defeat | `Enthralled (ENDING).mp3` | Brunet MP3 | 48778 | `b660f14666f0df3ef67e97cc6429d797` | 3.69 |

## Reuses

- **None.** Every slot has a dedicated source. `shop` ≠ `loadout` (Beer & Games! vs Magical Investigation).
- Prior mistaken shared Beer & Games! for both loadout+shop was corrected this pass.

## Locks

- Exactly **11** `.ogg` files in `assets/music/` (plus `.gitkeep`).
- Curated stage `tod-score-v0162/music/` md5s match `assets/music/`.
- Vendor zips stay under `tod-score-v0162/vendor/` — **not committed**.
- Do not touch plates / SFX / FX / Compose.
- `victory` / `defeat` are short ENDING stingers by design (2.0s / 3.7s).

## Paths

- Curated: `/workspace/tod-score-v0162/music/<slot>.ogg`
- Game: `/workspace/tower-of-darkness/assets/music/<slot>.ogg`
- Manifest twin: `/workspace/tod-score-v0162/MANIFEST.md`
