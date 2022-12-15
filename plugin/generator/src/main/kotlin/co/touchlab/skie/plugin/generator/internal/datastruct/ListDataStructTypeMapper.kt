package co.touchlab.skie.plugin.generator.internal.datastruct

import co.touchlab.skie.plugin.api.NativeKotlinType
import co.touchlab.skie.plugin.api.SwiftPoetScope
import co.touchlab.skie.plugin.api.type.KotlinTypeSpecKind
import io.outfoxx.swiftpoet.CodeBlock
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor

object ListDataStructTypeMapper : DataStructTypeMapper {

    context(SwiftPoetScope) override fun provideMapping(
        property: PropertyDescriptor,
        parameter: ValueParameterDescriptor,
    ): DataStructTypeMapper.Mapping? {
        val elementType = when (val nativeType = property.type.native) {
            is NativeKotlinType.Reference.Known.List -> nativeType.elementType
            is NativeKotlinType.Reference.Known.MutableList -> nativeType.elementType
            else -> return null
        }
        val elementGenericType = elementType.spec(KotlinTypeSpecKind.SWIFT_GENERICS)

        return when (elementType) {
            is NativeKotlinType.Value -> when (elementType) {
                NativeKotlinType.Value.BOOL -> DataStructTypeMapper.Mapping(
                    swiftTypeName = SwiftType.arrayOf(SwiftType.bool),
                    kotlinToSwiftMapping = CodeBlock.of("%N.map { \$0.boolValue }", property.swiftName),
                    swiftToKotlinMapping = CodeBlock.of("%N.map { %T(value: \$0) }", property.swiftName, elementGenericType),
                )

                NativeKotlinType.Value.CHAR -> DataStructTypeMapper.Mapping(
                    swiftTypeName = SwiftType.arrayOf(SwiftType.uint8),
                    kotlinToSwiftMapping = CodeBlock.of("%N.map { \$0.uint8Value }", property.swiftName),
                    swiftToKotlinMapping = CodeBlock.of("%N.map { %T(value: Int8(\$0)) }", property.swiftName, elementGenericType),
                )

                NativeKotlinType.Value.SHORT -> DataStructTypeMapper.Mapping(
                    swiftTypeName = SwiftType.arrayOf(SwiftType.int),
                    kotlinToSwiftMapping = CodeBlock.of("%N.map { \$0.intValue }", property.swiftName),
                    swiftToKotlinMapping = CodeBlock.of("%N.map { %T(value: Int16(\$0)) }", property.swiftName, elementGenericType),
                )

                NativeKotlinType.Value.INT -> DataStructTypeMapper.Mapping(
                    swiftTypeName = SwiftType.arrayOf(SwiftType.int),
                    kotlinToSwiftMapping = CodeBlock.of("%N.map { \$0.intValue }", property.swiftName),
                    swiftToKotlinMapping = CodeBlock.of("%N.map { %T(value: Int32(\$0)) }", property.swiftName, elementGenericType),
                )

                NativeKotlinType.Value.LONG_LONG -> DataStructTypeMapper.Mapping(
                    swiftTypeName = SwiftType.arrayOf(SwiftType.int),
                    kotlinToSwiftMapping = CodeBlock.of("%N.map { \$0.intValue }", property.swiftName),
                    swiftToKotlinMapping = CodeBlock.of("%N.map { %T(value: Int64(\$0)) }", property.swiftName, elementGenericType),
                )

                NativeKotlinType.Value.FLOAT -> DataStructTypeMapper.Mapping(
                    swiftTypeName = SwiftType.arrayOf(SwiftType.double),
                    kotlinToSwiftMapping = CodeBlock.of("%N.map { \$0.doubleValue }", property.swiftName),
                    swiftToKotlinMapping = CodeBlock.of("%N.map { %T(value: Float32(\$0)) }", property.swiftName, elementGenericType),
                )

                NativeKotlinType.Value.DOUBLE -> DataStructTypeMapper.Mapping(
                    swiftTypeName = SwiftType.arrayOf(SwiftType.double),
                    kotlinToSwiftMapping = CodeBlock.of("%N.map { \$0.doubleValue }", property.swiftName),
                    swiftToKotlinMapping = CodeBlock.of("%N.map { %T(value: Float64(\$0)) }", property.swiftName, elementGenericType),
                )

                else -> return null
            }

            is NativeKotlinType.Reference.Known.String -> DataStructTypeMapper.Mapping(
                swiftTypeName = SwiftType.arrayOf(SwiftType.string),
            )
            // TODO: Add support for datastructs
            else -> null
        }
    }
}
