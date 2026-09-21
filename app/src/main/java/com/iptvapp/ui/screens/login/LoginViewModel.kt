package com.iptvapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvapp.data.remote.XtreamApiService
import com.iptvapp.data.repository.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val xtreamApi: XtreamApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authManager.hostUrlFlow.collect { host ->
                if (!host.isNullOrBlank()) {
                    _uiState.update { it.copy(isAuthenticated = true) }
                }
            }
        }
    }

    fun updateHostUrl(hostUrl: String) {
        _uiState.update { it.copy(hostUrl = hostUrl, errorMessage = null) }
    }

    fun updateUsername(username: String) {
        _uiState.update { it.copy(username = username, errorMessage = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun authenticate(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.hostUrl.isBlank() || state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please fill out all fields.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // Ensure host URL has http prefix
                var host = state.hostUrl.trim()
                if (!host.startsWith("http://") && !host.startsWith("https://")) {
                    host = "http://$host"
                }

                // Remove trailing slash if present
                if (host.endsWith("/")) {
                    host = host.dropLast(1)
                }

                // Call authenticate endpoint
                val apiUrl = "$host/player_api.php"
                val response = xtreamApi.authenticate(
                    url = apiUrl,
                    username = state.username.trim(),
                    password = state.password.trim()
                )

                if (response.isSuccessful) {
                    // Check if JSON contains user_info as a quick verification
                    val body = response.body()
                    if (body != null && body.has("user_info")) {
                        // Save credentials on success
                        authManager.saveCredentials(host, state.username.trim(), state.password.trim())
                        _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                        onSuccess()
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid response from server.") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Authentication failed: HTTP ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Network error: ${e.localizedMessage}") }
            }
        }
    }
}

data class LoginUiState(
    val hostUrl: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)
