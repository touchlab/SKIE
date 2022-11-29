package co.touchlab.skie.acceptancetests.framework.internal.testrunner

internal class TestLogger {

    private val builder = StringBuilder()

    fun prepend(text: String) {
        builder.insert(0, text)
    }

    fun prependLine(text: String = "") {
        prepend(text + "\n")
    }

    fun prependSection(header: String, text: String) {
        if (text.isNotBlank()) {
            if (text.endsWith(System.lineSeparator())) {
                prepend(text)
            } else {
                prependLine(text)
            }

            prependLine("---------------- $header ----------------")
        }
    }

    fun appendSection(header: String, text: String) {
        if (text.isNotBlank()) {
            builder.endLine()

            builder.appendLine("---------------- $header ----------------")
            builder.append(text)

            builder.endLine()
        }
    }

    private fun StringBuilder.endLine() {
        if (this.isNotEmpty() && !this.endsWith(System.lineSeparator())) {
            this.appendLine()
        }
    }

    override fun toString(): String = builder.toString()
}
