package co.touchlab.swiftpack.spi

import co.touchlab.swiftpack.spec.SwiftPackModule
import java.io.File

data class NamespacedSwiftPackModule(
    val namespace: String,
    val module: SwiftPackModule,
) {
    data class Reference(
        val namespace: String,
        val moduleFile: File,
    )
}