@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.config.MessageCollectorAccess
import org.jetbrains.kotlin.config.messageCollector

internal val frameworkOutputPathConfigurationKey: CompilerConfigurationKey<String>
    get() = KonanConfigKeys.KONAN_OUTPUT_PATH

@OptIn(MessageCollectorAccess::class)
internal fun CompilerConfiguration.reportCompilerMessage(
    severity: CompilerMessageSeverity,
    message: String,
    location: CompilerMessageLocation?,
) {
    messageCollector.report(severity, message, location)
}
