package co.touchlab.skie.plugin.analytics

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

internal abstract class SkieUploadAnalyticsTask : DefaultTask() {

    @get:InputDirectory
    abstract val analyticsDirectory: Property<File>

    init {
        doNotTrackState("Must always run after link task.")
    }

    @TaskAction
    fun runTask() {
        // WIP
        // + disable based on extension
    }
}
