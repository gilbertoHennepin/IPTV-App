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

import com.iptvapp.updater.UpdateInfo
import com.iptvapp.updater.UpdateManager

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val channelDao: ChannelDao,
    private val categoryDao: CategoryDao,
    private val updateManager: UpdateManager
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

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    fun checkForUpdates() {
        _updateState.value = UpdateState.Checking
        viewModelScope.launch {
            val result = updateManager.checkForUpdates()
            if (result.isSuccess) {
                val info = result.getOrNull()
                if (info != null && info.isUpdateAvailable) {
                    _updateState.value = UpdateState.UpdateAvailable(info)
                } else {
                    _updateState.value = UpdateState.UpToDate
                }
            } else {
                _updateState.value = UpdateState.Error(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun startUpdateDownload(info: UpdateInfo) {
        _updateState.value = UpdateState.Downloading
        updateManager.startDownload(info)
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            authManager.clearCredentials()
            channelDao.deleteAllChannels()
            categoryDao.deleteAllCategories()
            _isLoggedOut.value = true
        }
    }
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class UpdateAvailable(val info: UpdateInfo) : UpdateState()
    object UpToDate : UpdateState()
    object Downloading : UpdateState()
    data class Error(val message: String) : UpdateState()
}
