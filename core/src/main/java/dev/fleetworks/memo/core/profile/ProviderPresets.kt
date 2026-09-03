package dev.fleetworks.memo.core.profile

data class ProviderPreset(val name: String, val baseUrl: String, val modelHint: String)

object ProviderPresets {
    val all = listOf(
        ProviderPreset("OpenAI", "https://api.openai.com/v1", "gpt-4o-mini"),
        ProviderPreset("OpenRouter", "https://openrouter.ai/api/v1", "openai/gpt-4o-mini"),
        ProviderPreset("DeepSeek", "https://api.deepseek.com/v1", "deepseek-chat"),
        ProviderPreset("Gemini", "https://generativelanguage.googleapis.com/v1beta/openai/", "gemini-2.0-flash")
    )

    fun byName(name: String): ProviderPreset? = all.find { it.name == name }
}
