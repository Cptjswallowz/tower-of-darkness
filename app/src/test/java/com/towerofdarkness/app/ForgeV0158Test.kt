package com.towerofdarkness.app

import com.towerofdarkness.app.data.MidRunAshbrand
import com.towerofdarkness.app.data.MidRunLoadout
import com.towerofdarkness.app.data.MidRunResume
import com.towerofdarkness.app.data.MidRunSlot
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.climb.ClimbKept
import com.towerofdarkness.app.domain.climb.ClimbKeptFlags
import com.towerofdarkness.app.domain.climb.ScrapPouch
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import com.towerofdarkness.app.domain.forge.Forge
import com.towerofdarkness.app.domain.forge.ForgeBranch
import com.towerofdarkness.app.domain.forge.ForgeSkillState
import com.towerofdarkness.app.domain.hub.HubOffers
import com.towerofdarkness.app.domain.path.PathGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * v0.1.58-forge — relic effects, scrap HUD, forge costs/cancel, Gate Sigil Kept.
 * See docs/forge-v0158.md + docs/forge-choices-v0158.md.
 */
class ForgeV0158Test {

    @Test
    fun sootRim_climbStart_plus1GoblinScrap() {
        assertEquals(0, ScrapPouch.climbStartGoblin(emptySet()))
        assertEquals(1, ScrapPouch.climbStartGoblin(setOf(ClimbKept.ID_SOOT_RIM)))
    }

    @Test
    fun ashPauldron_stacksWarmAsh_brace() {
        assertEquals(0, HubOffers.climbStartBrace(emptySet()))
        assertEquals(2, HubOffers.climbStartBrace(setOf(HubOffers.ID_WARM_ASH)))
        assertEquals(1, HubOffers.climbStartBrace(setOf(ClimbKept.ID_ASH_PAULDRON)))
        assertEquals(
            3,
            HubOffers.climbStartBrace(setOf(HubOffers.ID_WARM_ASH, ClimbKept.ID_ASH_PAULDRON))
        )
    }

    @Test
    fun trollTooth_firstDamagingHit_eliteOrBoss_plus2Once() {
        val enemy = Enemy.normal(EnemyKind.CAVE_TROLL, 3)
        val cards = listOf(CardCatalog.byId("hostflint")!!)
        val engine = CombatEngine(Random(1))
        var s = engine.start(
            activeCards = cards,
            enemy = enemy,
            weapon = WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 30,
            playerHp = 30,
            trollToothReady = true
        )
        assertTrue(s.trollToothReady)
        // Hostflint deals 5; with tooth → 7 raw before brace
        s = engine.diceTumble(s.copy(
            // force highlight hostflint by spending others empty
            highlightedId = "hostflint",
            lastFiredCard = cards[0]
        ))
        // Directly resolve skill path: use resolveSkill after setting lastFired
        s = s.copy(lastFiredCard = cards[0], highlightedId = "hostflint")
        val beforeHp = s.enemy.hp
        s = engine.resolveSkill(s)
        val dealt = beforeHp - s.enemy.hp
        assertEquals(7, dealt) // 5 + 2 tooth
        assertFalse(s.trollToothReady)
        // Second hit: no more +2
        val cards2 = listOf(CardCatalog.byId("hostflint")!!)
        s = s.copy(
            spentIds = emptySet(),
            lastFiredCard = cards2[0],
            highlightedId = "hostflint",
            activeCards = cards2
        )
        val before2 = s.enemy.hp
        s = engine.resolveSkill(s)
        assertEquals(5, before2 - s.enemy.hp)
    }

    @Test
    fun gateSigil_keptPlus1_winOrDeath_ownedOrEarned() {
        val flags = ClimbKeptFlags(gateWardenBeaten = true, f1CombatWon = true)
        val earned = ClimbKept.finishPayout(flags, emptySet(), won = true)
        assertTrue(earned.lines.any { it.label == "Gate Sigil" && it.amount == 1 })
        assertTrue(ClimbKept.ID_GATE_SIGIL in earned.newTrophyIds)

        val owned = ClimbKept.finishPayout(flags, setOf(ClimbKept.ID_GATE_SIGIL), won = false)
        assertTrue(owned.lines.any { it.label == "Gate Sigil" && it.amount == 1 })
        assertTrue(owned.newTrophyIds.isEmpty())

        val noGate = ClimbKept.finishPayout(
            ClimbKeptFlags(f1CombatWon = true), emptySet(), won = false
        )
        assertFalse(noGate.lines.any { it.label == "Gate Sigil" })
    }

