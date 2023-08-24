package co.touchlab.skie.api.phases.debug

import co.touchlab.skie.api.phases.SkieLinkingPhase
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.configuration.SkieConfiguration
import co.touchlab.skie.plugin.api.skieBuildDirectory
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.util.Command
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed class DumpSwiftApiPhase(
    private val skieContext: SkieContext,
    private val framework: FrameworkLayout,
) : SkieLinkingPhase {

    class BeforeApiNotes(
        skieConfiguration: SkieConfiguration,
        skieContext: SkieContext,
        framework: FrameworkLayout,
    ) : DumpSwiftApiPhase(skieContext, framework) {

        override val isActive: Boolean = SkieConfigurationFlag.Debug_DumpSwiftApiBeforeApiNotes in skieConfiguration.enabledConfigurationFlags
    }

    class AfterApiNotes(
        skieConfiguration: SkieConfiguration,
        skieContext: SkieContext,
        framework: FrameworkLayout,
    ) : DumpSwiftApiPhase(skieContext, framework) {

        override val isActive: Boolean = SkieConfigurationFlag.Debug_DumpSwiftApiAfterApiNotes in skieConfiguration.enabledConfigurationFlags
    }

    override fun execute() {
        val moduleName = framework.moduleName
        val apiFileBaseName = "${moduleName}_${this::class.simpleName}"
        val apiFile = skieContext.skieBuildDirectory.debug.dumps.apiFile(apiFileBaseName)
        val logFile = skieContext.skieBuildDirectory.debug.logs.apiFile(apiFileBaseName)

        val command = Command(
            "zsh",
            "-c",
            """echo "import Kotlin\n:type lookup $moduleName" | swift repl -F "${framework.framework.parentFile.absolutePath}" > "${apiFile.absolutePath}"""",
        )

        withBlockingTimeoutOrNull(15.seconds) {
            command.execute(handleError = false, logFile = logFile)
        } ?: error("${this::class.qualifiedName} timed out. This is likely due to exporting a type with the same name as the produced framework.")
    }

    private fun <T : Any> withBlockingTimeoutOrNull(timeout: Duration, block: () -> T): T? {
        val channel = Channel<T>()

        val thread = Thread {
            val value = block()

            runBlocking {
                channel.send(value)
            }
        }

        thread.start()

        return runBlocking {
            try {
                withTimeoutOrNull(timeout) {
                    channel.receive()
                }
            } finally {
                thread.interrupt()
            }
        }
    }
}
