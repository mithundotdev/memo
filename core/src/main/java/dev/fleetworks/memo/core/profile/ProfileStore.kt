package dev.fleetworks.memo.core.profile

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore("provider_profile")

class ProfileStore(private val context: Context) {
    private val baseUrlKey = stringPreferencesKey("base_url")
    private val apiKeyKey = stringPreferencesKey("api_key")
    private val modelKey = stringPreferencesKey("model")

    fun observeProfile(): Flow<ProviderProfile?> =
        context.profileDataStore.data.map { prefs ->
            val baseUrl = prefs[baseUrlKey].orEmpty()
            val apiKey = prefs[apiKeyKey].orEmpty()
            val model = prefs[modelKey].orEmpty()
            if (baseUrl.isBlank() && apiKey.isBlank() && model.isBlank()) null
            else ProviderProfile(baseUrl, apiKey, model)
        }

    suspend fun save(profile: ProviderProfile) {
        context.profileDataStore.edit { prefs ->
            prefs[baseUrlKey] = profile.baseUrl
            prefs[apiKeyKey] = profile.apiKey
            prefs[modelKey] = profile.model
        }
    }

    suspend fun clear() {
        context.profileDataStore.edit { it.clear() }
    }
}
