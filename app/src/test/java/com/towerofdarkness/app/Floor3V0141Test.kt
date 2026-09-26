package com.towerofdarkness.app

import com.towerofdarkness.app.data.MidRunAshbrand
import com.towerofdarkness.app.data.MidRunFloorLoadout
import com.towerofdarkness.app.data.MidRunLoadout
import com.towerofdarkness.app.data.MidRunResume
import com.towerofdarkness.app.data.MidRunSlot
import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.combat.BodyArt
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.EnemyKitRole
import com.towerofdarkness.app.domain.combat.EnemyKits
import com.towerofdarkness.app.domain.combat.EnemyLook
import com.towerofdarkness.app.domain.combat.EnemySkillKind
import com.towerofdarkness.app.domain.combat.HallwayPacks
import com.towerofdarkness.app.domain.combat.HallwayRole
import com.towerofdarkness.app.domain.glossary.Glossary
import com.towerofdarkness.app.domain.path.FloorArt
import com.towerofdarkness.app.domain.path.NodeType
import com.towerofdarkness.app.domain.path.PathGenerator
import com.towerofdarkness.app.nav.BossWinNav
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.nav.NavState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

/**
 * v0.1.41-floor3 — F3 graph, packs, Cave Troll, Gate-Warden, floor-locked loadout, save.
 */
class Floor3V0141Test {

    @Test
    fun f3Graph_fiveMidRows_threeBranches_restBeforeBoss() {
        for (seed in 0..99) {
            val path = PathGenerator.generate(floor = 3, rng = Random(seed.toLong()))
            assertEquals(3, path.floor)
            val mid = path.nodes.filter { it.type != NodeType.START && it.type != NodeType.BOSS }
            val midRows = mid.map { it.row }.toSet()
            assertEquals("seed $seed mid rows", setOf(1, 2, 3, 4, 5), midRows)
            val branchCols = mid.filter { it.id.startsWith("b") && it.id != "boss" }.map { it.col }.toSet()
            assertEquals("seed $seed 3 branches", setOf(0, 1, 2), branchCols)
            val boss = path.nodes.first { it.type == NodeType.BOSS }
            assertEquals(6, boss.row)

            val byId = path.nodes.associateBy { it.id }
            val outs = path.edges.groupBy({ it.from }, { it.to })
            val routes = mutableListOf<List<String>>()
            fun dfs(id: String, acc: MutableList<String>) {
                acc += id
                if (id == "boss") routes += acc.toList()
                else outs[id].orEmpty().forEach { dfs(it, acc) }
                acc.removeAt(acc.lastIndex)
            }
            dfs("start", mutableListOf())
            assertTrue(routes.isNotEmpty())
            for (route in routes) {
                val mids = route.filter {
                    byId[it]!!.type != NodeType.START && byId[it]!!.type != NodeType.BOSS
                }
                assertTrue(mids.isNotEmpty())
                assertEquals(NodeType.REST, byId[mids.last()]!!.type)
                val beforeRest = mids.dropLast(1)
                assertTrue(
                    "seed $seed route needs ≥2 COMBAT before Rest: ${beforeRest.map { byId[it]!!.type }}",
                    beforeRest.count { byId[it]!!.type == NodeType.COMBAT } >= 2
                )
            }
            assertTrue(path.nodes.count { it.type == NodeType.EVENT } <= 1)
            assertTrue(
                path.nodes.any { it.type == NodeType.TREASURE || it.type == NodeType.SHOP }
            )
        }
    }

    @Test
    fun f1f2Generator_shapeUnchanged_vsExistingLocks() {
        for (seed in 0..40) {
            val f1 = PathGenerator.generate(1, Random(seed.toLong()))
            val f2 = PathGenerator.generate(2, Random(seed.toLong()))
            // F1/F2: branch depth 2..3 (pre-merge); merge + boss follow. Not F3's fixed 4+merge.
            val f1BranchRows = f1.nodes.filter { it.id.startsWith("b") && it.id != "boss" }.map { it.row }.toSet()
            val f2BranchRows = f2.nodes.filter { it.id.startsWith("b") && it.id != "boss" }.map { it.row }.toSet()
            assertTrue("F1 branch rows $f1BranchRows", f1BranchRows.max() in 2..3)
            assertTrue("F2 branch rows $f2BranchRows", f2BranchRows.max() in 2..3)
            assertFalse("F1 must not use F3 five-row shape", f1BranchRows == setOf(1, 2, 3, 4))
            val f1Branches = f1.nodes.filter { it.id.startsWith("b") && it.id != "boss" }.map { it.col }.toSet().size
            assertTrue(f1Branches in 2..3)
            // F3 fixed shape still distinct
            val f3 = PathGenerator.generate(3, Random(seed.toLong()))
            val f3BranchRows = f3.nodes.filter { it.id.startsWith("b") && it.id != "boss" }.map { it.row }.toSet()
            assertEquals(setOf(1, 2, 3, 4), f3BranchRows)
        }
    }

