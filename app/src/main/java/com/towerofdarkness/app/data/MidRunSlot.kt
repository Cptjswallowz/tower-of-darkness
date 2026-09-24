package com.towerofdarkness.app.data

import com.towerofdarkness.app.domain.path.NodeType
import com.towerofdarkness.app.domain.path.PathEdge
import com.towerofdarkness.app.domain.path.PathNode
import com.towerofdarkness.app.domain.path.TowerPath
import com.towerofdarkness.app.nav.NavState

/** Resume target after cold start / Menu Continue. Never mid-combat. */
enum class MidRunResume {
    Path,
    FloorBreak
}

data class MidRunAshbrand(
    val weaponId: String,
    val level: Int,
    val charge: Int
)

data class MidRunLoadout(
    val locked: Boolean,
    val cardIds: List<String>
)

data class MidRunShopVisit(
    val nodeId: String,
    val floor: Int,
    val soldOfferIds: List<String>
)

data class MidRunPathSnap(
    val floor: Int,
    val layoutId: String,
    val revealed: List<String>,
    val scoutedTypeOnly: List<String>,
    val cleared: List<String>,
    val cursor: String?,
    val rumorTextByNode: Map<String, String>,
    val nodes: List<PathNodeSnap>,
    val edges: List<PathEdgeSnap>
)

data class PathNodeSnap(
    val id: String,
    val type: String,
    val row: Int,
    val col: Int,
    val rumor: String,
    val revealed: Boolean,
    val scoutedTypeOnly: Boolean,
    val cleared: Boolean,
    val packKind: String? = null,
    val packLook: String? = null
)

data class PathEdgeSnap(val from: String, val to: String)

/**
 * One mid-run slot (`midrun_v0112`). See docs/midrun-save-v0112.md.
 * Pure data + JSON codec — unit-testable without Android.
 */
