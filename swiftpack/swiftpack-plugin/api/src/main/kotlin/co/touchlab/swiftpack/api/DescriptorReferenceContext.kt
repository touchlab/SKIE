package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.reference.KotlinClassReference
import co.touchlab.swiftpack.spec.reference.KotlinEnumEntryReference
import co.touchlab.swiftpack.spec.reference.KotlinFunctionReference
import co.touchlab.swiftpack.spec.reference.KotlinPropertyReference
import co.touchlab.swiftpack.spec.reference.KotlinTypeReference
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.types.KotlinType

interface DescriptorReferenceContext {
    fun ClassDescriptor.classReference(): KotlinClassReference

    fun ClassDescriptor.enumEntryReference(): KotlinEnumEntryReference

    fun KotlinType.reference(): KotlinTypeReference<*>

    fun PropertyDescriptor.reference(): KotlinPropertyReference

    fun FunctionDescriptor.reference(): KotlinFunctionReference
}
