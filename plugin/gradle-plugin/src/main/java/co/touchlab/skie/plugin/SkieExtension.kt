package co.touchlab.skie.plugin

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.builder.ConfigurationBuilder
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass")
abstract class SkieExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    val isWildcardExportPrevented: Property<Boolean> = objects.property<Boolean>().convention(true)

    private val configurationBuilder = ConfigurationBuilder()

    fun configuration(builder: ConfigurationBuilder.() -> Unit) {
        configurationBuilder.apply(builder)
    }

    internal fun buildConfiguration(): Configuration =
        Configuration(configurationBuilder)
}
