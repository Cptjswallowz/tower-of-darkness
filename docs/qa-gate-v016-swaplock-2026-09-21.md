# QA Gate — v0.1.6-swaplock (2026-09-21)

**Project:** Tower of Darkness (Compose Android only)  
**Scope:** Measure-only gate for WO v0.1.6-swaplock. No balance / path / Wake / HP retunes.  
**Gate time:** 2026-09-21 20:29 EDT

---

## 1) APK + commit

| Item | Measured |
|------|----------|
| Path | `/workspace/tower-of-darkness/tower-of-darkness-debug.apk` |
| Size | **17710732** bytes |
| mtime | **2026-09-21 20:28:40 -0400** (EDT) |
| `git log -1` | `c7adc84 v0.1.6-swaplock: lock treasure Gain for the visit` |
| `git rev-parse --short HEAD` | **c7adc84** |
| Branch | `main...origin/main` (clean match to reported commit) |

Command evidence: `ls -la` / `stat -c '%s %y'`; `git log -1 --oneline`; `git rev-parse --short HEAD`.

---

## 2) Unit tests — TreasureSwapLockTest

**Command:**
```bash
./gradlew :app:testDebugUnitTest --tests '*TreasureSwapLockTest'
```
(from `/workspace/tower-of-darkness`)

**Gradle:** **BUILD SUCCESSFUL** (22 actionable; `:app:testDebugUnitTest` executed)

**JUnit XML:** `app/build/test-results/testDebugUnitTest/TEST-com.towerofdarkness.app.TreasureSwapLockTest.xml`  
`tests="3" failures="0" errors="0" skipped="0"`

| Test method | Result |
|-------------|--------|
| `treasureVisit_twoPreviews_sameGainId` | **PASS** |
| `treasureVisit_cancelDoesNotRerollGain` | **PASS** |
| `pickTreasureGainId_neverInLoadout` | **PASS** |

No assertion failures / error messages in XML `system-out` / `system-err`.

**Related tests:** Only `TreasureSwapLockTest.kt` under swap/treasure unit tests (no other swap/treasure `*Test.kt` in `app/src/test`).

---

## 3) Spot-check (code review; not device UI)

### a) Confirm consumes node; NO second Gain roll this visit — **PASS**

- Enter rolls Gain once: `GameController.kt:265-268` (`treasureSwapGainId = pickTreasureGainId(...)` on `NodeType.TREASURE`).
- Preview never re-rolls if already set: `GameController.kt:638-646` (`treasureBeginSwap` — `if (treasureSwapGainId == null)` only).
- Confirm uses locked `treasureSwapGainId`, applies loadout swap, then `clearTreasureVisit()` + `returnToPathAfterResolve()`: `GameController.kt:649-664`.
- Path consume: `returnToPathAfterResolve()` at `GameController.kt:448-451` → `path?.markCurrentCleared()`, `nodesCleared++`, `nav = Path`.
- Pure visit model (unit-tested): `TreasureVisit.beginPreview` copies `loseId` only — `GameController.kt:418-424`; enter via `treasureVisitEnter` rolls once — `:427-431`.
- UI: Confirm button → `gc.treasureConfirmSwap()` — `TreasureScreen.kt:98-99`.

### b) Cancel does not change Gain — **PASS**

- `treasureCancelSwap()` clears Lose only; keeps Gain: `GameController.kt:667-669`.
- `TreasureVisit.cancelPreview()` = `copy(loseId = null)` — `GameController.kt:424`.
- UI: Cancel → `gc.treasureCancelSwap()` — `TreasureScreen.kt:102-103`.
- Matches `treasureVisit_cancelDoesNotRerollGain` (seeds 0..31).

### c) Remnants path still works — **PASS**

- `treasureRemnants()`: `clearTreasureVisit()`, `runWallet += Balance.TREASURE_REMNANTS`, `returnToPathAfterResolve()` — `GameController.kt:632-635`.
- UI still exposes Take remnants when not previewing: `TreasureScreen.kt:49-52`.
- Swap lock does not gate remnants; remnants clears visit state then advances path same as confirm.

### d) No second roll on confirm — **PASS**

Confirm reads existing `treasureSwapGainId` (`:651`); does not call `pickTreasureGainId`. Clear happens after swap (`:662`), then leave node.

---

## 4) Gate verdict

| Check | Verdict |
|-------|---------|
| APK present + size/mtime recorded | PASS |
| HEAD == c7adc84 | PASS |
| `treasureVisit_twoPreviews_sameGainId` | PASS |
| `treasureVisit_cancelDoesNotRerollGain` | PASS |
| Related `pickTreasureGainId_neverInLoadout` | PASS |
| Spot-check Confirm / Cancel / Remnants / no 2nd roll | PASS |

# Gate: PASS

Evidence: Gradle BUILD SUCCESSFUL + JUnit 3/3 pass + code citations above. No production balance code modified by this gate.
