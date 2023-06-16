package co.touchlab.skie.gradle

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeMatchingStrategy

interface KotlinCompilerVersion: Named {
    companion object {
        val attribute = Attribute.of("co.touchlab.skie.kotlin.compiler.version", KotlinCompilerVersion::class.java)

        fun registerIn(dependencies: DependencyHandler) {
            dependencies.attributesSchema.attribute(attribute) {
                ordered { lhs, rhs ->
                    val lhsToolingVersion = KotlinToolingVersion(lhs.name)
                    val rhsToolingVersion = KotlinToolingVersion(rhs.name)

                    lhsToolingVersion.compareTo(rhsToolingVersion)
                }
            }
        }
    }
}
