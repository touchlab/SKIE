@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model.type.translation.impl

import co.touchlab.skie.api.model.type.translation.SwiftTranslationProblemCollector
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.reportCompilationWarning
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.descriptors.DeclarationDescriptorWithSource
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.source.getPsi

class CommonBackendContextSwiftTranslationProblemCollector(
    val context: CommonBackendContext,
) : SwiftTranslationProblemCollector {

    override fun reportWarning(text: String) {
        context.reportCompilationWarning(text)
    }

    override fun reportWarning(method: FunctionDescriptor, text: String) {
        val psi = (method as? DeclarationDescriptorWithSource)?.source?.getPsi()
            ?: return reportWarning(
                "$text\n    (at ${DescriptorRenderer.COMPACT_WITH_SHORT_TYPES.render(method)})"
            )

        val location = MessageUtil.psiElementToMessageLocation(psi)

        context.configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)
            .report(CompilerMessageSeverity.WARNING, text, location)
    }

    override fun reportException(throwable: Throwable) {
        throw throwable
    }
}
