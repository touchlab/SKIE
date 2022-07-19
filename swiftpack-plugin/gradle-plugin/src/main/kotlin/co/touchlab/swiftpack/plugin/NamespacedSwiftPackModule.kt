package co.touchlab.swiftpack.plugin

import co.touchlab.swiftpack.spec.SwiftPackModule

data class NamespacedSwiftPackModule(
    val namespace: String,
    val module: SwiftPackModule,
)