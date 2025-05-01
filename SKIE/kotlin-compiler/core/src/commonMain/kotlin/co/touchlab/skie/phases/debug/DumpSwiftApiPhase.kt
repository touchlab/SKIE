package co.touchlab.skie.phases.debug

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.util.Command
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeoutOrNull

sealed class DumpSwiftApiPhase : SirPhase {

    object BeforeApiNotes : DumpSwiftApiPhase() {

        context(SirPhase.Context)
        override fun isActive(): Boolean = SkieConfigurationFlag.Debug_DumpSwiftApiBeforeApiNotes.isEnabled
    }

    object AfterApiNotes : DumpSwiftApiPhase() {

        context(SirPhase.Context)
        override fun isActive(): Boolean = SkieConfigurationFlag.Debug_DumpSwiftApiAfterApiNotes.isEnabled
    }

    context(SirPhase.Context)
    override suspend fun execute() {
        val moduleName = framework.frameworkName
        val apiFileBaseName = "${moduleName}_${this::class.simpleName}"
        val apiFile = skieBuildDirectory.debug.dumps.apiFile(apiFileBaseName)
        val logFile = skieBuildDirectory.debug.logs.apiFile(apiFileBaseName)

        val command = Command(
            "zsh",
            "-c",
            """echo "import Kotlin\n:type lookup $moduleName" | swift repl -F "${framework.frameworkDirectory.parentFile.absolutePath}" > "${apiFile.absolutePath}"""",
        )

        try {
            withTimeoutOrNull(15.seconds) {
                command.execute(handleError = false, logFile = logFile)
            }
        } catch (e: TimeoutCancellationException) {
            error(
                "${this::class.qualifiedName} timed out. This is likely due to exporting a type with the same name as the produced framework.",
            )
        }
    }
}
