package co.touchlab.skie.api.model

import co.touchlab.skie.plugin.api.model.SwiftModelBuiltIns
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

class DefaultSwiftModelBuiltIns(
    private val swiftModelScope: SwiftModelScope,
) : SwiftModelBuiltIns {

    override val skieFlow: KotlinClassSwiftModel by lazy {
        swiftModelScope.exposedClasses
            .single { it.classDescriptor.fqNameSafe == FqName("co.touchlab.skie.runtime.coroutines.flow.SkieFlow") }
    }

    override val skieOptionalFlow: KotlinClassSwiftModel by lazy {
        swiftModelScope.exposedClasses
            .single { it.classDescriptor.fqNameSafe == FqName("co.touchlab.skie.runtime.coroutines.flow.SkieOptionalFlow") }
    }
}
