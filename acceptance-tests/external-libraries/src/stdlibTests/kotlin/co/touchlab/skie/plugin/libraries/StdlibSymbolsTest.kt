package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestLogger
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.CompilerArgumentsProvider
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestCompiler
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestLinker
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift.SwiftTestCompiler
import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.framework.BuildConfig
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import io.kotest.assertions.fail
import org.jetbrains.kotlin.backend.common.serialization.metadata.DynamicTypeDeserializer
import org.jetbrains.kotlin.builtins.konan.KonanBuiltIns
import org.jetbrains.kotlin.cli.klib.currentApiVersion
import org.jetbrains.kotlin.cli.klib.currentLanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.deserialization.PlatformDependentTypeTransformer
import org.jetbrains.kotlin.descriptors.packageFragments
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.util.KlibMetadataFactories
import org.jetbrains.kotlin.library.resolveSingleFileKlib
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyPublicApi
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import kotlin.io.path.Path
import kotlin.io.path.toPath
import kotlin.io.path.writeText
import kotlin.io.path.writer
import io.kotest.core.spec.style.FunSpec as KotestFunSpec

class StdlibSymbolsTest: KotestFunSpec({
    System.setProperty("konan.home", BuildConfig.KONAN_HOME)

    test("Compile all Kotlin Stdlib Symbols") {

        val tempDirectory = Path(System.getProperty("testTmpDir")).also {
            it.File().mkdirs()
        }

        val klibFactories = KlibMetadataFactories(::KonanBuiltIns, DynamicTypeDeserializer, PlatformDependentTypeTransformer.None)
        val stdlib = resolveSingleFileKlib(File(BuildConfig.KONAN_HOME, "klib/common/stdlib"))
        val storageManager = LockBasedStorageManager("klib")
        val versionSpec = LanguageVersionSettingsImpl(currentLanguageVersion, currentApiVersion)
        val module = klibFactories.DefaultDeserializedDescriptorFactory.createDescriptorAndNewBuiltIns(stdlib, versionSpec, storageManager, null)
        module.setDependencies(listOf(module))

        val packageFragmentProvider = module.packageFragmentProvider

        fun FqName.allPackages(): List<FqName> {
            return packageFragmentProvider.getSubPackagesOf(this) { true }
                .flatMap {
                    listOf(it) + it.allPackages()
                }
        }

        val unsupportedClassNames = listOf(
            "kotlin.native.Vector128",
        ).map { FqName(it) }.toSet()
        val classKinds = setOf(
            ClassKind.CLASS,
            ClassKind.ENUM_CLASS,
            ClassKind.INTERFACE,
            ClassKind.OBJECT,
        )
        fun Collection<DeclarationDescriptor>.allPublicClasses(): List<ClassDescriptor> {
            val classes = filterIsInstance<ClassDescriptor>().filter {
                it.isEffectivelyPublicApi && it.kind in classKinds && it.fqNameSafe !in unsupportedClassNames
            }

            return classes + classes.flatMap {
                it.staticScope.getDescriptorsFiltered { true }.allPublicClasses()
            }
        }

        fun Collection<DeclarationDescriptor>.allPublicGlobalFunctions(): List<FunctionDescriptor> {
            return filterIsInstance<FunctionDescriptor>().filter { it.isEffectivelyPublicApi }
        }

        val allPackageNames = FqName.ROOT.allPackages()
        val allPackages = allPackageNames.flatMap(packageFragmentProvider::packageFragments)
        val allDescriptors = allPackages.flatMap { it.getMemberScope().getContributedDescriptors() }
        val allClasses = allDescriptors.allPublicClasses()
        val allGlobalFunctions = allDescriptors.allPublicGlobalFunctions()

        val tempFileSystem = TempFileSystem(tempDirectory)
        val tempSourceFile = tempDirectory.resolve("KotlinFile.kt")

        FileSpec.builder("co.touchlab.skie.tests.stdlib", "StdlibSymbolAccess")
            .apply {
                allClasses.forEach { decl ->
                    addProperty(
                        PropertySpec
                            .builder(
                                decl.name.asString(),
                                decl.typeNameWithStarTypeParameters,
                            )
                            .getter(
                                FunSpec.getterBuilder()
                                    .addStatement("""error("Not supposed to be called.")""")
                                    .build(),
                            )
                            .build(),
                    )
                }
            }
            .build()
            .also {
                tempSourceFile.writer().use { writer ->
                    it.writeTo(writer)
                }
            }

        val testLogger = TestLogger()

        val skieConfiguration = Configuration {

        }

        val compilerArgumentsProvider = CompilerArgumentsProvider(
            optIn = listOf(
                "kotlin.ExperimentalMultiplatform",
                "kotlin.ExperimentalStdlibApi",
                "kotlin.experimental.ExperimentalTypeInference",
                "kotlin.time.ExperimentalTime",
                "kotlin.contracts.ExperimentalContracts",
                "kotlin.native.internal.InternalForKotlinNative",
            )
        )

        val swiftMainFile = javaClass.classLoader.getResource("main.swift")!!.toURI().toPath()
        val result = IntermediateResult.Value(listOf(tempSourceFile))
            .flatMap {
                val compiler = KotlinTestCompiler(tempFileSystem, testLogger)
                compiler.compile(
                    listOf(tempSourceFile),
                    compilerArgumentsProvider,
                    listOf(tempSourceFile),
                )
            }
            .flatMap {
                val linker = KotlinTestLinker(tempFileSystem, testLogger, true)
                linker.link(
                    it,
                    skieConfiguration,
                    compilerArgumentsProvider,
                )
            }
            .flatMap {
                val swiftCompiler = SwiftTestCompiler(tempFileSystem, testLogger, compilerArgumentsProvider.target)
                swiftCompiler.compile(it, swiftMainFile)
            }
            .finalize {
                TestResult.Success
            }
            .also {
                tempDirectory.resolve("run.log").writeText(testLogger.toString())
            }

        if (result != TestResult.Success) {
            fail("Test failed: $result")
        }
    }
})

val ClassDescriptor.className: ClassName
    get() {
        return when (val parent = containingDeclaration) {
            is ClassDescriptor -> parent.className.nestedClass(name.asString())
            is PackageFragmentDescriptor -> ClassName(parent.fqName.asString(), name.asString())
            else -> error("Unknown parent type: $parent")
        }
    }

val ClassDescriptor.typeNameWithStarTypeParameters: TypeName
    get() {
        return if (typeConstructor.parameters.isEmpty()) {
            className
        } else {
            className.parameterizedBy(
                typeConstructor.parameters.map {
                    STAR
                }
            )
        }
    }
