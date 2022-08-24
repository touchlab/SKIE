package co.touchlab.swiftgen.plugin.internal.util

import org.jetbrains.kotlin.backend.jvm.ir.psiElement
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.cli.jvm.compiler.report
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrDeclaration

class Reporter(private val compilerConfiguration: CompilerConfiguration) {

    fun report(severity: CompilerMessageSeverity, message: String, declaration: IrDeclaration?) {
        val location = MessageUtil.psiElementToMessageLocation(declaration?.psiElement)?.let {
            CompilerMessageLocation.create(it.path, it.line, it.column, it.lineContent)
        }

        compilerConfiguration.report(severity, message, location)
    }
}