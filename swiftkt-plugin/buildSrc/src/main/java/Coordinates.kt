object PluginCoordinates {
    const val ID = "co.touchlab.swiftkt"
    const val GROUP = "co.touchlab.swiftkt"
    const val ARTIFACT_ID = "swiftkt-plugin"
    const val DEFAULT_VERSION = "1.0.0"
    const val IMPLEMENTATION_CLASS = "co.touchlab.swiftkt.plugin.SwiftKtPlugin"

    val VERSION: String
        get() = System.getenv("RELEASE_VERSION") ?: DEFAULT_VERSION

    object Native {
        const val ID = "co.touchlab.swikt.native"
        const val IMPLEMENTATION_CLASS = "co.touchlab.swikt.plugin.SwiftKtNativePlugin"
    }
}

object PluginBundle {
    const val VCS = "https://github.com/Touchlab/SwiftKt"
    const val WEBSITE = "https://github.com/Touchlab/SwiftKt"
    const val DESCRIPTION = "A Gradle plugin to add Swift into Kotlin/Native framework."
    const val DISPLAY_NAME = "Swift and Kotlin, unified"
    val TAGS = listOf(
        "plugin",
        "gradle",
        "swift",
        "kotlin",
        "native",
    )
}

