package com.towerofdarkness.app.domain.combat

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.domain.cards.Card
import com.towerofdarkness.app.domain.effects.Equipment
import com.towerofdarkness.app.domain.effects.MoveEffect
import com.towerofdarkness.app.domain.effects.SkillEffect
import kotlin.random.Random

enum class CombatAnimStyle { QUICK, CHARGE_SHAKE_SLOWMO }

enum class CombatBeat {
    READY,
    AFTER_DICE,
    AFTER_SKILL,
    AFTER_WEAPON,
    AFTER_ENEMY,
    AWAITING_CONTINUE
}

/**
 * v0.1.61-continue: coarse combat UI phase.
 * Every tick: if allFoesHp <= 0 && phase != DEFEAT → VICTORY (anim must not block).
 */
enum class CombatPhase {
    COMBAT,
    VICTORY,
    DEFEAT
}

data class FloatingText(val text: String, val isPlayer: Boolean, val isCrit: Boolean = false)

data class CombatEvent(
    val message: String,
    val floating: FloatingText? = null,
    val animStyle: CombatAnimStyle = CombatAnimStyle.QUICK,
    val sound: String = "impact",
    val glossaryHints: List<String> = emptyList(),
    val goldLog: Boolean = false,
    /** Presentation only (v0.1.14): Brace absorbed on this hit; 0 = none. */
    val braceAbsorbed: Int = 0,
    /**
     * Presentation-only FX id (v0.1.37). Card id / enemy skill id / [CombatFx.ID_ASHBRAND_WAKE].
     * Null = no kernel stroke/flash for this log line (follow-up Brace/Soften/Spark lines).
     * Never affects damage math.
     */
    val fxId: String? = null,
    /** True when [fxId] is a player skill / Wake; false for enemy kit. */
    val fxPlayer: Boolean = true,
    /**
     * Presentation only (v0.1.38): Brace granted on this line; 0 = none.
     * Drives shield-pip count (capped at 5 in CombatFx.bracePipCount).
     */
    val braceGained: Int = 0,
    /**
     * Presentation only (v0.1.38): Soften amount applied on this line; 0 = none.
     * Drives red Soften pip pulse on foe — no plate / no extra slash.
     */
    val softenApplied: Int = 0
)

data class CombatState(
    val playerHp: Int,
    val playerMaxHp: Int,
    val brace: Int = 0,
    val enemy: Enemy,
    val activeCards: List<Card>,
    val spentIds: Set<String> = emptySet(),
    val highlightedId: String? = null,
    val weapon: WeaponRuntime,
    val round: Int = 1,
    val log: List<CombatEvent> = emptyList(),
    /** Soften remaining — reduces next damaging enemy kit skill (not Hide family / Nip). */
    val counterPenalty: Int = 0,
    val finished: Boolean = false,
    val playerWon: Boolean = false,
    /** v0.1.61-continue: COMBAT / VICTORY / DEFEAT — drives Continue + dice stop. */
    val phase: CombatPhase = CombatPhase.COMBAT,
    val lastFiredCard: Card? = null,
    val beat: CombatBeat = CombatBeat.READY,
    val weaponFlashed: Boolean = false,
    val fullProcThisCombat: Boolean = false,
    val fullProcKilled: Boolean = false,
    val lastSkillWasAttack: Boolean = false,
    val awaitingWeapon: Boolean = false,
    /** Queued independently — both may fire same beat. */
    val pendingFullWake: Boolean = false,
    val pendingSpark: Boolean = false,
    /** Sticky FULL Wake line for UI last-5 pin during Wake hold. */
    val pinnedWakeLine: String? = null,
    /** Enemy kit spent tile ids (same exhaust machine as player). */
    val enemySpentIds: Set<String> = emptySet(),
    val enemyHighlightedId: String? = null,
    /** Brace on the enemy (Hide / Rust Guard / Cinder Hide). Persists until eaten. */
    val enemyBrace: Int = 0,
    /** v0.1.58: Troll Tooth +2 on first damaging hit vs elite/boss this floor. */
    val trollToothReady: Boolean = false,
    /** v0.1.58: Dust Veil L3B — first fire this fight → weight +1 for rest of fight. */
    val pendingWeightBoostIds: Set<String> = emptySet(),
    val weightBoostedIds: Set<String> = emptySet()
)

