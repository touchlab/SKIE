package co.touchlab.swiftgen.plugin.internal.validation.rules

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.configuration.values.ValidationSeverity
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

internal interface BaseValidationRule<D : DeclarationDescriptor> : ValidationRule<D> {

    val message: String

    context(Reporter, Configuration) override fun validate(descriptor: D) {
        if (isSatisfied(descriptor)) {
            return
        }

        val severity = severity(descriptor)

        report(severity, message, descriptor)
    }

    fun isSatisfied(descriptor: D): Boolean

    context(Configuration)
    fun severity(descriptor: D): Reporter.Severity
}

internal val ValidationSeverity.asReporterSeverity: Reporter.Severity
    get() = when (this) {
        ValidationSeverity.Error -> Reporter.Severity.Error
        ValidationSeverity.Warning -> Reporter.Severity.Warning
        ValidationSeverity.None -> Reporter.Severity.None
    }