package co.touchlab.skie.test.util

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.util.AnnotationUtils

inline fun <reified A: Annotation> isTestMethodOrClassAnnotated(context: ExtensionContext): Boolean {
    return AnnotationUtils.isAnnotated(context.requiredTestMethod, A::class.java) ||
            AnnotationUtils.isAnnotated(context.requiredTestClass, A::class.java)
}
