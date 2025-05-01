package co.touchlab.skie.compilerinject.reflection.reflectors

import co.touchlab.skie.compilerinject.reflection.Reflector
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.impl.CompositePackageFragmentProvider
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl

class ModuleDescriptorImplReflector(override val instance: ModuleDescriptor) : Reflector(ModuleDescriptorImpl::class) {

    val packageFragmentProviderForModuleContent by declaredField<CompositePackageFragmentProvider>()
}
