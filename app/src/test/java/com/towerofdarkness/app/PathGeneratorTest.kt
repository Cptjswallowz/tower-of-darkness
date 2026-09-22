package com.towerofdarkness.app

import com.towerofdarkness.app.domain.path.NodeType
import com.towerofdarkness.app.domain.path.PathEdge
import com.towerofdarkness.app.domain.path.PathGenerator
import com.towerofdarkness.app.domain.path.PathNode
import com.towerofdarkness.app.domain.path.TowerPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PathGeneratorTest {

    @Test
    fun sharedDepth_allBranchesAlign_noRowGaps() {
        // Many seeds so both depth=2 and depth=3 are exercised
        for (seed in 0..80) {
            val path = PathGenerator.generate(floor = 1, rng = Random(seed))
            val midBranch = path.nodes.filter {
                it.id.startsWith("b") && it.type != NodeType.START &&
                    it.type != NodeType.BOSS && it.id != "merge"
            }
            assertTrue("seed $seed has mid nodes", midBranch.isNotEmpty())

            // Nodes that edge into merge must share the same row
            val intoMerge = path.edges.filter { it.to == "merge" }.map { it.from }.toSet()
            val rowsIntoMerge = path.nodes.filter { it.id in intoMerge }.map { it.row }.toSet()
            assertEquals(
                "seed $seed: all pre-merge branch ends share one row, got $rowsIntoMerge",
                1,
                rowsIntoMerge.size
            )

            val merge = path.nodes.first { it.id == "merge" }
            val boss = path.nodes.first { it.id == "boss" }
            val depth = rowsIntoMerge.first()
            assertEquals("seed $seed merge row", depth + 1, merge.row)
            assertEquals("seed $seed boss row", depth + 2, boss.row)

            // Within each branch, edges must not skip a row gap
            val branchCols = midBranch.map { it.col }.toSet()
            for (col in branchCols) {
                val chain = midBranch.filter { it.col == col }.sortedBy { it.row }
                assertEquals("seed $seed branch $col contiguous rows", (1..depth).toList(), chain.map { it.row })
                // consecutive edges exist along the branch
                var prev = "start"
                for (n in chain) {
                    assertTrue(
                        "seed $seed edge $prev→${n.id}",
                        path.edges.any { it.from == prev && it.to == n.id }
                    )
                    prev = n.id
                }
                assertTrue(
                    "seed $seed edge $prev→merge",
                    path.edges.any { it.from == prev && it.to == "merge" }
                )
            }
        }
    }


    @Test
    fun noRestOnRow1_firstTierNeverRest_unlessCareLock() {
        // Generation pool forbids REST on row 1; v0.1.10 CARE may convert row1 → REST
        // only when it is the last non-COMBAT on a care-less Start→Boss path.
        for (seed in 0..80) {
            val path = PathGenerator.generate(floor = 1, rng = Random(seed))
            val row1 = path.nodes.filter { it.row == 1 }
            assertTrue("seed $seed has row-1 nodes", row1.isNotEmpty())
            row1.forEach { n ->
                assertTrue(
                    "seed $seed ${n.id} edged from start",
                    path.edges.any { it.from == "start" && it.to == n.id }
                )
                if (n.type == NodeType.REST) {
                    // Must be CARE-forced: every S→B path through this node has CARE via this REST
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
                    val through = routes.filter { n.id in it }
                    assertTrue("seed $seed row1 REST ${n.id} on some S→B", through.isNotEmpty())
                    through.forEach { route ->
                        val care = route.count {
                            byId[it]!!.type == NodeType.REST || byId[it]!!.type == NodeType.SHOP
                        }
                        assertTrue(
                            "seed $seed row1 REST ${n.id} must provide CARE on $route",
                            care >= 1
                        )
                    }
                }
            }
        }
    }


    @Test
    fun atLeastOneCombat_beforeBoss() {
        for (seed in 0..120) {
            val path = PathGenerator.generate(floor = 1, rng = Random(seed))
            val beforeBoss = path.nodes.filter { it.type != NodeType.START && it.type != NodeType.BOSS }
            assertTrue(
                "seed $seed must have ≥1 COMBAT before boss: ${beforeBoss.map { it.type }}",
                beforeBoss.any { it.type == NodeType.COMBAT }
            )
        }
    }

    @Test
    fun eventsCappedAtOnePerFloor() {
        for (seed in 0..120) {
            val path = PathGenerator.generate(floor = 1, rng = Random(seed))
            val events = path.nodes.count { it.type == NodeType.EVENT }
            assertTrue("seed $seed events=$events (max 1)", events <= 1)
        }
    }


    /** All Start→Boss routes (not merely the floor) must include ≥1 COMBAT. */
    @Test
    fun everyStartToBossPath_hasAtLeastOneCombat() {
        for (seed in 0..200) {
            val path = PathGenerator.generate(floor = 1, rng = Random(seed.toLong()))
            val byId = path.nodes.associateBy { it.id }
            val outs = path.edges.groupBy({ it.from }, { it.to })
            val routes = mutableListOf<List<String>>()
            fun dfs(id: String, acc: MutableList<String>) {
                acc += id
                if (id == "boss") {
                    routes += acc.toList()
                } else {
                    for (next in outs[id].orEmpty()) dfs(next, acc)
                }
                acc.removeAt(acc.lastIndex)
            }
            dfs("start", mutableListOf())
            assertTrue("seed $seed must have Start→Boss routes", routes.isNotEmpty())
            for (route in routes) {
                val types = route.map { byId[it]!!.type }
                assertTrue(
                    "seed $seed route $route types=$types must include COMBAT",
                    types.any { it == NodeType.COMBAT }
                )
            }
            // Event cap still holds after routefight conversion
            assertTrue(
                "seed $seed events must stay ≤1",
                path.nodes.count { it.type == NodeType.EVENT } <= 1
            )
        }
    }

    /**
     * Constructed fightless branch + combat on sibling: enforceFloorRules must put
     * COMBAT on the fightless Start→Boss path (prefer merge) without raising events.
     */
    @Test
    fun enforceFloorRules_fightlessBranch_getsCombatPreferMerge() {
        val nodes = listOf(
            PathNode("start", NodeType.START, 0, 1, "s", revealed = true),
            PathNode("b0_r1", NodeType.COMBAT, 1, 0, "c"),
            PathNode("b0_r2", NodeType.SHOP, 2, 0, "sh"),
            PathNode("b1_r1", NodeType.TREASURE, 1, 1, "t"),
            PathNode("b1_r2", NodeType.EVENT, 2, 1, "e"),
            PathNode("merge", NodeType.REST, 3, 1, "r"),
            PathNode("boss", NodeType.BOSS, 4, 1, "b")
        )
        val edges = listOf(
            PathEdge("start", "b0_r1"),
            PathEdge("b0_r1", "b0_r2"),
            PathEdge("b0_r2", "merge"),
            PathEdge("start", "b1_r1"),
            PathEdge("b1_r1", "b1_r2"),
            PathEdge("b1_r2", "merge"),
            PathEdge("merge", "boss")
        )
        val raw = TowerPath(1, nodes, edges, "start")
        // Floor already has COMBAT on branch 0 — old rule would leave branch 1 fightless.
        val fixed = PathGenerator.enforceFloorRules(raw, Random(0))
        val byId = fixed.nodes.associateBy { it.id }
        assertTrue(
            "event cap ≤1",
            fixed.nodes.count { it.type == NodeType.EVENT } <= 1
        )
        assertEquals(
            "prefer merge → COMBAT when a path is fightless",
            NodeType.COMBAT,
            byId["merge"]!!.type
        )
        // CARE may convert the care-less branch EVENT → REST; combat on merge must remain
        assertTrue(
            "all routes keep COMBAT after CARE",
            true // asserted below in routes.forEach
        )
        val outs = fixed.edges.groupBy({ it.from }, { it.to })
        val routes = mutableListOf<List<String>>()
        fun dfs(id: String, acc: MutableList<String>) {
            acc += id
            if (id == "boss") routes += acc.toList()
            else outs[id].orEmpty().forEach { dfs(it, acc) }
            acc.removeAt(acc.lastIndex)
        }
        dfs("start", mutableListOf())
        routes.forEach { route ->
            assertTrue(
                "route $route must have COMBAT",
                route.any { byId[it]!!.type == NodeType.COMBAT }
            )
            assertTrue(
                "route $route must have CARE after routecare",
                route.any {
                    byId[it]!!.type == NodeType.REST || byId[it]!!.type == NodeType.SHOP
                }
            )
        }
    }


    /** All Start→Boss routes must include ≥1 CARE (REST or SHOP). */
    @Test
    fun everyStartToBossPath_hasAtLeastOneCare() {
        for (seed in 0..99) {
            for (floor in listOf(1, 2)) {
                val path = PathGenerator.generate(floor = floor, rng = Random(seed.toLong()))
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
                assertTrue("floor $floor seed $seed routes", routes.isNotEmpty())
                for (route in routes) {
                    val types = route.map { byId[it]!!.type }
                    assertTrue(
                        "floor $floor seed $seed route $route types=$types must include CARE",
                        types.any { it == NodeType.REST || it == NodeType.SHOP }
                    )
                    assertTrue(
                        "floor $floor seed $seed route $route must keep COMBAT",
                        types.any { it == NodeType.COMBAT }
                    )
                }
                assertTrue(
                    "floor $floor seed $seed events ≤1",
                    path.nodes.count { it.type == NodeType.EVENT } <= 1
                )
            }
        }
    }

    /**
     * Care-less branch (treasure/event/combat only) + shop on sibling: enforceFloorRules
     * must put REST on the care-less Start→Boss path (last non-COMBAT) without dropping
     * combat lock or raising events above 1.
     */
    @Test
    fun enforceFloorRules_carelessBranch_getsRestOnLastNonCombat() {
        val nodes = listOf(
            PathNode("start", NodeType.START, 0, 1, "s", revealed = true),
            PathNode("b0_r1", NodeType.SHOP, 1, 0, "sh"),
            PathNode("b0_r2", NodeType.COMBAT, 2, 0, "c"),
            PathNode("b1_r1", NodeType.TREASURE, 1, 1, "t"),
            PathNode("b1_r2", NodeType.EVENT, 2, 1, "e"),
            PathNode("merge", NodeType.COMBAT, 3, 1, "m"),
            PathNode("boss", NodeType.BOSS, 4, 1, "b")
        )
        val edges = listOf(
            PathEdge("start", "b0_r1"),
            PathEdge("b0_r1", "b0_r2"),
            PathEdge("b0_r2", "merge"),
            PathEdge("start", "b1_r1"),
            PathEdge("b1_r1", "b1_r2"),
            PathEdge("b1_r2", "merge"),
            PathEdge("merge", "boss")
        )
        val raw = TowerPath(1, nodes, edges, "start")
        val fixed = PathGenerator.enforceFloorRules(raw, Random(0))
        val byId = fixed.nodes.associateBy { it.id }
        assertTrue(
            "events ≤1",
            fixed.nodes.count { it.type == NodeType.EVENT } <= 1
        )
        // Branch 1 was care-less: last non-COMBAT is b1_r2 (merge is COMBAT) → REST
        assertEquals(
            "last non-COMBAT on care-less path → REST",
            NodeType.REST,
            byId["b1_r2"]!!.type
        )
        val outs = fixed.edges.groupBy({ it.from }, { it.to })
        val routes = mutableListOf<List<String>>()
        fun dfs(id: String, acc: MutableList<String>) {
            acc += id
            if (id == "boss") routes += acc.toList()
            else outs[id].orEmpty().forEach { dfs(it, acc) }
            acc.removeAt(acc.lastIndex)
        }
        dfs("start", mutableListOf())
        routes.forEach { route ->
            assertTrue(
                "route $route must have COMBAT",
                route.any { byId[it]!!.type == NodeType.COMBAT }
            )
            assertTrue(
                "route $route must have CARE",
                route.any {
                    byId[it]!!.type == NodeType.REST || byId[it]!!.type == NodeType.SHOP
                }
            )
        }
    }

}
