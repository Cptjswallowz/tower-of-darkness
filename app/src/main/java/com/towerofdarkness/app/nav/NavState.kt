package com.towerofdarkness.app.nav

sealed class NavState {
    data object MainMenu : NavState()
    data object Tutorial : NavState()
    data object Path : NavState()
    data object Loadout : NavState()
    data object Combat : NavState()
    data object Shop : NavState()
    data object Rest : NavState()
    data object Event : NavState()
    data object Treasure : NavState()
    data object RunSummary : NavState()
    data object MetaHub : NavState()
}
