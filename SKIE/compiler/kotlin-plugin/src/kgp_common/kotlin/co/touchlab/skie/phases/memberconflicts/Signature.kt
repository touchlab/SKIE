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
import co.touchlab.skie.sir.element.sharesDirectInheritanceHierarchy
import co.touchlab.skie.sir.type.NullableSirType
import co.touchlab.skie.sir.type.OirDeclaredSirType
import co.touchlab.skie.sir.type.SirDeclaredSirType
import co.touchlab.skie.sir.type.SirType

data class Signature(
    val receiver: Receiver,
    val identifierAfterVisibilityChanges: String,
    val valueParameters: List<ValueParameter>,
    val returnType: ReturnType,
    val scope: Scope,
) {

    sealed interface Receiver {

        data class Simple(val type: Type, val constraints: Set<Constraint>) : Receiver

        data class Constructor(val sirClass: SirClass, val constraints: Set<Constraint>) : Receiver

        object None : Receiver

        data class Constraint(val typeParameterName: String, val bounds: Set<Type>) {

            constructor(conditionalConstraint: SirConditionalConstraint) : this(
                typeParameterName = conditionalConstraint.typeParameter.name,
                bounds = conditionalConstraint.bounds.map { it.signatureType }.toSet(),
            )
        }
    }

    data class ValueParameter(val argumentLabel: String, val type: String)

    sealed interface ReturnType {

        class Specific(val type: Type) : ReturnType {

            override fun equals(other: kotlin.Any?): Boolean {
                if (this === other) return true
                if (other is Any) return true
                if (javaClass != other?.javaClass) return false

                other as Specific

                return type == other.type
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

    sealed interface Type {

        class Class(val sirClass: SirClass, val typeArguments: List<TypeArgument>) : Type {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Class

                if (typeArguments != other.typeArguments) return false

                return sirClass.sharesDirectInheritanceHierarchy(other.sirClass)
            }

            override fun hashCode(): Int = 0
        }

        class Optional(val nested: Type) : Type {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Optional

                return nested == other.nested
            }

            override fun hashCode(): Int = 1
        }

        class Special(val name: String) : Type {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Special

                return name == other.name
            }

            override fun hashCode(): Int = name.hashCode()
        }

        class TypeArgument(val type: SirType) {

            private val canonicalName: String = type.evaluate().canonicalName

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as TypeArgument

                return canonicalName == other.canonicalName
            }

            override fun hashCode(): Int = canonicalName.hashCode()
        }
    }

    enum class Scope {

        Member,
        Static,
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
                returnType = ReturnType.Specific(function.returnType.signatureType),
                scope = function.signatureScope,
            )

        operator fun invoke(constructor: SirConstructor): Signature {
            val receiver = constructor.receiver

            check(receiver is Receiver.Constructor) {
                "Constructors should always have a constructor receiver. Was: $receiver"
            }

            return Signature(
                receiver = receiver,
                identifierAfterVisibilityChanges = constructor.identifierAfterVisibilityChanges,
                valueParameters = constructor.signatureValueParameters,
                returnType = ReturnType.Specific(receiver.sirClass.defaultType.signatureType),
                scope = constructor.signatureScope,
            )
        }

        operator fun invoke(property: SirProperty): Signature =
            Signature(
                receiver = property.receiver,
                identifierAfterVisibilityChanges = property.identifierAfterVisibilityChanges,
                valueParameters = emptyList(),
                returnType = ReturnType.Any,
                scope = property.signatureScope,
            )

        private val SirFunction.signatureValueParameters: List<ValueParameter>
            get() = valueParameters.map { it.toSignatureParameter() }

        private val SirType.signatureType: Type
            get() {
                val evaluatedType = this.evaluate()

                return when (val typeWithoutTypeAliases = evaluatedType.type.inlineTypeAliases()) {
                    is SirDeclaredSirType -> {
                        check(typeWithoutTypeAliases.declaration is SirClass) {
                            "TypeAliases should have been inlined in the above step. Was: $this"
                        }

                        Type.Class(
                            sirClass = typeWithoutTypeAliases.declaration,
                            typeArguments = typeWithoutTypeAliases.typeArguments.map { Type.TypeArgument(it) },
                        )
                    }
                    is NullableSirType -> Type.Optional(typeWithoutTypeAliases.type.signatureType)
                    is OirDeclaredSirType -> error("Oir types should have been converted to Sir types in the above step. Was: $this")
                    else -> Type.Special(evaluatedType.canonicalName)
                }
            }

        private fun SirValueParameter.toSignatureParameter(): ValueParameter =
            ValueParameter(
                argumentLabel = this.labelOrName,
                type = this.type.evaluate().canonicalName,
            )

        private val SirCallableDeclaration.receiver: Receiver
            get() {
                val (sirClass, constraints) = when (val parent = parent) {
                    is SirClass -> parent.classDeclaration to emptySet()
                    is SirExtension -> parent.classDeclaration to parent.conditionalConstraints.map { Receiver.Constraint(it) }.toSet()
                    else -> return Receiver.None
                }

                return when (this) {
                    is SirConstructor -> Receiver.Constructor(sirClass, constraints)
                    is SirSimpleFunction, is SirProperty -> Receiver.Simple(sirClass.defaultType.signatureType, constraints)
                }
            }

        private val SirCallableDeclaration.signatureScope: Scope
            get() = when (this.scope) {
                SirScope.Member -> Scope.Member
                SirScope.Global, SirScope.Class, SirScope.Static -> Scope.Static
            }
    }
}

val SirCallableDeclaration.signature: Signature
    get() = Signature(this)
