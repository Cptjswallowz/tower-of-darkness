package com.towerofdarkness.app

import com.towerofdarkness.app.data.MidRunAshbrand
import com.towerofdarkness.app.data.MidRunFloorLoadout
import com.towerofdarkness.app.data.MidRunLoadout
import com.towerofdarkness.app.data.MidRunResume
import com.towerofdarkness.app.data.MidRunSlot
import com.towerofdarkness.app.domain.climb.ClimbKept
import com.towerofdarkness.app.domain.climb.ClimbKeptFlags
import com.towerofdarkness.app.domain.climb.FloorRumors
import com.towerofdarkness.app.domain.hub.HubOffers
import com.towerofdarkness.app.domain.path.PathGenerator
import com.towerofdarkness.app.nav.NavState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * v0.1.54-trollkept — F3 floor rumors + Kept remnants + trophies.
 * See docs/trollkept-v0154.md.
 */
class TrollkeptV0154Test {

    @Test
    fun floorRumors_generate_exactlyTwo_slot0Troll_slot1Path_noWallet() {
        assertEquals(3, FloorRumors.TROLL_POOL.size)
        assertEquals(3, FloorRumors.PATH_POOL.size)
        val seenTroll = mutableSetOf<String>()
        val seenPath = mutableSetOf<String>()
        for (seed in 0..200) {
            val pair = FloorRumors.generate(Random(seed.toLong()))
            assertEquals(2, pair.size)
            assertTrue(pair[0] in FloorRumors.TROLL_POOL)
            assertTrue(pair[1] in FloorRumors.PATH_POOL)
            assertFalse(pair[0] in FloorRumors.PATH_POOL)
            assertFalse(pair[1] in FloorRumors.TROLL_POOL)
            seenTroll += pair[0]
            seenPath += pair[1]
        }
        assertEquals(FloorRumors.TROLL_POOL.toSet(), seenTroll)
        assertEquals(FloorRumors.PATH_POOL.toSet(), seenPath)
        assertTrue(FloorRumors.isValidPair(listOf(FloorRumors.TROLL_POOL[0], FloorRumors.PATH_POOL[0])))
        assertFalse(FloorRumors.isValidPair(emptyList()))
        assertFalse(FloorRumors.isValidPair(listOf("only-one")))
    }

    @Test
    fun f3Explainer_exactString() {
        assertEquals(
            "From this floor on, read the rumors, pick five + weapon, then lock until the next stair.",
            FloorRumors.F3_EXPLAINER
        )
        // Wired into PathScreen
        val src = java.io.File("app/src/main/java/com/towerofdarkness/app/ui/screens/PathScreen.kt")
            .takeIf { it.isFile }
            ?: java.io.File("src/main/java/com/towerofdarkness/app/ui/screens/PathScreen.kt")
        assertTrue(src.isFile)
        val text = src.readText()
        assertTrue(text.contains("FloorRumors.F3_EXPLAINER"))
        assertFalse(text.contains("On Floor 3+, pick 5 skills + weapon once."))
    }

