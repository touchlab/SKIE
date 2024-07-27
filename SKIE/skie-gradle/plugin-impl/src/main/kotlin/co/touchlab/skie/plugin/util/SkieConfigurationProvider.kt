package co.touchlab.skie.plugin.util

import co.touchlab.skie.util.directory.SkieBuildDirectory
import org.gradle.api.provider.Provider
import java.io.File

val Provider<SkieBuildDirectory>.skieConfiguration: Provider<File>
    get() = map { it.skieConfiguration }
