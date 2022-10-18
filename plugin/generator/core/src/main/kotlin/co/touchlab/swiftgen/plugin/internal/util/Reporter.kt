package co.touchlab.swiftgen.plugin.internal.util

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.cli.jvm.compiler.report
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi

internal class Reporter(private val compilerConfiguration: CompilerConfiguration) {

    fun report(severity: Severity, message: String, declaration: DeclarationDescriptor?) {
        val location = MessageUtil.psiElementToMessageLocation(declaration?.findPsi())?.let {
            CompilerMessageLocation.create(it.path, it.line, it.column, it.lineContent)
        }

        when (severity) {
            Severity.Error -> compilerConfiguration.report(CompilerMessageSeverity.ERROR, message, location)
            Severity.Warning -> compilerConfiguration.report(CompilerMessageSeverity.WARNING, message, location)
            Severity.None -> {}
        }
    }

    enum class Severity {
        Error, Warning, None
    }
}