    @Test
    fun midRunSlot_floorRumors_andClimbKept_roundTrip() {
        val path = PathGenerator.generate(3, Random(54L))
        val rumors = FloorRumors.generate(Random(54L))
        val flags = ClimbKeptFlags(
            f1CombatWon = true,
            floor2Entered = true,
            floor3Entered = true,
            caveTrollKilled = true,
            gateWardenBeaten = false
        )
        val slot = MidRunSlot(
            runId = "run-tk",
            rngSeed = 54L,
            floor = 3,
            pendingStairContinue = false,
            path = MidRunSlot.fromPath(path, "floor3_54"),
            playerHp = 20,
            playerMaxHp = 30,
            runWallet = 9,
            runRemnantsEarned = 20,
            nodesCleared = 5,
            loadout = MidRunLoadout(true, listOf("strike", "guard", "spark", "cleave", "brace")),
            ashbrand = MidRunAshbrand("ashbrand", 2, 0),
            freeScoutCharges = 1,
            rumorRerolls = 2,
            combatSpeed2x = false,
            unlocksThisRun = emptyList(),
            shopVisits = emptyList(),
            resume = MidRunResume.Path,
            seenF3Explainer = true,
            floorLoadout = MidRunFloorLoadout(
                floor = 3, locked = true,
                cardIds = listOf("strike", "guard", "spark", "cleave", "brace"),
                weaponId = "ashbrand"
            ),
            floorRumors = rumors,
            climbKept = flags
        )
        val decoded = MidRunSlot.decode(MidRunSlot.encode(slot))!!
        assertEquals(rumors, decoded.floorRumors)
        assertEquals(flags, decoded.climbKept)
        assertTrue(decoded.seenF3Explainer)
        assertEquals(NavState.Path, MidRunSlot.resumeNav(decoded))

        // Missing climb_kept keys → false; floor>=3 migrates entered
        val jsonNoKept = MidRunSlot.encode(slot.copy(climbKept = ClimbKeptFlags(), floorRumors = emptyList()))
            .replace(""","climb_kept":\{[^}]*\}""".toRegex(), "")
            .replace(""","floor_rumors":\[[^\]]*\]""".toRegex(), "")
        val legacy = MidRunSlot.decode(jsonNoKept)!!
        assertEquals(emptyList<String>(), legacy.floorRumors)
        assertTrue("floor3 migrate", legacy.climbKept.floor3Entered)
        assertTrue("floor2 migrate", legacy.climbKept.floor2Entered)
        assertFalse(legacy.climbKept.f1CombatWon)
        assertFalse(legacy.climbKept.caveTrollKilled)
        assertFalse(legacy.climbKept.gateWardenBeaten)
    }

    @Test
    fun keptFormula_cases_winOrDeath_noWalletBank() {
        // Death on F1 with one combat win, no tithe
        val f1 = ClimbKept.finishPayout(
            ClimbKeptFlags(f1CombatWon = true),
            emptySet()
        )
        assertEquals(1, f1.kept)
        assertEquals(listOf("Floor 1 combat"), f1.lines.map { it.label })
        assertEquals(emptyList<String>(), f1.newTrophyIds)

        // Reach F2 only
        val f2 = ClimbKept.finishPayout(
            ClimbKeptFlags(f1CombatWon = true, floor2Entered = true),
            emptySet()
        )
        assertEquals(3, f2.kept) // 1+2
        assertEquals(listOf(ClimbKept.ID_SOOT_RIM), f2.newTrophyIds)
        assertEquals(listOf(ClimbKept.NAME_SOOT_RIM), f2.newTrophyNames)

        // Reach F3 + troll + gate + tithe = max 15
        val maxFlags = ClimbKeptFlags(
            f1CombatWon = true,
            floor2Entered = true,
            floor3Entered = true,
            caveTrollKilled = true,
            gateWardenBeaten = true
        )
        val max = ClimbKept.finishPayout(maxFlags, setOf(HubOffers.ID_ASH_TITHE))
        assertEquals(15, max.kept)
        assertEquals(
            listOf("Floor 1 combat", "Floor 2", "Floor 3", "Cave Troll", "Gate-Warden", "Ash Tithe"),
            max.lines.map { it.label }
        )
        assertEquals(
            listOf(
                ClimbKept.ID_SOOT_RIM,
                ClimbKept.ID_ASH_PAULDRON,
                ClimbKept.ID_TROLL_TOOTH,
                ClimbKept.ID_GATE_SIGIL
            ),
            max.newTrophyIds
        )

        // Death on F3 without gate: no +5
        val deathF3 = ClimbKept.finishPayout(
            maxFlags.copy(gateWardenBeaten = false),
            setOf(HubOffers.ID_ASH_TITHE)
        )
        assertEquals(10, deathF3.kept) // 1+2+3+1+3
        assertFalse(ClimbKept.ID_GATE_SIGIL in deathF3.newTrophyIds)

        // Wallet leftover is irrelevant — formula ignores it
        assertEquals(
            ClimbKept.headline(15),
            "Kept: 15 remnants"
        )
    }

