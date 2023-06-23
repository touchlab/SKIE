package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.version.target.Target

enum class AcceptanceTestsComponent(override val value: String): Target.Component {
    functional("functional"),
    typeMapping("type-mapping"),
    stdlib("stdlib"),
    libraries("libraries")
}

val Target.acceptanceTest: AcceptanceTestsComponent
    get() = component()
