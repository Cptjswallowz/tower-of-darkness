package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.CleaveKit
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.CombatFx
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.EnemyKitRole
import com.towerofdarkness.app.domain.combat.FxRecipient
import com.towerofdarkness.app.domain.combat.FxTier
import com.towerofdarkness.app.domain.combat.WakeArt
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import com.towerofdarkness.app.domain.cards.CardCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random
import java.io.File

/**
 * v0.1.42-cleavekit — CLEAVE slash-light + hit-flash wired into existing FX kernel.
 * Aim stays v0.1.40 (recipient bust / half-stage clip). Brace / Wake unchanged.
 * See docs/cleavekit-v0142.md.
 */
class CleaveKitV0142Test {

    @Test
    fun tag_isCleaveKitV0142() {
        assertEquals("v0.1.48-strokeboth", CombatFx.TAG)
        assertEquals("v0.1.48-strokeboth", CleaveKit.TAG)
    }

    @Test
    fun onlySlashLightAndHitFlashImported_noShieldBlock() {
        val drawable = File("src/main/res/drawable")
        // Unit tests run with cwd = app/
        val dir = if (drawable.isDirectory) drawable else File("app/src/main/res/drawable")
        assertTrue("drawable dir missing: ${dir.absolutePath}", dir.isDirectory)
        val names = dir.list()?.toSet().orEmpty()
        assertTrue("fx_slash_light.png", "fx_slash_light.png" in names)
        assertTrue("fx_hit_flash.png", "fx_hit_flash.png" in names)
        assertTrue("fx_hit_flash_additive.png", "fx_hit_flash_additive.png" in names)
        assertFalse("shield-block must not be imported", "fx_shield_block.png" in names)
        assertFalse("shield-block.png", "shield-block.png" in names)
        assertEquals(CleaveKit.SHIELD_BLOCK_BANNED, "shield-block")
        // Wake art untouched
        assertTrue("wake_vfx_slash.png" in names)
        assertTrue("ashbrand_spark.png" in names)
    }

    @Test
    fun atlasMetadata_matchesArtLock() {
        assertEquals(16, CleaveKit.SLASH_FRAMES)
        assertEquals(8, CleaveKit.SLASH_COLS)
        assertEquals(2, CleaveKit.SLASH_ROWS)
        assertEquals(24, CleaveKit.SLASH_FPS)
        assertEquals(256, CleaveKit.SLASH_CELL_PX)
        assertEquals(6, CleaveKit.SLASH_PEAK_FRAME)
        assertEquals(0.62f, CleaveKit.SLASH_ANCHOR_X, 0.0001f)
        assertEquals(12, CleaveKit.HIT_FLASH_FRAMES)
        assertEquals(6, CleaveKit.HIT_FLASH_COLS)
        assertEquals(2, CleaveKit.HIT_FLASH_PEAK_FRAME)
        assertTrue(CleaveKit.slashPeakInPlayWindow())
        assertTrue(CleaveKit.hitFlashPeakInPlayWindow())
        assertTrue(CleaveKit.slashPlayWithinBudget())
        assertTrue(CombatFx.STROKE_SMALL_MS <= CleaveKit.MAX_SLASH_MS_1X)
        assertEquals(1.3f, CleaveKit.MEDIUM_SCALE, 0.001f)
        assertEquals(1.3f, CleaveKit.slashScale(FxTier.MEDIUM), 0.001f)
        assertEquals(1.0f, CleaveKit.slashScale(FxTier.SMALL), 0.001f)
    }

    @Test
    fun rowMajorCellOrigin() {
        // slash 8 cols: frame 6 → col 6 row 0
        assertEquals(6 * 256 to 0, CleaveKit.slashCellOrigin(6))
        // frame 10 → col 2 row 1
        assertEquals(2 * 256 to 256, CleaveKit.slashCellOrigin(10))
        // hit-flash 6 cols: frame 2 → col 2 row 0
        assertEquals(2 * 256 to 0, CleaveKit.hitFlashCellOrigin(2))
        // frame 7 → col 1 row 1
        assertEquals(1 * 256 to 256, CleaveKit.hitFlashCellOrigin(7))
    }

