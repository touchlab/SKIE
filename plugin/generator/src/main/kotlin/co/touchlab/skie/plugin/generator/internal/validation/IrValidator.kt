package co.touchlab.skie.plugin.generator.internal.validation

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.generator.internal.util.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import co.touchlab.skie.plugin.generator.internal.validation.rules.ValidationRule
import co.touchlab.skie.plugin.generator.internal.validation.rules.sealed.SealedInteropRules
import co.touchlab.skie.plugin.generator.internal.validation.rules.validate
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class IrValidator(private val reporter: Reporter, private val configuration: Configuration) {

    private val classRules: List<ValidationRule<ClassDescriptor>> =
        SealedInteropRules.all

    fun validate(descriptorProvider: DescriptorProvider) {
        with(reporter) {
            with(configuration) {
                descriptorProvider.exportedClassDescriptors.forEach {
                    classRules.validate(it)
                }
            }
        }
    }
}
