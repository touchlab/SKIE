package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConditionalConstraint
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirScope
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.inheritsFrom

data class Signature(
    val receiver: Receiver?,
    val identifierAfterVisibilityChanges: String,
    val valueParameters: List<ValueParameter>,
    val returnType: ReturnType,
    val scope: SirScope,
    val conditionalConstraints: List<ConditionalConstraint>,
) {

    class Receiver(
        private val declaration: SirClass,
    ) {

        val canonicalName: String = declaration.defaultType.evaluate().canonicalName

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

    data class ConditionalConstraint(val typeParameter: String, val bounds: Set<String>) {

        constructor(conditionalConstraint: SirConditionalConstraint) : this(
            typeParameter = conditionalConstraint.typeParameter.name,
            bounds = conditionalConstraint.bounds.map { it.evaluate().canonicalName }.toSet(),
        )
    }

    companion object {

        operator fun invoke(callableDeclaration: SirCallableDeclaration): Signature =
            when (callableDeclaration) {
                is SirSimpleFunction -> Signature(callableDeclaration)
                is SirConstructor -> Signature(callableDeclaration)
                is SirProperty -> Signature(callableDeclaration)
            }

        operator fun invoke(function: SirSimpleFunction): Signature =
            Signature(
                receiver = function.receiver,
                identifierAfterVisibilityChanges = function.identifierAfterVisibilityChanges,
                valueParameters = function.signatureValueParameters,
                returnType = ReturnType.Specific(function.returnType.evaluate().canonicalName),
                scope = function.scope,
                conditionalConstraints = function.conditionalConstraints,
            )

        operator fun invoke(constructor: SirConstructor): Signature =
            Signature(
                receiver = constructor.receiver!!,
                identifierAfterVisibilityChanges = constructor.identifierAfterVisibilityChanges,
                valueParameters = constructor.signatureValueParameters,
                returnType = ReturnType.Specific(constructor.receiver!!.canonicalName),
                scope = constructor.scope,
                conditionalConstraints = constructor.conditionalConstraints,
            )

        operator fun invoke(property: SirProperty): Signature =
            Signature(
                receiver = property.receiver,
                identifierAfterVisibilityChanges = property.identifierAfterVisibilityChanges,
                valueParameters = emptyList(),
                returnType = ReturnType.Any,
                scope = property.scope,
                conditionalConstraints = property.conditionalConstraints,
            )

        private val SirFunction.signatureValueParameters: List<ValueParameter>
            get() = valueParameters.map { it.toSignatureParameter() }

        private fun SirValueParameter.toSignatureParameter(): ValueParameter =
            ValueParameter(
                argumentLabel = this.labelOrName,
                type = this.type.evaluate().canonicalName,
            )

        private val SirCallableDeclaration.receiver: Receiver?
            get() = when (val parent = parent) {
                is SirClass -> Receiver(parent)
                is SirExtension -> Receiver(parent.classDeclaration)
                else -> null
            }

        private val SirCallableDeclaration.conditionalConstraints: List<ConditionalConstraint>
            get() = when (val parent = parent) {
                is SirExtension -> parent.conditionalConstraints.map { ConditionalConstraint(it) }
                else -> emptyList()
            }
    }
}

val SirCallableDeclaration.signature: Signature
    get() = Signature(this)
