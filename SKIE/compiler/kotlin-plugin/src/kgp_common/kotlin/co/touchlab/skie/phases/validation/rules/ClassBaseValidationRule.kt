package co.touchlab.skie.phases.validation.rules

import co.touchlab.skie.configuration.Validation
import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.configuration.getConfiguration
import co.touchlab.skie.util.Reporter
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal interface ClassBaseValidationRule : BaseValidationRule<ClassDescriptor> {

    context(SkieConfiguration) override fun severity(descriptor: ClassDescriptor): Reporter.Severity =
        descriptor.getConfiguration(Validation.Severity).asReporterSeverity
}
