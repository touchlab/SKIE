package co.touchlab.skie.plugin.api.util.flow

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType

enum class SupportedFlow(val fromFqName: String, val toNonOptionalFqName: String, val toOptionalFqName: String) {
    Flow(
        "kotlinx.coroutines.flow.Flow",
        "co.touchlab.skie.runtime.coroutines.flow.SkieKotlinFlow",
        "co.touchlab.skie.runtime.coroutines.flow.SkieKotlinOptionalFlow",
    ),
    SharedFlow(
        "kotlinx.coroutines.flow.SharedFlow",
        "co.touchlab.skie.runtime.coroutines.flow.shared.SkieKotlinSharedFlow",
        "co.touchlab.skie.runtime.coroutines.flow.shared.SkieKotlinOptionalSharedFlow",
    );

    companion object {

        fun from(classDescriptor: ClassDescriptor): SupportedFlow? {
            val classFqName = classDescriptor.fqNameSafe.asString()
            classDescriptor.typeConstructor

            return values().firstOrNull { it.fromFqName == classFqName }
        }

        fun from(type: KotlinType): SupportedFlow? =
            (type.constructor.declarationDescriptor as? ClassDescriptor)?.let { from(it) }
    }
}
