package co.touchlab.skie.util.swift

import co.touchlab.skie.sir.element.SirElementWithAttributes
import co.touchlab.skie.sir.element.SirElementWithSwiftPoetBuilderModifications
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel
import io.outfoxx.swiftpoet.FunctionSpec

fun <T> T.addFunctionBodyWithErrorTypeHandling(
    swiftModel: KotlinDirectlyCallableMemberSwiftModel,
    realFunctionBuilder: FunctionSpec.Builder.() -> Unit,
) where T : SirElementWithSwiftPoetBuilderModifications<FunctionSpec.Builder>, T : SirElementWithAttributes {
    if (swiftModel.hasValidSignatureInSwift) {
        swiftPoetBuilderModifications.add {
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
        where T : SirElementWithSwiftPoetBuilderModifications<FunctionSpec.Builder>, T : SirElementWithAttributes {
    attributes.add("available(*, unavailable, message: \"$errorMessage\")")

    swiftPoetBuilderModifications.add {
        addStatement("""fatalError("$errorMessage")""")
    }
}

