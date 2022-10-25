package co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.namespace

import co.touchlab.skie.plugin.generator.internal.util.irbuilder.UnsupportedDeclarationDescriptorException
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.util.referenceFunction
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.serialization.deserialization.DeserializedPackageFragment

internal class DeserializedPackageNamespace(
    private val existingMember: FunctionDescriptor,
) : BaseDeserializedNamespace<PackageFragmentDescriptor>() {

    override val descriptor: DeserializedPackageFragment = existingMember.containingDeclaration as? DeserializedPackageFragment
        ?: throw IllegalArgumentException("existingMember must be a direct package member of a deserialized package fragment.")

    override val sourceElement: SourceElement = SourceElement { existingMember.findSourceFile() }

    override fun addDescriptor(declarationDescriptor: DeclarationDescriptor) {
        when (declarationDescriptor) {
            is SimpleFunctionDescriptor -> addFunctionDescriptor(declarationDescriptor)
            else -> throw UnsupportedDeclarationDescriptorException(declarationDescriptor)
        }
    }

    private fun addFunctionDescriptor(functionDescriptor: SimpleFunctionDescriptor) {
        descriptor.getMemberScope().addFunctionDescriptorToImpl(functionDescriptor)
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun generateNamespaceIr(generatorContext: GeneratorContext): IrDeclarationContainer =
        generatorContext.symbolTable.referenceFunction(existingMember).owner.parent as IrDeclarationContainer
}
