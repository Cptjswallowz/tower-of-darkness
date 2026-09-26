# Tower of Darkness

Android vertical slice — Kotlin + Jetpack Compose (`com.towerofdarkness.app`).

**Separate** from Tower of the World: no shared code, assets, or design.

## Status (v0.1.48-strokeboth)

- **v0.1.48-strokeboth:** filled crescent both ways; strokeWidth player vs enemy (core 0.14/0.18/0.10); peaks 500/450; F3 locked; Wake/Brace frozen. Tagged release. Still debug.
- **v0.1.47-strokethick:** thicken Wake-family stroke (core fracs 0.14/0.18/0.10; glow 0.30/0.38/0.22; COLOR_STROKE_YOU=0xFFF2E6D0); fix `FX stroke on You`; F3 still locked; Wake/Brace frozen. Tagged release. Still debug.
- **v0.1.46-strokefallback:** drawn Wake-family stroke primary (not CLEAVE sheet scale); F3 still locked; Wake/Brace frozen. Tagged release. Still debug.
- **v0.1.45-slashscale:** Sheet is loaded. Scale the overlay. Color may match art. F3 still locked. Wake/Brace frozen. Tagged release. Still debug.
- **v0.1.44-slashproof:** Prove slash-light is loaded and readable (atlas content crop + bust-diameter scale; debug `FX slash-light on <target>`). F3 still locked. Wake/Brace frozen. Tagged release. Still debug.
- **v0.1.43-slashread:** Make CLEAVE slash-light actually readable on busts (scale ~60–80% bust, Medium 1.3×, peak hold 200/220ms, contact hit-flash). F3 still locked. Wake/Brace frozen. Tagged release. Still debug.
- **v0.1.42-cleavekit:** CLEAVE slash-light + hit-flash into 0.1.40 FX kernel (Player/Enemy Small+Medium; Ashbrand spark→additive hit-flash on foe; Club/Gate Pulse→Enemy Small). Brace/Wake/F3 frozen. Tagged release. Still debug.
- **v0.1.41-floor3:** Floor 3 + Cave Troll x3 + floor-locked loadout from F3. F1/F2 frozen. Tagged release. Still debug.
- **v0.1.40-fxfix:** no screen flash, shields back, target-only slash. Tagged release. Still debug.
- **v0.1.39-fxread:** bigger Brace shields, heavier slash, aim frozen. Tagged release. Still debug.
- **v0.1.38-fxaim:** target-only slash, Brace shields on owner; kernel kept. Tagged release. Still debug.
- **v0.1.37-fx:** shared combat FX kernel (flash→stroke→float→shake); Wake clip frozen. Tagged release. Still debug.
- **v0.1.36-climbintro:** Title→Climb skippable full-screen intro clip (Media3); Resume/Hub live run never plays; missing asset silent skip. Combat frozen. Tagged release. Still debug.
- **v0.1.35-emberpool:** 4 Hub-gated Ashbrand Spark skills (Ember Draw / Brand Mark / Spark Tithe / Wake Echo). Tagged release. Still debug.
- **v0.1.34-blade:** Ashbrand still in tile (portrait); glossary frozen. Tagged release. Still debug.
- **v0.1.33-ashbrand:** Ashbrand still (crop-only icon, no plate) + tap glossary (Ember/Spark/Wake links). Wake math frozen. Tagged release. Still debug.
- **v0.1.32-enemykit:** enemy 3-card kits, glossary tap, Soften on next damage skill. Enemy live kits (same weight/grey as player bar). Soften → next damaging kit skill; Nip ignores Soften; Hide family Brace. 3 tiles under enemy HP. Tagged release. Still debug.
- **v0.1.31-floorart:** Path **backdrop** + **node type tokens** (fog until scout; Boss letter **B**). Captions kept. No Hub/Wake/2x/path-weight changes. Tagged release. Still debug.
- **v0.1.29-hubmore:** Hub **7 offers** — keep four; add **Hostblood** (+2 climb max, start full) / **Warm Ash** (Brace 2 first combat) / **Ash Tithe** (+3 rem at summary). Unlock ids only; bank/OWNED hubkeep intact. Tagged release. Still debug.
- **v0.1.28-hubkeep:** Hub stacks; baseline rumor restored (1/floor); Extra→2; Scout only if owned; no bank wipe; migration OWNED maps. Tagged release. Still debug.
- **v0.1.27-hub:** Hub **4 offers** (Scout **8** / Extra rumor **6** / Host of Embers **12** / Iron Lesson **12**); gated skills **Cinder Vow** / **Grave Nail**; MetaStore bank+unlocks persist; Art hub glyphs. Tagged release. Still debug.
- **v0.1.26-packs:** Hallway trash **Weak Goblin** / **Sturdy Orc**, **3 looks** each; F1 **75/25** F2 **30/70**. Tagged release. Still debug.
- **v0.1.24-rumorcharge:** Rumor + scout charges **decrement** and **block at 0**; art frozen. Tagged release. Still debug.
- **v0.1.23-nobg:** Combat portrait plates **removed** (PNG only). Title teal circle kept. Tagged release. Still debug.
- **v0.1.22-plate:** Freeze portrait plate — **static fill**; **no pulse**; victory tint **once**. Art frozen. Tagged release. Still debug.
- **v0.1.21-portraits:** Three attached stills slotted — **You**, **Ash-Warden**, **Seal-Warden** (no redraw). Trash placeholders unchanged. Wake / glyphs / volume / path / 2x / save frozen. Tagged release. Still debug.
- **v0.1.20-volume:** Soft **drop shadow** + **gold-ash** top-left rim + **2-tone folds** on You / Ash-Warden / skill glyphs / Ashbrand icon (Art bake; Compose chrome no-op). No 3D engine. Vow Plate = heavy plate distinct from Iron Mantle. Wake / path / 2x / save frozen; Seal-Warden / trash placeholders unchanged. Tagged release. Still debug.
- **v0.1.19-glyphs:** Skill tile **glyphs** only (24–32 dp) on combat bar + loadout; color = job (Brace green outline). Ashbrand blade only — no second glyph. Placeholder `glyph_*.png` (Art gen) (Art drop-in same names). Bodies / Wake art **frozen**. No combat math / path / 2x / save / pip changes. Tagged release. Still debug.
- **v0.1.18-bodies:** Player soldier **circular still** (`portrait_you`) + Ash-Warden F2 boss still only (`portrait_ash_warden`). Trash / Seal Spinner / F1 Seal-Warden keep old placeholders. Hallway You=soldier / enemy=old circle. Wake art **frozen**. Still debug.
- **v0.1.17-wakespeck:** Full Wake speck polish — **8–12 staggered gold/ash dots** along crescent + one **~8–12 px** impact burst on enemy at arrival (1-frame). Icon + crescent stroke **frozen** at 0.1.16. SPARK ember-only (no crescent/dots/burst). Same drawable names / holds / Wake math. All Wake art frozen after ship.
- **v0.1.16-wakeicon:** Wake art polish + freeze — Ashbrand cracked ash-iron + gold fuller on dark square (~48 dp); FULL crescent **thicker gold stroke (2–3×)** + **8–12 ash dots**; impact = one spark burst (not second slash). SPARK ember-only. Same beats/holds/drawable names. No Wake math / SFX / frame-count change.
- **v0.1.15-wakeart:** Ashbrand **icon** on loadout + combat weapon slot; FULL Wake **charge → crack → crescent** (wake_vfx_charge/slash/impact) on portrait stage with existing gold log + WAKE float; SPARK = tiny ember on icon only (no crescent). 2x halves frame holds. Placeholder bodies. No Wake math change.
- **v0.1.14-bracesync:** Brace hit **draw sync** — pip ticks / absorb float (−N on pip) **before** leftover HP bar; Brace “0” holds one beat then hides. Soften / absorb math unchanged.
- **v0.1.13-statuspips:** Brace / Soften **status pips** under fighter HP bars (glyph + remaining count); tap → glossary. Mirrors `brace` / `counterPenalty`; hide at 0. No damage retune.
- **v0.1.12-save:** one mid-run save slot (DataStore). Writes on node resolve / F1 stair Continue / loadout lock; Menu **Continue** resumes the run. No mid-beat save (never mid-dice or mid-Wake). **New climb** confirm-wipes the slot (meta bank / owned perks kept). `rng_seed` required.
- **v0.1.11-2x:** combat **1x ↔ 2x** toggle (active-rate label); timings are halved at 2x; dice/log/float/grey-out stay in sync; mid-fight toggle applies to the next beat. No 3x / no skip / no balance change.
- **v0.1.10-bossrest:** every Start→Boss path’s **last non-boss node is REST**; Rest Heal → **MAX HP**; Keen Eye Free Scout tap on fogged `?` reveals type and spends a charge (no enter). CARE-everywhere guarantee stays withdrawn.
- **v0.1.10-routecare reversed:** CARE (REST|SHOP) on every S→B removed earlier; tag/release `v0.1.10-routecare` left for history. Do **not** recreate CARE-everywhere.
- **Floor 2 climb:** Seal-Warden win → "The stair turns." → Floor 2 path (same run; no Hub). Ash-Warden clear → Floor 3 path (same run; loadout re-lock). Gate-Warden clear → summary → Hub.
- Persist across floor break: HP (no heal-to-full), remnants wallet, locked loadout, Ashbrand level/charge, unlocks-this-run
- Floor 2 trash HP **24** (F1 still **20**); Ash-Warden HP **32** (Seal-Warden still **28**); counters unchanged
- Path / Wake / swap lock / shop wallet / remnant gains: unchanged from v0.1.8 / v0.1.7 aside from bossrest path + Rest Heal
- Path: **every Start→Boss path has ≥1 COMBAT before pre-boss REST** (v0.1.7 + bossrest); **event cap ≤1** per floor; **no CARE guarantee**
- Treasure: Gain rolls **once on enter**; Cancel does not reroll (v0.1.6-swaplock)
- Weapon XP: full Wake + combat win → **+1 Ashbrand level**
- 5-skill exhaust bar + Ashbrand; Lv1 Wake threshold **3**; trash counters **7–9**; boss **6–9** / HP **28**
- Placeholders for art/SFX
- Still debug / placeholders.

