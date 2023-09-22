package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.KotlinValueParameterSwiftModel
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
                receiver = function.receiver.toSwiftPoetUsage().toString(), //.stableFqName,
                identifier = function.identifier,
                parameters = function.valueParameters.map { it.toSignatureParameter() },
                returnType = ReturnType.Specific(function.returnType.toSwiftPoetUsage().toString() /*.stableFqName*/),
            )

        private fun KotlinValueParameterSwiftModel.toSignatureParameter(): Parameter =
            Parameter(
                argumentLabel = this.argumentLabel,
                type = this.type.toSwiftPoetUsage().toString(), //stableFqName,
            )

        override fun visit(regularProperty: KotlinRegularPropertySwiftModel): Signature =
            Signature(
                receiver = regularProperty.receiver.toSwiftPoetUsage().toString(), //.stableFqName,
                identifier = regularProperty.identifier,
                parameters = emptyList(),
                returnType = ReturnType.Any,
            )
    }
}

val KotlinDirectlyCallableMemberSwiftModel.signature: Signature
    get() = Signature(this)
