package com.towerofdarkness.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.domain.cards.Card
import com.towerofdarkness.app.domain.combat.CombatAnimStyle
import com.towerofdarkness.app.domain.combat.CombatBeat
import com.towerofdarkness.app.domain.combat.CombatFx
import com.towerofdarkness.app.domain.combat.EnemyKits
import com.towerofdarkness.app.domain.combat.EnemySkill
import com.towerofdarkness.app.domain.combat.EnemySkillKind
import com.towerofdarkness.app.domain.combat.FxRecipient
import com.towerofdarkness.app.domain.combat.FxStrokeSpec
import com.towerofdarkness.app.domain.combat.FxTier
import com.towerofdarkness.app.domain.combat.GarnishKind
import com.towerofdarkness.app.domain.combat.GarnishSpec
import com.towerofdarkness.app.domain.combat.PlumeGarnishKit
import com.towerofdarkness.app.domain.combat.WeaponTag
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.domain.combat.BraceDrawSync
import com.towerofdarkness.app.domain.combat.StatusPips
import com.towerofdarkness.app.domain.combat.WakeArt
import com.towerofdarkness.app.domain.combat.WakeIconPhase
import com.towerofdarkness.app.domain.combat.WakeStageFrame
import androidx.compose.foundation.clickable
import com.towerofdarkness.app.ui.components.AshbrandIcon
import com.towerofdarkness.app.ui.components.CombatBracePipsOverlay
import com.towerofdarkness.app.ui.components.CombatHitFlashOverlay
import com.towerofdarkness.app.ui.components.CombatStrokeOverlay
import com.towerofdarkness.app.ui.components.CombatParticleGarnishOverlay
import com.towerofdarkness.app.ui.components.CombatTileFlash
import com.towerofdarkness.app.ui.components.SkillGlyphIcon
import com.towerofdarkness.app.ui.components.skillJobIsBrace
import com.towerofdarkness.app.domain.glyphs.SkillGlyph
import com.towerofdarkness.app.ui.components.EnemySilhouette
import com.towerofdarkness.app.ui.components.GlossaryText
import com.towerofdarkness.app.ui.components.HeroShowcase
import com.towerofdarkness.app.ui.components.StatusPipRow
import com.towerofdarkness.app.ui.components.WakeStageOverlay
import com.towerofdarkness.app.ui.theme.Accent
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Ember
import com.towerofdarkness.app.ui.theme.GlowRare
import com.towerofdarkness.app.ui.theme.GlowUncommon
import com.towerofdarkness.app.ui.theme.Gold
import com.towerofdarkness.app.ui.theme.Moss
import com.towerofdarkness.app.ui.theme.Panel
import com.towerofdarkness.app.ui.theme.Steel
import com.towerofdarkness.app.ui.theme.VoidBg
import kotlinx.coroutines.delay

