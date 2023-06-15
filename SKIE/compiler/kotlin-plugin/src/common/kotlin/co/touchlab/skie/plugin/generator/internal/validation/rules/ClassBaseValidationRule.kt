package co.touchlab.skie.plugin.generator.internal.validation.rules

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.gradle.Validation
import co.touchlab.skie.plugin.generator.internal.configuration.getConfiguration
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal interface ClassBaseValidationRule : BaseValidationRule<ClassDescriptor> {

    context(Configuration) override fun severity(descriptor: ClassDescriptor): Reporter.Severity =
        descriptor.getConfiguration(Validation.Severity).asReporterSeverity
}
