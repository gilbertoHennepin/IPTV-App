package com.iptvapp.ui.navigation

/**
 * Sealed class defining all navigation destinations in the app.
 * Each destination has a [route] string used by the NavController.
 */
sealed class Screen(val route: String) {

    /** Home screen — live channel grid. */
    data object Home : Screen("home")

    /** Channel detail / player screen. */
    data object ChannelDetail : Screen("channel_detail/{channelId}") {
        /** Builds a concrete route for a given channel ID. */
        fun createRoute(channelId: Long): String = "channel_detail/$channelId"
    }

    companion object {
        /** NavArgument key for the channel ID on the detail screen. */
        const val ARG_CHANNEL_ID = "channelId"
    }
}
