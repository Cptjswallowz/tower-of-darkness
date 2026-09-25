package com.towerofdarkness.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.nav.NavState
import com.towerofdarkness.app.ui.components.GlossaryDialog
import com.towerofdarkness.app.ui.screens.CombatScreen
import com.towerofdarkness.app.ui.screens.EventScreen
import com.towerofdarkness.app.ui.screens.FloorBreakScreen
import com.towerofdarkness.app.ui.screens.LoadoutScreen
import com.towerofdarkness.app.ui.screens.MainMenuScreen
import com.towerofdarkness.app.ui.screens.MetaHubScreen
import com.towerofdarkness.app.ui.screens.PathScreen
import com.towerofdarkness.app.ui.screens.RestScreen
import com.towerofdarkness.app.ui.screens.RunSummaryScreen
import com.towerofdarkness.app.ui.screens.ShopScreen
import com.towerofdarkness.app.ui.screens.TreasureScreen
import com.towerofdarkness.app.ui.screens.TutorialScreen
import com.towerofdarkness.app.ui.theme.TowerTheme
import com.towerofdarkness.app.ui.theme.VoidBg

class MainActivity : ComponentActivity() {
    private val gc: GameController by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            TowerTheme {
                // Background fills edge-to-edge; content respects status/nav/cutout insets
                Surface(Modifier.fillMaxSize(), color = VoidBg) {
                    Surface(
                        Modifier
                            .fillMaxSize()
                            .safeDrawingPadding(),
                        color = VoidBg
                    ) {
                        TowerRoot(gc)
                    }
                }
            }
        }
    }
}

@Composable
fun TowerRoot(gc: GameController) {
    if (!gc.midRunBootstrapped) {
        // Wait for DataStore mid-run read so Continue/cold-start does not flash Menu.
        return
    }
    when (gc.nav) {
        NavState.MainMenu -> MainMenuScreen(gc)
        NavState.Tutorial -> TutorialScreen(gc)
        NavState.Path -> PathScreen(gc)
        NavState.Loadout -> LoadoutScreen(gc)
        NavState.Combat -> CombatScreen(gc)
        NavState.Shop -> ShopScreen(gc)
        NavState.Rest -> RestScreen(gc)
        NavState.Event -> EventScreen(gc)
        NavState.Treasure -> TreasureScreen(gc)
        NavState.FloorBreak -> FloorBreakScreen(gc)
        NavState.RunSummary -> RunSummaryScreen(gc)
        NavState.MetaHub -> MetaHubScreen(gc)
    }
    GlossaryDialog(
        term = gc.glossaryTerm,
        onDismiss = { gc.showGlossary(null) },
        onTerm = { gc.showGlossary(it) }
    )
}
