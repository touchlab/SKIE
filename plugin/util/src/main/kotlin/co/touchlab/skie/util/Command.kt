package co.touchlab.skie.util
/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File
import java.nio.file.Files

open class Command(initialCommand: List<String>) {

    constructor(tool: String) : this(listOf(tool))
    constructor(vararg command: String) : this(command.toList<String>())

    protected val command = initialCommand.toMutableList()

    val argsWithExecutable: List<String> = command

    val args: List<String>
        get() = command.drop(1)

    var workingDirectory: File? = null

    operator fun String.unaryPlus(): Command {
        command += this
        return this@Command
    }

    operator fun List<String>.unaryPlus(): Command {
        command.addAll(this)
        return this@Command
    }

    operator fun File.unaryPlus(): Command {
        command += absolutePath
        return this@Command
    }

    var logger: ((() -> String) -> Unit)? = null

    fun logWith(newLogger: ((() -> String) -> Unit)): Command {
        logger = newLogger
        return this
    }

    fun execute(
        withErrors: Boolean = true,
        handleError: Boolean = true,
        logFile: File? = null,
    ): Result {
        log()

        // Note: getting process output could be done without redirecting to temporary file,
        // however this would require managing a thread to read `process.inputStream` because
        // it may have limited capacity.
        val tempOutputFile = Files.createTempFile(null, null).toFile().also { it.deleteOnExit() }
        logFile?.apply {
            // FIXME: This doesn't put quotes around arguments with spaces
            appendText(command.joinToString(" ", postfix = "\n\n\n"))
        }

        try {
            val builder = ProcessBuilder(command)
                .directory(workingDirectory)
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.appendTo(tempOutputFile))
                .redirectErrorStream(withErrors)
            val process = builder.start()
            val code = process.waitFor()
            val result = tempOutputFile.readLines()

            if (handleError) {
                handleExitCode(code, result)
            }

            return Result(code, result)
        } finally {
            logFile?.apply { appendText(tempOutputFile.readText()) }
            tempOutputFile.delete()
        }
    }

    class Result(val exitCode: Int, val outputLines: List<String>)

    private fun handleExitCode(code: Int, output: List<String> = emptyList()) {
        if (code != 0) error(
            """
            The ${command[0]} command returned non-zero exit code: $code.
            output:
            """.trimIndent() + "\n${output.joinToString("\n")}"
        )
    }

    private fun log() {
        logger?.let { it { command.joinToString(" ") } }
    }
}
