package co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors

import co.touchlab.swiftgen.plugin.internal.util.reflection.Reflector
import org.jetbrains.kotlin.descriptors.impl.DeclarationDescriptorImpl
import org.jetbrains.kotlin.name.Name

internal class DeclarationDescriptorImplReflector(
    override val instance: DeclarationDescriptorImpl,
) : Reflector(DeclarationDescriptorImpl::class) {

    var name: Name by declaredField()
}
