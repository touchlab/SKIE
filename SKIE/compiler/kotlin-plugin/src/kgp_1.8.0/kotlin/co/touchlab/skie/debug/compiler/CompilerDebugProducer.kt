@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.debug.compiler

import co.touchlab.skie.debug.compiler.kgp_180.CompilerConfigurationDump
import co.touchlab.skie.plugin.reflection.reflectedBy
import co.touchlab.skie.plugin.reflection.reflectors.UserVisibleIrModulesSupportReflector
import org.jetbrains.kotlin.backend.konan.BinaryOptions
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.Xcode
import org.jetbrains.kotlin.utils.ResolvedDependency

actual class CompilerDebugProducer actual constructor(
    private val config: KonanConfig,
) {

//    override fun produce(): String =
//        Json.encodeToString(getFullCompilerAnalytics())

    private fun getFullCompilerAnalytics(): CompilerConfigurationDump = CompilerConfigurationDump(
        compilerVersion = config.distribution.compilerVersion,
        languageVersion = config.languageVersionSettings.toString(),
        xcodeVersion = Xcode.findCurrent().version,
        properties = config.distribution.properties.mapKeys { it.key.toString() }.mapValues { it.value.toString() },
        host = HostManager.host.name,
        target = config.target.name,
        commonConfig = config.configuration.getCommonConfig(),
        konanConfig = config.configuration.getKonanConfig(),
        binaryConfig = config.configuration.getBinaryConfig(),
        mavenLibraries = getMavenLibraries(),
        cacheSupport = getCacheSupport(),
    )

    private fun CompilerConfiguration.getCommonConfig(): CompilerConfigurationDump.CommonConfig =
        CompilerConfigurationDump.CommonConfig(
            disableInline = get(CommonConfigurationKeys.DISABLE_INLINE),
            moduleName = get(CommonConfigurationKeys.MODULE_NAME),
            reportOutputFiles = get(CommonConfigurationKeys.REPORT_OUTPUT_FILES),
            metadataVersion = get(CommonConfigurationKeys.METADATA_VERSION)?.toString(),
            useFir = get(CommonConfigurationKeys.USE_FIR),
            expectActualLinker = get(CommonConfigurationKeys.EXPECT_ACTUAL_LINKER),
            useFirExtendedCheckers = get(CommonConfigurationKeys.USE_FIR_EXTENDED_CHECKERS),
            parallelBackendThreads = get(CommonConfigurationKeys.PARALLEL_BACKEND_THREADS),
            klibNormalizeAbsolutePath = get(CommonConfigurationKeys.KLIB_NORMALIZE_ABSOLUTE_PATH),
            produceKlibSignaturesClashChecks = get(CommonConfigurationKeys.PRODUCE_KLIB_SIGNATURES_CLASH_CHECKS),
            incrementalCompilation = get(CommonConfigurationKeys.INCREMENTAL_COMPILATION),
            allowAnyScriptsInSourceRoots = get(CommonConfigurationKeys.ALLOW_ANY_SCRIPTS_IN_SOURCE_ROOTS),
        )

    private fun CompilerConfiguration.getKonanConfig(): CompilerConfigurationDump.KonanConfig =
        CompilerConfigurationDump.KonanConfig(
            bundleId = get(KonanConfigKeys.BUNDLE_ID),
            checkDependencies = get(KonanConfigKeys.CHECK_DEPENDENCIES),
            debug = get(KonanConfigKeys.DEBUG),
            fakeOverrideValidator = get(KonanConfigKeys.FAKE_OVERRIDE_VALIDATOR),
            disabledPhases = get(KonanConfigKeys.DISABLED_PHASES),
            bitcodeEmbeddingMode = get(KonanConfigKeys.BITCODE_EMBEDDING_MODE)?.toString(),
            enableAssertions = get(KonanConfigKeys.ENABLE_ASSERTIONS),
            enabledPhases = get(KonanConfigKeys.ENABLED_PHASES),
            entry = get(KonanConfigKeys.ENTRY),
            makePerFileCache = get(KonanConfigKeys.MAKE_PER_FILE_CACHE),
            frameworkImportHeaders = get(KonanConfigKeys.FRAMEWORK_IMPORT_HEADERS),
            generateTestRunner = get(KonanConfigKeys.GENERATE_TEST_RUNNER)?.toString(),
            lightDebug = get(KonanConfigKeys.LIGHT_DEBUG),
            generateDebugTrampoline = get(KonanConfigKeys.GENERATE_DEBUG_TRAMPOLINE),
            linkerArgs = get(KonanConfigKeys.LINKER_ARGS),
            listPhases = get(KonanConfigKeys.LIST_PHASES),
            listTargets = get(KonanConfigKeys.LIST_TARGETS),
            metadataKlib = get(KonanConfigKeys.METADATA_KLIB),
            moduleName = get(KonanConfigKeys.MODULE_NAME),
            noDefaultLibs = get(KonanConfigKeys.NODEFAULTLIBS),
            noEndorsedLibs = get(KonanConfigKeys.NOENDORSEDLIBS),
            noMain = get(KonanConfigKeys.NOMAIN),
            noStdlib = get(KonanConfigKeys.NOSTDLIB),
            noPack = get(KonanConfigKeys.NOPACK),
            optimization = get(KonanConfigKeys.OPTIMIZATION),
            overrideClangOptions = get(KonanConfigKeys.OVERRIDE_CLANG_OPTIONS),
            allocationMode = get(KonanConfigKeys.ALLOCATION_MODE)?.toString(),
            exportKdoc = get(KonanConfigKeys.EXPORT_KDOC),
            printBitcode = get(KonanConfigKeys.PRINT_BITCODE),
            checkExternalCalls = get(KonanConfigKeys.CHECK_EXTERNAL_CALLS),
            printDescriptors = get(KonanConfigKeys.PRINT_DESCRIPTORS),
            printIr = get(KonanConfigKeys.PRINT_IR),
            printIrWithDescriptors = get(KonanConfigKeys.PRINT_IR_WITH_DESCRIPTORS),
            printLocations = get(KonanConfigKeys.PRINT_LOCATIONS),
            printFiles = get(KonanConfigKeys.PRINT_FILES),
            purgeUserLibs = get(KonanConfigKeys.PURGE_USER_LIBS),
            shortModuleName = get(KonanConfigKeys.SHORT_MODULE_NAME),
            staticFramework = get(KonanConfigKeys.STATIC_FRAMEWORK),
            target = get(KonanConfigKeys.TARGET),
            verifyBitcode = get(KonanConfigKeys.VERIFY_BITCODE),
            verifyIr = get(KonanConfigKeys.VERIFY_IR),
            verifyCompiler = get(KonanConfigKeys.VERIFY_COMPILER),
            debugInfoVersion = get(KonanConfigKeys.DEBUG_INFO_VERSION),
            coverage = get(KonanConfigKeys.COVERAGE),
            objcGenerics = get(KonanConfigKeys.OBJC_GENERICS),
            preLinkCaches = get(KonanConfigKeys.PRE_LINK_CACHES),
            overrideKonanProperties = get(KonanConfigKeys.OVERRIDE_KONAN_PROPERTIES),
            destroyRuntimeMode = get(KonanConfigKeys.DESTROY_RUNTIME_MODE)?.toString(),
            garbageCollector = get(KonanConfigKeys.GARBAGE_COLLECTOR)?.toString(),
            checkLldCompatibility = get(KonanConfigKeys.CHECK_LLD_COMPATIBILITY),
            propertyLazyInitialization = get(KonanConfigKeys.PROPERTY_LAZY_INITIALIZATION),
            workerExceptionHandling = get(KonanConfigKeys.WORKER_EXCEPTION_HANDLING)?.toString(),
            lazyIrForCaches = get(KonanConfigKeys.LAZY_IR_FOR_CACHES),
            partialLinkage = get(KonanConfigKeys.PARTIAL_LINKAGE),
            omitFrameworkBinary = get(KonanConfigKeys.OMIT_FRAMEWORK_BINARY),
        )

    private fun CompilerConfiguration.getBinaryConfig(): CompilerConfigurationDump.BinaryConfig =
        CompilerConfigurationDump.BinaryConfig(
            runtimeAssertionsMode = get(BinaryOptions.runtimeAssertionsMode)?.toString(),
            memoryModel = get(BinaryOptions.memoryModel)?.toString(),
            freezing = get(BinaryOptions.freezing)?.toString(),
            stripDebugInfoFromNativeLibs = get(BinaryOptions.stripDebugInfoFromNativeLibs),
            sourceInfoType = get(BinaryOptions.sourceInfoType)?.toString(),
            androidProgramType = get(BinaryOptions.androidProgramType)?.toString(),
            unitSuspendFunctionObjCExport = get(BinaryOptions.unitSuspendFunctionObjCExport)?.toString(),
            objcExportSuspendFunctionLaunchThreadRestriction = get(BinaryOptions.objcExportSuspendFunctionLaunchThreadRestriction)?.toString(),
            gcSchedulerType = get(BinaryOptions.gcSchedulerType)?.toString(),
            gcMarkSingleThreaded = get(BinaryOptions.gcMarkSingleThreaded),
            linkRuntime = get(BinaryOptions.linkRuntime)?.toString(),
            bundleId = get(BinaryOptions.bundleId),
            bundleShortVersionString = get(BinaryOptions.bundleShortVersionString),
            bundleVersion = get(BinaryOptions.bundleVersion),
            appStateTracking = get(BinaryOptions.appStateTracking)?.toString(),
            sanitizer = get(BinaryOptions.sanitizer)?.toString(),
            mimallocUseDefaultOptions = get(BinaryOptions.mimallocUseDefaultOptions),
        )

    private fun getMavenLibraries(): List<CompilerConfigurationDump.Library> {
        val exportedArtifactsPaths = config.resolve.exportedLibraries.map { it.libraryFile.absolutePath }.toSet()

        return config.userVisibleIrModulesSupport
            .reflectedBy<UserVisibleIrModulesSupportReflector>()
            .externalDependencyModules
            .map { it.toLibrary(exportedArtifactsPaths) }
    }

    private fun getCacheSupport(): CompilerConfigurationDump.CacheSupport =
        CompilerConfigurationDump.CacheSupport(
            hasStaticCaches = config.cacheSupport.cachedLibraries.hasStaticCaches,
            hasDynamicCaches = config.cacheSupport.cachedLibraries.hasDynamicCaches,
        )
}

private fun ResolvedDependency.toLibrary(exportedArtifactsPaths: Set<String>): CompilerConfigurationDump.Library =
    CompilerConfigurationDump.Library(
        uniqueNames = id.uniqueNames.toList(),
        version = selectedVersion.version,
        isExported = artifactPaths.any { it.path in exportedArtifactsPaths },
    )
