# Ember pool skills — v0.1.35-emberpool (WO Elliott)

Status: **design lock**. Four new **player** skills.  
**No** retune of existing cards (Iron Mantle, Hostflint, Cinder Vow, Grave Nail, etc.).

**Hub-gated** — Meta owns unlock offers / OWNED. Architect owns combat rules + glossary.

Reuse existing words only: **Brace** / **Soften** / **Spark** / **Wake**. No new status vocabulary.

**Spark** = Ashbrand **CHAIN charge pip** (same counter as Cinder Vow’s “≥1 pip”). Not a new resource. Quiet half-Wake **SPARK** roll rules unchanged (`wake-v014.md`).

**Frozen:** existing cards, Wake **math/thresholds**, enemy kits, path, 2x, art.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

---

## Hard order (every ember skill)

1. **Read Sparks** (charge) **BEFORE** the skill  
2. **Apply damage** (and any bonus damage that is part of the skill)  
3. **Add 1 Spark** if this skill **dealt damage**  
4. **Then** Ember Draw Brace / Brand Mark Soften / Spark Tithe spend  

**Wake threshold check** stays on the **next weapon beat**, **not** mid-skill (do not force FULL Wake inside the skill resolution).

---

## Skills (all Uncommon **w3**)

| Name | Effect |
|------|--------|
| **Ember Draw** | Deal **4**. If Sparks were **0** before this skill → gain **Brace 2**. Damage still adds **1 Spark** after. |
| **Brand Mark** | Deal **3**. If Sparks were **≥1** before this skill → **Soften 2**. Damage still adds **1 Spark** after. |
| **Spark Tithe** | Deal **6**. If Sparks were **≥1** before → spend **1 Spark AFTER** the damage pip is added (**net 0**). If Sparks were **0** before → only the new pip remains (**net +1**). Log **`Spark spent`** when it spends. |
| **Wake Echo** | Deal **5**. If **Wake already fired this fight** → deal **4 more** (**one** log line, not a second skill). Bonus does **not** add a second Spark. Damage still adds **1 Spark** (from the skill’s damaging resolve, once). |

Pool-gated until Meta Hub unlock. Loadout still exactly **5**; no duplicates.

### Spark Tithe net (Engineer)

```
before = charge
apply Deal 6
if dealt damage: charge += 1          // step 3
if before >= 1:
  charge -= 1                         // step 4 spend
  log "Spark spent"
// before 0 → charge ends at 1; before ≥1 → charge ends at before (net 0)
```

### Wake Echo log

One line covering total damage (5, or 5+4 when echo). Not two skill resolutions. Bonus hit does not grant an extra Spark beyond the single +1 from “dealt damage.”

---

## Glossary body text (tap name)

| Name | Glossary |
|------|----------|
| **Ember Draw** | Deal 4. If you had no Sparks before this skill, gain Brace 2. Still adds 1 Spark after the hit. |
| **Brand Mark** | Deal 3. If you already had Sparks before this skill, Soften 2. Still adds 1 Spark after the hit. |
| **Spark Tithe** | Deal 6. If you had Sparks before, spend 1 Spark after the new pip is added (net zero). If you had none, keep the new pip. Logs “Spark spent” when it spends. |
| **Wake Echo** | Deal 5. If Wake already fired this fight, deal 4 more in the same line. The bonus does not add another Spark. |

Tap skill name (tile or highlighted log word) → glossary. Brace / Soften / Spark / Wake entries reuse existing defs.

---


---

## Hub ADD (Meta) — exact ids

| Offer id | Title | Cost | Unlocks card id |
|----------|-------|------|-----------------|
| `cold_draw` | Cold Draw | 10 | `ember_draw` |
| `brand_lesson` | Brand Lesson | 10 | `brand_mark` |
| `spark_lesson` | Spark Lesson | 12 | `spark_tithe` |
| `echo_lesson` | Echo Lesson | 12 | `wake_echo` |

Persist OWNED via MetaStore `unlocked_cards` (offer id). Buy twice → spends 0 (already OWNED). Not in `CardCatalog.poolForRun` until OWNED (`HubOffers.skillUnlocked`). First 7 Hub offers unchanged at head; total **11**.

## Checklist

- [x] Order: read Sparks → damage → +1 Spark if damaged → then Brace/Soften/Tithe spend; Wake check next beat
- [x] Four skills Uncommon w3; Hub-gated (Meta); glossary bodies as table
- [x] Spark Tithe net 0 / net +1 + “Spark spent” log; Wake Echo one log line, bonus no second Spark
- [x] No new status words; no retune of existing cards / Wake math / enemy kits / path / 2x / art
