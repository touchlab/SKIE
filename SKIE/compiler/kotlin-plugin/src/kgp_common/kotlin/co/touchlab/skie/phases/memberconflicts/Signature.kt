package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.KotlinRegularPropertySwiftModel

data class Signature(
    val receiver: String,
    val identifier: String,
    val parameters: List<Parameter>,
    val returnType: ReturnType,
) {

    data class Parameter(val argumentLabel: String, val type: String)

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
    }

    private object SignatureConvertorVisitor : KotlinDirectlyCallableMemberSwiftModelVisitor<Signature> {

        override fun visit(function: KotlinFunctionSwiftModel): Signature =
            Signature(
                receiver = function.receiver.toSwiftPoetTypeName().toString(),
                identifier = function.kotlinSirCallableDeclaration.identifier,
                parameters = function.kotlinSirValueParameters.map { it.toSignatureParameter() },
                returnType = when (function.role) {
                    KotlinFunctionSwiftModel.Role.Constructor -> ReturnType.Specific(function.receiver.toSwiftPoetTypeName().toString())
                    else -> ReturnType.Specific(function.kotlinSirFunction.returnType.toSwiftPoetTypeName().toString())
                },
            )

        private fun SirValueParameter.toSignatureParameter(): Parameter =
            Parameter(
                argumentLabel = this.labelOrName,
                type = this.type.toSwiftPoetTypeName().toString(),
            )

        override fun visit(regularProperty: KotlinRegularPropertySwiftModel): Signature =
            Signature(
                receiver = regularProperty.receiver.toSwiftPoetTypeName().toString(),
                identifier = regularProperty.kotlinSirProperty.identifier,
                parameters = emptyList(),
                returnType = ReturnType.Any,
            )
    }
}

val KotlinDirectlyCallableMemberSwiftModel.signature: Signature
    get() = Signature(this)
