package co.touchlab.swiftkt.plugin

data class LoadCommand(
    val index: Int,
    val attributes: Map<String, String>,
) {

    val cmd: String?
        get() = attributes["cmd"]

    operator fun get(name: String): String? {
        return attributes[name]
    }
    companion object {
        private val loadCommand = "Load command (\\d+)((?:\\s\\s+(?:\\w+)\\s(?:.*))+)".toRegex()
        private val loadCommandAttribute = "(\\w+)\\s(.*)".toRegex()

        fun parseOtoolOutput(otoolOutput: String): List<LoadCommand> {
            return loadCommand.findAll(otoolOutput).map { loadCommandMatch ->
                LoadCommand(
                    index = loadCommandMatch.groupValues[1].toInt(),
                    attributes = loadCommandAttribute.findAll(loadCommandMatch.groupValues[2])
                        .map { it.groupValues[1] to it.groupValues[2] }.toMap()
                )
            }.toList()
        }
    }
}
