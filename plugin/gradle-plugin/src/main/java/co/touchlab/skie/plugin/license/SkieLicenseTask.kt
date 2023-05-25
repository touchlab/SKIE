package co.touchlab.skie.plugin.license

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path

internal abstract class SkieLicenseTask : DefaultTask() {

    @get:OutputFile
    abstract val outputLicensePath: Provider<Path>

    abstract val license: Provider<SkieLicense>

    init {
    }

    @TaskAction
    fun setupLicense() {
    }
}
