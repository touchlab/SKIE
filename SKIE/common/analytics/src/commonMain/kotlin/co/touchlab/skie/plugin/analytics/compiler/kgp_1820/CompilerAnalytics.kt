package co.touchlab.skie.plugin.analytics.compiler.kgp_1820

import kotlinx.serialization.Serializable

@Serializable
data class CompilerAnalytics(
    val compilerVersion: String?,
    val languageVersion: String,
    val xcodeVersion: String,
    val properties: Map<String, String>,
    val host: String,
    val target: String,
    val commonConfig: CommonConfig,
    val konanConfig: KonanConfig,
    val binaryConfig: BinaryConfig,
    val mavenLibraries: List<Library>?,
    val cacheSupport: CacheSupport,
) {

    @Serializable
    data class CommonConfig(
        val disableInline: Boolean?,
        val moduleName: String?,
        val reportOutputFiles: Boolean?,
        val metadataVersion: String?,
        val useFir: Boolean?,
        val useLightTree: Boolean?,
        val expectActualLinker: Boolean?,
        val useFirExtendedCheckers: Boolean?,
        val parallelBackendThreads: Int?,
        val klibNormalizeAbsolutePath: Boolean?,
        val produceKlibSignaturesClashChecks: Boolean?,
        val incrementalCompilation: Boolean?,
        val allowAnyScriptsInSourceRoots: Boolean?,
    )

    @Serializable
    data class KonanConfig(
        val bundleId: String?,
        val checkDependencies: Boolean?,
        val debug: Boolean?,
        val fakeOverrideValidator: Boolean?,
        val bitcodeEmbeddingMode: String?,
        val enableAssertions: Boolean?,
        val entry: String?,
        val makePerFileCache: Boolean?,
        val frameworkImportHeaders: List<String>?,
        val generateTestRunner: String?,
        val lightDebug: Boolean?,
        val generateDebugTrampoline: Boolean?,
        val linkerArgs: List<String>?,
        val listTargets: Boolean?,
        val metadataKlib: Boolean?,
        val moduleName: String?,
        val noDefaultLibs: Boolean?,
        val noEndorsedLibs: Boolean?,
        val noMain: Boolean?,
        val noStdlib: Boolean?,
        val noPack: Boolean?,
        val optimization: Boolean?,
        val overrideClangOptions: List<String>?,
        val allocationMode: String?,
        val exportKdoc: Boolean?,
        val printBitcode: Boolean?,
        val checkExternalCalls: Boolean?,
        val printIr: Boolean?,
        val printFiles: Boolean?,
        val purgeUserLibs: Boolean?,
        val shortModuleName: String?,
        val staticFramework: Boolean?,
        val target: String?,
        val verifyBitcode: Boolean?,
        val verifyIr: Boolean?,
        val verifyCompiler: Boolean?,
        val debugInfoVersion: Int?,
        val coverage: Boolean?,
        val objcGenerics: Boolean?,
        val preLinkCaches: Boolean?,
        val overrideKonanProperties: Map<String, String>?,
        val destroyRuntimeMode: String?,
        val garbageCollector: String?,
        val propertyLazyInitialization: Boolean?,
        val workerExceptionHandling: String?,
        val lazyIrForCaches: Boolean?,
        val partialLinkage: Boolean?,
        val omitFrameworkBinary: Boolean?,
    )

    @Serializable
    data class BinaryConfig(
        val runtimeAssertionsMode: String?,
        val memoryModel: String?,
        val freezing: String?,
        val stripDebugInfoFromNativeLibs: Boolean?,
        val sourceInfoType: String?,
        val androidProgramType: String?,
        val unitSuspendFunctionObjCExport: String?,
        val objcExportSuspendFunctionLaunchThreadRestriction: String?,
        val objcExportDisableSwiftMemberNameMangling: Boolean?,
        val gcSchedulerType: String?,
        val gcMarkSingleThreaded: Boolean?,
        val linkRuntime: String?,
        val bundleId: String?,
        val bundleShortVersionString: String?,
        val bundleVersion: String?,
        val appStateTracking: String?,
        val sanitizer: String?,
        val mimallocUseDefaultOptions: Boolean?,
        val mimallocUseCompaction: Boolean?,
        val preview19LLVMPipeline: Boolean?,
    )

    @Serializable
    data class Library(
        val uniqueNames: List<String>,
        val version: String,
        val isExported: Boolean,
    )

    @Serializable
    data class CacheSupport(
        val hasStaticCaches: Boolean,
        val hasDynamicCaches: Boolean,
    )
}
