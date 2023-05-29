package co.touchlab.skie.plugin.license

import org.eclipse.jgit.api.Git
import co.touchlab.skie.plugin.analytics.GradleAnalyticsManager
import co.touchlab.skie.plugin.util.registerSkieLinkBasedTask
import co.touchlab.skie.plugin.util.skieDirectories
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import co.touchlab.skie.plugin.analytics.GradleAnalyticsProducer
import co.touchlab.skie.gradle_plugin.BuildConfig
import co.touchlab.skie.plugin.license.util.getHashedPlatformUUID
import co.touchlab.skie.plugin.util.registerSkieTask
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import co.touchlab.skie.util.hashed
import java.io.File

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
            hashedGitRemotes = getHashedGitRemotes(),
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
        }

        linkTask.dependsOn(licenseTask)
    }

    private fun getHashedGitRemotes(): List<String> {
        val directoryWithGit = project.projectDir.findGitRoot() ?: return emptyList()

        val git = Git.open(directoryWithGit)

        return git.remoteList().call()
            .flatMap { it.urIs }
            .filter { it.isRemote }
            .map { it.host + "/" + it.path }
            .map { it.hashed() }
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

private tailrec fun File.findGitRoot(): File? =
    if (resolve(".git").exists()) this else parentFile?.findGitRoot()
