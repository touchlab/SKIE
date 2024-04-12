package co.touchlab.skie.test.runner

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

class SkieMatrixExtension(
    private val runValues: Map<Class<*>, Any>,
): ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return runValues.containsKey(parameterContext.parameter.type)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return checkNotNull(runValues[parameterContext.parameter.type]) {
            "Value for type ${parameterContext.parameter.type} not available! Check if `true` was returned for it in `supportsParameter`."
        }
    }
}
