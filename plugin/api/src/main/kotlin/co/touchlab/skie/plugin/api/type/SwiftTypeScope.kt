package co.touchlab.skie.plugin.api.type

import org.jetbrains.kotlin.types.KotlinType

interface SwiftTypeScope {

    val KotlinType.swiftName: String
}
