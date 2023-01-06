package co.touchlab.skie.plugin.generator.internal.coroutines.suspend.kotlin

import co.touchlab.skie.plugin.generator.internal.util.ir.SUSPEND_WRAPPER_CHECKED_EXCEPTIONS
import co.touchlab.skie.plugin.generator.internal.util.ir.addChild
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.jvm.serialization.JvmIrMangler.fqnString
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.impl.IrFieldImpl
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrFieldSymbolImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.referenceClassifier
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.KotlinType

internal class SuspendKotlinBridgeCheckedExceptionsGenerator {

    context(IrPluginContext, DeclarationIrBuilder)
    fun createGetCheckedExceptions(
        bridgingFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrExpression {
        val field = createCheckedExceptionsField(bridgingFunction, originalFunctionDescriptor)

        return irGetField(null, field)
    }

    private fun IrPluginContext.createCheckedExceptionsField(
        bridgingFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrFieldImpl {
        val fieldSymbol = IrFieldSymbolImpl()

        val field = IrFieldImpl(
            startOffset = 0,
            endOffset = 0,
            origin = SUSPEND_WRAPPER_CHECKED_EXCEPTIONS,
            symbol = fieldSymbol,
            name = Name.identifier(bridgingFunction.nameForCheckedExceptionsField),
            type = irBuiltIns.arrayClass.typeWith(irBuiltIns.kClassClass.typeWith(irBuiltIns.throwableType)),
            visibility = DescriptorVisibilities.PRIVATE,
            isFinal = true,
            isExternal = false,
            isStatic = true,
        )

        field.initializer = createCheckExceptionsFieldInitializer(fieldSymbol, originalFunctionDescriptor)

        bridgingFunction.file.addChild(field)

        return field
    }

    private val IrSimpleFunction.nameForCheckedExceptionsField: String
        get() = this.fqnString(false) + "_checkedExceptions"

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
}
