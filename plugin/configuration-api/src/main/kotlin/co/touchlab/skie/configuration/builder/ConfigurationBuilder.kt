package co.touchlab.skie.configuration.builder

import co.touchlab.skie.configuration.Configuration
import java.io.File
import java.nio.file.Path
import kotlin.io.path.readText

class ConfigurationBuilder {

    private val builders = mutableListOf<Builder>()

    fun group(targetFqNamePrefix: String = "", overridesAnnotations: Boolean = false, builder: ConfigurationGroupBuilder.() -> Unit) {
        val groupBuilder = ConfigurationGroupBuilder(targetFqNamePrefix, overridesAnnotations)

        builders.add(Builder.Group(groupBuilder))

        groupBuilder.builder()
    }

    fun from(path: Path) {
        builders.add(Builder.File(path))
    }

    fun from(paths: List<Path>) {
        val fileBuilders = paths.map { Builder.File(it) }

        builders.addAll(fileBuilders)
    }

    fun from(file: File) {
        from(file.toPath())
    }

    @JvmName("fromFiles")
    fun from(files: List<File>) {
        from(files.map { it.toPath() })
    }

    internal fun build(): Configuration =
        builders.map { it.build() }.fold(Configuration(emptyList()), Configuration::plus)

    private sealed interface Builder {

        fun build(): Configuration

        class Group(val groupBuilder: ConfigurationGroupBuilder) : Builder {

            override fun build(): Configuration =
                Configuration(listOf(groupBuilder.build()))
        }

        class File(val path: Path) : Builder {

            override fun build(): Configuration =
                Configuration.deserialize(path.readText())
        }
    }
}
