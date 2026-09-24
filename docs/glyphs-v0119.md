# Skill glyphs — v0.1.19-glyphs (WO Elliott)

Status: **implemented** (placeholder PNG drawables (Art gen); Art drop-in same names). UI art on skill tiles only.  
**No** combat math, Wake art, bodies, path, 2x, save, or pip logic changes.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

---

## Intent (Buriedbornes-style)

Every **non-Ashbrand** skill tile shows a small **picture on the tile**; the **name stays** readable. Glyph sits **24–32 dp above the name** on **combat bar** and **loadout** rows. Name + weight (`w#`) stay under / beside.

---

## Color = job

| Job | Tint | Cards |
|-----|------|-------|
| **Damage** | Default damage read | Hostflint, Emberbrand, Tower Pike, Ruin Seal, Shadow Latch, Cinder Step |
| **Brace / guard** | **Green** | Iron Mantle, Vow Plate, Dust Veil |
| **Mixed** | Mixed read (dmg+heal / heal) | Ash Press, Relic Shard |

Green-outline skills **keep green**. Exhaust / grey-out **dims the glyph** with the name.

---

## Glyph map (Elliott lock — do not invent alternates)

### Damage

| Skill | Glyph |
|-------|-------|
| Hostflint | flint / spark |
| Emberbrand | flame |
| Tower Pike | spear |
| Ruin Seal | cracked seal |
| Shadow Latch | hook |
| Cinder Step | boot + spark |

### Brace / guard (green)

| Skill | Glyph |
|-------|-------|
| Iron Mantle | kite shield |
| Vow Plate | heavy plate |
| Dust Veil | cloak |

### Mixed

| Skill | Glyph |
|-------|-------|
| Ash Press | hammer + plus (dmg + heal) |
| Relic Shard | shard + plus (heal) |

### Ashbrand

Existing **blade icon only**. **No second glyph** on the weapon slot.

---

## UI rules

- Tap / glossary unchanged.
- No new animations on common skills.
- Wake crescent / overlays stay **above** the bar.
- You / Ash-Warden / Seal-Warden / trash bodies untouched.

---

## Frozen

Wake art, Spark, bodies, 2x, save, pips logic, path, combat math.

---

## Checklist

- [x] All 11 mapped skills show the locked glyph (24–32 dp) on loadout + combat
- [x] Ashbrand: blade only — no second glyph
- [x] Brace jobs green; exhaust dims glyph + name
- [x] No invent icons; frozen systems untouched

## Engineer wire (v0.1.19)

- Domain: `SkillGlyph` / `SkillJob` (`domain/glyphs/SkillGlyph.kt`)
- UI: `SkillGlyphIcon` on combat `SkillSlot` (glyph above name) + loadout `CardRow` (glyph beside name)
- Drawables: `glyph_*.png` under `app/src/main/res/drawable/` — Art replaces same names
- Slot height: 72 dp (was 56) so name + w# stay readable
