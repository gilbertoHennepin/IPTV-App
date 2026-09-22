package com.iptvapp.ui.screens.vod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import coil.compose.AsyncImage
import com.iptvapp.data.local.entity.ChannelEntity

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VodScreen(
    onMovieClick: (Long) -> Unit,
    refreshLabel: String? = null,
    onRefreshClick: (() -> Unit)? = null,
    viewModel: VodViewModel = hiltViewModel()
) {
    val movies by viewModel.movies.collectAsState()

    // No extra Box wrapper — the parent HomeScreen already provides the container
    com.iptvapp.ui.components.CategorySidebarGrid(
        channels = movies,
        minGridCellSize = 150.dp,
        refreshLabel = refreshLabel,
        onRefreshClick = onRefreshClick,
        contentPadding = PaddingValues(start = 24.dp, end = 48.dp, top = 8.dp, bottom = 48.dp),
        itemContent = { movie, _ ->
            MovieCard(
                movie = movie,
                onClick = { onMovieClick(movie.id) },
                onLongClick = { viewModel.toggleFavorite(movie) }
            )
        }
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieCard(
    movie: ChannelEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        onLongClick = onLongClick,
        scale = CardDefaults.scale(focusedScale = 1.1f),
        colors = CardDefaults.colors(
            containerColor = Color(0xFF1E1E2E),
            focusedContainerColor = Color(0xFF282A36)
        ),
        modifier = Modifier
            .width(150.dp)
            .aspectRatio(2f / 3f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!movie.logoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = movie.logoUrl,
                    contentDescription = movie.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2C2C3E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = movie.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (movie.isFavorite) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favorite",
                    tint = Color.Red,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(16.dp)
                )
            }
        }
    }
}
