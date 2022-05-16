package co.touchlab.swikt.plugin

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

const val DEFAULT_OUTPUT_DIR = "swikt"

@Suppress("UnnecessaryAbstractClass")
abstract class SwiktExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    val outputDir: DirectoryProperty = objects.directoryProperty().convention(
        project.layout.buildDirectory.dir(DEFAULT_OUTPUT_DIR)
    )
}
