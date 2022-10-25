package co.touchlab.skie.plugin.api

import org.jetbrains.kotlin.types.KotlinType

interface SwiftTypeScope {
    val KotlinType.swiftName: String
}
