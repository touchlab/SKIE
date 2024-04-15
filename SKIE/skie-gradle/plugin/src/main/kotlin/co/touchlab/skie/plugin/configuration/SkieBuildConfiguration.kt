package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.configuration.util.takeIf
import co.touchlab.skie.plugin.util.SkieTarget
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SkieBuildConfiguration @Inject constructor(objects: ObjectFactory) {

    /**
     * Swift Library Evolution is required by XCFramework artifacts,
     * so this flag is always true for any XCFramework target.
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
