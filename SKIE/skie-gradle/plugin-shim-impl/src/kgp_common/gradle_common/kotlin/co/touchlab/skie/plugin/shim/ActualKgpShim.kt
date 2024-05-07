package co.touchlab.skie.plugin.shim

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import org.jetbrains.kotlin.konan.target.Distribution
import org.jetbrains.kotlin.konan.target.HostManager
import java.io.File
import java.util.Properties

// Constructed through reflection in SKIE Gradle Plugin.
@Suppress("unused")
class ActualKgpShim(
    private val project: Project,
) : KgpShim {

    override val launchScheduler = LaunchScheduler()

    override val hostIsMac: Boolean = HostManager.hostIsMac

    override fun getDistributionProperties(konanHome: String, propertyOverrides: Map<String, String>?): Properties =
        Distribution(konanHome = konanHome, propertyOverrides = propertyOverrides).properties

    override fun getKonanHome(): File =
        NativeCompilerDownloader(project).compilerDirectory

    override fun getKotlinPluginVersion(): String =
        project.getKotlinPluginVersion()
}
