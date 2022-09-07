package co.touchlab.swiftgen.plugin.internal.validation

import co.touchlab.swiftgen.plugin.internal.util.RecursiveClassDescriptorVisitor
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftgen.plugin.internal.validation.rules.ValidationRule
import co.touchlab.swiftgen.plugin.internal.validation.rules.sealed.SealedInteropRules
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class IrValidator(private val reporter: Reporter) {

    private val classRules: List<ValidationRule<ClassDescriptor>> =
        SealedInteropRules.all

    fun verify(module: IrModuleFragment) {
        module.descriptor.accept(Visitor(), Unit)
    }

    private inner class Visitor : RecursiveClassDescriptorVisitor() {

        override fun visitClass(descriptor: ClassDescriptor) {
            classRules
                .filter { !it.isSatisfied(descriptor) }
                .forEach { brokenRule ->
                    reporter.report(brokenRule.severity, brokenRule.message, descriptor)
                }
        }
    }
}