package com.iptvapp.ui.screens.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import coil.compose.AsyncImage

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SeriesDetailScreen(
    onEpisodeClick: (Long) -> Unit,
    viewModel: SeriesDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = Color(0xFF64FFDA))
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${uiState.error}", color = Color.Red, style = MaterialTheme.typography.titleLarge)
            }
        } else {
            val seriesInfo = uiState.seriesInfo?.info
            val episodesMap = uiState.seriesInfo?.episodes ?: emptyMap()

            // Find all available seasons (keys in episodes map)
            val availableSeasons = episodesMap.keys.toList().sortedBy { it.toIntOrNull() ?: 0 }
            var selectedSeasonIndex by remember { mutableStateOf(0) }

            Row(modifier = Modifier.fillMaxSize().padding(48.dp)) {
                // ── Left: Series Details ────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        // Cover Image
                        AsyncImage(
                            model = seriesInfo?.cover,
                            contentDescription = "Series Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(160.dp)
                                .height(240.dp)
                                .background(Color.DarkGray, MaterialTheme.shapes.medium)
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        // Info Text
                        Column {
                            Text(
                                text = seriesInfo?.name ?: "Unknown Series",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Genre: ${seriesInfo?.genre ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray
                            )
                            Text(
                                text = "Cast: ${seriesInfo?.cast ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = seriesInfo?.plot ?: "No plot available.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Seasons Tabs
                    if (availableSeasons.isNotEmpty()) {
                        TabRow(selectedTabIndex = selectedSeasonIndex) {
                            availableSeasons.forEachIndexed { index, seasonNum ->
                                Tab(
                                    selected = index == selectedSeasonIndex,
                                    onFocus = { selectedSeasonIndex = index }
                                ) {
                                    Text(
                                        text = "Season $seasonNum",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(48.dp))

                // ── Right: Episodes List ────────────────────────────────────
                val currentSeasonNum = availableSeasons.getOrNull(selectedSeasonIndex)
                val currentEpisodes = currentSeasonNum?.let { episodesMap[it] } ?: emptyList()

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentEpisodes) { episode ->
                        Card(
                            onClick = {
                                viewModel.createPlayableEpisode(episode) { generatedId ->
                                    onEpisodeClick(generatedId)
                                }
                            },
                            colors = CardDefaults.colors(
                                containerColor = Color(0xFF1E1E1E),
                                focusedContainerColor = Color(0xFF64FFDA)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${episode.episodeNum}.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    modifier = Modifier.width(32.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = episode.title ?: "Episode ${episode.episodeNum}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    if (!episode.info?.duration.isNullOrEmpty()) {
                                        Text(
                                            text = "Duration: ${episode.info?.duration}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.LightGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
