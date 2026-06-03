@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.jvm.compiler.report
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal val frameworkOutputPathConfigurationKey: CompilerConfigurationKey<String>
    get() = KonanConfigKeys.OUTPUT

internal fun CompilerConfiguration.reportCompilerMessage(
    severity: CompilerMessageSeverity,
    message: String,
    location: CompilerMessageLocation?,
) {
    report(severity, message, location)
}
