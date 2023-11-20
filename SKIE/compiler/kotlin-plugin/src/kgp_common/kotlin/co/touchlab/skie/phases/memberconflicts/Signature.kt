package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConditionalConstraint
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirEnumCase
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

sealed class Signature {

    abstract val receiver: Receiver
    abstract val identifier: String
    abstract val valueParameters: List<ValueParameter>
    abstract val returnType: ReturnType
    abstract val scope: Scope

    sealed class Function : Signature()

    class SimpleFunction(
        override val receiver: Receiver,
        override val identifier: String,
        override val valueParameters: List<ValueParameter>,
        override val returnType: ReturnType,
        override val scope: Scope,
    ) : Function() {

        override fun toString(): String =
            ("static ".takeIf { scope == Scope.Static } ?: "") +
                "func " +
                ("$receiver.".takeIf { receiver !is Receiver.None } ?: "") +
                identifier +
                "(${valueParameters.joinToString()})" +
                " -> $returnType"
    }

    class Constructor(
        override val receiver: Receiver.Constructor,
        override val valueParameters: List<ValueParameter>,
    ) : Function() {

        override val identifier: String = "init"

        override val returnType: ReturnType = ReturnType.Specific(receiver.sirClass.defaultType.signatureType)

        override val scope: Scope = Scope.Static

        override fun toString(): String =
            "func " +
                "$receiver." +
                identifier +
                "(${valueParameters.joinToString()})"
    }

    class Property(
        override val receiver: Receiver,
        override val identifier: String,
        override val scope: Scope,
    ) : Signature() {

        override val valueParameters: List<ValueParameter> = emptyList()

        override val returnType: ReturnType = ReturnType.Any

        override fun toString(): String =
            ("static ".takeIf { scope == Scope.Static } ?: "") +
                "var " +
                ("$receiver.".takeIf { receiver !is Receiver.None } ?: "") +
                identifier
    }

    class EnumCase(
        override val receiver: Receiver.Simple,
        override val identifier: String,
    ) : Signature() {

        override val valueParameters: List<ValueParameter> = emptyList()

        override val returnType: ReturnType = ReturnType.Any

        override val scope: Scope = Scope.Static

        override fun toString(): String =
            "case $receiver.$identifier"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Signature) return false

        if (receiver != other.receiver) return false
        if (identifier != other.identifier) return false
        if (valueParameters != other.valueParameters) return false
        if (returnType != other.returnType) return false
        if (scope != other.scope) return false

        if (this is EnumCase && other is Function || this is Function && other is EnumCase) return false

        return true
    }

    override fun hashCode(): Int {
        var result = receiver.hashCode()
        result = 31 * result + identifier.hashCode()
        result = 31 * result + valueParameters.hashCode()
        result = 31 * result + returnType.hashCode()
        result = 31 * result + scope.hashCode()
        return result
    }

    sealed interface Receiver {

        data class Simple(val type: Type.Class, val constraints: Set<Constraint>) : Receiver {

            override fun toString(): String =
                if (constraints.isEmpty()) {
                    type.toString()
                } else {
                    "($type" + "where ${constraints.joinToString(", ")})"
                }
        }

        data class Constructor(val sirClass: SirClass, val constraints: Set<Constraint>) : Receiver {

            override fun toString(): String =
                if (constraints.isEmpty()) {
                    sirClass.fqName.toString()
                } else {
                    "(${sirClass.fqName}" + "where ${constraints.joinToString(", ")})"
                }
        }

        object None : Receiver {

            override fun toString(): String = "None"
        }

        data class Constraint(val typeParameterName: String, val bounds: Set<Type>) {

            constructor(conditionalConstraint: SirConditionalConstraint) : this(
                typeParameterName = conditionalConstraint.typeParameter.name,
                bounds = conditionalConstraint.bounds.map { it.signatureType }.toSet(),
            )

            override fun toString(): String =
                "$typeParameterName: ${bounds.joinToString(" & ")}"
        }
    }

    data class ValueParameter(val argumentLabel: String, val type: String) {

        override fun toString(): String = "$argumentLabel: $type"
    }

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

            override fun toString(): String = type.toString()
        }

        object Any : ReturnType {

            override fun equals(other: kotlin.Any?): Boolean {
                if (other is ReturnType) return true

                return super.equals(other)
            }

            override fun hashCode(): Int = 0

            override fun toString(): String = "AnyType"
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

            override fun toString(): String = sirClass.fqName.toString() + ("<${typeArguments.joinToString()}>".takeIf { typeArguments.isNotEmpty() } ?: "")
        }

        data class Optional(val nested: Type) : Type {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Optional

                return nested == other.nested
            }

            override fun hashCode(): Int = 1

            override fun toString(): String = "$nested?"
        }

        data class Special(val name: String) : Type {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Special

                return name == other.name
            }

            override fun hashCode(): Int = name.hashCode()

            override fun toString(): String = name
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

            override fun toString(): String = canonicalName
        }
    }

    enum class Scope {

        Member,
        Static,
    }

    companion object {

        operator fun invoke(enumCase: SirEnumCase): Signature =
            EnumCase(
                receiver = Receiver.Simple(enumCase.parent.asReceiverType, emptySet()),
                identifier = enumCase.simpleName,
            )

        operator fun invoke(callableDeclaration: SirCallableDeclaration): Signature =
            when (callableDeclaration) {
                is SirSimpleFunction -> Signature(callableDeclaration)
                is SirConstructor -> Signature(callableDeclaration)
                is SirProperty -> Signature(callableDeclaration)
            }

        operator fun invoke(function: SirSimpleFunction): Signature =
            SimpleFunction(
                receiver = function.receiver,
                identifier = function.identifierAfterVisibilityChanges,
                valueParameters = function.signatureValueParameters,
                returnType = ReturnType.Specific(function.returnType.signatureType),
                scope = function.signatureScope,
            )

        operator fun invoke(constructor: SirConstructor): Signature {
            val receiver = constructor.receiver

            check(receiver is Receiver.Constructor) {
                "Constructors should always have a constructor receiver. Was: $receiver"
            }

            return Constructor(
                receiver = receiver,
                valueParameters = constructor.signatureValueParameters,
            )
        }

        operator fun invoke(property: SirProperty): Signature =
            Property(
                receiver = property.receiver,
                identifier = property.identifierAfterVisibilityChanges,
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
                    is SirSimpleFunction, is SirProperty -> Receiver.Simple(sirClass.asReceiverType, constraints)
                }
            }

        private val SirClass.asReceiverType: Type.Class
            get() = Type.Class(this, emptyList())

        private val SirCallableDeclaration.signatureScope: Scope
            get() = when (this.scope) {
                SirScope.Member -> Scope.Member
                SirScope.Global, SirScope.Class, SirScope.Static -> Scope.Static
            }
    }
}

val SirCallableDeclaration.signature: Signature
    get() = Signature(this)

val SirEnumCase.signature: Signature
    get() = Signature(this)
