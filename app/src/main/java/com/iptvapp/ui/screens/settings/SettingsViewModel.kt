package com.iptvapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvapp.data.local.dao.CategoryDao
import com.iptvapp.data.local.dao.ChannelDao
import com.iptvapp.data.repository.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val channelDao: ChannelDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    val hostUrl: StateFlow<String?> = authManager.hostUrlFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val username: StateFlow<String?> = authManager.usernameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            authManager.clearCredentials()
            channelDao.deleteAllChannels()
            categoryDao.deleteAllCategories()
            _isLoggedOut.value = true
        }
    }
}