    @Test
    fun hostflintAndHit_oneBustFoeOrYou() {
        val host = CombatFx.specForPlayer("hostflint")
        assertEquals(FxTier.SMALL, host.tier)
        assertEquals(FxRecipient.FOE, host.recipient)
        assertEquals(FxRecipient.FOE, host.stroke!!.recipient)
        assertTrue(host.useCleaveHitFlash) // v0.1.43: contact hit-flash on damage cut
        assertEquals(CleaveKit.CONTACT_HIT_FLASH_MS, host.hitFlashMs)
        assertFalse(CombatFx.slashCutGeom(host.recipient, host.tier).spansBothBusts())

        val hit = CombatFx.specForEnemy("hit", EnemyKind.GOBLIN)
        assertEquals(FxTier.SMALL, hit.tier)
        assertEquals(FxRecipient.YOU, hit.recipient)
        assertEquals(FxRecipient.YOU, hit.stroke!!.recipient)
        assertFalse(CombatFx.slashCutGeom(hit.recipient, hit.tier).spansBothBusts())
    }

    @Test
    fun towerPike_heavier1_3x_foeOnly() {
        val pike = CombatFx.specForPlayer("tower_pike")
        assertEquals(FxTier.MEDIUM, pike.tier)
        assertEquals(FxRecipient.FOE, pike.recipient)
        assertEquals(CombatFx.THICK_MEDIUM, pike.stroke!!.thickness, 0.001f)
        assertEquals(1.3f, CleaveKit.slashScale(pike.tier), 0.001f)
        assertFalse(CombatFx.slashCutGeom(pike.recipient, pike.tier).spansBothBusts())
        // Half-stage clip still FOE
        val clip = CombatFx.recipientClipXFrac(FxRecipient.FOE)
        assertEquals(0.5f, clip.start, 0.0001f)
        assertEquals(1f, clip.endInclusive, 0.0001f)
    }

    @Test
    fun mantleAndHide_bracePips_noSlash_noShieldBlock() {
        val mantle = CombatFx.playSpec(
            CombatFx.specForPlayer("iron_mantle"),
            braceGained = 3,
            fxPlayer = true
        )
        assertNull(mantle.beat.stroke)
        assertEquals(FxTier.NO_STROKE, mantle.beat.tier)
        assertEquals(3, mantle.bracePipCount)
        assertEquals(FxRecipient.YOU, mantle.braceOwner)
        assertFalse(mantle.beat.useCleaveHitFlash)

        val hide = CombatFx.playSpec(
            CombatFx.specForEnemy("hide", EnemyKind.CAVE_TROLL),
            braceGained = 4,
            fxPlayer = false
        )
        assertNull(hide.beat.stroke)
        assertEquals(FxTier.NO_STROKE, hide.beat.tier)
        assertEquals(4, hide.bracePipCount)
        assertEquals(FxRecipient.FOE, hide.braceOwner)
    }

    @Test
    fun ashbrandSpark_hitFlashOnFoe_notFullScreen() {
        val spark = CombatFx.specForSpark()
        assertTrue(spark.useCleaveHitFlash)
        assertEquals(FxRecipient.FOE, spark.recipient)
        assertNotNull(spark.stroke) // v0.1.46: short drawn stroke + tip spark
        assertFalse(spark.useWakeSlash)
        assertEquals(CleaveKit.HIT_FLASH_MS, spark.hitFlashMs)
        assertEquals(0L, spark.flashMs)
        assertEquals(CombatFx.STROKE_SPARK_MS, spark.strokeMs)
        // Clip is half-stage FOE — not full screen
        val clip = CombatFx.recipientClipXFrac(spark.recipient)
        assertEquals(0.5f, clip.start, 0.0001f)
        assertTrue(clip.endInclusive <= 1f + 0.0001f)
        assertTrue(CombatFx.isSparkId(CombatFx.ID_ASHBRAND_SPARK))
        assertEquals(FxTier.SMALL, CombatFx.tierForPlayer(CombatFx.ID_ASHBRAND_SPARK))
    }

    @Test
    fun engine_sparkWiresFxId_hitFlash() {
        val engine = CombatEngine(Random(42))
        val cards = listOf(
            CardCatalog.byId("hostflint")!!,
            CardCatalog.byId("emberbrand")!!,
            CardCatalog.byId("cinder_step")!!,
            CardCatalog.byId("dust_veil")!!,
            CardCatalog.byId("tower_pike")!!
        )
        var s = engine.start(
            activeCards = cards,
            enemy = Enemy.normal(EnemyKind.GOBLIN, 1),
            weapon = WeaponRuntime(WeaponCatalog.ashbrand, 1, 0),
            maxHp = 30,
            playerHp = 30
        )
        // Force spark-only weapon beat
        s = s.copy(
            awaitingWeapon = true,
            pendingSpark = true,
            pendingFullWake = false,
            weapon = s.weapon.copy(charge = 0)
        )
        s = engine.resolveWeapon(s)
        val sparkEv = s.log.last { it.message.contains("spark", ignoreCase = true) }
        assertEquals(CombatFx.ID_ASHBRAND_SPARK, sparkEv.fxId)
        assertTrue(sparkEv.fxPlayer)
        val spec = CombatFx.safeSpec(sparkEv.fxId, sparkEv.fxPlayer, s.enemy.kind)!!
        assertTrue(spec.useCleaveHitFlash)
        assertEquals(FxRecipient.FOE, spec.recipient)
    }

