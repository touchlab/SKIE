package co.touchlab.skie.plugin.shim

import org.gradle.api.Project
import java.io.File
import java.util.Properties

interface KgpShim {

    val launchScheduler: LaunchScheduler

    val hostIsMac: Boolean

    fun getDistributionProperties(
        konanHome: String,
        propertyOverrides: Map<String, String>?,
    ): Properties

    fun getKonanHome(project: Project): File
}
