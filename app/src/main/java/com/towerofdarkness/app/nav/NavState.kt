package com.towerofdarkness.app.nav

sealed class NavState {
    data object MainMenu : NavState()
    /** Title→Climb fullscreen intro (fresh only). Skip/end → Tutorial or Path. */
    data object ClimbIntro : NavState()
    data object Tutorial : NavState()
    data object Path : NavState()
    data object Loadout : NavState()
    data object Combat : NavState()
    data object Shop : NavState()
    data object Rest : NavState()
    data object Event : NavState()
    data object Treasure : NavState()
    /** After Floor 1 boss win — "The stair turns." before Floor 2 path. */
    data object FloorBreak : NavState()
    data object RunSummary : NavState()
    data object MetaHub : NavState()
    /** v0.1.64 — Assist specials row (stub title OK). */
    data object Settings : NavState()
}
