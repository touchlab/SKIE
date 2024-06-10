@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.internal.SwiftCompilerConfigurationKeys
import co.touchlab.skie.plugin.util.takeIf
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SkieBuildConfiguration @Inject constructor(objects: ObjectFactory) {

    /**
     * Enables Swift Library Evolution.
     *
     * Building with Swift Library Evolution increases compilation time, so it's recommended to use it only if you really need it.
     *
     * Note that Swift Library Evolution is required for building XCFrameworks so SKIE ignores this property in that case.
     * However, you should explicitly set this property to true if you need Swift Library Evolution for your XCFrameworks as this behavior might change in the future.
     */
    val enableSwiftLibraryEvolution: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    /**
     * If true, SKIE passes the `-no-clang-module-breadcrumbs` flag to the Swift compiler front end when building a static framework.
     *
     * Enable this flag when building a redistributable static framework (a framework that might be copied to other machines).
     *
     * This flag ensures that the compiler doesn't emit DWARF skeleton CUs for imported Clang modules, which would otherwise likely result in the following warning:
     *
     * ```
     * ...../xyz.pcm: No such file or directory
     * Linking a static library that was built with `-gmodules`, but the module cache was not found.
     * Redistributable static libraries should never be built with module debugging enabled.
     * The debug experience will be degraded due to incomplete debug information.
     * ```
     */
    val noClangModuleBreadcrumbsInStaticFrameworks: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    val enableParallelSwiftCompilation: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val enableConcurrentSkieCompilation: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val enableParallelSkieCompilation: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    /**
     * Additional Swift compiler arguments that will be passed to the Swift compiler.
     */
    val freeSwiftCompilerArgs: ListProperty<String> = objects.listProperty(String::class.java)

    /**
     * Configures SKIE to produce distributable frameworks.
     *
     * This option is only needed when the produced framework will be compiled against on another machine.
     * On the other hand, this option is unnecessary when the framework is only used locally, including as a dependency for a binary that is then distributed to other machines.
     *
     * Enables Swift Library Evolution and `-no-clang-module-breadcrumbs` flag.
     */
    fun produceDistributableFramework() {
        enableSwiftLibraryEvolution.set(true)
        noClangModuleBreadcrumbsInStaticFrameworks.set(true)
    }

    internal fun buildConfigurationFlags(): Set<SkieConfigurationFlag> =
        setOfNotNull(
            SkieConfigurationFlag.Build_SwiftLibraryEvolution takeIf enableSwiftLibraryEvolution,
            SkieConfigurationFlag.Build_NoClangModuleBreadcrumbsInStaticFramework takeIf noClangModuleBreadcrumbsInStaticFrameworks,
            SkieConfigurationFlag.Build_ParallelSwiftCompilation takeIf enableParallelSwiftCompilation,
            SkieConfigurationFlag.Build_ConcurrentSkieCompilation takeIf enableConcurrentSkieCompilation,
            SkieConfigurationFlag.Build_ParallelSkieCompilation takeIf enableParallelSkieCompilation,
        )

    internal fun buildItems(): Map<String, String?> = mapOf(
        SwiftCompilerConfigurationKeys.FreeCompilerArgs.name to SwiftCompilerConfigurationKeys.FreeCompilerArgs.serialize(freeSwiftCompilerArgs.get()),
    )
}
