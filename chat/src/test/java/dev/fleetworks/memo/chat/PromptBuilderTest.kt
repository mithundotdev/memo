package dev.fleetworks.memo.chat

import org.junit.Assert.assertEquals
import org.junit.Test

class PromptBuilderTest {
    @Test
    fun searchUsesPlainWords() {
        assertEquals(
            "looked through 3 notes",
            PromptBuilder.activityLine(ToolName.SEARCH_NOTES, "found 3 notes: a, b, c")
        )
    }

    @Test
    fun searchDefaultsToZero() {
        assertEquals("looked through 0 notes", PromptBuilder.activityLine(ToolName.SEARCH_NOTES, "nothing"))
    }
}
