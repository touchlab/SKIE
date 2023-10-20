package co.touchlab.skie.gradle

import org.gradle.api.Named
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Attribute

interface KotlinCompilerVersion : Named {
    companion object {

        val attribute = Attribute.of("co.touchlab.skie.kotlin.compiler.version", KotlinCompilerVersion::class.java)
    }
}
