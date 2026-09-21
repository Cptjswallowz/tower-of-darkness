package com.towerofdarkness.app

import com.towerofdarkness.app.domain.path.NodeType
import com.towerofdarkness.app.domain.path.PathGenerator
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
    fun noRestOnRow1_firstTierNeverRest() {
        for (seed in 0..80) {
            val path = PathGenerator.generate(floor = 1, rng = Random(seed))
            val row1 = path.nodes.filter { it.row == 1 }
            assertTrue("seed $seed has row-1 nodes", row1.isNotEmpty())
            row1.forEach { n ->
                assertTrue(
                    "seed $seed row1 ${n.id} must not be REST (was ${n.type})",
                    n.type != NodeType.REST
                )
                // Must be direct from start
                assertTrue(
                    "seed $seed ${n.id} edged from start",
                    path.edges.any { it.from == "start" && it.to == n.id }
                )
            }
            // Rest still allowed deeper
            val deeper = path.nodes.filter { it.row >= 2 && it.type != NodeType.BOSS }
            // no assertion that Rest must appear — only that row1 forbids it
            deeper.forEach { /* ok */ }
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
}
