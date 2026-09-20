package com.iptvapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.iptvapp.ui.screens.detail.ChannelDetailScreen
import com.iptvapp.ui.screens.home.HomeScreen

/**
 * Top-level navigation graph for the IPTV application.
 *
 * Configures [saveState] and [restoreState] on navigation actions
 * so that the Home grid preserves its scroll position and focus
 * state when returning from the detail screen.
 */
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // ── Home Screen (Channel Grid) ───────────────────────────────────
        composable(
            route = Screen.Home.route
        ) {
            HomeScreen(
                onChannelClick = { channelId ->
                    navController.navigate(
                        route = Screen.ChannelDetail.createRoute(channelId)
                    ) {
                        // Save the Home screen state (scroll + focus) before navigating
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // ── Channel Detail / Player Screen ───────────────────────────────
        composable(
            route = Screen.ChannelDetail.route,
            arguments = listOf(
                navArgument(Screen.ARG_CHANNEL_ID) {
                    type = NavType.LongType
                }
            )
        ) {
            ChannelDetailScreen(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}
