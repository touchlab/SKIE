package co.touchlab.swiftgen.plugin.internal.validation.rules

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.ir.IrElement

internal interface ValidationRule<IR : IrElement> {

    val severity: CompilerMessageSeverity

    val message: String

    fun isSatisfied(element: IR): Boolean
}