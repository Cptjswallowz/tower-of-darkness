package com.towerofdarkness.app

import com.towerofdarkness.app.domain.art.SharedTilePlate
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.climb.ScrapPouch
import com.towerofdarkness.app.domain.forge.Forge
import com.towerofdarkness.app.domain.forge.ForgeBranch
import com.towerofdarkness.app.domain.forge.ForgeSkillState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

/**
 * v0.1.59-tilepolish — combat pip levels, shop purse string, glossary upgraded,
 * PART B ui_tile_plate wire (docs/art-audio/TILEPOLISH_PLATE_v0.1.59.md).
 */
class TilepolishV0159Test {

    @Test
    fun combatPip_lv1None_lv2II_lv3III() {
        assertNull(Forge.combatPipLabel(1))
        assertEquals("II", Forge.combatPipLabel(2))
        assertEquals("III", Forge.combatPipLabel(3))
        assertEquals("III", Forge.combatPipLabel(99))
    }

    @Test
    fun forgeListPip_keepsRomanI_II_III() {
        assertEquals("I", Forge.pipLabel(1))
        assertEquals("II", Forge.pipLabel(2))
        assertEquals("III", Forge.pipLabel(3))
    }

    @Test
    fun shopPriceLine_saysPurseNotRem() {
        assertEquals("Goblin pile · 8 purse", ScrapPouch.shopPriceLine("Goblin pile", 8))
        assertEquals(
            "Orc pile · 14 purse",
            ScrapPouch.shopPriceLine(ScrapPouch.SHOP_ORC_PILE_TITLE, 14)
        )
        assertEquals("purse", ScrapPouch.SHOP_PRICE_UNIT)
        val shop = File("src/main/java/com/towerofdarkness/app/ui/screens/ShopScreen.kt").readText()
        assertTrue(shop.contains("ScrapPouch.shopPriceLine"))
        assertFalse("Shop must not show rem unit", shop.contains("\${offer.price} rem"))
    }

    @Test
    fun glossaryUpgraded_matchesForgeBody_forSkillLevel() {
        val pike = CardCatalog.byId(Forge.ID_TOWER_PIKE)!!
        val l2 = ForgeSkillState(level = 2, l2 = ForgeBranch.A)
        val body = Forge.glossaryBody(pike, l2).replace("**", "")
        assertEquals("Deal 10 damage.", body)
        val applied = Forge.applyCard(pike, l2)
        assertEquals(body, applied.effect.description.replace("**", ""))
        val loadout = Forge.applyLoadout(listOf(pike), mapOf(pike.id to l2))
        assertEquals(body, loadout.first().effect.description.replace("**", ""))
    }

    @Test
    fun combatSkillSlot_wiresPipAndLongPress() {
        val combat = File("src/main/java/com/towerofdarkness/app/ui/screens/CombatScreen.kt").readText()
        assertTrue(combat.contains("Forge.combatPipLabel"))
        assertTrue(combat.contains("onLongPressGlossary"))
        assertTrue(combat.contains("showSkillGlossary"))
        assertTrue(combat.contains("combinedClickable"))
        assertTrue(combat.contains("SharedTilePlateBox"))
        assertTrue(combat.contains("Alignment.TopEnd"))
        assertTrue(combat.contains("fontSize = 12.sp"))
        assertTrue(combat.contains("R.drawable.ui_tile_plate") ||
            File("src/main/java/com/towerofdarkness/app/ui/components/TilePlateBackdrop.kt")
                .readText().contains("R.drawable.ui_tile_plate"))
    }

    @Test
    fun uiTilePlate_md5MatchesArtDeliverable() {
        assertEquals("ui_tile_plate", SharedTilePlate.DRAWABLE_NAME)
        assertEquals(0.35f, SharedTilePlate.CENTER_ALPHA_DOC, 0.001f)
        val drawable = File("src/main/res/drawable/ui_tile_plate.png")
        assertTrue("ui_tile_plate.png must exist in drawable", drawable.isFile)
        assertEquals(241892L, drawable.length())
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(drawable.readBytes())
        val hex = digest.joinToString("") { "%02x".format(it) }
        assertEquals(SharedTilePlate.EXPECTED_MD5, hex)
        val backdrop = File("src/main/java/com/towerofdarkness/app/ui/components/TilePlateBackdrop.kt").readText()
        assertTrue(backdrop.contains("R.drawable.ui_tile_plate"))
        assertFalse("No FX packs on plate", backdrop.contains("plume") || backdrop.contains("kenney"))
        val forge = File("src/main/java/com/towerofdarkness/app/ui/screens/ForgeSheet.kt").readText()
        assertTrue(forge.contains("SharedTilePlateBox"))
        val path = File("src/main/java/com/towerofdarkness/app/ui/screens/PathScreen.kt").readText()
        assertTrue(path.contains("NODE_DISC_PLATE_ALPHA"))
        assertTrue(path.contains("SharedTilePlateBox"))
    }

    @Test
    fun hubRemnantsWording_unchanged() {
        val hub = File("src/main/java/com/towerofdarkness/app/ui/screens/MetaHubScreen.kt").readText()
        assertFalse(hub.contains("shopPriceLine"))
        val title = File("src/main/java/com/towerofdarkness/app/ui/screens/MainMenuScreen.kt").readText()
        assertTrue(title.contains("remnantsBank") || title.contains("titleBankLine"))
    }
}
