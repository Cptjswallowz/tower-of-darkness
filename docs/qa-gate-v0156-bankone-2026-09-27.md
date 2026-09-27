# QA gate — v0.1.56-bankone (2026-09-27)

**Gate result: PASS** (feature cases 1–9 green; packaging cleared at ship — root APK refreshed to app-debug)

| Field | Measured |
|-------|----------|
| Tip SHA | `9e96464fe22288764d20035f9fcf6f3b82217b74` |
| Tip == HEAD == origin/main | **YES** |
| versionCode / versionName | **57** / **0.1.56-bankone** |
| app-debug md5 | `1d5dea5f8e18585ac4b3b38be2a246f6` — **MATCHES claim** |
| Root APK md5 (at gate) | `cb7a50ee8b23c0e09aff8c7175f2c0cd` — **STALE (hubsplit)** |
| Root APK md5 (at ship) | `1d5dea5f8e18585ac4b3b38be2a246f6` — **MATCH** (copied from app-debug before tag) |
| root vs app-debug (at ship) | **CMP_OK** |
| Suite | **52** classes / **406** tests / **0** fail / **0** err / **0** skip |
| BankoneV0156Test | **5/5 PASS** |
| FAIL blockers | **none** (root refreshed at ship) |

Measured ET **2026-09-27 16:46 EDT**. No tag / no gh release / no messaging from this gate. Root APK left as-is (not overwritten).


**Ship note (Android Engineer):** Before `v0.1.56-bankone` tag/release, root `tower-of-darkness-debug.apk` was overwritten from `app/build/outputs/apk/debug/app-debug.apk`. Confirmed md5 `1d5dea5f8e18585ac4b3b38be2a246f6`. Release asset re-download md5 MATCH. Tag `v0.1.56-bankone` → `9e96464fe22288764d20035f9fcf6f3b82217b74`. No Drive mirror.

---

## A — Git

| Check | Result |
|-------|--------|
| `HEAD` | `9e96464fe22288764d20035f9fcf6f3b82217b74` |
| `origin/main` | `9e96464fe22288764d20035f9fcf6f3b82217b74` |
| Claimed tip | `9e96464fe22288764d20035f9fcf6f3b82217b74` |
| HEAD == origin/main == tip | **YES** |
| Subject | `feat: v0.1.56-bankone one remnants_bank wallet` |
| Author | Android Engineer \<android-engineer@tod.local\> |
| AuthorDate | 2026-09-27 16:43:20 -0400 (ET) |
| Parent | `dad4e55` (`release: v0.1.55-hubsplit README + QA gate`) |
| `--stat` tip | 13 files, +400/−25 — `MetaStore.kt`, `SummaryBankCommit.kt`, `GameController.kt`, Path/FloorBreak/Shop HUD, `BankoneV0156Test.kt`, Hubsplit test tweak, docs |
| Working tree | tracked clean vs HEAD; untracked: `docs/qa-gate-v0150-puffhold-2026-09-26.md` (pre-existing) + this gate doc |

---

## B — APK packaging

| Artifact | Size | md5 | mtime (ET) |
|----------|------|-----|------------|
| `app/build/outputs/apk/debug/app-debug.apk` | **42425134** | `1d5dea5f8e18585ac4b3b38be2a246f6` | **2026-09-27 16:41:04 EDT** |
| `tower-of-darkness-debug.apk` (root) | **42425134** | `cb7a50ee8b23c0e09aff8c7175f2c0cd` | **2026-09-27 16:13:19 EDT** |

| Check | Measured |
|-------|----------|
| Claimed APK md5 | `1d5dea5f8e18585ac4b3b38be2a246f6` |
| app-debug vs claim | **YES MATCH** |
| `output-metadata.json` | versionCode **57**, versionName **0.1.56-bankone**, outputFile `app-debug.apk` |
| `app/build.gradle.kts` | versionCode = 57, versionName = `"0.1.56-bankone"` |
| `cmp` root vs app-debug | **DIFF** — first differing byte **11648338** (exit 1) |
| Root deliverable | **STALE** — still hubsplit bytes (matches prior gate root md5 `cb7a50ee…`); mtime 16:13 vs app-debug 16:41 |

**Packaging finding:** root `tower-of-darkness-debug.apk` was **not** refreshed after the bankone assemble. Same byte length as app-debug but different content. **Do not tag** until root is copied from app-debug (measurement-only note; this gate did **not** overwrite root).

**app-debug match vs CoS kick: YES.** Root stale: **YES.**

---

## C — Hard locks

| Lock | Measured |
|------|----------|
| FX/SFX tip diff vs `dad4e55` / `f480b88` | **0** — no path changes for CombatFx / PlumeGarnishKit / IronclashSfx / SoundBus |
| ClimbKept.kt tip diff | **0** vs `dad4e55` |
| HubOffers.kt tip diff | **0** vs `dad4e55` |
| Payout table (ClimbKept.finishPayout) | F1+1 / F2+2 / F3+3 / Cave Troll+1 / Gate-Warden **+5** / Ash Tithe+3 (`ASH_TITHE_BONUS = 3`) — **UNCHANGED** |
| Hub sections | MetaHub Relics → Perks → Skills (HubRelics headers) — tip does not touch HubRelics/HubOffers |
| F3 rumors / loadout | no tip changes to rumor/loadout lock sources; TrollkeptV0154Test **7/7** green |

---

## D — Unit tests

Command: `./gradlew :app:testDebugUnitTest --rerun-tasks --offline`

