package co.touchlab.skie.test.filter

import co.touchlab.skie.test.annotation.filter.MatrixFilter
import co.touchlab.skie.test.runner.SkieTestMatrixSource
import org.junit.jupiter.api.extension.ExtensionContext

object OnlyConfigurationCacheMatrixFilter : MatrixFilter {
    override fun apply(context: ExtensionContext, source: SkieTestMatrixSource) {
        source.kotlinVersions.removeAll { kotlinVersion ->
            kotlinVersion.value.startsWith("1.8.") ||
                kotlinVersion.value.startsWith("1.9.0") ||
                kotlinVersion.value.startsWith("1.9.1")
        }
    }
}