    @Test
    fun f3SpawnWeights_approx20_40_40_andTrollLooksThird() {
        assertEquals(20, HallwayPacks.goblinWeightPct(3))
        assertEquals(40, HallwayPacks.orcWeightPct(3))
        assertEquals(40, HallwayPacks.caveTrollWeightPct(3))
        // F1/F2 frozen
        assertEquals(75, HallwayPacks.goblinWeightPct(1))
        assertEquals(25, HallwayPacks.orcWeightPct(1))
        assertEquals(30, HallwayPacks.goblinWeightPct(2))
        assertEquals(70, HallwayPacks.orcWeightPct(2))
        assertEquals(0, HallwayPacks.caveTrollWeightPct(1))
        assertEquals(0, HallwayPacks.caveTrollWeightPct(2))

        val n = 6000
        var g = 0; var o = 0; var t = 0
        for (i in 0 until n) {
            when (HallwayPacks.rollRole(3, Random(i.toLong() * 41))) {
                HallwayRole.WEAK_GOBLIN -> g++
                HallwayRole.STURDY_ORC -> o++
                HallwayRole.CAVE_TROLL -> t++
            }
        }
        assertTrue("goblin ${g * 100.0 / n}", abs(g * 100.0 / n - 20.0) < 3.5)
        assertTrue("orc ${o * 100.0 / n}", abs(o * 100.0 / n - 40.0) < 3.5)
        assertTrue("troll ${t * 100.0 / n}", abs(t * 100.0 / n - 40.0) < 3.5)

        val looks = mutableMapOf<EnemyLook, Int>()
        repeat(3000) { i ->
            val look = HallwayPacks.rollLook(HallwayRole.CAVE_TROLL, Random(i.toLong()))
            looks[look] = (looks[look] ?: 0) + 1
        }
        assertEquals(3, looks.size)
        EnemyLook.caveTrollLooks().forEach { look ->
            val c = looks[look]!!
            assertTrue("$look ~1/3 got $c", abs(c - 1000) < 300)
        }
    }

    @Test
    fun caveTroll_hp28_kitClubHideHit_braceNoSlashFamily() {
        assertEquals(28, Balance.CAVE_TROLL_HP)
        val e = Enemy.normal(EnemyKind.CAVE_TROLL, floor = 3, look = EnemyLook.CAVE_TROLL_A)
        assertEquals(28, e.hp)
        assertEquals(28, e.maxHp)
        assertFalse(e.isBoss)
        assertEquals(EnemyKitRole.CAVE_TROLL, EnemyKitRole.fromEnemy(e))
        val kit = EnemyKits.skillsFor(e)
        assertEquals(listOf("club", "hide", "hit"), kit.map { it.id })
        assertEquals(2, kit[0].weight)
        assertEquals(8, kit[0].damage)
        assertEquals(EnemySkillKind.DAMAGE, kit[0].kind)
        assertEquals(2, kit[1].weight)
        assertEquals(4, kit[1].braceGain)
        assertEquals(EnemySkillKind.BRACE, kit[1].kind)
        assertEquals("hide", kit[1].glossaryKey)
        assertEquals(5, kit[2].weight)
        assertEquals(7, kit[2].damageMin)
        assertEquals(9, kit[2].damageMax)
        assertNotNull(Glossary.definition("club"))
        assertTrue(Glossary.definition("club")!!.contains("troll", ignoreCase = true))
        // Hide id stays on FX NO_STROKE set (CombatFx frozen)
        assertEquals("hide", kit[1].id)
    }

