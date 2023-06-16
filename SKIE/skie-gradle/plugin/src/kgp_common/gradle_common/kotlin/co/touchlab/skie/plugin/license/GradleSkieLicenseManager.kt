package co.touchlab.skie.plugin.license

import co.touchlab.skie.gradle_plugin.BuildConfig
import co.touchlab.skie.plugin.analytics.GradleAnalyticsManager
import co.touchlab.skie.plugin.analytics.GradleAnalyticsProducer
import co.touchlab.skie.plugin.analytics.getGitRemotes
import co.touchlab.skie.plugin.directory.createSkieBuildDirectoryTask
import co.touchlab.skie.plugin.license.util.getHashedPlatformUUID
import co.touchlab.skie.plugin.util.registerSkieLinkBasedTask
import co.touchlab.skie.plugin.util.registerSkieTask
import co.touchlab.skie.plugin.directory.skieDirectories
import co.touchlab.skie.util.hashed
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

internal class GradleSkieLicenseManager(private val project: Project) {

    var licenseOrNull: SkieLicense? = null
        private set

    private lateinit var licenseRequestData: SkieLicense.RequestData

    private val licenseKey: SkieLicense.Key
        get() = (project.findProperty("touchlab.key") as? String)?.let { SkieLicense.Key(it) }
            ?: error("SKIE license key was not found. Please add `touchlab.key=YOUR_KEY` to gradle.properties file.")

    fun initializeLicensing(analyticsManager: GradleAnalyticsManager) {
        try {
            analyticsManager.withErrorLogging {
                loadLicense(analyticsManager)
            }
        } catch (e: Throwable) {
            project.logger.warn("Failed to load SKIE license. Reason: ${e.message}")
        }

        registerSkieResetLicenseTask(analyticsManager)

        renewLicenseInBackgroundIfExpiring()
    }

    private fun loadLicense(analyticsManager: GradleAnalyticsManager) {
        licenseRequestData = getLicenseRequestData(analyticsManager)

        val licensePath = SkieLicenseProvider.getValidLicenseLocationOrRequestNew(licenseRequestData)

        val license = SkieLicenseProvider.loadLicense(licensePath)

        license.printGradleMessages()

        licenseOrNull = license
    }

    private fun getLicenseRequestData(analyticsManager: GradleAnalyticsManager): SkieLicense.RequestData =
        SkieLicense.RequestData(
            buildId = analyticsManager.buildId,
            licenseKey = licenseKey,
            skieVersion = BuildConfig.KOTLIN_PLUGIN_VERSION,
            kotlinVersion = project.getKotlinPluginVersion(),
            isCI = GradleAnalyticsProducer.isCI,
            hashedRootProjectDiskLocation = GradleAnalyticsProducer.rootProjectDiskLocationHash(project),
            hashedPlatformUUID = getHashedPlatformUUID(),
            hashedGitRemotes = project.getGitRemotes().map { it.hashed() },
        )

    private fun registerSkieResetLicenseTask(analyticsManager: GradleAnalyticsManager) {
        project.registerSkieTask<SkieResetLicenseTask>("resetLicense", analyticsManager) {
            this.licenseRequestData.set(this@GradleSkieLicenseManager.licenseRequestData)
        }
    }

    private fun renewLicenseInBackgroundIfExpiring() {
        Thread {
            SkieLicenseProvider.tryToRenewLicenseIfExpiring(licenseRequestData)
        }.start()
    }

    fun configureLicensing(linkTask: KotlinNativeLink, analyticsManager: GradleAnalyticsManager) {
        if (!this::licenseRequestData.isInitialized) {
            error("License manager was not initialized")
        }

        val licenseTask = linkTask.registerSkieLinkBasedTask<SkieVerifyLicenseTask>("verifyLicense", analyticsManager) {
            this.licenseRequestData.set(this@GradleSkieLicenseManager.licenseRequestData)
            outputLicenseFile.set(linkTask.skieDirectories.buildDirectory.license)

            dependsOn(linkTask.createSkieBuildDirectoryTask)
        }

        linkTask.dependsOn(licenseTask)
    }

    private fun SkieLicense.printGradleMessages() {
        serverMessagesForGradleConfig.warnings.forEach {
            project.logger.warn(it)
        }
        serverMessagesForGradleConfig.info.forEach {
            project.logger.info(it)
        }
    }
}
