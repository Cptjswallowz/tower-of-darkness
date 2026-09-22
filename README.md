# Tower of Darkness

Android vertical slice — Kotlin + Jetpack Compose (`com.towerofdarkness.app`).

**Separate** from Tower of the World: no shared code, assets, or design.

## Status (v0.1.8-shopwallet — WIP)

- Shop: **Small Heal (+8) = 3 rem**; wallet ≥3 always gets ≥1 affordable offer; wallet 0–2 → "Nothing you can buy." + Leave (no grey stock); heal offers blocked/labeled **already full** at cap HP
- Path / Wake / swap lock / counters / HP / remnant gains: unchanged from v0.1.7
- No Floor 2.
- Path: **every Start→Boss path has ≥1 COMBAT**; **event cap 1** per floor
- Treasure: Gain rolls **once on enter**; Cancel does not reroll (v0.1.6-swaplock)
- Weapon XP: full Wake + combat win → **+1 Ashbrand level**
- 5-skill exhaust bar + Ashbrand; Lv1 Wake threshold **3**; trash counters **7–9**; boss **6–9** / HP **28**
- Placeholders for art/SFX
- Still debug / no mid-run save — not shippable

### Measured n=2000 from v0.1.4-wake (default 5 + Ashbrand lv1)
| Cohort | WR | Mean skill beats |
|--------|-----|------------------|
| Ash Wretch | 97.95% | 4.41 |
| Ruin Brute | 97.95% | 4.41 |
| Seal Spinner | 97.95% | 4.41 |
| Stone Hunger | 97.95% | 4.41 |
| Seal-Warden | 23.45% | 5.32 |

## Debug APK

[Release v0.1.8-shopwallet](https://github.com/Cptjswallowz/tower-of-darkness/releases/tag/v0.1.8-shopwallet)
Direct: [tower-of-darkness-debug.apk](https://github.com/Cptjswallowz/tower-of-darkness/releases/download/v0.1.8-shopwallet/tower-of-darkness-debug.apk)

## Build

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/home/box/deps/android-sdk
./gradlew :app:assembleDebug
```

Docs: `docs/wake-v014.md`, `docs/qa-gate-v015-path-2026-09-21.md`, `docs/qa-gate-v016-swaplock-2026-09-21.md`, `docs/qa-gate-v017-routefight-2026-09-22.md`, `docs/qa-gate-v018-shopwallet-2026-09-22.md`.
