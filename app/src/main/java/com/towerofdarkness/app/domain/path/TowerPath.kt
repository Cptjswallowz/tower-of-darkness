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
    /** Fixed one-floor slice: entry → 2–3 branches → merge → Boss. */
    fun generate(floor: Int = 1, rng: Random = Random.Default): TowerPath {
        val nodes = mutableListOf<PathNode>()
        val edges = mutableListOf<PathEdge>()
        val start = PathNode("start", NodeType.START, 0, 1, "The climb begins.", revealed = true)
        nodes += start

        val branchCount = rng.nextInt(2, 4)
        val midTypes = mutableListOf(
            NodeType.COMBAT, NodeType.SHOP, NodeType.REST, NodeType.EVENT, NodeType.TREASURE, NodeType.COMBAT
        ).shuffled(rng)

        val branchEnds = mutableListOf<String>()
        for (b in 0 until branchCount) {
            val depth = rng.nextInt(2, 4)
            var prev = start.id
            for (d in 1..depth) {
                val id = "b${b}_r$d"
                val type = midTypes[(b * 3 + d) % midTypes.size]
                val node = PathNode(
                    id = id, type = type, row = d, col = b,
                    rumor = RumorPools.forType(type, rng),
                    revealed = d == 1 // adjacent to start
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
            mergeId, mergeType, 4, 1,
            RumorPools.forType(mergeType, rng)
        )
        branchEnds.forEach { edges += PathEdge(it, mergeId) }

        val boss = PathNode(
            "boss", NodeType.BOSS, 5, 1,
            RumorPools.forType(NodeType.BOSS, rng)
        )
        nodes += boss
        edges += PathEdge(mergeId, boss.id)

        return TowerPath(floor, nodes, edges, start.id)
    }
}
