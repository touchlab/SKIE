package co.touchlab.skie.api.apinotes.fixes.memberconflicts

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.allExposedMembers
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.module.SkieModule

class KonanManglingApiNotesFix(
    private val skieModule: SkieModule,
    private val descriptorProvider: DescriptorProvider,
) {

    fun resetNames() {
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

