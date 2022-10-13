package co.touchlab.swiftgen.plugin.internal.validation.rules

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.configuration.ConfigurationKeys
import co.touchlab.swiftgen.plugin.internal.configuration.getConfiguration
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal interface ClassBaseValidationRule : BaseValidationRule<ClassDescriptor> {

    context(Configuration) override fun severity(descriptor: ClassDescriptor): Reporter.Severity =
        descriptor.getConfiguration(ConfigurationKeys.Validation.Severity).asReporterSeverity
}