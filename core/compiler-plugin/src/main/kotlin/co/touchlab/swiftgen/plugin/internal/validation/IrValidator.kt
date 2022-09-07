package co.touchlab.swiftgen.plugin.internal.validation

import co.touchlab.swiftgen.plugin.internal.util.IrWalker
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftgen.plugin.internal.validation.rules.ValidationRule
import co.touchlab.swiftgen.plugin.internal.validation.rules.sealed.SealedInteropRules
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

internal class IrValidator(private val reporter: Reporter) {

    private val classRules: List<ValidationRule<IrClass>> =
        SealedInteropRules.all

    fun verify(module: IrModuleFragment) {
        module.acceptVoid(Walker())
    }

    private inner class Walker : IrWalker {

        override fun visitClass(declaration: IrClass) {
            super.visitClass(declaration)

            classRules
                .filter { !it.isSatisfied(declaration) }
                .forEach { brokenRule ->
                    reporter.report(brokenRule.severity, brokenRule.message, declaration)
                }
        }
    }
}