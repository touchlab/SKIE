package co.touchlab.skie.plugin.generator.internal.datastruct

import co.touchlab.skie.plugin.api.model.property.reference
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSpecUsage
import co.touchlab.skie.plugin.api.model.type.NativeKotlinType
import co.touchlab.skie.plugin.api.module.SwiftPoetScope
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor

object ScalarDataStructTypeMapper : DataStructTypeMapper {

    context(SwiftPoetScope) override fun provideMapping(
        property: PropertyDescriptor,
        parameter: ValueParameterDescriptor,
    ): DataStructTypeMapper.Mapping? {
        fun initializerMapping(kotlinName: TypeName, swiftName: TypeName) = DataStructTypeMapper.Mapping(
            swiftTypeName = swiftName,
            kotlinToSwiftMapping = CodeBlock.of("%T(%N)", swiftName, property.swiftModel.reference),
            swiftToKotlinMapping = CodeBlock.of("%T(%N)", kotlinName, parameter.name.asString()),
        )

        return when (val nativeType = property.type.native) {
            NativeKotlinType.Value.BOOL -> DataStructTypeMapper.Mapping(swiftTypeName = SwiftType.bool)
            is NativeKotlinType.Value -> {
                initializerMapping(nativeType.spec(KotlinTypeSpecUsage.Default), nativeType.swiftPrimitiveType)
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
