package com.towerofdarkness.app

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.nav.ShopOffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.1.8-shopwallet: Small Heal @3; wallet≥3 always has an affordable offer;
 * empty wallet (0–2) hides stock; heal blocked at full HP.
 */
class ShopWalletTest {

    @Test
    fun healSmall_priceIs3() {
        val small = GameController.shopCatalog().first { it.kind == "heal_small" }
        assertEquals(3, small.price)
        assertEquals(3, Balance.SHOP_PRICE_MIN)
    }

    @Test
    fun wallet3_alwaysHasAffordableOffer() {
        for (seed in 0..200) {
            val offers = GameController.generateShopOffers(wallet = 3, seed = seed)
            assertTrue(
                "seed=$seed offers=${offers.map { it.kind to it.price }}",
                offers.any { it.price <= 3 }
            )
            assertTrue(
                "seed=$seed should include Small Heal @3",
                offers.any { it.kind == "heal_small" && it.price == 3 }
            )
        }
    }

    @Test
    fun wallet3_hpNotFull_smallHealPurchasableAndHeals() {
        val offers = GameController.generateShopOffers(wallet = 3, seed = 7)
        val heal = offers.first { it.kind == "heal_small" }
        assertTrue(GameController.canBuyShopOffer(heal, wallet = 3, playerHp = 20, maxHp = 30))
        val result = GameController.applyShopBuyWalletHp(heal, wallet = 3, playerHp = 20, maxHp = 30)
        assertNotNull(result)
        assertEquals(0, result!!.first)
        assertEquals(28, result.second) // +8
    }

    @Test
    fun wallet0_showsEmptyState_noPricedPurchase() {
        assertTrue(GameController.shopShowsEmptyState(0))
        assertTrue(GameController.shopShowsEmptyState(1))
        assertTrue(GameController.shopShowsEmptyState(2))
        assertFalse(GameController.shopShowsEmptyState(3))
        val offers = GameController.generateShopOffers(wallet = 0, seed = 1)
        for (o in offers) {
            assertFalse(
                "wallet=0 must not allow buy of ${o.kind}@${o.price}",
                GameController.canBuyShopOffer(o, wallet = 0, playerHp = 20, maxHp = 30)
            )
        }
    }

    @Test
    fun wallet3_hpFull_healBlocked_nonHealFollowsPrice() {
        val offers = GameController.generateShopOffers(wallet = 3, seed = 11)
        val heals = offers.filter { GameController.isHealOfferKind(it.kind) }
        assertTrue(heals.isNotEmpty())
        for (h in heals) {
            assertFalse(
                "full HP must block ${h.kind}",
                GameController.canBuyShopOffer(h, wallet = 3, playerHp = 30, maxHp = 30)
            )
            assertNull(GameController.applyShopBuyWalletHp(h, 3, 30, 30))
        }
        // Rumor / swap: still gated only by price vs wallet (wallet 3 < 6+)
        val nonHeal = offers.filter { !GameController.isHealOfferKind(it.kind) }
        for (o in nonHeal) {
            val can = GameController.canBuyShopOffer(o, wallet = 3, playerHp = 30, maxHp = 30)
            assertEquals(
                "non-heal ${o.kind}@${o.price} with wallet 3",
                o.price <= 3,
                can
            )
        }
        // With a richer wallet, non-heal becomes buyable at full HP
        val peek = ShopOffer("rumor_peek_t", "Rumor Peek", 6, "rumor_peek")
        assertTrue(GameController.canBuyShopOffer(peek, wallet = 6, playerHp = 30, maxHp = 30))
        val swap = ShopOffer("card_swap_t", "Card Swap", 12, "card_swap")
        assertTrue(GameController.canBuyShopOffer(swap, wallet = 12, playerHp = 30, maxHp = 30))
        assertFalse(GameController.canBuyShopOffer(swap, wallet = 3, playerHp = 30, maxHp = 30))
    }

    @Test
    fun doNotSellHealIntoFullHp() {
        val full = ShopOffer("heal_full_t", "Full Heal", 15, "heal_full")
        assertNull(GameController.applyShopBuyWalletHp(full, wallet = 20, playerHp = 30, maxHp = 30))
        val mid = ShopOffer("heal_mid_t", "Mid Heal", 10, "heal_mid")
        assertNull(GameController.applyShopBuyWalletHp(mid, wallet = 20, playerHp = 30, maxHp = 30))
    }
}