class CombatEngine(private val rng: Random = Random.Default) {

    fun start(
        activeCards: List<Card>,
        enemy: Enemy,
        weapon: WeaponRuntime,
        maxHp: Int = Balance.PLAYER_MAX_HP,
        playerHp: Int = maxHp,
        initialBrace: Int = 0,
        trollToothReady: Boolean = false,
        pendingWeightBoostIds: Set<String> = emptySet()
    ): CombatState = CombatState(
        playerHp = playerHp.coerceIn(1, maxHp),
        playerMaxHp = maxHp,
        brace = initialBrace.coerceAtLeast(0),
        enemy = enemy,
        activeCards = activeCards,
        weapon = weapon,
        trollToothReady = trollToothReady,
        pendingWeightBoostIds = pendingWeightBoostIds
    )

    /** A — dice tumble: reset cycle if needed, pick unspent weighted slot. */
    fun diceTumble(state: CombatState): CombatState {
        if (state.finished || state.phase != CombatPhase.COMBAT ||
            state.beat == CombatBeat.AWAITING_CONTINUE
        ) return state
        var spent = state.spentIds
        val events = mutableListOf<CombatEvent>()
        if (spent.size >= state.activeCards.size && state.activeCards.isNotEmpty()) {
            spent = emptySet()
            events += CombatEvent("Cycle reset", sound = "")
        }
        events += CombatEvent("Dice tumble…", sound = "dice")
        val live = state.activeCards.filter { it.id !in spent }
        val card = pickWeighted(live) { it.weight } ?: return state.copy(log = state.log + events)
        return state.copy(
            spentIds = spent,
            highlightedId = card.id,
            lastFiredCard = card,
            log = state.log + events,
            beat = CombatBeat.AFTER_DICE,
            weaponFlashed = false,
            pinnedWakeLine = null
        )
    }

    /**
     * C — resolve highlighted skill; mark spent; attack skills +1 weapon charge.
     * Ember pool (v0.1.35): read Sparks before → damage in resolveCard → +1 Spark →
     * then Brace / Soften / Tithe. Wake threshold still queues for next weapon beat.
     */
    fun resolveSkill(state: CombatState): CombatState {
        val card = state.lastFiredCard ?: return state
        val pipsBefore = state.weapon.charge
        val anim = if (card.rarity == Rarity.RARE || card.rarity == Rarity.LEGENDARY)
            CombatAnimStyle.CHARGE_SHAKE_SLOWMO else CombatAnimStyle.QUICK
        val (after, events, isAttack) = resolveCard(state, card, anim)
        val allEvents = events.toMutableList()
        var s = after.copy(
            spentIds = state.spentIds + card.id,
            highlightedId = card.id,
            lastSkillWasAttack = isAttack,
            beat = CombatBeat.AFTER_SKILL
        )
        // v0.1.58 Dust Veil L3B: first fire → weight +1 rest of fight
        if (card.id in s.pendingWeightBoostIds && card.id !in s.weightBoostedIds) {
            val boosted = s.activeCards.map {
                if (it.id == card.id) it.copy(weight = it.weight + 1) else it
            }
            s = s.copy(
                activeCards = boosted,
                pendingWeightBoostIds = s.pendingWeightBoostIds - card.id,
                weightBoostedIds = s.weightBoostedIds + card.id
            )
        }
        if (isAttack) {
            val w = s.weapon
            s = s.copy(weapon = w.copy(charge = (w.charge + 1).coerceAtMost(w.threshold + 2)))
        }
        // Ember pool side effects AFTER +1 Spark (order lock — emberpool-v0135.md)
        val ember = card.effect
        if (ember is SkillEffect.EmberPoolSkill) {
            if (ember.braceIfZeroBefore > 0 && pipsBefore == 0) {
                s = s.copy(brace = s.brace + ember.braceIfZeroBefore)
                allEvents += CombatEvent(
                    "Brace +${ember.braceIfZeroBefore}",
                    FloatingText("BRACE", true), anim, "brace",
                    glossaryHints = listOf("brace"),
                    braceGained = ember.braceIfZeroBefore
                )
            }
            if (ember.softenIfBeforeGte1 > 0 && pipsBefore >= 1) {
                s = s.copy(counterPenalty = s.counterPenalty + ember.softenIfBeforeGte1)
                allEvents += CombatEvent(
                    "Counter softened −${ember.softenIfBeforeGte1}",
                    sound = "soften",
                    glossaryHints = listOf("soften"),
                    softenApplied = ember.softenIfBeforeGte1
                )
            }
            if (ember.titheSpendIfBeforeGte1 && pipsBefore >= 1) {
                val w = s.weapon
                s = s.copy(weapon = w.copy(charge = (w.charge - 1).coerceAtLeast(0)))
                allEvents += CombatEvent(
                    "Spark spent",
                    sound = "",
                    glossaryHints = listOf("spark")
                )
            }
        }
        s = s.copy(log = state.log + allEvents)
        // CHAIN full and SPARK are independent — both may queue same beat (after tithe)
        val mustFull = s.weapon.charge >= s.weapon.threshold
        val sparkChance = 0.08f + 0.04f * s.weapon.level
        val spark = rng.nextFloat() < sparkChance
        s = s.copy(
            awaitingWeapon = mustFull || spark,
            pendingFullWake = mustFull,
            pendingSpark = spark
        )
        if (s.enemy.hp <= 0 && !s.awaitingWeapon) {
            return finishVictory(s)
        }
        return s
    }

