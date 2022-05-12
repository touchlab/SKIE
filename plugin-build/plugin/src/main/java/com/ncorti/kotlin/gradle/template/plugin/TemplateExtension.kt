package com.ncorti.kotlin.gradle.template.plugin

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

const val DEFAULT_OUTPUT_FILE = "template-example.txt"

@Suppress("UnnecessaryAbstractClass")
abstract class TemplateExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    // Example of a property that is mandatory. The task will
    // fail if this property is not set as is annotated with @Optional.
    val message: Property<String> = objects.property(String::class.java)

    // Example of a property that is optional.
    val tag: Property<String> = objects.property(String::class.java)

    // Example of a property with a default set with .convention
    val outputFile: RegularFileProperty = objects.fileProperty().convention(
        project.layout.buildDirectory.file(DEFAULT_OUTPUT_FILE)
    )
}
