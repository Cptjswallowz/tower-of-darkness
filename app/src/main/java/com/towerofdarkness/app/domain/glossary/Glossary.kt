package com.towerofdarkness.app.domain.glossary

object Glossary {
    val terms: Map<String, String> = mapOf(
        "stun" to "Stunned foes skip their next counterattack.",
        "freeze" to "Frozen foes skip their next counterattack.",
        "brace" to "Absorb damage before it reaches HP. Clears at round end if unused leftover.",
        "remnants" to "Echoes of fallen climbers — spend them in the Hub.",
        "rumor" to "Vague hints about a path node — never exact stats.",
        "loadout" to "Your active bar of 5–6 cards; dice pick which fires each round.",
        "scout" to "Reveal one adjacent fogged node's type only."
    )
    fun definition(term: String): String? =
        terms.entries.find { it.key.equals(term, ignoreCase = true) }?.value
}
