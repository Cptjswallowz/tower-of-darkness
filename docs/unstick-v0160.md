# Unstick — v0.1.60-unstick

Status: **implemented** (Android). Plate map: `docs/art-audio/UNSTICK_PLATE_MAP_v0.1.60.md`.

**Frozen:** remnants / Forge costs / relics / SFX / FX. No music. No Title/Hub restyle.

## PART A — Combat end softlock

1. Combat log = real log (last 4–6 lines: skill, damage, Brace, Soften, Victory).
2. On foe HP ≤ 0: stop dice loop; Victory in log; **Continue replaces Flee** in the bottom slot (Flee locked otherwise). Continue → floor map + scrap as 0.58.
3. Player HP ≤ 0: Defeat → Run Summary unchanged.
4. Mid-skill foe death: stroke/log hold **max 600ms** (`Balance.COMBAT_END_FORCE_MS`), then force win.
5. Debug: `COMBAT_END win|lose foeHp=0 youHp=N` (`Log.d("COMBAT", …)`).

**Continue binding:** `CombatScreen` enables Continue when `state.finished || beat == AWAITING_CONTINUE` (set when foeHp≤0 / defeat). `onClick → gc.continueAfterCombat()` which requires `s.finished`. **Not** log click.

**Log-well plate bug composable:** `SharedTilePlateBox` (`ui/components/TilePlateBackdrop.kt`) — must not wrap the log well / bottom slot.

## PART B — Plate allow-list

- KEEP plate under combat skill tiles, Ashbrand `WeaponBar`, Forge rows (baked α ≈ 25–40%).
- REMOVE / absent: log well, Flee/Continue slot, HP, Brace pips, foe kit chips, path node discs.
- II/III gold pip top-right on combat skill tiles only at Lv2/Lv3; Lv1 = no pip.
- Shop climb-wallet prices say `purse` not `rem`.

## Packaging

versionCode **61** · versionName **0.1.60-unstick**. **HOLD tag** — no gh release.