    /**
     * E — weapon beat. CHAIN full Wake and SPARK may both fire the same beat:
     * full Wake resets charge to 0; SPARK deals half and does not touch charge
     * (including after a same-beat reset).
     */
    fun resolveWeapon(state: CombatState): CombatState {
        if (!state.awaitingWeapon && !state.pendingFullWake && !state.pendingSpark) {
            return state.copy(
                beat = CombatBeat.AFTER_WEAPON,
                awaitingWeapon = false,
                pendingFullWake = false,
                pendingSpark = false
            )
        }
        var s = state
        var w = s.weapon
        val events = mutableListOf<CombatEvent>()
        var fullProc = s.fullProcThisCombat
        var fullKill = s.fullProcKilled
        val doFull = state.pendingFullWake || w.charge >= w.threshold
        val doSpark = state.pendingSpark

        var pinned: String? = s.pinnedWakeLine
        if (doFull) {
            val dmg = w.def.fullDmg(w.level)
            val applied = applyDamageToEnemy(s, dmg)
            s = applied.state
            // Exact CHAIN FULL Wake payoff line (gold + WAKE float + legendary anim)
            val wakeLine = "ASHBRAND — WAKE $dmg"
            events += CombatEvent(
                message = wakeLine,
                floating = FloatingText("WAKE", true, true),
                animStyle = CombatAnimStyle.CHARGE_SHAKE_SLOWMO,
                sound = "legendary",
                goldLog = true,
                fxId = CombatFx.ID_ASHBRAND_WAKE,
                fxPlayer = true
            )
            pinned = wakeLine
            fullProc = true
            if (s.enemy.hp <= 0) fullKill = true
            w = w.copy(charge = 0)
            s = s.copy(weapon = w, fullProcThisCombat = fullProc, fullProcKilled = fullKill)
        }

        if (doSpark) {
            val dmg = w.def.sparkDmg(w.level)
            val applied = applyDamageToEnemy(s, dmg)
            s = applied.state
            // Spark: plain log only — no anim, no gold, no WAKE float
            events += CombatEvent(
                message = "Ashbrand spark ($dmg)",
                floating = null,
                animStyle = CombatAnimStyle.QUICK,
                sound = "ember",
                goldLog = false,
                // v0.1.42: CLEAVE hit-flash on foe; icon ember stays WakeArt
                fxId = CombatFx.ID_ASHBRAND_SPARK,
                fxPlayer = true
            )
            s = s.copy(weapon = w)
        }

        s = s.copy(
            weaponFlashed = doFull, // gold slam only on FULL Wake
            log = s.log + events,
            awaitingWeapon = false,
            pendingFullWake = false,
            pendingSpark = false,
            pinnedWakeLine = pinned,
            beat = CombatBeat.AFTER_WEAPON
        )
        if (s.enemy.hp <= 0) return finishVictory(s)
        return s
    }

