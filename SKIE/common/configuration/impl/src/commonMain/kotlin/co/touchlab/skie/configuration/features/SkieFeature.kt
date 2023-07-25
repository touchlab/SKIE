@file:Suppress("EnumEntryName")

package co.touchlab.skie.configuration.features

enum class SkieFeature {

    CoroutinesInterop,
    FqNames,
    ParallelSwiftCompilation,
    WildcardExport,

    Debug_DumpSwiftApiBeforeApiNotes,
    Debug_DumpSwiftApiAfterApiNotes,
    Debug_PrintSkiePerformanceLogs,

    Analytics_Compiler,
    Analytics_Gradle,
    Analytics_Hardware,
    Analytics_GradlePerformance,
    Analytics_SkiePerformance,
    Analytics_SkieConfiguration,
    Analytics_Sysctl,
    Analytics_OpenSource;
}
