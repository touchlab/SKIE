package co.touchlab.skie.gradle.version.target

open class DimensionWithAliases<COMPONENT: Target.Component>(
    final override val name: String?,
    final override val commonName: String,
    final override val components: Set<COMPONENT>,
    aliases: Map<String, Set<COMPONENT>>,
): Target.Dimension<COMPONENT> {
    final override val componentsWithDimension: Set<Target.ComponentInDimension<COMPONENT>> = components.map {
        Target.ComponentInDimension(this, it)
    }.toSet()

    private val aliasesAndSpecificComponents = aliases +
        components.associate { it.value to setOf(it) } +
        mapOf(commonName to components)


    override val prefix: String = name?.let { "${it}_" } ?: ""
    protected val regexes = Regexes()

    override fun parse(string: String): Set<COMPONENT> {
        return tryParseEnumeratedComponents(string) ?:
            tryParseAny(string) ?:
            emptySet()
    }

    private fun tryParseEnumeratedComponents(string: String): Set<COMPONENT>? {
        return regexes.enumeration.matchEntire(string)?.let { match ->
            val rawValues = match.groupValues[1]
            rawValues.split(",").flatMap { rawValue ->
                checkNotNull(aliasesAndSpecificComponents[rawValue]) {
                    "Could not find component with value $rawValue when parsing $string!"
                }
            }.toSet()
        }
    }

    private fun tryParseAny(string: String): Set<COMPONENT>? {
        return regexes.any.matchEntire(string)?.let { match ->
            val possibleComponentNameOrAlias = match.groupValues[1]
            aliasesAndSpecificComponents[possibleComponentNameOrAlias]
        }
    }

    inner class Regexes {
        val range: Regex = "^${prefix}(.+)\\.\\.(.+)$".toRegex()
        val enumeration: Regex = "^${prefix}(.+(?:,.+)+)$".toRegex()
        val any: Regex = "^${prefix}(.+)$".toRegex()
    }
}
