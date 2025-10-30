package co.touchlab.skie.phases.features.suspend.kotlin

import co.touchlab.skie.kir.irbuilder.util.irFunctionExpression
import co.touchlab.skie.kir.irbuilder.util.irSimpleFunction
import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.irBuiltIns
import co.touchlab.skie.phases.skieSymbolTable
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.name.SpecialNames

class SuspendKotlinBridgeHandlerLambdaGenerator {

    context(KotlinIrPhase.Context, IrBlockBodyBuilder)
    fun createOriginalFunctionCallLambda(
        bridgingFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
        type: IrType,
    ): IrFunctionExpression =
        irFunctionExpression(
            type = type,
            origin = IrStatementOrigin.LAMBDA,
            function = createOriginalFunctionCallLambdaFunction(bridgingFunction, originalFunctionDescriptor),
        )

    context(KotlinIrPhase.Context, IrBlockBodyBuilder)
    private fun createOriginalFunctionCallLambdaFunction(
        bridgingFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrSimpleFunction =
        irSimpleFunction(
            name = SpecialNames.ANONYMOUS,
            visibility = DescriptorVisibilities.LOCAL,
            returnType = irBuiltIns.anyType.makeNullable(),
            origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA,
            isSuspend = true,
            body = { createOriginalFunctionCallLambdaFunctionBody(bridgingFunction, originalFunctionDescriptor) },
        )

    context(KotlinIrPhase.Context, DeclarationIrBuilder)
    private fun createOriginalFunctionCallLambdaFunctionBody(
        bridgingFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrBlockBody =
        irBlockBody {
            val originalFunctionSymbol = skieSymbolTable.descriptorExtension.referenceSimpleFunction(originalFunctionDescriptor)

            +irReturn(
                irCall(originalFunctionSymbol).apply {
                    setDispatchReceiverForDelegatingCall(bridgingFunction, originalFunctionDescriptor)
                    setExtensionReceiverForDelegatingCall(bridgingFunction, originalFunctionDescriptor)
                    setValueArgumentsForDelegatingCall(bridgingFunction, originalFunctionDescriptor)
                    setTypeArgumentsForDelegatingCall(bridgingFunction)
                },
            )
        }

    context(DeclarationIrBuilder)
    private fun IrCall.setDispatchReceiverForDelegatingCall(
        bridgingFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ) {
        if (originalFunctionDescriptor.dispatchReceiverParameter != null) {
            val dispatchReceiverParameter = bridgingFunction.valueParameters.first()

            dispatchReceiver = irGet(dispatchReceiverParameter)
        }
    }

    context(DeclarationIrBuilder)
    private fun IrCall.setExtensionReceiverForDelegatingCall(
        bridgingFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ) {
        if (originalFunctionDescriptor.extensionReceiverParameter != null) {
            val parameterIndex = if (originalFunctionDescriptor.dispatchReceiverParameter != null) 1 else 0

            val extensionReceiverParameter = bridgingFunction.valueParameters[parameterIndex]

            extensionReceiver = irGet(extensionReceiverParameter)
        }
    }

    context(DeclarationIrBuilder)
    private fun IrCall.setValueArgumentsForDelegatingCall(
        bridgingFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ) {
        val valueParameters = bridgingFunction.filterRealValueParameters(originalFunctionDescriptor)
        val valueArguments = valueParameters.map { irGet(it) }

        valueArguments.forEachIndexed { index, argument ->
            putValueArgument(index, argument)
        }
    }

    private fun IrSimpleFunction.filterRealValueParameters(originalFunctionDescriptor: FunctionDescriptor): List<IrValueParameter> {
        var result = this.valueParameters

        if (originalFunctionDescriptor.dispatchReceiverParameter != null) {
            result = result.drop(1)
        }
        if (originalFunctionDescriptor.extensionReceiverParameter != null) {
            result = result.drop(1)
        }

        result = result.dropLast(1)

        return result
    }

    private fun IrCall.setTypeArgumentsForDelegatingCall(bridgeFunction: IrSimpleFunction) {
        bridgeFunction.typeParameters.take(this.symbol.owner.typeParameters.size).forEach {
            putTypeArgument(it.index, it.defaultType)
        }
    }
}
