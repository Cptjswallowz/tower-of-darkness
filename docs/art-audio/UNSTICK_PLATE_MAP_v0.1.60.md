# WO v0.1.60-unstick PART B — plate usage map (Art)

Art drawable unchanged from v0.1.59-tilepolish.

| Field | Value |
| --- | --- |
| drawable | `R.drawable.ui_tile_plate` → `app/src/main/res/drawable/ui_tile_plate.png` |
| mirror | `assets/ui/ui_tile_plate.png` |
| md5 | `20dc01b0e3b934c2bb08c8b26cf77524` |
| center α | ~0.35 baked (band 25–40%) |
| tip at audit | `b98d202` |

## Painter

**Composable that draws the stone plate:** `SharedTilePlateBox` in `ui/components/TilePlateBackdrop.kt`  
loads `painterResource(R.drawable.ui_tile_plate)`. Constants: `domain/art/SharedTilePlate.kt`.

## KEEP (WO allow-list)

| Site | File | Function |
| --- | --- | --- |
| 5 combat skill tiles | `CombatScreen.kt` | `SkillSlot` → `SharedTilePlateBox` under glyph/name/`wN`/II·III pip |
| Ashbrand weapon plate | `CombatScreen.kt` | `WeaponBar` → `SharedTilePlateBox` |
| Forge rows | `ForgeSheet.kt` | each list row → `SharedTilePlateBox` |

Opacity: baked PNG center α; Compose `SharedTilePlate.OPACITY = 1f` (no extra crush). Panel over plate uses `PANEL_OVER_PLATE_ALPHA = 0.55f`.

## REMOVE / already absent

| Target | Status at tip |
| --- | --- |
| Combat log well | **No** `SharedTilePlateBox`. Log is `Column` + `GlossaryText` only (~L672–738). Not painting `ui_tile_plate`. |
| Flee / Continue slot | **No** plate. `OutlinedButton` / `Button` only (~L740–757). |
| HP numbers / `HpBar` | **No** plate. |
| Brace pips | **No** plate (`CombatBracePipsOverlay` / `StatusPipRow`). |
| Foe kit chips | **No** plate (`EnemyKitSlot` = Panel + border only). |

## Out of allow-list (flag for Engineer / cloud)

| Site | File | Note |
| --- | --- | --- |
| Path node discs | `PathScreen.kt` | **REMOVED** in v0.1.60-unstick (allow-list = skills / Ashbrand / Forge only). Plain Panel disc restored. |

## Read test (Art previews, arm's length)

Prior stage `/workspace/tod-tilepolish-v0159/`:

- `preview_skill_slot.png` — name + w2 + II readable over plate
- `preview_forge_row.png` — title + scrap numbers readable
- HP / purse / g·o / rumor / Flee·Continue: not on plate (plain chrome) — readable by absence of stone crush

## Art locks held

No CLEAVE/PLUME/Kenney FX, remnants, Forge costs, relic effects, SFX, or music. No new PNG. Art did not edit Compose (cloud/Engineer owns strip of Path nodes if required).
