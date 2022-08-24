package co.touchlab.swiftgen.plugin.internal.validation

import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftgen.plugin.internal.validation.rules.ValidationRule
import co.touchlab.swiftgen.plugin.internal.validation.rules.sealed.SealedInteropRules
import org.jetbrains.kotlin.ir.declarations.IrClass

internal class IrValidator(private val reporter: Reporter) {

    private val classRules: List<ValidationRule<IrClass>> =
        SealedInteropRules.all

    fun verify(declaration: IrClass) {
        classRules
            .filter { !it.isSatisfied(declaration) }
            .forEach { brokenRule ->
                reporter.report(brokenRule.severity, brokenRule.message, declaration)
            }
    }
}