package co.touchlab.skie.plugin.generator.internal.coroutines.suspend

import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.function.name
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

context(SwiftModelScope)
internal val FunctionDescriptor.suspendWrapperFunctionIdentifier: String
    get() {
        val suffixForPreventingCollisions = this.swiftModel.name.removeSuffix(":)").takeLastWhile { it == '_' }

        return this.swiftModel.identifier + suffixForPreventingCollisions
    }
