package co.touchlab.skie.plugin.generator.internal.datastruct

import co.touchlab.skie.plugin.api.model.type.translation.KotlinTypeSpecUsage
import co.touchlab.skie.plugin.api.model.type.translation.NativeKotlinType
import co.touchlab.skie.plugin.api.model.type.translation.NativeKotlinType.Reference.Known.Array.Primitive
import co.touchlab.skie.plugin.api.module.SwiftPoetScope
import io.outfoxx.swiftpoet.ARRAY
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DOUBLE
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileMemberSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.INT
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterizedTypeName
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.UINT8
import io.outfoxx.swiftpoet.parameterizedBy
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor

object ArrayDataStructTypeMapper : DataStructTypeMapper {

    private val helpers = mutableMapOf<NativeKotlinType.Reference.Known.Array, List<FileMemberSpec>>()

    context(SwiftPoetScope)
    override fun provideMapping(
        property: PropertyDescriptor,
        parameter: ValueParameterDescriptor,
    ): DataStructTypeMapper.Mapping? {
        val propertyType = property.type.native as? NativeKotlinType.Reference.Known.Array ?: return null
        return mapping(property.regularPropertySpec.name, propertyType)
    }

    context(SwiftPoetScope)
    private fun mapping(
        propertyName: String,
        arrayType: NativeKotlinType.Reference.Known.Array,
    ): DataStructTypeMapper.Mapping {
        return DataStructTypeMapper.Mapping(
            swiftTypeName = ARRAY.parameterizedBy(arrayType.swiftElementType),
            additionalSingletonDeclarations = helpersFor(arrayType),
            kotlinToSwiftMapping = CodeBlock.of("Array(kotlinArray: %N)", propertyName),
            swiftToKotlinMapping = CodeBlock.of("%T.from(array: %N)", arrayType.spec(KotlinTypeSpecUsage.TypeParam), propertyName),
        )
    }

    context(SwiftPoetScope)
    private fun helpersFor(arrayType: NativeKotlinType.Reference.Known.Array): List<FileMemberSpec> =
        helpers.getOrPut(arrayType) {
            val (kotlinToSwift, swiftToKotlin) = when (arrayType) {
                is NativeKotlinType.Reference.Known.Array.Generic -> if (arrayType.elementType == NativeKotlinType.Reference.Known.String) {
                    CodeBlock.of(
                        """
                            assert(value != nil, "A string in an arrays of strings is nullable even though it wasn't in Kotlin")
                            return %T(value ?? "")
                        """.trimIndent(), STRING
                    ) to CodeBlock.of("return value as %T", SwiftType.nsString)
                } else {
                    // TODO: Add support for arrays of data structs
                    return emptyList()
                }
                is Primitive -> {
                    val kotlinElementType = arrayType.elementType.spec(KotlinTypeSpecUsage.TypeParam)
                    val bridgedElementType = arrayType.elementType.spec(KotlinTypeSpecUsage.Default)
                    val swiftElementType = arrayType.swiftElementType
                    when (arrayType.elementType) {
                        PrimitiveType.BOOLEAN -> CodeBlock.of(
                            "return %T(value)",
                            swiftElementType
                        ) to CodeBlock.of("return %T(value: value)", kotlinElementType)
                        PrimitiveType.BYTE, PrimitiveType.SHORT, PrimitiveType.INT, PrimitiveType.LONG, PrimitiveType.FLOAT, PrimitiveType.DOUBLE ->
                            CodeBlock.of("return %T(value)", swiftElementType) to CodeBlock.of(
                                "return %T(value: %T(value))",
                                kotlinElementType,
                                bridgedElementType
                            )
                        PrimitiveType.CHAR -> CodeBlock.of(
                            """
                        let char = UnicodeScalar(value)
                        assert(char != nil, "A unichar is not convertable to UnicodeScalar!")
                        return %T(char ?? '?')
                    """.trimIndent(), SwiftType.unicodeScalar, SwiftType.character
                        ) to CodeBlock.of(
                            """
                        assert(value.utf16.count == 1, "A Character is not convertable to unichar!")
                        return value.utf16.first!
                    """.trimIndent()
                        )
                    }
                }
            }

            val kotlinArrayType = arrayType.spec(KotlinTypeSpecUsage.Default)
            val swiftElementType = arrayType.swiftElementType
            listOf(
                ExtensionSpec.builder(ARRAY)
                    .addModifiers(Modifier.INTERNAL)
                    .addConditionalConstraint(
                        TypeVariableName.typeVariable(
                            "Element",
                            TypeVariableName.bound(
                                TypeVariableName.Bound.Constraint.SAME_TYPE,
                                swiftElementType,
                            ),
                        )
                    )
                    .addFunction(
                        FunctionSpec.constructorBuilder()
                            .addParameter("kotlinArray", kotlinArrayType)
                            .addCode(
                                """
                                self = (0 ..< Int(kotlinArray.size)).map { index in
                                    let value = kotlinArray.get(index: Int32(index))
                                    %L
                                }${"\n"}
                                """.trimIndent(), kotlinToSwift
                            )
                            .build()
                    )
                    .build(),
                ExtensionSpec.builder(arrayType.rawKotlinName)
                    .addModifiers(Modifier.INTERNAL)
                    .addFunction(
                        FunctionSpec.builder("from")
                            .addAttribute("objc")
                            .addModifiers(Modifier.STATIC)
                            .addParameter("array", ARRAY.parameterizedBy(swiftElementType))
                            .returns(kotlinArrayType)
                            .addCode(
                                """
                                return %T(size: Int32(array.count)) { index in
                                    let value = array[index.intValue]
                                    %L
                                }
                                """.trimIndent(), kotlinArrayType, swiftToKotlin
                            )
                            .build()
                    )
                    .build(),
            ).map { FileMemberSpec.builder(it).build() }
        }

    context(SwiftPoetScope)
    private val NativeKotlinType.Reference.Known.Array.swiftElementType
        get() = when (this) {
            is Primitive -> when (elementType) {
                PrimitiveType.BOOLEAN -> BOOL
                PrimitiveType.BYTE -> UINT8
                PrimitiveType.SHORT, PrimitiveType.INT, PrimitiveType.LONG -> INT
                PrimitiveType.FLOAT, PrimitiveType.DOUBLE -> DOUBLE
                PrimitiveType.CHAR -> SwiftType.character
            }
            is NativeKotlinType.Reference.Known.Array.Generic -> elementType.spec(KotlinTypeSpecUsage.Default)
        }

    context(SwiftPoetScope)
    private val NativeKotlinType.Reference.Known.Array.rawKotlinName: DeclaredTypeName
        get() = when (val typeName = this.spec(KotlinTypeSpecUsage.Default)) {
            is DeclaredTypeName -> typeName
            is ParameterizedTypeName -> typeName.rawType
            else -> error("Unexpected type: $typeName")
        }
}