    @Test
    fun wakeUnchanged_artAndTier() {
        assertTrue(WakeArt.WAKE_ART_FULLY_FROZEN)
        assertEquals("wake_vfx_slash", WakeArt.VFX_SLASH)
        assertEquals("ashbrand_spark", WakeArt.SPARK_DRAWABLE)
        assertEquals(700L, WakeArt.FRAME_SLASH_MS)
        val wake = CombatFx.specForWake()
        assertEquals(FxTier.WAKE, wake.tier)
        assertTrue(wake.useWakeSlash)
        assertNull(wake.stroke)
        assertFalse(wake.useCleaveHitFlash)
        assertEquals(FxRecipient.FOE, wake.recipient)
    }

    @Test
    fun speed2x_halvesSlashAndHitFlash() {
        assertEquals(CombatFx.STROKE_SMALL_MS / 2, CombatFx.fxHoldMs(CombatFx.STROKE_SMALL_MS, 2))
        assertEquals(CombatFx.STROKE_MEDIUM_MS / 2, CombatFx.fxHoldMs(CombatFx.STROKE_MEDIUM_MS, 2))
        assertEquals(160L, CombatFx.fxHoldMs(CleaveKit.HIT_FLASH_MS, 2))
        assertEquals(350L, CombatFx.fxHoldMs(CombatFx.BRACE_PIP_MS, 2))
    }

    @Test
    fun clubAndGatePulse_mapEnemySmall_notMedium() {
        assertTrue("club" in CombatFx.ENEMY_SMALL)
        assertTrue("gate_pulse" in CombatFx.ENEMY_SMALL)
        assertFalse("club" in CombatFx.ENEMY_MEDIUM)
        assertFalse("gate_pulse" in CombatFx.ENEMY_MEDIUM)
        assertEquals(FxTier.SMALL, CombatFx.tierForEnemy("club"))
        assertEquals(FxTier.SMALL, CombatFx.tierForEnemy("gate_pulse"))
        val club = CombatFx.specForEnemy("club", EnemyKitRole.CAVE_TROLL)
        assertEquals(FxTier.SMALL, club.tier)
        assertEquals(FxRecipient.YOU, club.recipient)
        assertNotNull(club.stroke)
        assertEquals(CombatFx.THICK_SMALL * CombatFx.THICK_ENEMY_FACTOR, club.stroke!!.thickness, 0.001f)
        val gate = CombatFx.specForEnemy("gate_pulse", EnemyKitRole.GATE_WARDEN)
        assertEquals(FxTier.SMALL, gate.tier)
        assertEquals(FxRecipient.YOU, gate.recipient)
        // Existing Medium path still for cleave / seal_pulse
        assertEquals(FxTier.MEDIUM, CombatFx.tierForEnemy("cleave"))
        assertEquals(FxTier.MEDIUM, CombatFx.tierForEnemy("seal_pulse"))
    }

    @Test
    fun aimLocks_fromV0140_kept() {
        // Half-stage clip
        assertEquals(0f, CombatFx.recipientClipXFrac(FxRecipient.YOU).start, 0.0001f)
        assertEquals(0.5f, CombatFx.recipientClipXFrac(FxRecipient.YOU).endInclusive, 0.0001f)
        assertEquals(0.5f, CombatFx.recipientClipXFrac(FxRecipient.FOE).start, 0.0001f)
        // Glow tame
        val glow = CombatFx.strokeGlowWidth(CombatFx.THICK_SMALL)
        val core = CombatFx.strokeCoreWidth(CombatFx.THICK_SMALL)
        assertTrue(glow > core)
        assertTrue(core > CombatFx.HAIRLINE_CORE_PX_SMALL * 2.5f)
        assertTrue(glow < CombatFx.REF_BUST_WIDTH_PX * 0.55f)
    }
}
