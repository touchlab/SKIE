package co.touchlab.skie.plugin.generator.internal.datastruct

import co.touchlab.skie.plugin.api.NativeKotlinType
import co.touchlab.skie.plugin.api.NativeKotlinType.Reference.Known.Array.Primitive
import co.touchlab.skie.plugin.api.SwiftPoetScope
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DOUBLE
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileMemberSpec
import io.outfoxx.swiftpoet.INT
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.UINT8
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import kotlin.reflect.full.valueParameters

sealed interface DataStructTypeMapper {
    context(SwiftPoetScope)
    fun provideMapping(property: PropertyDescriptor, parameter: ValueParameterDescriptor): Mapping?

    val NativeKotlinType.Value.swiftPrimitiveType: DeclaredTypeName
        get() = when (this) {
            NativeKotlinType.Value.BOOL -> BOOL
            NativeKotlinType.Value.UNICHAR -> SwiftType.character
            NativeKotlinType.Value.CHAR -> UINT8
            NativeKotlinType.Value.SHORT, NativeKotlinType.Value.INT, NativeKotlinType.Value.LONG_LONG -> INT
            NativeKotlinType.Value.UNSIGNED_CHAR -> UINT8
            NativeKotlinType.Value.UNSIGNED_SHORT, NativeKotlinType.Value.UNSIGNED_INT, NativeKotlinType.Value.UNSIGNED_LONG_LONG -> SwiftType.uint
            NativeKotlinType.Value.FLOAT, NativeKotlinType.Value.DOUBLE -> DOUBLE
            NativeKotlinType.Value.POINTER -> TODO("pointer?")
        }

    data class Mapping(
        val swiftTypeName: TypeName,
        val additionalSingletonDeclarations: List<FileMemberSpec> = emptyList(),
        val kotlinToSwiftMapping: CodeBlock? = null,
        val swiftToKotlinMapping: CodeBlock? = null,
    ) {
        constructor(
            swiftTypeName: TypeName,
            additionalSingletonDeclaration: FileMemberSpec,
            kotlinToSwiftMapping: CodeBlock? = null,
            swiftToKotlinMapping: CodeBlock? = null,
        ): this(
            swiftTypeName,
            listOf(additionalSingletonDeclaration),
            kotlinToSwiftMapping,
            swiftToKotlinMapping,
        )
    }

    companion object {
        val supportedBuiltins: List<DataStructTypeMapper> = listOf(
            ScalarDataStructTypeMapper,
            ListDataStructTypeMapper,
            ArrayDataStructTypeMapper,
        )
    }
}
