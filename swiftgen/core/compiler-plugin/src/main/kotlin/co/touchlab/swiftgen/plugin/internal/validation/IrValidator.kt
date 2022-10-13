package co.touchlab.swiftgen.plugin.internal.validation

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftgen.plugin.internal.validation.rules.ValidationRule
import co.touchlab.swiftgen.plugin.internal.validation.rules.sealed.SealedInteropRules
import co.touchlab.swiftgen.plugin.internal.validation.rules.validate
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class IrValidator(private val reporter: Reporter, private val configuration: Configuration) {

    private val classRules: List<ValidationRule<ClassDescriptor>> =
        SealedInteropRules.all

    fun validate(descriptorProvider: DescriptorProvider) {
        with(reporter) {
            with(configuration) {
                descriptorProvider.classDescriptors.forEach {
                    classRules.validate(it)
                }
            }
        }
    }
}