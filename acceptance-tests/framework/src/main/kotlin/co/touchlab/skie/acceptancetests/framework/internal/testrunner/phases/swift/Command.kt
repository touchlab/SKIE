package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift

import org.jetbrains.kotlin.konan.file.createTempFile
import org.jetbrains.kotlin.library.impl.javaFile

import java.util.concurrent.TimeUnit
import java.util.logging.Logger

internal class CommandResult(val exitCode: Int, val stdOut: String)

internal fun String.execute(logger: Logger = Logger.getLogger("String.execute")): CommandResult {
    val command = this.split("\\s".toRegex())

    val tempOutputFile = createTempFile("skie-test-runner", ".out").also { it.deleteOnExit() }
    val process = ProcessBuilder(*command.toTypedArray())
        .redirectOutput(ProcessBuilder.Redirect.to(tempOutputFile.javaFile()))
        .redirectErrorStream(true)
        .start()

    val hasExited = process.waitFor(1, TimeUnit.MINUTES)
    if (!hasExited) {
        val commandLine = process.info().commandLine().orElse("N/A")
        logger.warning("Process `${commandLine}` has not exited after 1 minute. Sending SIGTERM signal.")
        process.destroy()
        val hasBeenTerminated = process.waitFor(1, TimeUnit.MINUTES)
        if (hasBeenTerminated) {
            logger.warning("Process `${commandLine}` has been terminated after timing out!")
        } else {
            logger.warning("Process `${commandLine}` has not been terminated after 1 minute. Sending SIGKILL signal.")
            val hasBeenKilled = process.destroyForcibly().waitFor(5, TimeUnit.MINUTES)
            if (hasBeenKilled) {
                logger.warning("Process `${commandLine}` has been killed after termination timed out!")
            } else {
                logger.severe("Unable to terminate process `${commandLine}`! Restart of the machine is advised!")
            }
        }
    }

    return CommandResult(
        exitCode = process.exitValue(),
        stdOut = tempOutputFile.javaFile().readText(),
    )
}
