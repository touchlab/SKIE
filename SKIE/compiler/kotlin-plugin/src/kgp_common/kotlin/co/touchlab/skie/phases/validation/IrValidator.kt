package co.touchlab.skie.phases.validation

import co.touchlab.skie.phases.SkieContext
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.phases.SkieCompilationPhase
import co.touchlab.skie.phases.validation.rules.ValidationRule
import co.touchlab.skie.phases.validation.rules.sealed.SealedInteropRules
import co.touchlab.skie.phases.validation.rules.validate
import org.jetbrains.kotlin.descriptors.ClassDescriptor

// WIP Remove
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
