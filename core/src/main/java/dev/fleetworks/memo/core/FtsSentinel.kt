package dev.fleetworks.memo.core

object FtsSentinel {
    const val SENTINEL = "wikilinktoken"

    fun forIndex(text: String): String =
        text.replace("[[", " $SENTINEL ").replace("]]", " $SENTINEL ")

    fun forQuery(text: String): String =
        text.replace("[[", " $SENTINEL ").replace("]]", " $SENTINEL ")
}
