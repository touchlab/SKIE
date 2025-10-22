package co.touchlab.skie.configuration

interface UntypedSkieConfigurationData<FLAGS> {

    val enabledConfigurationFlags: Set<FLAGS>

    val groups: List<Group>

    interface Group {

        val target: String

        val overridesAnnotations: Boolean

        val items: Map<String, String?>
    }
}
