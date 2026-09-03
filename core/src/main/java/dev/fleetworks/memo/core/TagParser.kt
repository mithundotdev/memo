package dev.fleetworks.memo.core

object TagParser {
    private val tagRegex = Regex("""#([A-Za-z0-9_-]+)""")

    fun extract(text: String): Set<String> =
        tagRegex.findAll(text).map { it.groupValues[1] }.toSet()
}
