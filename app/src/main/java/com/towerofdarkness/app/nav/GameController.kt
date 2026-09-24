package com.towerofdarkness.app.nav

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.towerofdarkness.app.data.MetaStore
import com.towerofdarkness.app.data.MidRunAshbrand
import com.towerofdarkness.app.data.MidRunLoadout
import com.towerofdarkness.app.data.MidRunResume
import com.towerofdarkness.app.data.MidRunShopVisit
import com.towerofdarkness.app.data.MidRunSlot
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
import com.towerofdarkness.app.domain.path.RumorPools
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
) {
    /** Floor 2 clear is the only victory path; title locked for v0.1.9. */
    val title: String
        get() = if (won) "Victory — The seal breaks." else "Defeat"
}

enum class BossWinNav { FLOOR_BREAK, SUMMARY_VICTORY }

/** Snapshot of run state that must survive F1→F2 (no heal / no loadout unlock). */
data class FloorBreakPersist(
    val playerHp: Int,
    val runWallet: Int,
    val loadoutIds: List<String>,
    val loadoutLocked: Boolean,
    val weaponLevel: Int,
    val weaponCharge: Int,
    val unlockedThisRun: Set<String>
) {
    fun assertSurvives(after: FloorBreakPersist) {
        require(after.playerHp == playerHp) { "HP must persist across floor break" }
        require(after.runWallet == runWallet) { "wallet must persist" }
        require(after.loadoutIds == loadoutIds) { "loadout must persist" }
        require(after.loadoutLocked) { "loadout must stay locked" }
        require(after.weaponLevel == weaponLevel) { "Ashbrand level must persist" }
        require(after.weaponCharge == weaponCharge) { "Ashbrand charge must persist" }
        require(after.unlockedThisRun == unlockedThisRun) { "unlocks must persist" }
    }
}

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
    /** Combat playback rate: 1 or 2. Persists for the run (cheap). Label shows ACTIVE rate. */
    var combatSpeedX by mutableStateOf(1)
        private set
    /** Mid-run slot present (Menu Continue). */
    var hasMidRunSlot by mutableStateOf(false)
        private set
    /** Cold-start / meta load finished (avoids Menu flash before resume). */
    var midRunBootstrapped by mutableStateOf(false)
        private set
    private var runId: String = ""
    private var runRngSeed: Long = 0L
    private var runRemnantsEarned: Int = 0
    private var unlocksThisRun: Set<String> = emptySet()
    private var shopVisits: List<MidRunShopVisit> = emptyList()
    private var pathLayoutId: String = ""
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
    /** Treasure preview Lose (tapped card). Cleared on cancel; does not affect Gain. */
    var treasureSwapLoseId by mutableStateOf<String?>(null)
        private set
    /** Treasure Gain rolled once on node enter; stable for the whole visit. */
    var treasureSwapGainId by mutableStateOf<String?>(null)
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
            val slot = meta.readMidRunSlot()
            hasMidRunSlot = slot != null
            if (slot != null) {
                applyMidRunSlot(slot)
                nav = MidRunSlot.resumeNav(slot)
            }
            midRunBootstrapped = true
        }
        viewModelScope.launch { meta.tutorialSeen.collect { tutorialSeen = it } }
        viewModelScope.launch { meta.remnantsBank.collect { remnantsBank = it } }
        viewModelScope.launch { meta.unlockedCards.collect { unlockedCards = it } }
        viewModelScope.launch { meta.metaHpBonus.collect { metaHpBonus = it } }
    }

    fun showGlossary(term: String?) { glossaryTerm = term }

    /** Toggle combat pace 1x ↔ 2x. Applies to the NEXT hold beat (current delay already committed). */
    fun toggleCombatSpeed() {
        combatSpeedX = if (combatSpeedX == 1) 2 else 1
    }

    fun goMenu() { nav = NavState.MainMenu }

    fun goHub() {
        // Summary → Hub clears mid-run slot after bank (bank already in finishRun).
        if (nav == NavState.RunSummary || summary != null) {
            clearMidRunSlotAsync()
        }
        nav = NavState.MetaHub
    }

    /** Menu Continue — resume Path or FloorBreak from slot. */
    fun continueClimb() {
        viewModelScope.launch {
            val slot = meta.readMidRunSlot() ?: return@launch
            hasMidRunSlot = true
            applyMidRunSlot(slot)
            nav = MidRunSlot.resumeNav(slot)
        }
    }

    /**
     * Start a climb. If a mid-run slot exists, caller must confirm wipe via [confirmNewClimb].
     * Without a slot, behaves as legacy Climb.
     */
    fun climb() {
        if (hasMidRunSlot) return // UI shows confirm; use confirmNewClimb
        beginClimbFresh()
    }

    /** After New-climb confirm — wipe mid-run slot, then Tutorial/Path. Meta untouched. */
    fun confirmNewClimb() {
        viewModelScope.launch {
            meta.clearMidRunSlot()
            hasMidRunSlot = false
            beginClimbFresh()
        }
    }

    private fun beginClimbFresh() {
        if (!tutorialSeen) {
            tutorialStep = 0
            nav = NavState.Tutorial
        } else {
            startNewRun()
        }
    }

    fun startNewRun() {
        resetRunIdentity()
        path = PathGenerator.generate(1, Random(MidRunSlot.floorSeed(runRngSeed, 1)))
        pathLayoutId = "floor1_$runRngSeed"
        loadout = emptyList()
        // this-run weapon level resets; keep Ashbrand (or last selected def) at Lv1
        equippedWeapon = WeaponRuntime(equippedWeapon.def, level = 1, charge = 0)
        loadoutLocked = false
        pendingNodeId = null
        runWallet = 0
        runRemnantsEarned = 0
        playerHp = Balance.PLAYER_MAX_HP + metaHpBonus
        nodesCleared = 0
        weightHitchCardId = null
        unlocksThisRun = emptySet()
        shopVisits = emptyList()
        // scout_charge: +1 free Scout usable from Path (or Rest) each climb — observable
        freeScoutCharges = if ("scout_charge" in unlockedCards) 1 else 0
        rumorRerolls = if ("rumor_clarity" in unlockedCards) 1 else 0
        summary = null
        combatState = null
        clearMidRunSlotAsync()
        // slice-screens: Path first; Loadout via Edit or first resolve tap
        nav = NavState.Path
    }

    private fun resetRunIdentity() {
        runId = java.util.UUID.randomUUID().toString()
        runRngSeed = Random.Default.nextLong()
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
        resetRunIdentity()
        path = PathGenerator.generate(1, Random(MidRunSlot.floorSeed(runRngSeed, 1)))
        pathLayoutId = "floor1_$runRngSeed"
        loadoutLocked = false
        pendingNodeId = null
        runWallet = 0
        runRemnantsEarned = 0
        unlocksThisRun = emptySet()
        shopVisits = emptyList()
        playerHp = Balance.PLAYER_MAX_HP + metaHpBonus
        nodesCleared = 0
        weightHitchCardId = null
        freeScoutCharges = if ("scout_charge" in unlockedCards) 1 else 0
        rumorRerolls = if ("rumor_clarity" in unlockedCards) 1 else 0
        combatState = null
        clearMidRunSlotAsync()
        nav = NavState.Path
    }

    fun completeTutorial() {
        if (loadout.isEmpty()) {
            loadout = CardCatalog.defaultLoadoutIds.mapNotNull { CardCatalog.byId(it) }
        }
        equippedWeapon = WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0)
        viewModelScope.launch { meta.setTutorialSeen(true) }
        resetRunIdentity()
        path = PathGenerator.generate(1, Random(MidRunSlot.floorSeed(runRngSeed, 1)))
        pathLayoutId = "floor1_$runRngSeed"
        loadoutLocked = false
        pendingNodeId = null
        runWallet = 0
        runRemnantsEarned = 0
        unlocksThisRun = emptySet()
        shopVisits = emptyList()
        playerHp = Balance.PLAYER_MAX_HP + metaHpBonus
        nodesCleared = 0
        weightHitchCardId = null
        freeScoutCharges = if ("scout_charge" in unlockedCards) 1 else 0
        rumorRerolls = if ("rumor_clarity" in unlockedCards) 1 else 0
        combatState = null
        clearMidRunSlotAsync()
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
            // Loadout confirm on Path — persist selection (lock write follows first resolve leave).
            persistMidRun(pendingStair = false, resume = MidRunResume.Path)
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
        var justLocked = false
        if (!loadoutLocked && node.type != NodeType.START) {
            loadoutLocked = true // CoS: lock on first leave Path into resolve
            justLocked = true
        }
        if (justLocked) {
            persistMidRun(pendingStair = false, resume = MidRunResume.Path)
        }
        when (node.type) {
            NodeType.COMBAT, NodeType.BOSS -> startCombat(node.type == NodeType.BOSS)
            NodeType.SHOP -> openShop(nodeId)
            NodeType.REST -> nav = NavState.Rest
            NodeType.EVENT -> nav = NavState.Event
            NodeType.TREASURE -> {
                treasureSwapLoseId = null
                treasureSwapGainId = pickTreasureGainId(loadout.map { it.id }.toSet(), unlockedCards, rng)
                nav = NavState.Treasure
            }
            NodeType.START -> nav = NavState.Path
        }
    }

    // --- Combat ---
    private fun startCombat(boss: Boolean) {
        val floor = path?.floor ?: 1
        val enemy = if (boss) Enemy.boss(floor) else Enemy.forFloorCombat(nodesCleared, floor)
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
            combatHold(Balance.DICE_MS)
            // B — slot already highlighted

            // C — skill
            s = engine.resolveSkill(s)
            combatState = s
            s.log.lastOrNull()?.sound?.let { sound.play(it) }
            val skillMs = if (s.lastFiredCard?.rarity == Rarity.RARE ||
                s.lastFiredCard?.rarity == Rarity.LEGENDARY
            ) Balance.SKILL_RARE_MS else Balance.SKILL_COMMON_MS
            combatHold(skillMs)

            // D — read hold
            combatHold(Balance.READ_HOLD_MS)

            // E — weapon AFTER skill, BEFORE enemy; FULL Wake hold 2300ms @1x
            if (s.awaitingWeapon || s.pendingFullWake || s.pendingSpark) {
                val fullWake = s.pendingFullWake
                s = engine.resolveWeapon(s)
                combatState = s
                s.log.lastOrNull()?.sound?.let { sound.play(it) }
                combatHold(if (fullWake) Balance.WEAPON_FULL_HOLD_MS else Balance.WEAPON_HOLD_MS)
            }

            if (s.finished) break
            if (s.enemy.hp <= 0) {
                if (!s.finished) {
                    // Ensure win flags so weapon XP applies (Spinner → Warden Lv2)
                    s = s.copy(
                        finished = true,
                        playerWon = true,
                        beat = com.towerofdarkness.app.domain.combat.CombatBeat.AWAITING_CONTINUE
                    )
                    combatState = s
                }
                break
            }

            // F — enemy counter
            s = engine.resolveEnemy(s)
            combatState = s
            s.log.lastOrNull()?.sound?.let { sound.play(it) }
            combatHold(Balance.ENEMY_HOLD_MS)

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

    /**
     * Same-run weapon XP: FULL Wake this fight + win → +1 level (cap 3) before next node.
     * Uses combat win signal OR enemy dead with player alive (guards unfinished-break bug).
     */
    private fun applyWeaponLevelUp(s: com.towerofdarkness.app.domain.combat.CombatState) {
        val won = s.playerWon || (s.enemy.hp <= 0 && s.playerHp > 0)
        if (!won) {
            // Still clear charge carry into next fight presentation
            equippedWeapon = equippedWeapon.copy(charge = 0)
            return
        }
        val from = maxOf(equippedWeapon.level, s.weapon.level).coerceIn(1, 3)
        val shouldLevel = s.fullProcThisCombat || s.fullProcKilled
        val newLevel = if (shouldLevel) (from + 1).coerceAtMost(3) else from
        equippedWeapon = WeaponRuntime(
            def = s.weapon.def,
            level = newLevel,
            charge = 0
        )
    }

    /**
     * Hold for one combat beat at the ACTIVE speed.
     * Snapshots [combatSpeedX] when the hold starts so a mid-fight toggle
     * only affects the NEXT beat (current delay already committed).
     */
    private suspend fun combatHold(baseMs: Long) {
        delay(combatHoldMs(baseMs, combatSpeedX))
    }

    /** Pure helpers for unit tests — weapon XP, treasure Gain lock, shop wallet, floor2. */
    companion object {
        /** Scale a 1x hold by speedX (1 or 2 only). No 3x; no skip. */
        fun combatHoldMs(baseMs: Long, speedX: Int): Long {
            val x = if (speedX >= 2) 2 else 1
            return (baseMs / x).coerceAtLeast(1L)
        }

        /** Floor 1 boss win → stair beat; Floor 2+ boss win → run summary (no Floor 3). */
        fun afterBossWinNav(floor: Int): BossWinNav =
            if (floor < 2) BossWinNav.FLOOR_BREAK else BossWinNav.SUMMARY_VICTORY

        /** Rest Heal → MAX HP; Deep Breath never stacks past max. */
        fun applyRestHealToMax(maxHp: Int): Int = maxHp

        /**
         * Free Scout pure apply: reveal fogged node type only, spend 1 charge, no enter.
         * Returns null if charges=0 or node not fogged / not scoutable.
         */
        fun applyFreeScout(
            path: TowerPath,
            nodeId: String,
            charges: Int
        ): Pair<TowerPath, Int>? {
            if (charges <= 0) return null
            val node = path.nodes.find { it.id == nodeId } ?: return null
            if (node.revealed || node.scoutedTypeOnly) return null
            if (node.type == NodeType.START || node.type == NodeType.BOSS) return null
            return path.withReveal(nodeId, typeOnly = true) to (charges - 1)
        }

        /**
         * Rumor re-roll pure apply: replace rumor text on one still-fogged node, spend 1.
         * Separate wallet from Free Scout — does not reveal type. Returns null if charges=0
         * or node not eligible (revealed / scouted / START / BOSS).
         */
        fun applyRumorReroll(
            path: TowerPath,
            nodeId: String,
            charges: Int,
            newRumor: String
        ): Pair<TowerPath, Int>? {
            if (charges <= 0) return null
            val node = path.nodes.find { it.id == nodeId } ?: return null
            if (node.revealed || node.scoutedTypeOnly) return null
            if (node.type == NodeType.START || node.type == NodeType.BOSS) return null
            val updated = path.copy(
                nodes = path.nodes.map {
                    if (it.id == nodeId) it.copy(rumor = newRumor) else it
                }
            )
            return updated to (charges - 1)
        }

        enum class FoggedTapKind { SCOUT, RUMOR }

        data class FoggedTapResult(
            val path: TowerPath,
            val freeScoutCharges: Int,
            val rumorRerolls: Int,
            val kind: FoggedTapKind
        )

        /**
         * Single fogged-node tap dispatcher: Scout-first if scout charges > 0 and eligible,
         * else rumor re-roll if rumor charges > 0 and eligible, else null (no-op).
         * Never decrements the wrong wallet.
         */
        fun dispatchFoggedNodeTap(
            path: TowerPath,
            nodeId: String,
            freeScoutCharges: Int,
            rumorRerolls: Int,
            newRumor: String
        ): FoggedTapResult? {
            if (freeScoutCharges > 0) {
                val scouted = applyFreeScout(path, nodeId, freeScoutCharges)
                if (scouted != null) {
                    return FoggedTapResult(
                        path = scouted.first,
                        freeScoutCharges = scouted.second,
                        rumorRerolls = rumorRerolls,
                        kind = FoggedTapKind.SCOUT
                    )
                }
            }
            if (rumorRerolls > 0) {
                val rerolled = applyRumorReroll(path, nodeId, rumorRerolls, newRumor)
                if (rerolled != null) {
                    return FoggedTapResult(
                        path = rerolled.first,
                        freeScoutCharges = freeScoutCharges,
                        rumorRerolls = rerolled.second,
                        kind = FoggedTapKind.RUMOR
                    )
                }
            }
            return null
        }

        /** UI copy for rumor wallet (always reportable, including 0). */
        fun rumorRerollsLabel(charges: Int): String = "Rumor re-rolls left: $charges"

        fun isHealOfferKind(kind: String): Boolean =
            kind == "heal_small" || kind == "heal_mid" || kind == "heal_full"

        /** Wallet 0–2: hide grey stock; show empty copy + Leave only. */
        fun shopShowsEmptyState(wallet: Int): Boolean =
            wallet < Balance.SHOP_PRICE_MIN

        fun canBuyShopOffer(
            offer: ShopOffer,
            wallet: Int,
            playerHp: Int,
            maxHp: Int
        ): Boolean {
            if (offer.sold || wallet < offer.price) return false
            if (isHealOfferKind(offer.kind) && playerHp >= maxHp) return false
            return true
        }

        /**
         * Apply a successful shop purchase to wallet/HP (effects that need path/loadout
         * are applied by [buyOffer] after this gate). Returns null if rejected.
         */
        fun applyShopBuyWalletHp(
            offer: ShopOffer,
            wallet: Int,
            playerHp: Int,
            maxHp: Int
        ): Pair<Int, Int>? {
            if (!canBuyShopOffer(offer, wallet, playerHp, maxHp)) return null
            val newWallet = wallet - offer.price
            val newHp = when (offer.kind) {
                "heal_small" -> (playerHp + 8).coerceAtMost(maxHp)
                "heal_mid" -> (playerHp + 15).coerceAtMost(maxHp)
                "heal_full" -> maxHp
                else -> playerHp
            }
            return newWallet to newHp
        }

        fun shopCatalog(): List<ShopOffer> = listOf(
            ShopOffer("heal_small", "Small Heal (+8)", 3, "heal_small"),
            ShopOffer("rumor_peek", "Rumor Peek", 6, "rumor_peek"),
            ShopOffer("heal_mid", "Mid Heal (+15)", 10, "heal_mid"),
            ShopOffer("card_swap", "Card Swap", 12, "card_swap"),
            ShopOffer("heal_full", "Full Heal", 15, "heal_full"),
            ShopOffer("card_swap_plus", "Premium Swap", 14, "card_swap_plus")
        )

        /**
         * Roll 4 shop offers. If [wallet] ≥ 3, guarantee ≥1 offer priced ≤ wallet
         * (prefer Small Heal @ 3).
         */
        fun generateShopOffers(wallet: Int, seed: Int): List<ShopOffer> {
            val r = Random(seed)
            val catalog = shopCatalog()
            val picked = mutableListOf<ShopOffer>()
            val usedKinds = mutableSetOf<String>()
            var attempts = 0
            while (picked.size < 4 && attempts < 40) {
                attempts++
                val tier = r.nextFloat()
                val pool = when {
                    tier < 0.50f -> catalog.filter { it.price in 3..8 }
                    tier < 0.85f -> catalog.filter { it.price in 9..12 }
                    else -> catalog.filter { it.price in 13..15 }
                }.filter { it.kind !in usedKinds }
                val choice = pool.ifEmpty {
                    catalog.filter { it.kind !in usedKinds }
                }.ifEmpty { emptyList() }.randomOrNull(r) ?: break
                usedKinds += choice.kind
                picked += choice.copy(id = "${choice.kind}_${picked.size}")
            }
            // Anti-stall: ensure at least one cheap (≤8) offer
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
            // Wallet affordability: if wallet ≥ 3, ensure ≥1 offer priced ≤ wallet
            if (wallet >= Balance.SHOP_PRICE_MIN && picked.none { it.price <= wallet }) {
                val healSmall = catalog.first { it.kind == "heal_small" }
                if (picked.isNotEmpty()) {
                    usedKinds.remove(picked.last().kind)
                    picked[picked.lastIndex] = healSmall.copy(id = "heal_small_wallet")
                } else {
                    picked += healSmall.copy(id = "heal_small_wallet")
                }
            }
            return picked
        }

        fun nextWeaponLevelAfterFight(
            startLevel: Int,
            fullProcThisCombat: Boolean,
            fullProcKilled: Boolean,
            playerWon: Boolean
        ): Int {
            if (!playerWon) return startLevel.coerceIn(1, 3)
            val from = startLevel.coerceIn(1, 3)
            val should = fullProcThisCombat || fullProcKilled
            return if (should) (from + 1).coerceAtMost(3) else from
        }

        /**
         * Roll treasure Gain once for a visit. Prefer unlocked rares not in loadout;
         * else any unused pool card. Null if nothing available.
         */
        fun pickTreasureGainId(
            loadoutIds: Set<String>,
            unlockedCards: Set<String>,
            rng: Random
        ): String? {
            val pool = CardCatalog.poolForRun(unlockedCards).filter { it.id !in loadoutIds }
            val rares = CardCatalog.all.filter {
                it.rarity.name == "RARE" && (it.id in unlockedCards || it.unlockCost == 0) &&
                    it.id !in loadoutIds
            }
            return when {
                rares.isNotEmpty() -> rares.random(rng).id
                pool.isNotEmpty() -> pool.random(rng).id
                else -> null
            }
        }

        /**
         * Visit-scoped treasure swap state: [gainId] locked at enter;
         * [loseId] set by preview taps; cancel clears lose only.
         */
        data class TreasureVisit(
            val gainId: String?,
            val loseId: String? = null
        ) {
            fun beginPreview(loseCardId: String?, loadoutIds: List<String>, rng: Random): TreasureVisit {
                if (loadoutIds.isEmpty() || gainId == null) return this
                val lose = loseCardId?.takeIf { it in loadoutIds } ?: loadoutIds.random(rng)
                return copy(loseId = lose)
            }

            fun cancelPreview(): TreasureVisit = copy(loseId = null)
        }

        fun treasureVisitEnter(
            loadoutIds: Set<String>,
            unlockedCards: Set<String>,
            rng: Random
        ): TreasureVisit = TreasureVisit(gainId = pickTreasureGainId(loadoutIds, unlockedCards, rng))
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
        persistMidRun(pendingStair = false, resume = MidRunResume.Path)
    }

    private fun onCombatEnd(s: CombatState) {
        val boss = s.enemy.isBoss
        val floor = path?.floor ?: 1
        if (s.playerWon) {
            var gain = if (boss) Balance.BOSS_WIN_REMNANTS else Balance.COMBAT_WIN_REMNANTS
            if (boss && "boss_bonus_2" in unlockedCards) gain += 2
            gainRemnants(gain)
            if (boss) {
                path = path?.markCurrentCleared()
                when (afterBossWinNav(floor)) {
                    BossWinNav.FLOOR_BREAK -> {
                        combatState = null
                        nav = NavState.FloorBreak
                        // Dual write #1: pending stair → FloorBreak resume
                        persistMidRun(pendingStair = true, resume = MidRunResume.FloorBreak)
                    }
                    BossWinNav.SUMMARY_VICTORY -> finishRun(won = true)
                }
            } else {
                returnToPathAfterResolve()
            }
        } else {
            gainRemnants(if (boss) Balance.BOSS_LOSS_REMNANTS else Balance.COMBAT_LOSS_REMNANTS)
            path = path?.markCurrentCleared()
            finishRun(won = false)
        }
    }

    /**
     * Floor 1 Seal-Warden win → short beat then Floor 2 path (same run).
     * Does not heal, unlock loadout, or reset Ashbrand XP/level/charge.
     */
    fun continueAfterFloorBreak() {
        if (nav != NavState.FloorBreak) return
        // Persist: playerHp, runWallet, loadout, loadoutLocked, equippedWeapon, unlocked-this-run
        path = PathGenerator.generate(2, Random(MidRunSlot.floorSeed(runRngSeed, 2)))
        pathLayoutId = "floor2_$runRngSeed"
        pendingNodeId = null
        combatState = null
        treasureSwapLoseId = null
        treasureSwapGainId = null
        shopOffers = emptyList()
        summary = null
        nav = NavState.Path
        // Dual write #2: stair Continue → F2 path, clear pending
        persistMidRun(pendingStair = false, resume = MidRunResume.Path)
    }

    fun fleeGrayed(): Boolean = true // CoS: Flee grayed

    // --- Shop ---
    private fun openShop(nodeId: String) {
        val seed = (path?.floor ?: 1) * 31 + nodeId.hashCode()
        shopOffers = generateShopOffers(runWallet, seed)
        nav = NavState.Shop
    }

    fun buyOffer(offer: ShopOffer) {
        val maxHp = Balance.PLAYER_MAX_HP + metaHpBonus
        if (!canBuyShopOffer(offer, runWallet, playerHp, maxHp)) return
        runWallet -= offer.price
        shopOffers = shopOffers.map { if (it.id == offer.id) it.copy(sold = true) else it }
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

    fun leaveShop() {
        val nodeId = path?.currentId
        val floor = path?.floor ?: 1
        if (nodeId != null) {
            val sold = shopOffers.filter { it.sold }.map { it.id }
            val rest = shopVisits.filterNot { it.nodeId == nodeId && it.floor == floor }
            shopVisits = rest + MidRunShopVisit(nodeId, floor, sold)
        }
        returnToPathAfterResolve()
    }

    // --- Rest ---
    fun restHealAmount(): Int {
        val bonus = if ("rest_heal_plus" in unlockedCards) 4 else 0
        return Balance.REST_HEAL_AMOUNT + bonus
    }

    fun restHeal() {
        val maxHp = Balance.PLAYER_MAX_HP + metaHpBonus
        // v0.1.10-bossrest: Rest Heal fills to MAX; Deep Breath does not overheal
        playerHp = applyRestHealToMax(maxHp)
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

    /**
     * Keen Eye Free Scout — tap a fogged (?) node: reveal type, spend 1 charge, do not enter.
     * Clear Fog rumor re-roll is a separate wallet and must not call this.
     */
    fun useFreeScoutOn(nodeId: String): Boolean {
        val p = path ?: return false
        val result = applyFreeScout(p, nodeId, freeScoutCharges) ?: return false
        path = result.first
        freeScoutCharges = result.second
        sound.play("ui")
        return true
    }

    /** Path Free Scout button — spends one charge on the first fogged non-start/boss node. */
    fun useFreeScout(): Boolean {
        if (freeScoutCharges <= 0) return false
        val p = path ?: return false
        val target = p.nodes.firstOrNull {
            !it.revealed && !it.scoutedTypeOnly &&
                it.type != NodeType.START && it.type != NodeType.BOSS
        } ?: return false
        return useFreeScoutOn(target.id)
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

    /**
     * rumor_clarity: re-roll rumor text on one fogged node (separate wallet from Free Scout).
     * Spends rumorRerolls only; does not reveal type.
     */
    fun rerollRumor(nodeId: String): Boolean {
        val p = path ?: return false
        val node = p.nodes.find { it.id == nodeId } ?: return false
        val newRumor = RumorPools.forType(node.type, rng)
        val result = applyRumorReroll(p, nodeId, rumorRerolls, newRumor) ?: return false
        path = result.first
        rumorRerolls = result.second
        sound.play("ui")
        return true
    }

    /**
     * Single fogged-? tap path: Scout-first if freeScoutCharges > 0, else rumor re-roll
     * if rumorRerolls > 0, else no-op (no state change). Wallets never shared.
     */
    fun onFoggedNodeTap(nodeId: String): Boolean {
        val p = path ?: return false
        val node = p.nodes.find { it.id == nodeId } ?: return false
        val newRumor = RumorPools.forType(node.type, rng)
        val result = dispatchFoggedNodeTap(
            p, nodeId, freeScoutCharges, rumorRerolls, newRumor
        ) ?: return false
        path = result.path
        freeScoutCharges = result.freeScoutCharges
        rumorRerolls = result.rumorRerolls
        sound.play("ui")
        return true
    }

    fun hasLoadoutFlex(): Boolean = false // v0.1.3: skill cap 5; perk hidden
    fun hasPerk(id: String): Boolean = id in unlockedCards

    // --- Event ---
    fun eventChoice(remnants: Boolean) {
        if (remnants) {
            gainRemnants(Balance.EVENT_REMNANTS)
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
    /** Clears visit-scoped preview + stored Gain (node leave / consume). */
    private fun clearTreasureVisit() {
        treasureSwapLoseId = null
        treasureSwapGainId = null
    }

    fun treasureRemnants() {
        clearTreasureVisit()
        gainRemnants(Balance.TREASURE_REMNANTS)
        returnToPathAfterResolve()
    }

    fun treasureBeginSwap(loseCardId: String? = null) {
        if (loadout.isEmpty()) return
        // Gain is rolled once on treasure enter; never reroll while visiting.
        if (treasureSwapGainId == null) {
            treasureSwapGainId = pickTreasureGainId(loadout.map { it.id }.toSet(), unlockedCards, rng)
        }
        if (treasureSwapGainId == null) return
        val lose = loseCardId?.let { id -> loadout.find { it.id == id } } ?: loadout.random(rng)
        treasureSwapLoseId = lose.id
    }

    fun treasureConfirmSwap() {
        val loseId = treasureSwapLoseId ?: return
        val gainId = treasureSwapGainId ?: return
        val gain = CardCatalog.byId(gainId) ?: return
        if (loadout.none { it.id == loseId }) {
            treasureSwapLoseId = null
            return
        }
        // Unlock rare if offered from locked set
        if (gain.unlockCost > 0 && gain.id !in unlockedCards) {
            unlocksThisRun = unlocksThisRun + gain.id
            viewModelScope.launch { meta.unlockCard(gain.id) }
        }
        loadout = loadout.map { if (it.id == loseId) gain else it }
        clearTreasureVisit()
        sound.play("ui")
        returnToPathAfterResolve()
    }

    fun treasureCancelSwap() {
        // Keep stored Gain for this visit; only drop Lose preview.
        treasureSwapLoseId = null
    }

    @Deprecated("Use treasureBeginSwap / treasureConfirmSwap")
    fun treasureCardSwap() {
        treasureBeginSwap()
        if (treasureSwapGainId != null && treasureSwapLoseId != null) treasureConfirmSwap()
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
        clearMidRunSlotAsync()
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

    // --- Mid-run save (v0.1.12) ---
    private fun gainRemnants(amount: Int) {
        if (amount <= 0) return
        runWallet += amount
        runRemnantsEarned += amount
    }

    private fun clearMidRunSlotAsync() {
        hasMidRunSlot = false
        viewModelScope.launch { meta.clearMidRunSlot() }
    }

    private fun persistMidRun(pendingStair: Boolean, resume: MidRunResume) {
        val slot = buildMidRunSlot(pendingStair, resume) ?: return
        hasMidRunSlot = true
        viewModelScope.launch { meta.writeMidRunSlot(slot) }
    }

    private fun buildMidRunSlot(pendingStair: Boolean, resume: MidRunResume): MidRunSlot? {
        val p = path ?: return null
        if (runId.isEmpty()) {
            resetRunIdentity()
        }
        val layout = pathLayoutId.ifEmpty { "floor${p.floor}_$runRngSeed" }
        return MidRunSlot(
            runId = runId,
            rngSeed = runRngSeed,
            floor = p.floor,
            pendingStairContinue = pendingStair,
            path = MidRunSlot.fromPath(p, layout),
            playerHp = playerHp,
            playerMaxHp = Balance.PLAYER_MAX_HP + metaHpBonus,
            runWallet = runWallet,
            runRemnantsEarned = runRemnantsEarned,
            nodesCleared = nodesCleared,
            loadout = MidRunLoadout(
                locked = loadoutLocked,
                cardIds = loadout.map { it.id }
            ),
            ashbrand = MidRunAshbrand(
                weaponId = equippedWeapon.def.id,
                level = equippedWeapon.level,
                charge = equippedWeapon.charge
            ),
            freeScoutCharges = freeScoutCharges,
            rumorRerolls = rumorRerolls,
            combatSpeed2x = combatSpeedX >= 2,
            unlocksThisRun = unlocksThisRun.toList(),
            shopVisits = shopVisits,
            resume = resume
        )
    }

    private fun applyMidRunSlot(slot: MidRunSlot) {
        runId = slot.runId
        runRngSeed = slot.rngSeed
        pathLayoutId = slot.path.layoutId
        path = MidRunSlot.toTowerPath(slot.path)
        playerHp = slot.playerHp
        runWallet = slot.runWallet
        runRemnantsEarned = slot.runRemnantsEarned
        nodesCleared = slot.nodesCleared
        loadout = slot.loadout.cardIds.mapNotNull { CardCatalog.byId(it) }
        loadoutLocked = slot.loadout.locked
        val weaponDef = WeaponCatalog.byId(slot.ashbrand.weaponId) ?: WeaponCatalog.default()
        equippedWeapon = WeaponRuntime(
            def = weaponDef,
            level = slot.ashbrand.level.coerceIn(1, 3),
            charge = slot.ashbrand.charge.coerceAtLeast(0)
        )
        freeScoutCharges = slot.freeScoutCharges
        rumorRerolls = slot.rumorRerolls
        combatSpeedX = if (slot.combatSpeed2x) 2 else 1
        unlocksThisRun = slot.unlocksThisRun.toSet()
        shopVisits = slot.shopVisits
        pendingNodeId = null
        combatState = null
        combatJob?.cancel()
        combatJob = null
        treasureSwapLoseId = null
        treasureSwapGainId = null
        shopOffers = emptyList()
        summary = null
        weightHitchCardId = null
    }

    override fun onCleared() {
        combatJob?.cancel()
        sound.release()
        super.onCleared()
    }
}
