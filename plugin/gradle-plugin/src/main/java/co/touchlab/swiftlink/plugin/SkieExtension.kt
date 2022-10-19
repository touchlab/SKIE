package co.touchlab.swiftlink.plugin

import co.touchlab.swiftgen.configuration.Configuration
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.kotlin.dsl.property
import co.touchlab.swiftgen.configuration.builder.ConfigurationBuilder
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