    @Test
    fun trophyIdempotent_alreadyOwnedNotReAwarded() {
        val flags = ClimbKeptFlags(
            floor2Entered = true,
            floor3Entered = true,
            caveTrollKilled = true,
            gateWardenBeaten = true
        )
        val owned = setOf(
            ClimbKept.ID_SOOT_RIM,
            ClimbKept.ID_ASH_PAULDRON,
            ClimbKept.ID_TROLL_TOOTH,
            ClimbKept.ID_GATE_SIGIL
        )
        val again = ClimbKept.finishPayout(flags, owned)
        assertTrue(again.newTrophyIds.isEmpty())
        assertTrue(again.newTrophyNames.isEmpty())
        // Still pays Kept even when trophies owned
        assertEquals(11, again.kept) // 2+3+1+5 (no f1, no tithe)

        val partial = ClimbKept.finishPayout(flags, setOf(ClimbKept.ID_SOOT_RIM))
        assertEquals(
            listOf(
                ClimbKept.ID_ASH_PAULDRON,
                ClimbKept.ID_TROLL_TOOTH,
                ClimbKept.ID_GATE_SIGIL
            ),
            partial.newTrophyIds
        )
        assertEquals(
            listOf(
                ClimbKept.NAME_ASH_PAULDRON,
                ClimbKept.NAME_TROLL_TOOTH,
                ClimbKept.NAME_GATE_SIGIL
            ),
            partial.newTrophyNames
        )
    }

    @Test
    fun overlayStackOrder_andMd5MatchArtManifest() {
        assertEquals(
            listOf("overlay_soot_rim", "overlay_ash_pauldron", "overlay_troll_tooth", "overlay_gate_sigil"),
            ClimbKept.OVERLAY_STACK.map { it.second }
        )
        val all = setOf(
            ClimbKept.ID_GATE_SIGIL,
            ClimbKept.ID_SOOT_RIM,
            ClimbKept.ID_TROLL_TOOTH,
            ClimbKept.ID_ASH_PAULDRON
        )
        assertEquals(
            listOf("overlay_soot_rim", "overlay_ash_pauldron", "overlay_troll_tooth", "overlay_gate_sigil"),
            ClimbKept.unlockedOverlayDrawables(all)
        )
        assertEquals(emptyList<String>(), ClimbKept.unlockedOverlayDrawables(emptySet()))
        assertTrue(ClimbKept.anyTrophyOwned(setOf(ClimbKept.ID_SOOT_RIM)))
        assertFalse(ClimbKept.anyTrophyOwned(emptySet()))

        // v0.1.55-hubsplit overlay art — authoritative md5s in docs/art-audio/HUBSPLIT_OVERLAYS_v0.1.55.md
        val expected = mapOf(
            "overlay_soot_rim" to "6d9431fac60ace69e7d1cefd06055655",
            "overlay_ash_pauldron" to "bd1bf237f3174b870ecd573a70e6830d",
            "overlay_troll_tooth" to "ef34f2f6d31975d2e674fec65d5e0e4c",
            "overlay_gate_sigil" to "3d0add7b710e6d8cf7d7677e5f4f3976"
        )
        for ((name, md5) in expected) {
            val f = listOf(
                java.io.File("app/src/main/res/drawable/$name.png"),
                java.io.File("src/main/res/drawable/$name.png")
            ).firstOrNull { it.isFile }
            assertNotNull(name, f)
            val actual = java.security.MessageDigest.getInstance("MD5")
                .digest(f!!.readBytes())
                .joinToString("") { "%02x".format(it) }
            assertEquals(name, md5, actual)
        }
    }

    @Test
    fun climbKeptMigrate_fromFloor() {
        val bare = ClimbKeptFlags()
        val m2 = ClimbKeptFlags.migrate(bare, floor = 2)
        assertTrue(m2.floor2Entered)
        assertFalse(m2.floor3Entered)
        val m3 = ClimbKeptFlags.migrate(bare, floor = 3)
        assertTrue(m3.floor2Entered)
        assertTrue(m3.floor3Entered)
        // Explicit false cave/f1/gate stay false
        assertFalse(m3.f1CombatWon)
        assertFalse(m3.caveTrollKilled)
        assertFalse(m3.gateWardenBeaten)
    }
}
