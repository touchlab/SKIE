package co.touchlab.skie.plugin.generator.internal.validation

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import co.touchlab.skie.plugin.generator.internal.validation.rules.ValidationRule
import co.touchlab.skie.plugin.generator.internal.validation.rules.sealed.SealedInteropRules
import co.touchlab.skie.plugin.generator.internal.validation.rules.validate
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class IrValidator(
    private val skieContext: SkieContext,
    private val descriptorProvider: DescriptorProvider,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    private val classRules: List<ValidationRule<ClassDescriptor>> =
        SealedInteropRules.all

    override fun runObjcPhase() {
        with(skieContext.reporter) {
            with(skieContext.skieConfiguration) {
                descriptorProvider.exposedClasses.forEach {
                    classRules.validate(it)
                }
            }
        }
    }
}
