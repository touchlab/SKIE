object PluginCoordinates {
    const val ID = "co.touchlab.skie"
    const val GROUP = "co.touchlab.skie"
    const val ARTIFACT_ID = "skie-plugin"
    const val DEFAULT_VERSION = "1.0.0"
    const val IMPLEMENTATION_CLASS = "co.touchlab.swiftlink.plugin.SwiftLinkPlugin"

    val VERSION: String
        get() = System.getenv("RELEASE_VERSION") ?: DEFAULT_VERSION
}

object PluginBundle {
    const val VCS = "https://github.com/touchlab/SKIE"
    const val WEBSITE = "https://github.com/touchlab/SKIE"
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
