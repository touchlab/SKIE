package co.touchlab.skie.test.trait.gradle

import co.touchlab.skie.test.util.KotlinVersion

interface GradleBuildFileBuilderTrait {
    fun buildGradle(kotlinVersion: KotlinVersion, block: BuildGradleBuilder.() -> Unit): String {
        val builder = BuildGradleBuilder(kotlinVersion)
        builder.block()
        return builder.toString()
    }
}
