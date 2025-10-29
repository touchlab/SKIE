package co.touchlab.skie.buildsetup.util

import org.gradle.api.Project
import org.gradle.api.provider.Provider

enum class MacOsCpuArchitecture(val systemName: String, val kotlinGradleName: String, val konanTarget: String) {
    Arm64("arm64", "macosArm64", "macos_arm64"),
    X64("x86_64", "macosX64", "macos_x64");

    companion object {

        fun getCurrent(project: Project): Provider<MacOsCpuArchitecture> =
            project.providers
                .exec { commandLine("uname", "-m") }
                .standardOutput
                .asText
                .map { output ->
                    val systemName = output.trim()

                    entries.firstOrNull { it.systemName == systemName } ?: error("Unsupported architecture: $systemName")
                }
    }
}
