package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.api.debug.DebugInfoDirectory
import co.touchlab.skie.plugin.api.debug.DumpSwiftApiPoint
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import java.io.File

object ConfigurationKeys {

    val swiftSourceFiles = CompilerConfigurationKey<List<File>>("Swift source files")
    val generatedSwiftDir = CompilerConfigurationKey<File>("generated Swift directory")
    val disableWildcardExport = CompilerConfigurationKey<Boolean>("disable wildcard export")

    object Debug {
        val infoDirectory = CompilerConfigurationKey<DebugInfoDirectory>("Directory with logs and other debug outputs")
        val dumpSwiftApiPoints = CompilerConfigurationKey<Set<DumpSwiftApiPoint>>("Points where to dump Swift API")
    }
}