    @Test
    fun scrapHud_exactForm() {
        assertEquals("HP 20 · purse 5 · g3 o1", ScrapPouch.hudLine(20, 5, 3, 1))
        assertEquals("HP 20 / 30 · purse 5 · g0 o0", ScrapPouch.hudLineWithMax(20, 30, 5, 0, 0))
    }

    @Test
    fun scrapDrops_tables() {
        val g = (0..200).map { ScrapPouch.dropGoblin(Random(it.toLong())) }
        assertTrue(g.any { it.goblin == 1 })
        assertTrue(g.any { it.goblin == 2 })
        assertEquals(ScrapPouch.dropOrc(), com.towerofdarkness.app.domain.climb.ScrapGrant(1, 1))
        assertEquals(ScrapPouch.dropElite(), com.towerofdarkness.app.domain.climb.ScrapGrant(2, 2))
        assertEquals(ScrapPouch.dropBoss(), com.towerofdarkness.app.domain.climb.ScrapGrant(0, 2))
        val t = (0..300).map { ScrapPouch.dropTreasure(Random(it.toLong())) }
        assertTrue(t.any { it == com.towerofdarkness.app.domain.climb.ScrapGrant(2, 0) })
        assertTrue(t.any { it == com.towerofdarkness.app.domain.climb.ScrapGrant(0, 1) })
        assertTrue(t.any { it == com.towerofdarkness.app.domain.climb.ScrapGrant(1, 1) })
    }

    @Test
    fun forgeCosts_l2_3g_l3_2g2o_cancelNoSpend() {
        assertTrue(Forge.canAffordL2(3))
        assertFalse(Forge.canAffordL2(2))
        assertEquals(0 to 5, Forge.spendL2(3, 5))
        assertNull(Forge.spendL2(2, 5))
        assertTrue(Forge.canAffordL3(2, 2))
        assertFalse(Forge.canAffordL3(2, 1))
        assertEquals(1 to 0, Forge.spendL3(3, 2))
        assertNull(Forge.spendL3(1, 2))
    }

    @Test
    fun forgeChips_exactArchitect_ironMantleL3B_fallback() {
        val pike = CardCatalog.byId("tower_pike")!!
        val l2 = Forge.choicesFor(pike, ForgeSkillState())
        assertEquals(listOf("+2 damage", "Soften 1"), l2.map { it.chip })
        val l3 = Forge.choicesFor(pike, ForgeSkillState(level = 2, l2 = ForgeBranch.A))
        assertEquals(listOf("+2 damage", "After fire: Brace 1"), l3.map { it.chip })

        val mantle = CardCatalog.byId("iron_mantle")!!
        val mL2 = Forge.choicesFor(mantle, ForgeSkillState())
        assertEquals(listOf("Brace +1", "On Brace: deal 2"), mL2.map { it.chip })
        val mL3 = Forge.choicesFor(mantle, ForgeSkillState(level = 2, l2 = ForgeBranch.A))
        // Fallback chip — no weight hook
        assertEquals(listOf("Brace +1", "On Brace: deal 2"), mL3.map { it.chip })
        assertEquals(
            "Gain **Brace 4** (absorb before HP). When this grants Brace, deal **2**.",
            mL3[1].glossaryBody
        )
    }

    @Test
    fun relicShard_unmapped() {
        assertFalse(Forge.isForgeable(Forge.ID_RELIC_SHARD))
        val shard = CardCatalog.byId("relic_shard")!!
        assertTrue(Forge.choicesFor(shard, ForgeSkillState()).isEmpty())
        assertEquals("Unmapped", Forge.greyReason(Forge.ID_RELIC_SHARD, ForgeSkillState(), 99, 99))
    }