    @Test
    fun gateWarden_hp36_kitPulse7_rustGuard_hit6to9() {
        assertEquals(36, Balance.BOSS_FLOOR3_HP)
        val boss = Enemy.boss(3)
        assertEquals(EnemyKind.GATE_WARDEN, boss.kind)
        assertEquals("Gate-Warden", boss.kind.displayName)
        assertEquals(36, boss.hp)
        assertEquals(36, boss.maxHp)
        assertTrue(boss.isBoss)
        assertNull(boss.look)
        val kit = EnemyKits.skillsFor(boss)
        assertEquals(listOf("gate_pulse", "rust_guard", "hit"), kit.map { it.id })
        assertEquals(2, kit[0].weight)
        assertEquals(7, kit[0].damage)
        assertEquals(EnemySkillKind.DAMAGE, kit[0].kind)
        assertEquals(2, kit[1].weight)
        assertEquals(4, kit[1].braceGain)
        assertEquals(EnemySkillKind.BRACE, kit[1].kind)
        assertEquals(5, kit[2].weight)
        assertEquals(6, kit[2].damageMin)
        assertEquals(9, kit[2].damageMax)
        assertNotNull(Glossary.definition("gate pulse"))
        // F1/F2 bosses unchanged
        assertEquals(EnemyKind.DRAGON, Enemy.boss(1).kind)
        assertEquals(28, Enemy.boss(1).hp)
        assertEquals(EnemyKind.ASH_WARDEN, Enemy.boss(2).kind)
        assertEquals(32, Enemy.boss(2).hp)
    }

    @Test
    fun bossWinNav_f2Stair_f3Hub() {
        assertEquals(BossWinNav.FLOOR_BREAK, GameController.afterBossWinNav(1))
        assertEquals(BossWinNav.FLOOR_BREAK, GameController.afterBossWinNav(2))
        assertEquals(BossWinNav.SUMMARY_VICTORY, GameController.afterBossWinNav(3))
        assertTrue(GameController.usesFloorLockedLoadout(3))
        assertFalse(GameController.usesFloorLockedLoadout(1))
        assertFalse(GameController.usesFloorLockedLoadout(2))
    }

    @Test
    fun floorArt_f3UsesCaveBackdrop_f1f2Unchanged() {
        assertEquals("floor_backdrop", FloorArt.BACKDROP_DRAWABLE)
        assertEquals("floor_backdrop_cave", FloorArt.BACKDROP_CAVE_DRAWABLE)
        assertEquals("floor_backdrop", FloorArt.backdropDrawableName(1))
        assertEquals("floor_backdrop", FloorArt.backdropDrawableName(2))
        assertEquals("floor_backdrop_cave", FloorArt.backdropDrawableName(3))
        assertTrue(FloorArt.backdropShipped())
        assertTrue(FloorArt.caveBackdropShipped())
        assertTrue(FloorArt.floor3Darker(3))
        assertFalse(FloorArt.floor3Darker(2))
        // F2 still darker via extra scrim on hall plate; F3 uses Art cave plate + base scrim
        assertTrue(FloorArt.scrimAlpha(2) > FloorArt.scrimAlpha(1))
        assertEquals(FloorArt.SCRIM_ALPHA_FLOOR1, FloorArt.scrimAlpha(3), 0.001f)
    }

