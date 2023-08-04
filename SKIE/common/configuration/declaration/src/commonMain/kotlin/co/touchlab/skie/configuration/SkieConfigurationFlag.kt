@file:Suppress("EnumEntryName")

package co.touchlab.skie.configuration

enum class SkieConfigurationFlag {

    Feature_CoroutinesInterop,
    Feature_FqNames,

    Build_ParallelSwiftCompilation,

    Migration_WildcardExport,

    Debug_DumpSwiftApiBeforeApiNotes,
    Debug_DumpSwiftApiAfterApiNotes,
    Debug_PrintSkiePerformanceLogs,
    Debug_CrashOnSoftErrors,

    Analytics_Identifying_Project,
    Analytics_Anonymous_Project,
    Analytics_Anonymous_GradlePerformance,
    Analytics_Anonymous_GradleEnvironment,
    Analytics_Identifying_Git,
    Analytics_Anonymous_Git,
    Analytics_Anonymous_Hardware,
    Analytics_Anonymous_CompilerEnvironment,
    Analytics_Anonymous_SkiePerformance,
    Analytics_Identifying_SkieConfiguration,
    Analytics_Anonymous_SkieConfiguration,
    Analytics_Anonymous_CompilerConfiguration,
    Analytics_Identifying_CompilerConfiguration,
}
