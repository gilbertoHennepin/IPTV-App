package com.iptvapp.ui.screens.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    val tabs = listOf("Live TV", "Movies", "Series", "Search", "Settings")

    val focusRequesters = remember { mutableStateMapOf<Long, FocusRequester>() }

    LaunchedEffect(lastFocusedId, channels) {
        val targetId = lastFocusedId ?: return@LaunchedEffect
        if (channels.isEmpty()) return@LaunchedEffect

        kotlinx.coroutines.delay(100)

        focusRequesters[targetId]?.let { requester ->
            try {
                requester.requestFocus()
                viewModel.clearFocusTarget()
            } catch (e: IllegalStateException) {
                // FocusRequester not yet attached
            }
        }
    }

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
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Tab Bar (compact) ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val refreshFocusRequester = remember { FocusRequester() }

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.focusProperties {
                        right = refreshFocusRequester
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = index == selectedTabIndex,
                            onFocus = { selectedTabIndex = index }
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                if (isSyncing || syncMessage != null) {
                    Text(
                        text = syncMessage ?: "Syncing...",
                        color = Color(0xFF64FFDA),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    // Empty space instead of top bar refresh
                    Spacer(modifier = Modifier.width(20.dp))
                }
            }

            // ── Content Area (fills ALL remaining space) ────────────────
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTabIndex) {
                    4 -> com.iptvapp.ui.screens.settings.SettingsScreen(onLogoutComplete = onLogout)
                    3 -> SearchScreen(onChannelClick = onChannelClick, onSeriesClick = onSeriesClick)
                    2 -> com.iptvapp.ui.screens.series.SeriesScreen(
                        onSeriesClick = onSeriesClick,
                        refreshLabel = "Refresh Series",
                        onRefreshClick = { viewModel.syncSeriesOnly() }
                    )
                    1 -> VodScreen(
                        onMovieClick = onChannelClick,
                        refreshLabel = "Refresh Movies",
                        onRefreshClick = { viewModel.syncMoviesOnly() }
                    )
                    else -> {
                        if (channels.isEmpty() && !isSyncing) {
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
                                refreshLabel = "Refresh Live TV",
                                onRefreshClick = { viewModel.syncLiveTvOnly() },
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
    }
}
