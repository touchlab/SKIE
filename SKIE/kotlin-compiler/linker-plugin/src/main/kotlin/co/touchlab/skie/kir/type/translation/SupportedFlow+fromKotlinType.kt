package co.touchlab.skie.kir.type.translation

import co.touchlab.skie.kir.type.SupportedFlow
import co.touchlab.skie.kir.type.SupportedFlow.values
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType

fun SupportedFlow.Companion.from(type: KotlinType): SupportedFlow? =
    (type.constructor.declarationDescriptor as? ClassDescriptor)?.let { from(it) }

private fun from(classDescriptor: ClassDescriptor): SupportedFlow? {
    val classFqName = classDescriptor.fqNameSafe.asString()

    return values().firstOrNull { it.coroutinesFlowFqName == classFqName }
}
