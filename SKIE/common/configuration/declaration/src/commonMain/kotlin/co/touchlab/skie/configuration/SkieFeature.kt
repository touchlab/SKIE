@file:Suppress("EnumEntryName")

package co.touchlab.skie.configuration

enum class SkieFeature {

    CoroutinesInterop,
    FqNames,
    ParallelSwiftCompilation,
    WildcardExport,

    Debug_DumpSwiftApiBeforeApiNotes,
    Debug_DumpSwiftApiAfterApiNotes,
    Debug_PrintSkiePerformanceLogs,

    Analytics_Identifying_Project,
    Analytics_Tracking_Project,
    Analytics_Anonymous_GradlePerformance,
    Analytics_Anonymous_GradleEnvironment,
    Analytics_Identifying_Git,
    Analytics_Anonymous_Git,
    Analytics_Tracking_Hardware,
    Analytics_Anonymous_Hardware,

    Analytics_CompilerEnvironment,
    Analytics_SkiePerformance,
    Analytics_SkieConfiguration,
    Analytics_Compiler,
}
