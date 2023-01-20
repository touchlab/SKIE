package co.touchlab.skie.plugin.api.module

import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.translation.KotlinTypeSpecUsage
import co.touchlab.skie.plugin.api.model.type.translation.NativeKotlinType
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.TypeName
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.types.KotlinType

interface SwiftPoetScope : SwiftModelScope {

    val KotlinType.native: NativeKotlinType

    fun PrimitiveType.spec(usage: KotlinTypeSpecUsage): TypeName

    fun KotlinType.spec(usage: KotlinTypeSpecUsage): TypeName

    fun NativeKotlinType.spec(usage: KotlinTypeSpecUsage): TypeName

    val PropertyDescriptor.regularPropertySpec: PropertySpec

    val PropertyDescriptor.interfaceExtensionPropertySpec: FunctionSpec

    val FunctionDescriptor.spec: FunctionSpec
}

context(SwiftPoetScope)
val ClassDescriptor.stableSpec: DeclaredTypeName
    get() = DeclaredTypeName.qualifiedLocalTypeName(this.swiftModel.stableFqName)

context(SwiftPoetScope)
val SourceFile.stableSpec: DeclaredTypeName
    get() = DeclaredTypeName.qualifiedLocalTypeName(this.swiftModel.stableFqName)
