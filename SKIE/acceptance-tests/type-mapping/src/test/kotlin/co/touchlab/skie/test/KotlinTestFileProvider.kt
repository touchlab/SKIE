package co.touchlab.skie.test

import co.touchlab.skie.kotlingenerator.ir.KotlinClass
import co.touchlab.skie.kotlingenerator.ir.KotlinDeclaration
import co.touchlab.skie.kotlingenerator.ir.KotlinFile
import co.touchlab.skie.kotlingenerator.ir.KotlinFunction
import co.touchlab.skie.kotlingenerator.ir.KotlinProperty
import co.touchlab.skie.kotlingenerator.ir.KotlinType
import co.touchlab.skie.kotlingenerator.ir.KotlinTypeParameter
import co.touchlab.skie.kotlingenerator.ir.KotlinValueParameter
import co.touchlab.skie.kotlingenerator.ir.parameterizedBy

object KotlinTestFileProvider {

    fun getTestFile(types: List<KotlinType>): KotlinFile =
        KotlinFile(
            packageName = "co.touchlab.skie.test",
            declarations = listOf(
                KotlinClass(
                    name = "TestInterface",
                    kind = KotlinClass.Kind.Interface,
                ),
                KotlinClass(
                    name = "SingleTypeParamClass",
                    typeParameters = listOf(
                        KotlinTypeParameter("T"),
                    ),
                ),
                KotlinClass(
                    name = "SingleAnyTypeParamClass",
                    typeParameters = listOf(
                        KotlinTypeParameter("T", KotlinType.Declared("kotlin", "Any")),
                    ),
                ),
                KotlinClass(
                    name = "RecursiveGenericsInterface",
                    kind = KotlinClass.Kind.Interface,
                    typeParameters = listOf(
                        KotlinTypeParameter("T") {
                            listOf(
                                KotlinType.Declared("co.touchlab.skie.test", "RecursiveGenericsInterface")
                                    .parameterizedBy(it),
                            )
                        },
                    ),
                ),
                KotlinClass(
                    name = "KotlinFile",
                    typeParameters = getTestClassTypeParameters(types),
                    declarations = getTestClassDeclarations(types),
                ),
            ),
        )

    private fun getTestClassTypeParameters(testTypes: List<KotlinType>): List<KotlinTypeParameter> {
        fun KotlinType.getTypeParametersRecursively(visitedTypeParameters: List<KotlinTypeParameter> = emptyList()): List<KotlinTypeParameter> =
            when (this) {
                is KotlinType.Declared -> emptyList()
                is KotlinType.Lambda -> {
                    this.parameterTypes.flatMap { it.getTypeParametersRecursively(visitedTypeParameters) } +
                        this.returnType.getTypeParametersRecursively(visitedTypeParameters) +
                        (this.receiverType?.getTypeParametersRecursively(visitedTypeParameters) ?: emptyList())
                }
                is KotlinType.Optional -> this.wrapped.getTypeParametersRecursively(visitedTypeParameters)
                is KotlinType.Parametrized -> {
                    this.typeArguments.flatMap { it.getTypeParametersRecursively(visitedTypeParameters) } +
                        this.kotlinType.getTypeParametersRecursively(visitedTypeParameters)
                }
                KotlinType.Star -> emptyList()
                is KotlinType.TypeParameterUsage -> {
                    if (this.typeParameter in visitedTypeParameters) {
                        emptyList()
                    } else {
                        this.getTypeParametersRecursively(visitedTypeParameters + this.typeParameter) + this.typeParameter
                    }
                }
            }

        return testTypes.flatMap { it.getTypeParametersRecursively() }.distinct().sortedBy { it.name }
    }

    private fun getTestClassDeclarations(testTypes: List<KotlinType>): List<KotlinDeclaration> =
        emptyList<KotlinDeclaration>() +
            testTypes
                .map { kotlinType ->
                    KotlinProperty(
                        name = "property_${kotlinType.getSafeName()}",
                        type = kotlinType,
                        initializer = "TODO()",
                    )
                } +
            testTypes
                .map { kotlinType ->
                    KotlinFunction(
                        name = "function_${kotlinType.getSafeName()}",
                        valueParameters = listOf(
                            KotlinValueParameter("value", kotlinType),
                        ),
                        returnType = kotlinType,
                        body = "return value",
                    )
                } +
            testTypes
                // There is a bug in the Kotlin compiler causing a crash when a function throws and returns a NativePtr
                .filter { it != TestedType.Builtin.NativePtr.kotlinType }
                .filterNot { it is KotlinType.TypeParameterUsage && it.typeParameter.bounds == listOf(TestedType.Builtin.NativePtr.kotlinType) }
                .map { kotlinType ->
                    KotlinFunction(
                        name = "throwing_function_${kotlinType.getSafeName()}",
                        valueParameters = listOf(
                            KotlinValueParameter("value", kotlinType),
                        ),
                        returnType = kotlinType,
                        body = "return value",
                        annotations = listOf(
                            "@Throws(Exception::class)",
                        ),
                    )
                } +
            testTypes
                .map { kotlinType ->
                    KotlinFunction(
                        name = "suspend_function_${kotlinType.getSafeName()}",
                        valueParameters = listOf(
                            KotlinValueParameter("value", kotlinType),
                        ),
                        returnType = kotlinType,
                        body = "return value",
                        isSuspend = true,
                    )
                } +
            testTypes
                .map { kotlinType ->
                    KotlinFunction(
                        name = "extension_function_${kotlinType.getSafeName()}",
                        extensionReceiver = kotlinType,
                        valueParameters = listOf(
                            KotlinValueParameter("value", kotlinType),
                        ),
                        returnType = kotlinType,
                        body = "return value",
                    )
                }
}
