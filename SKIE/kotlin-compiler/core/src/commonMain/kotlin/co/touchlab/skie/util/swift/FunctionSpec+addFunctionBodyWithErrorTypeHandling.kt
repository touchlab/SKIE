package co.touchlab.skie.util.swift

import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirElementWithAttributes
import co.touchlab.skie.sir.element.SirElementWithFunctionBodyBuilder
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.type.ExistentialSirType
import co.touchlab.skie.sir.type.LambdaSirType
import co.touchlab.skie.sir.type.NullableSirType
import co.touchlab.skie.sir.type.OirDeclaredSirType
import co.touchlab.skie.sir.type.OpaqueSirType
import co.touchlab.skie.sir.type.SirDeclaredSirType
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.SkieErrorSirType
import co.touchlab.skie.sir.type.SpecialSirType
import co.touchlab.skie.sir.type.TupleSirType
import co.touchlab.skie.sir.type.TypeParameterUsageSirType
import io.outfoxx.swiftpoet.FunctionSpec

fun <T> T.addFunctionDeclarationBodyWithErrorTypeHandling(
    sirCallableDeclaration: SirCallableDeclaration,
    realFunctionBuilder: FunctionSpec.Builder.() -> Unit,
) where T : SirElementWithFunctionBodyBuilder, T : SirElementWithAttributes {
    val errorType = sirCallableDeclaration.findFirstSkieErrorType()

    if (errorType == null) {
        bodyBuilder.add {
            realFunctionBuilder()
        }
    } else {
        addSkieErrorTypeFunctionBody(errorType)
    }
}

fun SirCallableDeclaration.findFirstSkieErrorType(): SkieErrorSirType? =
    when (this) {
        is SirConstructor -> findFirstSkieErrorType()
        is SirSimpleFunction -> findFirstSkieErrorType()
        is SirProperty -> findFirstSkieErrorType()
    }

private fun SirConstructor.findFirstSkieErrorType(): SkieErrorSirType? =
    valueParameters.firstNotNullOfOrNull { it.type.normalize().findFirstSkieErrorType() }

private fun SirSimpleFunction.findFirstSkieErrorType(): SkieErrorSirType? =
    returnType.normalize().findFirstSkieErrorType() ?: valueParameters.firstNotNullOfOrNull { it.type.normalize().findFirstSkieErrorType() }

private fun SirProperty.findFirstSkieErrorType(): SkieErrorSirType? =
    type.normalize().findFirstSkieErrorType()

private fun SirType.findFirstSkieErrorType(): SkieErrorSirType? =
    when (this) {
        is SirDeclaredSirType -> typeArguments.firstNotNullOfOrNull { it.findFirstSkieErrorType() }
        is LambdaSirType -> returnType.findFirstSkieErrorType() ?: valueParameterTypes.firstNotNullOfOrNull { it.findFirstSkieErrorType() }
        is NullableSirType -> type.findFirstSkieErrorType()
        is OpaqueSirType -> type.findFirstSkieErrorType()
        is ExistentialSirType -> type.findFirstSkieErrorType()
        is TupleSirType -> elements.firstNotNullOfOrNull { it.type.findFirstSkieErrorType() }
        is TypeParameterUsageSirType -> null
        is SpecialSirType -> null
        is OirDeclaredSirType -> error("Evaluated types cannot contain OirDeclaredSirType. Was: $this")
        is SkieErrorSirType -> this
    }

private fun <T> T.addSkieErrorTypeFunctionBody(errorType: SkieErrorSirType)
    where T : SirElementWithFunctionBodyBuilder, T : SirElementWithAttributes {
    attributes.add("available(*, unavailable, message: \"${errorType.errorMessage}\")")

    bodyBuilder.add {
        addStatement("""fatalError("${errorType.errorMessage}")""")
    }
}
