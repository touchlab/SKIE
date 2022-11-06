package co.touchlab.skie.plugin.generator.internal.datastruct

import co.touchlab.skie.plugin.api.NativeKotlinType
import co.touchlab.skie.plugin.api.SwiftPoetScope
import co.touchlab.skie.plugin.api.type.KotlinTypeSpecKind
import co.touchlab.skie.plugin.generator.internal.datastruct.DataStructTypeMapper
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isBoolean
import org.jetbrains.kotlin.ir.types.isByte
import org.jetbrains.kotlin.ir.types.isDouble
import org.jetbrains.kotlin.ir.types.isFloat
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isLong
import org.jetbrains.kotlin.ir.types.isShort
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.name.Name

object ScalarDataStructTypeMapper: DataStructTypeMapper {
    context(SwiftPoetScope) override fun provideMapping(
        property: PropertyDescriptor,
        parameter: ValueParameterDescriptor
    ): DataStructTypeMapper.Mapping? {
        fun initializerMapping(kotlinName: TypeName, swiftName: TypeName) = DataStructTypeMapper.Mapping(
            swiftTypeName = swiftName,
            kotlinToSwiftMapping = CodeBlock.of("%T(%N)", swiftName, property.swiftName),
            swiftToKotlinMapping = CodeBlock.of("%T(%N)", kotlinName, parameter.name.asString()),
        )

        return when (val nativeType = property.type.native) {
            NativeKotlinType.Value.BOOL -> DataStructTypeMapper.Mapping(swiftTypeName = SwiftType.bool)
            is NativeKotlinType.Value -> {
                initializerMapping(nativeType.spec(KotlinTypeSpecKind.BRIDGED), nativeType.swiftPrimitiveType)
            }
            NativeKotlinType.Reference.Known.String -> DataStructTypeMapper.Mapping(swiftTypeName = STRING)
            // type.isDataStruct() -> DataStructTypeMapper.Mapping(
            //     swiftTypeName = DeclaredTypeName.kotlin(type.getClass()!!).nestedType("Bridge"),
            //     kotlinToSwiftMapping = CodeBlock.of("%N.bridged", name.mangledKotlinPropertyName()),
            //     swiftToKotlinMapping = CodeBlock.of("%N.unbridged", name.asString()),
            // )
            else -> null
        }
    }
}
