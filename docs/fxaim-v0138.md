# Combat FX aim — v0.1.38-fxaim (WO Elliott)

Status: **implemented** (aim / paint fix on v0.1.37 kernel).  
Design lock + Engineer checklist below.

**Frozen:** Wake **math**; path / Hub / Climb; no new skills / clips.  
**Keep from v0.1.37:** tier sizes (Small / Medium / Wake), role colors, 2x duration halving, **fired tile flash**, existing log + hold, 1x/2x button, tap-skip, FX-fail → resolve+log anyway.

**Scope:** Tower of Darkness greenfield only.

**Supersedes** v0.1.37 stroke geometry: You↔foe bust-to-bust bar, full-width gold/yellow plate, and whole-row tint flash are **forbidden**. Tier maps and colors in `fx-v0137.md` still apply.

---

## Kill (must not ship)

| Ban | Why | Fix |
|-----|-----|-----|
| Full-width gold / yellow **rectangle** over You **and** foe | Looks like a plate wash | Removed stage `CombatTileFlash` |
| Straight **bar** through both portraits | Bust-to-bust line | `CombatStrokeOverlay` short arc on recipient only |
| Flash that **tints the whole combat row** | Keep only **fired tile** flash | Flash on `SkillSlot` / `EnemyKitSlot` bounds |

---

## SLASH (damage stroke)

Stroke paints **on the recipient bust only** — short **arc / cut**, not a line from attacker to target.

| Who deals dmg | Where slash draws |
|---------------|-------------------|
| **Player** skill | **Foe** portrait |
| **Enemy** skill | **You** portrait |

**Color** still by **attacker** role (`fx-v0137.md` color table). Enemy direction flip = slash on You; player slash on foe.

**Wake:** keep **current** Wake slash art + heavier shake. Target = **foe** only. Must **not** paint a plate / wash over You (`WakeStageOverlay` clipped to right ~58%).

Tier size (thin / thicker) still from the skill map in `fx-v0137.md`.

Geometry helpers: `CombatFx.slashCutGeom` / `SlashCutGeom.spansBothBusts()` (unit-tested).

---

## BRACE (no slash)

No damage slash for these resolves (Brace grant only):

**Iron Mantle**, **Vow Plate**, **Dust Veil** (Brace branch), **Ember Draw** (Brace branch), **Hide**, **Rust Guard**, **Cinder Hide**.

Instead: spawn **tiny shield pips** that **float + fade** around the **owner** (You Brace → around You; foe Hide/Guard → around foe).

| Rule | Lock |
|------|------|
| Count | **Brace gained**, drawn **capped at 5** (gain 1 → 1 pip … gain 6+ → 5 pips) via `CombatFx.bracePipCount` |
| Under-bust number | Existing Brace number under bust **stays** |
| Damaging same skill | If the skill also deals damage (e.g. Dust Veil Deal 4 / Ember Draw Deal 4), that hit still uses its mapped **slash on recipient**; Brace grant is pips only (`braceGained` on follow-up event, `fxId = null`) |

---

## Soften

**Red pip** on **foe** (`softenPulse` on `StatusPipRow`). **No plate** / no wash / no extra slash beyond the hit’s recipient slash (Brand Mark hit = Small on foe bust). Follow-up Soften line: `softenApplied > 0`, `fxId = null`.

---

## Still from kernel (unchanged)

1. Fired **tile** flash (not whole-row tint)  
2. Stroke **or** Brace pips (per above)  
3. Number float (dmg / Brace)  
4. Shake by tier  
5. Log + same hold  

Maps: Small / Medium / Wake / NO STROKE skill lists → `fx-v0137.md`.

---

## Files

| File | Role |
|------|------|
| `domain/combat/CombatFx.kt` | Recipient, slash geom, bracePipCount, FxPlaySpec |
| `domain/combat/CombatEngine.kt` | `braceGained` / `softenApplied` on events |
| `ui/components/CombatFxOverlay.kt` | Short recipient slash + brace pips; tile flash unconstrained |
| `ui/components/WakeStageOverlay.kt` | Foe-aimed clip (no You cover) |
| `ui/screens/CombatScreen.kt` | Wire flash→slash|pips→float→shake; tile-scoped flash |
| `CombatFxAimV0138Test.kt` | Geometry / pip count / Soften / maps |

---

## Checklist (Engineer)

- [x] No full-width gold/yellow rect; no bust-to-bust bar; no whole-row tint
- [x] Damage slash = short cut on **recipient** bust only (player→foe, enemy→You)
- [x] Wake slash on foe; no plate over You
- [x] Brace skills: shield pips on owner (cap 5); under-bust Brace number stays; no slash
- [x] Soften = red foe pip; no plate
- [x] Tiers, colors, 2x, tile flash, log, 1x/2x, fail-safe kept
