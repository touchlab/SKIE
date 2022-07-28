package co.touchlab.swiftkt.plugin

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

const val DEFAULT_OUTPUT_DIR = "swiftkt"

@Suppress("UnnecessaryAbstractClass")
abstract class SwiftKtExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    val isSwiftPackEnabled: Property<Boolean> = objects.property<Boolean>().convention(true)
}
