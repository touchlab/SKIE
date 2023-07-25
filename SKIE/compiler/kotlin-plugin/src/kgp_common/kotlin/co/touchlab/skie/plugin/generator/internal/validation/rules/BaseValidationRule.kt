package co.touchlab.skie.plugin.generator.internal.validation.rules

import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.configuration.values.ValidationSeverity
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

internal interface BaseValidationRule<D : DeclarationDescriptor> : ValidationRule<D> {

    val message: String

    context(Reporter, SkieConfiguration) override fun validate(descriptor: D) {
        if (isSatisfied(descriptor)) {
            return
        }

        val severity = severity(descriptor)

        report(severity, message, descriptor)
    }

    fun isSatisfied(descriptor: D): Boolean

    context(SkieConfiguration)
    fun severity(descriptor: D): Reporter.Severity
}

internal val ValidationSeverity.asReporterSeverity: Reporter.Severity
    get() = when (this) {
        ValidationSeverity.Error -> Reporter.Severity.Error
        ValidationSeverity.Warning -> Reporter.Severity.Warning
        ValidationSeverity.None -> Reporter.Severity.None
    }
