package com.iptvapp.ui.screens.epg

import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.iptvapp.data.remote.dto.EpgListingDto

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EpgGridScreen(
    onChannelClick: (Long) -> Unit,
    viewModel: EpgGridViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp)
    ) {
        Column {

            if (uiState.channels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading channels...", color = Color.Gray, style = MaterialTheme.typography.titleLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 48.dp, start = 24.dp, end = 24.dp)
                ) {
                    items(uiState.channels, key = { it.id }) { channel ->
                        LaunchedEffect(channel.streamId) {
                            viewModel.loadEpgForChannel(channel.streamId)
                        }

                        val listings = uiState.epgMap[channel.streamId]

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Channel Info Sidebar
                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFF1A1A2E), shape = MaterialTheme.shapes.medium)
                                    .padding(12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = channel.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // EPG Lazy Row
                            if (listings == null) {
                                // Loading skeleton
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(300.dp)
                                        .background(Color(0xFF151515), shape = MaterialTheme.shapes.medium),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Loading EPG...", color = Color.DarkGray)
                                }
                            } else if (listings.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(300.dp)
                                        .background(Color(0xFF151515), shape = MaterialTheme.shapes.medium),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No program data", color = Color.DarkGray)
                                }
                            } else {
                                LazyRow(
                                    modifier = Modifier.fillMaxHeight(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(listings, key = { it.id }) { listing ->
                                        EpgProgramCard(
                                            listing = listing,
                                            onClick = { onChannelClick(channel.id) }
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EpgProgramCard(
    listing: EpgListingDto,
    onClick: () -> Unit
) {
    // Decode base64 title & description
    val title = remember(listing.title) {
        try {
            String(Base64.decode(listing.title, Base64.DEFAULT))
        } catch (e: Exception) {
            listing.title
        }
    }

    val description = remember(listing.description) {
        try {
            if (!listing.description.isNullOrBlank()) {
                String(Base64.decode(listing.description, Base64.DEFAULT))
            } else ""
        } catch (e: Exception) {
            listing.description ?: ""
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF26263B))
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${listing.start} - ${listing.end}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64FFDA)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
