# Tower of Darkness

Android vertical slice — Kotlin + Jetpack Compose (`com.towerofdarkness.app`).

**Separate** from Tower of the World: no shared code, assets, or design.

## Status (v0.1.13-statuspips)

- **v0.1.13-statuspips:** Brace / Soften **status pips** under fighter HP bars (glyph + remaining count); tap → glossary. Mirrors `brace` / `counterPenalty`; hide at 0. No damage retune.
- **v0.1.12-save:** one mid-run save slot (DataStore). Writes on node resolve / F1 stair Continue / loadout lock; Menu **Continue** resumes the run. No mid-beat save (never mid-dice or mid-Wake). **New climb** confirm-wipes the slot (meta bank / owned perks kept). `rng_seed` required.
- **v0.1.11-2x:** combat **1x ↔ 2x** toggle (active-rate label); timings are halved at 2x; dice/log/float/grey-out stay in sync; mid-fight toggle applies to the next beat. No 3x / no skip / no balance change.
- **v0.1.10-bossrest:** every Start→Boss path’s **last non-boss node is REST**; Rest Heal → **MAX HP**; Keen Eye Free Scout tap on fogged `?` reveals type and spends a charge (no enter). CARE-everywhere guarantee stays withdrawn.
- **v0.1.10-routecare reversed:** CARE (REST|SHOP) on every S→B removed earlier; tag/release `v0.1.10-routecare` left for history. Do **not** recreate CARE-everywhere.
- **Floor 2 climb:** Seal-Warden win → "The stair turns." → Floor 2 path (same run; no Hub). Ash-Warden clear → summary **"Victory — The seal breaks."** → Hub. No Floor 3.
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

[Release v0.1.12-save](https://github.com/Cptjswallowz/tower-of-darkness/releases/tag/v0.1.12-save)
Direct: [tower-of-darkness-debug.apk](https://github.com/Cptjswallowz/tower-of-darkness/releases/download/v0.1.12-save/tower-of-darkness-debug.apk)

## Build

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/home/box/deps/android-sdk
./gradlew :app:assembleDebug
```

Docs: `docs/status-pips-v0113.md`, `docs/midrun-save-v0112.md`, `docs/save-v0112.md`, `docs/combat-2x-v0111.md`, `docs/bossrest-v0110.md`, `docs/floor2-v019.md`, `docs/wake-v014.md`, `docs/qa-gate-v015-path-2026-09-21.md`, `docs/qa-gate-v016-swaplock-2026-09-21.md`, `docs/qa-gate-v017-routefight-2026-09-22.md`, `docs/qa-gate-v018-shopwallet-2026-09-22.md`, `docs/qa-gate-v019-floor2-2026-09-22.md`, `docs/qa-gate-v0110-bossrest-2026-09-22.md`, `docs/qa-gate-v0112-save-2026-09-23.md`.
