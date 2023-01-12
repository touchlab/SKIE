package co.touchlab.skie.plugin.api.model.parameter

interface KotlinParameterSwiftModel {

    val original: KotlinParameterSwiftModel

    val isChanged: Boolean

    val argumentLabel: String
}
