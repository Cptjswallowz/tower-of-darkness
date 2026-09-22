# QA Gate: v0.1.8-shopwallet (Compose Android)

**Date:** 2026-09-22 (America/New_York, EDT)  
**Project root:** `/workspace/tower-of-darkness`  
**Scope:** Measure-only. No balance / path / Wake / swap / counters / HP retunes.  
**Hard exclusion:** Godot / tower-of-the-world / verify_v032 not touched or cited.

---

## 1) APK + git

| Item | Measured |
|------|----------|
| APK path | `/workspace/tower-of-darkness/tower-of-darkness-debug.apk` |
| APK size | **17711792** bytes |
| APK mtime | **2026-09-22 12:12:21 EDT** (`ls -la` / `stat`) |
| `git log -1` | `d763dad v0.1.8-shopwallet: Small Heal 3 rem, affordable stock, empty-wallet UX` |
| `git rev-parse HEAD` | `d763dadff57dd248efad3c716be06060d665dcec` |
| HEAD vs claimed | **Matches** claimed commit `d763dad` |

---

## 2) ShopWalletTest

**Command:**
```bash
./gradlew :app:testDebugUnitTest --tests '*ShopWalletTest'
```

**Gradle result:** **BUILD SUCCESSFUL** (1s; 22 actionable tasks: 1 executed, 21 up-to-date)

**JUnit XML:** `app/build/test-results/testDebugUnitTest/TEST-com.towerofdarkness.app.ShopWalletTest.xml`  
tests=6, failures=0, errors=0, skipped=0

| Test method | Result |
|-------------|--------|
| `healSmall_priceIs3` | **PASS** |
| `wallet3_alwaysHasAffordableOffer` | **PASS** |
| `wallet3_hpNotFull_smallHealPurchasableAndHeals` | **PASS** |
| `wallet0_showsEmptyState_noPricedPurchase` | **PASS** |
| `wallet3_hpFull_healBlocked_nonHealFollowsPrice` | **PASS** |
| `doNotSellHealIntoFullHp` | **PASS** |

### CoS case mapping

| Case | Intent | Covering test(s) | Verdict | Evidence |
|------|--------|------------------|---------|----------|
| **a** | wallet=3, HP not full → Small Heal @3 rem tappable; buy spends 3 and heals | `healSmall_priceIs3`, `wallet3_alwaysHasAffordableOffer`, `wallet3_hpNotFull_smallHealPurchasableAndHeals` | **PASS** | Catalog Small Heal price=3; wallet=3 always includes `heal_small`@3; `canBuy` true at HP 20/30; `applyShopBuyWalletHp` → wallet 0, HP 28 (+8) |
| **b** | wallet=0 → no priced buttons; "Nothing you can buy." + Leave only | `wallet0_showsEmptyState_noPricedPurchase` + UI spot-check | **PASS** | `shopShowsEmptyState(0..2)==true`; no offer `canBuy` at wallet=0. UI: `ShopScreen.kt:37` copy; `ShopScreen.kt:36–39` hide stock; `ShopScreen.kt:61–62` Leave always |
| **c** | wallet=3, HP full → heal not purchasable; other offers still price-gated | `wallet3_hpFull_healBlocked_nonHealFollowsPrice`, `doNotSellHealIntoFullHp` | **PASS** | Heals blocked / `apply` null at full HP; non-heal `canBuy` iff `price <= 3`; richer wallet still buys rumor/swap at full HP |

---

## 3) Spot-check (read-only)

**Empty-wallet copy:** `app/src/main/java/com/towerofdarkness/app/ui/screens/ShopScreen.kt:37` — `"Nothing you can buy."`  
**Empty gate:** `ShopScreen.kt:27` → `GameController.shopShowsEmptyState`; `GameController.kt:381–382` — `wallet < Balance.SHOP_PRICE_MIN`  
**Buy/heal gate:** `GameController.kt:384–393` (`canBuyShopOffer`); `399–413` (`applyShopBuyWalletHp`); Small Heal +8 at `408` / catalog `417`  
**UI Leave:** `ShopScreen.kt:61–62` — `OutlinedButton` "Leave" outside empty/stock branch (always shown)

---

## 4) Gate verdict

| Criterion | Result |
|-----------|--------|
| APK present with recorded size/mtime | Yes |
| HEAD == d763dad | Yes |
| All ShopWalletTest methods PASS | Yes (6/6) |
| CoS cases a/b/c | PASS / PASS / PASS |

### Overall Gate: **PASS**
