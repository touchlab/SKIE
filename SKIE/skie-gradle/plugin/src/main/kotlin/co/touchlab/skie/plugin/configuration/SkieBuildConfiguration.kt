package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.configuration.util.takeIf
import co.touchlab.skie.plugin.util.SkieTarget
import org.gradle.api.model.ObjectFactory
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
    val enableParallelSwiftCompilation: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val enableConcurrentSkieCompilation: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val enableParallelSkieCompilation: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    internal fun buildConfigurationFlags(outputKind: SkieTarget.OutputKind): Set<SkieConfigurationFlag> =
        setOfNotNull(
            decideSwiftLibraryEvolutionFlag(outputKind),
            SkieConfigurationFlag.Build_ParallelSwiftCompilation takeIf enableParallelSwiftCompilation,
            SkieConfigurationFlag.Build_ConcurrentSkieCompilation takeIf enableConcurrentSkieCompilation,
            SkieConfigurationFlag.Build_ParallelSkieCompilation takeIf enableParallelSkieCompilation,
        )

    private fun decideSwiftLibraryEvolutionFlag(outputKind: SkieTarget.OutputKind) =
        if (outputKind == SkieTarget.OutputKind.XCFramework) {
            SkieConfigurationFlag.Build_SwiftLibraryEvolution
        } else {
            SkieConfigurationFlag.Build_SwiftLibraryEvolution takeIf enableSwiftLibraryEvolution
        }
}
