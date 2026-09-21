package com.iptvapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.tv.material3.TabRow
import androidx.tv.material3.Tab
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.iptvapp.ui.screens.epg.EpgGridScreen
import com.iptvapp.ui.screens.search.SearchScreen
import com.iptvapp.ui.screens.vod.VodScreen

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
    onSeriesClick: (Long) -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val channels by viewModel.channels.collectAsState()
    val lastFocusedId by viewModel.lastFocusedChannelId.collectAsState()

    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Live TV", "TV Guide", "Movies", "Series", "Multi-View", "Favorites", "Search", "Settings")

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
            // ── Top Navigation Tabs & Sync Status ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, top = 40.dp, end = 48.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = index == selectedTabIndex,
                                onFocus = { selectedTabIndex = index }
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }

                    if (isSyncing || syncMessage != null) {
                        Text(
                            text = syncMessage ?: "Syncing...",
                            color = Color(0xFF64FFDA),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── Content ──────────────────────────────────────────────────
            if (selectedTabIndex == 7) {
                com.iptvapp.ui.screens.settings.SettingsScreen(onLogoutComplete = onLogout)
            } else if (selectedTabIndex == 6) {
                SearchScreen(onChannelClick = onChannelClick, onSeriesClick = onSeriesClick)
            } else if (selectedTabIndex == 5) {
                com.iptvapp.ui.screens.favorites.FavoritesScreen(onChannelClick = onChannelClick)
            } else if (selectedTabIndex == 4) {
                com.iptvapp.ui.screens.multiview.MultiStreamScreen(onBack = { selectedTabIndex = 0 })
            } else if (selectedTabIndex == 3) {
                com.iptvapp.ui.screens.series.SeriesScreen(onSeriesClick = onSeriesClick)
            } else if (selectedTabIndex == 2) {
                VodScreen(onMovieClick = onChannelClick)
            } else if (selectedTabIndex == 1) {
                EpgGridScreen(onChannelClick = onChannelClick)
            } else {
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
                    com.iptvapp.ui.components.CategorySidebarGrid(
                        channels = channels,
                        minGridCellSize = 200.dp,
                        itemContent = { channel, focusRequester ->
                            ChannelCard(
                                channel = channel,
                                focusRequester = focusRequester,
                                onFocused = { viewModel.onChannelFocused(channel.id) },
                                onClick = { onChannelClick(channel.id) },
                                onLongClick = { viewModel.toggleFavorite(channel) }
                            )
                        }
                    )
                }
            }
        }
    }
}
