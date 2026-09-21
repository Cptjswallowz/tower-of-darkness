# Tower of Darkness

Android vertical slice — Kotlin + Jetpack Compose (`com.towerofdarkness.app`).

**Separate** from Tower of the World: no shared code, assets, or design.

## Status (v0.1.3-combatbar)

- 5-skill loadout (exact) + Ashbrand weapon slot (CHAIN + SPARK)
- Exhaust bar: dice weighted among live slots; grey until cycle reset
- Combat beats: dice → skill → read hold → weapon → enemy; Continue after end
- Soft-lock / phone UI passes from earlier builds
- Placeholders for art/SFX; **no mid-run save**
- Debug APK only — not shippable

### Balance notes (measured n=2000)
- Ash Wretch: ~50.7% WR (default 5 + Ashbrand lv1)
- Seal-Warden: **23.45%** WR, mean skill beats ~5.3 — boss counter **6–9**, Boss HP **28**

## Debug APK

[Release v0.1.3-combatbar](https://github.com/Cptjswallowz/tower-of-darkness/releases/tag/v0.1.3-combatbar)  
Direct: [tower-of-darkness-debug.apk](https://github.com/Cptjswallowz/tower-of-darkness/releases/download/v0.1.3-combatbar/tower-of-darkness-debug.apk)

## Build

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/home/box/deps/android-sdk
./gradlew :app:assembleDebug
```

Docs under `docs/` (see `combat-bar-v013.md`, `qa-sim-v013-boss69-2026-09-20.md`).
