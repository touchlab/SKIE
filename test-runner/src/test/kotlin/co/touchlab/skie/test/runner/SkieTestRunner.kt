package co.touchlab.skie.test.runner

import co.touchlab.skie.test.annotation.MatrixTest
import co.touchlab.skie.test.annotation.filter.OnlyFor
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
        val parentClassFilterAnnotations =
            AnnotationUtils.findRepeatableAnnotations(context.requiredTestClass, OnlyFor::class.java)
        val methodFilterAnnotations = AnnotationUtils.findRepeatableAnnotations(testMethod, OnlyFor::class.java)
        val matrixFilter = (parentClassFilterAnnotations + methodFilterAnnotations).fold(MatrixFilter.empty) { acc, onlyFor ->
            acc.apply(onlyFor)
        }
        val filteredAxes = SkieTestRunnerConfiguration.filteredMatrixAxes(matrixFilter)

        val matrixAxes = testMethod.parameterTypes.mapNotNull { requestedAxisType ->
            filteredAxes[requestedAxisType]
        }

        val matrix = SkieTestMatrix(axes = matrixAxes)

        return matrix.mapCells {
            SkieTestMatrixContext(it) as TestTemplateInvocationContext
        }.stream()
    }
}
