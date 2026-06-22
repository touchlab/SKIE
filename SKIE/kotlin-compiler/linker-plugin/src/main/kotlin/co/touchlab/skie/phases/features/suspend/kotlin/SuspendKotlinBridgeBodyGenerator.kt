package co.touchlab.skie.phases.features.suspend.kotlin

import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.skieSymbolTable
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody

class SuspendKotlinBridgeBodyGenerator(
    private val suspendHandlerDescriptor: ClassDescriptor,
) {

    private val exceptionFieldGenerator = SuspendKotlinBridgeCheckedExceptionsGenerator()
    private val lambdaGenerator = SuspendKotlinBridgeHandlerLambdaGenerator()

    context(context: KotlinIrPhase.Context, declarationIrBuilder: DeclarationIrBuilder)
    fun createBody(
        bridgingFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrBody =
        declarationIrBuilder.irBlockBody {
            val suspendHandlerParameter = bridgingFunction.valueParameters.last()
            val checkedExceptions = exceptionFieldGenerator.createGetCheckedExceptions(bridgingFunction, originalFunctionDescriptor)
            val originalFunctionCallLambda = lambdaGenerator.createOriginalFunctionCallLambda(
                bridgingFunction = bridgingFunction,
                originalFunctionDescriptor = originalFunctionDescriptor,
                type = suspendHandlerLaunchMethod.valueParameters.last().type,
            )

            +irReturn(
                irCall(suspendHandlerLaunchMethod).apply {
                    dispatchReceiver = irGet(suspendHandlerParameter)

                    putValueArgument(0, checkedExceptions)
                    putValueArgument(1, originalFunctionCallLambda)
                },
            )
        }

    context(context: KotlinIrPhase.Context)
    private val suspendHandlerLaunchMethod: IrSimpleFunction
        get() {
            val suspendHandlerClass = context.skieSymbolTable.descriptorExtension.referenceClass(suspendHandlerDescriptor).owner

            return suspendHandlerClass.declarations
                .filterIsInstance<IrSimpleFunction>()
                .single { it.name.identifier == "launch" }
        }
}
