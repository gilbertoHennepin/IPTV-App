package com.iptvapp.ui.navigation

/**
 * Sealed class defining all navigation destinations in the app.
 * Each destination has a [route] string used by the NavController.
 */
sealed class Screen(val route: String) {

    /** Login screen. */
    data object Login : Screen("login")

    /** Home screen — live channel grid. */
    data object Home : Screen("home")

    /** Channel detail / player screen. */
    data object ChannelDetail : Screen("channel_detail/{channelId}") {
        /** Builds a concrete route for a given channel ID. */
        fun createRoute(channelId: Long): String = "channel_detail/$channelId"
    }

    /** Fullscreen Live Media3 ExoPlayer video player screen. */
    data object Player : Screen("player/{channelId}") {
        /** Builds a concrete route for a given channel ID. */
        fun createRoute(channelId: Long): String = "player/$channelId"
    }

    /** Series Detail screen for TV Series. */
    data object SeriesDetail : Screen("series_detail/{channelId}") {
        /** Builds a concrete route for a given series ID. */
        fun createRoute(seriesId: Long): String = "series_detail/$seriesId"
    }

    companion object {
        /** NavArgument key for the channel ID. */
        const val ARG_CHANNEL_ID = "channelId"
    }
}
