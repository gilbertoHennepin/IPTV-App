package com.iptvapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Create the DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_settings")

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val HOST_URL = stringPreferencesKey("host_url")
        val USERNAME = stringPreferencesKey("username")
        val PASSWORD = stringPreferencesKey("password")
    }

    val hostUrlFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[HOST_URL]
    }

    val usernameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME]
    }

    val passwordFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PASSWORD]
    }

    suspend fun saveCredentials(host: String, user: String, pass: String) {
        context.dataStore.edit { preferences ->
            preferences[HOST_URL] = host
            preferences[USERNAME] = user
            preferences[PASSWORD] = pass
        }
    }

    suspend fun clearCredentials() {
        context.dataStore.edit { preferences ->
            preferences.remove(HOST_URL)
            preferences.remove(USERNAME)
            preferences.remove(PASSWORD)
        }
    }
}