data class MidRunSlot(
    val schema: String = SCHEMA,
    val runId: String,
    val rngSeed: Long,
    val floor: Int,
    val pendingStairContinue: Boolean,
    val path: MidRunPathSnap,
    val playerHp: Int,
    val playerMaxHp: Int,
    val runWallet: Int,
    val runRemnantsEarned: Int,
    val nodesCleared: Int,
    val loadout: MidRunLoadout,
    val ashbrand: MidRunAshbrand,
    val freeScoutCharges: Int,
    val rumorRerolls: Int,
    val combatSpeed2x: Boolean,
    val unlocksThisRun: List<String>,
    val shopVisits: List<MidRunShopVisit>,
    val resume: MidRunResume
) {
    companion object {
        const val SCHEMA = "v0.1.12"

        fun floorSeed(runSeed: Long, floor: Int): Long =
            runSeed xor (floor.toLong() * -7046029254386353131L) // 0x9E3779B97F4A7C15

        fun fromPath(path: TowerPath, layoutId: String): MidRunPathSnap {
            return MidRunPathSnap(
                floor = path.floor,
                layoutId = layoutId,
                revealed = path.nodes.filter { it.revealed }.map { it.id },
                scoutedTypeOnly = path.nodes.filter { it.scoutedTypeOnly }.map { it.id },
                cleared = path.nodes.filter { it.cleared }.map { it.id },
                cursor = path.currentId,
                rumorTextByNode = path.nodes.associate { it.id to it.rumor },
                nodes = path.nodes.map {
                    PathNodeSnap(
                        id = it.id,
                        type = it.type.name,
                        row = it.row,
                        col = it.col,
                        rumor = it.rumor,
                        revealed = it.revealed,
                        scoutedTypeOnly = it.scoutedTypeOnly,
                        cleared = it.cleared,
                        packKind = it.packKind,
                        packLook = it.packLook
                    )
                },
                edges = path.edges.map { PathEdgeSnap(it.from, it.to) }
            )
        }

        fun toTowerPath(snap: MidRunPathSnap): TowerPath {
            val nodes = snap.nodes.map {
                PathNode(
                    id = it.id,
                    type = NodeType.valueOf(it.type),
                    row = it.row,
                    col = it.col,
                    rumor = it.rumor,
                    revealed = it.revealed,
                    scoutedTypeOnly = it.scoutedTypeOnly,
                    cleared = it.cleared,
                    packKind = it.packKind,
                    packLook = it.packLook
                )
            }
            val edges = snap.edges.map { PathEdge(it.from, it.to) }
            val cursor = snap.cursor
                ?: nodes.firstOrNull { it.type == NodeType.START }?.id
                ?: nodes.first().id
            return TowerPath(snap.floor, nodes, edges, cursor)
        }

        fun resumeNav(slot: MidRunSlot): NavState =
            if (slot.pendingStairContinue || slot.resume == MidRunResume.FloorBreak) {
                NavState.FloorBreak
            } else {
                NavState.Path
            }

        fun isActiveClimb(slot: MidRunSlot?): Boolean = slot != null

        fun encode(slot: MidRunSlot): String {
            val sb = StringBuilder()
            sb.append('{')
            fun raw(k: String, v: String) {
                if (sb.length > 1) sb.append(',')
                sb.append('"').append(k).append('"').append(':').append(v)
            }
            fun str(k: String, v: String) = raw(k, jsonString(v))
            fun num(k: String, v: Long) = raw(k, v.toString())
            fun num(k: String, v: Int) = raw(k, v.toString())
            fun bool(k: String, v: Boolean) = raw(k, if (v) "true" else "false")

            str("schema", slot.schema)
            str("run_id", slot.runId)
            num("rng_seed", slot.rngSeed)
            num("floor", slot.floor)
            bool("pending_stair_continue", slot.pendingStairContinue)
            raw("path", encodePath(slot.path))
            num("player_hp", slot.playerHp)
            num("player_max_hp", slot.playerMaxHp)
            num("run_wallet", slot.runWallet)
            num("run_remnants_earned", slot.runRemnantsEarned)
            num("nodes_cleared", slot.nodesCleared)
            raw(
                "loadout",
                "{" +
                    "\"locked\":${slot.loadout.locked}," +
                    "\"card_ids\":${encodeStringList(slot.loadout.cardIds)}" +
                    "}"
            )
            raw(
                "ashbrand",
                "{" +
                    "\"weapon_id\":${jsonString(slot.ashbrand.weaponId)}," +
                    "\"level\":${slot.ashbrand.level}," +
                    "\"charge\":${slot.ashbrand.charge}" +
                    "}"
            )
            num("free_scout_charges", slot.freeScoutCharges)
            num("rumor_rerolls", slot.rumorRerolls)
            bool("combat_speed_2x", slot.combatSpeed2x)
            raw("unlocks_this_run", encodeStringList(slot.unlocksThisRun))
            raw("shop_visits", encodeShopVisits(slot.shopVisits))
            str("resume", slot.resume.name)
            sb.append('}')
            return sb.toString()
        }

        fun decode(json: String): MidRunSlot? {
            return try {
                val o = JsonObj.parse(json) ?: return null
                val schema = o.str("schema") ?: return null
                if (schema != SCHEMA) return null
                val rngSeed = o.long("rng_seed") ?: return null
                val runId = o.str("run_id") ?: return null
                val floor = o.int("floor") ?: return null
                val pending = o.bool("pending_stair_continue") ?: false
                val pathObj = o.obj("path") ?: return null
                val path = decodePath(pathObj) ?: return null
                val loadoutObj = o.obj("loadout") ?: return null
                val ashObj = o.obj("ashbrand") ?: return null
                val resumeStr = o.str("resume") ?: "Path"
                val resume = when (resumeStr) {
                    "FloorBreak" -> MidRunResume.FloorBreak
                    else -> MidRunResume.Path
                }
                MidRunSlot(
                    schema = schema,
                    runId = runId,
                    rngSeed = rngSeed,
                    floor = floor,
                    pendingStairContinue = pending,
                    path = path,
                    playerHp = o.int("player_hp") ?: return null,
                    playerMaxHp = o.int("player_max_hp") ?: return null,
                    runWallet = o.int("run_wallet") ?: 0,
                    runRemnantsEarned = o.int("run_remnants_earned") ?: 0,
                    nodesCleared = o.int("nodes_cleared") ?: 0,
                    loadout = MidRunLoadout(
                        locked = loadoutObj.bool("locked") ?: false,
                        cardIds = loadoutObj.strList("card_ids")
                    ),
                    ashbrand = MidRunAshbrand(
                        weaponId = ashObj.str("weapon_id") ?: "ashbrand",
                        level = ashObj.int("level") ?: 1,
                        charge = ashObj.int("charge") ?: 0
                    ),
                    freeScoutCharges = o.int("free_scout_charges") ?: 0,
                    rumorRerolls = o.int("rumor_rerolls") ?: 0,
                    combatSpeed2x = o.bool("combat_speed_2x") ?: false,
                    unlocksThisRun = o.strList("unlocks_this_run"),
                    shopVisits = decodeShopVisits(o.arr("shop_visits")),
                    resume = resume
                )
            } catch (_: Exception) {
                null
            }
        }

        private fun encodePath(p: MidRunPathSnap): String {
            val nodesJson = p.nodes.joinToString(",") { n ->
                buildString {
                    append("{")
                    append("\"id\":").append(jsonString(n.id)).append(",")
                    append("\"type\":").append(jsonString(n.type)).append(",")
                    append("\"row\":").append(n.row).append(",")
                    append("\"col\":").append(n.col).append(",")
                    append("\"rumor\":").append(jsonString(n.rumor)).append(",")
                    append("\"revealed\":").append(n.revealed).append(",")
                    append("\"scouted_type_only\":").append(n.scoutedTypeOnly).append(",")
                    append("\"cleared\":").append(n.cleared)
                    n.packKind?.let { append(",\"pack_kind\":").append(jsonString(it)) }
                    n.packLook?.let { append(",\"pack_look\":").append(jsonString(it)) }
                    append("}")
                }
            }
            val edgesJson = p.edges.joinToString(",") { e ->
                "{\"from\":${jsonString(e.from)},\"to\":${jsonString(e.to)}}"
            }
            val cursorJson = if (p.cursor == null) "null" else jsonString(p.cursor)
            return "{" +
                "\"floor\":${p.floor}," +
                "\"layout_id\":${jsonString(p.layoutId)}," +
                "\"revealed\":${encodeStringList(p.revealed)}," +
                "\"scouted_type_only\":${encodeStringList(p.scoutedTypeOnly)}," +
                "\"cleared\":${encodeStringList(p.cleared)}," +
                "\"cursor\":$cursorJson," +
                "\"rumor_text_by_node\":${encodeStringMap(p.rumorTextByNode)}," +
                "\"nodes\":[$nodesJson]," +
                "\"edges\":[$edgesJson]" +
                "}"
        }

        private fun decodePath(o: JsonObj): MidRunPathSnap? {
            val nodesArr = o.arr("nodes") ?: return null
            val nodes = nodesArr.mapNotNull { el ->
                val n = el as? JsonObj ?: return@mapNotNull null
                PathNodeSnap(
                    id = n.str("id") ?: return@mapNotNull null,
                    type = n.str("type") ?: return@mapNotNull null,
                    row = n.int("row") ?: 0,
                    col = n.int("col") ?: 0,
                    rumor = n.str("rumor") ?: "",
                    revealed = n.bool("revealed") ?: false,
                    scoutedTypeOnly = n.bool("scouted_type_only") ?: false,
                    cleared = n.bool("cleared") ?: false,
                    packKind = n.str("pack_kind"),
                    packLook = n.str("pack_look")
                )
            }
            if (nodes.isEmpty()) return null
            val edgesArr = o.arr("edges") ?: emptyList()
            val edges = edgesArr.mapNotNull { el ->
                val e = el as? JsonObj ?: return@mapNotNull null
                PathEdgeSnap(
                    from = e.str("from") ?: return@mapNotNull null,
                    to = e.str("to") ?: return@mapNotNull null
                )
            }
            return MidRunPathSnap(
                floor = o.int("floor") ?: 1,
                layoutId = o.str("layout_id") ?: "",
                revealed = o.strList("revealed"),
                scoutedTypeOnly = o.strList("scouted_type_only"),
                cleared = o.strList("cleared"),
                cursor = o.str("cursor"),
                rumorTextByNode = o.strMap("rumor_text_by_node"),
                nodes = nodes,
                edges = edges
            )
        }

        private fun encodeShopVisits(list: List<MidRunShopVisit>): String =
            list.joinToString(",", "[", "]") { v ->
                "{" +
                    "\"node_id\":${jsonString(v.nodeId)}," +
                    "\"floor\":${v.floor}," +
                    "\"sold_offer_ids\":${encodeStringList(v.soldOfferIds)}" +
                    "}"
            }

        private fun decodeShopVisits(arr: List<Any?>?): List<MidRunShopVisit> {
            if (arr == null) return emptyList()
            return arr.mapNotNull { el ->
                val o = el as? JsonObj ?: return@mapNotNull null
                MidRunShopVisit(
                    nodeId = o.str("node_id") ?: return@mapNotNull null,
                    floor = o.int("floor") ?: 1,
                    soldOfferIds = o.strList("sold_offer_ids")
                )
            }
        }

        private fun encodeStringList(list: List<String>): String =
            list.joinToString(",", "[", "]") { jsonString(it) }

        private fun encodeStringMap(map: Map<String, String>): String =
            map.entries.joinToString(",", "{", "}") { (k, v) ->
                "${jsonString(k)}:${jsonString(v)}"
            }

        fun jsonString(s: String): String {
            val escaped = s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
            return "\"$escaped\""
        }
    }
}

