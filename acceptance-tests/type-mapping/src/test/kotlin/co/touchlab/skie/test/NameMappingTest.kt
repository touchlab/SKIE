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
import com.squareup.kotlinpoet.PropertySpec
import kotlin.io.path.writer

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeVariableName
import io.mockk.InternalPlatformDsl.toArray
import org.jetbrains.kotlin.konan.file.File
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.math.ceil
import kotlin.streams.toList
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class NameMappingTest {

    @OptIn(ExperimentalTime::class)
    @Test
    fun runTest() {
        System.setProperty("konan.home", BuildConfig.KONAN_HOME)
        val tempDirectory = Path(BuildConfig.BUILD).resolve("test-temp")
        tempDirectory.toFile().deleteRecursively()
        tempDirectory.toFile().mkdirs()

        val compilerConfiguration = CompilerConfiguration(
            dependencies = BuildConfig.DEPENDENCIES.toList(),
            exportedDependencies = BuildConfig.EXPORTED_DEPENDENCIES.toList(),
        )

        val splitTypes = TestedType.ALL.sortedBy { it.safeName }.chunked(2000)

        val onlyIndices = setOf<Int>(
            // *(0..31).toList().toTypedArray()
            // 126, 128, 129, 130, 131, 145, 336, 337, 339, 340, 341, 356
        )

        val testCompletionTracking = AtomicInteger(0)
        val testsToRun = splitTypes
            .mapIndexed { index, testedTypes -> index to testedTypes }
            .filter { onlyIndices.isEmpty() || onlyIndices.contains(it.first) }

        val failures = testsToRun
            .parallelStream()
            .map { (index, types) ->
                val testTime = measureTimedValue {
                    runTestForTypes(
                        types = types,
                        tempDirectory = tempDirectory.resolve("test-$index"),
                        compilerConfiguration = compilerConfiguration,
                    )
                }
                val result = testTime.value
                println("[${if (result is TestResult.Success) "PASS" else "FAIL"}] Finished test $index (${testCompletionTracking.incrementAndGet()}/${testsToRun.size}) in ${testTime.duration.toString(DurationUnit.SECONDS, 2)} seconds")
                index to result
            }
            .filter { it.second !is TestResult.Success }
            .collect(Collectors.toList())
            // TODO: Change to `toList()`
            // .toList()

        if (failures.isNotEmpty()) {
            failures.forEach { (index, result) ->
                println(result.actualErrorMessage)
            }
            println("To run only failed tests:")
            println(failures.joinToString(", ") { "${it.first}" })
            fail("${failures.size} tests failed.")
        }

    }

    private fun List<TestedType>.findClassTypeParams(): List<TypeVariableName> {
        fun TestedType.findClassTypeParams(): List<TypeVariableName> = when (this) {
            is TestedType.CopiedType -> (kotlinType as? TypeVariableName)?.let { listOf(it) } ?: emptyList()
            is TestedType.WithTypeParameters -> typeParameters.findClassTypeParams()
            is TestedType.Lambda -> parameterTypes.findClassTypeParams() + returnType.findClassTypeParams()
            is TestedType.Nullable -> wrapped.findClassTypeParams()
            is TestedType.TypeParam -> listOf(this.kotlinType)
            else -> emptyList()
        }
        return flatMap { testedType ->
            testedType.findClassTypeParams()
        }
    }

    private fun runTestForTypes(
        types: List<TestedType>,
        tempDirectory: Path,
        compilerConfiguration: CompilerConfiguration,
    ): TestResult {
        val tempFileSystem = TempFileSystem(tempDirectory)
        val tempSourceFile = tempDirectory.resolve("KotlinFile.kt")

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
                        types.findClassTypeParams().distinct()
                    )
                    .apply {
                        types.forEach { type ->
                            addProperty(
                                PropertySpec.builder("property_" + type.safeName, type.kotlinType)
                                    .initializer("TODO()")
                                    .build()
                            )
                        }

                        types.forEach { type ->
                            addFunction(
                                FunSpec.builder("function_${type.safeName}")
                                    .addParameter("value", type.kotlinType)
                                    .returns(type.kotlinType)
                                    .addStatement("return value")
                                    .build()
                            )
                        }

                        types.forEach { type ->
                            addFunction(
                                FunSpec.builder("suspend_function_${type.safeName}")
                                    .addModifiers(KModifier.SUSPEND)
                                    .addParameter("value", type.kotlinType)
                                    .returns(type.kotlinType)
                                    .addStatement("return value")
                                    .build()
                            )
                        }

                        types.forEach { type ->
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

        return IntermediateResult.Value(listOf(tempSourceFile))
            .flatMap {
                val compiler = KotlinTestCompiler(tempFileSystem, testLogger)
                compiler.compile(
                    listOf(tempSourceFile),
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
            .also {
                tempDirectory.resolve("run.log").writeText(testLogger.toString())
            }
    }
}
