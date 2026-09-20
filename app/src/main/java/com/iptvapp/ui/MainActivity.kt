package com.iptvapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.iptvapp.ui.navigation.AppNavGraph
import com.iptvapp.ui.theme.IPTVAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry-point Activity for the IPTV application.
 *
 * Hosts the Compose navigation graph inside the TV Material3 theme.
 * Annotated with @AndroidEntryPoint to enable Hilt injection
 * into this Activity and all hosted Composables.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IPTVAppTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}
