package co.touchlab.skie.plugin.configuration

import org.gradle.api.Project

internal val Project.skieExtension: SkieExtension
    get() = project.extensions.getByType(SkieExtension::class.java)