    /**
     * F — enemy live kit (v0.1.32): same weight / grey among unspent tiles.
     * Soften → next damaging skill (not Hide family); Nip ignores Soften.
     * Brace on You still absorbs before HP.
     */
    fun resolveEnemy(state: CombatState): CombatState {
        if (state.enemy.hp <= 0) return finishVictory(state)
        val kit = EnemyKits.skillsFor(state.enemy)
        var spent = state.enemySpentIds
        val events = mutableListOf<CombatEvent>()
        if (spent.size >= kit.size && kit.isNotEmpty()) {
            spent = emptySet()
            events += CombatEvent("Enemy cycle reset", sound = "")
        }
        val live = kit.filter { it.id !in spent }
        val skill = pickWeighted(live) { it.weight }
            ?: return state.copy(log = state.log + events, beat = CombatBeat.AFTER_ENEMY)
        events += CombatEvent("${state.enemy.kind.displayName} winds up…", sound = "")

        var s = state.copy(
            enemySpentIds = spent + skill.id,
            enemyHighlightedId = skill.id
        )
        var soften = s.counterPenalty

        when (skill.kind) {
            EnemySkillKind.BRACE -> {
                val gain = skill.braceGain
                s = s.copy(enemyBrace = s.enemyBrace + gain)
                val msg = "${s.enemy.kind.displayName} — ${skill.title} $gain"
                events += CombatEvent(
                    msg,
                    FloatingText("BRACE $gain", false),
                    sound = "brace",
                    glossaryHints = listOf(skill.glossaryKey, "brace"),
                    fxId = skill.id,
                    fxPlayer = false,
                    braceGained = gain
                )
                // Soften pip stays — Hide family does not consume Soften
            }
            EnemySkillKind.NIP -> {
                val raw = skill.rollDamage(rng)
                // Nip ignores Soften; Soften does not clear
                val (afterHit, hpDmg, absorbed) = hitPlayer(s, raw)
                s = afterHit
                val msg = buildString {
                    append("${s.enemy.kind.displayName} — ${skill.title} $raw (ignores Soften)")
                    if (absorbed > 0) append(" ($absorbed Brace)")
                }
                events += CombatEvent(
                    msg,
                    FloatingText("-$hpDmg", false),
                    sound = "impact",
                    glossaryHints = listOf(skill.glossaryKey, "nip", "soften") +
                        if (absorbed > 0) listOf("brace") else emptyList(),
                    braceAbsorbed = absorbed,
                    fxId = skill.id,
                    fxPlayer = false
                )
            }
            EnemySkillKind.DAMAGE -> {
                var raw = skill.rollDamage(rng)
                if (soften > 0) {
                    raw = (raw - soften).coerceAtLeast(1)
                    soften = 0
                }
                val (afterHit, hpDmg, absorbed) = hitPlayer(s, raw)
                s = afterHit.copy(counterPenalty = soften)
                val msg = buildString {
                    append("${s.enemy.kind.displayName} — ${skill.title} $raw")
                    if (absorbed > 0) append(" ($absorbed Brace)")
                }
                events += CombatEvent(
                    msg,
                    FloatingText("-$hpDmg", false),
                    sound = "impact",
                    glossaryHints = listOf(skill.glossaryKey) +
                        if (absorbed > 0) listOf("brace") else emptyList(),
                    braceAbsorbed = absorbed,
                    fxId = skill.id,
                    fxPlayer = false
                )
            }
        }

        if (s.playerHp <= 0) {
            events += CombatEvent("Defeat…", FloatingText("DOWN", false), sound = "miss")
            return s.copy(
                playerHp = 0,
                brace = 0,
                counterPenalty = 0,
                log = state.log + events,
                finished = true,
                playerWon = false,
                phase = CombatPhase.DEFEAT,
                highlightedId = null,
                enemyHighlightedId = skill.id,
                enemySpentIds = spent + skill.id,
                beat = CombatBeat.AWAITING_CONTINUE,
                round = state.round + 1
            )
        }
        return s.copy(
            log = state.log + events,
            highlightedId = null,
            beat = CombatBeat.AFTER_ENEMY,
            round = state.round + 1
        )
    }

