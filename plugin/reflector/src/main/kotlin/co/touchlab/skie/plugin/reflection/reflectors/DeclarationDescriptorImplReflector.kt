package co.touchlab.skie.plugin.reflection.reflectors

import co.touchlab.skie.plugin.reflection.Reflector
import org.jetbrains.kotlin.descriptors.impl.DeclarationDescriptorImpl
import org.jetbrains.kotlin.name.Name

class DeclarationDescriptorImplReflector(
    override val instance: DeclarationDescriptorImpl,
) : Reflector(DeclarationDescriptorImpl::class) {

    var name: Name by declaredField()
}
