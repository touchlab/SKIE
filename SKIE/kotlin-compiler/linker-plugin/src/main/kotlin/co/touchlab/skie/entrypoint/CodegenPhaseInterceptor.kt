@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
import co.touchlab.skie.compilerinject.interceptor.PhaseInterceptor
import co.touchlab.skie.configuration.SkieConfigurationFlag
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.driver.phases.CodegenInput
import org.jetbrains.kotlin.backend.konan.driver.phases.CodegenPhase
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity

internal class CodegenPhaseInterceptor : PhaseInterceptor<NativeGenerationState, CodegenInput, Unit> {
    override fun getInterceptedPhase(): Any = CodegenPhase

    override fun intercept(context: NativeGenerationState, input: CodegenInput, next: (NativeGenerationState, CodegenInput) -> Unit) {
        val mainSkieContext = context.config.configuration.mainSkieContext
        with(mainSkieContext) {
            if (SkieConfigurationFlag.Build_RelativeSourcePathsInDebugSymbols.isEnabled) {
                workaroundRelativeDebugPrefixMapBug(context)
            }
        }

        next(context, input)
    }

    private fun workaroundRelativeDebugPrefixMapBug(context: NativeGenerationState) {
        if (context.hasDebugInfo()) {
            context.context.messageCollector.report(
                severity = CompilerMessageSeverity.ERROR,
                message = "NativeGenerationState.debugInfo was initialized before debug-prefix-map workaround was applied! " +
                    "Please disable the debug-prefix-map SKIE feature and report this issue to the SKIE GitHub at https://github.com/touchlab/SKIE",
            )
        } else {
            /*
             * This piece of code removes
             */
            val existingMap = context.config.configuration.getMap(KonanConfigKeys.Companion.DEBUG_PREFIX_MAP)
            context.config.configuration.put(KonanConfigKeys.Companion.DEBUG_PREFIX_MAP, emptyMap())

            // Touch the `debugInfo` to create it while the `DEBUG_PREFIX_MAP` is empty.
            @Suppress("UNUSED_VARIABLE")
            val debugInfo = context.debugInfo

            // Set the `DEBUG_PREFIX_MAP` back to original value so the .kt source files can get remapped correctly.
            context.config.configuration.put(KonanConfigKeys.Companion.DEBUG_PREFIX_MAP, existingMap)
        }
    }
}
