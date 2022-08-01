package co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner

import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystem
import co.touchlab.swiftgen.acceptancetests.framework.internal.TestResult
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

internal class SwiftCodeEnhancer(private val tempFileSystem: TempFileSystem) {

    fun enhance(swiftFile: Path): Path {
        val swiftCopy = tempFileSystem.createFile(".swift")

        val swiftCode = swiftFile.readText()

        val modifiedSwiftCode = swiftCode
            .let(::addImports)
            .let(::addExitCallCheck)

        swiftCopy.writeText(modifiedSwiftCode)

        return swiftCopy
    }

    private fun addImports(code: String): String =
        "import Foundation\nimport Kotlin\n\n$code"

    private fun addExitCallCheck(code: String): String =
        "$code\n\nfatalError(\"${TestResult.MissingExit.ERROR_MESSAGE}\")"
}