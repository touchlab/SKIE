@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.test

import co.touchlab.skie.acceptancetests.framework.CompilerConfiguration
import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestLogger
import co.touchlab.skie.type_mapping.BuildConfig
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import co.touchlab.skie.configuration.Configuration
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.CHAR_SEQUENCE
import com.squareup.kotlinpoet.COMPARABLE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import kotlin.io.path.writer

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MUTABLE_LIST
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeVariableName
import kotlin.test.fail

class NameMappingTest {

    @Test
    fun runTest() {
        System.setProperty("konan.home", BuildConfig.KONAN_HOME)
        val tempDirectory = Path(BuildConfig.BUILD).resolve("test-temp")
        val tempFileSystem = TempFileSystem(tempDirectory)
        val tempSourceFile = tempDirectory.resolve("KotlinFile.kt")

        val compilerConfiguration = CompilerConfiguration(
            dependencies = BuildConfig.DEPENDENCIES.toList(),
            exportedDependencies = BuildConfig.EXPORTED_DEPENDENCIES.toList(),
        )
        // TODO Generate Kotlin code
        FileSpec.builder("co.touchlab.skie.test", "KotlinFile")
            .addType(
                TypeSpec.interfaceBuilder("TestInterface")
                    .build()
            )
            .addType(
                TypeSpec.classBuilder("SingleTypeParamClass")
                    // TODO: Add `T: Any`
                    .addTypeVariable(TypeVariableName("T"))
                    .build()
            )
            .addType(
                TypeSpec.classBuilder("KotlinFile")
                    .addTypeVariables(
                        TestedType.CLASS_TYPE_PARAMS.map { it.kotlinType }
                    )
                    .apply {
                        TestedType.ALL.forEach { type ->
                            addProperty(
                                PropertySpec.builder("property_" + type.safeName, type.kotlinType)
                                    .initializer("TODO()")
                                    .build()
                            )
                        }

                        TestedType.ALL_BUT_SECOND_LEVEL.forEach { type ->
                            addFunction(
                                FunSpec.builder("function_${type.safeName}")
                                    .addParameter("value", type.kotlinType)
                                    .returns(type.kotlinType)
                                    .addStatement("return value")
                                    .build()
                            )
                        }

                        TestedType.ALL_BUT_SECOND_LEVEL.forEach { type ->
                            addFunction(
                                FunSpec.builder("suspend_function_${type.safeName}")
                                    .addModifiers(KModifier.SUSPEND)
                                    .addParameter("value", type.kotlinType)
                                    .returns(type.kotlinType)
                                    .addStatement("return value")
                                    .build()
                            )
                        }

                        TestedType.ALL_BUT_SECOND_LEVEL.forEach { type ->
                            addFunction(
                                FunSpec.builder("extension_function_${type.safeName}")
                                    .receiver(type.kotlinType)
                                    .addParameter("value", type.kotlinType)
                                    .returns(type.kotlinType)
                                    .addStatement("return value")
                                    .build()
                            )
                        }

                        // TestedType.ONLY.ifEmpty { TestedType.BASIC }.forEach { type ->
                        //     if (type == TestedType.Builtin.Nothing) { return@forEach }
                        //     val typeVariable = TypeVariableName("T", type.kotlinType)
                        //     addFunction(
                        //         FunSpec.builder("generic_function_${type.safeName}")
                        //             .addTypeVariable(typeVariable)
                        //             .addParameter("value", typeVariable)
                        //             .returns(typeVariable)
                        //             .addStatement("return value")
                        //             .build()
                        //     )
                        // }

                        // val typeVariable = TypeVariableName("T", SET.parameterizedBy(STRING), LIST.parameterizedBy(STRING), COMPARABLE.parameterizedBy(STRING))
                        // addFunction(
                        //     FunSpec.builder("complex_generic_function")
                        //         .addTypeVariable(typeVariable)
                        //         .addParameter("value", typeVariable)
                        //         .returns(typeVariable)
                        //         .addStatement("return value")
                        //         .build()
                        // )
                    }
                    .build()
            )
            .build()
            .also {
                tempSourceFile.writer().use { writer ->
                    it.writeTo(writer)
                }
            }

        val testLogger = TestLogger()

        val skieConfiguration = Configuration {

        }

        val testResult = IntermediateResult.Value(listOf(tempSourceFile))
            .flatMap {
                val compiler = KotlinTestCompiler(tempFileSystem, testLogger)
                compiler.compile(
                    listOf(tempDirectory.resolve("KotlinFile.kt")),
                    compilerConfiguration,
                )
            }
            .finalize {
                val linker = KotlinTestLinker(tempFileSystem, testLogger)
                linker.link(
                    it,
                    skieConfiguration,
                    compilerConfiguration,
                )
            }

        if (testResult != TestResult.Success) {
            println(testResult.actualErrorMessage)
            fail("Tests failed")
        }
    }
}
