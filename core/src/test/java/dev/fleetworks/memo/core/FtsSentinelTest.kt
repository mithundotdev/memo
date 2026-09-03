package dev.fleetworks.memo.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FtsSentinelTest {
    @Test
    fun replacesBracketsOnIndex() {
        val out = FtsSentinel.forIndex("See [[My Note]] today")
        assertFalse(out.contains("[["))
        assertFalse(out.contains("]]"))
        assertTrue(out.contains(FtsSentinel.SENTINEL))
    }

    @Test
    fun queryTransformMatchesIndexTransform() {
        assertEquals(FtsSentinel.forIndex("[[Title]]"), FtsSentinel.forQuery("[[Title]]"))
    }

    @Test
    fun leavesPlainTextWithoutSentinel() {
        assertEquals("hello world", FtsSentinel.forIndex("hello world"))
    }

    @Test
    fun titleBacklinkQueryContainsSentinel() {
        val out = FtsSentinel.forQuery("[[Shopping]]")
        assertTrue(out.contains("Shopping"))
        assertTrue(out.contains(FtsSentinel.SENTINEL))
    }
}
