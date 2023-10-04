package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.kir.allExposedMembers
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

object RemoveKonanManglingPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        descriptorProvider.allExposedMembers
            .map { it.swiftModel }
            .forEach { it.accept(ResetNameVisitor) }
    }

    private object ResetNameVisitor : MutableKotlinCallableMemberSwiftModelVisitor.Unit {

        override fun visit(function: MutableKotlinFunctionSwiftModel) {
            when (function.role) {
                KotlinFunctionSwiftModel.Role.Constructor -> {
                    function.kotlinSirConstructor.resetName()
                    function.bridgedSirConstructor?.resetName()
                }
                else -> {
                    function.kotlinSirFunction.resetName(function.descriptor)
                    function.bridgedSirFunction?.resetName(function.descriptor)
                }
            }
        }

        private fun SirFunction.resetName(functionDescriptor: FunctionDescriptor) {
            this.identifier = this.identifier.stripMangling(functionDescriptor.name.asString())

            valueParameters.forEach {
                it.resetName()
            }
        }

        private fun SirConstructor.resetName() {
            valueParameters.forEach {
                it.resetName()
            }
        }

        private fun SirValueParameter.resetName() {
            this.label = this.labelOrName.stripMangling(this.name)
        }

        override fun visit(regularProperty: MutableKotlinRegularPropertySwiftModel) {
            regularProperty.kotlinSirProperty.resetName(regularProperty.descriptor)
            regularProperty.bridgedSirProperty?.resetName(regularProperty.descriptor)
        }

        private fun SirProperty.resetName(propertyDescriptor: PropertyDescriptor) {
            this.identifier = identifier.stripMangling(propertyDescriptor.name.asString())
        }

        private fun String.stripMangling(kotlinName: String): String {
            val thisWithoutAnyUnderscores = this.dropLastWhile { it == '_' }
            if (thisWithoutAnyUnderscores.isBlank()) {
                return "_"
            }

            val kotlinNameUnderscores = kotlinName.takeLastWhile { it == '_' }

            return thisWithoutAnyUnderscores + kotlinNameUnderscores
        }
    }
}

