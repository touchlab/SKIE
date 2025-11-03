package co.touchlab.skie.phases.features.suspend.kotlin

import co.touchlab.skie.kir.irbuilder.util.addChild
import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.irBuiltIns
import co.touchlab.skie.phases.irFactory
import co.touchlab.skie.phases.pluginContext
import co.touchlab.skie.phases.skieSymbolTable
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.konan.KonanFqNames
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.descriptors.findClassifierAcrossModuleDependencies
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOriginImpl
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrFieldSymbolImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.referenceClassifier
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.constants.KClassValue.Value
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.KotlinType

class SuspendKotlinBridgeCheckedExceptionsGenerator {

    context(KotlinIrPhase.Context, DeclarationIrBuilder)
    fun createGetCheckedExceptions(
        bridgingFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrExpression {
        val field = createCheckedExceptionsField(bridgingFunction, originalFunctionDescriptor)

        return irGetField(null, field)
    }

    context(KotlinIrPhase.Context)
    private fun createCheckedExceptionsField(
        bridgingFunction: IrSimpleFunction,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrField {
        val fieldSymbol = IrFieldSymbolImpl()

        val field = irFactory.createField(
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

        bridgingFunction.parentPackageFragment.addChild(field)

        return field
    }

    @Suppress("RecursivePropertyAccessor")
    private val IrDeclaration.parentPackageFragment: IrPackageFragment
        get() = parent as? IrPackageFragment ?: (parent as IrDeclaration).parentPackageFragment

    private val IrSimpleFunction.nameForCheckedExceptionsField: String
        get() = this.name.identifier + "__checkedExceptions"

    context(KotlinIrPhase.Context)
    private fun createCheckExceptionsFieldInitializer(
        fieldSymbol: IrFieldSymbolImpl,
        originalFunctionDescriptor: FunctionDescriptor,
    ): IrExpressionBody =
        DeclarationIrBuilder(pluginContext.generatorContext, fieldSymbol, 0, 0).run {
            irExprBody(
                irCall(irBuiltIns.arrayOf).apply {
                    val checkedExceptionClassReferences = createCheckedExceptionClassReferences(originalFunctionDescriptor)

                    val varargElementType = irBuiltIns.kClassClass.typeWith(irBuiltIns.throwableType)
                    val vararg = irVararg(varargElementType, checkedExceptionClassReferences)

                    putTypeArgument(0, varargElementType)
                    putValueArgument(0, vararg)
                },
            )
        }

    context(KotlinIrPhase.Context)
    private fun createCheckedExceptionClassReferences(
        originalFunctionDescriptor: FunctionDescriptor,
    ): List<IrClassReference> =
        originalFunctionDescriptor.declaredThrownExceptions
            .map { exceptionTypeSymbol ->
                IrClassReferenceImpl(
                    startOffset = 0,
                    endOffset = 0,
                    type = irBuiltIns.kClassClass.typeWith(exceptionTypeSymbol.defaultType),
                    symbol = exceptionTypeSymbol,
                    classType = exceptionTypeSymbol.defaultType,
                )
            }

    context(KotlinIrPhase.Context)
    private val FunctionDescriptor.declaredThrownExceptions: List<IrClassifierSymbol>
        get() {
            val throwsAnnotation = this.annotations.findAnnotation(KonanFqNames.throws) ?: return emptyList()

            @Suppress("UNCHECKED_CAST")
            val exceptionClasses = throwsAnnotation.argumentValue("exceptionClasses")?.value as List<KClassValue>

            return exceptionClasses.map { it.getClassifierSymbol(this.module) }
        }

    context(KotlinIrPhase.Context)
    private fun KClassValue.getClassifierSymbol(module: ModuleDescriptor): IrClassifierSymbol =
        when (val value = this.value) {
            is Value.LocalClass -> value.type.toIrSymbol()
            is Value.NormalClass -> module.findClassifierAcrossModuleDependencies(value.classId)!!.defaultType.toIrSymbol()
        }

    context(KotlinIrPhase.Context)
    private fun KotlinType.toIrSymbol(): IrClassifierSymbol =
        when (val classifier = this.constructor.declarationDescriptor) {
            null -> error("No declaration descriptor for type $this.")
            is TypeAliasDescriptor -> classifier.expandedType.toIrSymbol()
            else -> skieSymbolTable.kotlinSymbolTable.referenceClassifier(classifier)
        }

    companion object {

        private val SUSPEND_WRAPPER_CHECKED_EXCEPTIONS: IrDeclarationOriginImpl =
            IrDeclarationOriginImpl("SUSPEND_WRAPPER_CHECKED_EXCEPTIONS", true)
    }
}
