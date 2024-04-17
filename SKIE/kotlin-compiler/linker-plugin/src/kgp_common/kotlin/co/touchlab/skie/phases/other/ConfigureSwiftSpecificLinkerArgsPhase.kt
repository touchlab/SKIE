package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.LinkCompilerPhase
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.konan.target.platformName
import java.io.File

object ConfigureSwiftSpecificLinkerArgsPhase : LinkCompilerPhase {

    context(LinkCompilerPhase.Context)
    override suspend fun execute() {
        val swiftLibSearchPaths = listOf(
            File(configurables.absoluteTargetToolchain, "usr/lib/swift/${configurables.platformName().lowercase()}"),
            File(configurables.absoluteTargetSysRoot, "usr/lib/swift"),
        ).flatMap { listOf("-L", it.absolutePath) }

        val otherLinkerFlags = listOf(
            "-rpath", "/usr/lib/swift", "-dead_strip",
        )

        konanConfig.configuration.addAll(KonanConfigKeys.LINKER_ARGS, swiftLibSearchPaths)
        konanConfig.configuration.addAll(KonanConfigKeys.LINKER_ARGS, otherLinkerFlags)
    }
}
