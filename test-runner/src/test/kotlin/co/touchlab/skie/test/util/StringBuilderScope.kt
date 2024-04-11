package co.touchlab.skie.test.util

interface StringBuilderScope {
    operator fun String.unaryPlus()

    operator fun StringBuilder.unaryPlus()
}