/** Minimal JSON object reader for mid-run slot (no Reflect / no org.json). */
internal class JsonObj(private val map: Map<String, Any?>) {
    fun str(k: String): String? = map[k] as? String
    fun int(k: String): Int? = when (val v = map[k]) {
        is Int -> v
        is Long -> v.toInt()
        is Double -> v.toInt()
        else -> null
    }
    fun long(k: String): Long? = when (val v = map[k]) {
        is Long -> v
        is Int -> v.toLong()
        is Double -> v.toLong()
        else -> null
    }
    fun bool(k: String): Boolean? = map[k] as? Boolean
    fun obj(k: String): JsonObj? = map[k] as? JsonObj
    @Suppress("UNCHECKED_CAST")
    fun arr(k: String): List<Any?>? = map[k] as? List<Any?>
    fun strList(k: String): List<String> =
        arr(k)?.mapNotNull { it as? String } ?: emptyList()
    fun strMap(k: String): Map<String, String> {
        val o = map[k] as? JsonObj ?: return emptyMap()
        return o.map.mapNotNull { (key, v) -> (v as? String)?.let { key to it } }.toMap()
    }

    companion object {
        fun parse(json: String): JsonObj? {
            val v = JsonParser(json).parseValue() ?: return null
            return v as? JsonObj
        }
    }
}

