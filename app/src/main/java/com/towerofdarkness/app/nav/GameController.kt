package com.towerofdarkness.app.nav

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.towerofdarkness.app.data.MetaStore
import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.cards.Card
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.CombatBeat
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.domain.combat.CombatState
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.path.NodeType
import com.towerofdarkness.app.domain.path.PathGenerator
import com.towerofdarkness.app.domain.path.TowerPath
import com.towerofdarkness.app.domain.sound.AssetSoundBus
import com.towerofdarkness.app.domain.sound.SoundBus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ShopOffer(
    val id: String,
    val title: String,
    val price: Int,
    val kind: String, // heal_small, heal_mid, heal_full, rumor_peek, card_swap
    val sold: Boolean = false
)

data class RunSummaryData(
    val won: Boolean,
    val nodesCleared: Int,
    val remnantsEarned: Int,
    val floorReached: Int,
    val nearMiss: Boolean
)

class GameController(app: Application) : AndroidViewModel(app) {
    val meta = MetaStore(app)
    val sound: SoundBus = AssetSoundBus(app)

    var nav by mutableStateOf<NavState>(NavState.MainMenu)
        private set

    var tutorialSeen by mutableStateOf(false)
        private set
    var remnantsBank by mutableStateOf(0)
        private set
    var unlockedCards by mutableStateOf(CardCatalog.starterUnlockedIds())
        private set
    var metaHpBonus by mutableStateOf(0)
        private set

    // Run state
    var path by mutableStateOf<TowerPath?>(null)
        private set
    var loadout by mutableStateOf<List<Card>>(emptyList())
        private set
    var equippedWeapon by mutableStateOf(WeaponRuntime(WeaponCatalog.default()))
        private set
    var loadoutLocked by mutableStateOf(false)
        private set
    var pendingNodeId by mutableStateOf<String?>(null)
        private set
    var runWallet by mutableStateOf(0)
        private set
    var playerHp by mutableStateOf(Balance.PLAYER_MAX_HP)
        private set
    var nodesCleared by mutableStateOf(0)
        private set
    var combatState by mutableStateOf<CombatState?>(null)
        private set
    var shopOffers by mutableStateOf<List<ShopOffer>>(emptyList())
        private set
    var summary by mutableStateOf<RunSummaryData?>(null)
        private set
    var glossaryTerm by mutableStateOf<String?>(null)
        private set
    var weightHitchCardId by mutableStateOf<String?>(null)
        private set
    var freeScoutCharges by mutableStateOf(0)
        private set
    var rumorRerolls by mutableStateOf(0)
        private set

    // Tutorial progress
    var tutorialStep by mutableStateOf(0) // 0 rumor, 1 path, 2 loadout, 3 combat
        private set

    private val engine = CombatEngine()
    private var combatJob: Job? = null
    private val rng = Random.Default

    init {
        viewModelScope.launch {
            tutorialSeen = meta.tutorialSeen.first()
            remnantsBank = meta.remnantsBank.first()
            unlockedCards = meta.unlockedCards.first()
            metaHpBonus = meta.metaHpBonus.first()
        }
        viewModelScope.launch { meta.tutorialSeen.collect { tutorialSeen = it } }
        viewModelScope.launch { meta.remnantsBank.collect { remnantsBank = it } }
        viewModelScope.launch { meta.unlockedCards.collect { unlockedCards = it } }
        viewModelScope.launch { meta.metaHpBonus.collect { metaHpBonus = it } }
    }

    fun showGlossary(term: String?) { glossaryTerm = term }

    fun goMenu() { nav = NavState.MainMenu }
    fun goHub() { nav = NavState.MetaHub }

    fun climb() {
        if (!tutorialSeen) {
            tutorialStep = 0
            nav = NavState.Tutorial
        } else {
            startNewRun()
        }
    }

    fun startNewRun() {
        path = PathGenerator.generate(1, rng)
        loadout = emptyList()
        // this-run weapon level resets; keep Ashbrand (or last selected def) at Lv1
        equippedWeapon = WeaponRuntime(equippedWeapon.def, level = 1, charge = 0)
        loadoutLocked = false
        pendingNodeId = null
        runWallet = 0
        playerHp = Balance.PLAYER_MAX_HP + metaHpBonus
        nodesCleared = 0
        weightHitchCardId = null
        // scout_charge: +1 free Scout usable from Path (or Rest) each climb — observable
        freeScoutCharges = if ("scout_charge" in unlockedCards) 1 else 0
        rumorRerolls = if ("rumor_clarity" in unlockedCards) 1 else 0
        summary = null
        combatState = null
        // slice-screens: Path first; Loadout via Edit or first resolve tap
        nav = NavState.Path
    }

