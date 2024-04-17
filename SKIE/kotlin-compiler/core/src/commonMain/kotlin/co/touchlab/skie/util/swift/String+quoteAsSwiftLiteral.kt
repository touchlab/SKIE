package co.touchlab.skie.util.swift

fun String.quoteAsSwiftLiteral(): String =
    buildString {
        append('"')

        this@quoteAsSwiftLiteral.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '\t' -> append("\\t")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '"' -> append("\\\"")
                '\'' -> append("\\\'")
                else -> append(char)
            }
        }

        append('"')
    }
