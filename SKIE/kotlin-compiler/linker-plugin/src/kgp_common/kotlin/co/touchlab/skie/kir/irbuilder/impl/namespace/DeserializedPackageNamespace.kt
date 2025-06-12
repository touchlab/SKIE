@file:OptIn(ObsoleteDescriptorBasedAPI::class, UnsafeDuringIrConstructionAPI::class)

package co.touchlab.skie.kir.irbuilder.impl.namespace

import co.touchlab.skie.kir.irbuilder.UnsupportedDeclarationDescriptorException
import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.skieSymbolTable
import org.jetbrains.kotlin.descriptors.findPackage
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.ir.util.referenceFunction
import org.jetbrains.kotlin.serialization.deserialization.DeserializedPackageFragment

class DeserializedPackageNamespace(
    private val existingMember: FunctionDescriptor,
) : BaseDeserializedNamespace<PackageFragmentDescriptor>() {

    override val descriptor: DeserializedPackageFragment = existingMember.findPackage() as? DeserializedPackageFragment
        ?: throw IllegalArgumentException("existingMember must be a member of a deserialized package fragment.")

    override val sourceElement: SourceElement = SourceElement { existingMember.findSourceFile() }

    override fun addDescriptorIntoDescriptorHierarchy(declarationDescriptor: DeclarationDescriptor) {
        when (declarationDescriptor) {
            is SimpleFunctionDescriptor -> addFunctionDescriptor(declarationDescriptor)
            else -> throw UnsupportedDeclarationDescriptorException(declarationDescriptor)
        }
    }

    private fun addFunctionDescriptor(functionDescriptor: SimpleFunctionDescriptor) {
        descriptor.getMemberScope().addFunctionDescriptorToImpl(functionDescriptor)
    }

    context(KotlinIrPhase.Context)
    override fun generateNamespaceIr(): IrDeclarationContainer =
        skieSymbolTable.kotlinSymbolTable.referenceFunction(existingMember).owner.getPackageFragment()
}
