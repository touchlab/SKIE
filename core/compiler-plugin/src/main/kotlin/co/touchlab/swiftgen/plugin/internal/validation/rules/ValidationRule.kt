package co.touchlab.swiftgen.plugin.internal.validation.rules

import co.touchlab.swiftgen.configuration.values.ValidationSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

internal interface ValidationRule<D : DeclarationDescriptor> {

    val message: String

    fun isSatisfied(descriptor: D): Boolean

    fun severity(descriptor: D): CompilerMessageSeverity
}

context(ValidationRule<*>)
fun ValidationSeverity.toCompilerMessageSeverity(): CompilerMessageSeverity = when (this) {
    ValidationSeverity.Error -> CompilerMessageSeverity.ERROR
    ValidationSeverity.Warning -> CompilerMessageSeverity.WARNING
}
