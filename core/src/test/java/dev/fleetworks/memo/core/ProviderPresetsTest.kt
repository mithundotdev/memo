package dev.fleetworks.memo.core

import dev.fleetworks.memo.core.profile.ProviderPresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProviderPresetsTest {
    @Test
    fun coversFourProviders() {
        assertEquals(listOf("OpenAI", "OpenRouter", "DeepSeek", "Gemini"), ProviderPresets.all.map { it.name })
    }

    @Test
    fun openAiPreset() {
        val preset = ProviderPresets.byName("OpenAI")!!
        assertEquals("https://api.openai.com/v1", preset.baseUrl)
        assertEquals("gpt-4o-mini", preset.modelHint)
    }

    @Test
    fun unknownIsNull() {
        assertNull(ProviderPresets.byName("Other"))
    }
}
