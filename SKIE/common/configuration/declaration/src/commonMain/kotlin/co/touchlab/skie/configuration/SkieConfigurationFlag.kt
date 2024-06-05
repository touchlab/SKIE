@file:Suppress("EnumEntryName")

package co.touchlab.skie.configuration

enum class SkieConfigurationFlag {

    Feature_CoroutinesInterop,
    Feature_DefaultArgumentsInExternalLibraries,

    Feature_FlowCombineConvertor,
    Feature_FutureCombineExtension,
    Feature_SwiftUIObserving,

    Build_SwiftLibraryEvolution,
    Build_ParallelSwiftCompilation,
    Build_ParallelSkieCompilation,
    Build_ConcurrentSkieCompilation,
    Build_NoClangModuleBreadcrumbsInStaticFramework,

    Migration_WildcardExport,
    Migration_AnyMethodsAsFunctions,

    Debug_VerifyDescriptorProviderConsistency,
    Debug_DumpSwiftApiBeforeApiNotes,
    Debug_DumpSwiftApiAfterApiNotes,
    Debug_PrintSkiePerformanceLogs,
    Debug_CrashOnSoftErrors,
    Debug_LoadAllPlatformApiNotes,
    Debug_GenerateFileForEachExportedClass,
    Debug_UseStableTypeAliases,

    Analytics_GradlePerformance,
    Analytics_GradleEnvironment,
    Analytics_Git,
    Analytics_Hardware,
    Analytics_CompilerEnvironment,
    Analytics_SkiePerformance,
    Analytics_Project,
    Analytics_SkieConfiguration,
    Analytics_CompilerConfiguration,
    Analytics_Modules,
}