    /** Advance to next round — clear unused Brace leftover per cards-v0. Enemy Brace persists. */
    fun readyNext(state: CombatState): CombatState =
        state.copy(beat = CombatBeat.READY, weaponFlashed = false, brace = 0, enemyHighlightedId = null)

    private fun finishVictory(state: CombatState): CombatState {
        val events = listOf(
            CombatEvent("Victory", FloatingText("WIN", true, true), CombatAnimStyle.CHARGE_SHAKE_SLOWMO, "legendary")
        )
        return state.copy(
            log = state.log + events,
            finished = true,
            playerWon = true,
            phase = CombatPhase.VICTORY,
            highlightedId = null,
            awaitingWeapon = false,
            beat = CombatBeat.AWAITING_CONTINUE
        )
    }

    /**
     * v0.1.61-continue win check — call every beat / frame.
     * if (allFoesHp <= 0 && phase != DEFEAT) phase = VICTORY
     */
    fun applyWinCheck(state: CombatState): CombatState {
        if (state.phase == CombatPhase.DEFEAT || state.playerHp <= 0) {
            return if (state.phase == CombatPhase.DEFEAT) state
            else state.copy(
                phase = CombatPhase.DEFEAT,
                finished = true,
                playerWon = false,
                beat = CombatBeat.AWAITING_CONTINUE
            )
        }
        if (state.enemy.hp <= 0 && state.phase != CombatPhase.DEFEAT) {
            if (state.phase == CombatPhase.VICTORY && state.finished && state.playerWon) {
                return state.copy(beat = CombatBeat.AWAITING_CONTINUE)
            }
            return forceVictory(state)
        }
        return state
    }

    /** Public force-win used by GC failsafe + mid-anim path. */
    fun forceVictory(state: CombatState): CombatState {
        if (state.playerHp <= 0) return state
        val hasVictory = state.log.any {
            it.message == "Victory" || it.message.startsWith("Victory")
        }
        val log = if (hasVictory) state.log else state.log + CombatEvent(
            "Victory",
            FloatingText("WIN", true, true),
            CombatAnimStyle.CHARGE_SHAKE_SLOWMO,
            "legendary"
        )
        return state.copy(
            log = log,
            finished = true,
            playerWon = true,
            phase = CombatPhase.VICTORY,
            highlightedId = null,
            awaitingWeapon = false,
            pendingFullWake = false,
            pendingSpark = false,
            beat = CombatBeat.AWAITING_CONTINUE
        )
    }

    /** Player Brace absorbs before HP. Returns (state, hpDamage, absorbed). */
    private fun hitPlayer(state: CombatState, rawDmg: Int): Triple<CombatState, Int, Int> {
        var dmg = rawDmg.coerceAtLeast(0)
        var brace = state.brace
        var absorbed = 0
        if (brace > 0 && dmg > 0) {
            absorbed = minOf(brace, dmg)
            brace -= absorbed
            dmg -= absorbed
        }
        val newHp = (state.playerHp - dmg).coerceAtLeast(0)
        return Triple(state.copy(playerHp = newHp, brace = brace), dmg, absorbed)
    }

    /** Enemy Brace absorbs player damage before enemy HP. */
    private data class EnemyDmgResult(val state: CombatState, val hpDamage: Int, val absorbed: Int)

