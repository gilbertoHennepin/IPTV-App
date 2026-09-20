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

    com.iptvapp.ui.components.CategorySidebarGrid(
        channels = seriesList,
        minGridCellSize = 140.dp,
        contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 8.dp, bottom = 48.dp),
        itemContent = { series, focusRequester ->
            ChannelCard(
                channel = series,
                focusRequester = focusRequester,
                onFocused = { },
                onClick = { onSeriesClick(series.id) }
            )
        }
    )
}
