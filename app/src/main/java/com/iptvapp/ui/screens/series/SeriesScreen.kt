package com.iptvapp.ui.screens.series

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Text
import androidx.tv.material3.MaterialTheme
import com.iptvapp.ui.screens.home.ChannelCard

@Composable
fun SeriesScreen(
    onSeriesClick: (Long) -> Unit,
    viewModel: SeriesViewModel = hiltViewModel()
) {
    val seriesList by viewModel.seriesList.collectAsState()

    if (seriesList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No TV Series found.",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray
            )
        }
    } else {
        val focusRequesters = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateMapOf<Long, androidx.compose.ui.focus.FocusRequester>() }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 8.dp, bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(seriesList, key = { it.id }) { series ->
                val focusRequester = focusRequesters.getOrPut(series.id) { androidx.compose.ui.focus.FocusRequester() }
                ChannelCard(
                    channel = series,
                    focusRequester = focusRequester,
                    onFocused = { },
                    onClick = { onSeriesClick(series.id) }
                )
            }
        }
    }
}
