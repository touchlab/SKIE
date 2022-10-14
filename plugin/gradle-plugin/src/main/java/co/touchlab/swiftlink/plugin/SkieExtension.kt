package co.touchlab.swiftlink.plugin

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

const val DEFAULT_OUTPUT_DIR = "skie"

@Suppress("UnnecessaryAbstractClass")
abstract class SkieExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    val isWildcardExportPrevented: Property<Boolean> = objects.property<Boolean>().convention(true)
}
