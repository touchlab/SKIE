package co.touchlab.skie.util

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.cli.jvm.compiler.report
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi

// WIP 3 remove unused code and reformat

class Reporter(private val compilerConfiguration: CompilerConfiguration) {

    fun report(severity: Severity, message: String, declaration: DeclarationDescriptor? = null) {
        val location = MessageUtil.psiElementToMessageLocation(declaration?.findPsi())?.let {
            CompilerMessageLocation.create(it.path, it.line, it.column, it.lineContent)
        }

        when (severity) {
            Severity.Error -> compilerConfiguration.report(CompilerMessageSeverity.ERROR, message, location)
            Severity.Warning -> compilerConfiguration.report(CompilerMessageSeverity.WARNING, message, location)
            Severity.None -> {}
        }
    }

    fun error(message: String, declaration: DeclarationDescriptor? = null) {
        report(Severity.Error, message, declaration)
    }

    fun warning(message: String, declaration: DeclarationDescriptor? = null) {
        report(Severity.Warning, message, declaration)
    }

    enum class Severity {
        Error, Warning, None
    }
}
