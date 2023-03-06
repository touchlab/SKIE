package co.touchlab.skie.plugin.api.model.type

enum class FlowMappingStrategy {
    Full, GenericsOnly, None;

    fun forGenerics(): FlowMappingStrategy =
        when (this) {
            Full, GenericsOnly -> Full
            None -> None
        }
}
