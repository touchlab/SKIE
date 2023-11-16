@file:Suppress("EnumEntryName")

package co.touchlab.skie.configuration

enum class SkieConfigurationFlag {

    Feature_CoroutinesInterop,
    Feature_DefaultArgumentsInExternalLibraries,

    Build_ParallelSwiftCompilation,

    Migration_WildcardExport,

    Debug_DumpSwiftApiBeforeApiNotes,
    Debug_DumpSwiftApiAfterApiNotes,
    Debug_PrintSkiePerformanceLogs,
    Debug_CrashOnSoftErrors,
    Debug_LoadAllPlatformApiNotes,
    Debug_GenerateFileForEachExportedClass,

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
