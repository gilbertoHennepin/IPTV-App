package com.iptvapp.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.CompactCard
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.iptvapp.data.local.entity.ChannelEntity

/**
 * A single channel card for the Home grid.
 *
 * Built with [CompactCard] from `androidx.tv.material3` — the canonical
 * TV card component that supports D-Pad focus scaling out of the box.
 *
 * @param channel       The channel data to display
 * @param focusRequester [FocusRequester] for this card — used to programmatically
 *                       restore focus after back-navigation
 * @param onFocused     Callback fired when this card gains D-Pad focus
 * @param onClick       Callback fired when the user presses Select / Enter
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelCard(
    channel: ChannelEntity,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompactCard(
        onClick = onClick,
        image = {
            // Placeholder channel logo area
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .padding(2.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                // Channel initial as fallback when no logo is loaded
                Text(
                    text = channel.name.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF90CAF9)
                )
            }
        },
        title = {
            Text(
                text = channel.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        },
        subtitle = {
            channel.categoryName?.let { category ->
                Text(
                    text = category,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = Color(0xFFB0BEC5)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        },
        modifier = modifier
            .width(176.dp)
            // Attach the FocusRequester so we can programmatically request focus
            .focusRequester(focusRequester)
            // Track when this card gains D-Pad focus
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    onFocused()
                }
            },
        scale = CardDefaults.scale(
            focusedScale = 1.1f  // Enlarge card to 110% when focused via D-Pad
        ),
        shape = CardDefaults.shape(
            shape = RoundedCornerShape(12.dp),
            focusedShape = RoundedCornerShape(14.dp)
        )
    )
}
