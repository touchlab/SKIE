package co.touchlab.skie.test.runner

import co.touchlab.skie.test.*
import co.touchlab.skie.test.util.*
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object SkieTestRunnerConfiguration {

    val testLevel = value<TestLevel>("testLevel") ?: TestLevel.Thorough
    val testTypes = set<TestType>("testTypes")

    val targets = list("matrix.targets", ::KotlinTargetOrPreset) ?: listOf(KotlinTarget.Preset.Root)
    val configurations = list<BuildConfiguration>("matrix.configurations")
    val linkModes = list<LinkMode>("matrix.linkModes")
    val kotlinVersions = list("matrix.kotlinVersions", ::KotlinVersion) ?: listOf("1.8.0", "1.8.20", "1.9.20").map(::KotlinVersion)
    val gradleVersions = list("matrix.gradleVersions", ::GradleVersion) ?: listOf("8.6").map(::GradleVersion)

    fun filteredMatrixAxes(filter: MatrixFilter) = buildList<SkieTestMatrix.Axis<*>> {
        this += SkieTestMatrix.Axis<BuildConfiguration>(
            "Configuration",
            configurations.intersectOrKeepIfEmpty(filter.configurations.toList())
        )
        this += SkieTestMatrix.Axis<LinkMode>("Linkage", linkModes.intersectOrKeepIfEmpty(filter.linkModes.toList()))
        this += SkieTestMatrix.Axis<KotlinVersion>(
            "Kotlin",
            kotlinVersions.intersectOrKeepIfEmpty(filter.kotlinVersions.map(::KotlinVersion))
        )

        val filteredTargets = if (filter.targets.isNotEmpty()) {
            targets.targets.filter(filter.targets::contains)
        } else {
            targets.targets
        }
        this += SkieTestMatrix.Axis("Target", filteredTargets)
        this += SkieTestMatrix.Axis("Target", filteredTargets.filterIsInstance<KotlinTarget.Native>())
        this += SkieTestMatrix.Axis("Target", filteredTargets.filterIsInstance<KotlinTarget.Native.Darwin>())
        this += SkieTestMatrix.Axis("Target", filteredTargets.filterIsInstance<KotlinTarget.Native.Ios>())
        this += SkieTestMatrix.Axis("Target", filteredTargets.filterIsInstance<KotlinTarget.Native.MacOS>())

        // TODO: Add filtering
        val filteredPresets = targets.presets
        this += SkieTestMatrix.Axis("Preset", filteredPresets)
        this += SkieTestMatrix.Axis("Preset", filteredPresets.filterIsInstance<KotlinTarget.Preset.Native>())
        this += SkieTestMatrix.Axis("Preset", filteredPresets.filterIsInstance<KotlinTarget.Preset.Native.Darwin>())
    }.associateBy { it.type }


//     private operator fun <E: Enum<E>> getValue(skieTestMatrix: SkieTestMatrix, property: KProperty<*>): E {
// //         return System.getProperty(property.name, "").toBoolean()
//         TODO()
//     }

    private fun <T: Any> value(property: String, deserialize: (String) -> T?): T? {
        val properties = System.getProperties()
        return if (properties.containsKey(property)) {
            val rawValue = properties.getProperty(property)
            deserialize(rawValue)
        } else {
            null
        }
    }

    private inline fun <reified E: Enum<E>> value(property: String): E? = value(property) {
        enumValueOf<E>(it)
    }

    private fun <T: Any> list(property: String, deserialize: (String) -> T?): List<T>? {
        val properties = System.getProperties()
        return if (properties.containsKey(property)) {
            val rawValue = properties.getProperty(property)
            rawValue.split(',').mapNotNull {
                deserialize(it.trim())
            }
        } else {
            null
        }
    }

    private fun <T: Any> nestedList(property: String, deserialize: (String) -> List<T>): List<T>? {
        val properties = System.getProperties()
        return if (properties.containsKey(property)) {
            val rawValue = properties.getProperty(property)
            rawValue.split(',').flatMap {
                deserialize(it.trim())
            }
        } else {
            null
        }
    }

    private inline fun <reified E: Enum<E>> list(property: String): List<E> = list(property) {
        enumValueOf<E>(it)
    } ?: enumValues<E>().toList()

    private fun <T: Any> set(property: String, deserialize: (String) -> T?): Set<T>? = list(property, deserialize)?.toSet()

    private inline fun <reified E: Enum<E>> set(property: String): Set<E> = set(property) {
        enumValueOf<E>(it)
    } ?: enumValues<E>().toSet()

    private inline fun <reified E: Enum<E>> singleValue(crossinline default: () -> E) = PropertyDelegateProvider { _: SkieTestRunnerConfiguration, property ->
        val properties = System.getProperties()
        val value = if (properties.containsKey(property.name)) {
            val rawValue = properties.getProperty(property.name)
            enumValueOf<E>(rawValue)
        } else {
            default()
        }
        PropertyStorage(value)
    }

    private inline fun <reified E: Enum<E>> multipleValues(crossinline default: () -> List<E> = { enumValues<E>().toList() }) = PropertyDelegateProvider { _: SkieTestRunnerConfiguration, property ->
        val properties = System.getProperties()
        val values = if (properties.containsKey(property.name)) {
            val rawValue = properties.getProperty(property.name)
            rawValue.split(',').map {
                enumValueOf<E>(it)
            }

        } else {
            default()
        }
        PropertyStorage(values)
    }

    private fun <T> List<T>.intersectOrKeepIfEmpty(other: List<T>): List<T> {
        return if (other.isNotEmpty()) {
            val otherAsSet = other.toSet()
            filter(otherAsSet::contains)
        } else {
            this
        }
    }

    private data class PropertyStorage<T>(val value: T): ReadOnlyProperty<SkieTestRunnerConfiguration, T> {
        override operator fun getValue(thisRef: SkieTestRunnerConfiguration, property: KProperty<*>): T = value
    }
}
