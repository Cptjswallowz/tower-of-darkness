package com.towerofdarkness.app.domain.glossary

object Glossary {
    private const val BRACE_DEF =
        "Absorb damage before it reaches HP. Clears at round end if unused leftover."

    val terms: Map<String, String> = mapOf(
        "stun" to "Stunned foes skip their next counterattack.",
        "freeze" to "Frozen foes skip their next counterattack.",
        "brace" to BRACE_DEF,
        "soften" to "Reduces the enemy’s next damaging skill. Does not apply to Hide, Rust Guard, or Cinder Hide. Nip ignores Soften. Remaining reduce shows on the Soften pip; clears when a damaging skill resolves.",
        "remnants" to "Echoes of fallen climbers — spend them in the Hub.",
        "rumor" to "Vague hints about a path node — never exact stats.",
        "loadout" to "Your active bar of 5–6 cards; dice pick which fires each round.",
        "scout" to "Reveal one adjacent fogged node's type only.",
        // v0.1.32 enemy kit terms
        "shiv" to "A quick goblin stab.",
        "nip" to "Small cut. Soften does not reduce this hit.",
        "cleave" to "A heavy orc swing.",
        "hide" to BRACE_DEF,
        "seal pulse" to "A sealed strike from the Warden.",
        "rust guard" to BRACE_DEF,
        "coal slam" to "A coal-heavy slam from the Ash-Warden.",
        "cinder hide" to BRACE_DEF,
        "hit" to "A basic strike — the high-weight card in an enemy kit."
    )
    fun definition(term: String): String? =
        terms.entries.find { it.key.equals(term, ignoreCase = true) }?.value
}
