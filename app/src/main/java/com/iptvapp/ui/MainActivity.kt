package com.iptvapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.iptvapp.ui.theme.IPTVAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry-point Activity for the IPTV application.
 * Annotated with @AndroidEntryPoint to enable Hilt injection
 * into this Activity and its hosted Composables.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IPTVAppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "IPTV App",
                        color = Color.White
                    )
                }
            }
        }
    }
}
