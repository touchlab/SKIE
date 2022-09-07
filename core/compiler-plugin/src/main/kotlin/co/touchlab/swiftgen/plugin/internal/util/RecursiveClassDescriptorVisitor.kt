package co.touchlab.swiftgen.plugin.internal.util

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor
import org.jetbrains.kotlin.descriptors.impl.DeclarationDescriptorVisitorEmptyBodies
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.DescriptorUtils

// Temporary code - not correct. Based on DeepVisitor from Konan. Remove after transition to Descriptors.
@Deprecated("Descriptors")
internal abstract class RecursiveClassDescriptorVisitor : DeclarationDescriptorVisitorEmptyBodies<Unit, Unit>() {

    override fun visitPackageFragmentDescriptor(descriptor: PackageFragmentDescriptor, data: Unit) {
        visitChildren(DescriptorUtils.getAllDescriptors(descriptor.getMemberScope()), data)
    }

    override fun visitPackageViewDescriptor(descriptor: PackageViewDescriptor, data: Unit) {
        // Workaround because we do not filter non-exported modules yet.
        if (descriptor.fqName != FqName.ROOT && !descriptor.fqName.asString().startsWith("tests")) {
            return
        }

        visitChildren(DescriptorUtils.getAllDescriptors(descriptor.memberScope), data)
    }

    override fun visitClassDescriptor(descriptor: ClassDescriptor, data: Unit) {
        visitClass(descriptor)

        visitChildren(DescriptorUtils.getAllDescriptors(descriptor.defaultType.memberScope), data)
    }

    override fun visitModuleDeclaration(descriptor: ModuleDescriptor, data: Unit) {
        descriptor.getPackage(FqName.ROOT).accept(this, data)
    }

    private fun visitChildren(descriptors: Collection<DeclarationDescriptor>, data: Unit) {
        descriptors.forEach {
            it.accept(this, data)
        }
    }

    protected abstract fun visitClass(descriptor: ClassDescriptor)
}
