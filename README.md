# Tower of Darkness

Android vertical slice — Kotlin + Jetpack Compose (`com.towerofdarkness.app`).

**Separate** from Tower of the World: no shared code, assets, or design.

## Status

- Navigable slice loop in source (Menu → Tutorial → Path → Loadout → nodes → Boss → Summary → Hub)
- Soft-lock P1 pass (Skip→default-5, Rest Leave, Hub ladder, Path-before-Loadout, scout_charge)
- Art/SFX are placeholders (SFX are distinct stubs, not production)
- Mid-run save not included yet
- Debug APK only — not shippable

## Debug APK

[Release v0.1.0-slice](https://github.com/Cptjswallowz/tower-of-darkness/releases/tag/v0.1.0-slice)  
Direct: [tower-of-darkness-debug.apk](https://github.com/Cptjswallowz/tower-of-darkness/releases/download/v0.1.0-slice/tower-of-darkness-debug.apk)

## Build

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/home/box/deps/android-sdk
./gradlew :app:assembleDebug
```

Docs live under `docs/`.
