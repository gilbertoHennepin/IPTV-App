package com.iptvapp.ui.components

import androidx.compose.foundation.focusable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.iptvapp.data.local.entity.ChannelEntity

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategorySidebarGrid(
    channels: List<ChannelEntity>,
    minGridCellSize: Dp,
    contentPadding: PaddingValues = PaddingValues(start = 24.dp, end = 48.dp, top = 8.dp, bottom = 48.dp),
    itemContent: @Composable (ChannelEntity, FocusRequester) -> Unit
) {
    if (channels.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No content available.",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray
            )
        }
        return
    }

    // 1. Extract unique categories from the dataset
    val categories = remember(channels) {
        listOf("All") + channels.mapNotNull { it.categoryName }.distinct().sorted()
    }

    // 2. Track selected category
    var selectedCategory by remember { mutableStateOf("All") }

    // 3. Filter channels based on selected category
    val filteredChannels = remember(channels, selectedCategory) {
        if (selectedCategory == "All") channels else channels.filter { it.categoryName == selectedCategory }
    }

    // A map of FocusRequesters for the grid items to handle fast scrolling safely
    val gridFocusRequesters = remember { androidx.compose.runtime.mutableStateMapOf<Long, FocusRequester>() }

    Row(modifier = Modifier.fillMaxSize()) {
        // ── Left: Categories Sidebar ──
        LazyColumn(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight(),
            contentPadding = PaddingValues(start = 48.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                var isFocused by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .onFocusChanged { state ->
                            isFocused = state.isFocused
                            if (state.isFocused) {
                                selectedCategory = category
                            }
                        }
                        .focusable()
                        .background(
                            color = if (isFocused) Color(0xFF64FFDA).copy(alpha = 0.2f) else Color.Transparent,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isFocused || selectedCategory == category) Color(0xFF64FFDA) else Color.LightGray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        // ── Right: Content Grid ──
        if (filteredChannels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No content in this category.",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = minGridCellSize),
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = filteredChannels,
                    key = { it.id }
                ) { channel ->
                    val focusRequester = gridFocusRequesters.getOrPut(channel.id) { FocusRequester() }
                    itemContent(channel, focusRequester)
                }
            }
        }
    }
}
