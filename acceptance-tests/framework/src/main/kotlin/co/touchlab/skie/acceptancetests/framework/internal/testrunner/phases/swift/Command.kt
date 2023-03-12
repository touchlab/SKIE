package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift

import java.util.concurrent.TimeUnit

internal data class CommandResult(val exitCode: Int, val stdOut: String)

internal fun String.execute(): CommandResult {
    val command = this.split("\\s".toRegex())

    val process = ProcessBuilder(*command.toTypedArray())
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .redirectErrorStream(true)
        .start()

    val hasExited = process.waitFor(1, TimeUnit.MINUTES)
    if (hasExited) {
        return CommandResult(
            exitCode = process.exitValue(),
            stdOut = process.inputStream.bufferedReader().readText(),
        )
    } else {
        val commandLine = process.info().commandLine().orElse("N/A")
        println("Process `${commandLine}` has not exited after 1 minute. Sending SIGTERM signal.")
        process.destroy()
        val hasBeenTerminated = process.waitFor(1, TimeUnit.MINUTES)
        if (hasBeenTerminated) {
            throw IllegalStateException("Process `${commandLine}` has been terminated after timing out!")
        } else {
            println("Process `${commandLine}` has not been terminated after 1 minute. Sending SIGKILL signal.")
            val hasBeenKilled = process.destroyForcibly().waitFor(5, TimeUnit.MINUTES)
            if (hasBeenKilled) {
                throw IllegalStateException("Process `${commandLine}` has been killed after termination timed out!")
            } else {
                throw UnknownError("Unable to terminate process `${commandLine}`! Restart of the machine is advised!")
            }
        }
    }
}
