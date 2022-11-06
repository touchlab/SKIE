package co.touchlab.skie.plugin.generator.internal.datastruct

import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.Renderers

object DataStructErrorMessages: DefaultErrorMessages.Extension {
    private val MAP = DiagnosticFactoryToRendererMap()

    init {
        MAP.put(
            DataStructErrors.DATA_STRUCT_NOT_DATA_CLASS,
            "{0} is annotated with @DataStruct but isn't a data class!",
            Renderers.CAPITALIZED_DECLARATION_NAME_WITH_KIND_AND_PLATFORM,
        )
        MAP.put(
            DataStructErrors.UNSUPPORTED_TYPE,
            "Type {0} of parameter {1} is not supported by @DataStruct.",
            Renderers.RENDER_TYPE,
            Renderers.NAME,
        )
    }

    override fun getMap(): DiagnosticFactoryToRendererMap = MAP
}
