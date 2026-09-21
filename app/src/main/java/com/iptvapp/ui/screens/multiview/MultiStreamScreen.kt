package com.iptvapp.ui.screens.multiview

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.iptvapp.data.local.entity.ChannelEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MultiStreamScreen(
    onBack: () -> Unit,
    viewModel: MultiStreamViewModel = hiltViewModel()
) {
    val layout by viewModel.layout.collectAsState()
    val panes by viewModel.panes.collectAsState()
    val isSelectingChannelForPane by viewModel.isSelectingChannelForPane.collectAsState()
    val liveChannels by viewModel.liveChannels.collectAsState()

    BackHandler {
        if (isSelectingChannelForPane != null) {
            viewModel.hideChannelSelector()
        } else {
            onBack()
        }
    }

    // A simple side drawer for channel selection
    Row(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        
        // Settings / Layout Sidebar
        Column(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .background(Color(0xFF1E1E2E)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LayoutButton(
                text = "2",
                isSelected = layout == MultiStreamLayout.TWO_STREAMS,
                onClick = { viewModel.setLayout(MultiStreamLayout.TWO_STREAMS) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            LayoutButton(
                text = "3",
                isSelected = layout == MultiStreamLayout.THREE_STREAMS,
                onClick = { viewModel.setLayout(MultiStreamLayout.THREE_STREAMS) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            LayoutButton(
                text = "4",
                isSelected = layout == MultiStreamLayout.FOUR_STREAMS,
                onClick = { viewModel.setLayout(MultiStreamLayout.FOUR_STREAMS) }
            )
        }

        // Main Video Grid
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            when (layout) {
                MultiStreamLayout.TWO_STREAMS -> TwoStreamLayout(panes, viewModel)
                MultiStreamLayout.THREE_STREAMS -> ThreeStreamLayout(panes, viewModel)
                MultiStreamLayout.FOUR_STREAMS -> FourStreamLayout(panes, viewModel)
            }
        }

        // Channel Selector Sidebar (Slides in from right when active)
        if (isSelectingChannelForPane != null) {
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF1A1A2E))
            ) {
                Column {
                    Text(
                        "Select Channel",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(liveChannels) { channel ->
                            var isFocused by remember { mutableStateOf(false) }
                            Text(
                                text = channel.name,
                                color = if (isFocused) Color(0xFF64FFDA) else Color.LightGray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectChannelForPane(channel) }
                                    .onFocusChanged { isFocused = it.isFocused }
                                    .focusable()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LayoutButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                if (isSelected) Color(0xFF2979FF) else if (isFocused) Color.DarkGray else Color.Transparent,
                shape = androidx.compose.foundation.shape.CircleShape
            )
            .border(
                width = 2.dp,
                color = if (isFocused) Color(0xFF64FFDA) else Color.Transparent,
                shape = androidx.compose.foundation.shape.CircleShape
            )
            .clickable { onClick() }
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TwoStreamLayout(panes: List<StreamPaneState>, viewModel: MultiStreamViewModel) {
    if (panes.size < 2) return
    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            StreamPane(panes[0], viewModel)
        }
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            StreamPane(panes[1], viewModel)
        }
    }
}

@Composable
fun ThreeStreamLayout(panes: List<StreamPaneState>, viewModel: MultiStreamViewModel) {
    if (panes.size < 3) return
    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            StreamPane(panes[0], viewModel)
        }
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                StreamPane(panes[1], viewModel)
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                StreamPane(panes[2], viewModel)
            }
        }
    }
}

@Composable
fun FourStreamLayout(panes: List<StreamPaneState>, viewModel: MultiStreamViewModel) {
    if (panes.size < 4) return
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                StreamPane(panes[0], viewModel)
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                StreamPane(panes[1], viewModel)
            }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                StreamPane(panes[2], viewModel)
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                StreamPane(panes[3], viewModel)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun StreamPane(state: StreamPaneState, viewModel: MultiStreamViewModel) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .border(
                width = if (state.isFocused) 4.dp else 1.dp,
                color = if (state.isFocused) Color(0xFF64FFDA) else Color.DarkGray
            )
            .clickable { viewModel.showChannelSelector(state.id) }
            .onFocusChanged { 
                if (it.isFocused) {
                    viewModel.onPaneFocused(state.id)
                }
            }
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        if (state.player == null) {
            Text("Hardware Decoder Limit Reached", color = Color.Red)
        } else if (state.currentChannel == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Click to select channel", color = Color.White)
            }
        } else {
            AndroidView(
                factory = {
                    PlayerView(context).apply {
                        player = state.player
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(android.graphics.Color.BLACK)
                    }
                },
                update = { view ->
                    if (view.player != state.player) {
                        view.player = state.player
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Audio Indicator overlay
            if (state.isFocused) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.CircleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("🔊 Audio", color = Color(0xFF64FFDA), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
