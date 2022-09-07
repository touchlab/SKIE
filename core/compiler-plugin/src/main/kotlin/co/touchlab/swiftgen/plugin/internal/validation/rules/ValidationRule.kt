package co.touchlab.swiftgen.plugin.internal.validation.rules

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

internal interface ValidationRule<D : DeclarationDescriptor> {

    val severity: CompilerMessageSeverity

    val message: String

    fun isSatisfied(descriptor: D): Boolean
}