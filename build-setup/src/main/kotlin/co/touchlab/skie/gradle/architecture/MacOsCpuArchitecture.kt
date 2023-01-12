package co.touchlab.skie.gradle.architecture

import org.codehaus.groovy.runtime.ProcessGroovyMethods

enum class MacOsCpuArchitecture(val systemName: String, val kotlinGradleName: String, val konanTarget: String) {
    Arm64("arm64", "macosArm64", "macos_arm64"),
    X64("x86_64", "macosX64", "macos_x64");

    companion object {

        fun getCurrent(): MacOsCpuArchitecture {
            val systemName = "uname -m".let(ProcessGroovyMethods::execute).let(ProcessGroovyMethods::getText).trim()

            return values().firstOrNull { it.systemName == systemName } ?: error("Unsupported architecture: $systemName")
        }
    }
}
