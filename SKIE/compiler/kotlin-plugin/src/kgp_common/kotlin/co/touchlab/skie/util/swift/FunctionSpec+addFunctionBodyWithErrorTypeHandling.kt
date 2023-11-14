package co.touchlab.skie.util.swift

import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirElementWithAttributes
import co.touchlab.skie.sir.element.SirElementWithFunctionBodyBuilder
import io.outfoxx.swiftpoet.FunctionSpec

fun <T> T.addFunctionDeclarationBodyWithErrorTypeHandling(
    sirCallableDeclaration: SirCallableDeclaration,
    realFunctionBuilder: FunctionSpec.Builder.() -> Unit,
) where T : SirElementWithFunctionBodyBuilder, T : SirElementWithAttributes {
    if (sirCallableDeclaration.hasValidSignature) {
        bodyBuilder.add {
            realFunctionBuilder()
        }
    } else {
        addSkieLambdaErrorFunctionBody()
    }
}

private const val errorMessage =
    "Due to an Obj-C/Swift interop limitation, SKIE cannot generate Swift types with a lambda type argument. " +
        "Example of such type is: A<() -> Unit> where A<T> is a generic class. " +
        "The original declarations can still be used in the same way as other declarations hidden by SKIE (and with the same limitations as without SKIE)."

private fun <T> T.addSkieLambdaErrorFunctionBody()
    where T : SirElementWithFunctionBodyBuilder, T : SirElementWithAttributes {
    attributes.add("available(*, unavailable, message: \"$errorMessage\")")

    bodyBuilder.add {
        addStatement("""fatalError("$errorMessage")""")
    }
}

