package co.touchlab.skie.test.util

import java.io.File
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import kotlin.io.path.readText
import kotlin.io.path.writeText

fun String.execute(workDir: File, logger: Logger = Logger.getLogger("String.execute")): CommandResult {
    val command = this.split("\\s".toRegex())

    val tempOutputFile = kotlin.io.path.createTempFile(workDir.toPath(), "skie-test-runner", ".out")
    tempOutputFile.writeText("Running command:\n$this\nin ${workDir.toPath()}\n")
    val process = ProcessBuilder(*command.toTypedArray())
        .redirectOutput(ProcessBuilder.Redirect.appendTo(tempOutputFile.toFile()))
        .redirectErrorStream(true)
        .directory(workDir)
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
        stdOut = tempOutputFile.readText(),
    )
}
