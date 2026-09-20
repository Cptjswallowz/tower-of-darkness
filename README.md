# Tower of Darkness

Vertical-slice Android game: high-fantasy roguelike deck-builder dungeon crawler.
Kotlin · Jetpack Compose · Material3 · single Activity.

Package: `com.towerofdarkness.app`

## Build debug APK

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/home/box/deps/android-sdk
export ANDROID_SDK_ROOT=/home/box/deps/android-sdk
cd /workspace/tower-of-darkness
./gradlew :app:assembleDebug
```

`local.properties` already sets `sdk.dir=/home/box/deps/android-sdk`.

APK output:
- `app/build/outputs/apk/debug/app-debug.apk`
- Dated handoff copy: `tower-of-darkness-debug.apk` (after successful build)

compileSdk / targetSdk **35**, minSdk **26**.

## What’s playable end-to-end

Happy path state machine:
**Menu → Tutorial (first run) → Loadout → Path → node resolve (Combat / Shop / Rest / Event / Treasure) → Boss → Run Summary → Hub → Climb again**

- Main menu with modular hero showcase (Compose Canvas layers + rarity glow)
- Tutorial with rumor → path → loadout → combat beats; Skip after rumor + loadout
- Path: branching graph, rumor fog, scout type-only reveal
- Loadout: 11-card pool (cards-v0), pick 5–6, confirm gated; locks on first Path→resolve leave
- Dice auto-combat with Brace, floating text, legendary/rare charge+shake; Flee grayed
- Boss HP **28**, boss counter **8–12**; normal enemy HP **20**, counter **7–11**
- Shop prices **5–15** remnants; Rest Heal/Scout; Event A/B; Treasure remnants or rare unlock swap
- Run Summary + Meta Hub unlocks (cheapest card unlock **15**)

## What’s stubbed

- Settings screen
- Mid-run save/resume (DataStore persists tutorial/remnants/unlocks only)
- PNG hero/enemy layers optional (Compose shapes used; PH assets under `app/src/main/assets/art/` + `audio/sfx/` wired via SoundPool with ToneGenerator fallback)
- Shop card-swap UI is auto-swap (not a full picker)
- Win-rate ~84% / ~3.6 rounds are **targets** in `Balance` / docs — not measured

## Balance constants

See `app/.../domain/Balance.kt` and `docs/balance-targets.md`.

## Known blockers

- None for assembleDebug if SDK platform 35 + build-tools 35.0.0 present (they are on this box).

## Layout

```
app/src/main/java/com/towerofdarkness/app/
  MainActivity.kt
  data/MetaStore.kt
  domain/  Balance, Rarity, effects/, cards/, combat/, path/, glossary/, sound/
  nav/     NavState, GameController
  ui/      theme/, components/, screens/
```