    // --- Tutorial ---
    fun tutorialNext() {
        if (tutorialStep < 3) tutorialStep++
    }

    fun tutorialOnLoadoutConfirmed(selected: List<Card>) {
        loadout = selected
        tutorialStep = maxOf(tutorialStep, 3)
    }


    fun skipTutorial() {
        if (!skipAllowed()) return
        // Docs: Skip ALWAYS forces default cards 1–5 + Ashbrand
        loadout = CardCatalog.defaultLoadoutIds.mapNotNull { CardCatalog.byId(it) }
        equippedWeapon = WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0)
        viewModelScope.launch { meta.setTutorialSeen(true) }
        path = PathGenerator.generate(1, rng)
        loadoutLocked = false
        pendingNodeId = null
        runWallet = 0
        playerHp = Balance.PLAYER_MAX_HP + metaHpBonus
        nodesCleared = 0
        weightHitchCardId = null
        freeScoutCharges = if ("scout_charge" in unlockedCards) 1 else 0
        rumorRerolls = if ("rumor_clarity" in unlockedCards) 1 else 0
        combatState = null
        nav = NavState.Path
    }

    fun completeTutorial() {
        if (loadout.isEmpty()) {
            loadout = CardCatalog.defaultLoadoutIds.mapNotNull { CardCatalog.byId(it) }
        }
        equippedWeapon = WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0)
        viewModelScope.launch { meta.setTutorialSeen(true) }
        path = PathGenerator.generate(1, rng)
        loadoutLocked = false
        pendingNodeId = null
        runWallet = 0
        playerHp = Balance.PLAYER_MAX_HP + metaHpBonus
        nodesCleared = 0
        weightHitchCardId = null
        freeScoutCharges = if ("scout_charge" in unlockedCards) 1 else 0
        rumorRerolls = if ("rumor_clarity" in unlockedCards) 1 else 0
        combatState = null
        nav = NavState.Path
    }

    // Fix canSkip: rumor seen (step>=1) AND loadout done
    /** Skip after rumor + loadout beats seen (step>=2). Skip forces default 5 regardless of current picks. */
    fun skipAllowed(): Boolean = tutorialStep >= 2

    // --- Loadout ---
    fun openLoadout() {
        if (!loadoutLocked) nav = NavState.Loadout
    }

    fun confirmLoadout(selected: List<Card>, weaponId: String? = null) {
        if (selected.size != Balance.LOADOUT_MAX) return
        val w = weaponId?.let { WeaponCatalog.byId(it) } ?: equippedWeapon.def
        loadout = selected
        equippedWeapon = WeaponRuntime(w, level = equippedWeapon.level.coerceIn(1, 3), charge = 0)
        sound.play("ui")
        if (nav == NavState.Tutorial || tutorialStep == 2) {
            tutorialStep = 3
            // stay in tutorial until complete/skip
            if (nav == NavState.Tutorial) return
        }
        val pending = pendingNodeId
        if (pending != null && path != null) {
            enterNode(pending)
        } else {
            nav = NavState.Path
        }
    }

    // --- Path ---
    fun selectPathNode(nodeId: String) {
        val p = path ?: return
        val choices = p.choices().map { it.id }
        if (nodeId !in choices && p.currentId != nodeId) {
            // allow only adjacent choices
            if (nodeId !in p.edges.filter { it.from == p.currentId }.map { it.to }) return
        }
        if (!loadoutLocked && (loadout.size != Balance.LOADOUT_MAX)) {
            pendingNodeId = nodeId
            nav = NavState.Loadout
            return
        }
        if (!loadoutLocked && loadout.isNotEmpty()) {
            // first leave into resolve → will lock inside enterNode
        }
        enterNode(nodeId)
    }

    private fun enterNode(nodeId: String) {
        val p = path ?: return
        val node = p.node(nodeId)
        path = p.moveTo(nodeId)
        pendingNodeId = null
        if (!loadoutLocked && node.type != NodeType.START) {
            loadoutLocked = true // CoS: lock on first leave Path into resolve
        }
        when (node.type) {
            NodeType.COMBAT, NodeType.BOSS -> startCombat(node.type == NodeType.BOSS)
            NodeType.SHOP -> openShop(nodeId)
            NodeType.REST -> nav = NavState.Rest
            NodeType.EVENT -> nav = NavState.Event
            NodeType.TREASURE -> nav = NavState.Treasure
            NodeType.START -> nav = NavState.Path
        }
    }

    // --- Combat ---
    private fun startCombat(boss: Boolean) {
        val enemy = if (boss) Enemy.boss() else Enemy.forFloorCombat(nodesCleared)
        val cards = effectiveLoadout()
        val maxHp = Balance.PLAYER_MAX_HP + metaHpBonus
        combatState = engine.start(
            activeCards = cards,
            enemy = enemy,
            weapon = equippedWeapon.copy(charge = 0),
            maxHp = maxHp,
            playerHp = playerHp.coerceAtMost(maxHp)
        )
        nav = NavState.Combat
        sound.play("dice")
        combatJob?.cancel()
        combatJob = viewModelScope.launch { runCombatBeats() }
    }

    private suspend fun runCombatBeats() {
        var s = combatState ?: return
        while (!s.finished) {
            // A — dice tumble
            s = engine.diceTumble(s)
            combatState = s
            s.log.lastOrNull()?.sound?.let { sound.play(it) }
            delay(Balance.DICE_MS)
            // B — slot already highlighted

            // C — skill
            s = engine.resolveSkill(s)
            combatState = s
            s.log.lastOrNull()?.sound?.let { sound.play(it) }
            val skillMs = if (s.lastFiredCard?.rarity == Rarity.RARE ||
                s.lastFiredCard?.rarity == Rarity.LEGENDARY
            ) Balance.SKILL_RARE_MS else Balance.SKILL_COMMON_MS
            delay(skillMs)

            // D — read hold
            delay(Balance.READ_HOLD_MS)

            // E — weapon AFTER skill, BEFORE enemy; FULL Wake hold 2300ms
            if (s.awaitingWeapon || s.pendingFullWake || s.pendingSpark) {
                val fullWake = s.pendingFullWake
                s = engine.resolveWeapon(s)
                combatState = s
                s.log.lastOrNull()?.sound?.let { sound.play(it) }
                delay(if (fullWake) Balance.WEAPON_FULL_HOLD_MS else Balance.WEAPON_HOLD_MS)
            }

            if (s.finished) break
            if (s.enemy.hp <= 0) break

            // F — enemy counter
            s = engine.resolveEnemy(s)
            combatState = s
            s.log.lastOrNull()?.sound?.let { sound.play(it) }
            delay(Balance.ENEMY_HOLD_MS)

            if (s.finished) break
            s = engine.readyNext(s)
            combatState = s
        }
        playerHp = s.playerHp
        // Apply this-run weapon level-up rules before Continue
        applyWeaponLevelUp(s)
        combatState = s.copy(beat = CombatBeat.AWAITING_CONTINUE)
        // Wait for Continue — do not auto-nav
    }

    private fun applyWeaponLevelUp(s: com.towerofdarkness.app.domain.combat.CombatState) {
        if (!s.playerWon) return
        val w = s.weapon
        val shouldLevel = s.fullProcKilled || s.fullProcThisCombat
        if (shouldLevel && w.level < 3) {
            equippedWeapon = w.copy(level = w.level + 1, charge = 0)
        } else {
            equippedWeapon = w.copy(charge = 0)
        }
    }

    fun continueAfterCombat() {
        val s = combatState ?: return
        if (!s.finished) return
        onCombatEnd(s)
    }

    private fun effectiveLoadout(): List<Card> {
        val hitch = weightHitchCardId
        return loadout.map { c ->
            if (c.id == hitch) c.copy(weight = (c.weight - 1).coerceAtLeast(1)) else c
        }
    }


    private fun returnToPathAfterResolve() {
        path = path?.markCurrentCleared()
        nodesCleared++
        nav = NavState.Path
    }

    private fun onCombatEnd(s: CombatState) {
        val boss = s.enemy.isBoss
        if (s.playerWon) {
            var gain = if (boss) Balance.BOSS_WIN_REMNANTS else Balance.COMBAT_WIN_REMNANTS
            if (boss && "boss_bonus_2" in unlockedCards) gain += 2
            runWallet += gain
            if (boss) {
                path = path?.markCurrentCleared()
                finishRun(won = true)
            } else {
                returnToPathAfterResolve()
            }
        } else {
            runWallet += if (boss) Balance.BOSS_LOSS_REMNANTS else Balance.COMBAT_LOSS_REMNANTS
            path = path?.markCurrentCleared()
            finishRun(won = false)
        }
    }

    fun fleeGrayed(): Boolean = true // CoS: Flee grayed

    // --- Shop ---
    private fun openShop(nodeId: String) {
        val seed = (path?.floor ?: 1) * 31 + nodeId.hashCode()
        val r = Random(seed)
        val catalog = listOf(
            ShopOffer("heal_small", "Small Heal (+8)", 5, "heal_small"),
            ShopOffer("rumor_peek", "Rumor Peek", 6, "rumor_peek"),
            ShopOffer("heal_mid", "Mid Heal (+15)", 10, "heal_mid"),
            ShopOffer("card_swap", "Card Swap", 12, "card_swap"),
            ShopOffer("heal_full", "Full Heal", 15, "heal_full"),
            ShopOffer("card_swap_plus", "Premium Swap", 14, "card_swap_plus")
        )
        val picked = mutableListOf<ShopOffer>()
        val usedKinds = mutableSetOf<String>()
        var attempts = 0
        while (picked.size < 4 && attempts < 40) {
            attempts++
            val tier = r.nextFloat()
            val pool = when {
                tier < 0.50f -> catalog.filter { it.price in 5..8 }
                tier < 0.85f -> catalog.filter { it.price in 9..12 }
                else -> catalog.filter { it.price in 13..15 }
            }.filter { it.kind !in usedKinds }
            val choice = pool.ifEmpty {
                catalog.filter { it.kind !in usedKinds }
            }.ifEmpty { emptyList() }.randomOrNull(r) ?: break
            usedKinds += choice.kind
            picked += choice.copy(id = "${choice.kind}_${picked.size}")
        }
        // Anti-stall: ensure at least one cheap (≤8) offer; replace last if needed without duplicating kind
        if (picked.none { it.price <= 8 }) {
            val cheap = catalog.filter { it.price <= 8 && it.kind !in usedKinds.minus(picked.lastOrNull()?.kind) }
                .ifEmpty { catalog.filter { it.price <= 8 } }
                .first()
            if (picked.isNotEmpty()) {
                usedKinds.remove(picked.last().kind)
                picked[picked.lastIndex] = cheap.copy(id = "${cheap.kind}_forced")
                usedKinds += cheap.kind
            } else {
                picked += cheap.copy(id = "${cheap.kind}_forced")
            }
        }
        shopOffers = picked
        nav = NavState.Shop
    }

    fun buyOffer(offer: ShopOffer) {
        if (offer.sold || runWallet < offer.price) return
        runWallet -= offer.price
        shopOffers = shopOffers.map { if (it.id == offer.id) it.copy(sold = true) else it }
        val maxHp = Balance.PLAYER_MAX_HP + metaHpBonus
        when (offer.kind) {
            "heal_small" -> playerHp = (playerHp + 8).coerceAtMost(maxHp)
            "heal_mid" -> playerHp = (playerHp + 15).coerceAtMost(maxHp)
            "heal_full" -> playerHp = maxHp
            "rumor_peek" -> scoutAdjacent()
            "card_swap", "card_swap_plus" -> { /* UI handles swap sheet simply: auto-swap last */ autoSwap() }
        }
        sound.play("ui")
    }

    private fun autoSwap() {
        val pool = CardCatalog.poolForRun(unlockedCards).filter { c -> loadout.none { it.id == c.id } }
        if (pool.isEmpty() || loadout.isEmpty()) return
        val out = loadout.last()
        val inn = pool.random(rng)
        loadout = loadout.dropLast(1) + inn
    }

    fun leaveShop() { returnToPathAfterResolve() }

    // --- Rest ---
    fun restHealAmount(): Int {
        val bonus = if ("rest_heal_plus" in unlockedCards) 4 else 0
        return Balance.REST_HEAL_AMOUNT + bonus
    }

    fun restHeal() {
        val maxHp = Balance.PLAYER_MAX_HP + metaHpBonus
        playerHp = (playerHp + restHealAmount()).coerceAtMost(maxHp)
        returnToPathAfterResolve()
    }

    fun restScout() {
        scoutAdjacent()
        returnToPathAfterResolve()
    }

    /** Leave Rest without Heal/Scout. */
    fun leaveRest() {
        returnToPathAfterResolve()
    }

    /** Path free scout from scout_charge — spends one charge when a fogged adjacent exists. */
    fun useFreeScout(): Boolean {
        if (freeScoutCharges <= 0) return false
        val p = path ?: return false
        val fogged = p.edges.filter { it.from == p.currentId }
            .map { p.node(it.to) }
            .filter { !it.revealed && !it.scoutedTypeOnly }
        val target = fogged.firstOrNull() ?: return false
        path = p.withReveal(target.id, typeOnly = true)
        freeScoutCharges--
        sound.play("ui")
        return true
    }

    private fun scoutAdjacent() {
        val p = path ?: return
        val fogged = p.edges.filter { it.from == p.currentId }
            .map { p.node(it.to) }
            .filter { !it.revealed && !it.scoutedTypeOnly }
        val target = fogged.firstOrNull() ?: return
        // CoS: Scout reveals node type only
        path = p.withReveal(target.id, typeOnly = true)
    }

    /** rumor_clarity: re-roll rumor text on one fogged node (once per climb). */
    fun rerollRumor(nodeId: String): Boolean {
        if (rumorRerolls <= 0) return false
        val p = path ?: return false
        val node = p.nodes.find { it.id == nodeId } ?: return false
        if (node.revealed || node.type == com.towerofdarkness.app.domain.path.NodeType.START) return false
        val newRumor = com.towerofdarkness.app.domain.path.RumorPools.forType(node.type, rng)
        path = p.copy(nodes = p.nodes.map {
            if (it.id == nodeId) it.copy(rumor = newRumor) else it
        })
        rumorRerolls--
        sound.play("ui")
        return true
    }

    fun hasLoadoutFlex(): Boolean = false // v0.1.3: skill cap 5; perk hidden
    fun hasPerk(id: String): Boolean = id in unlockedCards

    // --- Event ---
    fun eventChoice(remnants: Boolean) {
        if (remnants) {
            runWallet += Balance.EVENT_REMNANTS
        } else {
            // B — Heal 8, or take 4 damage (never hitch-only)
            if (rng.nextBoolean()) {
                playerHp = (playerHp + 8).coerceAtMost(Balance.PLAYER_MAX_HP + metaHpBonus)
            } else {
                playerHp = (playerHp - 4).coerceAtLeast(1)
            }
        }
        returnToPathAfterResolve()
    }

    // --- Treasure ---
    fun treasureRemnants() {
        runWallet += Balance.TREASURE_REMNANTS
        returnToPathAfterResolve()
    }

    fun treasureCardSwap() {
        // Offer rare if not owned — CoS: rares from Treasure and Hub
        val rares = CardCatalog.all.filter { it.rarity.name == "RARE" && it.id !in unlockedCards }
        if (rares.isNotEmpty()) {
            val rare = rares.random(rng)
            viewModelScope.launch { meta.unlockCard(rare.id) }
        }
        autoSwap()
        returnToPathAfterResolve()
    }

    // --- Summary / Hub ---
    private fun finishRun(won: Boolean) {
        val earned = runWallet
        // Near-miss only if boss reached AND boss HP remaining ≤ 8
        val bossFight = combatState?.enemy?.isBoss == true
        val bossHpLeft = combatState?.enemy?.hp ?: 999
        val near = !won && bossFight && bossHpLeft <= 8
        summary = RunSummaryData(won, nodesCleared, earned, path?.floor ?: 1, near)
        viewModelScope.launch { meta.addRemnants(earned) }
        runWallet = 0
        nav = NavState.RunSummary
    }

    fun hubUnlock(cardId: String) {
        val card = CardCatalog.byId(cardId) ?: return
        val cost = card.unlockCost.coerceAtLeast(Balance.CHEAPEST_CARD_UNLOCK)
        viewModelScope.launch {
            if (meta.spendRemnants(cost)) {
                meta.unlockCard(cardId)
                sound.play("ui")
            }
        }
    }

    fun hubUnlockMetaHp() {
        viewModelScope.launch {
            if (metaHpBonus == 0 && meta.spendRemnants(15)) {
                meta.setMetaHpBonus(2)
                sound.play("ui")
            }
        }
    }

    /** Persist scout_charge perk in unlockedCards set (id "scout_charge"). */
    fun hubUnlockScoutCharge() {
        viewModelScope.launch {
            if ("scout_charge" !in unlockedCards && meta.spendRemnants(20)) {
                meta.unlockCard("scout_charge")
                sound.play("ui")
            }
        }
    }

    fun hubUnlockPerk(perkId: String, cost: Int) {
        viewModelScope.launch {
            if (perkId !in unlockedCards && meta.spendRemnants(cost)) {
                meta.unlockCard(perkId)
                sound.play("ui")
            }
        }
    }

    override fun onCleared() {
        combatJob?.cancel()
        sound.release()
        super.onCleared()
    }
}
