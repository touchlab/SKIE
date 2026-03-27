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

    private fun enhanceCode(swiftCode: String): String {
        val (cleanedCode, extraImports) = extractImports(swiftCode)

        return cleanedCode
            .let(::addExitCallCheck)
            .let(::addMainFunction)
            .let {
                addImports(it, extraImports)
            }
    }

    private fun extractImports(swiftCode: String): Pair<String, String> {
        val lines = swiftCode.lines()
        val cleanedCode = lines.filter { !it.startsWith("import ") }.joinToString("\n")
        val extraImports = lines.filter { it.startsWith("import ") }.joinToString("\n")
        return cleanedCode to extraImports
    }

    private fun addExitCallCheck(code: String): String =
        "$code\n\nfatalError(\"${TestResult.MissingExit.ERROR_MESSAGE}\")\n"

    private fun addMainFunction(code: String): String =
        "@main struct Main {\n\n    static func main() async {\n" +
            code.prependIndent("        ").trimEnd() +
            "\n    }\n}\n"

    private fun addImports(code: String, extraImports: String): String =
        "import Foundation\nimport Kotlin\n\n$extraImports\n\n$code"
}
