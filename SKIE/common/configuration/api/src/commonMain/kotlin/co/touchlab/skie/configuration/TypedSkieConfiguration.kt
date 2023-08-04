package co.touchlab.skie.configuration

interface TypedSkieConfiguration<FLAGS> {

    val enabledConfigurationFlags: Set<FLAGS>

    val groups: List<Group>

    interface Group {

        val target: String

        val overridesAnnotations: Boolean

        val items: Map<String, String?>
    }
}
