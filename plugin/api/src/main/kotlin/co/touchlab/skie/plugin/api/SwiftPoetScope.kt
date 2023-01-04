package co.touchlab.skie.plugin.api

import co.touchlab.skie.plugin.api.type.KotlinTypeSpecKind
import co.touchlab.skie.plugin.api.type.KotlinTypeSpecUsage
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

interface SwiftPoetScope : SwiftScope {

    val KotlinType.native: NativeKotlinType

    fun KotlinType.spec(kind: KotlinTypeSpecKind): TypeName

    fun KotlinType.spec(usage: KotlinTypeSpecUsage): TypeName

    fun PrimitiveType.spec(kind: KotlinTypeSpecKind): TypeName

    fun NativeKotlinType.spec(kind: KotlinTypeSpecKind): TypeName

    val ClassDescriptor.spec: DeclaredTypeName

    val SourceFile.spec: DeclaredTypeName

    val PropertyDescriptor.spec: PropertySpec

    val FunctionDescriptor.spec: FunctionSpec
}
