package co.touchlab.skie.test.filter

import co.touchlab.skie.test.annotation.filter.MatrixFilter
import co.touchlab.skie.test.annotation.filter.OnlyFor
import co.touchlab.skie.test.runner.SkieTestMatrixSource
import co.touchlab.skie.test.util.KotlinVersion
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.util.AnnotationUtils

object OnlyForMatrixFilter: MatrixFilter {

    override fun apply(context: ExtensionContext, source: SkieTestMatrixSource) {
        val parentClassFilterAnnotations = AnnotationUtils.findRepeatableAnnotations(
            context.requiredTestClass,
            OnlyFor::class.java,
        )
        val methodFilterAnnotations = AnnotationUtils.findRepeatableAnnotations(
            context.requiredTestMethod,
            OnlyFor::class.java,
        )

        (parentClassFilterAnnotations + methodFilterAnnotations).forEach { filter ->
            filter.targets.map { it.target }.applyTo(source.targets)
            filter.linkModes.toList().applyTo(source.linkModes)
            filter.configurations.toList().applyTo(source.configurations)
            filter.kotlinVersions.map(::KotlinVersion).applyTo(source.kotlinVersions)
        }
    }

    private inline fun <reified T> Collection<T>.applyTo(source: MutableList<T>) {
        if (this.isNotEmpty()) {
            val thisSet = this.toSet()
            source.removeAll {
                it !in thisSet
            }
        }
    }
}
