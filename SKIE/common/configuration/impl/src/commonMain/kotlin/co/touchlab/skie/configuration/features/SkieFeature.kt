@file:Suppress("EnumEntryName")

package co.touchlab.skie.configuration.features

enum class SkieFeature {

    CoroutinesInterop,
    FqNames,
    ParallelSwiftCompilation,
    WildcardExport,
    DumpSwiftApiBeforeApiNotes,
    DumpSwiftApiAfterApiNotes,


    Analytics_Compiler,
    Analytics_Gradle,
    Analytics_Hardware,
    Analytics_GradlePerformance,
    Analytics_SkiePerformance,
    Analytics_SkieConfiguration,
    Analytics_Sysctl,
    Analytics_OpenSource;
}
