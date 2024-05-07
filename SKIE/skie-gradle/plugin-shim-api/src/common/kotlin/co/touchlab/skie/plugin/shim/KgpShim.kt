package co.touchlab.skie.plugin.shim

import java.io.File
import java.util.Properties

interface KgpShim {

    val launchScheduler: LaunchScheduler

    val hostIsMac: Boolean

    fun getDistributionProperties(
        konanHome: String,
        propertyOverrides: Map<String, String>?,
    ): Properties

    fun getKonanHome(): File

    fun getKotlinPluginVersion(): String
}
