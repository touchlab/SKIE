package co.touchlab.skie.kir.descriptor

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor

interface MutableDescriptorProvider : DescriptorProvider {

    fun exposeCallableMember(callableDeclaration: CallableMemberDescriptor)
}
