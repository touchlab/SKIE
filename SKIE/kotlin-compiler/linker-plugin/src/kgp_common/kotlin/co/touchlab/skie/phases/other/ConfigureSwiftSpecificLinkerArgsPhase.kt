package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.LinkPhase
import co.touchlab.skie.phases.configurables
import co.touchlab.skie.phases.konanConfig
import co.touchlab.skie.util.KotlinCompilerVersion
import co.touchlab.skie.util.current
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.konan.target.platformName
import java.io.File

object ConfigureSwiftSpecificLinkerArgsPhase : LinkPhase {

    context(LinkPhase.Context)
    override suspend fun execute() {
        val swiftLibSearchPaths = listOf(
            File(
                configurables.absoluteTargetToolchain,
                "lib/swift/${configurables.platformName().lowercase()}",
            ),
            File(configurables.absoluteTargetSysRoot, "usr/lib/swift"),
        ).flatMap { listOf("-L", it.absolutePath) }

        val otherLinkerFlags = listOf(
            "-rpath", "/usr/lib/swift", "-dead_strip",
        )

        konanConfig.configuration.addAll(KonanConfigKeys.LINKER_ARGS, swiftLibSearchPaths)
        konanConfig.configuration.addAll(KonanConfigKeys.LINKER_ARGS, otherLinkerFlags)
    }
}
