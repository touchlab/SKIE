package co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors

import co.touchlab.swiftgen.plugin.internal.util.reflection.Reflector
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.impl.CompositePackageFragmentProvider
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl

internal class ModuleDescriptorImplReflector(
    override val instance: ModuleDescriptor,
) : Reflector(ModuleDescriptorImpl::class) {

    val packageFragmentProviderForModuleContent by declaredField<CompositePackageFragmentProvider>()
}