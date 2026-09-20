package com.towerofdarkness.app.domain.path

import kotlin.random.Random

enum class NodeType { START, COMBAT, EVENT, TREASURE, SHOP, REST, BOSS }

data class PathNode(
    val id: String,
    val type: NodeType,
    val row: Int,
    val col: Int,
    val rumor: String,
    val revealed: Boolean = false,
    val scoutedTypeOnly: Boolean = false,
    val cleared: Boolean = false
)

data class PathEdge(val from: String, val to: String)

data class TowerPath(
    val floor: Int,
    val nodes: List<PathNode>,
    val edges: List<PathEdge>,
    val currentId: String
) {
    fun current(): PathNode = nodes.first { it.id == currentId }
    fun node(id: String): PathNode = nodes.first { it.id == id }
    fun choices(): List<PathNode> {
        val outs = edges.filter { it.from == currentId }.map { it.to }.toSet()
        return nodes.filter { it.id in outs && !it.cleared }
    }
    fun withReveal(id: String, typeOnly: Boolean = false): TowerPath {
        val updated = nodes.map {
            if (it.id == id) it.copy(
                revealed = if (typeOnly) it.revealed else true,
                scoutedTypeOnly = if (typeOnly) true else it.scoutedTypeOnly
            ) else it
        }
        return copy(nodes = updated)
    }
    fun moveTo(id: String): TowerPath {
        val updated = nodes.map {
            when {
                it.id == currentId && it.type != NodeType.START -> it.copy(cleared = true)
                it.id == id -> it.copy(revealed = true)
                else -> it
            }
        }
        // Reveal adjacent from new position
        val adj = edges.filter { it.from == id }.map { it.to }.toSet()
        val revealedAdj = updated.map {
            if (it.id in adj) it.copy(revealed = true) else it
        }
        return copy(nodes = revealedAdj, currentId = id)
    }

    /** Mark the node the player is standing on as cleared (after resolve completes). */
    fun markCurrentCleared(): TowerPath {
        val updated = nodes.map {
            if (it.id == currentId) it.copy(cleared = true, revealed = true) else it
        }
        return copy(nodes = updated)
    }
}

object RumorPools {
    val combat = listOf(
        "Steel answers steel beyond the fog.",
        "A watchful shape; not the seal-warden yet.",
        "The stones remember a fight."
    )
    val shop = listOf(
        "A latch and a ledger — someone still sells.",
        "Coin-light, remnant-warm.",
        "Wares behind a half-drawn curtain."
    )
    val rest = listOf(
        "Quiet enough to bind a wound.",
        "A cold niche out of the wind.",
        "Ash settles; breath comes easier."
    )
    val event = listOf(
        "A choice waits where the corridor forks.",
        "Someone left a question on the wall.",
        "Not a fight — not a shop — something else."
    )
    val treasure = listOf(
        "A coffer that forgot its owner.",
        "Something small, sealed, and heavy with luck.",
        "Glint without a shopkeep."
    )
    val boss = listOf(
        "The seal hums. This is the end of the floor.",
        "A name you do not know yet, already angry.",
        "No trader, no rest — only the gate."
    )

    fun forType(type: NodeType, rng: Random): String = when (type) {
        NodeType.COMBAT -> combat.random(rng)
        NodeType.SHOP -> shop.random(rng)
        NodeType.REST -> rest.random(rng)
        NodeType.EVENT -> event.random(rng)
        NodeType.TREASURE -> treasure.random(rng)
        NodeType.BOSS -> boss.random(rng)
        NodeType.START -> "The climb begins."
    }
}

object PathGenerator {
    /**
     * One-floor slice: entry → 2–3 branches (shared depth 2 or 3) → merge → Boss.
     * Shared depth prevents empty/skipped tiers when branches previously differed.
     */
    fun generate(floor: Int = 1, rng: Random = Random.Default): TowerPath {
        val nodes = mutableListOf<PathNode>()
        val edges = mutableListOf<PathEdge>()
        val start = PathNode("start", NodeType.START, 0, 1, "The climb begins.", revealed = true)
        nodes += start

        val branchCount = rng.nextInt(2, 4)
        val depth = rng.nextInt(2, 4) // ONE shared depth for all branches (2 or 3)
        val row1Pool = listOf(NodeType.COMBAT, NodeType.EVENT, NodeType.TREASURE, NodeType.SHOP)
        val laterPool = listOf(
            NodeType.COMBAT, NodeType.SHOP, NodeType.REST, NodeType.EVENT, NodeType.TREASURE, NodeType.COMBAT
        )

        val branchEnds = mutableListOf<String>()
        for (b in 0 until branchCount) {
            var prev = start.id
            for (d in 1..depth) {
                val id = "b${b}_r$d"
                // No Rest on run start: row 1 (direct from Start) cannot be REST
                val type = if (d == 1) {
                    row1Pool[(b + rng.nextInt(row1Pool.size)) % row1Pool.size]
                } else {
                    laterPool[(b * 3 + d + rng.nextInt(3)) % laterPool.size]
                }
                val node = PathNode(
                    id = id, type = type, row = d, col = b,
                    rumor = RumorPools.forType(type, rng),
                    revealed = d == 1
                )
                nodes += node
                edges += PathEdge(prev, id)
                prev = id
            }
            branchEnds += prev
        }

        val mergeId = "merge"
        val mergeType = listOf(NodeType.COMBAT, NodeType.REST, NodeType.EVENT).random(rng)
        nodes += PathNode(
            mergeId, mergeType, row = depth + 1, col = 1,
            rumor = RumorPools.forType(mergeType, rng)
        )
        branchEnds.forEach { edges += PathEdge(it, mergeId) }

        val boss = PathNode(
            "boss", NodeType.BOSS, row = depth + 2, col = 1,
            rumor = RumorPools.forType(NodeType.BOSS, rng)
        )
        nodes += boss
        edges += PathEdge(mergeId, boss.id)

        return TowerPath(floor, nodes, edges, start.id)
    }
}
