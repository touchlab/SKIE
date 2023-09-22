package co.touchlab.skie.phases.debug

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.util.Command
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed class DumpSwiftApiPhase : SirPhase {

    object BeforeApiNotes : DumpSwiftApiPhase() {

        context(SirPhase.Context)
        override fun isActive(): Boolean =
            SkieConfigurationFlag.Debug_DumpSwiftApiBeforeApiNotes in skieConfiguration.enabledConfigurationFlags
    }

    object AfterApiNotes : DumpSwiftApiPhase() {

        context(SirPhase.Context)
        override fun isActive(): Boolean =
            SkieConfigurationFlag.Debug_DumpSwiftApiAfterApiNotes in skieConfiguration.enabledConfigurationFlags
    }

    context(SirPhase.Context)
    override fun execute() {
        val moduleName = framework.moduleName
        val apiFileBaseName = "${moduleName}_${this::class.simpleName}"
        val apiFile = skieBuildDirectory.debug.dumps.apiFile(apiFileBaseName)
        val logFile = skieBuildDirectory.debug.logs.apiFile(apiFileBaseName)

        val command = Command(
            "zsh",
            "-c",
            """echo "import Kotlin\n:type lookup $moduleName" | swift repl -F "${framework.framework.parentFile.absolutePath}" > "${apiFile.absolutePath}"""",
        )

        withBlockingTimeoutOrNull(15.seconds) {
            command.execute(handleError = false, logFile = logFile)
        }
            ?: error("${this::class.qualifiedName} timed out. This is likely due to exporting a type with the same name as the produced framework.")
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
