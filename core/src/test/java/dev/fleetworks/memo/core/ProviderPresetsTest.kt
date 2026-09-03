package dev.fleetworks.memo.core

import dev.fleetworks.memo.core.profile.ProviderPresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProviderPresetsTest {
    @Test
    fun coversAllProviders() {
        assertEquals(
            listOf("Kilo", "OpenAI", "OpenRouter", "DeepSeek", "Gemini"),
            ProviderPresets.all.map { it.name }
        )
    }

    @Test
    fun kiloPreset() {
        val preset = ProviderPresets.byName("Kilo")!!
        assertEquals("https://api.kilo.ai/api/gateway/", preset.baseUrl)
        assertEquals("stepfun/step-3.7-flash:free", preset.modelHint)
    }

    @Test
    fun openAiPreset() {
        val preset = ProviderPresets.byName("OpenAI")!!
        assertEquals("https://api.openai.com/v1/", preset.baseUrl)
        assertEquals("gpt-4o-mini", preset.modelHint)
    }

    @Test
    fun unknownIsNull() {
        assertNull(ProviderPresets.byName("Other"))
    }
}
