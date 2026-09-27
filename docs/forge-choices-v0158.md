# Forge choice text — v0.1.58-forge PART D (WO Elliott)

Status: **design lock** — **choice text + glossary card bodies only**.  
Baseline tip `5810af1` (v0.1.57).

**Scope:** Tower of Darkness greenfield. Do **not** invent skill names beyond this WO / existing catalog (`cards-v0.md`, `emberpool-v0135.md`).  
**Frozen:** new enemies, Floor 4, SFX, skill math outside the listed upgrades.

Write the **chosen** upgrade line into the card body for tap-glossary (full sentence body below). Choice chips use the **Choice** column exactly.

---

## How to read levels

| Step | Picks |
|------|-------|
| **L2** | Choose **A** or **B** once |
| **L3** | Choose **A** or **B** once (stacks on the L2 pick) |

Bodies below are the **full** glossary string after that pick (L2 alone) or after both picks (L2+L3). Base text = current catalog effect.

---

## Named trees

### Tower Pike (base: Deal **8** damage.)

| Id | Choice (chip) | Glossary body after this pick alone / with prior |
|----|---------------|--------------------------------------------------|
| **L2A** | `+2 damage` | Deal **10** damage. |
| **L2B** | `Soften 1` | Deal **8** damage. **Soften 1**. |
| **L3A** (after L2A) | `+2 damage` | Deal **12** damage. |
| **L3A** (after L2B) | `+2 damage` | Deal **10** damage. **Soften 1**. |
| **L3B** (after L2A) | `After fire: Brace 1` | Deal **10** damage. After this fires, gain **Brace 1**. |
| **L3B** (after L2B) | `After fire: Brace 1` | Deal **8** damage. **Soften 1**. After this fires, gain **Brace 1**. |

Mechanical lock: L2A/L3A = **+2 dmg** each; L2B = **Soften 1**; L3B = after fire, **Brace 1**.

---

### Dust Veil (base: Deal **4** damage. Gain **Brace 2**.)

| Id | Choice (chip) | Glossary body |
|----|---------------|---------------|
| **L2A** | `Brace +1` | Deal **4** damage. Gain **Brace 3**. |
| **L2B** | `Also Soften 1` | Deal **4** damage. Gain **Brace 2**. **Soften 1**. |
| **L3A** (after L2A) | `Brace +1` | Deal **4** damage. Gain **Brace 4**. |
| **L3A** (after L2B) | `Brace +1` | Deal **4** damage. Gain **Brace 3**. **Soften 1**. |
| **L3B** (after L2A) | `First fire: weight +1` | Deal **4** damage. Gain **Brace 3**. The first time this fires this fight, this skill’s **weight +1** for the rest of the fight. |
| **L3B** (after L2B) | `First fire: weight +1` | Deal **4** damage. Gain **Brace 2**. **Soften 1**. The first time this fires this fight, this skill’s **weight +1** for the rest of the fight. |

Mechanical lock: L2A/L3A = Brace grant **+1** each; L2B = also **Soften 1**; L3B = first fire this fight → that skill’s **weight +1** this fight.

---

### Iron Mantle (base: Gain **Brace 3** (absorb before HP). If Brace remains at round end, clear it.)

| Id | Choice (chip) | Glossary body |
|----|---------------|---------------|
| **L2A** | `Brace +1` | Gain **Brace 4** (absorb before HP). If Brace remains at round end, clear it. |
| **L2B** | `On Brace: deal 2` | Gain **Brace 3** (absorb before HP). When this grants Brace, deal **2**. If Brace remains at round end, clear it. |
| **L3A** (after L2A) | `Brace +1` | Gain **Brace 5** (absorb before HP). If Brace remains at round end, clear it. |
| **L3A** (after L2B) | `Brace +1` | Gain **Brace 4** (absorb before HP). When this grants Brace, deal **2**. If Brace remains at round end, clear it. |
| **L3B** (after L2A) | `First Brace keeps weight` | Gain **Brace 4** (absorb before HP). The first Brace this fight from this skill does **not** consume this skill’s weight. If Brace remains at round end, clear it. |
| **L3B** (after L2B) | `First Brace keeps weight` | Gain **Brace 3** (absorb before HP). When this grants Brace, deal **2**. The first Brace this fight from this skill does **not** consume this skill’s weight. If Brace remains at round end, clear it. |

**L3B engine gate (WO):** Prefer “first Brace pip this fight does not consume a turn-weight.” If that weight hook is **not** real in code, **do not invent** it — use **deal 2** instead (same verb as L2B). Fallback glossary when weight hook absent:

