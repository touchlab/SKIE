package co.touchlab.skie.plugin.api.util

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.supertypes

val KotlinType.isFlow: Boolean
    get() = (supertypes() + this)
        .any { (constructor.declarationDescriptor as? ClassDescriptor)?.fqNameSafe?.asString() == "kotlinx.coroutines.flow.Flow" }
