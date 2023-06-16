package co.touchlab.skie.plugin.license

import co.touchlab.skie.plugin.util.BaseSkieTask
import co.touchlab.skie.util.directory.SkieLicensesDirectory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.io.IOException

internal abstract class SkieResetLicenseTask : BaseSkieTask() {

    @get:Input
    abstract val licenseRequestData: Property<SkieLicense.RequestData>

    override fun runTask() {
        if (!SkieLicensesDirectory.directory.deleteRecursively()) {
            throw IOException("Failed to delete licenses directory.")
        }

        SkieLicenseProvider.renewLicense(licenseRequestData.get())
    }
}
