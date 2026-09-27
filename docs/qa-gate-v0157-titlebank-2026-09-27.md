# QA gate — v0.1.57-titlebank (2026-09-27)

**Gate result: PASS** (cases 1–9 green; packaging CMP_OK; suite green)

| Field | Measured |
|-------|----------|
| Tip SHA | `5810af12830bfd14eb445b85454a9eb8907f6857` |
| Tip == HEAD == origin/main | **YES** |
| versionCode / versionName | **58** / **0.1.57-titlebank** |
| app-debug md5 | `c1254dcbe4fc63ab873fcb3a058c6c9a` — **MATCHES claim** |
| Root APK md5 | `c1254dcbe4fc63ab873fcb3a058c6c9a` — **MATCH** |
| root vs app-debug | **CMP_OK** |
| Suite | **53** classes / **412** tests / **0** fail / **0** err / **0** skip |
| TitlebankV0157Test | **6/6 PASS** |
| BankoneV0156Test | **5/5 PASS** |
| FAIL blockers | **none** |

Measured ET **2026-09-27 18:01 EDT**. No tag / no gh release / no messaging from this gate. Root APK left as-is (not overwritten).

---

## A — Git

| Check | Result |
|-------|--------|
| `HEAD` | `5810af12830bfd14eb445b85454a9eb8907f6857` |
| `origin/main` | `5810af12830bfd14eb445b85454a9eb8907f6857` |
| Claimed tip | `5810af12830bfd14eb445b85454a9eb8907f6857` |
| HEAD == origin/main == tip | **YES** |
| Subject | `feat: v0.1.57-titlebank MetaStore Title + victory seal Kept` |
| Author | Android Engineer \<android-engineer@tod.local\> |
| AuthorDate | 2026-09-27 17:57:48 -0400 (ET) |
| Parent | `f66131ef3534da56ce3ddc4e0164db6e83191d3e` (`release: v0.1.56-bankone README + QA gate`) |
| Bankone feature tip (FX baseline) | `9e96464fe22288764d20035f9fcf6f3b82217b74` |
| `--stat` tip | 11 files, +318/−13 — `ClimbKept.kt`, `GameController.kt`, `MainMenuScreen.kt`, `RunSummaryScreen.kt`, `TitlebankV0157Test.kt`, Trollkept test tweak, docs |
| Working tree | tracked clean vs HEAD; untracked: `docs/qa-gate-v0150-puffhold-2026-09-26.md` (pre-existing) + this gate doc |

---

## B — APK packaging

| Artifact | Size | md5 | mtime (ET) |
|----------|------|-----|------------|
| `app/build/outputs/apk/debug/app-debug.apk` | **42425134** | `c1254dcbe4fc63ab873fcb3a058c6c9a` | **2026-09-27 17:57:33 EDT** |
| `tower-of-darkness-debug.apk` (root) | **42425134** | `c1254dcbe4fc63ab873fcb3a058c6c9a` | **2026-09-27 17:57:37 EDT** |

| Check | Measured |
|-------|----------|
| Claimed APK md5 | `c1254dcbe4fc63ab873fcb3a058c6c9a` |
| app-debug vs claim | **YES MATCH** |
| root vs claim | **YES MATCH** |
| `output-metadata.json` | versionCode **58**, versionName **0.1.57-titlebank**, outputFile `app-debug.apk` |
| `app/build.gradle.kts` | versionCode = 58, versionName = `"0.1.57-titlebank"` |
| `cmp` root vs app-debug | **CMP_OK** (identical bytes) |

**Packaging finding:** root deliverable matches app-debug. No overwrite performed by this gate.

---

## C — Hard locks

| Lock | Measured |
|------|----------|
| FX/SFX tip diff vs parent `f66131e` | **0** — no path changes for CombatFx / PlumeGarnishKit / IronclashSfx / SoundBus |
| FX/SFX tip diff vs bankone tip `9e96464` | **0** |
| HubRelics / HubOffers prices / overlay art tip diff | **0** (HubOffers.kt not in tip `--stat`; titleBankLine/hubBankLine unchanged) |
| ClimbKept.kt tip diff | **in scope** — added victory-only `seal_breaks` +3 when `won`; base F1+1 / F2+2 / F3+3 / Cave Troll+1 / Gate-Warden+5 / Ash Tithe+3 (`ASH_TITHE_BONUS = 3`) **UNCHANGED** |
| Hub sections | MetaHub Relics → Perks → Skills — HubRelics untouched |
| Mid-climb purse / combat / overlay art | no tip touches |

---

## D — Unit tests

Command: `./gradlew :app:testDebugUnitTest --rerun-tasks --offline`

