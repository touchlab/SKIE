package com.ncorti.kotlin.gradle.template.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "templateExampleConfig"
const val TASK_NAME = "templateExample"

abstract class TemplatePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Add the 'template' extension object
        val extension = project.extensions.create(EXTENSION_NAME, TemplateExtension::class.java, project)

        // Add a task that uses configuration from the extension object
        project.tasks.register(TASK_NAME, TemplateExampleTask::class.java) {
            it.tag.set(extension.tag)
            it.message.set(extension.message)
            it.outputFile.set(extension.outputFile)
        }
    }
}
