package com.iptvapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/**
 * Home screen displaying the live channel catalog in a [TvLazyVerticalGrid].
 *
 * Focus management:
 * 1. Each [ChannelCard] is assigned a [FocusRequester] stored in a map keyed by channel ID.
 * 2. When a card gains D-Pad focus, its channel ID is recorded in [HomeViewModel.lastFocusedChannelId].
 * 3. After returning from the detail screen (popBackStack), a [LaunchedEffect] reads the
 *    stored ID and invokes [FocusRequester.requestFocus] on the exact card the user left from.
 *
 * @param onChannelClick Callback with the channel ID when a card is selected.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onChannelClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val channels by viewModel.channels.collectAsState()
    val lastFocusedId by viewModel.lastFocusedChannelId.collectAsState()

    // ── FocusRequester Map ────────────────────────────────────────────────
    // One FocusRequester per channel, keyed by channel ID.
    // Using remember + mutableStateMapOf so requesters survive recomposition
    // but are garbage-collected when the composable leaves the tree.
    val focusRequesters = remember { mutableStateMapOf<Long, FocusRequester>() }

    // ── Focus Restoration after Back Navigation ──────────────────────────
    // When we return from the detail screen, lastFocusedId is non-null.
    // We wait for the grid to be composed (channels non-empty) then
    // request focus on the previously-selected card.
    LaunchedEffect(lastFocusedId, channels) {
        val targetId = lastFocusedId ?: return@LaunchedEffect
        if (channels.isEmpty()) return@LaunchedEffect

        // Small delay to ensure the grid items are laid out and
        // their FocusRequesters are attached before requesting focus
        kotlinx.coroutines.delay(100)

        focusRequesters[targetId]?.let { requester ->
            try {
                requester.requestFocus()
                viewModel.clearFocusTarget()
            } catch (e: IllegalStateException) {
                // FocusRequester not yet attached — will retry on next recomposition
            }
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0D0D),
                        Color(0xFF1A1A2E),
                        Color(0xFF0D0D0D)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Header ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, top = 40.dp, end = 48.dp, bottom = 16.dp)
            ) {
                Column {
                    Text(
                        text = "Live TV",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${channels.size} channels",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF78909C)
                    )
                }
            }

            // ── Channel Grid ─────────────────────────────────────────────
            if (channels.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No channels loaded",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF546E7A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add your Xtream Codes server to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF455A64)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 176.dp),
                    contentPadding = PaddingValues(
                        start = 48.dp,
                        end = 48.dp,
                        top = 8.dp,
                        bottom = 48.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = channels,
                        key = { it.id }
                    ) { channel ->
                        // Get or create a FocusRequester for this channel
                        val focusRequester = focusRequesters.getOrPut(channel.id) {
                            FocusRequester()
                        }

                        ChannelCard(
                            channel = channel,
                            focusRequester = focusRequester,
                            onFocused = {
                                viewModel.onChannelFocused(channel.id)
                            },
                            onClick = {
                                onChannelClick(channel.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
