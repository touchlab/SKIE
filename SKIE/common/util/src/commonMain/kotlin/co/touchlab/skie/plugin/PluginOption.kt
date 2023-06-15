package co.touchlab.skie.plugin

data class PluginOption<T>(
    val optionName: String,
    val valueDescription: String,
    val description: String,
    val isRequired: Boolean = false,
    val allowMultipleOccurrences: Boolean = false,
    val serialize: (T) -> String,
    val deserialize: (String) -> T,
)
