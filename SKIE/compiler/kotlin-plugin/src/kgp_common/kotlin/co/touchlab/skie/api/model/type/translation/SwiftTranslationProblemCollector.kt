@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model.type.translation

import org.jetbrains.kotlin.descriptors.FunctionDescriptor

interface SwiftTranslationProblemCollector {

    fun reportWarning(text: String)
    fun reportWarning(method: FunctionDescriptor, text: String)
    fun reportException(throwable: Throwable)

    object SILENT : SwiftTranslationProblemCollector {

        override fun reportWarning(text: String) {}
        override fun reportWarning(method: FunctionDescriptor, text: String) {}
        override fun reportException(throwable: Throwable) {}
    }

}