| Metric | Measured |
|--------|----------|
| BUILD | **SUCCESSFUL** |
| Gradle wall | **9s** |
| XML dir | `app/build/test-results/testDebugUnitTest/` |
| Classes | **52** |
| Tests | **406** |
| Failures | **0** |
| Errors | **0** |
| Skipped | **0** |
| XML time sum | **0.915s** |
| Measured clock | **2026-09-27 16:46 EDT** |

### Named suites

| Class | Result |
|-------|--------|
| BankoneV0156Test | **5/5** PASS — `titleAndHub_sameRemnantsBankGetter`, `climbStart_doesNotZeroRemnantsBank`, `summaryMenuAndContinue_oneTxnCommitAndLog`, `bankWriteLogLine_formatAndSources`, `floorHud_noRemOrRemnants_usesPurse` |
| HubsplitV0155Test | **6/6** PASS |
| TrollkeptV0154Test | **7/7** PASS |
| WakebladeV0153Test | **12/12** PASS |
| PuffholdV0150Test | **7/7** PASS |
| StrokeBothV0148Test | **7/7** PASS |

---

## E — Cases 1–9

### 1. Title Hub·N and Hub Remnants N — same remnants_bank getter — **PASS**

- `HubOffers.titleBankLine` / `hubBankLine` format from same `bank: Int` arg.
- MainMenu: `HubOffers.titleBankLine(gc.remnantsBank)` (MainMenuScreen.kt:69).
- MetaHub: `HubOffers.hubBankLine(gc.remnantsBank)` (MetaHubScreen.kt:65).
- `gc.remnantsBank` collected from `meta.remnantsBank` Flow (`KEY_REMNANTS` = `remnants_bank`).
- BankoneV0156Test.`titleAndHub_sameRemnantsBankGetter` PASS.

### 2. Floor Path/FloorBreak/Shop HUD — no rem/remnants for purse — **PASS**

Measured HUD strings:

- Path: `"HP ${gc.playerHp} · purse ${gc.runWallet}"`
- FloorBreak: `"HP ${gc.playerHp} · purse ${gc.runWallet} · Ashbrand Lv…"`
- Shop: `"HP ${gc.playerHp} / $maxHp · purse ${gc.runWallet}"`

BankoneV0156Test.`floorHud_noRemOrRemnants_usesPurse` PASS.  
Note (non-blocker): Shop **offer CTA** still uses `"${offer.title} · ${offer.price} rem"` as purse-price suffix — not the HUD bank line; HUD itself is purse-labeled.

### 3. Summary Kept = this-run payout only — **PASS**

- `finishRun`: `payout = ClimbKept.finishPayout(...)`; `summary.kept = payout.kept` / `keptLines = payout.lines`.
- Sheet Kept is ephemeral payout; MetaStore bank write deferred to leave.
- Headline helper: `ClimbKept.headline(kept)` = `"Kept: $kept remnants"`.

### 4. Continue: bank+=Kept + trophies one txn; BANK log; then nav — **PASS**

- `goHub()` → `leaveRunSummary(NavState.MetaHub)` → source=`"continue"`.
- `meta.commitSummaryBank(s.kept, s.newTrophyIds)` one `edit{}`; then `Log.i("BANK", bankWriteLogLine(...))`; then clear mid-run + `nav = dest`.
- Log format: `BANK write prev=X add=Y now=Z source=continue`.
- Local `remnantsBank = write.now` (== prev + Kept).

### 5. Menu: same commit path source=menu — **PASS**

- `goMenu()` from Summary → `leaveRunSummary(NavState.MainMenu)` → source=`"menu"`.
- Same `commitSummaryBank` + `bankWriteLogLine` path; Menu does **not** bare-nav when summary present.
- BankoneV0156Test.`summaryMenuAndContinue_oneTxnCommitAndLog` PASS.

### 6. New Climb does NOT zero remnants_bank — **PASS**

- `startNewRun` / `skipTutorial`: `runWallet = 0` with comment `remnants_bank MUST NOT be zeroed`; no `remnantsBank = 0`.
- `confirmNewClimb` clears mid-run slot only (“Meta untouched”).
- BankoneV0156Test.`climbStart_doesNotZeroRemnantsBank` PASS.

### 7. No partial write — bank and trophies same edit{} — **PASS**

- `MetaStore.commitSummaryBank`: single `context.dataStore.edit` writes `KEY_REMNANTS` and `KEY_UNLOCKED` together.
- Returns `SummaryBankWrite(prev, add, now)` for log.

### 8. Engineer unit set green — **PASS**

- BankoneV0156Test **5/5**; HubsplitV0155Test **6/6**; TrollkeptV0154Test **7/7**; Wakeblade **12/12**; Puffhold **7/7**; StrokeBoth **7/7**.
- Full suite XML: **52** / **406** / **0** / **0** / **0**.

### 9. FX/SFX tip diff still 0 — **PASS**

- `git diff --name-only dad4e55..HEAD` and `f480b88..HEAD`: **0** CombatFx / PlumeGarnishKit / IronclashSfx / SoundBus paths.

---

## F — Fail blockers

1. **PACKAGING — root APK stale:** `/workspace/tower-of-darkness/tower-of-darkness-debug.apk` md5 `cb7a50ee8b23c0e09aff8c7175f2c0cd` (hubsplit, mtime 16:13 ET) ≠ app-debug `1d5dea5f8e18585ac4b3b38be2a246f6` (bankone, mtime 16:41 ET). **CMP_DIFF.** Refresh root from app-debug before tag. (This gate left root untouched.)

No feature / lock / suite blockers.

---

## Gate result

**PASS** on tip + cases 1–9 + unit suite, with explicit **FAIL blocker: root APK stale (refresh from app-debug before tag)**.

CoS: hold tag until root deliverable matches app-debug md5 `1d5dea5f8e18585ac4b3b38be2a246f6`.
