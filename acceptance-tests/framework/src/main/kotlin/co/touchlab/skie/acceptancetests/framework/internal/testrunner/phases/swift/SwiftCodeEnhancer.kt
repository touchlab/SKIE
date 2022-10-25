package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift

import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.TestResult
import java.nio.file.Path
import kotlin.io.path.writeText

internal class SwiftCodeEnhancer(private val tempFileSystem: TempFileSystem) {

    fun enhance(swiftCode: String): Path {
        val swiftCopy = tempFileSystem.createFile("swift-source.swift")

        val enhancedCode = enhanceCode(swiftCode)

        swiftCopy.writeText(enhancedCode)

        return swiftCopy
    }

    private fun enhanceCode(swiftCode: String): String =
        swiftCode
            .let(::addExitCallCheck)
            .let(::addMainFunction)
            .let(::addImports)

    private fun addExitCallCheck(code: String): String =
        "$code\n\nfatalError(\"${TestResult.MissingExit.ERROR_MESSAGE}\")\n"

    private fun addMainFunction(code: String): String =
        "@main struct Main {\n\n    static func main() async {\n" +
            code.prependIndent("        ").trimEnd() +
            "\n    }\n}\n"

    private fun addImports(code: String): String =
        "import Foundation\nimport Kotlin\n\n$code"
}
