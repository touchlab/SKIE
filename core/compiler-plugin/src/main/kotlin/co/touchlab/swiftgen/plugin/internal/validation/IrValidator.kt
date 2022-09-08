package co.touchlab.swiftgen.plugin.internal.validation

import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftgen.plugin.internal.validation.rules.ValidationRule
import co.touchlab.swiftgen.plugin.internal.validation.rules.sealed.SealedInteropRules
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class IrValidator(private val reporter: Reporter) {

    private val classRules: List<ValidationRule<ClassDescriptor>> =
        SealedInteropRules.all

    fun validate(descriptorProvider: DescriptorProvider) {
        descriptorProvider.classDescriptors.forEach {
            validate(it)
        }
    }

    private fun validate(descriptor: ClassDescriptor) {
        classRules
            .filter { !it.isSatisfied(descriptor) }
            .forEach { brokenRule ->
                reporter.report(brokenRule.severity, brokenRule.message, descriptor)
            }
    }
}