package co.touchlab.skie.test.runner

import co.touchlab.skie.test.util.*
import org.slf4j.LoggerFactory

object SkieTestRunnerConfiguration {
    private val logger = LoggerFactory.getLogger(SkieTestRunnerConfiguration::class.java)

    val testLevel = value<TestLevel>("testLevel") ?: TestLevel.Thorough
    val testTypes = set<TestType>("testTypes")

    val targets = list("matrix.targets", ::KotlinTargetOrPreset) ?: listOf(KotlinTarget.Preset.Root)
    val configurations = list<BuildConfiguration>("matrix.configurations")
    val linkModes = list<LinkMode>("matrix.linkModes")
    // TODO Automatically derive from Build-setup
    val kotlinVersions = list("matrix.kotlinVersions", ::KotlinVersion) ?: listOf("1.8.0", "1.8.20", "1.9.0", "1.9.20", "2.0.0", "2.0.20", "2.1.0", "2.1.10").map(::KotlinVersion)
    val gradleVersions = list("matrix.gradleVersions", ::GradleVersion) ?: listOf("8.6").map(::GradleVersion)

    fun buildMatrixSource(): SkieTestMatrixSource {
        val allTargets = targets.targets
        return SkieTestMatrixSource(
            targets = allTargets.toMutableList(),
            presets = targets.presets.takeIf { it.isNotEmpty() }?.toMutableList()
                ?: KotlinTarget.Preset.Root.children.presets.filter { preset ->
                    allTargets.toSet().intersect(preset.targets.toSet()).isNotEmpty()
                }.toMutableList(),
            configurations = configurations.toMutableList(),
            linkModes = linkModes.toMutableList(),
            kotlinVersions = kotlinVersions.toMutableList(),
        )
    }

    fun buildMatrixAxes(source: SkieTestMatrixSource) = buildList<SkieTestMatrix.Axis<*>> {
        this += SkieTestMatrix.Axis<BuildConfiguration>("Configuration", source.configurations)
        this += SkieTestMatrix.Axis<LinkMode>("Linkage", source.linkModes)
        this += SkieTestMatrix.Axis<KotlinVersion>("Kotlin", source.kotlinVersions)

        this += SkieTestMatrix.Axis("Target", source.targets)
        this += SkieTestMatrix.Axis("Target", source.targets.filterIsInstance<KotlinTarget.Native>())
        this += SkieTestMatrix.Axis("Target", source.targets.filterIsInstance<KotlinTarget.Native.Darwin>())
        this += SkieTestMatrix.Axis("Target", source.targets.filterIsInstance<KotlinTarget.Native.Ios>())
        this += SkieTestMatrix.Axis("Target", source.targets.filterIsInstance<KotlinTarget.Native.MacOS>())

        this += SkieTestMatrix.Axis("Preset", source.presets)
        this += SkieTestMatrix.Axis("Preset", source.presets.filterIsInstance<KotlinTarget.Preset.Native>())
        this += SkieTestMatrix.Axis("Preset", source.presets.filterIsInstance<KotlinTarget.Preset.Native.Darwin>())
    }.associateBy { it.type }

    private fun <T: Any> value(property: String, deserialize: (String) -> T?): T? {
        val properties = System.getProperties()
        return if (properties.containsKey(property)) {
            val rawValue = properties.getProperty(property).trim()
            deserialize(rawValue)
                .logErrorOnUnknownValue(property, rawValue)
        } else {
            null
        }
    }

    private inline fun <reified E: Enum<E>> value(property: String): E? = value(property) { rawValue ->
        enumValues<E>().singleOrNull { enumEntry ->
            enumEntry.name.equals(rawValue, ignoreCase = true)
        }
    }

    private fun <T: Any> list(property: String, deserialize: (String) -> T?): List<T>? {
        val properties = System.getProperties()
        return if (properties.containsKey(property)) {
            val rawValue = properties.getProperty(property)
            rawValue.split(',').mapNotNull { listItem ->
                val trimmedListItem = listItem.trim()
                deserialize(trimmedListItem)
                    .logErrorOnUnknownValue(property, trimmedListItem)
            }.takeIf { it.isNotEmpty() }
        } else {
            null
        }
    }

    private inline fun <reified E: Enum<E>> list(property: String): List<E> = list(property) { rawValue ->
        enumValues<E>().singleOrNull { enumEntry ->
            enumEntry.name.equals(rawValue, ignoreCase = true)
        }
    } ?: enumValues<E>().toList()

    private fun <T: Any> set(property: String, deserialize: (String) -> T?): Set<T>? = list(property, deserialize)?.toSet()

    private inline fun <reified E: Enum<E>> set(property: String): Set<E> = set(property) { rawValue ->
        enumValues<E>().singleOrNull { enumEntry ->
            enumEntry.name.equals(rawValue, ignoreCase = true)
        }
    } ?: enumValues<E>().toSet()

    private fun <T> List<T>.intersectOrKeepIfEmpty(other: List<T>): List<T> {
        return if (other.isNotEmpty()) {
            val otherAsSet = other.toSet()
            filter(otherAsSet::contains)
        } else {
            this
        }
    }

    private fun <T: Any> T?.logErrorOnUnknownValue(property: String, rawValue: String): T? {
        if (this == null) {
            logger.error("Couldn't deserialize value $rawValue for property $property")
        }
        return this
    }
}
