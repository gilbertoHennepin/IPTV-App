package com.iptvapp.ui.screens.detail

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.iptvapp.player.PlayerManager
import com.iptvapp.player.PlayerEventListener
import com.iptvapp.player.PlaybackState

/**
 * Channel detail screen showing channel info and initiating playback.
 *
 * Pressing the Back button (KEYCODE_BACK) triggers [onBackPressed]
 * which pops the back stack, returning to the Home grid where
 * focus is restored to the previously-selected card.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelDetailScreen(
    onBackPressed: () -> Unit,
    viewModel: ChannelDetailViewModel = hiltViewModel()
) {
    val channel by viewModel.channel.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var playbackState by remember { mutableStateOf(PlaybackState.IDLE) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0D0D),
                        Color(0xFF16213E),
                        Color(0xFF0D0D0D)
                    )
                )
            )
            // Handle D-Pad Back button
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackPressed()
                    true
                } else {
                    false
                }
            }
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading...",
                        color = Color(0xFF78909C),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            channel == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Channel not found",
                        color = Color(0xFFEF5350),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            else -> {
                val ch = channel!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 48.dp, vertical = 40.dp)
                ) {
                    // ── Channel Header ───────────────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Channel initial avatar
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF1E88E5),
                                            Color(0xFF42A5F5)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ch.name.take(2).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        Column {
                            Text(
                                text = ch.name,
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White
                            )
                            ch.categoryName?.let { cat ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF90CAF9)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // ── Channel Info ─────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1A1A2E).copy(alpha = 0.8f))
                            .padding(24.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InfoRow(label = "Stream Type", value = ch.streamType ?: "live")
                            InfoRow(label = "Stream ID", value = ch.streamId?.toString() ?: "—")
                            InfoRow(label = "Catch-up", value = if (ch.tvArchive) "Available" else "Not available")

                            // Playback status
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF78909C),
                                    modifier = Modifier.width(120.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (playbackState) {
                                                PlaybackState.READY -> Color(0xFF4CAF50)
                                                PlaybackState.BUFFERING -> Color(0xFFFFC107)
                                                PlaybackState.IDLE -> Color(0xFF78909C)
                                                PlaybackState.ENDED -> Color(0xFFEF5350)
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = playbackState.name.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Back hint ────────────────────────────────────────
                    Text(
                        text = "Press BACK to return to channel list",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color(0xFF455A64)
                    )
                }
            }
        }
    }
}

/**
 * Simple label → value row for the channel info card.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun InfoRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF78909C),
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}
