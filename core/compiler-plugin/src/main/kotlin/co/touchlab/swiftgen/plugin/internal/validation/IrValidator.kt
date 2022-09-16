package co.touchlab.swiftgen.plugin.internal.validation

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftgen.plugin.internal.validation.rules.ValidationRule
import co.touchlab.swiftgen.plugin.internal.validation.rules.sealed.SealedInteropRules
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class IrValidator(private val reporter: Reporter, configuration: Configuration) {

    private val classRules: List<ValidationRule<ClassDescriptor>> =
        SealedInteropRules(configuration).all

    fun validate(descriptorProvider: DescriptorProvider) {
        descriptorProvider.classDescriptors.forEach {
            validate(it)
        }
    }

    private fun validate(descriptor: ClassDescriptor) {
        classRules
            .filter { !it.isSatisfied(descriptor) }
            .forEach { brokenRule ->
                val severity = brokenRule.severity(descriptor)

                reporter.report(severity, brokenRule.message, descriptor)
            }
    }
}