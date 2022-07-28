package co.touchlab.swiftgen.sealed.acceptancetests.framework.internal.testrunner

import java.nio.file.Path
import java.util.concurrent.TimeUnit

internal data class CommandResult(val exitCode: Int, val stdOut: String, val stdErr: String)

internal fun String.execute(): CommandResult {
    val command = this.split("\\s".toRegex())
    val process = ProcessBuilder(*command.toTypedArray())
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    process.waitFor(1, TimeUnit.MINUTES)

    return CommandResult(
        exitCode = process.exitValue(),
        stdOut = process.inputStream.bufferedReader().readText(),
        stdErr = process.errorStream.bufferedReader().readText(),
    )
}
