package co.touchlab.skie.kotlingenerator

class SmartStringBuilder {

    private val storage: StringBuilder = StringBuilder()

    operator fun String.unaryPlus() {
        storage.appendLine(this)
    }

    fun append(text: String) {
        storage.append(text)
    }

    fun indented(perform: SmartStringBuilder.() -> Unit) {
        val builder = SmartStringBuilder()

        builder.perform()

        builder.storage.lines().dropLast(1).forEach { storage.appendLine("    $it") }
    }

    fun <T> Iterable<T>.forEachNestedWithEmptyLines(action: SmartStringBuilder.(T) -> Unit) {
        val elements = this.toList()

        if (elements.isEmpty()) {
            return
        }

        +""

        indented {
            elements.forEachIndexed { index, element ->
                if (index > 0) {
                    +""
                }

                action(element)
            }
        }
    }

    override fun toString(): String =
        storage.lines().joinToString("\n") { it.takeIf { it.isNotBlank() } ?: "" }

    companion object {

        operator fun invoke(build: SmartStringBuilder.() -> Unit): String =
            SmartStringBuilder().apply(build).toString()
    }
}

