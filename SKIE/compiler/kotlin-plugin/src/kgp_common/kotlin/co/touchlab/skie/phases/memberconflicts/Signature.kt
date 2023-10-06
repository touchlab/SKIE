package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.SirValueParameterParent
import co.touchlab.skie.sir.element.identifierAfterVisibilityChanges
import co.touchlab.skie.sir.element.inheritsFrom
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.KotlinRegularPropertySwiftModel

data class Signature(
    val receiver: Receiver?,
    val identifierAfterVisibilityChanges: String,
    val valueParameters: List<ValueParameter>,
    val returnType: ReturnType,
) {

    // WIP 2 Needs to correctly account for type parameters
    class Receiver(
        private val declaration: SirClass,
    ) {

        val canonicalName: String = declaration.defaultType.canonicalName

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Receiver

            if (declaration == other.declaration) return true

            if (declaration.inheritsFrom(other.declaration)) return true
            if (other.declaration.inheritsFrom(declaration)) return true

            return false
        }

        override fun hashCode(): Int = 0
    }

    data class ValueParameter(val argumentLabel: String, val type: String)

    sealed interface ReturnType {

        class Specific(val name: String) : ReturnType {

            override fun equals(other: kotlin.Any?): Boolean {
                if (this === other) return true
                if (other is Any) return true
                if (javaClass != other?.javaClass) return false

                other as Specific

                return name == other.name
            }

            override fun hashCode(): Int = 0
        }

        object Any : ReturnType {

            override fun equals(other: kotlin.Any?): Boolean {
                if (other is ReturnType) return true

                return super.equals(other)
            }

            override fun hashCode(): Int = 0
        }
    }

    companion object {

        operator fun invoke(directlyCallableMember: KotlinDirectlyCallableMemberSwiftModel): Signature =
            directlyCallableMember.accept(SignatureConvertorVisitor)

        operator fun invoke(callableDeclaration: SirCallableDeclaration): Signature =
            when (callableDeclaration) {
                is SirFunction -> Signature(callableDeclaration)
                is SirConstructor -> Signature(callableDeclaration)
                is SirProperty -> Signature(callableDeclaration)
            }

        operator fun invoke(function: SirFunction): Signature =
            Signature(
                receiver = function.receiver,
                identifierAfterVisibilityChanges = function.identifierAfterVisibilityChanges,
                valueParameters = function.signatureValueParameters,
                returnType = ReturnType.Specific(function.returnType.canonicalName),
            )

        operator fun invoke(constructor: SirConstructor): Signature =
            Signature(
                receiver = constructor.receiver!!,
                identifierAfterVisibilityChanges = constructor.identifierAfterVisibilityChanges,
                valueParameters = constructor.signatureValueParameters,
                returnType = ReturnType.Specific(constructor.receiver!!.canonicalName),
            )

        operator fun invoke(property: SirProperty): Signature =
            Signature(
                receiver = property.receiver,
                identifierAfterVisibilityChanges = property.identifierAfterVisibilityChanges,
                valueParameters = emptyList(),
                returnType = ReturnType.Any,
            )

        private val SirValueParameterParent.signatureValueParameters: List<ValueParameter>
            get() = valueParameters.map { it.toSignatureParameter() }

        private fun SirValueParameter.toSignatureParameter(): ValueParameter =
            ValueParameter(
                argumentLabel = this.labelOrName,
                // WIP 2 canonicalName does not addresses type parameters
                type = this.type.canonicalName,
            )

        private val SirCallableDeclaration.receiver: Receiver?
            get() = when (val parent = parent) {
                is SirClass -> Receiver(parent)
                is SirExtension -> if (parent.conditionalConstraints.isEmpty()) {
                    Receiver(parent.classDeclaration)
                } else {
                    // WIP 2 Needs to correctly account for type parameters
                    Receiver(parent.classDeclaration)
                }
                else -> null
            }
    }

    private object SignatureConvertorVisitor : KotlinDirectlyCallableMemberSwiftModelVisitor<Signature> {

        override fun visit(function: KotlinFunctionSwiftModel): Signature =
            invoke(function.kotlinSirCallableDeclaration)

        override fun visit(regularProperty: KotlinRegularPropertySwiftModel): Signature =
            invoke(regularProperty.kotlinSirCallableDeclaration)
    }
}

val KotlinDirectlyCallableMemberSwiftModel.signature: Signature
    get() = Signature(this)

val SirCallableDeclaration.signature: Signature
    get() = Signature(this)
