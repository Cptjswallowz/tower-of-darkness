# Floor 3 — v0.1.41-floor3 (WO Elliott)

Status: **design lock**. Base tip `9478a3f` / release `e71a8d1` (v0.1.40-fxfix).  
**FX frozen** at **0.1.40** (`fxaim-v0138.md` + fxfix/read lineage). Do not retune FX here.

**Scope:** Tower of Darkness greenfield only.  
**Do not invent** beyond Elliott. Closed opens (Gate Pulse / Hit / Hub return / troll filenames) locked below.

**Supersedes:** `floor2-v019.md` “No Floor 3” / F2 clear ends climb. F1/F2 **graph depth** and F1/F2 **hallway pack tables** stay as locked elsewhere unless named below.

---

## Explicit non-goals (frozen)

- New player skills  
- New Hub perks  
- Multi-enemy fights  
- F1 / F2 **graph** changes (depth unchanged)  
- FX / Wake math / Climb intro  

---

## Floor 3 graph

Longer than F1/F2. **F1/F2 depth unchanged.**

| Rule | Lock |
|------|------|
| Node-rows after Start | **5** |
| Mid-tier paths | **3** (not 2) |
| Start | **One** Start |
| Rest | **Immediately before** Boss |
| Combat before Rest | **≥2 COMBAT** on every Start→Boss route before that Rest |
| Events | **≤1** on the floor |
| Economy node | **≥1 Treasure OR Shop** on the floor |
| Art | Reuse **F1/F2 path tokens**; F3 backdrop = `floor_backdrop_cave` (Art); F1/F2 keep `floor_backdrop` — **no** redraw of tokens |

Rest heal = **same as F1/F2** Rest (no new formula).

---

## Hallway spawns

### F3 only

| Pack | Weight |
|------|--------|
| Weak Goblin | **20%** |
| Sturdy Orc | **40%** |
| Cave Troll | **40%** |

Cave Troll looks **A / B / C**: equal **1/3** each (same look-roll / persist / re-roll cadence as packs — `packs-v0126.md`).

Drawable basenames (Art): `cave_troll_a`, `cave_troll_b`, `cave_troll_c`.

**F1 / F2 hallway tables unchanged** (F1 75/25, F2 30/70 — `packs-v0126.md`). Bosses never roll hallway packs.

### Cave Troll kit

| | Lock |
|--|------|
| HP | **28** |
| Role | Hallway trash (not boss) |

| Skill | w | Effect | FX tier (existing kernel) |
|-------|---|--------|---------------------------|
| **Club** | 2 | Deal **8** | **MEDIUM** |
| **Hide** | 2 | **Brace 4** on the troll | **NO STROKE** (shield pips — `fxaim-v0138.md`) |
| **Hit** | 5 | Deal **7–9** | **SMALL** |

Glossary: **Club** = heavy troll swing.

Same dice / weight / grey machine as other enemy kits (`enemykit-v0132.md`). Soften: next damaging enemy skill; Hide does not consume Soften.

---

## Boss — Gate-Warden

| | Lock |
|--|------|
| HP | **36** |
| Art | Reuse **Seal-Warden** bust + **colder copper** tint if no new bust |

| Skill | w | Effect |
|-------|---|--------|
| **Gate Pulse** | 2 | Deal **7** — mirror **Seal Pulse** |
| **Rust Guard** | 2 | **Brace 4** on the Warden (existing Rust Guard) |
| **Hit** | 5 | Deal **6–9** |

Rest before boss = F1/F2 heal (above).

### Post-F3 clear

Beat **Gate-Warden** → **Hub**. **No Floor 4** yet.

---

## Loadout lock (F3+)

| Floor | Rule |
|-------|------|
| **F1 / F2** | Keep **pick-5 per combat** (Elliott lock this WO) |
| **F3+** | See flow below |

### F3+ flow (each floor)

1. Enter floor → **map + rumors first** (Scout still works).  
2. **First time** on that floor → open **Loadout**: pick **5 + weapon**.  
3. **Confirm** → loadout **locks for all combats on that floor**.  
4. **No** per-fight re-pick while on the floor.  
5. **Next floor** → unlock → pick → Confirm → lock again for that floor.

| Extra | Lock |
|-------|------|
| First F3 ever | **Explainer sheet once per install** (persist flag) |
| In combat | Uses the **locked five**; grey-out / dice **unchanged** |

---

## Open (do not invent)

- Floor 4+ **graph** (only F3+ **loadout** rule + Hub return after Gate-Warden are stated)
- Exact Gate-Warden win **summary copy** (destination = Hub; wording not locked)

---

## Engineer checklist

- [x] F3 graph: 5 rows after Start; 3 mid paths; Rest before Boss; ≥2 COMBAT before Rest; Events ≤1; ≥1 Treasure or Shop; F1/F2 depth untouched  
- [x] F3 `floor_backdrop_cave`; F1/F2 `floor_backdrop` untouched; reuse path tokens (no redraw)  
- [x] F3 hallway 20/40/40 Goblin/Orc/Troll; troll look 1/3; F1/F2 weights unchanged  
- [x] Cave Troll HP 28; Club 8 w2 MEDIUM; Hide Brace 4 w2 no slash; Hit 7–9 w5 SMALL; glossary Club; looks `cave_troll_a/b/c`  
- [x] Gate-Warden HP 36; Gate Pulse deal 7 w2; Rust Guard Brace 4 w2; Hit 6–9 w5; Seal art + colder copper if no new bust  
- [ ] Beat Gate-Warden → Hub (no Floor 4)  
- [x] F3+ loadout: map+rumors → pick 5+weapon once/floor → Confirm locks all combats; F1/F2 pick-5 per combat; first-F3 explainer once/install  
- [x] No new player skills / Hub perks / multi-enemy / F1–F2 graph / FX / Wake / Climb changes  
