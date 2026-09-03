package dev.fleetworks.memo.core

import org.junit.Assert.assertEquals
import org.junit.Test

class TagParserTest {
    @Test
    fun extractsSingleTag() {
        assertEquals(setOf("todo"), TagParser.extract("Buy milk #todo"))
    }

    @Test
    fun extractsMultipleTags() {
        assertEquals(setOf("work", "urgent"), TagParser.extract("#work fix #urgent bug"))
    }

    @Test
    fun returnsEmptyWhenNoTags() {
        assertEquals(emptySet<String>(), TagParser.extract("plain text"))
    }

    @Test
    fun ignoresHashWithoutName() {
        assertEquals(emptySet<String>(), TagParser.extract("# and #!"))
    }

    @Test
    fun dedupesRepeatedTags() {
        assertEquals(setOf("idea"), TagParser.extract("#idea plus #idea"))
    }
}