    private fun applyDamageToEnemy(state: CombatState, raw: Int): EnemyDmgResult {
        var s = state
        var dmg = raw.coerceAtLeast(0)
        // v0.1.58 Troll Tooth: first damaging hit this floor vs elite OR boss +2 once
        if (s.trollToothReady && dmg > 0 && isToothTarget(s.enemy)) {
            dmg += 2
            s = s.copy(trollToothReady = false)
        }
        var eBrace = s.enemyBrace
        var absorbed = 0
        if (eBrace > 0 && dmg > 0) {
            absorbed = minOf(eBrace, dmg)
            eBrace -= absorbed
            dmg -= absorbed
        }
        val enemy = s.enemy.copy(hp = (s.enemy.hp - dmg).coerceAtLeast(0))
        return EnemyDmgResult(s.copy(enemy = enemy, enemyBrace = eBrace), dmg, absorbed)
    }

    private fun isToothTarget(enemy: Enemy): Boolean =
        enemy.isBoss || enemy.kind == EnemyKind.CAVE_TROLL

    private fun resolveCard(
        state: CombatState, card: Card, anim: CombatAnimStyle
    ): Triple<CombatState, List<CombatEvent>, Boolean> {
        val events = mutableListOf<CombatEvent>()
        var s = state
        val sound = "impact"
        var isAttack = false

        when (val e = card.effect) {
            is SkillEffect.Damage -> {
                isAttack = true
                val applied = applyDamageToEnemy(s, e.damage)
                s = applied.state
                events += CombatEvent(
                    "${card.title} deals ${e.damage}",
                    FloatingText("-${e.damage}", true, card.rarity == Rarity.RARE),
                    anim, sound,
                    fxId = card.id, fxPlayer = true
                )
            }
            is SkillEffect.DamageAndHeal -> {
                isAttack = true
                val applied = applyDamageToEnemy(s, e.damage)
                s = applied.state
                val nh = (s.playerHp + e.heal).coerceAtMost(s.playerMaxHp)
                events += CombatEvent(
                    "${card.title}: ${e.damage} dmg, +${e.heal} HP",
                    FloatingText("-${e.damage}", true), anim, sound,
                    fxId = card.id, fxPlayer = true
                )
                s = s.copy(playerHp = nh)
            }
            is SkillEffect.DamageAndBraceIfAshPips -> {
                isAttack = true
                val applied = applyDamageToEnemy(s, e.damage)
                s = applied.state
                events += CombatEvent(
                    "${card.title} deals ${e.damage}",
                    FloatingText("-${e.damage}", true), anim, sound,
                    fxId = card.id, fxPlayer = true
                )
                var brace = s.brace
                if (s.weapon.pipsFilled >= e.minPips) {
                    brace += e.brace
                    events += CombatEvent(
                        "Brace +${e.brace}",
                        FloatingText("BRACE", true), anim, "brace",
                        glossaryHints = listOf("brace"),
                        braceGained = e.brace
                    )
                }
                if (e.afterFireBrace > 0) {
                    brace += e.afterFireBrace
                    events += CombatEvent(
                        "Brace +${e.afterFireBrace}",
                        FloatingText("BRACE", true), anim, "brace",
                        glossaryHints = listOf("brace"),
                        braceGained = e.afterFireBrace
                    )
                }
                var soften = s.counterPenalty
                if (e.extraSoften > 0) {
                    soften += e.extraSoften
                    events += CombatEvent(
                        "Soften ${e.extraSoften}",
                        sound = "soften",
                        glossaryHints = listOf("soften"),
                        softenApplied = e.extraSoften
                    )
                }
                s = s.copy(brace = brace, counterPenalty = soften)
            }
            is SkillEffect.EmberPoolSkill -> {
                // Damage (+ Wake Echo bonus) only; Brace/Soften/Tithe after charge++ in resolveSkill
                isAttack = true
                var dmg = e.damage
                if (e.echoBonusIfWakeFired > 0 && s.fullProcThisCombat) {
                    dmg += e.echoBonusIfWakeFired
                }
                val applied = applyDamageToEnemy(s, dmg)
                s = applied.state
                events += CombatEvent(
                    "${card.title} deals $dmg",
                    FloatingText("-$dmg", true), anim, sound,
                    glossaryHints = listOf("spark", "wake", "brace", "soften").filter { t ->
                        card.effect.description.contains(t, ignoreCase = true)
                    },
                    fxId = card.id, fxPlayer = true
                )
                if (e.afterFireBrace > 0) {
                    s = s.copy(brace = s.brace + e.afterFireBrace)
                    events += CombatEvent(
                        "Brace +${e.afterFireBrace}",
                        FloatingText("BRACE", true), anim, "brace",
                        glossaryHints = listOf("brace"),
                        braceGained = e.afterFireBrace
                    )
                }
                if (e.extraSoften > 0) {
                    s = s.copy(counterPenalty = s.counterPenalty + e.extraSoften)
                    events += CombatEvent(
                        "Soften ${e.extraSoften}",
                        sound = "soften",
                        glossaryHints = listOf("soften"),
                        softenApplied = e.extraSoften
                    )
                }
            }
            is MoveEffect.DamageAndSoften -> {
                isAttack = true
                val applied = applyDamageToEnemy(s, e.damage)
                s = applied.state
                val brace = s.brace + e.braceGain
                events += CombatEvent(
                    "${card.title} deals ${e.damage}",
                    FloatingText("-${e.damage}", true), anim, sound,
                    fxId = card.id, fxPlayer = true
                )
                if (e.braceGain > 0) {
                    events += CombatEvent(
                        "Brace +${e.braceGain}",
                        FloatingText("BRACE", true), anim, "brace",
                        glossaryHints = listOf("brace"),
                        braceGained = e.braceGain
                    )
                }
                if (e.counterPenalty > 0) {
                    events += CombatEvent(
                        "Counter softened −${e.counterPenalty}",
                        sound = "soften",
                        glossaryHints = listOf("soften"),
                        softenApplied = e.counterPenalty
                    )
                }
                s = s.copy(brace = brace, counterPenalty = s.counterPenalty + e.counterPenalty)
            }
            is Equipment.GainBrace -> {
                isAttack = false
                s = s.copy(brace = s.brace + e.brace)
                events += CombatEvent(
                    "${card.title}: Brace ${e.brace}",
                    FloatingText("BRACE ${e.brace}", true), anim, "brace",
                    glossaryHints = listOf("brace"),
                    fxId = card.id, fxPlayer = true,
                    braceGained = e.brace
                )
                if (e.onBraceDeal > 0) {
                    val applied = applyDamageToEnemy(s, e.onBraceDeal)
                    s = applied.state
                    events += CombatEvent(
                        "On Brace: ${e.onBraceDeal} dmg",
                        FloatingText("-${e.onBraceDeal}", true), anim, "impact",
                        fxId = card.id, fxPlayer = true
                    )
                }
                if (e.afterFireSoften > 0) {
                    s = s.copy(counterPenalty = s.counterPenalty + e.afterFireSoften)
                    events += CombatEvent(
                        "Soften ${e.afterFireSoften}",
                        sound = "soften",
                        glossaryHints = listOf("soften"),
                        softenApplied = e.afterFireSoften
                    )
                }
            }
            is Equipment.HealOrBrace -> {
                isAttack = false
                if (s.playerHp >= s.playerMaxHp) {
                    s = s.copy(brace = s.brace + e.braceIfFull)
                    events += CombatEvent(
                        "${card.title}: Brace ${e.braceIfFull}",
                        FloatingText("BRACE", true), anim, "brace",
                        glossaryHints = listOf("brace"),
                        fxId = card.id, fxPlayer = true,
                        braceGained = e.braceIfFull
                    )
                } else {
                    val nh = (s.playerHp + e.heal).coerceAtMost(s.playerMaxHp)
                    s = s.copy(playerHp = nh)
                    events += CombatEvent(
                        "${card.title}: +${e.heal} HP",
                        FloatingText("+${e.heal}", true), anim, "",
                        fxId = card.id, fxPlayer = true
                    )
                }
            }
        }
        return Triple(s, events, isAttack)
    }

    private fun <T> pickWeighted(items: List<T>, weightOf: (T) -> Int): T? {
        if (items.isEmpty()) return null
        val total = items.sumOf { weightOf(it) }
        if (total <= 0) return items.last()
        var r = rng.nextInt(total)
        for (item in items) {
            r -= weightOf(item)
            if (r < 0) return item
        }
        return items.last()
    }
}
