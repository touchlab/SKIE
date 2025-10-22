package co.touchlab.skie.configuration

sealed interface FileOrClassConfiguration {

    val configuration: SkieConfiguration

    data class File(override val configuration: FileConfiguration) : FileOrClassConfiguration

    data class Class(override val configuration: ClassConfiguration) : FileOrClassConfiguration
}
