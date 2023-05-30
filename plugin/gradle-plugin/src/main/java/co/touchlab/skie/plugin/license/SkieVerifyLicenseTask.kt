package co.touchlab.skie.plugin.license

import co.touchlab.skie.plugin.license.util.SkieLicenseCleaner
import co.touchlab.skie.plugin.util.BaseSkieTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import java.io.File

internal abstract class SkieVerifyLicenseTask : BaseSkieTask() {

    @get:Input
    abstract val licenseRequestData: Property<SkieLicense.RequestData>

    @get:OutputFile
    abstract val outputLicenseFile: Property<File>

    init {
        doNotTrackState("License must be verified before each link task.")
    }

    override fun runTask() {
        val licenseRequestData = licenseRequestData.get()

        copyLicenseFile(licenseRequestData)
        renewLicenseInBackground(licenseRequestData)
        deleteOldLicensesInBackground()
    }

    private fun copyLicenseFile(licenseRequestData: SkieLicense.RequestData) {
        val licenseFile = SkieLicenseProvider.getValidLicenseLocationOrRequestNew(licenseRequestData).toFile()

        val licenseContent = licenseFile.readBytes()

        val outputLicenseFile = outputLicenseFile.get()

        outputLicenseFile.writeBytes(licenseContent)
    }

    // WIP Test if this doesn't delay the task anyway
    private fun renewLicenseInBackground(licenseRequestData: SkieLicense.RequestData) {
        Thread {
            SkieLicenseProvider.tryToRenewLicense(licenseRequestData)
        }.start()
    }

    private fun deleteOldLicensesInBackground() {
        Thread {
            SkieLicenseCleaner.deleteObsoleteLicenses()
        }.start()
    }
}
