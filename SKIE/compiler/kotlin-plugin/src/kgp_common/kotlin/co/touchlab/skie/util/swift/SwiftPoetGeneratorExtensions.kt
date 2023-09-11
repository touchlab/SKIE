package co.touchlab.skie.util.swift

import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.FunctionSpec

fun FunctionSpec.Builder.addFunctionBodyWithErrorTypeHandling(
    swiftModel: KotlinDirectlyCallableMemberSwiftModel,
    realFunctionBuilder: FunctionSpec.Builder.() -> Unit,
): FunctionSpec.Builder =
    this.apply {
        if (swiftModel.hasValidSignatureInSwift) {
            realFunctionBuilder()
        } else {
            addSkieLambdaErrorFunctionBody()
        }
    }

private const val errorMessage =
    "Due to an Obj-C/Swift interop limitation, SKIE cannot generate Swift types with a lambda type argument. " +
            "Example of such type is: A<() -> Unit> where A<T> is a generic class. " +
            "The original declarations can still be used in the same way as other declarations hidden by SKIE (and with the same limitations as without SKIE)."

private fun FunctionSpec.Builder.addSkieLambdaErrorFunctionBody() {
    addAttribute(AttributeSpec.available("*," to """unavailable, message: "$errorMessage""""))
    addStatement("""fatalError("$errorMessage")""")
}

