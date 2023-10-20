package co.touchlab.skie.gradle.version.target

import groovy.lang.Closure
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

interface MultiDimensionTargetDependencyHandler : KotlinDependencyHandler {

    /**
     * Adds a dependency as compileOnly for the main compilation and as implementation for the test compilation.
     */
    fun weak(dependencyNotation: Any): Dependency?

    /**
     * Adds a dependency as compileOnly for the main compilation and as implementation for the test compilation.
     */
    fun weak(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit): ExternalModuleDependency

    /**
     * Adds a dependency as compileOnly for the main compilation and as implementation for the test compilation.
     */
    fun <T : Dependency> weak(dependency: T, configure: T.() -> Unit): T

    /**
     * Adds a dependency as compileOnly for the main compilation and as implementation for the test compilation.
     */
    fun weak(dependencyNotation: String, configure: Closure<*>) =
        weak(dependencyNotation) { project.configure(this, configure) }

    /**
     * Adds a dependency as compileOnly for the main compilation and as implementation for the test compilation.
     */
    fun <T : Dependency> weak(dependency: T, configure: Closure<*>) =
        weak(dependency) { project.configure(this, configure) }

    /**
     * Adds a dependency only if this source set is for the test compilation.
     */
    fun testOnly(dependencyNotation: Any): Dependency?

    /**
     * Adds a dependency only if this source set is for the test compilation.
     */
    fun testOnly(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit): ExternalModuleDependency?

    /**
     * Adds a dependency only if this source set is for the test compilation.
     */
    fun <T : Dependency> testOnly(dependency: T, configure: T.() -> Unit): T?

    /**
     * Adds a dependency only if this source set is for the test compilation.
     */
    fun testOnly(dependencyNotation: String, configure: Closure<*>) =
        testOnly(dependencyNotation) { project.configure(this, configure) }

    /**
     * Adds a dependency only if this source set is for the test compilation.
     */
    fun <T : Dependency> testOnly(dependency: T, configure: Closure<*>) =
        testOnly(dependency) { project.configure(this, configure) }
}
