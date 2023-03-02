package co.touchlab.skie.api.phases.debug

import co.touchlab.skie.api.phases.SkieLinkingPhase
import co.touchlab.skie.plugin.Command
import co.touchlab.skie.plugin.api.debug.DumpSwiftApiPoint
import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import org.jetbrains.kotlin.backend.common.CommonBackendContext

class DumpSwiftApiPhase(
    val point: DumpSwiftApiPoint,
    val context: CommonBackendContext,
    val framework: FrameworkLayout,
): SkieLinkingPhase {

    override val isActive: Boolean = point in context.skieContext.dumpSwiftApiPoints

    override fun execute() {
        val moduleName = framework.moduleName
        val apiFileName = "${moduleName}_${point.name}"
        val apiFile = context.skieContext.debugInfoDirectory.dumps.resolve(
            "$apiFileName.swift"
        )
        val logFile = context.skieContext.debugInfoDirectory.logs.resolve(
            "$apiFileName.log"
        )

        Command(
            "zsh",
            "-c",
            """echo "import Kotlin\n:type lookup $moduleName" | swift repl -F "${framework.framework.parentFile.absolutePath}" > "${apiFile.absolutePath}""""
        ).execute(handleError = false, logFile = logFile)


    }
}
