package dev.fleetworks.memo.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fleetworks.memo.chat.OpenAiChatSource
import dev.fleetworks.memo.core.profile.ProfileStore
import dev.fleetworks.memo.core.profile.ProviderPreset
import dev.fleetworks.memo.core.profile.ProviderPresets
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
    val selectedPresetName = MutableStateFlow<String?>(null)
    val saved = MutableStateFlow(false)

    fun load() {
        viewModelScope.launch {
            val current = profiles.observeProfile().first()
            if (current == null) {
                val first = ProviderPresets.all.first()
                baseUrl.value = first.baseUrl
                model.value = first.modelHint
                selectedPresetName.value = first.name
            } else {
                baseUrl.value = current.baseUrl
                apiKey.value = current.apiKey
                model.value = current.model
                selectedPresetName.value = ProviderPresets.all
                    .find { it.baseUrl.trimEnd('/') == current.baseUrl.trimEnd('/') }
                    ?.name
            }
        }
    }

    fun applyPreset(preset: ProviderPreset) {
        baseUrl.value = preset.baseUrl
        if (model.value.isBlank() || selectedPresetName.value != null) {
            model.value = preset.modelHint
        }
        selectedPresetName.value = preset.name
        saved.value = false
    }

    fun onBaseUrlChange(value: String) {
        baseUrl.value = value
        selectedPresetName.value = ProviderPresets.all
            .find { it.baseUrl.trimEnd('/') == value.trimEnd('/') }
            ?.name
        saved.value = false
    }

    fun onModelChange(value: String) {
        model.value = value
        saved.value = false
    }

    fun onKeyChange(value: String) {
        apiKey.value = value
        saved.value = false
    }

    fun save() {
        viewModelScope.launch {
            profiles.save(ProviderProfile(baseUrl.value.trim(), apiKey.value.trim(), model.value.trim()))
            saved.value = true
            status.value = "saved"
        }
    }

    fun test() {
        viewModelScope.launch {
            busy.value = true
            status.value = "testing…"
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
