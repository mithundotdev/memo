package dev.fleetworks.memo.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fleetworks.memo.chat.OpenAiChatSource
import dev.fleetworks.memo.core.profile.ProfileStore
import dev.fleetworks.memo.core.profile.ProviderProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(private val profiles: ProfileStore) : ViewModel() {
    val baseUrl = MutableStateFlow("")
    val apiKey = MutableStateFlow("")
    val model = MutableStateFlow("")
    val status = MutableStateFlow<String?>(null)
    val busy = MutableStateFlow(false)

    fun load() {
        viewModelScope.launch {
            val current = profiles.observeProfile().first() ?: return@launch
            baseUrl.value = current.baseUrl
            apiKey.value = current.apiKey
            model.value = current.model
        }
    }

    fun save() {
        viewModelScope.launch {
            profiles.save(ProviderProfile(baseUrl.value.trim(), apiKey.value.trim(), model.value.trim()))
            status.value = "saved"
        }
    }

    fun test() {
        viewModelScope.launch {
            busy.value = true
            status.value = null
            try {
                val profile = ProviderProfile(baseUrl.value.trim(), apiKey.value.trim(), model.value.trim())
                OpenAiChatSource(profile).test()
                status.value = "ok"
            } catch (e: Exception) {
                status.value = e.message ?: "failed"
            } finally {
                busy.value = false
            }
        }
    }

    fun currentStatus(): StateFlow<String?> = status
}
