package com.francescodf.sequenzatimer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.francescodf.sequenzatimer.data.repository.SequenceRepository
import com.francescodf.sequenzatimer.ui.screens.editor.EditorScreen
import com.francescodf.sequenzatimer.ui.screens.home.HomeScreen
import com.francescodf.sequenzatimer.ui.screens.playback.PlaybackScreen

/**
 * Guards against double-tap navigation: only navigates if the current
 * lifecycle state is RESUMED (i.e., the current screen is fully visible).
 */
private fun NavController.safeNavigate(route: Route) {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        navigate(route)
    }
}

@Composable
fun AppNavigation(repository: SequenceRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.Home) {
        composable<Route.Home> {
            HomeScreen(
                repository = repository,
                onCreateNew = { navController.safeNavigate(Route.Editor()) },
                onEditSequence = { id -> navController.safeNavigate(Route.Editor(sequenceId = id)) },
                onPlaySequence = { id -> navController.safeNavigate(Route.Playback(sequenceId = id)) }
            )
        }
        composable<Route.Editor> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Editor>()
            EditorScreen(
                repository = repository,
                sequenceId = if (route.sequenceId == -1L) null else route.sequenceId,
                onNavigateBack = { navController.popBackStack() },
                onSaveAndPlay = { id ->
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.navigate(Route.Playback(sequenceId = id)) {
                            popUpTo<Route.Home> { inclusive = false }
                        }
                    }
                }
            )
        }
        composable<Route.Playback> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Playback>()
            PlaybackScreen(
                repository = repository,
                sequenceId = route.sequenceId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
