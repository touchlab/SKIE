package co.touchlab.swiftgen.plugin.internal.util.ir

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.KotlinType

internal class IrBuilder(
    private val descriptorRegistrar: DescriptorRegistrar,
) {

    val moduleDescriptor: ModuleDescriptor
        get() = descriptorRegistrar.moduleDescriptor

    private val sourceElement = SourceElement { SourceFile { IrGenerator.generatedFileName } }

    fun createFunction(
        name: String,
        init: FunctionBuilder.(SimpleFunctionDescriptor) -> Unit,
    ): SimpleFunctionDescriptor =
        createFunction(Name.identifier(name), init)

    fun createFunction(
        name: Name,
        init: FunctionBuilder.(SimpleFunctionDescriptor) -> Unit,
    ): SimpleFunctionDescriptor = create {
        object : IrTemplate<SimpleFunctionDescriptor, IrSimpleFunction, IrSimpleFunctionSymbol> {

            val builder: FunctionBuilder = FunctionBuilder(moduleDescriptor.builtIns)

            override fun createDescriptor(): SimpleFunctionDescriptor {
                val descriptor = SimpleFunctionDescriptorImpl.create(
                    descriptorRegistrar.syntheticPackageDescriptor,
                    Annotations.EMPTY,
                    name,
                    CallableMemberDescriptor.Kind.SYNTHESIZED,
                    sourceElement,
                )

                this.builder.init(descriptor)
                checkNotNull(builder.body) { "Function must have a body." }

                descriptor.initialize(
                    builder.extensionReceiverParameter,
                    builder.dispatchReceiverParameter,
                    builder.contextReceiverParameters,
                    builder.typeParameters,
                    builder.valueParameters,
                    builder.returnType,
                    builder.modality,
                    builder.visibility,
                )

                return descriptor
            }

            override fun getSymbol(descriptor: SimpleFunctionDescriptor, symbolTable: ReferenceSymbolTable): IrSimpleFunctionSymbol =
                symbolTable.referenceSimpleFunction(descriptor)

            override fun initializeIr(
                declaration: IrSimpleFunction,
                symbolTable: ReferenceSymbolTable,
                declarationIrBuilder: DeclarationIrBuilder,
            ) {
                declaration.body = builder.body!!(symbolTable, declarationIrBuilder, declaration)
            }
        }
    }

    class FunctionBuilder(val builtIns: KotlinBuiltIns) {

        var extensionReceiverParameter: ReceiverParameterDescriptor? = null

        var dispatchReceiverParameter: ReceiverParameterDescriptor? = null

        var contextReceiverParameters: List<ReceiverParameterDescriptor> = emptyList()

        var typeParameters: List<TypeParameterDescriptor> = emptyList()

        var valueParameters: List<ValueParameterDescriptor> = emptyList()

        var returnType: KotlinType = builtIns.unitType

        var modality: Modality = Modality.FINAL

        var visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC

        var body: (context(ReferenceSymbolTable) DeclarationIrBuilder.(IrSimpleFunction) -> IrBody)? = null
    }

    private fun <D : DeclarationDescriptor, I : IrDeclaration, S : IrBindableSymbol<*, I>> create(
        builder: () -> IrTemplate<D, I, S>,
    ): D {
        val irTemplate = builder()

        return descriptorRegistrar.add(irTemplate)
    }
}