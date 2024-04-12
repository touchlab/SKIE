package co.touchlab.skie.test.annotation.filter

import co.touchlab.skie.test.runner.SkieTestMatrixSource
import org.junit.jupiter.api.extension.ExtensionContext

interface MatrixFilter {
    fun apply(context: ExtensionContext, source: SkieTestMatrixSource)
}
