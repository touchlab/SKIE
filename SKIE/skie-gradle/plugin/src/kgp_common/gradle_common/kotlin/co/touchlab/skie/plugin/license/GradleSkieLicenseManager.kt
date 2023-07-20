package co.touchlab.skie.plugin.license

import org.gradle.api.Project

internal class GradleSkieLicenseManager(private val project: Project) {

    private val licenseKey: String
        get() = (project.findProperty("touchlab.key") as? String)
            ?: error("SKIE license key was not found. Please add `touchlab.key=YOUR_KEY` to gradle.properties file.")

    fun initializeLicensing() {
        if (!licenseKey.isValidLicenseKey) {
            throw IllegalStateException("Invalid SKIE license key: $licenseKey")
        }
    }

    private val String.isValidLicenseKey: Boolean
        get() = length == 26 && all { it.isDigit() || it.isUpperCase() }
}
