package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift

import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestLogger
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.CompilerArgumentsProvider
import org.jetbrains.kotlin.konan.target.TargetTriple
import org.jetbrains.kotlin.konan.target.withOSVersion
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class SwiftTestCompiler(
    private val tempFileSystem: TempFileSystem,
    private val testLogger: TestLogger,
    private val target: CompilerArgumentsProvider.Target,
) {

    fun compile(kotlinFramework: Path, swiftFile: Path): IntermediateResult<Path> {
        val output = tempFileSystem.createFile("swift-binary")

        val command = createCompileSwiftCommand(kotlinFramework, swiftFile, output)

        val result = command.execute()

        testLogger.appendSection("Swift compiler", result.stdOut)

        return interpretResult(result, output)
    }

    private fun createCompileSwiftCommand(
        kotlinFramework: Path,
        swiftFile: Path,
        output: Path,
    ): String = listOf(
        "/usr/bin/xcrun",
        "-sdk", target.sdk,
        "swiftc",
        swiftFile.absolutePathString(),
        "-F",
        kotlinFramework.parent.absolutePathString(),
        // Adds rpath so that the binary can find the framework when linking dynamically
        "-Xlinker", "-rpath", "-Xlinker", "@executable_path",
        "-v",
        "-target", target.targetTriple.toString(),
        // "-driver-time-compilation",
        // "-print-educational-notes",
        // "-Xfrontend", "-debug-constraints",
        "-o",
        output.absolutePathString(),
        // Workaround for https://github.com/apple/swift/issues/55127
        "-parse-as-library",
    ).joinToString(" ")

    private fun interpretResult(result: CommandResult, output: Path): IntermediateResult<Path> =
        if (result.exitCode == 0) {
            IntermediateResult.Value(output)
        } else {
            IntermediateResult.Error(TestResult.SwiftCompilationError(result.stdOut))
        }
}
