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

    override val prefix: String = name?.let { "${it}_" } ?: ""

    private val aliasesAndSpecificComponents = aliases.mapValues { (name, components) ->
            SourceSet.ComponentSet.Enumerated(prefix + name, this, components)
        } +
        components.associate { it.value to SourceSet.ComponentSet.Specific(it.value, this, it) } +
        mapOf(commonName to SourceSet.ComponentSet.Common(prefix + commonName, this, components))


    protected val regexes = Regexes()

    override fun parse(string: String): SourceSet.ComponentSet<COMPONENT>? {
        return tryParseEnumeratedComponents(string) ?:
            tryParseAny(string)
    }

    private fun tryParseEnumeratedComponents(string: String): SourceSet.ComponentSet<COMPONENT>? {
        return regexes.enumeration.matchEntire(string)?.let { match ->
            val rawValues = match.groupValues[1]
            val value = rawValues.split(",").flatMap { rawValue ->
                checkNotNull(aliasesAndSpecificComponents[rawValue]) {
                    "Could not find component with value $rawValue when parsing $string!"
                }.components
            }.toSet()

            SourceSet.ComponentSet.Enumerated(string, this, value)
        }
    }

    private fun tryParseAny(string: String): SourceSet.ComponentSet<COMPONENT>? {
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
