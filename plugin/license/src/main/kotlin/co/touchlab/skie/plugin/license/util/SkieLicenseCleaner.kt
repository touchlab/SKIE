package co.touchlab.skie.plugin.license.util

import co.touchlab.skie.util.directory.SkieLicensesDirectory
import java.nio.file.Path
import java.time.temporal.ChronoUnit
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

object SkieLicenseCleaner {

    fun deleteObsoleteLicenses() {
        SkieLicensesDirectory.directory.toPath()
            .listDirectoryEntries()
            .forEach {
                deleteInvalidLicenses(it)
                deleteOlderLicenses(it)
            }
    }

    private fun deleteInvalidLicenses(directoryWithLicenses: Path) {
        directoryWithLicenses
            .getLicensePaths()
            .filter { !JwtParser.isValidJwt(it) }
            .forEach {
                it.deleteIfExists()
            }
    }

    private fun deleteOlderLicenses(directoryWithLicenses: Path) {
        val licenses = directoryWithLicenses
            .getLicensePaths()
            .mapNotNull { path ->
                JwtParser.tryParseJwt(path.readText())?.let { license -> LicenseWithPath(license, path) }
            }

        val newestLicenseIssuedAt = licenses.maxOf { it.license.issuedAt }

        val threshold = newestLicenseIssuedAt.minus(1, ChronoUnit.HOURS)

        licenses
            .filter { it.license.issuedAt.isBefore(threshold) }
            .forEach { it.path.deleteIfExists() }
    }
}
