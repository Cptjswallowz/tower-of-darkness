# Hub more — v0.1.29-hubmore (WO LOCKED, Meta only)

Status: **implemented** (feature tip; no tag until QA PASS + CoS green). Owner: **Meta & Ops** + Engineer.  
Combat art **frozen**.

**Keep** v0.1.27 four offers + v0.1.28 baseline/stack/migration (`hub-v0127.md`, `hubkeep-v0128.md`).  
**ADD** three climb perks below. **Do not** wipe `remnants_bank`. **Do not** reset OWNED. **Do not** replace the four.

---

## Hub offer set (live = 7)

| # | Title | unlock_id | Cost | Effect (one-liner) | Once? |
|---|-------|-----------|------|--------------------|-------|
| 1 | Scout | `scout_charge` | 8 | +1 Free Scout / climb | yes |
| 2 | Extra rumor | `extra_rumor` | 6 | +1 rumor re-roll / floor (stacks on baseline 1) | yes |
| 3 | Host of Embers | `host_of_embers` | 12 | Unlock Cinder Vow | yes |
| 4 | Iron Lesson | `iron_lesson` | 12 | Unlock Grave Nail | yes |
| 5 | **Hostblood** | `hostblood` | **10** | +2 max HP each climb; start at new max | **Once** |
| 6 | **Warm Ash** | `warm_ash` | **8** | Start each climb with Brace **2** | **Once** |
| 7 | **Ash Tithe** | `ash_tithe` | **8** | +3 remnants at run summary (win **or** death) | **Once** |

Same CTA: **Buy** / **OWNED** / **Can't afford**. Title rem == Hub rem after every buy.

---

## MetaStore fields / ids

**No new DataStore keys.** Reuse existing `meta_v0` store:

| Key (existing) | Use |
|----------------|-----|
| `remnants_bank` (`KEY_REMNANTS`) | Unchanged; Buy spends immediately |
| `unlocked_cards` (`KEY_UNLOCKED` string set) | Persist all Hub `unlock_id`s including the three new ones |
| `meta_hp_bonus` (`KEY_META_HP`) | **Leave as-is** for legacy Iron Blood (`meta_hp_2`). Hostblood does **not** write this key — grant is derived from `hostblood` ∈ unlocks at climb start |

New unlock ids only:

```
hostblood | warm_ash | ash_tithe
```

Buy path (same as four): `spendRemnants(cost)` → `unlockCard(unlock_id)`.

---

## Hostblood — climb HP / max + Ash Press cap

| Rule | Value |
|------|--------|
| Base max | Whatever climb already uses: `Balance.PLAYER_MAX_HP` (**30**) + legacy `metaHpBonus` if any (**→ 32**). **Do not** retune trash/boss HP. |
| Hostblood owned | `climbMaxHp = baseMax + 2` |
| Climb start | `playerHp = climbMaxHp` (full to new max). **Does not** heal mid-run when bought mid-meta — only next climb start. |
| Once | OWNED after one buy — **not** a stack of ten (+20). |
| Ash Press | Heal cap follows **`climbMaxHp`** (not hard-coded 30). Same for Rest / shop heals / event heal clamps that use player max. |
| Mid-run | Persist `player_hp` / `player_max_hp` as today; Hostblood does not rewrite mid-climb schema. |

Formula:

```
baseMax     = PLAYER_MAX_HP + metaHpBonus          // 30 or 32
climbMaxHp  = baseMax + (2 if hostblood in unlocks else 0)
@ climb start / tutorial skip / completeTutorial:
  playerHp = climbMaxHp
  playerMaxHp = climbMaxHp   // wherever max is tracked for UI + heal clamps
```

Legacy `meta_hp_2` / `metaHpBonus` remain if present (hubkeep ADD). Hostblood stacks on that base once owned.

---

## Warm Ash — Brace on climb start

| Rule | Value |
|------|--------|
| When | Each **new climb** start (same sites as Scout/rumor grants: `startNewRun` / tutorial skip / completeTutorial). **Not** on FloorBreak→F2. **Not** mid-combat buy. |
| Effect | Player begins climb with **Brace 2** already applied before first combat resolve. |
| Hook | Set climb-scoped `pendingStartBrace = 2` (or apply into first `CombatState` player brace when combat opens). Clear after applied / on summary. |
| Mid-run resume | If brace already spent in climb, midrun slot carries combat brace as today — do **not** re-grant Warm Ash on Continuity resume. |

```
@ climb start:
  if warm_ash in unlocks → pendingStartBrace = 2
@ first combat enter (or CombatState init):
  player.brace = max(player.brace, pendingStartBrace); clear pending
```

---

## Ash Tithe — summary award hook

| Rule | Value |
|------|--------|
| When | `finishRun(won)` — **win or death** (both paths that land Run Summary) |
| Amount | **+3** remnants into bank (with the run wallet bank) |
| Hook | Inside `finishRun`, before or with `meta.addRemnants(...)`:  
  `bonus = if (ash_tithe in unlocks) 3 else 0`  
  `meta.addRemnants(runWallet + bonus)`  
  Surface +3 on summary UX if easy (optional). |
| Not | Not added to mid-run `run_wallet` during climb; summary-only. |

---

## Migration / bank / OWNED (v0.1.28 stays intact)

| Rule | Confirm |
|------|---------|
| Bank | **Never** wipe `remnants_bank` |
| OWNED | **Never** reset unlock set to install new rows |
| hubkeep migrate | Still run: `rumor_clarity`→`extra_rumor`; card ids→Host/Iron; Scout flag kept |
| New three | Absent until bought — **no** auto-OWNED from legacy Iron Blood |
| Additive | Old unlocks (`meta_hp_2`, `boss_bonus_2`, rares, …) remain in set |

---

## Frozen

Combat art, packs, Wake, path weights, 2x, trash/boss HP numbers, Cinder/Grave effects.

---

## Engineer checklist

- [x] Hub list = 7 offers; costs 8/6/12/12/10/8/8
- [x] Persist three ids in `unlocked_cards`; bank spend unchanged
- [x] Hostblood → climbMaxHp +2; start HP = max; Ash Press/heal caps use climbMaxHp
- [x] Warm Ash → Brace 2 at climb start only
- [x] Ash Tithe → +3 in `finishRun` win or death
- [x] hubkeep migration + bank/OWNED untouched

---

## Changelog

| Date | Change |
|------|--------|
| 2026-09-25 | v0.1.29-hubmore — Hostblood / Warm Ash / Ash Tithe Meta lock |
| 2026-09-25 | Engineer — Hostblood/Warm Ash/Ash Tithe wired; HubMoreV0129Test |
