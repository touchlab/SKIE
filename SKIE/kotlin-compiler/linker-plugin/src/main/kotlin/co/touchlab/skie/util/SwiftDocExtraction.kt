package co.touchlab.skie.util

private val KDOC_REGEX = Regex("""/\*\*.*?\*/""", setOf(RegexOption.DOT_MATCHES_ALL))

internal fun extractSwiftDocBefore(source: String, endOffset: Int): String? {
    // get the kdoc (if it exists)
    if (endOffset <= 0 || endOffset > source.length) return null
    val textBefore = source.substring(0, endOffset)
    val match = KDOC_REGEX.findAll(textBefore).lastOrNull() ?: return null

    // make sure there are no code opening characters between the kdoc and declaration
    val between = textBefore.substring(match.range.last + 1)
    if (between.any { it == '{' || it == '}' || it == ';' }) return null

    return match.value.rawKDocToSwiftDocumentation().takeIf { it.isNotBlank() }
}

private fun String.rawKDocToSwiftDocumentation(): String {
    // strip *'s
    val inner = removePrefix("/**").removeSuffix("*/")
    val lines = inner.lines().map { line ->
        val stripped = line.trimStart()
        when {
            stripped.startsWith("* ") -> stripped.removePrefix("* ")
            stripped == "*" -> ""
            stripped.startsWith("*") -> stripped.removePrefix("*")
            else -> stripped
        }
    }.dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }

    // identify where the content ends and the tags start
    val tagStartIndex = lines.indexOfFirst { it.trimStart().startsWith("@") }
    val mainLines = if (tagStartIndex < 0) lines else lines.subList(0, tagStartIndex)
    val tagLinesList = if (tagStartIndex < 0) emptyList() else lines.subList(tagStartIndex, lines.size)
    val mainContent = mainLines.joinToString("\n").trimEnd()

    // parse out the tags into pairs of "tag":"tagContent"
    val allTagPairs = mutableListOf<Pair<String, String>>()
    var currentTag: String? = null
    val currentTagContent = StringBuilder()
    for (line in tagLinesList) {
        val trimmed = line.trimStart()
        if (trimmed.startsWith("@")) {
            currentTag?.let { allTagPairs.add(it to currentTagContent.toString().trim()) }
            currentTag = trimmed.substringBefore(' ').removePrefix("@")
            currentTagContent.clear()
            val rest = trimmed.substringAfter(' ', "").trimStart()
            currentTagContent.append(rest)
        } else {
            if (currentTagContent.isNotEmpty()) currentTagContent.append(" ")
            currentTagContent.append(line.trim())
        }
    }
    currentTag?.let { allTagPairs.add(it to currentTagContent.toString().trim()) }

    // swiftify tag names/format
    val convertedTags = allTagPairs.mapNotNull { (tag, tagContent) ->
        val tagName = tagContent.substringBefore(' ')
        val tagValue = tagContent.substringAfter(' ', "").trim()
        when (tag.lowercase()) {
            "param", "property" -> "- Parameter $tagName: $tagValue"
            "return", "returns" -> "- Returns: $tagContent"
            "throws", "exception" -> if (tagValue.isNotEmpty()) "- Throws: `$tagName` $tagValue" else "- Throws: $tagContent"
            else -> null
        }
    }

    return buildString {
        append(mainContent)
        if (mainContent.isNotEmpty() && convertedTags.isNotEmpty()) append("\n\n")
        append(convertedTags.joinToString("\n"))
    }.trim()
}
