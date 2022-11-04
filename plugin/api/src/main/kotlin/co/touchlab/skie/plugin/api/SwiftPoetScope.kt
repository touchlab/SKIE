package co.touchlab.skie.plugin.api

import co.touchlab.skie.plugin.api.type.KotlinTypeSpecKind
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.TypeName
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.types.KotlinType

interface SwiftPoetScope : SwiftScope {
    fun KotlinType.spec(kind: KotlinTypeSpecKind): TypeName

    val ClassDescriptor.spec: DeclaredTypeName

    val PropertyDescriptor.spec: PropertySpec

    val FunctionDescriptor.spec: FunctionSpec
}
