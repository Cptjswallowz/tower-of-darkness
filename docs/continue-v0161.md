# Continue — v0.1.61-continue

Status: **implemented** (Android). HOLD tag — no gh release.

**Not** a reuse of 0.1.60-unstick. Phone FAIL on 0.60 (Seal-Warden 0/28, empty stone well, no Victory/Continue, Flee gone) required a real layout + phase fix.

## Root cause (stone under Ashbrand)

**File that drew stone into the log+bottom well:** `app/src/main/java/com/towerofdarkness/app/ui/components/TilePlateBackdrop.kt` (`SharedTilePlateBox`).

`WeaponBar` hosts `SharedTilePlateBox` with wrap-content height. The plate `Image` used `Modifier.fillMaxSize()`, which proposed the Column's maxHeight upward and **expanded the stone plate into the rectangle under Ashbrand** (log + bottom button). Log got crushed; Continue/Flee clipped or invisible.

**Fix:** `Image` → `Modifier.matchParentSize()` so the plate sizes to the content-measured Box and cannot expand into the log well. Log Column stays plate-free (GlossaryText only).

## Win check function (`CombatEngine.applyWinCheck`)

```kotlin
fun applyWinCheck(state: CombatState): CombatState {
    if (state.phase == CombatPhase.DEFEAT || state.playerHp <= 0) {
        return if (state.phase == CombatPhase.DEFEAT) state
        else state.copy(
            phase = CombatPhase.DEFEAT,
            finished = true,
            playerWon = false,
            beat = CombatBeat.AWAITING_CONTINUE
        )
    }
    if (state.enemy.hp <= 0 && state.phase != CombatPhase.DEFEAT) {
        if (state.phase == CombatPhase.VICTORY && state.finished && state.playerWon) {
            return state.copy(beat = CombatBeat.AWAITING_CONTINUE)
        }
        return forceVictory(state)
    }
    return state
}
```

Also: `forceVictory`, UI `forceVictoryFromUi` after `Balance.COMBAT_END_FORCE_MS` (400ms), debug `COMBAT_END win foe=0 you=N phase=VICTORY`.

## Packaging

versionCode **62** · versionName **0.1.61-continue**. HOLD tag.
