package co.touchlab.skie.plugin.generator.internal.validation

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import co.touchlab.skie.plugin.generator.internal.validation.rules.ValidationRule
import co.touchlab.skie.plugin.generator.internal.validation.rules.sealed.SealedInteropRules
import co.touchlab.skie.plugin.generator.internal.validation.rules.validate
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class IrValidator(
    private val descriptorProvider: DescriptorProvider,
    private val reporter: Reporter,
    private val configuration: Configuration,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    private val classRules: List<ValidationRule<ClassDescriptor>> =
        SealedInteropRules.all

    override fun execute() {
        with(reporter) {
            with(configuration) {
                descriptorProvider.exportedClassDescriptors.forEach {
                    classRules.validate(it)
                }
            }
        }
    }
}
