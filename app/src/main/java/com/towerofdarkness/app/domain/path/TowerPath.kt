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

        return enforceFloorRules(TowerPath(floor, nodes, edges, start.id), rng)
    }

    /**
     * v0.1.5-path + v0.1.7-routefight + v0.1.10-routecare locks:
     * - ≥1 COMBAT node before boss (mid + merge) on the floor
     * - every Start→Boss path contains ≥1 COMBAT
     * - every Start→Boss path contains ≥1 CARE (REST or SHOP)
     * - at most 1 EVENT per floor
     */
    internal fun enforceFloorRules(path: TowerPath, rng: Random): TowerPath {
        var nodes = path.nodes.toMutableList()
        fun idx(id: String) = nodes.indexOfFirst { it.id == id }
        fun replaceType(at: Int, type: NodeType) {
            val n = nodes[at]
            nodes[at] = n.copy(type = type, rumor = RumorPools.forType(type, rng))
        }

        val midIds = nodes.filter {
            it.type != NodeType.START && it.type != NodeType.BOSS
        }.map { it.id }

        // Cap events at 1 — convert extras to non-event
        val eventIdxs = midIds.map { idx(it) }.filter { nodes[it].type == NodeType.EVENT }
        if (eventIdxs.size > 1) {
            val keep = eventIdxs.first()
            for (i in eventIdxs.drop(1)) {
                val row = nodes[i].row
                val replacements = if (row == 1) {
                    listOf(NodeType.COMBAT, NodeType.SHOP, NodeType.TREASURE) // no REST on row 1
                } else {
                    listOf(NodeType.COMBAT, NodeType.SHOP, NodeType.TREASURE, NodeType.REST)
                }
                replaceType(i, replacements.random(rng))
            }
            if (nodes[keep].type != NodeType.EVENT) replaceType(keep, NodeType.EVENT)
        }

        // Ensure ≥1 COMBAT before boss (floor-wide)
        val hasCombat = midIds.any { nodes[idx(it)].type == NodeType.COMBAT }
        if (!hasCombat) {
            // Prefer merge; else first mid node. Combat requirement wins over keeping an Event.
            val mergeI = idx("merge")
            val candidate = if (mergeI >= 0) mergeI else midIds.map { idx(it) }.first()
            replaceType(candidate, NodeType.COMBAT)
        }

        // v0.1.7-routefight: every Start→Boss route must include ≥1 COMBAT
        // (floor-wide combat can sit on an unused branch while the player path is fightless).
        ensureCombatOnEveryStartToBossPath(nodes, path.edges, rng)

        // v0.1.10-routecare: every Start→Boss route must include ≥1 CARE (REST|SHOP).
        // Runs after combat lock; converts last eligible non-COMBAT → REST (prefer heal).
        ensureCareOnEveryStartToBossPath(nodes, path.edges, rng)

        // CARE may leave a route fightless if a shared COMBAT was never the conversion
        // target but an earlier pass interacted oddly — repair combat without stripping
        // a path's only CARE node.
        ensureCombatPreservingCare(nodes, path.edges, rng)

        return path.copy(nodes = nodes)
    }

    /**
     * If a Start→Boss path has 0 COMBAT, convert its last non-boss node to COMBAT
     * (prefer merge when present on that path). Event cap remains ≤1 (conversion only
     * removes events).
     */
    private fun ensureCombatOnEveryStartToBossPath(
        nodes: MutableList<PathNode>,
        edges: List<PathEdge>,
        rng: Random
    ) {
        fun idx(id: String) = nodes.indexOfFirst { it.id == id }
        fun replaceType(at: Int, type: NodeType) {
            val n = nodes[at]
            nodes[at] = n.copy(type = type, rumor = RumorPools.forType(type, rng))
        }

        val outs = edges.groupBy({ it.from }, { it.to })
        val routes = mutableListOf<List<String>>()
        fun dfs(id: String, acc: MutableList<String>) {
            acc += id
            if (nodes[idx(id)].type == NodeType.BOSS || id == "boss") {
                routes += acc.toList()
            } else {
                for (next in outs[id].orEmpty()) dfs(next, acc)
            }
            acc.removeAt(acc.lastIndex)
        }
        val startId = nodes.firstOrNull { it.type == NodeType.START }?.id ?: return
        dfs(startId, mutableListOf())

        for (route in routes) {
            val midOnPath = route.filter { id ->
                val t = nodes[idx(id)].type
                t != NodeType.START && t != NodeType.BOSS
            }
            if (midOnPath.any { nodes[idx(it)].type == NodeType.COMBAT }) continue

            val candidateId = when {
                "merge" in midOnPath -> "merge"
                midOnPath.isNotEmpty() -> midOnPath.last()
                else -> continue
            }
            replaceType(idx(candidateId), NodeType.COMBAT)
        }
    }

    /**
     * If a Start→Boss path has 0 CARE (REST|SHOP), convert its last non-boss,
     * non-COMBAT node to REST. Prefer REST over SHOP (heal is the point).
     * Do not convert the only COMBAT on a path; if all mid nodes are COMBAT, convert
     * a non-sole COMBAT → REST only when no S→B route would become fightless.
     * Do not add nodes. Event cap remains ≤1.
     */
    private fun ensureCareOnEveryStartToBossPath(
        nodes: MutableList<PathNode>,
        edges: List<PathEdge>,
        rng: Random
    ) {
        fun idx(id: String) = nodes.indexOfFirst { it.id == id }
        fun replaceType(at: Int, type: NodeType) {
            val n = nodes[at]
            nodes[at] = n.copy(type = type, rumor = RumorPools.forType(type, rng))
        }
        fun isCare(t: NodeType) = t == NodeType.REST || t == NodeType.SHOP

        val outs = edges.groupBy({ it.from }, { it.to })
        val routes = mutableListOf<List<String>>()
        fun dfs(id: String, acc: MutableList<String>) {
            acc += id
            if (nodes[idx(id)].type == NodeType.BOSS || id == "boss") {
                routes += acc.toList()
            } else {
                for (next in outs[id].orEmpty()) dfs(next, acc)
            }
            acc.removeAt(acc.lastIndex)
        }
        val startId = nodes.firstOrNull { it.type == NodeType.START }?.id ?: return
        dfs(startId, mutableListOf())

        for (route in routes) {
            val midOnPath = route.filter { id ->
                val t = nodes[idx(id)].type
                t != NodeType.START && t != NodeType.BOSS
            }
            if (midOnPath.any { isCare(nodes[idx(it)].type) }) continue

            // Prefer last non-boss, non-COMBAT → REST (WO).
            val eligible = midOnPath.filter { nodes[idx(it)].type != NodeType.COMBAT }
            if (eligible.isNotEmpty()) {
                replaceType(idx(eligible.last()), NodeType.REST)
                continue
            }

            // All mid nodes are COMBAT: convert a COMBAT → REST only if this path has
            // ≥2 COMBAT and no Start→Boss route would become fightless (shared merge).
            val combatIds = midOnPath.filter { nodes[idx(it)].type == NodeType.COMBAT }
            if (combatIds.size < 2) continue
            for (candidate in combatIds.asReversed()) {
                val breaksCombat = routes.any { r ->
                    r.count { id ->
                        val t = if (id == candidate) NodeType.REST else nodes[idx(id)].type
                        t == NodeType.COMBAT
                    } == 0
                }
                if (!breaksCombat) {
                    replaceType(idx(candidate), NodeType.REST)
                    break
                }
            }
        }
    }

    /**
     * Post-CARE combat repair: if a Start→Boss path somehow has 0 COMBAT, convert a
     * non-CARE mid node to COMBAT (prefer merge when non-CARE). Never strip a path's
     * only CARE. If every mid node is CARE and there are ≥2, convert the last CARE.
     */
    private fun ensureCombatPreservingCare(
        nodes: MutableList<PathNode>,
        edges: List<PathEdge>,
        rng: Random
    ) {
        fun idx(id: String) = nodes.indexOfFirst { it.id == id }
        fun replaceType(at: Int, type: NodeType) {
            val n = nodes[at]
            nodes[at] = n.copy(type = type, rumor = RumorPools.forType(type, rng))
        }
        fun isCare(t: NodeType) = t == NodeType.REST || t == NodeType.SHOP

        val outs = edges.groupBy({ it.from }, { it.to })
        val routes = mutableListOf<List<String>>()
        fun dfs(id: String, acc: MutableList<String>) {
            acc += id
            if (nodes[idx(id)].type == NodeType.BOSS || id == "boss") {
                routes += acc.toList()
            } else {
                for (next in outs[id].orEmpty()) dfs(next, acc)
            }
            acc.removeAt(acc.lastIndex)
        }
        val startId = nodes.firstOrNull { it.type == NodeType.START }?.id ?: return
        dfs(startId, mutableListOf())

        for (route in routes) {
            val midOnPath = route.filter { id ->
                val t = nodes[idx(id)].type
                t != NodeType.START && t != NodeType.BOSS
            }
            if (midOnPath.any { nodes[idx(it)].type == NodeType.COMBAT }) continue

            val nonCare = midOnPath.filter { !isCare(nodes[idx(it)].type) }
            val candidateId = when {
                nonCare.isNotEmpty() -> {
                    if ("merge" in nonCare) "merge" else nonCare.last()
                }
                midOnPath.count { isCare(nodes[idx(it)].type) } >= 2 ->
                    midOnPath.last { isCare(nodes[idx(it)].type) }
                else -> continue
            }
            replaceType(idx(candidateId), NodeType.COMBAT)
        }
    }
}
