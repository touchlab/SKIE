package co.touchlab.swiftpack.api

import org.jetbrains.kotlin.types.KotlinType

interface SwiftTypeScope {
    val KotlinType.swiftName: String
}
