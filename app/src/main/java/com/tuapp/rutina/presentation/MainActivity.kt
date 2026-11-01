package com.tuapp.rutina.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.tuapp.rutina.presentation.components.CenteredMessage
import com.tuapp.rutina.presentation.screens.DaysScreen
import com.tuapp.rutina.presentation.screens.ExerciseDetailScreen
import com.tuapp.rutina.presentation.screens.ExercisesScreen
import com.tuapp.rutina.presentation.screens.ProgressionsScreen
import com.tuapp.rutina.presentation.screens.SettingsScreen
import com.tuapp.rutina.presentation.theme.RutinaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

private object Routes {
    const val PROGRESSIONS = "progressions"
    const val DAYS = "days/{p}"
    const val EXERCISES = "exercises/{p}/{d}"
    const val DETAIL = "detail/{p}/{d}/{e}"
    const val SETTINGS = "settings"

    fun days(p: Int) = "days/$p"
    fun exercises(p: Int, d: Int) = "exercises/$p/$d"
    fun detail(p: Int, d: Int, e: Int) = "detail/$p/$d/$e"
}

private fun NavBackStackEntry.intArg(name: String): Int =
    arguments?.getString(name)?.toIntOrNull() ?: -1

@Composable
fun WearApp(viewModel: RutinaViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberSwipeDismissableNavController()

    RutinaTheme {
        AppScaffold {
            SwipeDismissableNavHost(navController = navController, startDestination = Routes.PROGRESSIONS) {
                composable(Routes.PROGRESSIONS) {
                    when {
                        state.loading -> CenteredMessage("Cargando…")
                        state.loadError != null -> CenteredMessage(state.loadError!!)
                        else -> ProgressionsScreen(
                            progressions = state.progressions,
                            onProgressionClick = { navController.navigate(Routes.days(it)) },
                            onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                        )
                    }
                }
                composable(Routes.DAYS) { entry ->
                    val p = entry.intArg("p")
                    val progression = state.progressions.getOrNull(p)
                    if (progression == null) {
                        CenteredMessage("Progresión no encontrada")
                    } else {
                        DaysScreen(progression) { d -> navController.navigate(Routes.exercises(p, d)) }
                    }
                }
                composable(Routes.EXERCISES) { entry ->
                    val p = entry.intArg("p")
                    val d = entry.intArg("d")
                    val day = state.progressions.getOrNull(p)?.days?.getOrNull(d)
                    if (day == null) {
                        CenteredMessage("Día no encontrado")
                    } else {
                        ExercisesScreen(day) { e -> navController.navigate(Routes.detail(p, d, e)) }
                    }
                }
                composable(Routes.DETAIL) { entry ->
                    val day = state.progressions.getOrNull(entry.intArg("p"))?.days?.getOrNull(entry.intArg("d"))
                    if (day == null) {
                        CenteredMessage("Ejercicio no encontrado")
                    } else {
                        ExerciseDetailScreen(day, initialIndex = entry.intArg("e"))
                    }
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        lastUpdate = state.lastUpdate,
                        updateStatus = state.updateStatus,
                        onUpdateClick = viewModel::updateRoutine,
                    )
                }
            }
        }
    }
}
