package co.touchlab.skie.plugin.reflection.reflectors

import co.touchlab.skie.plugin.reflection.Reflector
import org.jetbrains.kotlin.descriptors.impl.VariableDescriptorImpl
import org.jetbrains.kotlin.types.KotlinType

class VariableDescriptorImplReflector(
    override val instance: VariableDescriptorImpl,
) : Reflector(VariableDescriptorImpl::class) {

    var outType by declaredField<KotlinType>()
}
