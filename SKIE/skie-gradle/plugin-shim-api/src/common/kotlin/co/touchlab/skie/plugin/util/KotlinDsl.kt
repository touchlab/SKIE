package co.touchlab.skie.plugin.util

import org.gradle.api.DomainObjectCollection
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskCollection

inline fun <reified S : Any> DomainObjectCollection<in S>.withType() =
    withType(S::class.java)

inline fun <reified S : Any> NamedDomainObjectCollection<in S>.withType(): NamedDomainObjectCollection<S> =
    withType(S::class.java)

inline fun <reified S : Task> TaskCollection<in S>.withType(): TaskCollection<S> =
    withType(S::class.java)

fun Configuration.exclude(group: String? = null, module: String? = null): Configuration =
    exclude(
        mapOfNonNullValuesOf(
            "group" to group,
            "module" to module,
        ),
    )

fun mapOfNonNullValuesOf(vararg entries: Pair<String, String?>): Map<String, String> =
    mutableMapOf<String, String>().apply {
        for ((k, v) in entries) {
            if (v != null) {
                put(k, v)
            }
        }
    }

inline fun <reified T : Named> ObjectFactory.named(name: String): T =
    named(T::class.java, name)

inline fun <reified T> ObjectFactory.newInstance(vararg parameters: Any): T =
    newInstance(T::class.java, *parameters)