    @Test
    fun floorLoadout_saveRestore_andExplainerFlag() {
        val path = PathGenerator.generate(3, Random(41L))
        val snap = MidRunSlot.fromPath(path, "floor3_41")
        val slot = MidRunSlot(
            runId = "run-f3",
            rngSeed = 41L,
            floor = 3,
            pendingStairContinue = false,
            path = snap,
            playerHp = 22,
            playerMaxHp = 30,
            runWallet = 18,
            runRemnantsEarned = 30,
            nodesCleared = 4,
            loadout = MidRunLoadout(
                locked = true,
                cardIds = listOf("strike", "guard", "spark", "cleave", "brace")
            ),
            ashbrand = MidRunAshbrand("ashbrand", 2, 1),
            freeScoutCharges = 0,
            rumorRerolls = 1,
            combatSpeed2x = false,
            unlocksThisRun = emptyList(),
            shopVisits = emptyList(),
            resume = MidRunResume.Path,
            seenF3Explainer = true,
            floorLoadout = MidRunFloorLoadout(
                floor = 3,
                locked = true,
                cardIds = listOf("strike", "guard", "spark", "cleave", "brace"),
                weaponId = "ashbrand"
            )
        )
        val decoded = MidRunSlot.decode(MidRunSlot.encode(slot))!!
        assertTrue(decoded.seenF3Explainer)
        assertNotNull(decoded.floorLoadout)
        assertEquals(3, decoded.floorLoadout!!.floor)
        assertTrue(decoded.floorLoadout!!.locked)
        assertEquals(5, decoded.floorLoadout!!.cardIds.size)
        assertEquals("ashbrand", decoded.floorLoadout!!.weaponId)
        assertEquals(NavState.Path, MidRunSlot.resumeNav(decoded))

        // Unlocked re-pick → Loadout
        val unlocked = decoded.copy(
            floorLoadout = decoded.floorLoadout!!.copy(locked = false)
        )
        assertEquals(NavState.Loadout, MidRunSlot.resumeNav(unlocked))

        // F1/F2 null floor_loadout defaults
        val f1 = MidRunSlot(
            runId = "run-f1",
            rngSeed = 1L,
            floor = 1,
            pendingStairContinue = false,
            path = MidRunSlot.fromPath(PathGenerator.generate(1, Random(1L)), "f1"),
            playerHp = 30,
            playerMaxHp = 30,
            runWallet = 0,
            runRemnantsEarned = 0,
            nodesCleared = 0,
            loadout = MidRunLoadout(false, emptyList()),
            ashbrand = MidRunAshbrand("ashbrand", 1, 0),
            freeScoutCharges = 0,
            rumorRerolls = 1,
            combatSpeed2x = false,
            unlocksThisRun = emptyList(),
            shopVisits = emptyList(),
            resume = MidRunResume.Path
        )
        val f1d = MidRunSlot.decode(MidRunSlot.encode(f1))!!
        assertFalse(f1d.seenF3Explainer)
        assertNull(f1d.floorLoadout)
        assertEquals(NavState.Path, MidRunSlot.resumeNav(f1d))
    }

    @Test
    fun pathCombatNodes_authorCaveTrollPack_persist() {
        var sawTroll = false
        for (seed in 0..80) {
            val path = PathGenerator.generate(3, Random(seed.toLong() + 900))
            for (n in path.nodes.filter { it.type == NodeType.COMBAT }) {
                assertNotNull(n.packKind)
                assertNotNull(n.packLook)
                assertTrue(
                    n.packKind == "GOBLIN" || n.packKind == "ORC" || n.packKind == "CAVE_TROLL"
                )
                if (n.packKind == "CAVE_TROLL") {
                    sawTroll = true
                    assertNotNull(EnemyLook.fromId(n.packLook))
                    assertEquals(HallwayRole.CAVE_TROLL, EnemyLook.fromId(n.packLook)!!.role)
                }
            }
        }
        assertTrue("expected some Cave Troll authored packs", sawTroll)
    }

    @Test
    fun caveTrollArt_shippedMd5() {
        BodyArt.caveTrollDrawableNames().forEach { name ->
            assertTrue(BodyArt.packArtShipped(name))
        }
        val md5 = BodyArt.caveTrollMd5ByDrawable()
        for ((name, expect) in md5) {
            val f = listOf(
                java.io.File("app/src/main/res/drawable/$name.png"),
                java.io.File("src/main/res/drawable/$name.png")
            ).firstOrNull { it.isFile }
            assertNotNull(name, f)
            val actual = java.security.MessageDigest.getInstance("MD5")
                .digest(f!!.readBytes())
                .joinToString("") { "%02x".format(it) }
            assertEquals(name, expect, actual)
        }
        assertEquals(BodyArt.GATE_WARDEN_DRAWABLE, BodyArt.enemyPortraitDrawableName(EnemyKind.GATE_WARDEN))
        val gate = listOf(
            java.io.File("app/src/main/res/drawable/portrait_gate_warden.png"),
            java.io.File("src/main/res/drawable/portrait_gate_warden.png")
        ).firstOrNull { it.isFile }
        assertNotNull(gate)
        val gMd5 = java.security.MessageDigest.getInstance("MD5")
            .digest(gate!!.readBytes())
            .joinToString("") { "%02x".format(it) }
        assertEquals(BodyArt.GATE_WARDEN_MD5, gMd5)
    }
}
