package co.touchlab.skie.gradle.version.target

class ComparableDimension<COMPONENT>(
    name: String?,
    commonName: String,
    components: Set<COMPONENT>,
    aliases: Map<String, Set<COMPONENT>>,
): DimensionWithAliases<COMPONENT>(name, commonName, components, aliases) where COMPONENT: Comparable<COMPONENT>, COMPONENT: Target.Component {

    private val sortedComponents = components.sorted()

    override fun parse(string: String): SourceSet.ComponentSet<COMPONENT>? {
        return tryParseRange(string) ?: super.parse(string)
    }

    private fun tryParseRange(string: String): SourceSet.ComponentSet<COMPONENT>? {
        return regexes.range.matchEntire(string)?.let { match ->
            val (startValue, endValue) = match.destructured
            val startComponentIndex = checkNotNull(sortedComponents.indexOfFirst { it.value == startValue }.takeIf { it >= 0 }) {
                "Could not find start component with value $startValue when parsing $string!"
            }
            val endLabel = checkNotNull(sortedComponents.indexOfLast { it.value == endValue }.takeIf { it >= 0 }) {
                "Could not find end component with value $endValue when parsing $string!"
            }

            SourceSet.ComponentSet.Enumerated(
                name = string,
                dimension = this,
                components = sortedComponents.subList(startComponentIndex, endLabel + 1).toSet(),
            )
        }
    }
}
