package co.touchlab.skie.test.util

interface StringBuilderScope {
    operator fun String.unaryPlus()

    operator fun StringBuilder.unaryPlus()

    operator fun Iterable<String>.unaryPlus() {
        this.forEach {
            +it
        }
    }
}
