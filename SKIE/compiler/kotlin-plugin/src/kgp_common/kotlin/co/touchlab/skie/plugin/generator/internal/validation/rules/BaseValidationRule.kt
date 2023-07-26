package co.touchlab.skie.plugin.generator.internal.validation.rules

import co.touchlab.skie.configuration.Validation
import co.touchlab.skie.plugin.api.configuration.SkieConfiguration
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

internal val Validation.Severity.Level.asReporterSeverity: Reporter.Severity
    get() = when (this) {
        Validation.Severity.Level.Error -> Reporter.Severity.Error
        Validation.Severity.Level.Warning -> Reporter.Severity.Warning
        Validation.Severity.Level.None -> Reporter.Severity.None
    }
