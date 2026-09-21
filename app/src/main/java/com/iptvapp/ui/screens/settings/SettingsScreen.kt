package com.iptvapp.ui.screens.settings

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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun SettingsScreen(
    onLogoutComplete: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val hostUrl by viewModel.hostUrl.collectAsState()
    val username by viewModel.username.collectAsState()
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            onLogoutComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(500.dp)
                .background(Color(0xFF1E1E2E), MaterialTheme.shapes.large)
                .padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF64FFDA)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Account Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Server URL:", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                Text(text = hostUrl ?: "Unknown", color = Color.White, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Username:", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                Text(text = username ?: "Unknown", color = Color.White, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.checkForUpdates() },
                colors = ButtonDefaults.colors(
                    containerColor = Color(0xFF2979FF),
                    focusedContainerColor = Color(0xFF448AFF),
                    contentColor = Color.White,
                    focusedContentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Settings, contentDescription = "Updates")
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = when (updateState) {
                            is UpdateState.Checking -> "Checking..."
                            is UpdateState.Downloading -> "Downloading..."
                            else -> "Check for Updates"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.colors(
                    containerColor = Color(0xFFE53935),
                    focusedContainerColor = Color(0xFFFF5252),
                    contentColor = Color.White,
                    focusedContentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Logout and Clear Data")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Logging out will erase the local database and preferences.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // Update Dialogs
            if (updateState is UpdateState.UpdateAvailable) {
                val info = (updateState as UpdateState.UpdateAvailable).info
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { viewModel.resetUpdateState() },
                    title = { Text("Update Available", color = Color.White) },
                    text = { 
                        Column {
                            Text("Version ${info.latestVersionName} is available!", color = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Release Notes:", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(info.releaseNotes, color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF1E1E2E),
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = { viewModel.startUpdateDownload(info) }) {
                            Text("Download & Install", color = Color(0xFF64FFDA))
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { viewModel.resetUpdateState() }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            } else if (updateState is UpdateState.UpToDate) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { viewModel.resetUpdateState() },
                    title = { Text("Up to Date", color = Color.White) },
                    text = { Text("You are already on the latest version.", color = Color.LightGray) },
                    containerColor = Color(0xFF1E1E2E),
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = { viewModel.resetUpdateState() }) {
                            Text("OK", color = Color(0xFF64FFDA))
                        }
                    }
                )
            } else if (updateState is UpdateState.Error) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { viewModel.resetUpdateState() },
                    title = { Text("Update Error", color = Color.White) },
                    text = { Text("Could not check for updates: ${(updateState as UpdateState.Error).message}", color = Color.LightGray) },
                    containerColor = Color(0xFF1E1E2E),
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = { viewModel.resetUpdateState() }) {
                            Text("OK", color = Color(0xFF64FFDA))
                        }
                    }
                )
            }
        }
    }
}
