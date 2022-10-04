package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.symbol.KotlinClass
import co.touchlab.swiftpack.spec.symbol.KotlinEnumEntry
import co.touchlab.swiftpack.spec.symbol.KotlinFunction
import co.touchlab.swiftpack.spec.symbol.KotlinProperty
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.types.KotlinType

interface DescriptorReferenceContext {
    fun ClassDescriptor.classReference(): KotlinClass

    fun ClassDescriptor.enumEntryReference(): KotlinEnumEntry

    fun KotlinType.reference(): co.touchlab.swiftpack.spec.symbol.KotlinType<*>

    fun PropertyDescriptor.reference(): KotlinProperty

    fun FunctionDescriptor.reference(): KotlinFunction
}
