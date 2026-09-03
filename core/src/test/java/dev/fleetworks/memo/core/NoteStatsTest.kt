package dev.fleetworks.memo.core

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteStatsTest {
    @Test
    fun blankIsZero() {
        assertEquals(0, NoteStats.wordCount("   "))
    }

    @Test
    fun countsAcrossLines() {
        assertEquals(6, NoteStats.wordCount("hello world\none two three four"))
    }

    @Test
    fun ignoresExtraWhitespace() {
        assertEquals(3, NoteStats.wordCount("  one   two\tthree  "))
    }
}
