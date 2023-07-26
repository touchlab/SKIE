package co.touchlab.skie.configuration

interface TypedSkieConfiguration<FLAGS> {

    val enabledFeatures: Set<FLAGS>

    val groups: List<Group>

    interface Group {

        val target: String

        val overridesAnnotations: Boolean

        val items: Map<String, String?>
    }
}
