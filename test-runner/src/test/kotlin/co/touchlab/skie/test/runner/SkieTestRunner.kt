package co.touchlab.skie.test.runner

import co.touchlab.skie.test.annotation.MatrixTest
import co.touchlab.skie.test.annotation.filter.FilterMatrixWith
import io.kotest.mpp.newInstanceNoArgConstructorOrObjectInstance
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider
import org.junit.platform.commons.util.AnnotationUtils
import java.util.stream.Stream

class SkieTestRunner: TestTemplateInvocationContextProvider {
    override fun supportsTestTemplate(context: ExtensionContext): Boolean {
        return AnnotationUtils.isAnnotated(context.testMethod, MatrixTest::class.java)
    }

    override fun provideTestTemplateInvocationContexts(context: ExtensionContext): Stream<TestTemplateInvocationContext> {
        val testMethod = context.requiredTestMethod
        val parentClassFilterAnnotations = AnnotationUtils.findRepeatableAnnotations(
            context.requiredTestClass,
            FilterMatrixWith::class.java
        )
        val methodFilterAnnotations = AnnotationUtils.findRepeatableAnnotations(
            testMethod,
            FilterMatrixWith::class.java
        )
        val uniqueMatrixFilterClasses = (parentClassFilterAnnotations + methodFilterAnnotations).map {
            it.value
        }.toSet()

        val uniqueMatrixFilters = uniqueMatrixFilterClasses.map {
            it.newInstanceNoArgConstructorOrObjectInstance()
        }

        val matrixSource = SkieTestRunnerConfiguration.buildMatrixSource()
        uniqueMatrixFilters.forEach { filter ->
            filter.apply(context, matrixSource)
        }
        val filteredAxes = SkieTestRunnerConfiguration.buildMatrixAxes(matrixSource)

        val matrixAxes = testMethod.parameterTypes.mapNotNull { requestedAxisType ->
            filteredAxes[requestedAxisType]
        }

        val matrix = SkieTestMatrix(axes = matrixAxes)

        return matrix.mapCells {
            // Oh it's needed, otherwise it's all red-underscored.
            @Suppress("USELESS_CAST")
            SkieTestMatrixContext(it) as TestTemplateInvocationContext
        }.stream()
    }
}
