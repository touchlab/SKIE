package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.LinkPhase
import co.touchlab.skie.phases.configurables
import co.touchlab.skie.phases.konanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.konan.target.platformName
import java.io.File

object ConfigureSwiftSpecificLinkerArgsPhase : LinkPhase {

    context(context: LinkPhase.Context)
    override suspend fun execute() {
        val swiftLibSearchPaths = listOf(
            File(
                context.configurables.absoluteTargetToolchain,
                "lib/swift/${context.configurables.platformName().lowercase()}",
            ),
            File(context.configurables.absoluteTargetSysRoot, "usr/lib/swift"),
        ).flatMap { listOf("-L", it.absolutePath) }

        val otherLinkerFlags = listOf(
            "-rpath", "/usr/lib/swift", "-dead_strip",
        )

        context.konanConfig.configuration.addAll(KonanConfigKeys.LINKER_ARGS, swiftLibSearchPaths)
        context.konanConfig.configuration.addAll(KonanConfigKeys.LINKER_ARGS, otherLinkerFlags)
    }
}
