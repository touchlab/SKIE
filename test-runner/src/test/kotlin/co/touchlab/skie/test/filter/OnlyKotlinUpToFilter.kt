package co.touchlab.skie.test.filter

import co.touchlab.skie.test.annotation.filter.MatrixFilter
import co.touchlab.skie.test.annotation.filter.OnlyKotlinUpTo
import co.touchlab.skie.test.runner.SkieTestMatrixSource
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.util.AnnotationUtils

object OnlyKotlinUpToFilter : MatrixFilter {

    override fun apply(context: ExtensionContext, source: SkieTestMatrixSource) {
        val parentClassFilterAnnotations = AnnotationUtils.findRepeatableAnnotations(
            context.requiredTestClass,
            OnlyKotlinUpTo::class.java,
        )
        val methodFilterAnnotations = AnnotationUtils.findRepeatableAnnotations(
            context.requiredTestMethod,
            OnlyKotlinUpTo::class.java,
        )

        (parentClassFilterAnnotations + methodFilterAnnotations).forEach { filter ->
            source.kotlinVersions.removeAll { version ->
                val components = version.value.split(".").map { component -> component.takeWhile { it.isDigit() } }
                val major = components.first().toInt()
                val minor = components.getOrNull(1)?.toInt() ?: 0
                val patch = components.getOrNull(2)?.toInt() ?: 0

                var result = false

                if (filter.major >= 0) {
                    result = result || major > filter.major
                }

                if (filter.minor >= 0) {
                    result = result || major == filter.major && minor > filter.minor
                }

                if (filter.patch >= 0) {
                    result = result || major == filter.major && minor == filter.minor && patch > filter.patch
                }

                result
            }
        }
    }
}
