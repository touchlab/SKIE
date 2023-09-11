package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.phases.SkieLinkingPhase
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.kir.allExposedMembers
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.MutableKotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.phases.SkieModule

class RemoveKonanManglingPhase(
    private val skieModule: SkieModule,
    private val descriptorProvider: DescriptorProvider,
) : SkieLinkingPhase {

    override fun execute() {
        skieModule.configure(SkieModule.Ordering.First) {
            descriptorProvider.allExposedMembers
                .map { it.swiftModel }
                .forEach { it.accept(ResetNameVisitor) }
        }
    }

    private object ResetNameVisitor : MutableKotlinCallableMemberSwiftModelVisitor.Unit {

        override fun visit(function: MutableKotlinFunctionSwiftModel) {
            function.identifier = function.identifier.stripMangling(function.descriptor.name.asString())

            function.valueParameters.forEach {
                it.resetName()
            }
        }

        private fun MutableKotlinValueParameterSwiftModel.resetName() {
            this.argumentLabel = this.argumentLabel.stripMangling(this.parameterName)
        }

        override fun visit(regularProperty: MutableKotlinRegularPropertySwiftModel) {
            regularProperty.identifier = regularProperty.identifier.stripMangling(regularProperty.descriptor.name.asString())
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