| Metric | Measured |
|--------|----------|
| BUILD | **SUCCESSFUL** |
| Gradle wall | **8s** |
| XML dir | `app/build/test-results/testDebugUnitTest/` |
| Classes | **53** |
| Tests | **412** |
| Failures | **0** |
| Errors | **0** |
| Skipped | **0** |
| XML time sum | **0.884s** |
| Measured clock | **2026-09-27 18:01 EDT** |

### Named suites

| Class | Result |
|-------|--------|
| TitlebankV0157Test | **6/6** PASS — `titleRemnantSource_isMetaStore_notPurseOrLastPayout`, `finishRun_doesNotInflateRemnantsBankWithLastPayout`, `afterSummaryLeave_titleBankUpdatesFromMetaStoreCommit`, `victorySealPlus3_onlyOnWin_clearTotal15`, `death_noSealLine_totalsUnchangedWithTithe`, `summary_mutedShopPurseLine_andNoPurseToBank` |
| BankoneV0156Test | **5/5** PASS |
| HubsplitV0155Test | **6/6** PASS |
| TrollkeptV0154Test | **7/7** PASS (incl. clear+Tithe **18**) |

---

## E — Cases 1–9

### 1. Title Hub·N and Hub Remnants N — MetaStore remnants_bank only — **PASS**

- MainMenu: `HubOffers.titleBankLine(gc.remnantsBank)` (MainMenuScreen.kt:77); comment + LaunchedEffect bind MetaStore path.
- MetaHub: `HubOffers.hubBankLine(gc.remnantsBank)` (MetaHubScreen.kt:65).
- `gc.remnantsBank` collected from `meta.remnantsBank` Flow (`GameController.kt` collect).
- No Title source from `runWallet` / `lastPayout` / `summary?.kept` (TitlebankV0157Test.`titleRemnantSource_isMetaStore_notPurseOrLastPayout`).

### 2. After Summary Continue OR Menu: Title recomposes from disk — **PASS**

- `leaveRunSummary`: `commitSummaryBank` then `remnantsBank = write.now` (or `meta.remnantsBank.first()` if already committed); then nav; on MainMenu dest logs `TITLE bank=$remnantsBank source=metastore`.
- TitlebankV0157Test.`afterSummaryLeave_titleBankUpdatesFromMetaStoreCommit` + `finishRun_doesNotInflateRemnantsBankWithLastPayout` (leave asserts `meta.remnantsBank.first()` + TITLE log).

### 3. finishRun does NOT set remnantsBank = prev+Kept — **PASS**

- Diff removes `remnantsBank = committed.remnantsBank`; comment: do NOT set remnantsBank = committed.
- TitlebankV0157Test.`finishRun_doesNotInflateRemnantsBankWithLastPayout` asserts absence of `remnantsBank = committed.remnantsBank`.

### 4. Log TITLE bank=N source=metastore present — **PASS**

- MainMenuScreen LaunchedEffect: `Log.i("TITLE", "TITLE bank=${gc.remnantsBank} source=metastore")`.
- leaveRunSummary Menu path: same TITLE log after commit.
- Asserted by TitlebankV0157Test title + finishRun leave tests.

### 5. Victory: "The seal breaks" +3 → Kept 15 / Tithe 18 — **PASS**

- `ClimbKept.finishPayout(..., won)` adds `KeptLine("seal_breaks", "The seal breaks", 3)` iff `won`.
- TitlebankV0157Test.`victorySealPlus3_onlyOnWin_clearTotal15` → kept **15**.
- TrollkeptV0154Test.`keptFormula_cases_winOrDeath_noWalletBank` → max with Tithe **18**.

### 6. Death: NO seal line; death totals unchanged — **PASS**

- TitlebankV0157Test.`victorySealPlus3_onlyOnWin_clearTotal15` death kept **7** (1+2+3+1), no seal.
- `death_noSealLine_totalsUnchangedWithTithe` → **10** with Tithe (1+2+3+1+3), no seal.

### 7. Summary muted under Kept — **PASS**

- RunSummaryScreen: exact `"Shop purse ends with the climb."` muted Bone under Kept list.
- TitlebankV0157Test.`summary_mutedShopPurseLine_andNoPurseToBank` — finishPayout has no runWallet; finishRun comment forbids banking leftover purse.

### 8. Root APK md5 matches app-debug — **PASS**

- Both `c1254dcbe4fc63ab873fcb3a058c6c9a`, size 42425134, **CMP_OK**.

### 9. Unit suite green; FX/SFX tip diff 0 — **PASS**

- Suite **53/412/0/0/0**; Titlebank **6/6**; Bankone **5/5**.
- FX/SFX tip diff vs parent and vs bankone tip `9e96464`: **0**.

---

## F — Result / blockers

**FAIL blockers: none.**

Gate result: **PASS** — tip match, packaging CMP_OK, cases 1–9, unit suite green, FX/SFX tip diff 0.

CoS: tip holds at `5810af12830bfd14eb445b85454a9eb8907f6857`; this gate did not tag or message.
