package co.touchlab.skie.plugin.api.module

import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName
import org.jetbrains.kotlin.descriptors.ClassDescriptor

context(SwiftModelScope)
val ClassDescriptor.stableSpec: DeclaredTypeName
    get() = DeclaredTypeName.qualifiedLocalTypeName(this.swiftModel.stableFqName)
