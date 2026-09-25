# Enemy kits — v0.1.32-enemykit (WO Elliott)

Status: **design lock**. Enemies use the **same** dice / weight / grey-out machine as the player bar.  
Looks (pack variants) are **cosmetic only** — kit is per **role**, not per look.

**Floor art PASS.** Freeze: Hub / path math / Wake math / 2x / portraits / map tokens.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

---

## Beat order

1. Player skill resolves (existing bar).
2. Then roll **enemy live kit** with the same weight-pick among **unspent** tiles; grey spent; exhausted cannot roll; cycle reset when all spent (same machine as player).
3. Combat log shape: **`{EnemyTitle} — {SkillName} {N}`**  
   Example: `Sturdy Orc — Cleave 8`.

**Basic Hit** = the high-weight card in each kit (w5).

No bleed / stun / summon / extra verbs beyond the kits below.

---

## Soften (enemy-kit era)

| Rule | Lock |
|------|------|
| Target | Soften applies to the **next ENEMY skill that deals damage** |
| Exempt | **Hide**, **Rust Guard**, **Cinder Hide** (Brace skills — Soften does **not** consume on these) |
| After | Pip **clears** when it reduces a damaging enemy skill |
| Nip | Log **must** say it **ignores Soften**; Nip does not consume Soften |
| Brace on You | Still **absorbs before HP** (unchanged) |

This supersedes the older Soften = “counter reduce only” framing in `status-pips-v0113.md` **when enemy kits are live** — Soften now reduces the next damaging enemy **kit skill**, not a monolithic counter roll.

---

## Kits (same for every look in the role)

### Weak Goblin

| Skill | w | Effect |
|-------|---|--------|
| **Shiv** | 2 | Deal **5** |
| **Nip** | 2 | Deal **3** — log **MUST** say ignores Soften |
| **Hit** | 5 | Deal **7–9** (current trash hit band) |

### Sturdy Orc

| Skill | w | Effect |
|-------|---|--------|
| **Cleave** | 2 | Deal **8** |
| **Hide** | 2 | **Brace 3** on the orc |
| **Hit** | 5 | Deal **8–10** (current band) |

### Seal-Warden (F1 boss)

| Skill | w | Effect |
|-------|---|--------|
| **Seal Pulse** | 2 | Deal **7** |
| **Rust Guard** | 2 | **Brace 4** on the Warden |
| **Hit** | 5 | Deal **6–9** (current boss band) |

### Ash-Warden (F2 boss)

| Skill | w | Effect |
|-------|---|--------|
| **Coal Slam** | 2 | Deal **9** |
| **Cinder Hide** | 2 | **Brace 3** on the Warden |
| **Hit** | 5 | Deal **6–9** (current boss band) |

---

## UI

| Rule | Lock |
|------|------|
| Layout | **Two special tiles + Hit** under enemy HP / pips |
| Chrome | Smaller, **same** card chrome as player skills — **not** a fifth player-sized bar |
| Spent | **Grey** when spent (same grey-out) |
| Glossary | Tap tile **or** highlighted log skill word → glossary |

### New glossary entries

| Term | Copy lock |
|------|-----------|
| Shiv | (short strike gloss — Engineer/Art OK to polish; keep vague-stats-safe) |
| Nip | **Small cut. Soften does not reduce this hit.** |
| Cleave | (heavy swing) |
| Hide | Reuse **Brace** definition (Brace on the enemy) |
| Seal Pulse | (seal strike) |
| Rust Guard | Reuse **Brace** definition |
| Coal Slam | (coal strike) |
| Cinder Hide | Reuse **Brace** definition |

---

## Frozen

Hub (`hub-v0127` / hubkeep / hubmore), path math, Wake math, 2x, portraits, map tokens. Pack **titles/looks/weights** stay `packs-v0126.md` (cosmetic looks only).

---

## Checklist

- [x] Enemy kits use player dice/weight/grey machine; looks cosmetic only
- [x] Log `{Title} — {Skill} N`; Hit = w5; kits match table
- [x] Soften → next damaging enemy skill (not Hide/Rust Guard/Cinder Hide); Nip ignores Soften in log; Brace on You before HP
- [x] UI: 2 specials + Hit under enemy HP; smaller chrome; grey spent; tap → glossary (Nip copy locked)
- [x] No bleed/stun/summon; freeze Hub/path/Wake math/2x/portraits/map tokens