internal class JsonParser(private val s: String) {
    private var i = 0

    fun parseValue(): Any? {
        skipWs()
        if (i >= s.length) return null
        val c = s[i]
        return when (c) {
            '{' -> parseObject()
            '[' -> parseArray()
            '"' -> parseString()
            't' -> {
                expect("true")
                true
            }
            'f' -> {
                expect("false")
                false
            }
            'n' -> {
                expect("null")
                null
            }
            else -> if (c == '-' || c in '0'..'9') parseNumber() else null
        }
    }

    private fun parseObject(): JsonObj {
        expect('{')
        val map = linkedMapOf<String, Any?>()
        skipWs()
        if (peek('}')) {
            i++
            return JsonObj(map)
        }
        while (true) {
            skipWs()
            val key = parseString()
            skipWs()
            expect(':')
            val value = parseValue()
            map[key] = value
            skipWs()
            when {
                peek(',') -> i++
                peek('}') -> {
                    i++
                    break
                }
                else -> break
            }
        }
        return JsonObj(map)
    }

    private fun parseArray(): List<Any?> {
        expect('[')
        val list = mutableListOf<Any?>()
        skipWs()
        if (peek(']')) {
            i++
            return list
        }
        while (true) {
            list += parseValue()
            skipWs()
            when {
                peek(',') -> i++
                peek(']') -> {
                    i++
                    break
                }
                else -> break
            }
        }
        return list
    }

    private fun parseString(): String {
        expect('"')
        val sb = StringBuilder()
        while (i < s.length) {
            val c = s[i++]
            when (c) {
                '"' -> break
                '\\' -> {
                    if (i >= s.length) break
                    when (val e = s[i++]) {
                        '"', '\\', '/' -> sb.append(e)
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            if (i + 4 <= s.length) {
                                val hex = s.substring(i, i + 4)
                                sb.append(hex.toInt(16).toChar())
                                i += 4
                            }
                        }
                        else -> sb.append(e)
                    }
                }
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    private fun parseNumber(): Number {
        val start = i
        if (peek('-')) i++
        while (i < s.length && s[i] in '0'..'9') i++
        var isDouble = false
        if (peek('.')) {
            isDouble = true
            i++
            while (i < s.length && s[i] in '0'..'9') i++
        }
        if (i < s.length && (s[i] == 'e' || s[i] == 'E')) {
            isDouble = true
            i++
            if (peek('+') || peek('-')) i++
            while (i < s.length && s[i] in '0'..'9') i++
        }
        val raw = s.substring(start, i)
        return if (isDouble) {
            raw.toDouble()
        } else {
            val asLong = raw.toLong()
            if (asLong in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) asLong.toInt() else asLong
        }
    }

    private fun skipWs() {
        while (i < s.length && s[i].isWhitespace()) i++
    }

    private fun peek(c: Char): Boolean = i < s.length && s[i] == c

    private fun expect(c: Char) {
        skipWs()
        check(i < s.length && s[i] == c) { "expected $c at $i" }
        i++
    }

    private fun expect(lit: String) {
        skipWs()
        check(s.startsWith(lit, i)) { "expected $lit at $i" }
        i += lit.length
    }
}