    @Test
    fun forgeGlossary_pikeL2A_andStacked() {
        val pike = CardCatalog.byId("tower_pike")!!
        assertEquals(
            "Deal **10** damage.",
            Forge.glossaryBody(pike, ForgeSkillState(2, ForgeBranch.A))
        )
        assertEquals(
            "Deal **12** damage.",
            Forge.glossaryBody(pike, ForgeSkillState(3, ForgeBranch.A, ForgeBranch.A))
        )
        assertEquals(
            "Deal **8** damage. **Soften 1**. After this fires, gain **Brace 1**.",
            Forge.glossaryBody(pike, ForgeSkillState(3, ForgeBranch.B, ForgeBranch.B))
        )
    }

    @Test
    fun midRun_scrapAndForge_roundTrip() {
        val path = PathGenerator.generate(1, Random(58L))
        val forge = mapOf(
            "tower_pike" to ForgeSkillState(2, ForgeBranch.A),
            "dust_veil" to ForgeSkillState(3, ForgeBranch.B, ForgeBranch.A)
        )
        val slot = MidRunSlot(
            runId = "run-forge",
            rngSeed = 58L,
            floor = 1,
            pendingStairContinue = false,
            path = MidRunSlot.fromPath(path, "f1"),
            playerHp = 22,
            playerMaxHp = 30,
            runWallet = 11,
            runRemnantsEarned = 11,
            nodesCleared = 2,
            loadout = MidRunLoadout(true, CardCatalog.defaultLoadoutIds),
            ashbrand = MidRunAshbrand("ashbrand", 1, 0),
            freeScoutCharges = 0,
            rumorRerolls = 1,
            combatSpeed2x = false,
            unlocksThisRun = emptyList(),
            shopVisits = emptyList(),
            resume = MidRunResume.Path,
            scrapGoblin = 4,
            scrapOrc = 2,
            forgeLevels = Forge.encodeLevels(forge),
            forgeBranches = Forge.encodeBranches(forge),
            trollToothFloor = 1
        )
        val json = MidRunSlot.encode(slot)
        val back = MidRunSlot.decode(json)
        assertNotNull(back)
        assertEquals(4, back!!.scrapGoblin)
        assertEquals(2, back.scrapOrc)
        assertEquals(2, back.forgeLevels["tower_pike"])
        assertEquals("A", back.forgeBranches["tower_pike"])
        assertEquals("BA", back.forgeBranches["dust_veil"])
        assertEquals(1, back.trollToothFloor)
        // remnants_bank never in mid-run
        assertFalse(json.contains("remnants_bank"))
    }

    @Test
    fun shopPiles_oneEach() {
        val offers = com.towerofdarkness.app.nav.GameController.generateShopOffers(20, 58)
        assertEquals(1, offers.count { it.kind == "goblin_pile" })
        assertEquals(1, offers.count { it.kind == "orc_pile" })
        assertEquals(ScrapPouch.SHOP_GOBLIN_PILE_TITLE, offers.first { it.kind == "goblin_pile" }.title)
        assertEquals(ScrapPouch.SHOP_ORC_PILE_TITLE, offers.first { it.kind == "orc_pile" }.title)
        assertEquals(8, offers.first { it.kind == "goblin_pile" }.price)
        assertEquals(14, offers.first { it.kind == "orc_pile" }.price)
    }

    @Test
    fun remnantsBank_titleBinding_untouched_inForgeSources() {
        fun read(vararg rel: String): String {
            for (r in rel) {
                val f = java.io.File(r)
                if (f.isFile) return f.readText()
            }
            error("missing ${rel.toList()}")
        }
        val meta = read(
            "app/src/main/java/com/towerofdarkness/app/data/MetaStore.kt",
            "src/main/java/com/towerofdarkness/app/data/MetaStore.kt"
        )
        assertTrue(meta.contains("KEY_REMNANTS"))
        val forge = read(
            "app/src/main/java/com/towerofdarkness/app/domain/forge/Forge.kt",
            "src/main/java/com/towerofdarkness/app/domain/forge/Forge.kt"
        )
        assertFalse(forge.contains("remnants_bank"))
        assertFalse(forge.contains("remnantsBank"))
    }
}
