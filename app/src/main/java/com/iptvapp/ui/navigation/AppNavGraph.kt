package com.iptvapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.iptvapp.ui.screens.detail.ChannelDetailScreen
import com.iptvapp.ui.screens.home.HomeScreen
import com.iptvapp.ui.screens.login.LoginScreen
import com.iptvapp.ui.screens.player.PlayerScreen

/**
 * Top-level navigation graph for the IPTV application.
 *
 * Configures [saveState] and [restoreState] on navigation actions
 * so that the Home grid preserves its scroll position and focus
 * state when returning from the player screen.
 */
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // ── Login Screen ─────────────────────────────────────────────────
        composable(
            route = Screen.Login.route
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        // ── Home Screen (Channel Grid) ───────────────────────────────────
        composable(
            route = Screen.Home.route
        ) {
            HomeScreen(
                onChannelClick = { channelId ->
                    navController.navigate(
                        route = Screen.Player.createRoute(channelId)
                    ) {
                        // Save the Home screen state (scroll + focus) before navigating
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // ── Fullscreen Live Video Player Screen ──────────────────────────
        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument(Screen.ARG_CHANNEL_ID) {
                    type = NavType.LongType
                }
            )
        ) {
            PlayerScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // ── Channel Detail Screen (Legacy / Info) ────────────────────────
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
