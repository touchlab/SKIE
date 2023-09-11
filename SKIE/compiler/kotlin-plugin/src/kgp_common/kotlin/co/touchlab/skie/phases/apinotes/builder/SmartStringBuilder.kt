package co.touchlab.skie.phases.apinotes.builder

class SmartStringBuilder {

    private val storage: StringBuilder = StringBuilder()

    operator fun String.unaryPlus() {
        storage.appendLine(this)
    }

    fun indented(perform: SmartStringBuilder.() -> Unit) {
        val builder = SmartStringBuilder()

        builder.perform()

        builder.storage.lines().forEach { storage.appendLine("  $it") }
    }

    override fun toString(): String =
        storage.toString()

    companion object {

        operator fun invoke(build: SmartStringBuilder.() -> Unit): String =
            SmartStringBuilder().apply(build).toString()
    }
}
