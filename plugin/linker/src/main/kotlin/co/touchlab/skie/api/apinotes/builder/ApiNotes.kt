package co.touchlab.skie.api.apinotes.builder

class ApiNotes(
    private val moduleName: String,
    private val classes: List<ApiNotesType>,
    private val protocols: List<ApiNotesType>,
) {

    fun createApiNotesFileContent(): String = SmartStringBuilder {
        +"Name: \"$moduleName\""

        if (classes.isNotEmpty()) {
            +"Classes:"
            classes.forEach {
                it.appendApiNote()
            }
        }

        if (protocols.isNotEmpty()) {
            +"Protocols:"
            protocols.forEach {
                it.appendApiNote()
            }
        }
    }
}
