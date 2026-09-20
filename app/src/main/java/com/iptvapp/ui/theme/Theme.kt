package com.iptvapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

/**
 * TV-optimized Material 3 theme for the IPTV application.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun IPTVAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme()

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
