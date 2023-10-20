package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.version.target.SourceSet
import co.touchlab.skie.gradle.version.target.Target

enum class AcceptanceTestsComponent(override val value: String) : Target.Component {
    functional("functional"),
    typeMapping("type-mapping"),
    stdlib("stdlib"),
    libraries("libraries")
}

val Target.acceptanceTest: AcceptanceTestsComponent
    get() = component()

val SourceSet.acceptanceTest: AcceptanceTestsComponent?
    get() = componentSet<AcceptanceTestsComponent>().components.singleOrNull()
