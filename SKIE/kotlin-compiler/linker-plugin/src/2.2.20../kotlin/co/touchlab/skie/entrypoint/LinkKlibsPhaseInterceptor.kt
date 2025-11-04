@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
import co.touchlab.skie.compilerinject.interceptor.PhaseInterceptor
import co.touchlab.skie.context.KotlinIrPhaseContext
import org.jetbrains.kotlin.backend.konan.LinkKlibsContext
import org.jetbrains.kotlin.backend.konan.LinkKlibsInput
import org.jetbrains.kotlin.backend.konan.LinkKlibsOutput
import org.jetbrains.kotlin.backend.konan.driver.phases.LinkKlibsPhase
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.psi2ir.generators.GeneratorExtensions
import org.jetbrains.kotlin.psi2ir.generators.TypeTranslatorImpl

internal class LinkKlibsPhaseInterceptor : PhaseInterceptor<LinkKlibsContext, LinkKlibsInput, LinkKlibsOutput> {

    override fun getInterceptedPhase(): Any = LinkKlibsPhase

    override fun intercept(
        context: LinkKlibsContext,
        input: LinkKlibsInput,
        next: (LinkKlibsContext, LinkKlibsInput) -> LinkKlibsOutput,
    ): LinkKlibsOutput {
        val phaseOutput = next(context, input)

        val mainSkieContext = context.config.configuration.mainSkieContext

        val symbolTable = phaseOutput.symbolTable as SymbolTable
        val languageVersionSettings = context.config.configuration.languageVersionSettings

        val typeTranslator = TypeTranslatorImpl(
            symbolTable = symbolTable,
            languageVersionSettings = languageVersionSettings,
            moduleDescriptor = input.moduleDescriptor,
            extensions = GeneratorExtensions(),
            allowErrorTypeInAnnotations = false,
        )

        EntrypointUtils.runKotlinIrPhases(
            mainSkieContext = mainSkieContext,
            moduleFragment = phaseOutput.irModule,
            pluginContext = KotlinIrPhaseContext.CompatibleIrPluginContext(
                symbolTable = symbolTable,
                irBuiltIns = phaseOutput.irBuiltIns,
                bindingContext = context.bindingContext,
                linker = phaseOutput.irLinker,
                languageVersionSettings = languageVersionSettings,
                typeTranslator = typeTranslator,
            ),
        )

        return phaseOutput
    }
}
