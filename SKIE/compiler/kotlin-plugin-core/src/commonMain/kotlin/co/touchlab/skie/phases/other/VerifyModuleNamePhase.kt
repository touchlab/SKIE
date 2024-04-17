package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.ClassExportCorePhase
import co.touchlab.skie.phases.ClassExportPhase

object VerifyModuleNamePhase : ClassExportCorePhase {

    private val problematicKeywords = listOf(
        "associatedtype",
        "class",
        "deinit",
        "enum",
        "extension",
        "fileprivate",
        "func",
        "import",
        "init",
        "inout",
        "internal",
        "let",
        "open",
        "operator",
        "private",
        "protocol",
        "Protocol",
        "public",
        "static",
        "struct",
        "subscript",
        "typealias",
        "var",
        "break",
        "case",
        "continue",
        "default",
        "defer",
        "do",
        "else",
        "fallthrough",
        "for",
        "guard",
        "if",
        "in",
        "repeat",
        "return",
        "switch",
        "where",
        "while",
        "as",
        "async",
        "await",
        "any",
        "catch",
        "false",
        "is",
        "nil",
        "rethrows",
        "super",
        "self",
        "Self",
        "some",
        "throw",
        "throws",
        "true",
        "try",
        "Type",
        "Any",
        "framework",
    )

    context(ClassExportPhase.Context)
    override suspend fun execute() {
        check(framework.moduleName !in problematicKeywords) {
            "The name '${framework.moduleName}' is a reserved keyword in Swift and cannot be used as framework name with SKIE."
        }
    }
}

