package co.touchlab.swiftgen.sealed.acceptancetests.framework.internal.testrunner

internal class Logger {

    private val builder = StringBuilder()

    fun write(header: String, text: String) {
        if (text.isNotBlank()) {
            builder.appendLine("---------------- $header ----------------")
            builder.appendLine(text)
        }
    }

    override fun toString(): String =
        builder.toString()
}