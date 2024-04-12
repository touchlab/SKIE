package co.touchlab.skie.test.util


fun buildString(content: StringBuilderScope.() -> Unit): String {
    val builder = StringBuilder()
    content(object: StringBuilderScope {
        override fun String.unaryPlus() {
            builder.append(this)
        }

        override fun StringBuilder.unaryPlus() {
            builder.append(this)
        }

    })
    return builder.toString()
}
