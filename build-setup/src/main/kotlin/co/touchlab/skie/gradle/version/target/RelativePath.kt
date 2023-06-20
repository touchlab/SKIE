package co.touchlab.skie.gradle.version.target

import java.nio.file.Path

data class RelativePath(val components: List<String>) {
    fun toPath(base: Path): Path {
        return components.fold(base) { path, component ->
            path.resolve(component)
        }
    }
}
