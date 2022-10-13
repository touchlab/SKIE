package co.touchlab.swiftpack.plugin

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import java.io.File

object SwiftPackConfigurationKeys {
    val outputDir = CompilerConfigurationKey<File>("outputDir")
}