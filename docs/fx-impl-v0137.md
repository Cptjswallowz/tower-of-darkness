# Combat FX kernel — impl notes (v0.1.37-fx)

Design source: `docs/fx-v0137.md`. Presentation only — no damage / Wake math change.

## Files

| File | Role |
|------|------|
| `domain/combat/CombatFx.kt` | Pure maps (tier / color / duration / stroke). Unit-testable. |
| `domain/combat/CombatEngine.kt` | Sets `CombatEvent.fxId` / `fxPlayer` on **primary** resolve lines only. |
| `ui/components/CombatFxOverlay.kt` | Stroke + tile flash Compose. |
| `ui/screens/CombatScreen.kt` | Kernel order: flash → stroke → float → shake; 2x via `CombatFx.fxHoldMs`. |
| `WakeArt.kt` / `WakeStageOverlay.kt` | Unchanged Wake slash sequence; FX layers heavier shake on Wake tier. |

## Rules

- Follow-up Brace / Soften / Spark-spent lines: `fxId = null` (no second stroke).
- Wake Echo: one Medium stroke for combined damage line.
- Wake tier id: `CombatFx.ID_ASHBRAND_WAKE` only.
- Fail-safe: `safeSpec` + UI try/catch; resolve+log never blocked.
- Hold timings in `GameController.combatHold` unchanged.