| Id | Fallback glossary (no weight hook) |
|----|-------------------------------------|
| **L3B** (after L2A) | Gain **Brace 4** (absorb before HP). When this grants Brace, deal **2**. If Brace remains at round end, clear it. |
| **L3B** (after L2B) | Gain **Brace 3** (absorb before HP). When this grants Brace, deal **2**. If Brace remains at round end, clear it. |

(Choice chip may stay `First Brace keeps weight` in UI only if the hook ships; if fallback, chip = `On Brace: deal 2`.)

---

## Template — every other slotted skill that **deals damage**

Applies to catalog damage skills **except** Tower Pike / Dust Veil (named above). Includes e.g. Hostflint, Cinder Step, Emberbrand, Ruin Seal, Ash Press, Shadow Latch, Cinder Vow, Grave Nail, Ember Draw, Brand Mark, Spark Tithe, Wake Echo — **no new names**.

| Id | Choice (chip) | Effect |
|----|---------------|--------|
| **L2A** | `+2 damage` | Damage line **+2** |
| **L2B** | `Soften 1` | Add **Soften 1** |
| **L3A** | `+2 damage` | Damage line **+2** again |
| **L3B** | `After fire: Brace 1` | After this fires, gain **Brace 1** |

### Example — Hostflint (base: Deal **5** damage.)

| Path | Glossary body |
|------|---------------|
| L2A | Deal **7** damage. |
| L2B | Deal **5** damage. **Soften 1**. |
| L2A→L3A | Deal **9** damage. |
| L2B→L3A | Deal **7** damage. **Soften 1**. |
| L2A→L3B | Deal **7** damage. After this fires, gain **Brace 1**. |
| L2B→L3B | Deal **5** damage. **Soften 1**. After this fires, gain **Brace 1**. |

Same pattern on any other damage skill: bump the printed damage number by **+2** per A pick; keep all non-damage clauses; append Soften / after-fire Brace as chosen.

**Wake Echo:** +2 applies to the **5** base only (not a second +2 on the conditional **+4**). Soften / Brace lines append as usual.

---

## Template — every other slotted skill that **grants Brace** (no damage line)

Applies to **Vow Plate** (and any future Brace-only slotted skill already in catalog). **Iron Mantle** uses the named tree above.

| Id | Choice (chip) | Effect |
|----|---------------|--------|
| **L2A** | `Brace +1` | Brace grant **+1** |
| **L2B** | `Also deal 2` | When this grants Brace, also deal **2** |
| **L3A** | `Brace +1` | Brace grant **+1** again |
| **L3B** | `After fire: Soften 1` | After this fires, **Soften 1** |

### Vow Plate (base: Gain **Brace 5**.)

| Path | Glossary body |
|------|---------------|
| L2A | Gain **Brace 6**. |
| L2B | Gain **Brace 5**. When this grants Brace, deal **2**. |
| L2A→L3A | Gain **Brace 7**. |
| L2B→L3A | Gain **Brace 6**. When this grants Brace, deal **2**. |
| L2A→L3B | Gain **Brace 6**. After this fires, **Soften 1**. |
| L2B→L3B | Gain **Brace 5**. When this grants Brace, deal **2**. After this fires, **Soften 1**. |

---

## Classification (no new names)

| Bucket | Skills |
|--------|--------|
| Named | **Tower Pike**, **Dust Veil**, **Iron Mantle** |
| Damage template | Hostflint, Cinder Step, Emberbrand, Ruin Seal, Ash Press, Shadow Latch, Cinder Vow, Grave Nail, Ember Draw, Brand Mark, Spark Tithe, Wake Echo |
| Brace template | **Vow Plate** |
| Out of forge text this WO | **Relic Shard** (heal / conditional Brace — Elliott did not map; do not invent) |

---

## Explicit non-goals

- New enemies / Floor 4 / SFX  
- New skill names  
- New engine hooks beyond existing weight / Soften / Brace (Iron Mantle L3B falls back to deal **2** if needed)

---

## Engineer checklist

- [ ] Choice chips match **Choice** column strings exactly  
- [ ] Tap-glossary shows the matching **glossary body** for L2 and L2+L3  
- [ ] Named trees for Pike / Dust Veil / Mantle; templates for other slotted damage / Brace skills  
- [ ] Iron Mantle L3B: weight hook or deal-2 fallback — no new engine  
- [ ] No Relic Shard forge lines until locked  