### Measured n=2000 from v0.1.4-wake (default 5 + Ashbrand lv1)
| Cohort | WR | Mean skill beats |
|--------|-----|------------------|
| Ash Wretch | 97.95% | 4.41 |
| Ruin Brute | 97.95% | 4.41 |
| Seal Spinner | 97.95% | 4.41 |
| Stone Hunger | 97.95% | 4.41 |
| Seal-Warden | 23.45% | 5.32 |

## Debug APK

Built locally as `tower-of-darkness-debug.apk` (see repo root after assemble).

[Release v0.1.48-strokeboth](https://github.com/Cptjswallowz/tower-of-darkness/releases/tag/v0.1.48-strokeboth)
Direct: [tower-of-darkness-debug.apk](https://github.com/Cptjswallowz/tower-of-darkness/releases/download/v0.1.48-strokeboth/tower-of-darkness-debug.apk)

## Build

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/home/box/deps/android-sdk
./gradlew :app:assembleDebug
```

Docs: `docs/qa-gate-v0148-strokeboth-2026-09-26.md`, `docs/strokeboth-v0148.md`, `docs/qa-gate-v0147-strokethick-2026-09-26.md`, `docs/strokethick-v0147.md`, `docs/qa-gate-v0146-strokefallback-2026-09-26.md`, `docs/strokefallback-v0146.md`, `docs/qa-gate-v0145-slashscale-2026-09-26.md`, `docs/slashscale-v0145.md`, `docs/qa-gate-v0144-slashproof-2026-09-26.md`, `docs/slashproof-v0144.md`, `docs/qa-gate-v0143-slashread-2026-09-26.md`, `docs/slashread-v0143.md`, `docs/art-audio/SLASHREAD_v0.1.43.md`, `docs/qa-gate-v0142-cleavekit-2026-09-26.md`, `docs/cleavekit-v0142.md`, `docs/art-audio/CLEAVE_v0.1.42.md`, `docs/qa-gate-v0141-floor3-2026-09-26.md`, `docs/floor3-v0141.md`, `docs/floor3-save-v0141.md`, `docs/qa-gate-v0140-fxfix-2026-09-26.md`, `docs/fxfix-v0140.md`, `docs/qa-gate-v0139-fxread-2026-09-26.md`, `docs/fxread-v0139.md`, `docs/qa-gate-v0138-fxaim-2026-09-26.md`, `docs/qa-gate-v0137-fx-2026-09-26.md`, `docs/qa-gate-v0136-climbintro-2026-09-26.md`, `docs/climbintro-v0136.md`, `docs/qa-gate-v0135-emberpool-2026-09-25.md`, `docs/emberpool-v0135.md`, `docs/qa-gate-v0134-blade-2026-09-25.md`, `docs/blade-v0134.md`, `docs/qa-gate-v0133-ashbrand-2026-09-25.md`, `docs/ashbrand-v0133.md`, `docs/art-audio/ASHBRAND_v0.1.33.md`, `docs/qa-gate-v0132-enemykit-2026-09-25.md`, `docs/enemykit-v0132.md`, `docs/qa-gate-v0131-floorart-2026-09-25.md`, `docs/floorart-v0131.md`, `docs/art-audio/FLOOR_ART_v0.1.31.md`, `docs/qa-gate-v0129-hubmore-2026-09-25.md`, `docs/hubmore-v0129.md`, `docs/qa-gate-v0128-hubkeep-2026-09-24.md`, `docs/hubkeep-v0128.md`, `docs/qa-gate-v0127-hub-2026-09-24.md`, `docs/hub-v0127.md`, `docs/art-audio/HUB_GLYPHS_v0.1.27.md`, `docs/qa-gate-v0126-packs-2026-09-24.md`, `docs/packs-v0126.md`, `docs/art-audio/PACKS_v0.1.26.md`, `docs/qa-gate-v0124-rumorcharge-2026-09-24.md`, `docs/rumorcharge-v0124.md`, `docs/qa-gate-v0123-nobg-2026-09-24.md`, `docs/nobg-v0123.md`, `docs/art-audio/NOBG_v0.1.23.md`, `docs/qa-gate-v0122-plate-2026-09-24.md`, `docs/plate-v0122.md`, `docs/art-audio/PLATE_v0.1.22.md`, `docs/qa-gate-v0121-portraits-2026-09-24.md`, `docs/portraits-v0121.md`, `docs/art-audio/PORTRAITS_v0.1.21.md`, `docs/qa-gate-v0120-volume-2026-09-23.md`, `docs/volume-v0120.md`, `docs/art-audio/VOLUME_v0.1.20.md`, `docs/qa-gate-v0119-glyphs-2026-09-23.md`, `docs/glyphs-v0119.md`, `docs/art-audio/GLYPHS_v0.1.19.md`, `docs/qa-gate-v0118-bodies-2026-09-23.md`, `docs/bodies-v0118.md`, `docs/art-audio/BODIES_v0.1.18.md`, `docs/qa-gate-v0117-wakespeck-2026-09-23.md`, `docs/wake-speck-v0117.md`, `docs/art-audio/WAKE_ART_v0.1.17.md`, `docs/wake-icon-v0116.md`, `docs/art-audio/WAKE_ICON_v0.1.16.md`, `docs/qa-gate-v0116-wakeicon-2026-09-23.md`, `docs/wake-art-v0115.md`, `docs/art-audio/WAKE_ART_v0.1.15.md`, `docs/qa-gate-v0115-wakeart-2026-09-23.md`, `docs/brace-sync-v0114.md`, `docs/qa-gate-v0114-bracesync-2026-09-23.md`, `docs/status-pips-v0113.md`, `docs/midrun-save-v0112.md`, `docs/save-v0112.md`, `docs/combat-2x-v0111.md`, `docs/bossrest-v0110.md`, `docs/floor2-v019.md`, `docs/wake-v014.md`, `docs/qa-gate-v015-path-2026-09-21.md`, `docs/qa-gate-v016-swaplock-2026-09-21.md`, `docs/qa-gate-v017-routefight-2026-09-22.md`, `docs/qa-gate-v018-shopwallet-2026-09-22.md`, `docs/qa-gate-v019-floor2-2026-09-22.md`, `docs/qa-gate-v0110-bossrest-2026-09-22.md`, `docs/qa-gate-v0112-save-2026-09-23.md`, `docs/qa-gate-v0113-statuspips-2026-09-23.md`.
