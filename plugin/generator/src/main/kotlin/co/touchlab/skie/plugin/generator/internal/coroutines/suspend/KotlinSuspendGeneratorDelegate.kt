package co.touchlab.skie.plugin.generator.internal.coroutines.suspend

import co.touchlab.skie.plugin.api.SkieModule
import co.touchlab.skie.plugin.generator.internal.util.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.createCollisionFreeString
import co.touchlab.skie.plugin.generator.internal.util.ir.SUSPEND_WRAPPER_CHECKED_EXCEPTIONS
import co.touchlab.skie.plugin.generator.internal.util.ir.addChild
import co.touchlab.skie.plugin.generator.internal.util.ir.builder.irFunctionExpression
import co.touchlab.skie.plugin.generator.internal.util.ir.builder.irSimpleFunction
import co.touchlab.skie.plugin.generator.internal.util.ir.copy
import co.touchlab.skie.plugin.generator.internal.util.ir.copyWithoutDefaultValue
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.FunctionBuilder
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.impl.IrFieldImpl
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrFieldSymbolImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.referenceClassifier
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.KotlinType

internal class KotlinSuspendGeneratorDelegate(
    private val module: SkieModule,
    private val declarationBuilder: DeclarationBuilder,
    private val descriptorProvider: DescriptorProvider,
) {

    private val suspendHandlerDescriptor: ClassDescriptor by lazy {
        descriptorProvider.classDescriptors
            .single { it.fqNameSafe == FqName("co.touchlab.skie.runtime.coroutines.Skie_SuspendHandler") }
    }

    fun generateKotlinBridgingFunction(function: FunctionDescriptor): FunctionDescriptor {
        val descriptor = createBridgingFunction(function)

        hideBridgingFunction(descriptor)

        return descriptor
    }

    private fun createBridgingFunction(
        function: FunctionDescriptor,
    ): FunctionDescriptor =
        // TODO Does not work with two function with the same name
        declarationBuilder.createFunction(
            name = function.name,
            namespace = declarationBuilder.getCustomNamespace("Skie_suspend"),
            annotations = Annotations.EMPTY,
        ) {
            valueParameters = function.valueParameters.copyWithoutDefaultValue(descriptor) + createSuspendHandlerValueParameter(function)
            typeParameters = function.typeParameters.copy(descriptor)
            returnType = function.builtIns.unitType
            isSuspend = false
            modality = Modality.FINAL
            visibility = function.visibility
            body = {
                createBody(it, function)
            }
        }

    private fun hideBridgingFunction(descriptor: FunctionDescriptor) {
        module.configure {
            descriptor.isHiddenFromSwift = true
        }
    }

    private fun FunctionBuilder.createSuspendHandlerValueParameter(
        function: FunctionDescriptor,
    ): ValueParameterDescriptorImpl =
        ValueParameterDescriptorImpl(
            containingDeclaration = descriptor,
            original = null,
            index = function.valueParameters.size,
            annotations = Annotations.EMPTY,
            name = function.collisionFreeNameForSuspendHandlerValueParameter,
            outType = suspendHandlerDescriptor.defaultType,
            declaresDefaultValue = false,
            isCrossinline = false,
            isNoinline = false,
            varargElementType = null,
            source = SourceElement.NO_SOURCE,
        )

    private val FunctionDescriptor.collisionFreeNameForSuspendHandlerValueParameter: Name
        get() = createCollisionFreeString("suspendHandler") { this.hasValueParameterNamed(it) }
            .let { Name.identifier(it) }

    private fun FunctionDescriptor.hasValueParameterNamed(name: String): Boolean =
        this.valueParameters.any { name == it.name.toString() }

    context(IrPluginContext)
    private fun DeclarationIrBuilder.createBody(
        bridgeFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrBody =
        irBlockBody {
            val suspendHandlerParameter = bridgeFunction.valueParameters.last()
            val checkedExceptions = createGetCheckedExceptions(bridgeFunction, originalFunctionDescriptor)
            val originalFunctionCallLambda = createOriginalFunctionCallLambda(bridgeFunction, originalFunctionDescriptor)

            +irReturn(
                irCall(suspendHandlerLaunchMethod).apply {
                    dispatchReceiver = irGet(suspendHandlerParameter)

                    putValueArgument(0, checkedExceptions)
                    putValueArgument(1, originalFunctionCallLambda)
                }
            )
        }

    context(IrPluginContext)
    private fun DeclarationIrBuilder.createGetCheckedExceptions(
        bridgeFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrExpression {
        val field = createCheckedExceptionsField(bridgeFunction, originalFunctionDescriptor)

        return irGetField(null, field)
    }

    private fun IrPluginContext.createCheckedExceptionsField(
        bridgeFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrFieldImpl {
        val fieldSymbol = IrFieldSymbolImpl()

        val field = IrFieldImpl(
            startOffset = 0,
            endOffset = 0,
            origin = SUSPEND_WRAPPER_CHECKED_EXCEPTIONS,
            symbol = fieldSymbol,
            name = Name.identifier(bridgeFunction.name.identifier + "_checkedExceptions"),
            type = irBuiltIns.arrayClass.typeWith(irBuiltIns.kClassClass.typeWith(irBuiltIns.throwableType)),
            visibility = DescriptorVisibilities.PRIVATE,
            isFinal = true,
            isExternal = false,
            isStatic = true,
        )

        field.initializer = createCheckExceptionsFieldInitializer(fieldSymbol, originalFunctionDescriptor)

        (bridgeFunction.parent as IrDeclarationContainer).addChild(field)

        return field
    }

    private fun IrPluginContext.createCheckExceptionsFieldInitializer(
        fieldSymbol: IrFieldSymbolImpl,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrExpressionBody =
        DeclarationIrBuilder(this, fieldSymbol, 0, 0).run {
            irExprBody(
                irCall(irBuiltIns.arrayOf).apply {
                    val checkedExceptionClassReferences = createCheckedExceptionClassReferences(originalFunctionDescriptor)

                    val varargElementType = irBuiltIns.kClassClass.typeWith(irBuiltIns.throwableType)
                    val vararg = irVararg(varargElementType, checkedExceptionClassReferences)

                    putTypeArgument(0, varargElementType)
                    putValueArgument(0, vararg)
                }
            )
        }

    context(IrPluginContext)
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun createCheckedExceptionClassReferences(
        originalFunctionDescriptor: FunctionDescriptor,
    ): List<IrClassReference> =
        originalFunctionDescriptor.declaredThrownExceptions
            .map { exceptionType ->
                val exceptionTypeSymbol = symbolTable.referenceClassifier(exceptionType.constructor.declarationDescriptor!!)

                IrClassReferenceImpl(
                    startOffset = 0,
                    endOffset = 0,
                    type = irBuiltIns.kClassClass.typeWith(exceptionTypeSymbol.defaultType),
                    symbol = exceptionTypeSymbol,
                    classType = exceptionTypeSymbol.defaultType
                )
            }

    private val FunctionDescriptor.declaredThrownExceptions: List<KotlinType>
        get() {
            val throwsAnnotation = this.annotations.findAnnotation(FqName("kotlin.Throws")) ?: return emptyList()

            @Suppress("UNCHECKED_CAST")
            val exceptionClasses = throwsAnnotation.argumentValue("exceptionClasses")?.value as List<KClassValue>

            return exceptionClasses.map { it.getArgumentType(this.module) }
        }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private val IrPluginContext.suspendHandlerLaunchMethod: IrSimpleFunction
        get() {
            val suspendHandlerClass = symbolTable.referenceClass(suspendHandlerDescriptor).owner

            return suspendHandlerClass.declarations
                .filterIsInstance<IrSimpleFunction>()
                .single { it.name.identifier == "launch" }
        }

    context(IrPluginContext)
    private fun IrBlockBodyBuilder.createOriginalFunctionCallLambda(
        bridgeFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrFunctionExpression =
        irFunctionExpression(
            type = suspendHandlerLaunchMethod.valueParameters.last().type,
            origin = IrStatementOrigin.LAMBDA,
            function = createOriginalFunctionCallLambdaFunction(bridgeFunction, originalFunctionDescriptor)
        )

    context(IrPluginContext)
    private fun IrBlockBodyBuilder.createOriginalFunctionCallLambdaFunction(
        bridgeFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrSimpleFunction =
        irSimpleFunction(
            name = SpecialNames.ANONYMOUS,
            visibility = DescriptorVisibilities.LOCAL,
            returnType = irBuiltIns.anyType.makeNullable(),
            origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA,
            isSuspend = true,
            body = { createOriginalFunctionCallLambdaFunctionBody(bridgeFunction, originalFunctionDescriptor) }
        )

    context(IrPluginContext)
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun DeclarationIrBuilder.createOriginalFunctionCallLambdaFunctionBody(
        bridgeFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrBlockBody =
        irBlockBody {
            val originalFunctionSymbol = symbolTable.referenceSimpleFunction(originalFunctionDescriptor)
            val passedValueArguments = bridgeFunction.valueParameters.dropLast(1).map { irGet(it) }

            +irReturn(irCall(originalFunctionSymbol).apply {
                passedValueArguments.forEachIndexed { index, argument ->
                    putValueArgument(index, argument)
                }
                bridgeFunction.typeParameters.forEach {
                    putTypeArgument(it.index, it.defaultType)
                }
            })
        }
}
