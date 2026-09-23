# QA Gate: v0.1.14-bracesync (Compose Android)

**Date:** 2026-09-23 (America/New_York, EDT)  
**Project root:** `/workspace/tower-of-darkness`  
**Scope:** Brace hit **draw sync** only (pip → absorb float → HP). No absorb math / Wake / 2x / save / path / shop / swap / bossrest retune.  
**Hard exclusion:** Godot / tower-of-the-world not touched.

## Checklist (engineer)

- [x] On hit: pip update → float `−N` on pip → then HP bar leftover
- [x] Same-beat: honor existing log/resolver order; no invented pre-block
- [x] Brace 0: show “0” this beat; hide next beat
- [x] Soften unchanged; frozen systems untouched
- [x] Unit tests: BraceSyncV0114Test + StatusPipsV0113Test
- [x] APK copied to repo root; **no tag / no GitHub release**

## Tests

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

BraceSyncV0114Test (7) + StatusPipsV0113Test (8) + full suite: see commit notes.
