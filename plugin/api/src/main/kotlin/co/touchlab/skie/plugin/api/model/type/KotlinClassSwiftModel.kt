package co.touchlab.skie.plugin.api.model.type

import org.jetbrains.kotlin.descriptors.ClassDescriptor

interface KotlinClassSwiftModel : KotlinTypeSwiftModel {

    val classDescriptor: ClassDescriptor

    override val original: KotlinClassSwiftModel
}