@Composable
fun CombatScreen(gc: GameController) {
    val state = gc.combatState
    val shake = remember { Animatable(0f) }
    var floatMsg by remember { mutableStateOf<String?>(null) }
    var diceShake by remember { mutableStateOf(0f) }
    // v0.1.14: staged HP so pip + absorb float draw before leftover HP bar move
    var displayedPlayerHp by remember { mutableIntStateOf(-1) }
    var braceAbsorbFloat by remember { mutableStateOf<Int?>(null) }
    var prevLogSize by remember { mutableIntStateOf(0) }
    // v0.1.15: Wake icon + portrait crescent (FULL only); SPARK ember on icon
    var wakeIconPhase by remember { mutableStateOf(WakeIconPhase.IDLE) }
    var wakeStageFrame by remember { mutableStateOf(WakeStageFrame.NONE) }
    // v0.1.38-fxaim: tile flash (skill tile only) / recipient slash / Brace pips / Soften pulse
    var tileFlash by remember { mutableStateOf(false) }
    var tileFlashColor by remember { mutableStateOf(CombatFx.COLOR_YOU) }
    var strokeSpec by remember { mutableStateOf<FxStrokeSpec?>(null) }
    var strokeVisible by remember { mutableStateOf(false) }
    var strokeTier by remember { mutableStateOf(FxTier.SMALL) }
    var strokeHoldMs by remember { mutableStateOf(CombatFx.STROKE_SMALL_MS) }
    var hitFlashVisible by remember { mutableStateOf(false) }
    var hitFlashRecipient by remember { mutableStateOf<FxRecipient?>(null) }
    var hitFlashHoldMs by remember { mutableStateOf(com.towerofdarkness.app.domain.combat.CleaveKit.HIT_FLASH_MS) }
    var hitFlashTint by remember { mutableStateOf(CombatFx.COLOR_YOU) }
    var bracePipCount by remember { mutableStateOf(0) }
    var bracePipOwner by remember { mutableStateOf<FxRecipient?>(null) }
    var bracePipVisible by remember { mutableStateOf(false) }
    var bracePipProgress by remember { mutableStateOf(0f) }
    var softenPulse by remember { mutableStateOf(false) }
    // v0.1.48-strokeboth: combat-log echo of drawn stroke (primary) + optional tip garnish
    var strokeDebugLine by remember { mutableStateOf<String?>(null) }
    var slashDebugLine by remember { mutableStateOf<String?>(null) }
    var slashDebugTarget by remember { mutableStateOf("foe") }
    var sparkStroke by remember { mutableStateOf(false) }
    // v0.1.49-plumegarnish: tip spark / dust puff / Soften kenney / Brace flare
    var tipGarnishSpec by remember { mutableStateOf<GarnishSpec?>(null) }
    var tipGarnishVisible by remember { mutableStateOf(false) }
    var tipGarnishRecipient by remember { mutableStateOf<FxRecipient?>(null) }
    var tipGarnishHoldMs by remember { mutableStateOf(PlumeGarnishKit.EMBER_MS) }
    var tipGarnishDebug by remember { mutableStateOf<String?>(null) }
    var dustGarnishSpec by remember { mutableStateOf<GarnishSpec?>(null) }
    var dustGarnishVisible by remember { mutableStateOf(false) }
    var dustGarnishHoldMs by remember { mutableStateOf(PlumeGarnishKit.PUFF_MS) }
    var dustGarnishDebug by remember { mutableStateOf<String?>(null) }
    var braceFlareSpec by remember { mutableStateOf<GarnishSpec?>(null) }
    var braceFlareVisible by remember { mutableStateOf(false) }
    var braceFlareHoldMs by remember { mutableStateOf(PlumeGarnishKit.BRACE_FLARE_MS) }
    var braceFlareDebug by remember { mutableStateOf<String?>(null) }
    var softenGarnishDebug by remember { mutableStateOf<String?>(null) }
    if (state != null && displayedPlayerHp < 0) {
        displayedPlayerHp = state.playerHp
    }

    // New fight: snap staged HP (do not carry prior fight's lag)
    LaunchedEffect(state?.enemy?.kind, state?.log?.size == 0) {
        if (state != null && state.log.isEmpty()) {
            displayedPlayerHp = state.playerHp
            braceAbsorbFloat = null
            prevLogSize = 0
        }
    }

    // Snapshot speed with this log beat so mid-toggle VFX follows NEXT beat holds
    // v0.1.37-fx kernel: flash → stroke → float → shake (fail-safe; never blocks resolve+log)
    LaunchedEffect(state?.log?.size) {
        val s = state ?: return@LaunchedEffect
        val last = s.log.lastOrNull() ?: return@LaunchedEffect
        val newEvents = s.log.drop(prevLogSize.coerceAtMost(s.log.size))
        prevLogSize = s.log.size
        val speed = gc.combatSpeedX
        val tick = GameController.combatHoldMs(40L, speed)
        val floatHold = CombatFx.fxHoldMs(CombatFx.FLOAT_MS, speed)
        val absorbHold = GameController.combatHoldMs(BraceDrawSync.ABSORB_FLOAT_MS, speed)
        // Brace absorb in THIS log delta (hit may be followed by Defeat…)
        val absorbEvent = newEvents.lastOrNull { it.braceAbsorbed > 0 }
        if (absorbEvent != null && BraceDrawSync.delayHpBarAfter(absorbEvent)) {
            braceAbsorbFloat = absorbEvent.braceAbsorbed
            delay(absorbHold)
            braceAbsorbFloat = null
            displayedPlayerHp = s.playerHp
        } else {
            braceAbsorbFloat = null
            displayedPlayerHp = s.playerHp
        }
        if (last.message.contains("Dice tumble")) {
            repeat(8) {
                diceShake = if (it % 2 == 0) 4f else -4f
                delay(tick)
            }
            diceShake = 0f
        }
        // Primary FX line in this delta (skill / enemy / Wake); follow-ups may carry Brace/Soften only
        val fxEvent = newEvents.lastOrNull { it.fxId != null } ?: last.takeIf { it.fxId != null }
        val braceEv = newEvents.lastOrNull { it.braceGained > 0 }
        val softenEv = newEvents.lastOrNull { it.softenApplied > 0 }
        try {
            // Harden fxPlayer: enemy kit ids always FOE_TO_YOU / recipient YOU
            // (avoids `fxPlayer != false` defaulting enemy hits onto the foe bust).
            val fxId = fxEvent?.fxId
            val fxPlayerResolved = CombatFx.resolveFxPlayer(
                fxId,
                fxEvent?.fxPlayer ?: true
            )
            val beat = CombatFx.safeSpec(fxId, fxPlayerResolved, s.enemy.kind)
            val play = when {
                beat != null -> CombatFx.playSpec(
                    beat = beat,
                    braceGained = braceEv?.braceGained ?: fxEvent?.braceGained ?: 0,
                    softenApplied = softenEv?.softenApplied ?: fxEvent?.softenApplied ?: 0,
                    fxPlayer = fxPlayerResolved
                )
                else -> CombatFx.playBraceOrSoftenOnly(
                    braceGained = braceEv?.braceGained ?: 0,
                    softenApplied = softenEv?.softenApplied ?: 0,
                    fxPlayer = CombatFx.resolveFxPlayer(
                        null,
                        (braceEv?.fxPlayer ?: softenEv?.fxPlayer) ?: true
                    )
                )
            }
            if (play != null) {
                val spec = play.beat
                // 1. Fired tile flash — skill / enemy tile only (not stage wash)
                // Spark skips tile flash via flashMs==0; damage strokes still flash the tile
                // even when contact hit-flash is on (v0.1.43).
                if (beat != null && spec.flashMs > 0L) {
                    tileFlashColor = CombatFx.colorArgb(spec.role)
                    tileFlash = true
                    delay(CombatFx.fxHoldMs(spec.flashMs, speed))
                    tileFlash = false
                }
                // 2a. Recipient slash — CLEAVE slash-light (unless NO STROKE / Wake slash)
                // Contact hit-flash overlaps stroke start (on the cut); SPARK is hit-flash only.
                if (spec.stroke != null && !spec.useWakeSlash) {
                    strokeSpec = spec.stroke
                    strokeTier = spec.tier
                    strokeHoldMs = CombatFx.fxHoldMs(spec.strokeMs, speed)
                    sparkStroke = CombatFx.isSparkId(fxId ?: "")
                    slashDebugTarget = CombatFx.strokeTargetLabel(
                        spec.stroke!!.recipient,
                        if (spec.stroke!!.recipient == FxRecipient.FOE) {
                            s.enemy.kind.displayName
                        } else null
                    )
                    // Sync combat-log proof immediately (enemy You / player foe).
                    // Overlay also logs via onStrokeDebug — keep both paths.
                    strokeDebugLine = CombatFx.strokeDebugLine(slashDebugTarget)
                    slashDebugLine = null // tip garnish only
                    strokeVisible = true
                    if (spec.useCleaveHitFlash && spec.hitFlashMs > 0L) {
                        hitFlashRecipient = spec.recipient
                        hitFlashHoldMs = CombatFx.fxHoldMs(spec.hitFlashMs, speed)
                        hitFlashTint = CombatFx.colorArgb(spec.role)
                        hitFlashVisible = true
                    }
                    // v0.1.49: tip spark / dust puff alongside locked stroke (TARGET bust / tip)
                    val kinds = PlumeGarnishKit.kindsForAbility(fxId)
                    if (GarnishKind.TIP_SPARK in kinds || sparkStroke) {
                        val tip = if (sparkStroke || fxId == CombatFx.ID_ASHBRAND_SPARK) {
                            PlumeGarnishKit.tipSparkSpec()
                        } else {
                            // optional hit-confirm: PLUME tip if sampler present else Kenney
                            PlumeGarnishKit.tipSparkSpec()
                        }
                        tipGarnishSpec = tip
                        tipGarnishRecipient = spec.stroke!!.recipient
                        tipGarnishHoldMs = CombatFx.fxHoldMs(tip.holdMs, speed)
                        tipGarnishVisible = true
                    }
                    if (GarnishKind.DUST_PUFF in kinds) {
                        val dust = PlumeGarnishKit.dustPuffSpec(fxId ?: "dust_veil")
                        dustGarnishSpec = dust
                        dustGarnishHoldMs = CombatFx.fxHoldMs(dust.holdMs, speed)
                        dustGarnishVisible = true
                    }
                    delay(strokeHoldMs)
                    strokeVisible = false
                    strokeSpec = null
                    sparkStroke = false
                    strokeDebugLine = null
                    slashDebugLine = null
                    hitFlashVisible = false
                    hitFlashRecipient = null
                    tipGarnishVisible = false
                    tipGarnishSpec = null
                    tipGarnishRecipient = null
                    tipGarnishDebug = null
                    dustGarnishVisible = false
                    dustGarnishSpec = null
                    dustGarnishDebug = null
                } else if (spec.useCleaveHitFlash) {
                    // 2a2. Ashbrand SPARK — CLEAVE hit-flash on foe bust (additive, not full-screen)
                    hitFlashRecipient = spec.recipient
                    hitFlashHoldMs = CombatFx.fxHoldMs(spec.hitFlashMs, speed)
                    hitFlashTint = CombatFx.colorArgb(spec.role)
                    hitFlashVisible = true
                    delay(hitFlashHoldMs)
                    hitFlashVisible = false
                    hitFlashRecipient = null
                }
                // 2b. Brace shield pips around owner (no slash for brace grant)
                if (play.bracePipCount > 0 && play.braceOwner != null) {
                    bracePipCount = play.bracePipCount
                    bracePipOwner = play.braceOwner
                    // First visible frame at progress 0 → full alpha via bracePipAlpha
                    bracePipProgress = 0f
                    bracePipVisible = true
                    // v0.1.49: Brace GAIN flare only — does NOT replace floating pips
                    val flare = PlumeGarnishKit.braceFlareSpec()
                    braceFlareSpec = flare
                    braceFlareHoldMs = CombatFx.fxHoldMs(flare.holdMs, speed)
                    braceFlareVisible = true
                    val pipHold = CombatFx.fxHoldMs(CombatFx.BRACE_PIP_MS, speed)
                    val steps = 6
                    repeat(steps) { i ->
                        bracePipProgress = (i + 1).toFloat() / steps
                        delay((pipHold / steps).coerceAtLeast(1L))
                        if (i == 2) {
                            // Flare is shorter than pip hold — clear early
                            braceFlareVisible = false
                        }
                    }
                    bracePipVisible = false
                    bracePipCount = 0
                    bracePipOwner = null
                    bracePipProgress = 0f
                    braceFlareVisible = false
                    braceFlareSpec = null
                    braceFlareDebug = null
                }
                // 2c. Soften red pip pulse on foe
                if (play.softenPipPulse) {
                    softenPulse = true
                    // Soften Kenney pip under foe HP (status-active overlay also draws while counterPenalty>0)
                    softenGarnishDebug = PlumeGarnishKit.kenneyDebugLine(
                        PlumeGarnishKit.softenPipSpec().sheetId,
                        "foe"
                    )
                    delay(CombatFx.fxHoldMs(CombatFx.SOFTEN_PULSE_MS, speed))
                    softenPulse = false
                }
                // 3. Number float (damage and/or Brace)
                val floatText = fxEvent?.floating?.text
                    ?: braceEv?.floating?.text
                    ?: last.floating?.text
                if (floatText != null) {
                    floatMsg = floatText
                    delay(floatHold)
                    floatMsg = null
                }
                // 4. Shake by tier (Wake = heavier; layered with WakeStageOverlay)
                if (spec.shake.amplitude > 0f && spec.shake.pulses > 0) {
                    val amp = spec.shake.amplitude
                    val pulses = spec.shake.pulses
                    val pulseBudget = CombatFx.fxHoldMs(spec.shakeMs, speed) / pulses.coerceAtLeast(1)
                    repeat(pulses) {
                        shake.snapTo(if (it % 2 == 0) amp else -amp)
                        delay(pulseBudget.coerceAtLeast(1L))
                    }
                    shake.snapTo(0f)
                }
            } else {
                // No fx / brace / soften — legacy float / rare anim only
                last.floating?.let {
                    floatMsg = it.text
                    delay(floatHold)
                    floatMsg = null
                }
                if (last.animStyle == CombatAnimStyle.CHARGE_SHAKE_SLOWMO) {
                    repeat(6) {
                        shake.snapTo(if (it % 2 == 0) 8f else -8f)
                        delay(tick)
                    }
                    shake.snapTo(0f)
                }
            }
        } catch (_: Throwable) {
            // Fail-safe: clear FX chrome; resolve+log already committed by engine
            tileFlash = false
            strokeVisible = false
            strokeSpec = null
            slashDebugLine = null
            hitFlashVisible = false
            hitFlashRecipient = null
            bracePipVisible = false
            bracePipCount = 0
            softenPulse = false
            shake.snapTo(0f)
            last.floating?.let { floatMsg = it.text }
        }
    }

    // FULL Wake: charge → crack → crescent frames → clear; SPARK: ember only
    LaunchedEffect(state?.beat, state?.weaponFlashed, state?.log?.size, gc.combatSpeedX) {
        val s = state
        if (s == null) {
            wakeIconPhase = WakeIconPhase.IDLE
            wakeStageFrame = WakeStageFrame.NONE
            return@LaunchedEffect
        }
        if (!WakeArt.isWeaponBeat(s)) {
            wakeIconPhase = WakeIconPhase.IDLE
            wakeStageFrame = WakeStageFrame.NONE
            return@LaunchedEffect
        }
        val speed = gc.combatSpeedX
        if (WakeArt.isSparkOnly(s)) {
            wakeStageFrame = WakeStageFrame.NONE
            wakeIconPhase = WakeIconPhase.SPARK_EMBER
            delay(WakeArt.holdMs(Balance.WEAPON_HOLD_MS, speed))
            wakeIconPhase = WakeIconPhase.IDLE
            return@LaunchedEffect
        }
        if (!WakeArt.isFullWakeBeat(s)) {
            wakeIconPhase = WakeIconPhase.IDLE
            wakeStageFrame = WakeStageFrame.NONE
            return@LaunchedEffect
        }
        // Sequence fits inside WEAPON_FULL_HOLD_MS; 2x halves each frame
        var elapsed = 0L
        for (step in WakeArt.stageSequence()) {
            wakeIconPhase = WakeArt.iconPhase(s, elapsed, speed)
            wakeStageFrame = step.frame
            // v0.1.49: Wake tip spark (PLUME grinder-sparks additive) on IMPACT
            if (step.frame == WakeStageFrame.IMPACT) {
                val tip = PlumeGarnishKit.tipSparkSpec()
                tipGarnishSpec = tip
                tipGarnishRecipient = FxRecipient.FOE
                tipGarnishHoldMs = CombatFx.fxHoldMs(tip.holdMs, speed)
                tipGarnishVisible = true
                slashDebugTarget = CombatFx.strokeTargetLabel(
                    FxRecipient.FOE, s.enemy.kind.displayName
                )
            }
            val budget = WakeArt.holdMs(step.baseMs, speed)
            delay(budget)
            elapsed += budget
        }
        wakeStageFrame = WakeStageFrame.NONE
        tipGarnishVisible = false
        tipGarnishSpec = null
        tipGarnishRecipient = null
        wakeIconPhase = WakeIconPhase.IDLE
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(VoidBg)
            .graphicsLayer { translationX = shake.value + diceShake }
            .padding(16.dp)
    ) {
        if (state == null) {
            Text("No combat.", color = Bone)
            return
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                if (state.enemy.isBoss) "BOSS · ${state.enemy.kind.displayName}" else state.enemy.kind.displayName,
                color = if (state.enemy.isBoss) Ember else Gold,
                fontSize = 18.sp
            )
            // 1x ↔ 2x toggle — label shows ACTIVE rate
            OutlinedButton(onClick = { gc.toggleCombatSpeed() }) {
                Text("${gc.combatSpeedX}x")
            }
        }
        Text("Round ${state.round}", color = Bone.copy(0.6f), fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))

        Box(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // v0.1.23-nobg: combat You = PNG only; no plate / victory ring
                    HeroShowcase(
                        rarity = Rarity.COMMON,
                        modifier = Modifier.height(90.dp)
                    )
                    Text("You", color = Bone, fontSize = 12.sp)
                    HpBar(displayedPlayerHp.coerceAtLeast(0), state.playerMaxHp, Moss)
                    // Status pips under HP — Brace pip ticks first; absorb float then HP (v0.1.14)
                    StatusPipRow(
                        pips = StatusPips.forPlayer(state),
                        onTerm = { gc.showGlossary(it) },
                        braceAbsorbFloat = braceAbsorbFloat
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // v0.1.23-nobg: Warden PNG only; trash Canvas body unchanged
                    EnemySilhouette(
                        kind = state.enemy.kind,
                        isBoss = state.enemy.isBoss,
                        look = state.enemy.look
                    )
                    Text(state.enemy.kind.displayName, color = Bone, fontSize = 12.sp)
                    HpBar(state.enemy.hp, state.enemy.maxHp, Ember)
                    // Soften / enemy Brace pips
                    StatusPipRow(
                        pips = StatusPips.forEnemy(state),
                        onTerm = { gc.showGlossary(it) },
                        softenPulse = softenPulse
                    )
                    // v0.1.32: 2 specials + Hit under enemy HP (smaller chrome; not a 5th bar)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        EnemyKits.skillsFor(state.enemy).forEach { skill ->
                            val isCurrent = skill.id == state.enemyHighlightedId
                            Box {
                                EnemyKitSlot(
                                    skill = skill,
                                    spent = skill.id in state.enemySpentIds,
                                    current = isCurrent,
                                    onTap = { gc.showGlossary(skill.glossaryKey) }
                                )
                                CombatTileFlash(
                                    visible = tileFlash && isCurrent && !fxFlashIsPlayer(state),
                                    colorArgb = tileFlashColor,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                        }
                    }
                }
            }
            // FULL Wake crescent only — safe of status bar / skill row (art margins)
            if (wakeStageFrame != WakeStageFrame.NONE) {
                WakeStageOverlay(
                    frame = wakeStageFrame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .align(Alignment.TopCenter)
                )
            }
            // v0.1.48-strokeboth: drawn Wake-family stroke on recipient bust (primary).
            // 180.dp stage strip; half-stage X clip unchanged. Optional tip garnish only.
            CombatStrokeOverlay(
                stroke = strokeSpec,
                visible = strokeVisible,
                tier = strokeTier,
                holdMs = strokeHoldMs,
                debugTarget = slashDebugTarget,
                sparkStroke = sparkStroke,
                onStrokeDebug = { line -> strokeDebugLine = line },
                onSlashLightDebug = { line -> slashDebugLine = line },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.TopCenter)
            )
            CombatHitFlashOverlay(
                recipient = hitFlashRecipient,
                visible = hitFlashVisible,
                holdMs = hitFlashHoldMs,
                tintArgb = hitFlashTint,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.TopCenter)
            )
            CombatBracePipsOverlay(
                count = bracePipCount,
                owner = bracePipOwner,
                visible = bracePipVisible,
                progress = bracePipProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.TopCenter)
            )
            // v0.1.49-plumegarnish: tip spark at stroke tip (PLUME grinder-sparks / Kenney spark)
            CombatParticleGarnishOverlay(
                spec = tipGarnishSpec,
                recipient = tipGarnishRecipient,
                visible = tipGarnishVisible,
                holdMs = tipGarnishHoldMs,
                debugTarget = slashDebugTarget,
                atStrokeTip = true,
                tier = strokeTier,
                onDebug = { tipGarnishDebug = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.TopCenter)
            )
            // Dust puff on TARGET bust only (Dust Veil / Ash Press / Cinder Step)
            CombatParticleGarnishOverlay(
                spec = dustGarnishSpec,
                recipient = FxRecipient.FOE,
                visible = dustGarnishVisible,
                holdMs = dustGarnishHoldMs,
                debugTarget = slashDebugTarget,
                atStrokeTip = false,
                onDebug = { dustGarnishDebug = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.TopCenter)
            )
            // Brace GAIN flare on owner — alongside floating pips (not a replacement)
            CombatParticleGarnishOverlay(
                spec = braceFlareSpec,
                recipient = bracePipOwner,
                visible = braceFlareVisible,
                holdMs = braceFlareHoldMs,
                debugTarget = when (bracePipOwner) {
                    FxRecipient.YOU -> "You"
                    FxRecipient.FOE -> "foe"
                    null -> "You"
                },
                atStrokeTip = false,
                onDebug = { braceFlareDebug = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.TopCenter)
            )
            // Soften Kenney circle under foe HP while Soften status active
            CombatParticleGarnishOverlay(
                spec = if (state.counterPenalty > 0) PlumeGarnishKit.softenPipSpec() else null,
                recipient = FxRecipient.FOE,
                visible = state.counterPenalty > 0,
                holdMs = PlumeGarnishKit.SOFTEN_PIP_PULSE_MS,
                debugTarget = "foe",
                underHp = true,
                atStrokeTip = false,
                onDebug = { softenGarnishDebug = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.TopCenter)
            )
        }

        Box(Modifier.fillMaxWidth().height(36.dp), contentAlignment = Alignment.Center) {
            floatMsg?.let { Text(it, color = Gold, fontSize = 22.sp) }
        }

        // 5 skill slots
        Text("Skills", color = Bone.copy(0.7f), fontSize = 11.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            state.activeCards.take(5).forEach { card ->
                val isCurrent = card.id == state.highlightedId
                Box(Modifier.weight(1f)) {
                    SkillSlot(
                        card = card,
                        spent = card.id in state.spentIds,
                        current = isCurrent,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Fired tile flash — this skill tile only (not whole combat row)
                    CombatTileFlash(
                        visible = tileFlash && isCurrent && fxFlashIsPlayer(state),
                        colorArgb = tileFlashColor,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))

        // Weapon row (not in dice)
        WeaponBar(state.weapon, flashed = state.weaponFlashed, iconPhase = wakeIconPhase, onAshbrandTap = { gc.showGlossary("ashbrand") })

        Spacer(Modifier.height(8.dp))
        Column(Modifier.weight(1f)) {
            // Pin FULL Wake line so it stays visible for the Wake hold beat
            val recent = state.log.takeLast(5).toMutableList()
            state.pinnedWakeLine?.let { pin ->
                if (recent.none { it.message == pin }) {
                    recent.add(0, com.towerofdarkness.app.domain.combat.CombatEvent(pin, goldLog = true))
                    while (recent.size > 5) recent.removeAt(0)
                }
            }
            // v0.1.48-strokeboth: prove drawn stroke (primary) + optional tip garnish
            strokeDebugLine?.let { dbg ->
                if (recent.none { it.message == dbg }) {
                    recent.add(com.towerofdarkness.app.domain.combat.CombatEvent(dbg, goldLog = true))
                    while (recent.size > 5) recent.removeAt(0)
                }
            }
            tipGarnishDebug?.let { dbg ->
                if (recent.none { it.message == dbg }) {
                    recent.add(com.towerofdarkness.app.domain.combat.CombatEvent(dbg, goldLog = true))
                    while (recent.size > 5) recent.removeAt(0)
                }
            }
            dustGarnishDebug?.let { dbg ->
                if (recent.none { it.message == dbg }) {
                    recent.add(com.towerofdarkness.app.domain.combat.CombatEvent(dbg, goldLog = true))
                    while (recent.size > 5) recent.removeAt(0)
                }
            }
            braceFlareDebug?.let { dbg ->
                if (recent.none { it.message == dbg }) {
                    recent.add(com.towerofdarkness.app.domain.combat.CombatEvent(dbg, goldLog = true))
                    while (recent.size > 5) recent.removeAt(0)
                }
            }
            softenGarnishDebug?.let { dbg ->
                if (recent.none { it.message == dbg }) {
                    recent.add(com.towerofdarkness.app.domain.combat.CombatEvent(dbg, goldLog = true))
                    while (recent.size > 5) recent.removeAt(0)
                }
            }
            slashDebugLine?.let { dbg ->
                if (recent.none { it.message == dbg }) {
                    recent.add(com.towerofdarkness.app.domain.combat.CombatEvent(dbg, goldLog = true))
                    while (recent.size > 5) recent.removeAt(0)
                }
            }
            recent.forEachIndexed { idx, ev ->
                val latest = idx == recent.lastIndex
                val color = when {
                    ev.goldLog || ev.message.startsWith("ASHBRAND — WAKE") -> Gold
                    latest -> Gold
                    else -> Bone.copy(0.85f)
                }
                GlossaryText(
                    text = ev.message,
                    highlights = (ev.glossaryHints + listOf(
                        "brace", "soften", "stun", "freeze",
                        "shiv", "nip", "cleave", "hide",
                        "seal pulse", "rust guard", "coal slam", "cinder hide", "hit",
                        "ember", "spark", "wake", "ashbrand"
                    )).distinct(),
                    onTerm = { gc.showGlossary(it) },
                    color = color,
                    fontSizeSp = if (ev.goldLog || latest) 17 else 14
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) {
                Text("Flee (locked)")
            }
        }
        if (state.finished || state.beat == CombatBeat.AWAITING_CONTINUE) {
            Spacer(Modifier.height(8.dp))
            Text(
                if (state.playerWon) "Victory" else "Defeat",
                color = Gold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { gc.continueAfterCombat() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Continue") }
        }
    }
}

@Composable
private fun SkillSlot(card: Card, spent: Boolean, current: Boolean, modifier: Modifier = Modifier) {
    // Brace-job skills keep Moss/green outline (WO v0.1.19); current still Gold.
    val border = when {
        current -> Gold
        spent -> Steel.copy(0.3f)
        skillJobIsBrace(card.id) -> Moss
        card.rarity == Rarity.RARE -> GlowRare
        card.rarity == Rarity.UNCOMMON -> GlowUncommon
        else -> Bone.copy(0.35f)
    }
    val dimmed = spent && !current
    Column(
        modifier
            .height(SkillGlyph.SKILL_SLOT_HEIGHT_DP.dp)
            .background(Panel.copy(alpha = if (dimmed) 0.85f else 1f), RoundedCornerShape(6.dp))
            .border(if (current) 2.dp else 1.dp, border, RoundedCornerShape(6.dp))
            .padding(horizontal = 2.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glyph ABOVE name; spent greys at VolumeArt.DIMMED_GLYPH_ALPHA (readable, no crush).
        SkillGlyphIcon(cardId = card.id, spent = dimmed, size = SkillGlyph.GLYPH_SIZE_DP.dp)
        Text(card.title, color = if (dimmed) Bone.copy(0.65f) else Bone, fontSize = 9.sp, maxLines = 2)
        Text("w${card.weight}", color = Bone.copy(if (dimmed) 0.4f else 0.5f), fontSize = 8.sp)
    }
}

@Composable
private fun WeaponBar(
    weapon: com.towerofdarkness.app.domain.combat.WeaponRuntime,
    flashed: Boolean,
    iconPhase: WakeIconPhase = WakeIconPhase.IDLE,
    onAshbrandTap: () -> Unit = {}
) {
    val tagColor = when (weapon.def.statusTag) {
        WeaponTag.Ember -> Ember
        WeaponTag.Notch -> Accent
        WeaponTag.Guard -> Moss
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(Panel, RoundedCornerShape(8.dp))
            .border(if (flashed) 2.dp else 1.dp, if (flashed) Gold else tagColor, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Ashbrand icon — same slot size as loadout weapon plate
        if (weapon.def.id == "ashbrand") {
            // Tap → glossary only; does not fire Wake or spend Sparks.
            AshbrandIcon(phase = iconPhase, onClick = onAshbrandTap)
        }
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text("${weapon.def.title}  Lv${weapon.level}", color = Bone, fontSize = 13.sp)
            Text("${weapon.def.statusTag} · ${weapon.def.abilityTitle}", color = tagColor, fontSize = 10.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(weapon.threshold) { i ->
                Box(
                    Modifier
                        .size(10.dp)
                        .background(
                            if (i < weapon.pipsFilled) tagColor else Steel.copy(0.4f),
                            CircleShape
                        )
                )
            }
        }
    }
}


@Composable
private fun EnemyKitSlot(
    skill: EnemySkill,
    spent: Boolean,
    current: Boolean,
    onTap: () -> Unit
) {
    val border = when {
        current -> Gold
        spent -> Steel.copy(0.3f)
        skill.kind == EnemySkillKind.BRACE -> Moss
        else -> Bone.copy(0.35f)
    }
    val dimmed = spent && !current
    Column(
        Modifier
            .height(48.dp)
            .clickable(onClick = onTap)
            .background(Panel.copy(alpha = if (dimmed) 0.85f else 1f), RoundedCornerShape(4.dp))
            .border(if (current) 2.dp else 1.dp, border, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            skill.title,
            color = if (dimmed) Bone.copy(0.65f) else Bone,
            fontSize = 8.sp,
            maxLines = 1
        )
        Text(
            "w${skill.weight}",
            color = Bone.copy(if (dimmed) 0.4f else 0.5f),
            fontSize = 7.sp
        )
    }
}

@Composable
private fun HpBar(hp: Int, max: Int, color: androidx.compose.ui.graphics.Color) {
    val f = if (max <= 0) 0f else hp.toFloat() / max
    Column(Modifier.padding(top = 4.dp)) {
        LinearProgressIndicator(progress = { f }, modifier = Modifier.fillMaxWidth(0.4f).height(8.dp), color = color)
        Text("$hp / $max", color = Bone, fontSize = 11.sp)
    }
}

/** True when the current tile flash belongs on the player skill row (not enemy kit). */
private fun fxFlashIsPlayer(state: com.towerofdarkness.app.domain.combat.CombatState): Boolean {
    val lastFx = state.log.asReversed().firstOrNull { it.fxId != null } ?: return true
    return lastFx.fxPlayer
}
