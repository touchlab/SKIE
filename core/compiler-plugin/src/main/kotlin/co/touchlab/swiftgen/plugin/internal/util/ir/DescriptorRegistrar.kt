package co.touchlab.swiftgen.plugin.internal.util.ir

import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectedBy
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.CompositePackageFragmentProviderReflector
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.ModuleDescriptorImplReflector
import org.jetbrains.kotlin.backend.common.SimpleMemberScope
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentProviderImpl
import org.jetbrains.kotlin.descriptors.impl.MutablePackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.impl.PackageFragmentDescriptorImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.scopes.MemberScope

internal class DescriptorRegistrar(val moduleDescriptor: ModuleDescriptor) {

    private var isFrozen = false

    private val mutableDescriptorsWithIrTemplate = mutableMapOf<DeclarationDescriptor, IrTemplate<*, *, *>>()

    val descriptorsWithIrTemplate: Map<DeclarationDescriptor, IrTemplate<*, *, *>> by ::mutableDescriptorsWithIrTemplate

    private val syntheticPackageChildren = mutableListOf<DeclarationDescriptor>()

    val syntheticPackageDescriptor: PackageFragmentDescriptor = SyntheticPackageFragmentDescriptor(
        module = moduleDescriptor,
        fqName = FqName("co.touchlab.swiftgen.generated"),
        memberScope = SimpleMemberScope(syntheticPackageChildren),
    )

    init {
        require(moduleDescriptor.name.asString() == "<Kotlin>") {
            "Descriptors can only be injected into the <Kotlin> module used in the linking pass."
        }
    }

    fun registerDescriptors() {
        freeze()

        addDescriptorsToSyntheticPackage()

        addSyntheticPackageToModule()
    }

    private fun addDescriptorsToSyntheticPackage() {
        syntheticPackageChildren.addAll(descriptorsWithIrTemplate.keys)
    }

    private fun addSyntheticPackageToModule() {
        val allPackagedDescriptors = listOf(
            MutablePackageFragmentDescriptor(moduleDescriptor, FqName("co")),
            MutablePackageFragmentDescriptor(moduleDescriptor, FqName("co.touchlab")),
            MutablePackageFragmentDescriptor(moduleDescriptor, FqName("co.touchlab.swiftgen")),
            syntheticPackageDescriptor,
        )

        val generatedPackageProvider = PackageFragmentProviderImpl(allPackagedDescriptors)
        val existingPackageProvider = moduleDescriptor.reflectedBy<ModuleDescriptorImplReflector>().packageFragmentProviderForModuleContent
        val innerProviders = existingPackageProvider.reflectedBy<CompositePackageFragmentProviderReflector>().providers

        innerProviders.add(generatedPackageProvider)
    }

    fun <D : DeclarationDescriptor> add(irTemplate: IrTemplate<D, *, *>): D {
        checkNotFrozen()

        val descriptor = irTemplate.createDescriptor()

        mutableDescriptorsWithIrTemplate[descriptor] = irTemplate

        return descriptor
    }

    private fun freeze() {
        isFrozen = true
    }

    private fun checkNotFrozen() {
        check(!isFrozen) { "Cannot add additional IR after the IR generation began." }
    }

    private class SyntheticPackageFragmentDescriptor(
        module: ModuleDescriptor,
        fqName: FqName,
        private val memberScope: MemberScope,
    ) : PackageFragmentDescriptorImpl(module, fqName) {

        override fun getMemberScope(): MemberScope = memberScope
    }
}