# Tower of Darkness

Android vertical slice — Kotlin + Jetpack Compose (`com.towerofdarkness.app`).

**Separate** from Tower of the World: no shared code, assets, or design.

## Status (v0.1.5-path)

- Path: **≥1 COMBAT** node before boss; **events ≤1** per floor
- Treasure: Lose / Gain **preview** before commit; Cancel keeps current card; remnants option unchanged
- Weapon XP: full Wake + combat win → **+1 Ashbrand level** before next node (same-run fix)
- 5-skill exhaust bar + Ashbrand (CHAIN full Wake + quiet SPARK)
- **FULL Wake** legendary beat: gold `ASHBRAND — WAKE N`, float WAKE, pin in log, ~2300ms
- Lv1 Wake threshold **3** (dmg still 4/6/8)
- Trash counters **7–9**; boss counter **6–9**, Boss HP **28**
- Placeholders for art/SFX; **no mid-run save**
- Debug APK only — not shippable

### Measured n=2000 from v0.1.4-wake (default 5 + Ashbrand lv1)
| Cohort | WR | Mean skill beats |
|--------|-----|------------------|
| Ash Wretch | 97.95% | 4.41 |
| Ruin Brute | 97.95% | 4.41 |
| Seal Spinner | 97.95% | 4.41 |
| Stone Hunger | 97.95% | 4.41 |
| Seal-Warden | 23.45% | 5.32 |

## Debug APK

[Release v0.1.5-path](https://github.com/Cptjswallowz/tower-of-darkness/releases/tag/v0.1.5-path)  
Direct: [tower-of-darkness-debug.apk](https://github.com/Cptjswallowz/tower-of-darkness/releases/download/v0.1.5-path/tower-of-darkness-debug.apk)

## Build

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/home/box/deps/android-sdk
./gradlew :app:assembleDebug
```

Docs: `docs/wake-v014.md`, `docs/qa-gate-v015-path-2026-09-21.md`.
