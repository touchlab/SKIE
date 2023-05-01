package co.touchlab.skie.plugin

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.debug.DebugInfoDirectory
import co.touchlab.skie.plugin.api.debug.DumpSwiftApiPoint
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import java.io.File

object ConfigurationKeys {

    val buildId = CompilerConfigurationKey<String>("SKIE build ID")
    val jwtWithLicense = CompilerConfigurationKey<String>("JWT with SKIE license")
    val disableWildcardExport = CompilerConfigurationKey<Boolean>("disable wildcard export")
    val skieConfiguration = CompilerConfigurationKey<Configuration>("SKIE configuration")
    val analyticsDir = CompilerConfigurationKey<File>("Directory with SKIE analytics")

    object SwiftCompiler {
        val sourceFiles = CompilerConfigurationKey<List<File>>("Swift source files")
        val generatedDir = CompilerConfigurationKey<File>("generated Swift directory")
        val swiftVersion = CompilerConfigurationKey<String>("Swift version")
        val parallelCompilation = CompilerConfigurationKey<Boolean>("Is Swift parallel compilation enabled")
        val additionalFlags = CompilerConfigurationKey<List<String>>("Additional Swift compiler flags")
    }

    object Debug {
        val infoDirectory = CompilerConfigurationKey<DebugInfoDirectory>("Directory with logs and other debug outputs")
        val dumpSwiftApiPoints = CompilerConfigurationKey<Set<DumpSwiftApiPoint>>("Points where to dump Swift API")
    }
}
