package com.iptvapp.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    val urlFocus = remember { androidx.compose.ui.focus.FocusRequester() }
    val userFocus = remember { androidx.compose.ui.focus.FocusRequester() }
    val passFocus = remember { androidx.compose.ui.focus.FocusRequester() }
    val btnFocus = remember { androidx.compose.ui.focus.FocusRequester() }

    // Don't auto-focus text fields — let the user D-Pad navigate and press Enter to type

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Xtream Codes Login",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            TvTextField(
                value = uiState.hostUrl,
                onValueChange = viewModel::updateHostUrl,
                label = "Host URL (e.g., http://provider.com:8080)",
                imeAction = ImeAction.Next,
                onImeAction = { userFocus.requestFocus() },
                modifier = Modifier
                    .focusRequester(urlFocus)
                    .focusProperties { 
                        down = userFocus
                        next = userFocus 
                    }
            )

            TvTextField(
                value = uiState.username,
                onValueChange = viewModel::updateUsername,
                label = "Username",
                imeAction = ImeAction.Next,
                onImeAction = { passFocus.requestFocus() },
                modifier = Modifier
                    .focusRequester(userFocus)
                    .focusProperties { 
                        up = urlFocus
                        down = passFocus
                        next = passFocus
                    }
            )

            TvTextField(
                value = uiState.password,
                onValueChange = viewModel::updatePassword,
                label = "Password",
                isPassword = true,
                imeAction = ImeAction.Done,
                onImeAction = { btnFocus.requestFocus() },
                modifier = Modifier
                    .focusRequester(passFocus)
                    .focusProperties { 
                        up = userFocus
                        down = btnFocus
                        next = btnFocus
                    }
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                Button(
                    onClick = { viewModel.authenticate(onLoginSuccess) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(btnFocus)
                        .focusProperties { 
                            up = passFocus
                        }
                ) {
                    Text(
                        text = "Login",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun TvTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { androidx.compose.material3.Text(label, color = if (isFocused) Color.White else Color.Gray) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Uri,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.LightGray,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .then(
                if (isFocused) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
                } else {
                    Modifier
                }
            )
    )
}
