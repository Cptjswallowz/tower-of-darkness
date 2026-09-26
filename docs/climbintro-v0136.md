# Climb intro — v0.1.36-climbintro

Status: **shipped** (feature tip). Title → Climb plays a fullscreen intro clip, then Floor 1.

**WO:** v0.1.36-climbintro  
**Frozen:** combat, Wake, Hub offers/shop logic, path, skills, You combat portrait.

---

## Behavior

1. **Title (MainMenu) → Climb** (no mid-run slot) → `NavState.ClimbIntro` fullscreen clip (dark behind) → then existing `beginClimbFresh` (Tutorial if unseen, else Path / Floor 1).
2. **New climb** (wipe confirm when a mid-run slot exists) → same intro → fresh climb. Meta bank / OWNED untouched.
3. **Skip:** tap anywhere **or** visible **Skip** label aborts playback → Floor 1 immediately. Does not block input for the full ~15s.
4. **Resume / Continue** (`continueClimb`, cold-start mid-run restore) → **no** intro.
5. **Hub:** no Climb CTA; Hub → Back to title only. Live mid-run resume never routes through ClimbIntro.
6. **Missing asset** → silent skip (no crash, no toast); Climb still goes to Floor 1 via `beginClimbFresh`.
7. **No loop.** No title-idle autoplay. Clip only on fresh Title Climb / New climb wipe→fresh.
8. **You combat portrait** unchanged.

---

## Asset

| Item | Value |
|------|--------|
| App path | `app/src/main/assets/climb_intro.mp4` |
| Playback URI | `asset:///climb_intro.mp4` |
| md5 | `ead7b72c2a37c09b82f65ff015bfc29d` |
| size | 14122212 bytes |
| duration | ~15.04 s |
| encode | h264 784×1168 @ 24 fps (Art CONFIRM raw; **no** re-encode / crop) |

Oval: letterbox OK (`RESIZE_MODE_FIT`); do not crop the gold oval.

---

## Implementation

- `NavState.ClimbIntro` between Title and Tutorial/Path.
- `ClimbIntroGate.shouldShowIntro(freshClimb, assetPresent)` — unit-tested gate.
- `GameController.climb` / `confirmNewClimb` → `enterClimbIntroOrFresh`; `finishClimbIntro` → `beginClimbFresh`.
- `ClimbIntroScreen` — Media3 ExoPlayer + PlayerView; release on dispose; dark full-screen.
- Gradle: `androidx.media3:media3-exoplayer` + `media3-ui` **1.9.4**.

---

## QA left

Play-path / device video QA (skip mid-clip, resume never plays, missing-asset silent skip on device). Unit tests cover gate + asset md5 only.
